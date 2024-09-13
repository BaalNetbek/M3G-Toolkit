package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class AnimationTrack extends Object3D {
   public static final int ALPHA = 256;
   public static final int AMBIENT_COLOR = 257;
   public static final int COLOR = 258;
   public static final int CROP = 259;
   public static final int DENSITY = 260;
   public static final int DIFFUSE_COLOR = 261;
   public static final int EMISSIVE_COLOR = 262;
   public static final int FAR_DISTANCE = 263;
   public static final int FIELD_OF_VIEW = 264;
   public static final int INTENSITY = 265;
   public static final int MORPH_WEIGHTS = 266;
   public static final int NEAR_DISTANCE = 267;
   public static final int ORIENTATION = 268;
   public static final int PICKABILITY = 269;
   public static final int SCALE = 270;
   public static final int SHININESS = 271;
   public static final int SPECULAR_COLOR = 272;
   public static final int SPOT_ANGLE = 273;
   public static final int SPOT_EXPONENT = 274;
   public static final int TRANSLATION = 275;
   public static final int VISIBILITY = 276;
   private KeyframeSequence m_keyframeSequence;
   private AnimationController m_controller;
   private long m_targetProperty;

   public AnimationTrack(KeyframeSequence sequence, int property) {
      if (sequence == null) {
         throw new NullPointerException("AnimationTrack: sequence is null");
      } else if (property >= 256 && property <= 276) {
         this.m_keyframeSequence = sequence;
         this.m_targetProperty = (long)property;
      } else {
         throw new IllegalArgumentException("AnimationTrack: property is an invalid value");
      }
   }

   public void setController(AnimationController controller) {
      this.m_controller = controller;
   }

   public AnimationController getController() {
      return this.m_controller;
   }

   public KeyframeSequence getKeyframeSequence() {
      return this.m_keyframeSequence;
   }

   public int getTargetProperty() {
      return (int)(this.m_targetProperty & -1L);
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_keyframeSequence != null) {
         if (references != null) {
            references[numReferences] = this.m_keyframeSequence;
         }

         ++numReferences;
      }

      if (this.m_controller != null) {
         if (references != null) {
            references[numReferences] = this.m_controller;
         }

         ++numReferences;
      }

      return numReferences;
   }

   AnimationTrack() {
   }

   public int getObjectType() {
      return 2;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      M3GObject obj = this.getObjectAtIndex(table, index, 19);
      if (obj != null) {
         this.m_keyframeSequence = (KeyframeSequence)obj;
         index = is.readObjectIndex();
         if (index != 0L) {
            obj = this.getObjectAtIndex(table, index, 1);
            if (obj == null) {
               throw new IOException("AnimationTrack:controller-index = " + index);
            }

            this.setController((AnimationController)obj);
         }

         this.m_targetProperty = is.readUInt32();
         if (this.m_targetProperty < 256L || this.m_targetProperty > 276L) {
            throw new IOException("AnimationTrack:targetProperty = " + this.m_targetProperty);
         }
      } else {
         throw new IOException("AnimationTrack:keyframeSequence-index = " + index);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_keyframeSequence);
      if (index > 0) {
         os.writeObjectIndex((long)index);
         if (this.m_controller != null) {
            index = table.indexOf(this.m_controller);
            if (index <= 0) {
               throw new IOException("AnimationTrack:controller-index = " + index);
            }

            os.writeObjectIndex((long)index);
         }

         os.writeUInt32(this.m_targetProperty);
      } else {
         throw new IOException("AnimationTrack:keyframeSequence-index = " + index);
      }
   }

   protected void buildReferenceTable(ArrayList table) {
      this.m_keyframeSequence.buildReferenceTable(table);
      if (this.m_controller != null) {
         this.m_controller.buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }
}
