package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Texture2D extends Transformable {
   public static final int FILTER_BASE_LEVEL = 208;
   public static final int FILTER_LINEAR = 209;
   public static final int FILTER_NEAREST = 210;
   public static final int FUNC_ADD = 224;
   public static final int FUNC_BLEND = 225;
   public static final int FUNC_DECAL = 226;
   public static final int FUNC_MODULATE = 227;
   public static final int FUNC_REPLACE = 228;
   public static final int WRAP_CLAMP = 240;
   public static final int WRAP_REPEAT = 241;
   private int m_blendColor;
   private int m_blending;
   private int m_wrappingS;
   private int m_wrappingT;
   private int m_levelFilter;
   private int m_imageFilter;
   private Image2D m_image;
   private boolean m_textureSet = false;
   int[] m_id = new int[1];

   public Texture2D(Image2D image) {
      this.setImage(image);
      this.m_wrappingS = 241;
      this.m_wrappingT = 241;
      this.m_levelFilter = 208;
      this.m_imageFilter = 210;
      this.m_blending = 227;
      this.m_blendColor = 0;
   }

   public void setImage(Image2D image) {
      if (image == null) {
         throw new NullPointerException("Texture2D: image is null");
      } else if (this.isPower2(image.getWidth()) && this.isPower2(image.getHeight())) {
         this.m_image = image;
         this.setTexture();
      } else {
         throw new IllegalArgumentException("Texture2D: the width or height of image is not a positive power of two (1, 2, 4, 8, 16, etc.)");
      }
   }

   public Image2D getImage() {
      return this.m_image;
   }

   public void setFiltering(int levelFilter, int imageFilter) {
      if (levelFilter >= 208 && levelFilter <= 210) {
         if (imageFilter >= 209 && imageFilter <= 210) {
            this.m_levelFilter = levelFilter;
            this.m_imageFilter = imageFilter;
         } else {
            throw new IllegalArgumentException("Texture2D: imageFilter is not one of FILTER_NEAREST, FILTER_LINEAR");
         }
      } else {
         throw new IllegalArgumentException("Texture2D: levelFilter is not one of FILTER_BASE_LEVEL, FILTER_NEAREST, FILTER_LINEAR");
      }
   }

   public int getImageFilter() {
      return this.m_imageFilter;
   }

   public int getLevelFilter() {
      return this.m_levelFilter;
   }

   public void setWrapping(int wrapS, int wrapT) {
      if (wrapS >= 240 && wrapS <= 241 && wrapT >= 240 && wrapT <= 241) {
         this.m_wrappingS = wrapS;
         this.m_wrappingT = wrapT;
      } else {
         throw new IllegalArgumentException("Texture2D: wrapS or wrapT is not one of WRAP_CLAMP, WRAP_REPEAT");
      }
   }

   public int getWrappingS() {
      return this.m_wrappingS;
   }

   public int getWrappingT() {
      return this.m_wrappingT;
   }

   public void setBlending(int func) {
      if (func >= 224 && func <= 228) {
         this.m_blending = func;
      } else {
         throw new IllegalArgumentException("Texture2D: func is not one of FUNC_REPLACE, FUNC_MODULATE, FUNC_DECAL, FUNC_BLEND, FUNC_ADD");
      }
   }

   public int getBlending() {
      return this.m_blending;
   }

   public void setBlendColor(int RGB) {
      this.m_blendColor = RGB;
   }

   public int getBlendColor() {
      return this.m_blendColor;
   }

   private boolean isPower2(int n) {
      for(int i = 1; i < 1024; i <<= 1) {
         if (n == i) {
            return true;
         }
      }

      return false;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_image != null) {
         if (references != null) {
            references[numReferences] = this.m_image;
         }

         ++numReferences;
      }

      return numReferences;
   }

   Texture2D() {
   }

   public int getObjectType() {
      return 17;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      M3GObject obj = this.getObjectAtIndex(table, index, 10);
      if (obj != null) {
         this.m_image = (Image2D)obj;
         this.setBlendColor(is.readColorRGB());
         this.setBlending(is.readByte());
         this.setWrapping(is.readByte(), is.readByte());
         this.setFiltering(is.readByte(), is.readByte());
      } else {
         throw new IOException("Texture2D:image-index = " + index);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_image);
      if (index > 0) {
         os.writeObjectIndex((long)index);
         os.writeColorRGB(this.m_blendColor);
         os.writeByte(this.m_blending);
         os.writeByte(this.m_wrappingS);
         os.writeByte(this.m_wrappingT);
         os.writeByte(this.m_levelFilter);
         os.writeByte(this.m_imageFilter);
      } else {
         throw new IOException("Texture2D:image-index = " + index);
      }
   }

   protected void buildReferenceTable(ArrayList table) {
      this.m_image.buildReferenceTable(table);
      super.buildReferenceTable(table);
   }

   void setupGL(GL gl, float[] scaleBias) {
      if (!this.m_textureSet) {
         this.setTexture();
      }

      gl.glEnable(3553);
      gl.glBindTexture(3553, this.m_id[0]);
      gl.glTexParameteri(3553, 10241, this.getGLFilter(this.m_imageFilter));
      gl.glTexParameteri(3553, 10240, this.getGLFilter(this.m_imageFilter));
      gl.glTexParameteri(3553, 10242, this.getGLWrap(this.m_wrappingS));
      gl.glTexParameteri(3553, 10243, this.getGLWrap(this.m_wrappingT));
      gl.glTexEnvi(8960, 8704, this.getGLBlend());
      gl.glTexEnvfv(8960, 8705, Color.intToFloatArray(this.m_blendColor), 0);
      Transform t = new Transform();
      this.getCompositeTransform(t);
      gl.glMatrixMode(5890);
      t.setGL(gl);
      gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
      gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);
      gl.glMatrixMode(5888);
   }

   int getGLFilter(int filter) {
      switch(filter) {
      case 209:
         return 9987;
      case 210:
         return 9986;
      default:
         return 9728;
      }
   }

   int getGLWrap(int wrap) {
      switch(wrap) {
      case 240:
         return 10496;
      default:
         return 10497;
      }
   }

   int getGLBlend() {
      switch(this.m_blending) {
      case 224:
         return 260;
      case 225:
         return 3042;
      case 226:
      default:
         return 8449;
      case 227:
         return 8448;
      case 228:
         return 7681;
      }
   }

   void setTexture() {
      GL gl = Graphics3D.getInstance().getGL();
      if (gl != null) {
         gl.glGenTextures(1, this.m_id, 0);
         gl.glBindTexture(3553, this.m_id[0]);
         ByteBuffer pixels = this.m_image.getPixels();
         Graphics3D.getInstance().getGLU().gluBuild2DMipmaps(3553, this.m_image.getBytesPerPixel(), this.m_image.getWidth(), this.m_image.getHeight(), this.m_image.getGLFormat(), 5121, pixels);
         gl.glTexImage2D(3553, 0, this.m_image.getBytesPerPixel(), this.m_image.getWidth(), this.m_image.getHeight(), 0, this.m_image.getGLFormat(), 5121, pixels);
         this.m_textureSet = true;
      }

   }
}
