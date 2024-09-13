package com.wizzer.m3g;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SceneSection extends Section {
   private ArrayList<Object3D> m_objects3D = new ArrayList();

   public void addObject3D(Object3D object) {
      this.m_objects3D.add(object);
   }

   public Object3D[] getObjects3D() {
      return (Object3D[])this.m_objects3D.toArray(new Object3D[this.m_objects3D.size()]);
   }

   public void removeObject3D(Object3D object) {
      this.m_objects3D.remove(object);
   }

   public void removeObjects3D() {
      this.m_objects3D.clear();
   }

   protected void readObjects(M3GInputStream is, ArrayList table) throws IOException {
      while(is.available() > 0) {
         byte type = (byte)is.readByte();
         long length = is.readUInt32();
         Object3D object = null;
         if (type == 1) {
            object = new AnimationController();
         } else if (type == 2) {
            object = new AnimationTrack();
         } else if (type == 3) {
            object = new Appearance();
         } else if (type == 4) {
            object = new Background();
         } else if (type == 5) {
            object = new Camera();
         } else if (type == 6) {
            object = new CompositingMode();
         } else if (type == 7) {
            object = new Fog();
         } else if (type == 8) {
            object = new PolygonMode();
         } else if (type == 9) {
            object = new Group();
         } else if (type == 10) {
            object = new Image2D();
         } else if (type == 11) {
            object = new TriangleStripArray();
         } else if (type == 12) {
            object = new Light();
         } else if (type == 13) {
            object = new Material();
         } else if (type == 14) {
            object = new Mesh();
         } else if (type == 15) {
            object = null;
         } else if (type == 16) {
            object = new SkinnedMesh();
         } else if (type == 17) {
            object = new Texture2D();
         } else if (type == 18) {
            object = new Sprite3D();
         } else if (type == 19) {
            object = new KeyframeSequence();
         } else if (type == 20) {
            object = new VertexArray();
         } else if (type == 21) {
            object = new VertexBuffer();
         } else {
            if (type != 22) {
               throw new IOException("SceneSection.type=" + type);
            }

            object = new World();
         }

         ((Object3D)object).unmarshall(is, table);
         this.m_objects3D.add(object);
         table.add(object);
      }

   }

   protected void writeObjects(M3GOutputStream os, ArrayList table) throws IOException {
      for(int i = 0; i < table.size(); ++i) {
         Object obj = table.get(i);
         if (obj instanceof Object3D) {
            Object3D o3d = (Object3D)obj;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            o3d.marshall(new M3GOutputStream(baos), table);
            os.writeByte(o3d.getObjectType());
            os.writeUInt32((long)baos.size());
            baos.writeTo(os);
         }
      }

   }

   protected void readObjects2(M3GInputStream is, ArrayList table) throws IOException {
      String[] names = new String[]{"HEADER", "ANIMATION_CONTROLLER", "ANIMATION_TRACK", "APPEARANCE", "BACKGROUND", "CAMERA", "COMPOSITING_MODE", "FOG", "POLYGON_MODE", "GROUP", "IMAGE2D", "TRIANGLE_STRIP_ARRAY", "LIGHT", "MATERIAL", "MESH", "MORPHING_MESH", "SKINNED_MESH", "TEXTURE2D", "SPRITE3D", "KEYFRAME_SEQUENCE", "VERTEX_ARRAY", "VERTEX_BUFFER", "WORLD"};
      ArrayList datas = new ArrayList();

      while(is.available() > 0) {
         byte type = (byte)is.readByte();
         long length = is.readUInt32();
         byte[] data = new byte[(int)length];
         is.read(data);
         Object3D object = null;
         if (type == 1) {
            object = new AnimationController();
         } else if (type == 2) {
            object = new AnimationTrack();
         } else if (type == 3) {
            object = new Appearance();
         } else if (type == 4) {
            object = new Background();
         } else if (type == 5) {
            object = new Camera();
         } else if (type == 6) {
            object = new CompositingMode();
         } else if (type == 7) {
            object = new Fog();
         } else if (type == 8) {
            object = new PolygonMode();
         } else if (type == 9) {
            object = new Group();
         } else if (type == 10) {
            object = new Image2D();
         } else if (type == 11) {
            object = new TriangleStripArray();
         } else if (type == 12) {
            object = new Light();
         } else if (type == 13) {
            object = new Material();
         } else if (type == 14) {
            object = new Mesh();
         } else if (type == 15) {
            object = null;
         } else if (type == 16) {
            object = new SkinnedMesh();
         } else if (type == 17) {
            object = new Texture2D();
         } else if (type == 18) {
            object = new Sprite3D();
         } else if (type == 19) {
            object = new KeyframeSequence();
         } else if (type == 20) {
            object = new VertexArray();
         } else if (type == 21) {
            object = new VertexBuffer();
         } else {
            if (type != 22) {
               throw new IOException("SceneSection.type=" + type);
            }

            object = new World();
         }

         ((Object3D)object).unmarshall(new M3GInputStream(new ByteArrayInputStream(data)), table);
         this.m_objects3D.add(object);
         table.add(object);
         datas.add(object);
         datas.add(data);
      }

      for(int i = 0; i < datas.size(); i += 2) {
         Object3D object = (Object3D)datas.get(i);
         byte[] data = (byte[])datas.get(i + 1);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         object.marshall(new M3GOutputStream(baos), table);
         byte[] data2 = baos.toByteArray();
         System.out.println(names[object.getObjectType()] + " (" + data.length + "/" + data2.length + "/" + Arrays.equals(data, data2) + ")");
         if (!Arrays.equals(data, data2)) {
            System.out.println("");
            System.out.println("--");

            int u;
            for(u = 0; u < data.length; ++u) {
               System.out.print(Integer.toHexString(data[u] & 255) + ",");
            }

            System.out.println("\n--\n");

            for(u = 0; u < data2.length; ++u) {
               System.out.print(Integer.toHexString(data2[u] & 255) + ",");
            }

            System.out.println("");
            System.out.println("--");
            System.out.println("");
         }
      }

   }
}
