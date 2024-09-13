package com.wizzer.m3g.nvtristrip;

class NvFaceInfo {
   public int m_v0;
   public int m_v1;
   public int m_v2;
   public int m_stripId;
   public int m_testStripId;
   public int m_experimentId;
   public boolean m_isFake;

   public NvFaceInfo(int v0, int v1, int v2) {
      this(v0, v1, v2, false);
   }

   public NvFaceInfo(int v0, int v1, int v2, boolean isFake) {
      this.m_v0 = v0;
      this.m_v1 = v1;
      this.m_v2 = v2;
      this.m_isFake = isFake;
      this.m_stripId = this.m_testStripId = this.m_experimentId = -1;
   }

   public void set(NvFaceInfo face) {
      this.m_v0 = face.m_v0;
      this.m_v1 = face.m_v1;
      this.m_v2 = face.m_v2;
      this.m_stripId = face.m_stripId;
      this.m_testStripId = face.m_testStripId;
      this.m_experimentId = face.m_experimentId;
   }
}
