package com.wizzer.m3g.toolkit.util;

import com.wizzer.m3g.AnimationController;
import com.wizzer.m3g.AnimationTrack;
import com.wizzer.m3g.Appearance;
import com.wizzer.m3g.Background;
import com.wizzer.m3g.Camera;
import com.wizzer.m3g.CompositingMode;
import com.wizzer.m3g.ExternalReference;
import com.wizzer.m3g.ExternalReferencesSection;
import com.wizzer.m3g.Fog;
import com.wizzer.m3g.Group;
import com.wizzer.m3g.HeaderObject;
import com.wizzer.m3g.HeaderSection;
import com.wizzer.m3g.Image2D;
import com.wizzer.m3g.IndexBuffer;
import com.wizzer.m3g.KeyframeSequence;
import com.wizzer.m3g.Light;
import com.wizzer.m3g.M3GFile;
import com.wizzer.m3g.Material;
import com.wizzer.m3g.Mesh;
import com.wizzer.m3g.MorphingMesh;
import com.wizzer.m3g.Node;
import com.wizzer.m3g.Object3D;
import com.wizzer.m3g.PolygonMode;
import com.wizzer.m3g.SceneSection;
import com.wizzer.m3g.Section;
import com.wizzer.m3g.SkinnedMesh;
import com.wizzer.m3g.Sprite3D;
import com.wizzer.m3g.Texture2D;
import com.wizzer.m3g.Transform;
import com.wizzer.m3g.Transformable;
import com.wizzer.m3g.TriangleStripArray;
import com.wizzer.m3g.VertexArray;
import com.wizzer.m3g.VertexBuffer;
import com.wizzer.m3g.World;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class DumpM3g {
   protected M3GFile m_m3gFile = null;
   protected PrintStream m_out = null;
   protected HeaderSection m_header = null;
   protected boolean m_verbose = false;

   private DumpM3g() {
   }

   public DumpM3g(M3GFile file) {
      this.m_m3gFile = file;
   }

   public void setVerbose(boolean flag) {
      this.m_verbose = flag;
   }

   public void setOutputStream(OutputStream stream) {
      if (stream == null) {
         this.m_out = null;
      } else {
         this.m_out = new PrintStream(stream);
      }

   }

   public void dumpHeaderSection() throws IOException {
      this.m_header = this.m_m3gFile.getHeaderSection();
      this.dumpHeaderInfo(this.m_header);
   }

   protected void dumpSectionInfo(Section section) {
      this.println("************************************************************");
      this.println("******************* Section Information ********************");
      this.println("************************************************************");
      this.println("Compression Scheme: " + section.getCompressionScheme());
      byte[] data = this.intToByteArray(section.getTotalSectionLength());
      this.println("Total Section Length: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray(section.getUncompressedLength());
      this.println("Uncompressed Length: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray(section.getChecksum());
      this.println("Checksum: " + Unsigned.readDWORD(data, 0) + " (0x" + Integer.toHexString(section.getChecksum()) + ")");
   }

   protected void dumpHeaderInfo(HeaderSection header) {
      if (this.m_verbose) {
         this.dumpSectionInfo(header);
      }

      HeaderObject hObject = header.getHeaderObject();
      this.println("************************************************************");
      this.println("******************** Header Information ********************");
      this.println("************************************************************");
      this.println("Version Major Number: " + hObject.getVersionMajor());
      this.println("Version Minor Number: " + hObject.getVersionMinor());
      this.println("Has External References: " + hObject.isHasExternalReferences());
      byte[] data = this.intToByteArray((int)hObject.getTotalFileSize());
      this.println("Total File Size: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray((int)hObject.getApproximateContentSize());
      this.println("Approximate Content Size: " + Unsigned.readDWORD(data, 0));
      this.println("Authoring Field: " + hObject.getAuthoringField());
      this.println("************************************************************");
   }

   public void dumpExternalReferenceSection() {
      if (this.m_header.getHeaderObject().isHasExternalReferences()) {
         ExternalReferencesSection reference = this.m_m3gFile.getExternalReferencesSection();
         if (this.m_verbose) {
            this.dumpSectionInfo(reference);
         }

         ExternalReference[] refs = reference.getExternalReferences();
         this.println("************************************************************");
         this.println("******************* External References ********************");
         this.println("************************************************************");
         this.println("Current Working Directory: " + ExternalReference.getCwd());

         for(int i = 0; i < refs.length; ++i) {
            String uri = refs[i].getURI();
            this.println("Reference " + i + ": " + uri);
         }

         this.println("************************************************************");
      }
   }

   public void dumpSceneSections() {
      SceneSection[] scenes = this.m_m3gFile.getSceneSections();
      this.println("");
      this.println("##### Number of Scene Sections: " + scenes.length);

      for(int i = 0; i < scenes.length; ++i) {
         this.println("##### Scene Section " + i);
         if (this.m_verbose) {
            this.dumpSectionInfo(scenes[i]);
         }

         Object3D[] objects = scenes[i].getObjects3D();

         for(int j = 0; j < objects.length; ++j) {
            this.dumpObject(objects[j]);
         }
      }

   }

   protected void dumpObject(Object3D object) {
      this.println("");
      this.println("************************************************************");
      this.println("******************* Scene Section Object *******************");
      this.println("************************************************************");
      this.println("Instance Identifier: " + object);
      int type = object.getObjectType();
      switch(type) {
      case 1:
         this.println("Object: AnimationController");
         this.dumpAnimationController((AnimationController)object);
         break;
      case 2:
         this.println("Object: AnimationTrack");
         this.dumpAnimationTrack((AnimationTrack)object);
         break;
      case 3:
         this.println("Object: Appearance");
         this.dumpAppearance((Appearance)object);
         break;
      case 4:
         this.println("Object: Background");
         this.dumpBackground((Background)object);
         break;
      case 5:
         this.println("Object: Camera");
         this.dumpCamera((Camera)object);
         break;
      case 6:
         this.println("Object: CompositingMode");
         this.dumpCompositingMode((CompositingMode)object);
         break;
      case 7:
         this.println("Object: Fog");
         this.dumpFog((Fog)object);
         break;
      case 8:
         this.println("Object: PolygonMode");
         this.dumpPolygonMode((PolygonMode)object);
         break;
      case 9:
         this.println("Object: Group");
         this.dumpGroup((Group)object);
         break;
      case 10:
         this.println("Object: Image2D");
         this.dumpImage2D((Image2D)object);
         break;
      case 11:
         this.println("Object: TriangleStripArray");
         this.dumpTriangleStripArray((TriangleStripArray)object);
         break;
      case 12:
         this.println("Object: Light");
         this.dumpLight((Light)object);
         break;
      case 13:
         this.println("Object: Material");
         this.dumpMaterial((Material)object);
         break;
      case 14:
         this.println("Object: Mesh");
         this.dumpMesh((Mesh)object);
         break;
      case 15:
         this.println("Object: MorphingMesh");
         this.dumpMorphingMesh((MorphingMesh)object);
         break;
      case 16:
         this.println("Object: SkinnedMesh");
         this.dumpSkinnedMesh((SkinnedMesh)object);
         break;
      case 17:
         this.println("Object: Texture2D");
         this.dumpTexture2D((Texture2D)object);
         break;
      case 18:
         this.println("Object: Sprite3D");
         this.dumpSprite3D((Sprite3D)object);
         break;
      case 19:
         this.println("Object: KeyFrameSequence");
         this.dumpKeyframeSequence((KeyframeSequence)object);
         break;
      case 20:
         this.println("Object: VertexArray");
         this.dumpVertexArray((VertexArray)object);
         break;
      case 21:
         this.println("Object: VertexBuffer");
         this.dumpVertexBuffer((VertexBuffer)object);
         break;
      case 22:
         this.println("Object: World");
         this.dumpWorld((World)object);
         break;
      default:
         this.println("Object: UKNOWN = " + type);
      }

      this.println("************************************************************");
   }

   protected void dumpAnimationController(AnimationController controller) {
      this.dumpObject3D(controller);
      this.println("Speed: " + controller.getSpeed());
      this.println("Weight: " + controller.getWeight());
      this.println("Active Interval Start: " + controller.getActiveIntervalStart());
      this.println("Active Interval end: " + controller.getActiveIntervalEnd());
      this.println("Reference Sequence Time: " + controller.getRefSequenceTime());
      this.println("Reference World Time: " + controller.getRefWorldTime());
   }

   protected void dumpAnimationTrack(AnimationTrack track) {
      this.dumpObject3D(track);
      this.println("Keyframe Sequence Reference: " + track.getKeyframeSequence());
      this.println("Animation Controller Reference: " + track.getController());
      byte[] data = this.intToByteArray(track.getTargetProperty());
      this.println("Property ID: " + Unsigned.readDWORD(data, 0));
   }

   protected void dumpAppearance(Appearance appearance) {
      this.dumpObject3D(appearance);
      int layer = appearance.getLayer();
      this.println("Layer: " + layer);
      CompositingMode cmode = appearance.getCompositingMode();
      this.println("Compositing Mode Reference: " + cmode);
      Fog fog = appearance.getFog();
      this.println("Fog Reference: " + fog);
      PolygonMode pmode = appearance.getPolygonMode();
      this.println("Polygon Mode Reference: " + pmode);
      Material material = appearance.getMaterial();
      this.println("Material Reference: " + material);
      Texture2D textures = appearance.getTexture(0);
   }

   protected void dumpBackground(Background background) {
      this.dumpObject3D(background);
      int color = background.getColor();
      this.println("Color: 0x" + Integer.toHexString(color));
      this.println("Background Image Reference: " + background.getImage());
      this.println("Background Image Mode x: " + background.getImageModeX());
      this.println("Background Image Mode y: " + background.getImageModeY());
      this.println("Crop x Location: " + background.getCropX());
      this.println("Crop y Location: " + background.getCropY());
      this.println("Crop Width: " + background.getCropWidth());
      this.println("Crop Height: " + background.getCropHeight());
      this.println("Depth Clear Enabled: " + background.isDepthClearEnabled());
      this.println("Color Clear Enabled: " + background.isColorClearEnabled());
   }

   protected void dumpCamera(Camera camera) {
      this.dumpNode(camera);
      float[] params = new float[4];
      int type = camera.getProjection(params);
      switch(type) {
      case 48:
         this.println("Projection Type: Generic");
         break;
      case 49:
         this.println("Projection Type: Parallel");
         this.println("fovy: " + params[0]);
         this.println("aspectRatio: " + params[1]);
         this.println("near: " + params[2]);
         this.println("far: " + params[3]);
         break;
      case 50:
         this.println("Projection Type: Perspective");
         this.println("fovy: " + params[0]);
         this.println("aspectRatio: " + params[1]);
         this.println("near: " + params[2]);
         this.println("far: " + params[3]);
         break;
      default:
         this.println("Projection Type: UKNOWN = " + type);
      }

   }

   protected void dumpCompositingMode(CompositingMode mode) {
      this.dumpObject3D(mode);
      this.println("Depth Test Enabled: " + mode.isDepthTestEnabled());
      this.println("Depth Write Enabled: " + mode.isDepthWriteEnabled());
      this.println("Color Write Enabled: " + mode.isColorWriteEnabled());
      this.println("Alpha Write Enabled: " + mode.isAlphaWriteEnabled());
      this.println("Blending: " + mode.getBlending());
      this.println("Alpha Threshold: " + mode.getAlphaThreshold());
      this.println("Depth Offset Factor: " + mode.getDepthOffsetFactor());
      this.println("Depth Offset Units: " + mode.getDepthOffsetUnits());
   }

   protected void dumpFog(Fog fog) {
      this.dumpObject3D(fog);
      int color = fog.getColor();
      this.println("Color: 0x" + Integer.toHexString(color));
      int mode = fog.getMode();
      this.println("Mode: " + mode);
      if (mode == 80) {
         this.println("Density: " + fog.getDensity());
      } else if (mode == 81) {
         this.println("Near: " + fog.getNearDistance());
         this.println("Far: " + fog.getFarDistance());
      } else {
         this.println("Mode Type UNKNOWN");
      }

   }

   protected void dumpGroup(Group group) {
      this.dumpNode(group);
      this.println("####### Group Information ######");
      int count = group.getChildCount();
      this.println("Number of Children: " + count);

      for(int i = 0; i < count; ++i) {
         Node child = group.getChild(i);
         this.println("Child Node Reference: " + child);
      }

      this.println("################################");
   }

   protected void dumpImage2D(Image2D image) {
      this.dumpObject3D(image);
      this.println("Format: " + image.getFormat());
      boolean isMutable = image.isMutable();
      this.println("Is Mutable: " + isMutable);
      byte[] data = this.intToByteArray(image.getWidth());
      this.println("Width: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray(image.getHeight());
      this.println("Height: " + Unsigned.readDWORD(data, 0));
      if (isMutable) {
         BufferedImage var4 = image.getImage();
      }

   }

   protected void dumpKeyframeSequence(KeyframeSequence sequence) {
      this.dumpObject3D(sequence);
      this.println("Interpolation: " + sequence.getInterpolationType());
      this.println("Repeat Mode: " + sequence.getRepeatMode());
      int encoding = sequence.getEncoding();
      this.println("Encoding: " + encoding);
      byte[] data = this.intToByteArray(sequence.getDuration());
      this.println("Duration: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray(sequence.getValidRangeFirst());
      this.println("Valid Range First: " + Unsigned.readDWORD(data, 0));
      data = this.intToByteArray(sequence.getValidRangeLast());
      this.println("Valid Range Last: " + Unsigned.readDWORD(data, 0));
      int componentCount = sequence.getComponentCount();
      data = this.intToByteArray(componentCount);
      this.println("Component Count: " + Unsigned.readDWORD(data, 0));
      int keyframeCount = sequence.getKeyframeCount();
      data = this.intToByteArray(keyframeCount);
      this.println("Keyframe Count " + Unsigned.readDWORD(data, 0));
      if (encoding == 0) {
         for(int i = 0; i < keyframeCount; ++i) {
            this.println("Key Frame " + i + ": ");
            int time = sequence.getTimes()[i];
            data = this.intToByteArray(time);
            this.println("\tDuration: " + Unsigned.readDWORD(data, 0));

            for(int j = 0; j < componentCount; ++j) {
               this.println("\tVector Value: " + sequence.getKeyFrames()[j]);
            }
         }
      } else if (encoding != 1) {
      }

   }

   protected void dumpLight(Light light) {
      this.dumpNode(light);
      this.println("Attenuation Constant: " + light.getConstantAttenuation());
      this.println("Attenuation Linear: " + light.getLinearAttenuation());
      this.println("Attenuation Quadratic: " + light.getQuadraticAttenuation());
      int color = light.getColor();
      this.println("Color: 0x" + Integer.toHexString(color));
      this.println("Mode: " + light.getMode());
      this.println("Intensity: " + light.getIntensity());
      this.println("Spot Angle:" + light.getSpotAngle());
      this.println("Spot Exponent:" + light.getSpotExponent());
   }

   protected void dumpMaterial(Material material) {
      this.dumpObject3D(material);
      int ambient = material.getColor(1024);
      this.println("Ambient Color: 0x" + Integer.toHexString(ambient));
      int diffuse = material.getColor(2048);
      this.println("Diffuse Color: 0x" + Integer.toHexString(diffuse));
      int emissive = material.getColor(4096);
      this.println("Emissive Color: 0x" + Integer.toHexString(emissive));
      int specular = material.getColor(8192);
      this.println("Specular Color: 0x" + Integer.toHexString(specular));
      this.println("Shininess: " + material.getShininess());
      this.println("Vertex Color Tracking Enabled: " + material.isVertexColorTrackingEnabled());
   }

   protected void dumpMesh(Mesh mesh) {
      this.dumpNode(mesh);
      VertexBuffer buffer = mesh.getVertexBuffer();
      this.println("Vertex Buffer Reference: " + buffer);
      int count = mesh.getSubmeshCount();
      this.println("Submesh Count: " + count);

      for(int i = 0; i < count; ++i) {
         IndexBuffer indexBuffer = mesh.getIndexBuffer(i);
         this.println("Submesh " + i + " IndexBuffer Reference: " + indexBuffer);
         Appearance appearance = mesh.getAppearance(i);
         this.println("Submesh " + i + " Appearance Reference: " + appearance);
      }

   }

   protected void dumpMorphingMesh(MorphingMesh mesh) {
      this.dumpMesh(mesh);
      int count = mesh.getMorphTargetCount();
      this.println("Morph Target count: " + count);
      float[] weights = new float[count];
      mesh.getWeights(weights);

      for(int i = 0; i < count; ++i) {
         VertexBuffer morphTarget = mesh.getMorphTarget(i);
         this.println("Morph Target " + i + ": " + morphTarget);
         this.println("Initial Weight: " + weights[i]);
      }

   }

   protected void dumpSkinnedMesh(SkinnedMesh mesh) {
      this.dumpMesh(mesh);
      this.println("Skeleton: " + mesh.getSkeleton());
      int count = mesh.getTransformReferenceCount();
      this.println("Transform Reference Count: " + count);

      for(int i = 0; i < count; ++i) {
         Node bone = mesh.getTransformNode(i);
         this.println("Transform Node: " + bone);
         this.println("First Vertex: " + mesh.getFirstVertex(i));
         this.println("Vertex Count: " + mesh.getVertexCount(i));
         this.println("Weight: " + mesh.getWeight(i));
      }

   }

   protected void dumpNode(Node node) {
      this.dumpTransformable(node);
      this.println("####### Node Information #######");
      this.println("Enable Rendering: " + node.isRenderingEnabled());
      this.println("Enable Picking: " + node.isPickingEnabled());
      this.println("Alpha Factor: " + node.getAlphaFactor());
      byte[] data = this.intToByteArray(node.getScope());
      this.println("Scope: " + Unsigned.readDWORD(data, 0));
      this.println("Algnment: " + node.hasAlignment());
      if (node.hasAlignment()) {
         this.println("z Target: " + node.getZTarget());
         this.println("y Target: " + node.getYTarget());
         this.println("z Reference: " + node.getZReference());
         this.println("y Reference: " + node.getYReference());
      }

      this.println("################################");
   }

   protected void dumpObject3D(Object3D obj) {
      this.println("##### Object3D Information #####");
      this.println("User Identifier: " + obj.getUserID());
      int trackCount = obj.getAnimationTrackCount();
      this.println("Number of Animation Tracks: " + trackCount);

      for(int i = 0; i < trackCount; ++i) {
         AnimationTrack track = obj.getAnimationTrack(i);
         this.println("Animation Track " + i + " Reference: " + track);
      }

      this.println("################################");
   }

   protected void dumpPolygonMode(PolygonMode mode) {
      this.dumpObject3D(mode);
      this.println("Culling: " + mode.getCulling());
      this.println("Shading: " + mode.getShading());
      this.println("Winding: " + mode.getWinding());
      this.println("Two-Sided Lighting Enabled: " + mode.isTwoSidedLightingEnabled());
      this.println("Local Camera Lighting Enabled: " + mode.isLocalCameraLightingEnabled());
      this.println("Perspective Correction Enabled: " + mode.isPerspectiveCorrectionEnabled());
   }

   protected void dumpSprite3D(Sprite3D sprite) {
      this.dumpNode(sprite);
   }

   protected void dumpTransformable(Transformable trans) {
      this.dumpObject3D(trans);
      this.println("## Transformable Information ###");
      this.println("Has Component Transform: " + trans.hasComponentTransform());
      this.println("Has General Transform: " + trans.hasGeneralTransform());
      if (trans.hasComponentTransform()) {
         float[] translation = new float[3];
         trans.getTranslation(translation);
         StringBuffer buffer = new StringBuffer("x = ");
         buffer.append(translation[0]);
         buffer.append("; y = ");
         buffer.append(translation[1]);
         buffer.append("; z = ");
         buffer.append(translation[2]);
         this.println("Translation: " + buffer.toString());
         float[] scale = new float[3];
         trans.getScale(scale);
         buffer = new StringBuffer("x = ");
         buffer.append(scale[0]);
         buffer.append("; y = ");
         buffer.append(scale[1]);
         buffer.append("; z = ");
         buffer.append(scale[2]);
         this.println("Scale: " + buffer.toString());
         float[] angleAxis = new float[4];
         trans.getOrientation(angleAxis);
         buffer = new StringBuffer();
         buffer.append(angleAxis[0]);
         this.println("Orientation Angle: " + buffer.toString());
         angleAxis = new float[4];
         trans.getOrientation(angleAxis);
         buffer = new StringBuffer("x = ");
         buffer.append(angleAxis[1]);
         buffer.append("; y = ");
         buffer.append(angleAxis[2]);
         buffer.append("; z = ");
         buffer.append(angleAxis[3]);
         this.println("Orientation Axis: " + buffer.toString());
      }

      if (trans.hasGeneralTransform()) {
         Transform transform = new Transform();
         trans.getTransform(transform);
         float[] matrix = new float[16];
         transform.get(matrix);
         StringBuffer buffer = new StringBuffer("[ ");

         for(int i = 0; i < 16; ++i) {
            buffer.append(matrix[i]);
            buffer.append(" ");
         }

         buffer.append("]");
         this.println("General Transform: " + buffer.toString());
      }

      this.println("################################");
   }

   protected void dumpTexture2D(Texture2D texture) {
      this.dumpTransformable(texture);
      Image2D image = texture.getImage();
      this.println("Image Reference: " + image);
      int blendColor = texture.getBlendColor();
      this.println("Blend Color: 0x" + Integer.toHexString(blendColor));
      this.println("Blending: " + texture.getBlending());
      this.println("WrappingS: " + texture.getWrappingS());
      this.println("WrappingT: " + texture.getWrappingT());
      this.println("Level Filter: " + texture.getLevelFilter());
      this.println("Image Filter: " + texture.getImageFilter());
   }

   protected void dumpTriangleStripArray(TriangleStripArray array) {
      this.dumpObject3D(array);
      int encoding = array.getEncoding();
      this.println("Encoding: " + encoding);
      int[] stripLengths;
      int i;
      if (encoding >= 0 && encoding < 3) {
         this.println("StartIndex: " + array.getStartIndex());
      } else {
         stripLengths = array.getIndices();
         this.print("Indices: [ ");

         for(i = 0; i < stripLengths.length; ++i) {
            if (i % 16 == 0) {
               this.println((String)null);
               this.print("\t");
            }

            this.print(stripLengths[i]);
            if (i != stripLengths.length - 1) {
               this.print(", ");
            }
         }

         this.println((String)null);
         this.println("]");
      }

      stripLengths = array.getStripLengths();
      this.print("Strip Lengths: [ ");

      for(i = 0; i < stripLengths.length; ++i) {
         if (i % 16 == 0) {
            this.println((String)null);
            this.print("\t");
         }

         this.print(stripLengths[i]);
         if (i != stripLengths.length - 1) {
            this.print(", ");
         }
      }

      this.println((String)null);
      this.println("]");
   }

   protected void dumpVertexArray(VertexArray array) {
      this.dumpObject3D(array);
      this.println("Component Size: " + array.getComponentSize());
      this.println("Component Count: " + array.getComponentCount());
      this.println("Encoding: " + array.getEncoding());
      this.println("Vertex Count: " + array.getVertexCount());
      int numElements;
      int i;
      if (array.getComponentSize() == 1) {
         ByteBuffer buffer = (ByteBuffer)array.getBuffer();
         numElements = array.getVertexCount() * array.getComponentSize();
         byte[] vertices = new byte[numElements];
         buffer.get(vertices);
         this.print("Vertices: [ ");

         for(i = 0; i < vertices.length; ++i) {
            if (i % (array.getComponentCount() * 5) == 0) {
               this.println((String)null);
               this.print("\t");
            }

            this.print(vertices[i]);
            if (i != vertices.length - 1) {
               this.print(", ");
            }
         }

         this.println((String)null);
         this.println("]");
         buffer.rewind();
      } else {
         ShortBuffer buffer = (ShortBuffer)array.getBuffer();
         numElements = array.getVertexCount() * array.getComponentSize();
         short[] vertices = new short[numElements];
         buffer.get(vertices);
         this.print("Vertices: [ ");

         for(i = 0; i < vertices.length; ++i) {
            if (i % (array.getComponentCount() * 5) == 0) {
               this.println((String)null);
               this.print("\t");
            }

            this.print(vertices[i]);
            if (i != vertices.length - 1) {
               this.print(", ");
            }
         }

         this.println((String)null);
         this.println("]");
         buffer.rewind();
      }

   }

   protected void dumpVertexBuffer(VertexBuffer buffer) {
      this.dumpObject3D(buffer);
      int defaultColor = buffer.getDefaultColor();
      this.println("Default Color: 0x" + Integer.toHexString(defaultColor));
      float[] scaleBias = new float[4];
      int vCount = buffer.getVertexCount();
      this.println("Number of Vertices: " + vCount);
      VertexArray positions = buffer.getPositions(scaleBias);
      this.println("Position Array Reference: " + positions);
      this.println("Postion Scale :" + scaleBias[0]);
      this.println("Position X Bias :" + scaleBias[1]);
      this.println("Position Y Bias :" + scaleBias[2]);
      this.println("Position Z Bias :" + scaleBias[3]);
      VertexArray normals = buffer.getNormals();
      this.println("Normal Array Reference: " + normals);
      VertexArray colors = buffer.getColors();
      this.println("Color Array Reference: " + colors);
      int texcoordArrayCount = buffer.getTexcoordArrayCount();

      for(int i = 0; i < texcoordArrayCount; ++i) {
         VertexArray texCoords = buffer.getTexCoords(i, scaleBias);
         this.println("Texture Coordinate Array Reference: " + texCoords);
         this.println("Texture Coord Scale :" + scaleBias[0]);
         this.println("Texture Coord X Bias :" + scaleBias[1]);
         this.println("Texture Coord Y Bias :" + scaleBias[2]);
         this.println("Texture Coord Z Bias :" + scaleBias[3]);
      }

   }

   protected void dumpWorld(World world) {
      this.dumpGroup(world);
      Camera camera = world.getActiveCamera();
      this.println("Active Camera Reference: " + camera);
      Background background = world.getBackground();
      this.println("Background Reference: " + background);
   }

   protected void println(String str) {
      if (this.m_out != null) {
         if (str == null) {
            this.m_out.println();
         } else {
            this.m_out.println(str);
         }

         this.m_out.flush();
      } else if (str == null) {
         System.out.println();
      } else {
         System.out.println(str);
      }

   }

   protected void print(String str) {
      if (this.m_out != null) {
         this.m_out.print(str);
      } else {
         System.out.print(str);
      }

   }

   protected void print(int value) {
      if (this.m_out != null) {
         this.m_out.print(value);
      } else {
         System.out.print(value);
      }

   }

   private byte[] intToByteArray(int integer) {
      int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
      byte[] byteArray = new byte[4];

      for(int n = 0; n < byteNum; ++n) {
         byteArray[3 - n] = (byte)(integer >>> n * 8);
      }

      return byteArray;
   }

   private boolean parseArgs(String[] args) throws IllegalArgumentException {
      boolean retVal = false;
      return retVal;
   }

   public static void main(String[] args) {
      String filename = null;
      File file = null;
      M3GFile m3gFile = null;
      boolean isVerbose = false;
      if (args == null || args.length <= 0 || args.length >= 3) {
         System.err.println("DumpM3g: Unable to parse command-line arguments.");
         System.exit(-1);
      }

      for(int i = 0; i < args.length; ++i) {
         String arg = args[i];
         if (arg.equals("-v")) {
            isVerbose = true;
         } else {
            filename = arg;
         }
      }

      file = new File(filename);
      if (!file.exists() || !file.canRead()) {
         System.err.println("DumpM3g: File " + filename + " can not be opened for reading.");
         System.exit(-1);
      }

      try {
         m3gFile = new M3GFile(file);
         if (file == null) {
            throw new IOException("DumpM3g: File " + filename + " can not be opened for reading.");
         }
      } catch (IOException var8) {
         System.err.println(var8.getMessage());
         System.exit(-1);
      }

      DumpM3g dumpM3g = new DumpM3g(m3gFile);
      dumpM3g.setVerbose(isVerbose);

      try {
         dumpM3g.dumpHeaderSection();
         dumpM3g.dumpExternalReferenceSection();
         dumpM3g.dumpSceneSections();
      } catch (IOException var7) {
         System.err.println(var7.getMessage());
         System.exit(-1);
      }

   }
}
