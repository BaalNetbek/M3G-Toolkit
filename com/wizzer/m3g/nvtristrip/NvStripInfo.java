package com.wizzer.m3g.nvtristrip;

class NvStripInfo {
   public NvStripStartInfo m_startInfo;
   public NvFaceInfoVec m_faces;
   public int m_stripId;
   public int m_experimentId;
   public boolean m_visited;
   public int m_numDegenerates;

   public NvStripInfo(NvStripStartInfo startInfo, int stripId) {
      this(startInfo, stripId, -1);
   }

   public NvStripInfo(NvStripStartInfo startInfo, int stripId, int experimentId) {
      this.m_startInfo = startInfo;
      this.m_stripId = stripId;
      this.m_experimentId = experimentId;
      this.m_faces = new NvFaceInfoVec();
      this.m_visited = false;
      this.m_numDegenerates = 0;
   }

   public boolean isExperiment() {
      return this.m_experimentId >= 0;
   }

   public boolean isInStrip(NvFaceInfo faceInfo) {
      if (faceInfo == null) {
         return false;
      } else {
         return this.m_experimentId >= 0 ? faceInfo.m_testStripId == this.m_stripId : faceInfo.m_stripId == this.m_stripId;
      }
   }

   public boolean sharesEdge(NvFaceInfo faceInfo, NvEdgeInfoVec edgeInfos) {
      NvEdgeInfo currEdge = NvStripifier.findEdgeInfo(edgeInfos, faceInfo.m_v0, faceInfo.m_v1);
      if (!this.isInStrip(currEdge.m_face0) && !this.isInStrip(currEdge.m_face1)) {
         currEdge = NvStripifier.findEdgeInfo(edgeInfos, faceInfo.m_v1, faceInfo.m_v2);
         if (!this.isInStrip(currEdge.m_face0) && !this.isInStrip(currEdge.m_face1)) {
            currEdge = NvStripifier.findEdgeInfo(edgeInfos, faceInfo.m_v2, faceInfo.m_v0);
            return this.isInStrip(currEdge.m_face0) || this.isInStrip(currEdge.m_face1);
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public void combine(NvFaceInfoVec forward, NvFaceInfoVec backward) {
      int numFaces = backward.size();

      int i;
      for(i = numFaces - 1; i >= 0; --i) {
         this.m_faces.add(backward.get(i));
      }

      numFaces = forward.size();

      for(i = 0; i < numFaces; ++i) {
         this.m_faces.add(forward.get(i));
      }

   }

   public boolean unique(NvFaceInfoVec faceVec, NvFaceInfo face) {
      boolean v0 = false;
      boolean v1 = false;
      boolean v2 = false;

      for(int i = 0; i < faceVec.size(); ++i) {
         NvFaceInfo faceInfo = faceVec.get(i);
         if (!v0 && (faceInfo.m_v0 == face.m_v0 || faceInfo.m_v1 == face.m_v0 || faceInfo.m_v2 == face.m_v0)) {
            v0 = true;
         }

         if (!v1 && (faceInfo.m_v0 == face.m_v1 || faceInfo.m_v1 == face.m_v1 || faceInfo.m_v2 == face.m_v1)) {
            v1 = true;
         }

         if (!v2 && (faceInfo.m_v0 == face.m_v2 || faceInfo.m_v1 == face.m_v2 || faceInfo.m_v2 == face.m_v2)) {
            v2 = true;
         }

         if (v0 && v1 && v2) {
            return false;
         }
      }

      return true;
   }

   public boolean isMarked(NvFaceInfo faceInfo) {
      return faceInfo.m_stripId >= 0 || this.isExperiment() && faceInfo.m_experimentId == this.m_experimentId;
   }

   public void markTriangle(NvFaceInfo faceInfo) {
      assert !this.isMarked(faceInfo);

      if (this.isExperiment()) {
         faceInfo.m_experimentId = this.m_experimentId;
         faceInfo.m_testStripId = this.m_stripId;
      } else {
         assert faceInfo.m_stripId == -1;

         faceInfo.m_experimentId = -1;
         faceInfo.m_stripId = this.m_stripId;
      }

   }

   public void build(NvEdgeInfoVec edgeInfos, NvFaceInfoVec faceInfos) {
      IntVec scratchIndices = new IntVec();
      NvFaceInfoVec forwardFaces = new NvFaceInfoVec();
      NvFaceInfoVec backwardFaces = new NvFaceInfoVec();
      forwardFaces.add(this.m_startInfo.m_startFace);
      this.markTriangle(this.m_startInfo.m_startFace);
      int v0 = this.m_startInfo.m_toV1 ? this.m_startInfo.m_startEdge.m_v0 : this.m_startInfo.m_startEdge.m_v1;
      int v1 = this.m_startInfo.m_toV1 ? this.m_startInfo.m_startEdge.m_v1 : this.m_startInfo.m_startEdge.m_v0;
      scratchIndices.add(v0);
      scratchIndices.add(v1);
      int v2 = NvStripifier.getNextIndex(scratchIndices, this.m_startInfo.m_startFace);
      scratchIndices.add(v2);
      int nv0 = v1;
      int nv1 = v2;

      NvFaceInfo nextFace;
      int testnv0;
      int testnv0;
      NvFaceInfo nextNextFace;
      NvFaceInfo testNextFace;
      for(nextFace = NvStripifier.findOtherFace(edgeInfos, v1, v2, this.m_startInfo.m_startFace); nextFace != null && !this.isMarked(nextFace); nextFace = NvStripifier.findOtherFace(edgeInfos, testnv0, testnv0, nextFace)) {
         testnv0 = nv1;
         testnv0 = NvStripifier.getNextIndex(scratchIndices, nextFace);
         NvFaceInfo nextNextFace = NvStripifier.findOtherFace(edgeInfos, nv1, testnv0, nextFace);
         if (nextNextFace == null || this.isMarked(nextNextFace)) {
            nextNextFace = NvStripifier.findOtherFace(edgeInfos, nv0, testnv0, nextFace);
            if (nextNextFace != null && !this.isMarked(nextNextFace)) {
               testNextFace = new NvFaceInfo(nv0, nv1, nv0, true);
               forwardFaces.add(testNextFace);
               this.markTriangle(testNextFace);
               scratchIndices.add(nv0);
               testnv0 = nv0;
               ++this.m_numDegenerates;
            }
         }

         forwardFaces.add(nextFace);
         this.markTriangle(nextFace);
         scratchIndices.add(testnv0);
         nv0 = testnv0;
         nv1 = testnv0;
      }

      NvFaceInfoVec tempAllFaces = new NvFaceInfoVec();

      for(testnv0 = 0; testnv0 < forwardFaces.size(); ++testnv0) {
         tempAllFaces.add(forwardFaces.get(testnv0));
      }

      scratchIndices.clear();
      scratchIndices.add(v2);
      scratchIndices.add(v1);
      scratchIndices.add(v0);
      nv0 = v1;
      nv1 = v0;

      int testnv1;
      for(nextFace = NvStripifier.findOtherFace(edgeInfos, v1, v0, this.m_startInfo.m_startFace); nextFace != null && !this.isMarked(nextFace) && this.unique(tempAllFaces, nextFace); nextFace = NvStripifier.findOtherFace(edgeInfos, testnv0, testnv1, nextFace)) {
         testnv0 = nv1;
         testnv1 = NvStripifier.getNextIndex(scratchIndices, nextFace);
         nextNextFace = NvStripifier.findOtherFace(edgeInfos, nv1, testnv1, nextFace);
         if (nextNextFace == null || this.isMarked(nextNextFace)) {
            testNextFace = NvStripifier.findOtherFace(edgeInfos, nv0, testnv1, nextFace);
            if (testNextFace != null && !this.isMarked(testNextFace)) {
               NvFaceInfo tempFace = new NvFaceInfo(nv0, nv1, nv0, true);
               backwardFaces.add(tempFace);
               this.markTriangle(tempFace);
               scratchIndices.add(nv0);
               testnv0 = nv0;
               ++this.m_numDegenerates;
            }
         }

         backwardFaces.add(nextFace);
         tempAllFaces.add(nextFace);
         this.markTriangle(nextFace);
         scratchIndices.add(testnv1);
         nv0 = testnv0;
         nv1 = testnv1;
      }

      this.combine(forwardFaces, backwardFaces);
   }
}
