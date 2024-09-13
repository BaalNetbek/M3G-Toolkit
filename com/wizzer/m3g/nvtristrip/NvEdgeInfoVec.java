package com.wizzer.m3g.nvtristrip;

import java.util.Arrays;

class NvEdgeInfoVec {
   protected NvEdgeInfo[] m_data;
   protected int m_size;

   public NvEdgeInfoVec() {
      this(64);
   }

   public NvEdgeInfoVec(int initialCapacity) {
      this.m_data = new NvEdgeInfo[initialCapacity];
   }

   public void add(NvEdgeInfo value) {
      this.ensureCapacity(this.m_size + 1);
      this.m_data[this.m_size++] = value;
   }

   public void set(int index, NvEdgeInfo value) {
      this.m_data[index] = value;
   }

   public NvEdgeInfo get(int index) {
      return this.m_data[index];
   }

   public void clear() {
      Arrays.fill(this.m_data, (Object)null);
      this.m_size = 0;
   }

   public int size() {
      return this.m_size;
   }

   public boolean contains(NvEdgeInfo value) {
      for(int i = 0; i < this.m_data.length; ++i) {
         if (this.m_data[i] == value) {
            return true;
         }
      }

      return false;
   }

   public void ensureCapacity(int minCapacity) {
      if (minCapacity > this.m_data.length) {
         int newCapacity = this.m_data.length * 3 / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         NvEdgeInfo[] temp = new NvEdgeInfo[newCapacity];
         System.arraycopy(this.m_data, 0, temp, 0, this.m_size);
         this.m_data = temp;
      }

   }
}
