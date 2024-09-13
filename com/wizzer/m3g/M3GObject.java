package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public abstract class M3GObject {
   public static final int HEADER = 0;
   public static final int ANIMATION_CONTROLLER = 1;
   public static final int ANIMATION_TRACK = 2;
   public static final int APPEARANCE = 3;
   public static final int BACKGROUND = 4;
   public static final int CAMERA = 5;
   public static final int COMPOSITING_MODE = 6;
   public static final int FOG = 7;
   public static final int POLYGON_MODE = 8;
   public static final int GROUP = 9;
   public static final int IMAGE2D = 10;
   public static final int TRIANGLE_STRIP_ARRAY = 11;
   public static final int LIGHT = 12;
   public static final int MATERIAL = 13;
   public static final int MESH = 14;
   public static final int MORPHING_MESH = 15;
   public static final int SKINNED_MESH = 16;
   public static final int TEXTURE2D = 17;
   public static final int SPRITE3D = 18;
   public static final int KEYFRAME_SEQUENCE = 19;
   public static final int VERTEX_ARRAY = 20;
   public static final int VERTEX_BUFFER = 21;
   public static final int WORLD = 22;
   public static final int EXTERNAL_REFERENCE = 255;
   private boolean m_root = true;

   public boolean isRoot() {
      return this.m_root;
   }

   public abstract int getObjectType();

   protected abstract void unmarshall(M3GInputStream var1, ArrayList var2) throws IOException;

   protected abstract void marshall(M3GOutputStream var1, ArrayList var2) throws IOException;

   protected void buildReferenceTable(ArrayList table) {
      if (!table.contains(this)) {
         table.add(this);
      }

   }

   protected M3GObject getObjectAtIndex(ArrayList table, long index, int type) {
      if (index > 0L && index < (long)table.size()) {
         Object obj = table.get((int)(index & -1L));
         if (obj instanceof M3GObject && (type == -1 || ((M3GObject)obj).getObjectType() == type)) {
            ((M3GObject)obj).m_root = false;
            return (M3GObject)obj;
         } else {
            return obj instanceof M3GObject && ((M3GObject)obj).getObjectType() == 255 ? ((ExternalReference)obj).getReference() : null;
         }
      } else {
         return null;
      }
   }
}
