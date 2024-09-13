package com.wizzer.m3g.nvtristrip;

import java.util.Arrays;

class IntVec {
   protected int[] m_data;
   protected int m_size;

   public IntVec() {
      this(64);
   }

   public IntVec(int initialCapacity) {
      this.m_data = new int[initialCapacity];
   }

   public void add(int value) {
      this.ensureCapacity(this.m_size + 1);
      this.m_data[this.m_size++] = value;
   }

   public void set(int index, int value) {
      this.m_data[index] = value;
   }

   public int get(int index) {
      return this.m_data[index];
   }

   public void clear() {
      Arrays.fill(this.m_data, 0);
      this.m_size = 0;
   }

   public int size() {
      return this.m_size;
   }

   public void ensureCapacity(int minCapacity) {
      if (minCapacity > this.m_data.length) {
         int newCapacity = this.m_data.length * 3 / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         int[] temp = new int[newCapacity];
         System.arraycopy(this.m_data, 0, temp, 0, this.m_size);
         this.m_data = temp;
      }

   }
}
