package com.wizzer.m3g.nvtristrip;

class NvStripStartInfo {
   public NvFaceInfo m_startFace;
   public NvEdgeInfo m_startEdge;
   public boolean m_toV1;

   public NvStripStartInfo(NvFaceInfo startFace, NvEdgeInfo startEdge, boolean toV1) {
      this.m_startFace = startFace;
      this.m_startEdge = startEdge;
      this.m_toV1 = toV1;
   }
}
