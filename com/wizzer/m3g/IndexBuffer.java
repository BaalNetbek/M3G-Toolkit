package com.wizzer.m3g;

import com.sun.opengl.util.BufferUtil;
import java.nio.IntBuffer;

public abstract class IndexBuffer extends Object3D {
   protected IntBuffer m_buffer = null;

   protected IndexBuffer() {
   }

   public int getIndexCount() {
      return this.m_buffer != null ? this.m_buffer.limit() : 0;
   }

   public abstract void getIndices(int[] var1);

   IntBuffer getBuffer() {
      return this.m_buffer;
   }

   protected void allocate(int numElements) {
      this.m_buffer = BufferUtil.newIntBuffer(numElements);
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }
}
