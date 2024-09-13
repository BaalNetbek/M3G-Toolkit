package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class MorphingMesh extends Mesh {
   private VertexBuffer[] m_morphTargets;
   private int m_morphTargetCount;
   private float[] m_weights;

   public int getMorphTargetCount() {
      return this.m_morphTargetCount;
   }

   public VertexBuffer getMorphTarget(int index) {
      if (index >= 0 && index < this.m_morphTargetCount) {
         return this.m_morphTargets[index];
      } else {
         throw new IndexOutOfBoundsException("MorphingMesh: index is either < 0 or > getMorphTargetCount()");
      }
   }

   public void getWeights(float[] weights) {
      if (weights == null) {
         throw new NullPointerException("MorphingMesh: weights is null");
      } else if (weights.length < this.m_morphTargetCount) {
         throw new IllegalArgumentException("MorphingMesh: size of weights not big enough.");
      } else {
         for(int i = 0; i < this.m_morphTargetCount; ++i) {
            weights[i] = this.m_weights[i];
         }

      }
   }

   public void setWeights(float[] weights) {
      if (weights == null) {
         throw new NullPointerException("MorphingMesh: weights is null");
      } else if (weights.length < this.m_morphTargetCount) {
         throw new IllegalArgumentException("MorphingMesh: size of weights not big enough.");
      } else {
         for(int i = 0; i < this.m_morphTargetCount; ++i) {
            this.m_weights[i] = weights[i];
         }

      }
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);

      for(int i = 0; i < this.m_morphTargets.length; ++i) {
         if (references != null) {
            references[numReferences] = this.m_morphTargets[i];
         }

         ++numReferences;
      }

      return numReferences;
   }

   public int getObjectType() {
      return 15;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_morphTargetCount = (int)is.readUInt32();
      this.m_morphTargets = new VertexBuffer[this.m_morphTargetCount];
      this.m_weights = new float[this.m_morphTargetCount];

      for(int i = 0; i < this.m_morphTargetCount; ++i) {
         long index = is.readObjectIndex();
         M3GObject obj = this.getObjectAtIndex(table, index, 21);
         if (obj == null || !(obj instanceof VertexBuffer)) {
            throw new IOException("MorphingMesh:morphTarget-index = " + index);
         }

         this.m_morphTargets[i] = (VertexBuffer)obj;
         float initialWeight = is.readFloat32();
         this.m_weights[i] = initialWeight;
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeUInt32((long)this.m_morphTargetCount);

      for(int i = 0; i < this.m_morphTargetCount; ++i) {
         int index = table.indexOf(this.m_morphTargets[i]);
         if (index <= 0) {
            throw new IOException("MorphingMesh:morphTarget-index = " + index);
         }

         os.writeObjectIndex((long)index);
         os.writeFloat32(this.m_weights[i]);
      }

   }
}
