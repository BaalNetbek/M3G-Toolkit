package com.wizzer.m3g.nvtristrip;

public class PrimitiveGroup {
   public static final int PT_LIST = 0;
   public static final int PT_STRIP = 1;
   public static final int PT_FAN = 2;
   public int m_type = 1;
   public int[] m_indices;
   public int m_numIndices;
}
