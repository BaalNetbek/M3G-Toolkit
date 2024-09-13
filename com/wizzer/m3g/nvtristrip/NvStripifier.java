package com.wizzer.m3g.nvtristrip;

class NvStripifier {
   public static final int CACHE_INEFFICIENCY = 6;
   protected IntVec m_indices = new IntVec();
   protected int m_cacheSize;
   protected int m_minStripLength;
   protected float m_meshJump;
   protected boolean m_firstTimeResetPoint;

   public NvStripifier() {
   }

   public void stripify(IntVec in_indices, int in_cacheSize, int in_minStripLength, int maxIndex, NvStripInfoVec outStrips, NvFaceInfoVec outFaceList) {
      this.m_meshJump = 0.0F;
      this.m_firstTimeResetPoint = true;
      int numSamples = 10;
      this.m_cacheSize = Math.max(1, in_cacheSize - 6);
      this.m_minStripLength = in_minStripLength;
      this.m_indices = in_indices;
      NvFaceInfoVec allFaceInfos = new NvFaceInfoVec();
      NvEdgeInfoVec allEdgeInfos = new NvEdgeInfoVec();
      this.buildStripifyInfo(allFaceInfos, allEdgeInfos, maxIndex);
      NvStripInfoVec allStrips = new NvStripInfoVec();
      this.findAllStrips(allStrips, allFaceInfos, allEdgeInfos, numSamples);
      this.splitUpStripsAndOptimize(allStrips, outStrips, allEdgeInfos, outFaceList);
   }

   public int createStrips(NvStripInfoVec allStrips, IntVec stripIndices, boolean stitchStrips, boolean restart, int restartVal) {
      int numSeparateStrips = 0;
      NvFaceInfo tLastFace = new NvFaceInfo(0, 0, 0);
      int nStripCount = allStrips.size();

      assert nStripCount > 0;

      int accountForNegatives = 0;

      for(int i = 0; i < nStripCount; ++i) {
         NvStripInfo strip = allStrips.get(i);
         int nStripFaceCount = strip.m_faces.size();

         assert nStripFaceCount > 0;

         NvFaceInfo tFirstFace = new NvFaceInfo(strip.m_faces.get(0).m_v0, strip.m_faces.get(0).m_v1, strip.m_faces.get(0).m_v2);
         int nUnique;
         if (nStripFaceCount > 1) {
            nUnique = getUniqueVertexInB(strip.m_faces.get(1), tFirstFace);
            int pivot;
            if (nUnique == tFirstFace.m_v1) {
               pivot = tFirstFace.m_v0;
               tFirstFace.m_v0 = tFirstFace.m_v1;
               tFirstFace.m_v1 = pivot;
            } else if (nUnique == tFirstFace.m_v2) {
               pivot = tFirstFace.m_v0;
               tFirstFace.m_v0 = tFirstFace.m_v2;
               tFirstFace.m_v2 = pivot;
            }

            if (nStripFaceCount > 2) {
               int temp;
               if (isDegenerate(strip.m_faces.get(1))) {
                  pivot = strip.m_faces.get(1).m_v1;
                  if (tFirstFace.m_v1 == pivot) {
                     temp = tFirstFace.m_v1;
                     tFirstFace.m_v1 = tFirstFace.m_v2;
                     tFirstFace.m_v2 = temp;
                  }
               } else {
                  int[] nShared = new int[2];
                  getSharedVertices(strip.m_faces.get(2), tFirstFace, nShared);
                  if (nShared[0] == tFirstFace.m_v1 && nShared[1] == -1) {
                     temp = tFirstFace.m_v1;
                     tFirstFace.m_v1 = tFirstFace.m_v2;
                     tFirstFace.m_v2 = temp;
                  }
               }
            }
         }

         if (i != 0 && stitchStrips && !restart) {
            stripIndices.add(tFirstFace.m_v0);
            if (this.nextIsCW(stripIndices.size() - accountForNegatives) != this.isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1)) {
               stripIndices.add(tFirstFace.m_v0);
            }
         } else if (!this.isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1)) {
            stripIndices.add(tFirstFace.m_v0);
         }

         stripIndices.add(tFirstFace.m_v0);
         stripIndices.add(tFirstFace.m_v1);
         stripIndices.add(tFirstFace.m_v2);
         tLastFace.set(tFirstFace);

         for(int j = 1; j < nStripFaceCount; ++j) {
            nUnique = getUniqueVertexInB(tLastFace, strip.m_faces.get(j));
            if (nUnique != -1) {
               stripIndices.add(nUnique);
               tLastFace.m_v0 = tLastFace.m_v1;
               tLastFace.m_v1 = tLastFace.m_v2;
               tLastFace.m_v2 = nUnique;
            } else {
               stripIndices.add(strip.m_faces.get(j).m_v2);
               tLastFace.m_v0 = strip.m_faces.get(j).m_v0;
               tLastFace.m_v1 = strip.m_faces.get(j).m_v1;
               tLastFace.m_v2 = strip.m_faces.get(j).m_v2;
            }
         }

         if (stitchStrips && !restart) {
            if (i != nStripCount - 1) {
               stripIndices.add(tLastFace.m_v2);
            }
         } else if (restart) {
            stripIndices.add(restartVal);
         } else {
            stripIndices.add(-1);
            ++accountForNegatives;
            ++numSeparateStrips;
         }

         tLastFace.m_v0 = tLastFace.m_v1;
         tLastFace.m_v1 = tLastFace.m_v2;
      }

      if (stitchStrips || restart) {
         numSeparateStrips = 1;
      }

      return numSeparateStrips;
   }

   protected boolean isMoneyFace(NvFaceInfo face) {
      return this.faceContainsIndex(face, 800) && this.faceContainsIndex(face, 812) && this.faceContainsIndex(face, 731);
   }

   protected boolean faceContainsIndex(NvFaceInfo face, int index) {
      return face.m_v0 == index || face.m_v1 == index || face.m_v2 == index;
   }

   protected boolean isCW(NvFaceInfo faceInfo, int v0, int v1) {
      if (faceInfo.m_v0 == v0) {
         return faceInfo.m_v1 == v1;
      } else if (faceInfo.m_v1 == v0) {
         return faceInfo.m_v2 == v1;
      } else {
         return faceInfo.m_v0 == v1;
      }
   }

   protected boolean nextIsCW(int numIndices) {
      return numIndices % 2 == 0;
   }

   protected NvFaceInfo findGoodResetPoint(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos) {
      NvFaceInfo result = null;
      if (result == null) {
         int numFaces = faceInfos.size();
         int startPoint;
         if (this.m_firstTimeResetPoint) {
            startPoint = this.findStartPoint(faceInfos, edgeInfos);
            this.m_firstTimeResetPoint = false;
         } else {
            startPoint = (int)(((float)numFaces - 1.0F) * this.m_meshJump);
         }

         if (startPoint == -1) {
            startPoint = (int)(((float)numFaces - 1.0F) * this.m_meshJump);
         }

         int i = startPoint;

         label32: {
            while(faceInfos.get(i).m_stripId >= 0) {
               ++i;
               if (i >= numFaces) {
                  i = 0;
               }

               if (i == startPoint) {
                  break label32;
               }
            }

            result = faceInfos.get(i);
         }

         this.m_meshJump += 0.1F;
         if (this.m_meshJump > 1.0F) {
            this.m_meshJump = 0.05F;
         }
      }

      return result;
   }

   protected void findAllStrips(NvStripInfoVec allStrips, NvFaceInfoVec allFaceInfos, NvEdgeInfoVec allEdgeInfos, int numSamples) {
      int experimentId = 0;
      int stripId = 0;
      boolean done = false;
      int var8 = 0;

      while(!done) {
         ++var8;
         NvStripInfoVec[] experiments = new NvStripInfoVec[numSamples * 6];

         int experimentIndex;
         for(experimentIndex = 0; experimentIndex < experiments.length; ++experimentIndex) {
            experiments[experimentIndex] = new NvStripInfoVec();
         }

         experimentIndex = 0;
         NvFaceInfoVec resetPoints = new NvFaceInfoVec();

         int numExperiments;
         NvStripInfo strip01;
         for(numExperiments = 0; numExperiments < numSamples; ++numExperiments) {
            NvFaceInfo nextFace = this.findGoodResetPoint(allFaceInfos, allEdgeInfos);
            if (nextFace == null) {
               done = true;
               break;
            }

            if (!resetPoints.contains(nextFace)) {
               resetPoints.add(nextFace);

               assert nextFace.m_stripId < 0;

               NvEdgeInfo edge01 = findEdgeInfo(allEdgeInfos, nextFace.m_v0, nextFace.m_v1);
               strip01 = new NvStripInfo(new NvStripStartInfo(nextFace, edge01, true), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip01);
               NvEdgeInfo edge10 = findEdgeInfo(allEdgeInfos, nextFace.m_v0, nextFace.m_v1);
               NvStripInfo strip10 = new NvStripInfo(new NvStripStartInfo(nextFace, edge10, false), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip10);
               NvEdgeInfo edge12 = findEdgeInfo(allEdgeInfos, nextFace.m_v1, nextFace.m_v2);
               NvStripInfo strip12 = new NvStripInfo(new NvStripStartInfo(nextFace, edge12, true), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip12);
               NvEdgeInfo edge21 = findEdgeInfo(allEdgeInfos, nextFace.m_v1, nextFace.m_v2);
               NvStripInfo strip21 = new NvStripInfo(new NvStripStartInfo(nextFace, edge21, false), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip21);
               NvEdgeInfo edge20 = findEdgeInfo(allEdgeInfos, nextFace.m_v2, nextFace.m_v0);
               NvStripInfo strip20 = new NvStripInfo(new NvStripStartInfo(nextFace, edge20, true), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip20);
               NvEdgeInfo edge02 = findEdgeInfo(allEdgeInfos, nextFace.m_v2, nextFace.m_v0);
               NvStripInfo strip02 = new NvStripInfo(new NvStripStartInfo(nextFace, edge02, false), stripId++, experimentId++);
               experiments[experimentIndex++].add(strip02);
            }
         }

         numExperiments = experimentIndex;

         int bestIndex;
         for(bestIndex = 0; bestIndex < numExperiments; ++bestIndex) {
            experiments[bestIndex].get(0).build(allEdgeInfos, allFaceInfos);
            int experimentId2 = experiments[bestIndex].get(0).m_experimentId;
            strip01 = experiments[bestIndex].get(0);
            NvStripStartInfo startInfo = new NvStripStartInfo((NvFaceInfo)null, (NvEdgeInfo)null, false);

            while(this.findTraversal(allFaceInfos, allEdgeInfos, strip01, startInfo)) {
               strip01 = new NvStripInfo(startInfo, stripId++, experimentId2);
               strip01.build(allEdgeInfos, allFaceInfos);
               experiments[bestIndex].add(strip01);
            }
         }

         bestIndex = 0;
         double bestValue = 0.0D;

         for(int i = 0; i < numExperiments; ++i) {
            float avgStripSizeWeight = 1.0F;
            float numTrisWeight = 0.0F;
            float numStripsWeight = 0.0F;
            float avgStripSize = this.avgStripSize(experiments[i]);
            float numStrips = (float)experiments[i].size();
            float value = avgStripSize * avgStripSizeWeight + numStrips * numStripsWeight;
            if ((double)value > bestValue) {
               bestValue = (double)value;
               bestIndex = i;
            }
         }

         this.commitStrips(allStrips, experiments[bestIndex]);
      }

   }

   protected void splitUpStripsAndOptimize(NvStripInfoVec allStrips, NvStripInfoVec outStrips, NvEdgeInfoVec edgeInfos, NvFaceInfoVec outFaceList) {
      int threshold = this.m_cacheSize;
      NvStripInfoVec tempStrips = new NvStripInfoVec();

      int j;
      int numTimes;
      int numLeftover;
      int faceCtr;
      int i;
      for(int i = 0; i < allStrips.size(); ++i) {
         NvStripInfo currentStrip = null;
         NvStripStartInfo startInfo = new NvStripStartInfo((NvFaceInfo)null, (NvEdgeInfo)null, false);
         int actualStripSize = 0;

         for(j = 0; j < allStrips.get(i).m_faces.size(); ++j) {
            if (!isDegenerate(allStrips.get(i).m_faces.get(j))) {
               ++actualStripSize;
            }
         }

         if (actualStripSize > threshold) {
            numTimes = actualStripSize / threshold;
            numLeftover = actualStripSize % threshold;
            int degenerateCount = 0;

            label196:
            for(j = 0; j < numTimes; ++j) {
               currentStrip = new NvStripInfo(startInfo, 0, -1);
               faceCtr = j * threshold + degenerateCount;
               boolean firstTime = true;

               while(true) {
                  while(true) {
                     while(faceCtr < threshold + j * threshold + degenerateCount) {
                        if (isDegenerate(allStrips.get(i).m_faces.get(faceCtr))) {
                           ++degenerateCount;
                           if ((faceCtr + 1 != threshold + j * threshold + degenerateCount || j == numTimes - 1 && numLeftover < 4 && numLeftover > 0) && !firstTime) {
                              currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                           } else {
                              ++faceCtr;
                           }
                        } else {
                           currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                           firstTime = false;
                        }
                     }

                     if (j == numTimes - 1 && numLeftover < 4 && numLeftover > 0) {
                        int ctr = 0;

                        while(ctr < numLeftover) {
                           if (!isDegenerate(allStrips.get(i).m_faces.get(faceCtr))) {
                              currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                              ++ctr;
                           } else {
                              currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                              ++degenerateCount;
                           }
                        }

                        numLeftover = 0;
                     }

                     tempStrips.add(currentStrip);
                     continue label196;
                  }
               }
            }

            faceCtr = j * threshold + degenerateCount;
            if (numLeftover != 0) {
               currentStrip = new NvStripInfo(startInfo, 0, -1);
               i = 0;
               boolean firstTime = true;

               while(i < numLeftover) {
                  if (!isDegenerate(allStrips.get(i).m_faces.get(faceCtr))) {
                     ++i;
                     firstTime = false;
                     currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                  } else if (!firstTime) {
                     currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
                  } else {
                     ++faceCtr;
                  }
               }

               tempStrips.add(currentStrip);
            }
         } else {
            currentStrip = new NvStripInfo(startInfo, 0, -1);

            for(j = 0; j < allStrips.get(i).m_faces.size(); ++j) {
               currentStrip.m_faces.add(allStrips.get(i).m_faces.get(j));
            }

            tempStrips.add(currentStrip);
         }
      }

      NvStripInfoVec tempStrips2 = new NvStripInfoVec();
      this.removeSmallStrips(tempStrips, tempStrips2, outFaceList);
      outStrips.clear();
      if (tempStrips2.size() != 0) {
         VertexCache vcache = new VertexCache(this.m_cacheSize);
         float bestNumHits = -1.0F;
         numTimes = 0;
         numLeftover = 0;
         float minCost = 10000.0F;

         for(faceCtr = 0; faceCtr < tempStrips2.size(); ++faceCtr) {
            i = 0;

            for(j = 0; j < tempStrips2.get(faceCtr).m_faces.size(); ++j) {
               i += this.numNeighbors(tempStrips2.get(faceCtr).m_faces.get(j), edgeInfos);
            }

            float currCost = (float)i / (float)tempStrips2.get(faceCtr).m_faces.size();
            if (currCost < minCost) {
               minCost = currCost;
               numLeftover = faceCtr;
            }
         }

         this.updateCacheStrip(vcache, tempStrips2.get(numLeftover));
         outStrips.add(tempStrips2.get(numLeftover));
         tempStrips2.get(numLeftover).m_visited = true;
         boolean wantsCW = tempStrips2.get(numLeftover).m_faces.size() % 2 == 0;

         while(true) {
            bestNumHits = -1.0F;

            for(i = 0; i < tempStrips2.size(); ++i) {
               if (!tempStrips2.get(i).m_visited) {
                  float numHits = this.calcNumHitsStrip(vcache, tempStrips2.get(i));
                  if (numHits > bestNumHits) {
                     bestNumHits = numHits;
                     numTimes = i;
                  } else if (numHits >= bestNumHits) {
                     NvStripInfo strip = tempStrips2.get(i);
                     int nStripFaceCount = strip.m_faces.size();
                     NvFaceInfo tFirstFace = new NvFaceInfo(strip.m_faces.get(0).m_v0, strip.m_faces.get(0).m_v1, strip.m_faces.get(0).m_v2);
                     if (nStripFaceCount > 1) {
                        int nUnique = getUniqueVertexInB(strip.m_faces.get(1), tFirstFace);
                        int temp;
                        if (nUnique == tFirstFace.m_v1) {
                           temp = tFirstFace.m_v0;
                           tFirstFace.m_v0 = tFirstFace.m_v1;
                           tFirstFace.m_v1 = temp;
                        } else if (nUnique == tFirstFace.m_v2) {
                           temp = tFirstFace.m_v0;
                           tFirstFace.m_v0 = tFirstFace.m_v2;
                           tFirstFace.m_v2 = temp;
                        }

                        if (nStripFaceCount > 2) {
                           int[] nShared = new int[2];
                           getSharedVertices(strip.m_faces.get(2), tFirstFace, nShared);
                           if (nShared[0] == tFirstFace.m_v1 && nShared[1] == -1) {
                              int temp = tFirstFace.m_v1;
                              tFirstFace.m_v1 = tFirstFace.m_v2;
                              tFirstFace.m_v2 = temp;
                           }
                        }
                     }

                     if (wantsCW == this.isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1)) {
                        numTimes = i;
                     }
                  }
               }
            }

            if (bestNumHits == -1.0F) {
               break;
            }

            tempStrips2.get(numTimes).m_visited = true;
            this.updateCacheStrip(vcache, tempStrips2.get(numTimes));
            outStrips.add(tempStrips2.get(numTimes));
            wantsCW = tempStrips2.get(numTimes).m_faces.size() % 2 == 0 ? wantsCW : !wantsCW;
         }
      }

   }

   protected void removeSmallStrips(NvStripInfoVec allStrips, NvStripInfoVec allBigStrips, NvFaceInfoVec faceList) {
      faceList.clear();
      allBigStrips.clear();
      NvFaceInfoVec tempFaceList = new NvFaceInfoVec();

      for(int i = 0; i < allStrips.size(); ++i) {
         if (allStrips.get(i).m_faces.size() < this.m_minStripLength) {
            for(int j = 0; j < allStrips.get(i).m_faces.size(); ++j) {
               tempFaceList.add(allStrips.get(i).m_faces.get(j));
            }
         } else {
            allBigStrips.add(allStrips.get(i));
         }
      }

      if (tempFaceList.size() > 0) {
         boolean[] visitedList = new boolean[tempFaceList.size()];
         VertexCache vcache = new VertexCache(this.m_cacheSize);
         int bestNumHits = true;
         int numHits = false;
         int bestIndex = 0;

         while(true) {
            int bestNumHits = -1;

            for(int i = 0; i < tempFaceList.size(); ++i) {
               if (!visitedList[i]) {
                  int numHits = this.calcNumHitsFace(vcache, tempFaceList.get(i));
                  if (numHits > bestNumHits) {
                     bestNumHits = numHits;
                     bestIndex = i;
                  }
               }
            }

            if (bestNumHits == -1) {
               break;
            }

            visitedList[bestIndex] = true;
            this.updateCacheFace(vcache, tempFaceList.get(bestIndex));
            faceList.add(tempFaceList.get(bestIndex));
         }
      }

   }

   protected boolean findTraversal(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos, NvStripInfo strip, NvStripStartInfo startInfo) {
      int v = strip.m_startInfo.m_toV1 ? strip.m_startInfo.m_startEdge.m_v1 : strip.m_startInfo.m_startEdge.m_v0;
      NvFaceInfo untouchedFace = null;

      NvEdgeInfo edgeIter;
      for(edgeIter = edgeInfos.get(v); edgeIter != null; edgeIter = edgeIter.m_v0 == v ? edgeIter.m_nextV0 : edgeIter.m_nextV1) {
         NvFaceInfo face0 = edgeIter.m_face0;
         NvFaceInfo face1 = edgeIter.m_face1;
         if (face0 != null && !strip.isInStrip(face0) && face1 != null && !strip.isMarked(face1)) {
            untouchedFace = face1;
            break;
         }

         if (face1 != null && !strip.isInStrip(face1) && face0 != null && !strip.isMarked(face0)) {
            untouchedFace = face0;
            break;
         }
      }

      startInfo.m_startFace = untouchedFace;
      startInfo.m_startEdge = edgeIter;
      if (edgeIter != null) {
         if (strip.sharesEdge(startInfo.m_startFace, edgeInfos)) {
            startInfo.m_toV1 = edgeIter.m_v0 == v;
         } else {
            startInfo.m_toV1 = edgeIter.m_v1 == v;
         }
      }

      return startInfo.m_startFace != null;
   }

   protected void commitStrips(NvStripInfoVec allStrips, NvStripInfoVec strips) {
      int numStrips = strips.size();

      for(int i = 0; i < numStrips; ++i) {
         NvStripInfo strip = strips.get(i);
         strip.m_experimentId = -1;
         allStrips.add(strip);
         NvFaceInfoVec faces = strips.get(i).m_faces;
         int numFaces = faces.size();

         for(int j = 0; j < numFaces; ++j) {
            strip.markTriangle(faces.get(j));
         }
      }

   }

   protected float avgStripSize(NvStripInfoVec strips) {
      int sizeAccum = 0;
      int numStrips = strips.size();

      for(int i = 0; i < numStrips; ++i) {
         NvStripInfo strip = strips.get(i);
         sizeAccum += strip.m_faces.size();
         sizeAccum -= strip.m_numDegenerates;
      }

      return (float)sizeAccum / (float)numStrips;
   }

   protected int findStartPoint(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos) {
      int bestCtr = -1;
      int bestIndex = -1;

      for(int i = 0; i < faceInfos.size(); ++i) {
         int ctr = 0;
         if (findOtherFace(edgeInfos, faceInfos.get(i).m_v0, faceInfos.get(i).m_v1, faceInfos.get(i)) == null) {
            ++ctr;
         }

         if (findOtherFace(edgeInfos, faceInfos.get(i).m_v1, faceInfos.get(i).m_v2, faceInfos.get(i)) == null) {
            ++ctr;
         }

         if (findOtherFace(edgeInfos, faceInfos.get(i).m_v2, faceInfos.get(i).m_v0, faceInfos.get(i)) == null) {
            ++ctr;
         }

         if (ctr > bestCtr) {
            bestCtr = ctr;
            bestIndex = i;
         }
      }

      if (bestCtr == 0) {
         return -1;
      } else {
         return bestIndex;
      }
   }

   protected void updateCacheStrip(VertexCache vcache, NvStripInfo strip) {
      for(int i = 0; i < strip.m_faces.size(); ++i) {
         if (!vcache.inCache(strip.m_faces.get(i).m_v0)) {
            vcache.addEntry(strip.m_faces.get(i).m_v0);
         }

         if (!vcache.inCache(strip.m_faces.get(i).m_v1)) {
            vcache.addEntry(strip.m_faces.get(i).m_v1);
         }

         if (!vcache.inCache(strip.m_faces.get(i).m_v2)) {
            vcache.addEntry(strip.m_faces.get(i).m_v2);
         }
      }

   }

   protected void updateCacheFace(VertexCache vcache, NvFaceInfo face) {
      if (!vcache.inCache(face.m_v0)) {
         vcache.addEntry(face.m_v0);
      }

      if (!vcache.inCache(face.m_v1)) {
         vcache.addEntry(face.m_v1);
      }

      if (!vcache.inCache(face.m_v2)) {
         vcache.addEntry(face.m_v2);
      }

   }

   protected float calcNumHitsStrip(VertexCache vcache, NvStripInfo strip) {
      int numHits = 0;
      int numFaces = 0;

      for(int i = 0; i < strip.m_faces.size(); ++i) {
         if (vcache.inCache(strip.m_faces.get(i).m_v0)) {
            ++numHits;
         }

         if (vcache.inCache(strip.m_faces.get(i).m_v1)) {
            ++numHits;
         }

         if (vcache.inCache(strip.m_faces.get(i).m_v2)) {
            ++numHits;
         }

         ++numFaces;
      }

      return (float)numHits / (float)numFaces;
   }

   protected int calcNumHitsFace(VertexCache vcache, NvFaceInfo face) {
      int numHits = 0;
      if (vcache.inCache(face.m_v0)) {
         ++numHits;
      }

      if (vcache.inCache(face.m_v1)) {
         ++numHits;
      }

      if (vcache.inCache(face.m_v2)) {
         ++numHits;
      }

      return numHits;
   }

   protected int numNeighbors(NvFaceInfo face, NvEdgeInfoVec edgeInfoVec) {
      int numNeighbors = 0;
      if (findOtherFace(edgeInfoVec, face.m_v0, face.m_v1, face) != null) {
         ++numNeighbors;
      }

      if (findOtherFace(edgeInfoVec, face.m_v1, face.m_v2, face) != null) {
         ++numNeighbors;
      }

      if (findOtherFace(edgeInfoVec, face.m_v2, face.m_v0, face) != null) {
         ++numNeighbors;
      }

      return numNeighbors;
   }

   protected void buildStripifyInfo(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos, int maxIndex) {
      int numIndices = this.m_indices.size();
      faceInfos.ensureCapacity(numIndices / 3);

      int numTriangles;
      for(numTriangles = 0; numTriangles < maxIndex + 1; ++numTriangles) {
         edgeInfos.add((NvEdgeInfo)null);
      }

      numTriangles = numIndices / 3;
      int index = 0;
      boolean[] faceUpdated = new boolean[3];

      for(int i = 0; i < numTriangles; ++i) {
         boolean mightAlreadyExist = true;
         faceUpdated[0] = false;
         faceUpdated[1] = false;
         faceUpdated[2] = false;
         int v0 = this.m_indices.get(index++);
         int v1 = this.m_indices.get(index++);
         int v2 = this.m_indices.get(index++);
         if (!isDegenerate(v0, v1, v2)) {
            NvFaceInfo faceInfo = new NvFaceInfo(v0, v1, v2);
            NvEdgeInfo edgeInfo01 = findEdgeInfo(edgeInfos, v0, v1);
            if (edgeInfo01 == null) {
               mightAlreadyExist = false;
               edgeInfo01 = new NvEdgeInfo(v0, v1);
               edgeInfo01.m_nextV0 = edgeInfos.get(v0);
               edgeInfo01.m_nextV1 = edgeInfos.get(v1);
               edgeInfos.set(v0, edgeInfo01);
               edgeInfos.set(v1, edgeInfo01);
               edgeInfo01.m_face0 = faceInfo;
            } else if (edgeInfo01.m_face1 == null) {
               edgeInfo01.m_face1 = faceInfo;
               faceUpdated[0] = true;
            } else {
               System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
            }

            NvEdgeInfo edgeInfo12 = findEdgeInfo(edgeInfos, v1, v2);
            if (edgeInfo12 == null) {
               mightAlreadyExist = false;
               edgeInfo12 = new NvEdgeInfo(v1, v2);
               edgeInfo12.m_nextV0 = edgeInfos.get(v1);
               edgeInfo12.m_nextV1 = edgeInfos.get(v2);
               edgeInfos.set(v1, edgeInfo12);
               edgeInfos.set(v2, edgeInfo12);
               edgeInfo12.m_face0 = faceInfo;
            } else if (edgeInfo12.m_face1 == null) {
               edgeInfo12.m_face1 = faceInfo;
               faceUpdated[1] = true;
            } else {
               System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
            }

            NvEdgeInfo edgeInfo20 = findEdgeInfo(edgeInfos, v2, v0);
            if (edgeInfo20 == null) {
               mightAlreadyExist = false;
               edgeInfo20 = new NvEdgeInfo(v2, v0);
               edgeInfo20.m_nextV0 = edgeInfos.get(v2);
               edgeInfo20.m_nextV1 = edgeInfos.get(v0);
               edgeInfos.set(v2, edgeInfo20);
               edgeInfos.set(v0, edgeInfo20);
               edgeInfo20.m_face0 = faceInfo;
            } else if (edgeInfo20.m_face1 == null) {
               edgeInfo20.m_face1 = faceInfo;
               faceUpdated[2] = true;
            } else {
               System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
            }

            if (mightAlreadyExist) {
               if (this.alreadyExists(faceInfo, faceInfos)) {
                  if (faceUpdated[0]) {
                     edgeInfo01.m_face1 = null;
                  }

                  if (faceUpdated[1]) {
                     edgeInfo12.m_face1 = null;
                  }

                  if (faceUpdated[2]) {
                     edgeInfo20.m_face1 = null;
                  }
               } else {
                  faceInfos.add(faceInfo);
               }
            } else {
               faceInfos.add(faceInfo);
            }
         }
      }

   }

   protected boolean alreadyExists(NvFaceInfo faceInfo, NvFaceInfoVec faceInfos) {
      for(int i = 0; i < faceInfos.size(); ++i) {
         NvFaceInfo fi = faceInfos.get(i);
         if (fi.m_v0 == faceInfo.m_v0 && fi.m_v1 == faceInfo.m_v1 && fi.m_v2 == faceInfo.m_v2) {
            return true;
         }
      }

      return false;
   }

   public static int getUniqueVertexInB(NvFaceInfo faceA, NvFaceInfo faceB) {
      int facev0 = faceB.m_v0;
      if (facev0 != faceA.m_v0 && facev0 != faceA.m_v1 && facev0 != faceA.m_v2) {
         return facev0;
      } else {
         int facev1 = faceB.m_v1;
         if (facev1 != faceA.m_v0 && facev1 != faceA.m_v1 && facev1 != faceA.m_v2) {
            return facev1;
         } else {
            int facev2 = faceB.m_v2;
            return facev2 != faceA.m_v0 && facev2 != faceA.m_v1 && facev2 != faceA.m_v2 ? facev2 : -1;
         }
      }
   }

   public static void getSharedVertices(NvFaceInfo faceA, NvFaceInfo faceB, int[] vertex) {
      vertex[0] = -1;
      vertex[1] = -1;
      int facev0 = faceB.m_v0;
      if (facev0 == faceA.m_v0 || facev0 == faceA.m_v1 || facev0 == faceA.m_v2) {
         if (vertex[0] != -1) {
            vertex[1] = facev0;
            return;
         }

         vertex[0] = facev0;
      }

      int facev1 = faceB.m_v1;
      if (facev1 == faceA.m_v0 || facev1 == faceA.m_v1 || facev1 == faceA.m_v2) {
         if (vertex[0] != -1) {
            vertex[1] = facev1;
            return;
         }

         vertex[0] = facev1;
      }

      int facev2 = faceB.m_v2;
      if (facev2 == faceA.m_v0 || facev2 == faceA.m_v1 || facev2 == faceA.m_v2) {
         if (vertex[0] != -1) {
            vertex[1] = facev2;
            return;
         }

         vertex[0] = facev2;
      }

   }

   public static boolean isDegenerate(NvFaceInfo face) {
      if (face.m_v0 == face.m_v1) {
         return true;
      } else if (face.m_v0 == face.m_v2) {
         return true;
      } else {
         return face.m_v1 == face.m_v2;
      }
   }

   public static boolean isDegenerate(int v0, int v1, int v2) {
      if (v0 == v1) {
         return true;
      } else if (v0 == v2) {
         return true;
      } else {
         return v1 == v2;
      }
   }

   protected static int getNextIndex(IntVec indices, NvFaceInfo face) {
      int numIndices = indices.size();

      assert numIndices >= 2;

      int v0 = indices.get(numIndices - 2);
      int v1 = indices.get(numIndices - 1);
      int fv0 = face.m_v0;
      int fv1 = face.m_v1;
      int fv2 = face.m_v2;
      if (fv0 != v0 && fv0 != v1) {
         if (fv1 != v0 && fv1 != v1 || fv2 != v0 && fv2 != v1) {
            System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
            System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
         }

         return fv0;
      } else if (fv1 != v0 && fv1 != v1) {
         if (fv0 != v0 && fv0 != v1 || fv2 != v0 && fv2 != v1) {
            System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
            System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
         }

         return fv1;
      } else if (fv2 != v0 && fv2 != v1) {
         if (fv0 != v0 && fv0 != v1 || fv1 != v0 && fv1 != v1) {
            System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
            System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
         }

         return fv2;
      } else if (fv0 != fv1 && fv0 != fv2) {
         if (fv1 != fv0 && fv1 != fv2) {
            return fv2 != fv0 && fv2 != fv1 ? -1 : fv2;
         } else {
            return fv1;
         }
      } else {
         return fv0;
      }
   }

   protected static NvEdgeInfo findEdgeInfo(NvEdgeInfoVec edgeInfos, int v0, int v1) {
      NvEdgeInfo infoIter = edgeInfos.get(v0);

      while(infoIter != null) {
         if (infoIter.m_v0 == v0) {
            if (infoIter.m_v1 == v1) {
               return infoIter;
            }

            infoIter = infoIter.m_nextV0;
         } else {
            assert infoIter.m_v1 == v0;

            if (infoIter.m_v0 == v1) {
               return infoIter;
            }

            infoIter = infoIter.m_nextV1;
         }
      }

      return null;
   }

   protected static NvFaceInfo findOtherFace(NvEdgeInfoVec edgeInfos, int v0, int v1, NvFaceInfo faceInfo) {
      NvEdgeInfo edgeInfo = findEdgeInfo(edgeInfos, v0, v1);
      if (edgeInfo == null && v0 == v1) {
         return null;
      } else {
         assert edgeInfo != null;

         return edgeInfo.m_face0 == faceInfo ? edgeInfo.m_face1 : edgeInfo.m_face0;
      }
   }
}
