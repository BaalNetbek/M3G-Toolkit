package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class KeyframeSequence extends Object3D {
   public static final int LINEAR = 176;
   public static final int SLERP = 177;
   public static final int SPLINE = 178;
   public static final int SQUAD = 179;
   public static final int STEP = 180;
   public static final int CONSTANT = 192;
   public static final int LOOP = 193;
   private int m_interpolation;
   private int m_repeatMode;
   private int m_encoding;
   private int m_duration;
   private int m_validRangeFirst;
   private int m_validRangeLast;
   private int m_componentCount;
   private int m_keyframeCount;
   private float[] m_keyframes;
   private int[] m_times;

   public KeyframeSequence(int numKeyframes, int numComponents, int interpolation) {
      if (numKeyframes < 1) {
         throw new IllegalArgumentException("KeyframeSequence: numKeyframes < 1");
      } else if (numComponents < 1) {
         throw new IllegalArgumentException("KeyframeSequence: numComponents < 1");
      } else if (interpolation >= 176 && interpolation <= 180) {
         if ((interpolation == 177 || interpolation == 179) && numComponents != 4) {
            throw new IllegalArgumentException("KeyframeSequence: interpolation is not a valid interpolation mode for keyframes of size numComponents");
         } else {
            this.m_keyframeCount = numKeyframes;
            this.m_keyframes = new float[numKeyframes * numComponents];
            this.m_times = new int[numKeyframes];
            this.m_componentCount = numComponents;
            this.m_interpolation = interpolation;
            this.m_repeatMode = 192;
            this.m_duration = 0;
            this.m_validRangeFirst = 0;
            this.m_validRangeLast = this.m_keyframeCount - 1;
         }
      } else {
         throw new IllegalArgumentException("KeyframeSequence: interpolation is not one of LINEAR, SLERP, SPLINE, SQUAD, STEP");
      }
   }

   public void setKeyframe(int index, int time, float[] value) {
      if (value.length < this.m_componentCount) {
         throw new IllegalArgumentException("KeyframeSequence: value.length <  numComponents");
      } else if (time < 0) {
         throw new IllegalArgumentException("KeyframeSequence: time < 0");
      } else {
         System.arraycopy(value, 0, this.m_keyframes, index * this.m_componentCount, value.length);
         this.m_times[index] = time;
      }
   }

   public void setValidRange(int first, int last) {
      if (first >= 0 && first < this.m_keyframeCount) {
         if (last >= 0 && last < this.m_keyframeCount) {
            this.m_validRangeFirst = first;
            this.m_validRangeLast = last;
         } else {
            throw new IllegalArgumentException("KeyframeSequence: (last < 0) || (last >= numKeyframes)");
         }
      } else {
         throw new IllegalArgumentException("KeyframeSequence: (first < 0) || (first >= numKeyframes)");
      }
   }

   public void setDuration(int duration) {
      if (duration <= 0) {
         throw new IllegalArgumentException("KeyframeSequence: duration <= 0");
      } else {
         this.m_duration = duration;
      }
   }

   public int getDuration() {
      return this.m_duration;
   }

   public void setRepeatMode(int mode) {
      if (mode != 192 && mode != 193) {
         throw new IllegalArgumentException("KeyframeSequence: mode is not one of CONSTANT, LOOP");
      } else {
         this.m_repeatMode = mode;
      }
   }

   public int getRepeatMode() {
      return this.m_repeatMode;
   }

   public int getInterpolationType() {
      return this.m_interpolation;
   }

   public int getValidRangeFirst() {
      return this.m_validRangeFirst;
   }

   public int getValidRangeLast() {
      return this.m_validRangeLast;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   KeyframeSequence() {
   }

   public int getObjectType() {
      return 19;
   }

   public int getEncoding() {
      return this.m_encoding;
   }

   public int getComponentCount() {
      return this.m_componentCount;
   }

   public int getKeyframeCount() {
      return this.m_keyframeCount;
   }

   public int[] getTimes() {
      return this.m_times;
   }

   public float[] getKeyFrames() {
      return this.m_keyframes;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_interpolation = is.readByte();
      if (this.m_interpolation >= 176 && this.m_interpolation <= 180) {
         this.setRepeatMode(is.readByte());
         this.m_encoding = is.readByte();
         this.m_duration = (int)is.readUInt32();
         this.m_validRangeFirst = (int)is.readUInt32();
         this.m_validRangeLast = (int)is.readUInt32();
         this.m_componentCount = (int)is.readUInt32();
         this.m_keyframeCount = (int)is.readUInt32();
         this.m_keyframes = new float[this.m_keyframeCount * this.m_componentCount];
         this.m_times = new int[this.m_keyframeCount];
         if (this.m_encoding != 0) {
            throw new IOException("KeyframeSequence:encoding = " + this.m_encoding);
         } else {
            int i = 0;

            for(int var4 = 0; i < this.m_keyframeCount; ++i) {
               this.m_times[i] = (int)is.readInt32();

               for(int c = 0; c < this.m_componentCount; ++c) {
                  this.m_keyframes[var4++] = is.readFloat32();
               }
            }

         }
      } else {
         throw new IOException("KeyframeSequence:interpolation = " + this.m_interpolation);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeByte(this.m_interpolation);
      os.writeByte(this.m_repeatMode);
      int encoding = 0;
      os.writeByte((int)encoding);
      os.writeUInt32((long)this.m_duration);
      os.writeUInt32((long)this.m_validRangeFirst);
      os.writeUInt32((long)this.m_validRangeLast);
      os.writeUInt32((long)this.m_componentCount);
      os.writeUInt32((long)this.m_keyframeCount);
      if (encoding == 0) {
         int i = 0;

         for(int var5 = 0; i < this.m_keyframeCount; ++i) {
            os.writeInt32(this.m_times[i]);

            for(int c = 0; c < this.m_componentCount; ++c) {
               os.writeFloat32(this.m_keyframes[var5++]);
            }
         }
      }

   }
}
