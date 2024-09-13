package com.wizzer.m3g.nvtristrip;

import java.util.Arrays;

class VertexCache {
   private int[] m_entries;
   private int m_numEntries;

   public VertexCache() {
      this(16);
   }

   public VertexCache(int size) {
      this.m_entries = new int[this.m_numEntries = size];
      this.clear();
   }

   public boolean inCache(int entry) {
      for(int i = 0; i < this.m_entries.length; ++i) {
         if (this.m_entries[i] == entry) {
            return true;
         }
      }

      return false;
   }

   public int addEntry(int entry) {
      int removed = this.m_entries[this.m_entries.length - 1];

      for(int i = this.m_numEntries - 2; i >= 0; --i) {
         this.m_entries[i + 1] = this.m_entries[i];
      }

      this.m_entries[0] = entry;
      return removed;
   }

   public void clear() {
      Arrays.fill(this.m_entries, -1);
   }

   public void copy(VertexCache inVcache) {
      for(int i = 0; i < this.m_numEntries; ++i) {
         inVcache.set(i, this.m_entries[i]);
      }

   }

   public int at(int index) {
      return this.m_entries[index];
   }

   public void set(int index, int value) {
      this.m_entries[index] = value;
   }
}
