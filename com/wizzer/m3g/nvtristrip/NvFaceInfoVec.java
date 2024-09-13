package com.wizzer.m3g.nvtristrip;

import java.util.Arrays;

class NvFaceInfoVec {
   protected NvFaceInfo[] data;
   protected int size;

   public NvFaceInfoVec() {
      this(64);
   }

   public NvFaceInfoVec(int initialCapacity) {
      this.data = new NvFaceInfo[initialCapacity];
   }

   public void add(NvFaceInfo value) {
      this.ensureCapacity(this.size + 1);
      this.data[this.size++] = value;
   }

   public void set(int index, NvFaceInfo value) {
      this.data[index] = value;
   }

   public NvFaceInfo get(int index) {
      return this.data[index];
   }

   public void clear() {
      Arrays.fill(this.data, (Object)null);
      this.size = 0;
   }

   public int size() {
      return this.size;
   }

   public boolean contains(NvFaceInfo value) {
      for(int i = 0; i < this.data.length; ++i) {
         if (this.data[i] == value) {
            return true;
         }
      }

      return false;
   }

   public void ensureCapacity(int minCapacity) {
      if (minCapacity > this.data.length) {
         int newCapacity = this.data.length * 3 / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         NvFaceInfo[] temp = new NvFaceInfo[newCapacity];
         System.arraycopy(this.data, 0, temp, 0, this.size);
         this.data = temp;
      }

   }
}
