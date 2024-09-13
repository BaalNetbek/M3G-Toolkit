package com.wizzer.m3g.nvtristrip;

class NvEdgeInfo {
   public int m_v0;
   public int m_v1;
   public NvFaceInfo m_face0;
   public NvFaceInfo m_face1;
   public NvEdgeInfo m_nextV0;
   public NvEdgeInfo m_nextV1;

   public NvEdgeInfo(int v0, int v1) {
      this.m_v0 = v0;
      this.m_v1 = v1;
   }
}
