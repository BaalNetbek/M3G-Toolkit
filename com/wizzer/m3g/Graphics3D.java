package com.wizzer.m3g;

import com.wizzer.m3g.lcdui.Graphics;
import com.wizzer.m3g.midp.MIDPEmulator;
import com.wizzer.m3g.midp.Reflection;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

public final class Graphics3D {
   static final int MAX_LIGHT_COUNT = 8;
   public static final int ANTIALIAS = 0;
   public static final int DITHER = 0;
   public static final int OVERWRITE = 0;
   public static final int TRUE_COLOR = 0;
   private static Graphics3D m_instance = null;
   private int m_numTextureUnits = 8;
   private int m_viewportX = 0;
   private int m_viewportY = 0;
   private int m_viewportWidth = 0;
   private int m_viewportHeight = 0;
   private GL m_gl = null;
   private GLU m_glu = null;
   private Object m_renderTarget = null;
   private GLCanvas m_currentGLCanvas;
   private RenderEventListener m_currentRenderListener;
   private boolean m_depthBufferEnabled = true;
   private int m_hints;
   private Camera m_camera;
   private Transform m_cameraTransform;
   private ArrayList m_lights = new ArrayList();
   private CompositingMode m_defaultCompositingMode = new CompositingMode();
   private PolygonMode m_defaultPolygonMode = new PolygonMode();

   private Graphics3D() {
   }

   public static Graphics3D getInstance() {
      if (m_instance == null) {
         m_instance = new Graphics3D();
      }

      return m_instance;
   }

   public void bindTarget(Object target, RenderEventListener renderListener) throws NullPointerException, IllegalStateException, IllegalArgumentException {
      if (target == null) {
         throw new NullPointerException("target is null");
      } else if (this.m_renderTarget != null) {
         throw new IllegalStateException("rendering target already specified");
      } else {
         if (target instanceof Image2D) {
            Image2D imageTarget = (Image2D)target;
            if (!imageTarget.isMutable()) {
               throw new IllegalArgumentException("target is not a mutable Image2D");
            }

            if (imageTarget.getGLFormat() != 99 || imageTarget.getGLFormat() != 100) {
               throw new IllegalArgumentException("internal format must be RGB or RGBA");
            }
         } else if (!(target instanceof Graphics)) {
            throw new IllegalArgumentException("target is not a Image2D or Graphics");
         }

         if (target instanceof Graphics) {
            this.m_renderTarget = target;
            this.m_currentRenderListener = renderListener;
            GLCanvas canvas = MIDPEmulator.getInstance().getRenderTarget((Graphics)target);
            this.m_currentGLCanvas = canvas;
            canvas.addGLEventListener(renderListener);
            canvas.display();
         }

      }
   }

   public void bindTarget(Object target) throws NullPointerException, IllegalStateException, IllegalArgumentException {
      if (target == null) {
         throw new NullPointerException("target is null");
      } else if (this.m_renderTarget != null) {
         throw new IllegalStateException("rendering target already specified");
      } else {
         if (target instanceof Image2D) {
            Image2D imageTarget = (Image2D)target;
            if (!imageTarget.isMutable()) {
               throw new IllegalArgumentException("target is not a mutable Image2D");
            }

            if (imageTarget.getGLFormat() != 99 || imageTarget.getGLFormat() != 100) {
               throw new IllegalArgumentException("internal format must be RGB or RGBA");
            }
         } else if (!(target instanceof Graphics)) {
            throw new IllegalArgumentException("target is not a Image2D or Graphics");
         }

         if (target instanceof Graphics) {
            this.m_renderTarget = target;
            GLCanvas canvas = MIDPEmulator.getInstance().getRenderTarget((Graphics)target);
            this.m_currentGLCanvas = canvas;
            this.setGL(canvas.getGL());
            int contextStatus = canvas.getContext().makeCurrent();
            if (this.m_viewportHeight == 0 || this.m_viewportWidth == 0) {
               this.setViewport(0, 0, canvas.getWidth(), canvas.getHeight());
            }
         }

      }
   }

   public void bindTarget(Object target, boolean depthBuffer, int hints) throws NullPointerException, IllegalStateException, IllegalArgumentException {
      int bitmask = 0;
      if (hints != 0 && (hints & ~bitmask) != 0) {
         throw new IllegalArgumentException("invalid hints");
      } else {
         this.bindTarget(target, (RenderEventListener)null);
         this.m_depthBufferEnabled = depthBuffer;
         this.m_hints = hints;
      }
   }

   public Object getTarget() {
      return this.m_renderTarget;
   }

   public void releaseTarget() {
      if (this.m_currentGLCanvas != null) {
         if (this.m_currentRenderListener != null) {
            this.m_currentGLCanvas.removeGLEventListener(this.m_currentRenderListener);
            this.m_currentRenderListener = null;
         } else {
            GLDrawable d = (GLDrawable)Reflection.getField(this.m_currentGLCanvas, "drawable");
            d.swapBuffers();
         }
      }

      this.m_currentGLCanvas = null;
      this.m_renderTarget = null;
   }

   public void clear(Background background) throws IllegalArgumentException, IllegalStateException {
      if (this.m_renderTarget == null) {
         throw new IllegalStateException("no rendering target");
      } else {
         if (background != null && this.m_renderTarget instanceof Image2D) {
            int imageFormat = ((Image2D)this.m_renderTarget).getFormat();
            Image2D backgroundImage = background.getImage();
            if (imageFormat != backgroundImage.getFormat()) {
               throw new IllegalArgumentException("invalid background image format");
            }
         }

         if (background != null) {
            background.setupGL(this.m_gl);
         } else {
            this.m_gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.m_gl.glClear(16640);
         }

      }
   }

   public int addLight(Light light, Transform transform) throws NullPointerException {
      if (light == null) {
         throw new NullPointerException("light must not be null");
      } else {
         this.m_lights.add(light);
         int index = this.m_lights.size() - 1;
         if (index < 8) {
            this.m_gl.glPushMatrix();
            transform.multGL(this.m_gl);
            light.setupGL(this.m_gl);
            this.m_gl.glPopMatrix();
         }

         return index;
      }
   }

   public void setLight(int index, Light light, Transform transform) throws IndexOutOfBoundsException {
      if (index >= 0 && index < this.getLightCount()) {
         this.m_lights.set(index, light);
      } else {
         throw new IndexOutOfBoundsException("index out of range");
      }
   }

   public void resetLights() {
      this.m_lights.clear();

      for(int i = 0; i < 8; ++i) {
         this.m_gl.glDisable(16384 + i);
      }

   }

   public int getLightCount() {
      return 8;
   }

   public Light getLight(int index, Transform transform) throws IndexOutOfBoundsException {
      if (index >= 0 && index < this.getLightCount()) {
         Light light;
         if (index < this.m_lights.size()) {
            light = (Light)this.m_lights.get(index);
         } else {
            light = null;
         }

         return light;
      } else {
         throw new IndexOutOfBoundsException("index out of range");
      }
   }

   public int getHints() {
      return this.m_hints;
   }

   public boolean isDepthBufferEnabled() {
      return this.m_depthBufferEnabled;
   }

   public void setViewport(int x, int y, int width, int height) throws IllegalArgumentException {
      if (width >= 0 && height > 0) {
         this.m_viewportX = x;
         this.m_viewportY = y;
         this.m_viewportWidth = width;
         this.m_viewportHeight = height;
         this.m_gl.glViewport(x, y, width, height);
      } else {
         throw new IllegalArgumentException("invalid width or height");
      }
   }

   public int getViewportX() {
      return this.m_viewportX;
   }

   public int getViewportY() {
      return this.m_viewportY;
   }

   public int getViewportWidth() {
      return this.m_viewportWidth;
   }

   public int getViewportHeight() {
      return this.m_viewportHeight;
   }

   public void setDepthRange(float near, float far) throws IllegalArgumentException {
      if (!(near < 0.0F) && !(near > 1.0F)) {
         if (far < 0.0F || far > 1.0F) {
            throw new IllegalArgumentException("far value out of range");
         }
      } else {
         throw new IllegalArgumentException("near value out of range");
      }
   }

   public float getDepthRangeNear() {
      return 0.0F;
   }

   public float getDepthRangeFar() {
      return 0.0F;
   }

   public void setCamera(Camera camera, Transform transform) throws ArithmeticException {
      this.m_camera = camera;
      this.m_cameraTransform = transform;
      Transform t = new Transform();
      this.m_gl.glMatrixMode(5889);
      camera.getProjection(t);
      t.setGL(this.m_gl);
      this.m_gl.glMatrixMode(5888);
      t.set(transform);
      t.invert();
      t.setGL(this.m_gl);
   }

   public Camera getCamera(Transform transform) {
      if (transform != null) {
         transform.set(this.m_cameraTransform);
      }

      return this.m_camera;
   }

   public void render(Node node, Transform transform) throws NullPointerException, IllegalArgumentException, IllegalStateException {
      if (node == null) {
         throw new NullPointerException("node == null");
      } else if (this.m_renderTarget == null) {
         throw new IllegalStateException("no render target");
      } else if (this.m_camera == null) {
         throw new IllegalStateException("no current camera");
      } else {
         if (node instanceof Mesh) {
            Mesh mesh = (Mesh)node;
            int subMeshes = mesh.getSubmeshCount();
            VertexBuffer vertices = mesh.getVertexBuffer();

            for(int i = 0; i < subMeshes; ++i) {
               this.render(vertices, mesh.getIndexBuffer(i), mesh.getAppearance(i), transform);
            }
         } else if (node instanceof Sprite3D) {
            Sprite3D sprite = (Sprite3D)node;
            sprite.render(this.m_gl, transform);
         } else {
            boolean var10000 = node instanceof Group;
         }

      }
   }

   public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform) throws NullPointerException, IllegalStateException {
      if (vertices == null) {
         throw new NullPointerException("vertices == null");
      } else if (triangles == null) {
         throw new NullPointerException("triangles == null");
      } else if (appearance == null) {
         throw new NullPointerException("appearance == null");
      } else if (this.m_renderTarget == null) {
         throw new IllegalStateException("no render target");
      } else if (this.m_camera == null) {
         throw new IllegalStateException("no current camera");
      } else {
         float[] scaleBias = new float[4];
         VertexArray positions = vertices.getPositions(scaleBias);
         FloatBuffer pos = positions.getFloatBuffer();
         pos.position(0);
         this.m_gl.glVertexPointer(positions.getComponentCount(), 5126, 0, pos);
         this.m_gl.glEnableClientState(32884);
         VertexArray normals = vertices.getNormals();
         if (normals != null) {
            FloatBuffer norm = normals.getFloatBuffer();
            norm.position(0);
            this.m_gl.glEnable(2977);
            this.m_gl.glNormalPointer(5126, 0, norm);
            this.m_gl.glEnableClientState(32885);
         } else {
            this.m_gl.glDisable(2977);
            this.m_gl.glDisableClientState(32885);
         }

         VertexArray colors = vertices.getColors();
         if (colors != null) {
            Buffer buffer = colors.getBuffer();
            buffer.position(0);
            this.m_gl.glColorPointer(colors.getComponentCount(), colors.getComponentTypeGL(), 0, buffer);
            this.m_gl.glEnableClientState(32886);
         } else {
            this.m_gl.glDisableClientState(32886);
         }

         for(int i = 0; i < 8; ++i) {
            float[] texScaleBias = new float[4];
            VertexArray texcoords = vertices.getTexCoords(i, texScaleBias);
            this.m_gl.glActiveTexture('蓀' + i);
            this.m_gl.glClientActiveTexture('蓀' + i);
            if (texcoords != null) {
               FloatBuffer tex = texcoords.getFloatBuffer();
               tex.position(0);
               if (appearance.getTexture(i) != null) {
                  appearance.getTexture(i).setupGL(this.m_gl, texScaleBias);
               } else {
                  this.m_gl.glDisable(3553);
               }

               this.m_gl.glTexCoordPointer(texcoords.getComponentCount(), 5126, 0, tex);
               this.m_gl.glEnableClientState(32888);
            } else {
               this.m_gl.glDisable(3553);
               this.m_gl.glDisableClientState(32888);
            }
         }

         this.setAppearance(appearance);
         this.m_gl.glPushMatrix();
         transform.multGL(this.m_gl);
         this.m_gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
         this.m_gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);
         if (triangles instanceof TriangleStripArray) {
            IntBuffer indices = triangles.getBuffer();
            indices.position(0);
            this.m_gl.glDrawElements(5, triangles.getIndexCount(), 5125, indices);
         } else {
            this.m_gl.glDrawElements(4, triangles.getIndexCount(), 5125, triangles.getBuffer());
         }

         this.m_gl.glPopMatrix();
      }
   }

   public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform, int scope) throws NullPointerException, IllegalArgumentException, IllegalStateException {
      this.render(vertices, triangles, appearance, transform);
   }

   public void render(World world) throws NullPointerException, IllegalStateException {
      if (world == null) {
         throw new NullPointerException("world == null");
      } else if (this.m_renderTarget == null) {
         throw new IllegalStateException("no render target");
      } else {
         this.clear(world.getBackground());
         Transform t = new Transform();
         Camera c = world.getActiveCamera();
         if (c == null) {
            throw new IllegalStateException("World has no active camera.");
         } else if (!c.getTransformTo(world, t)) {
            throw new IllegalStateException("Camera is not in world.");
         } else {
            this.setCamera(c, t);
            this.resetLights();
            this.populateLights(world, world);
            this.renderDescendants(world, world);
         }
      }
   }

   private void populateLights(World world, Object3D obj) {
      int numReferences = obj.getReferences((Object3D[])null);
      if (numReferences > 0) {
         Object3D[] objArray = new Object3D[numReferences];
         obj.getReferences(objArray);

         for(int i = 0; i < numReferences; ++i) {
            if (objArray[i] instanceof Light) {
               Transform t = new Transform();
               Light light = (Light)objArray[i];
               if (light.isRenderingEnabled() && light.getTransformTo(world, t)) {
                  this.addLight(light, t);
               }
            }

            this.populateLights(world, objArray[i]);
         }
      }

   }

   private void renderDescendants(World world, Object3D obj) {
      int numReferences = obj.getReferences((Object3D[])null);
      if (numReferences > 0) {
         Object3D[] objArray = new Object3D[numReferences];
         obj.getReferences(objArray);

         for(int i = 0; i < numReferences; ++i) {
            if (objArray[i] instanceof Node) {
               Transform t = new Transform();
               Node node = (Node)objArray[i];
               node.getTransformTo(world, t);
               this.render(node, t);
            }

            this.renderDescendants(world, objArray[i]);
         }
      }

   }

   void setAppearance(Appearance appearance) {
      if (appearance == null) {
         throw new NullPointerException("appearance must not be null");
      } else {
         PolygonMode polyMode = appearance.getPolygonMode();
         if (polyMode == null) {
            polyMode = this.m_defaultPolygonMode;
         }

         polyMode.setupGL(this.m_gl);
         if (appearance.getMaterial() != null) {
            appearance.getMaterial().setupGL(this.m_gl, polyMode.getLightTarget());
         } else {
            this.m_gl.glDisable(2896);
         }

         if (appearance.getFog() != null) {
            appearance.getFog().setupGL(this.m_gl);
         } else {
            this.m_gl.glDisable(2912);
         }

         if (appearance.getCompositingMode() != null) {
            appearance.getCompositingMode().setupGL(this.m_gl);
         } else {
            this.m_defaultCompositingMode.setupGL(this.m_gl);
         }

      }
   }

   void setGL(GL gl) {
      this.m_gl = gl;
   }

   GL getGL() {
      return this.m_gl != null ? this.m_gl : MIDPEmulator.getInstance().getGL();
   }

   GLU getGLU() {
      if (this.m_glu == null) {
         this.m_glu = new GLU();
      }

      return this.m_glu;
   }

   int getTextureUnitCount() {
      return this.m_numTextureUnits;
   }

   void disableTextureUnits() {
      for(int i = 0; i < this.m_numTextureUnits; ++i) {
         this.m_gl.glActiveTexture('蓀' + i);
         this.m_gl.glDisable(3553);
      }

   }
}
