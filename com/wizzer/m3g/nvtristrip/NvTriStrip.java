package com.wizzer.m3g.nvtristrip;

import java.util.Arrays;

public class NvTriStrip {
   public static final int CACHESIZE_GEFORCE1_2 = 16;
   public static final int CACHESIZE_GEFORCE3 = 24;
   private int m_cacheSize = 16;
   private boolean m_stitchStrips = true;
   private int m_minStripSize;
   private boolean m_listsOnly;
   private int m_restartVal;
   private boolean m_restart;

   public void enableRestart(int restartVal) {
      this.m_restartVal = restartVal;
      this.m_restart = true;
   }

   public void disableRestart() {
      this.m_restart = false;
   }

   public void setListsOnly(boolean listsOnly) {
      this.m_listsOnly = listsOnly;
   }

   public void setCacheSize(int cacheSize) {
      this.m_cacheSize = cacheSize;
   }

   public void setStitchStrips(boolean stitchStrips) {
      this.m_stitchStrips = stitchStrips;
   }

   public void setMinStripSize(int minStripSize) {
      this.m_minStripSize = minStripSize;
   }

   private boolean sameTriangle(int firstTri0, int firstTri1, int firstTri2, int secondTri0, int secondTri1, int secondTri2) {
      boolean isSame = false;
      if (firstTri0 == secondTri0) {
         if (firstTri1 == secondTri1 && firstTri2 == secondTri2) {
            isSame = true;
         }
      } else if (firstTri0 == secondTri1) {
         if (firstTri1 == secondTri2 && firstTri2 == secondTri0) {
            isSame = true;
         }
      } else if (firstTri0 == secondTri2 && firstTri1 == secondTri0 && firstTri2 == secondTri1) {
         isSame = true;
      }

      return isSame;
   }

   private boolean testTriangle(int v0, int v1, int v2, NvFaceInfoVec[] in_bins, int NUMBINS) {
      boolean isLegit = false;
      int ctr = v0 % NUMBINS;

      int k;
      for(k = 0; k < in_bins[ctr].size(); ++k) {
         if (this.sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2)) {
            isLegit = true;
            break;
         }
      }

      if (!isLegit) {
         ctr = v1 % NUMBINS;

         for(k = 0; k < in_bins[ctr].size(); ++k) {
            if (this.sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2)) {
               isLegit = true;
               break;
            }
         }

         if (!isLegit) {
            ctr = v2 % NUMBINS;

            for(k = 0; k < in_bins[ctr].size(); ++k) {
               if (this.sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2)) {
                  isLegit = true;
                  break;
               }
            }
         }
      }

      return isLegit;
   }

   public PrimitiveGroup[] generateStrips(int[] in_indices) {
      return this.generateStrips(in_indices, false);
   }

   public PrimitiveGroup[] generateStrips(int[] in_indices, boolean validateEnabled) {
      int numGroups = false;
      IntVec tempIndices = new IntVec(in_indices.length);
      int maxIndex = 0;
      int minIndex = Integer.MAX_VALUE;

      for(int i = 0; i < in_indices.length; ++i) {
         tempIndices.add(in_indices[i]);
         if (in_indices[i] > maxIndex) {
            maxIndex = in_indices[i];
         }

         if (in_indices[i] < minIndex) {
            minIndex = in_indices[i];
         }
      }

      NvStripInfoVec tempStrips = new NvStripInfoVec();
      NvFaceInfoVec tempFaces = new NvFaceInfoVec();
      NvStripifier stripifier = new NvStripifier();
      stripifier.stripify(tempIndices, this.m_cacheSize, this.m_minStripSize, maxIndex, tempStrips, tempFaces);
      IntVec stripIndices = new IntVec();
      int numSeparateStrips = false;
      PrimitiveGroup[] primGroups;
      PrimitiveGroup[] primGroupArray;
      int startingLoc;
      int i;
      int j;
      int v0;
      int v0;
      int numGroups;
      boolean flip;
      if (this.m_listsOnly) {
         numGroups = 1;
         primGroups = new PrimitiveGroup[]{new PrimitiveGroup()};
         primGroupArray = primGroups;
         startingLoc = 0;

         for(i = 0; i < tempStrips.size(); ++i) {
            startingLoc += tempStrips.get(i).m_faces.size() * 3;
         }

         startingLoc += tempFaces.size() * 3;
         primGroups[0].m_type = 0;
         primGroups[0].m_numIndices = startingLoc;
         primGroups[0].m_indices = new int[startingLoc];
         i = 0;

         for(j = 0; j < tempStrips.size(); ++j) {
            for(v0 = 0; v0 < tempStrips.get(j).m_faces.size(); ++v0) {
               if (!NvStripifier.isDegenerate(tempStrips.get(j).m_faces.get(v0))) {
                  primGroupArray[0].m_indices[i++] = tempStrips.get(j).m_faces.get(v0).m_v0;
                  primGroupArray[0].m_indices[i++] = tempStrips.get(j).m_faces.get(v0).m_v1;
                  primGroupArray[0].m_indices[i++] = tempStrips.get(j).m_faces.get(v0).m_v2;
               } else {
                  primGroupArray[0].m_numIndices -= 3;
               }
            }
         }

         for(j = 0; j < tempFaces.size(); ++j) {
            primGroupArray[0].m_indices[i++] = tempFaces.get(j).m_v0;
            primGroupArray[0].m_indices[i++] = tempFaces.get(j).m_v1;
            primGroupArray[0].m_indices[i++] = tempFaces.get(j).m_v2;
         }
      } else {
         int numSeparateStrips = stripifier.createStrips(tempStrips, stripIndices, this.m_stitchStrips, this.m_restart, this.m_restartVal);

         assert this.m_stitchStrips && numSeparateStrips == 1 || !this.m_stitchStrips;

         numGroups = numSeparateStrips;
         if (tempFaces.size() != 0) {
            numGroups = numSeparateStrips + 1;
         }

         primGroups = new PrimitiveGroup[numGroups];

         for(int i = 0; i < numGroups; ++i) {
            primGroups[i] = new PrimitiveGroup();
         }

         primGroupArray = primGroups;
         startingLoc = 0;

         for(i = 0; i < numSeparateStrips; ++i) {
            flip = false;
            if (this.m_stitchStrips) {
               j = stripIndices.size();
            } else {
               int i = false;

               for(v0 = startingLoc; v0 < stripIndices.size() && stripIndices.get(v0) != -1; ++v0) {
               }

               j = v0 - startingLoc;
            }

            primGroupArray[i].m_type = 1;
            primGroupArray[i].m_indices = new int[j];
            primGroupArray[i].m_numIndices = j;
            v0 = 0;

            for(v0 = startingLoc; v0 < j + startingLoc; ++v0) {
               primGroupArray[i].m_indices[v0++] = stripIndices.get(v0);
            }

            startingLoc += j + 1;
         }

         if (tempFaces.size() != 0) {
            i = numGroups - 1;
            primGroupArray[i].m_type = 0;
            primGroupArray[i].m_indices = new int[tempFaces.size() * 3];
            primGroupArray[i].m_numIndices = tempFaces.size() * 3;
            j = 0;

            for(v0 = 0; v0 < tempFaces.size(); ++v0) {
               primGroupArray[i].m_indices[j++] = tempFaces.get(v0).m_v0;
               primGroupArray[i].m_indices[j++] = tempFaces.get(v0).m_v1;
               primGroupArray[i].m_indices[j++] = tempFaces.get(v0).m_v2;
            }
         }
      }

      if (validateEnabled) {
         int NUMBINS = 100;
         NvFaceInfoVec[] in_bins = new NvFaceInfoVec[NUMBINS];

         for(i = 0; i < NUMBINS; ++i) {
            in_bins[i] = new NvFaceInfoVec();
         }

         for(i = 0; i < in_indices.length; i += 3) {
            NvFaceInfo faceInfo = new NvFaceInfo(in_indices[i], in_indices[i + 1], in_indices[i + 2]);
            in_bins[in_indices[i] % NUMBINS].add(faceInfo);
         }

         label131:
         for(i = 0; i < numGroups; ++i) {
            int v1;
            switch(primGroups[i].m_type) {
            case 0:
               j = 0;

               while(true) {
                  if (j >= primGroups[i].m_numIndices) {
                     continue label131;
                  }

                  v0 = primGroups[i].m_indices[j];
                  v0 = primGroups[i].m_indices[j + 1];
                  v1 = primGroups[i].m_indices[j + 2];
                  if (!NvStripifier.isDegenerate(v0, v0, v1) && !this.testTriangle(v0, v0, v1, in_bins, NUMBINS)) {
                     return null;
                  }

                  j += 3;
               }
            case 1:
               flip = false;

               for(v0 = 2; v0 < primGroups[i].m_numIndices; ++v0) {
                  v0 = primGroups[i].m_indices[v0 - 2];
                  v1 = primGroups[i].m_indices[v0 - 1];
                  int v2 = primGroups[i].m_indices[v0];
                  if (flip) {
                     int swap = v1;
                     v1 = v2;
                     v2 = swap;
                  }

                  if (NvStripifier.isDegenerate(v0, v1, v2)) {
                     flip = !flip;
                  } else {
                     if (!this.testTriangle(v0, v1, v2, in_bins, NUMBINS)) {
                        return null;
                     }

                     flip = !flip;
                  }
               }
            case 2:
            }
         }
      }

      return primGroups;
   }

   public PrimitiveGroup[] remapIndices(PrimitiveGroup[] in_primGroups, int numVerts) {
      PrimitiveGroup[] remappedGroups = new PrimitiveGroup[in_primGroups.length];

      for(int i = 0; i < remappedGroups.length; ++i) {
         remappedGroups[i] = new PrimitiveGroup();
      }

      int[] indexCache = new int[numVerts];
      Arrays.fill(indexCache, -1);
      int indexCtr = 0;

      for(int i = 0; i < in_primGroups.length; ++i) {
         int numIndices = in_primGroups[i].m_numIndices;
         remappedGroups[i].m_type = in_primGroups[i].m_type;
         remappedGroups[i].m_numIndices = numIndices;
         remappedGroups[i].m_indices = new int[numIndices];

         for(int j = 0; j < numIndices; ++j) {
            int cachedIndex = indexCache[in_primGroups[i].m_indices[j]];
            if (cachedIndex == -1) {
               remappedGroups[i].m_indices[j] = indexCtr;
               indexCache[in_primGroups[i].m_indices[j]] = indexCtr++;
            } else {
               remappedGroups[i].m_indices[j] = cachedIndex;
            }
         }
      }

      return remappedGroups;
   }
}
