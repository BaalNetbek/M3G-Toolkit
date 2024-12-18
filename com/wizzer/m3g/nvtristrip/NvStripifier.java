// COPYRIGHT_BEGIN
//
// Copyright (C) 2000-2008  Wizzer Works (msm@wizzerworks.com)
// 
// This file is part of the M3G Toolkit.
//
// The M3G Toolkit is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
//
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// COPYRIGHT_END

// Declare package.
package com.wizzer.m3g.nvtristrip;

class NvStripifier
{
	public static final int CACHE_INEFFICIENCY  = 6;

	protected IntVec m_indices;
	protected int m_cacheSize;
	protected int m_minStripLength;
	protected float m_meshJump;
	protected boolean m_firstTimeResetPoint;

	public NvStripifier()
	{
		m_indices = new IntVec();
	}

	/**
	 * Strip
	 * 
	 * @param in_indices The input indices of the mesh to stripify.
	 * @param in_cacheSize The target cache size.
	 * @param in_minStripLength The minimal strip length.
	 * @param maxIndex The maximum index to process.
	 * @param outStrips The structure to place the strips in.
	 * @param outFaceList The structure to place the faces in.
	 */
	public void stripify(IntVec in_indices, int in_cacheSize, int in_minStripLength, 
		int maxIndex, NvStripInfoVec outStrips, NvFaceInfoVec outFaceList)
	{
		m_meshJump = 0.0f;
		m_firstTimeResetPoint = true; // Used in FindGoodResetPoint()
		// The number of times to run the experiments
		int numSamples = 10;
		// The cache size clamped to one.
		m_cacheSize = Math.max(1, in_cacheSize - CACHE_INEFFICIENCY);
		m_minStripLength = in_minStripLength;  // This is the strip size threshold below which we dump the strip into a list
		m_indices = in_indices;
		// Build the stripification info.
		NvFaceInfoVec allFaceInfos = new NvFaceInfoVec();
		NvEdgeInfoVec allEdgeInfos = new NvEdgeInfoVec();
		buildStripifyInfo(allFaceInfos, allEdgeInfos, maxIndex);
		NvStripInfoVec allStrips = new NvStripInfoVec();
		// stripify
		findAllStrips(allStrips, allFaceInfos, allEdgeInfos, numSamples);
		// Split up the strips into cache friendly pieces,  optimize them,
		// then dump these into outStrips.
		splitUpStripsAndOptimize(allStrips, outStrips, allEdgeInfos, outFaceList);
	}

	/**
	 * Generates actual strips from the list-in-strip-order.
	 */ 
	public int createStrips(NvStripInfoVec allStrips, IntVec stripIndices, boolean stitchStrips,
		boolean restart,int restartVal)
	{
		int numSeparateStrips = 0;
		NvFaceInfo tLastFace = new NvFaceInfo(0,0,0);
		int nStripCount = allStrips.size();
		assert(nStripCount > 0);
		// We infer the cw/ccw ordering depending on the number of indices.
		// This is screwed up by the fact that we insert -1s to denote changing strips.
		// this is to account for that
		int accountForNegatives = 0;
		for (int i = 0; i < nStripCount; i++)
		{
			NvStripInfo strip = allStrips.get(i);
			int nStripFaceCount = strip.m_faces.size();
			assert(nStripFaceCount > 0);
			// Handle the first face in the strip.
			{
				NvFaceInfo tFirstFace = new NvFaceInfo(strip.m_faces.get(0).m_v0, strip.m_faces.get(0).m_v1, strip.m_faces.get(0).m_v2);
				// If there is a second face,reorder vertices such that the
				// unique vertex is first.
				if (nStripFaceCount > 1)
				{
					int nUnique = NvStripifier.getUniqueVertexInB(strip.m_faces.get(1), tFirstFace);
					if (nUnique == tFirstFace.m_v1)
					{
						int temp = tFirstFace.m_v0;
						tFirstFace.m_v0 = tFirstFace.m_v1;
						tFirstFace.m_v1 = temp;
					}
					else if (nUnique == tFirstFace.m_v2)
					{
						int temp = tFirstFace.m_v0;
						tFirstFace.m_v0 = tFirstFace.m_v2;
						tFirstFace.m_v2 = temp;
					}
					// If there is a third face,reorder vertices such that the
					// shared vertex is last.
					if (nStripFaceCount > 2)
					{
						if (isDegenerate(strip.m_faces.get(1)))
						{
							int pivot = strip.m_faces.get(1).m_v1;
							if (tFirstFace.m_v1 == pivot)
							{
								int temp = tFirstFace.m_v1;
								tFirstFace.m_v1 = tFirstFace.m_v2;
								tFirstFace.m_v2 = temp;
							}
						}
						else
						{
							int nShared[] = new int[2];
							getSharedVertices(strip.m_faces.get(2), tFirstFace,nShared);
							if ((nShared[0] == tFirstFace.m_v1) && (nShared[1] == -1))
							{
								int temp = tFirstFace.m_v1;
								tFirstFace.m_v1 = tFirstFace.m_v2;
								tFirstFace.m_v2 = temp;
							}
						}
					}
				}
				if ((i == 0) || ! stitchStrips || restart)
				{
					if (! isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1))
						stripIndices.add(tFirstFace.m_v0);
				}
				else
				{
					// Double tap the first in the new strip.
					stripIndices.add(tFirstFace.m_v0);
					// Check CW/CCW ordering.
					if (nextIsCW(stripIndices.size() - accountForNegatives) != isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1))
					{
						stripIndices.add(tFirstFace.m_v0);
					}
				}
				stripIndices.add(tFirstFace.m_v0);
				stripIndices.add(tFirstFace.m_v1);
				stripIndices.add(tFirstFace.m_v2);
				// Update last face info.
				tLastFace.set(tFirstFace);
			}
			for (int j = 1; j < nStripFaceCount; j++)
			{
				int nUnique = getUniqueVertexInB(tLastFace, strip.m_faces.get(j));
				if (nUnique != -1)
				{
					stripIndices.add(nUnique);
					// Update last face info.
					tLastFace.m_v0 = tLastFace.m_v1;
					tLastFace.m_v1 = tLastFace.m_v2;
					tLastFace.m_v2 = nUnique;
				}
				else
				{
					// We've hit a degenerate.
					stripIndices.add(strip.m_faces.get(j).m_v2);
					tLastFace.m_v0 = strip.m_faces.get(j).m_v0; // tLastFace.v1;
					tLastFace.m_v1 = strip.m_faces.get(j).m_v1; // tLastFace.v2;
					tLastFace.m_v2 = strip.m_faces.get(j).m_v2; // tLastFace.v1;
				}
			}
			// Double tap between strips.
			if (stitchStrips && ! restart)
			{
				if (i != nStripCount-1) stripIndices.add(tLastFace.m_v2);
			}
			else if (restart)
			{
				stripIndices.add(restartVal);
			}
			else
			{
				//-1 index indicates next strip.
				stripIndices.add(-1);
				accountForNegatives++;
				numSeparateStrips++;
			}
			// Update last face info.
			tLastFace.m_v0 = tLastFace.m_v1;
			tLastFace.m_v1 = tLastFace.m_v2;
			//tLastFace.v2 = tLastFace.v2;
		}
		if (stitchStrips || restart) numSeparateStrips=1;
		return numSeparateStrips;
	}

	protected boolean isMoneyFace(NvFaceInfo face)
	{
		return (faceContainsIndex(face, 800) && faceContainsIndex(face, 812) && 
				faceContainsIndex(face, 731));
	}

	protected boolean faceContainsIndex(NvFaceInfo face, int index)
	{
		return ((face.m_v0 == index) || (face.m_v1 == index) || (face.m_v2 == index));
	}

	// Returns true if the face is ordered in CW fashion.
	protected boolean isCW(NvFaceInfo faceInfo, int v0, int v1)
	{
		if (faceInfo.m_v0 == v0) return (faceInfo.m_v1 == v1);
		else if (faceInfo.m_v1 == v0) return (faceInfo.m_v2 == v1);
		else return (faceInfo.m_v0 == v1);
	}

	// Returns true if the next face should be ordered in CW fashion.
	protected boolean nextIsCW(int numIndices)
	{
		return ((numIndices % 2) == 0);
	}

	// A good reset point is one near other committed areas so that
	// we know that when we've made the longest strips its because
	// we're stripifying in the same general orientation.
	protected NvFaceInfo findGoodResetPoint(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos)
	{
		// We hop into different areas of the mesh to try to get
		// other large open spans done.  Areas of small strips can
		// just be left to triangle lists added at the end.
		NvFaceInfo result = null;
		if (result == null)
		{
			int numFaces = faceInfos.size();
			int startPoint;
			if (m_firstTimeResetPoint)
			{
				// First time, find a face with few neighbors (look for an edge of the mesh).
				startPoint = findStartPoint(faceInfos, edgeInfos);
				m_firstTimeResetPoint = false;
			}
			else startPoint = (int)(((float)numFaces - 1) * m_meshJump);
			if (startPoint == -1)
			{
				startPoint = (int)(((float)numFaces - 1) * m_meshJump);
				//meshJump += 0.1f;
				//if (meshJump > 1.0f)
				//	meshJump = .05f;
			}
			int i = startPoint;
			do
			{
				// If this guy isn't visited, try him.
				if (faceInfos.get(i).m_stripId < 0)
				{
					result = faceInfos.get(i);
					break;
				}
				// Update the index and clamp to 0-(numFaces-1).
				if (++i >= numFaces) i = 0;
			} while (i != startPoint);
			// Update the meshJump.
			m_meshJump += 0.1f;
			if (m_meshJump > 1.0f) m_meshJump = .05f;
		}
		// Return the best face we found.
		return result;
	}

	/**
	 * Does the stripification and puts output strips into vector <i>allStrips</i>
	 *
	 * Works by running a number of experiments in different areas of the mesh, and
	 * accepting the one which results in the longest strips.  It then accepts this, and moves
	 * on to a different area of the mesh.  We try to jump around the mesh some, to ensure that
	 * large open spans of strips get generated.
	 * 
	 * @param allStrips
	 * @param allFaceInfos
	 * @param allEdgeInfos
	 * @param numSamples
	 */
	protected void findAllStrips(NvStripInfoVec allStrips,NvFaceInfoVec allFaceInfos,NvEdgeInfoVec allEdgeInfos,int numSamples)
	{
		// The experiments.
		int experimentId = 0;
		int stripId = 0;
		boolean done = false;
		int loopCtr = 0;
		while (! done)
		{
			loopCtr++;
			//
			// PHASE 1: Set up numSamples * numEdges experiments
			//
			NvStripInfoVec experiments[] = new NvStripInfoVec[numSamples*6];
			for (int i = 0; i < experiments.length; i++)
				experiments[i] = new NvStripInfoVec();
			int experimentIndex = 0;
			NvFaceInfoVec resetPoints = new NvFaceInfoVec();
			for (int i = 0; i < numSamples; i++)
			{
				// Try to find another good reset point.
				// If there are none to be found, we are done.
				NvFaceInfo nextFace = findGoodResetPoint(allFaceInfos,allEdgeInfos);
				if (nextFace == null)
				{
					done=true;
					break;
				}
				// If we have already evaluated starting at this face in this slew
				// of experiments, then skip going any further.
				else if (resetPoints.contains(nextFace)) continue;
				// Trying it now...
				resetPoints.add(nextFace);
				// Otherwise, we shall now try experiments for starting on the 01,12,and 20 edges.
				assert(nextFace.m_stripId < 0);
				// Build the strip off of this face's 0-1 edge.
				NvEdgeInfo edge01 = findEdgeInfo(allEdgeInfos, nextFace.m_v0, nextFace.m_v1);
				NvStripInfo strip01 = new NvStripInfo(new NvStripStartInfo(nextFace, edge01, true), stripId++, experimentId++);
				experiments[experimentIndex++].add(strip01);
				// Build the strip off of this face's 1-0 edge.
				NvEdgeInfo edge10 = findEdgeInfo(allEdgeInfos, nextFace.m_v0, nextFace.m_v1);
				NvStripInfo strip10 = new NvStripInfo(new NvStripStartInfo(nextFace, edge10, false), stripId++, experimentId++);
				experiments[experimentIndex++].add(strip10);
				// Build the strip off of this face's 1-2 edge.
				NvEdgeInfo edge12 = findEdgeInfo(allEdgeInfos, nextFace.m_v1, nextFace.m_v2);
				NvStripInfo strip12 = new NvStripInfo(new NvStripStartInfo(nextFace,edge12, true), stripId++, experimentId++);
				experiments[experimentIndex++].add(strip12);
				// Build the strip off of this face's 2-1 edge.
				NvEdgeInfo edge21 = findEdgeInfo(allEdgeInfos, nextFace.m_v1, nextFace.m_v2);
				NvStripInfo strip21 = new NvStripInfo(new NvStripStartInfo(nextFace, edge21, false), stripId++, experimentId++);
				experiments[experimentIndex++].add(strip21);
				// Build the strip off of this face's 2-0 edge.
				NvEdgeInfo edge20 = findEdgeInfo(allEdgeInfos,nextFace.m_v2,nextFace.m_v0);
				NvStripInfo strip20 = new NvStripInfo(new NvStripStartInfo(nextFace, edge20, true),stripId++,experimentId++);
				experiments[experimentIndex++].add(strip20);
				// Build the strip off of this face's 0-2 edge.
				NvEdgeInfo edge02 = findEdgeInfo(allEdgeInfos, nextFace.m_v2, nextFace.m_v0);
				NvStripInfo strip02 = new NvStripInfo(new NvStripStartInfo(nextFace, edge02, false),  stripId++,experimentId++);
				experiments[experimentIndex++].add(strip02);
			}
			
			//
			// PHASE 2: Iterate through that we setup in the last phase
			// and really build each of the strips and strips that follow to see how
			// far we get
			//
			int numExperiments = experimentIndex;
			for (int i = 0; i < numExperiments; i++)
			{
				// Get the strip set and
				// build the first strip of the list.
				experiments[i].get(0).build(allEdgeInfos,allFaceInfos);
				int experimentId2 = experiments[i].get(0).m_experimentId;
				NvStripInfo stripIter = experiments[i].get(0);
				NvStripStartInfo startInfo = new NvStripStartInfo(null, null, false);
				while (findTraversal(allFaceInfos, allEdgeInfos, stripIter, startInfo))
				{
					// Create the new strip info.
					stripIter = new NvStripInfo(startInfo, stripId++, experimentId2);
					// Build the next strip.
					stripIter.build(allEdgeInfos,allFaceInfos);
					// Add it to the list.
					experiments[i].add(stripIter);
				}
			}
			
			//
			// Phase 3: Find the experiment that has the most promise.
			//
			int bestIndex = 0;
			double bestValue = 0;
			for (int i = 0; i < numExperiments; i++)
			{
				float avgStripSizeWeight = 1.0f;
				float numTrisWeight = 0.0f;
				float numStripsWeight = 0.0f;
				float avgStripSize = avgStripSize(experiments[i]);
				float numStrips=(float)experiments[i].size();
				float value = avgStripSize*avgStripSizeWeight+(numStrips*numStripsWeight);
				//float value = 1.f / numStrips;
				//float value = numStrips * avgStripSize;
				if (value > bestValue)
				{
					bestValue = value;
					bestIndex = i;
				}
			}
			
			//
			// Phase 4: commit the best experiment of the bunch.
			//
			commitStrips(allStrips, experiments[bestIndex]);
		}
	}

	/**
	 * Splits the input vector of strips (allStrips) into smaller, cache friendly pieces, then
	 * reorders these pieces to maximize cache hits.
	 * The final strips are output through outStrips
	 * 
	 * @param allStrips
	 * @param outStrips
	 * @param edgeInfos
	 * @param outFaceList
	 */
	protected void splitUpStripsAndOptimize(NvStripInfoVec allStrips, NvStripInfoVec outStrips, 
		NvEdgeInfoVec edgeInfos, NvFaceInfoVec outFaceList)
	{
		int threshold = m_cacheSize;
		NvStripInfoVec tempStrips = new NvStripInfoVec();
		int j;
		// Split up strips into threshold-sized pieces.
		for (int i = 0; i <allStrips.size(); i++)
		{
			NvStripInfo currentStrip = null;
			NvStripStartInfo startInfo = new NvStripStartInfo(null,null,false);
			int actualStripSize = 0;
			for (j = 0; j < allStrips.get(i).m_faces.size(); ++j)
			{
				if (! isDegenerate(allStrips.get(i).m_faces.get(j)))
					actualStripSize++;
			}
			if (actualStripSize/*allStrips[i].faces.size()*/ > threshold)
			{
				int numTimes = actualStripSize /*allStrips[i].faces.size()*//threshold;
				int numLeftover = actualStripSize /*allStrips[i].faces.size()*/%threshold;
				int degenerateCount = 0;
				for (j=0; j < numTimes; j++)
				{
					currentStrip = new NvStripInfo(startInfo, 0, -1);
					int faceCtr = j * threshold + degenerateCount;
					boolean firstTime = true;
					while (faceCtr < threshold + (j * threshold) + degenerateCount)
					{
						if (isDegenerate(allStrips.get(i).m_faces.get(faceCtr)))
						{
							degenerateCount++;
							// Last time or first time through, no need for a degenerate.
							if ((((faceCtr+1) != threshold + (j * threshold) + degenerateCount) ||
								 ((j == numTimes-1) && (numLeftover < 4) && (numLeftover > 0))) &&
								 ! firstTime)
							{
								currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
							}
							else ++faceCtr;
						}
						else
						{
							currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
							firstTime = false;
						}
					}
					/*
					 for (int faceCtr = j*threshold; faceCtr < threshold+(j*threshold); faceCtr++)
					 {
						 currentStrip.faces.add(allStrips[i].faces[faceCtr]);
					 }
					 */
					if (j == numTimes - 1) // Last time through.
					{
						if ((numLeftover < 4) && (numLeftover > 0)) // Way too small.
						{
							// Just add to last strip.
							int ctr = 0;
							while (ctr < numLeftover)
							{
								if (! isDegenerate(allStrips.get(i).m_faces.get(faceCtr)))
								{
								    currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
								    ++ctr;
								}
								else
								{
								    currentStrip.m_faces.add(allStrips.get(i).m_faces.get(faceCtr++));
								    ++degenerateCount;
								}
							}
							numLeftover = 0;
						}
					}
					tempStrips.add(currentStrip);
				}
				int leftOff = j * threshold + degenerateCount;
				if (numLeftover != 0)
				{
					currentStrip = new NvStripInfo(startInfo, 0, -1);
					int ctr = 0;
					boolean firstTime = true;
					while (ctr < numLeftover)
					{
						if (! isDegenerate(allStrips.get(i).m_faces.get(leftOff)))
						{
							ctr++;
							firstTime = false;
							currentStrip.m_faces.add(allStrips.get(i).m_faces.get(leftOff++));
						}
						else if (! firstTime)
							currentStrip.m_faces.add(allStrips.get(i).m_faces.get(leftOff++));
						else leftOff++;
					}
					/*
					 for(int k = 0; k < numLeftover; k++)
					 {
						 currentStrip.faces.add(allStrips[i].faces[leftOff++]);
					 }
					 */
					tempStrips.add(currentStrip);
				}
			}
			else
			{
				// We're not just doing a tempStrips.add(allStrips[i]) because
				// this way we can delete allStrips later to free the memory.
				currentStrip = new NvStripInfo(startInfo,0,-1);
				for (j = 0; j < allStrips.get(i).m_faces.size(); j++)
					currentStrip.m_faces.add(allStrips.get(i).m_faces.get(j));
				tempStrips.add(currentStrip);
			}
		}
		// Add small strips to face list.
		NvStripInfoVec tempStrips2 = new NvStripInfoVec();
		removeSmallStrips(tempStrips, tempStrips2, outFaceList);
		outStrips.clear();
		// Screw optimization for now.
		//	for(i = 0; i < tempStrips.size(); ++i)
		//    outStrips.add(tempStrips[i]);
		if (tempStrips2.size() != 0)
		{
			// Optimize for the vertex cache.
			VertexCache vcache = new VertexCache(m_cacheSize);
			float bestNumHits = -1.0f;
			float numHits;
			int bestIndex = 0;
			int firstIndex = 0;
			float minCost = 10000.0f;
			for (int i = 0; i < tempStrips2.size(); i++)
			{
				int numNeighbors = 0;
				// Find strip with least number of neighbors per face.
				for (j = 0; j < tempStrips2.get(i).m_faces.size();j++)
				{
					numNeighbors += numNeighbors(tempStrips2.get(i).m_faces.get(j),edgeInfos);
				}
				float currCost = (float)numNeighbors / (float)tempStrips2.get(i).m_faces.size();
				if (currCost < minCost)
				{
					minCost = currCost;
					firstIndex = i;
				}
			}
			updateCacheStrip(vcache,tempStrips2.get(firstIndex));
			outStrips.add(tempStrips2.get(firstIndex));
			tempStrips2.get(firstIndex).m_visited = true;
			boolean wantsCW = (tempStrips2.get(firstIndex).m_faces.size() % 2 == 0);
			// This n^2 algo is what slows down stripification so much....
			// needs to be improved.
			while (true)
			{
				bestNumHits = -1.0f;
				// Find best strip to add next,given the current cache.
				for (int i = 0; i < tempStrips2.size(); i++)
				{
					if (tempStrips2.get(i).m_visited) continue;
					numHits = calcNumHitsStrip(vcache, tempStrips2.get(i));
					if (numHits > bestNumHits)
					{
						bestNumHits = numHits;
						bestIndex = i;
					}
					else if (numHits >= bestNumHits)
					{
						// Check previous strip to see if this one requires it to switch polarity.
						NvStripInfo strip = tempStrips2.get(i);
						int nStripFaceCount = strip.m_faces.size();
						NvFaceInfo tFirstFace = new NvFaceInfo(strip.m_faces.get(0).m_v0,strip.m_faces.get(0).m_v1,strip.m_faces.get(0).m_v2);
						// If there is a second face,reorder vertices such that the
						// unique vertex is first.
						if (nStripFaceCount > 1)
						{
							int nUnique = NvStripifier.getUniqueVertexInB(strip.m_faces.get(1), tFirstFace);
							if (nUnique == tFirstFace.m_v1)
							{
								int temp = tFirstFace.m_v0;
								tFirstFace.m_v0 = tFirstFace.m_v1;
								tFirstFace.m_v1 = temp;
							}
							else if (nUnique == tFirstFace.m_v2)
							{
								int temp = tFirstFace.m_v0;
								tFirstFace.m_v0 = tFirstFace.m_v2;
								tFirstFace.m_v2 = temp;
							}
							// If there is a third face, reorder vertices such that the
							// shared vertex is last.
							if (nStripFaceCount > 2)
							{
								int nShared[] = new int[2];
								getSharedVertices(strip.m_faces.get(2), tFirstFace,nShared);
								if ((nShared[0] == tFirstFace.m_v1) && (nShared[1] == -1))
								{
									int temp = tFirstFace.m_v1;
									tFirstFace.m_v1 = tFirstFace.m_v2;
									tFirstFace.m_v2 = temp;
								}
							}
						}
						// Check CW/CCW ordering.
						if (wantsCW == isCW(strip.m_faces.get(0), tFirstFace.m_v0, tFirstFace.m_v1))
						{
							// I like this one!
							bestIndex = i;
						}
					}
				}
				if (bestNumHits == -1.0f) break;
				tempStrips2.get(bestIndex).m_visited = true;
				updateCacheStrip(vcache, tempStrips2.get(bestIndex));
				outStrips.add(tempStrips2.get(bestIndex));
				wantsCW = ((tempStrips2.get(bestIndex).m_faces.size() % 2 == 0) ? wantsCW : ! wantsCW);
			}
		}
	}

	/**
	 * Remove the small strips.
	 * 
	 * @param allStrips The whole strip vector. All small strips will be deleted from this list,
	 * to avoid leaking memory.
	 * @param allBigStrips An out parameter which will contain all strips above <i>m_minStripLength</i>.
	 * @param faceList An out parameter which will contain all faces which were removed from the striplist.
	 */
	protected void removeSmallStrips(NvStripInfoVec allStrips, NvStripInfoVec allBigStrips, NvFaceInfoVec faceList)
	{
		faceList.clear();
		allBigStrips.clear();  // Make sure these are empty.
		NvFaceInfoVec tempFaceList = new NvFaceInfoVec();
		for (int i = 0; i < allStrips.size(); i++)
		{
			if (allStrips.get(i).m_faces.size() < m_minStripLength)
			{
				// Strip is too small, add faces to faceList.
				for (int j = 0; j < allStrips.get(i).m_faces.size();j++)
					tempFaceList.add(allStrips.get(i).m_faces.get(j));
			}
			else allBigStrips.add(allStrips.get(i));
		}
		if (tempFaceList.size() > 0)
		{
			boolean visitedList[] = new boolean[tempFaceList.size()];
			VertexCache vcache = new VertexCache(m_cacheSize);
			int bestNumHits = -1;
			int numHits = 0;
			int bestIndex = 0;
			while (true)
			{
				bestNumHits = -1;
				// Find best face to add next, given the current cache.
				for (int i = 0; i < tempFaceList.size(); i++)
				{
					if (visitedList[i]) continue;
					numHits = calcNumHitsFace(vcache, tempFaceList.get(i));
					if (numHits > bestNumHits)
					{
						bestNumHits = numHits;
						bestIndex = i;
					}
				}
				if (bestNumHits == -1) break;
				visitedList[bestIndex] = true;
				updateCacheFace(vcache, tempFaceList.get(bestIndex));
				faceList.add(tempFaceList.get(bestIndex));
			}
		}
	}

	/**
	 * Finds the next face to start the next strip on.
	 */
	protected boolean findTraversal(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos, NvStripInfo strip, NvStripStartInfo startInfo)
	{
		// If the strip was v0.v1 on the edge, then v1 will be a vertex in the next edge.
		int v = (strip.m_startInfo.m_toV1 ? strip.m_startInfo.m_startEdge.m_v1 : strip.m_startInfo.m_startEdge.m_v0);
		NvFaceInfo untouchedFace = null;
		NvEdgeInfo edgeIter = edgeInfos.get(v);
		while (edgeIter != null)
		{
			NvFaceInfo face0 = edgeIter.m_face0;
			NvFaceInfo face1 = edgeIter.m_face1;
			if ((face0 != null && ! strip.isInStrip(face0)) && face1 != null && ! strip.isMarked(face1))
			{
				untouchedFace = face1;
				break;
			}
			if ((face1 != null && ! strip.isInStrip(face1)) && face0 != null && ! strip.isMarked(face0))
			{
				untouchedFace = face0;
				break;
			}
			// Find the next edgeIter.
			edgeIter=(edgeIter.m_v0 == v ? edgeIter.m_nextV0 : edgeIter.m_nextV1);
		}
		startInfo.m_startFace = untouchedFace;
		startInfo.m_startEdge = edgeIter;
		if (edgeIter != null)
		{
			//note! used to be v1
			if (strip.sharesEdge(startInfo.m_startFace, edgeInfos))
				startInfo.m_toV1 = (edgeIter.m_v0 == v);
			else
				startInfo.m_toV1 = (edgeIter.m_v1 == v);
		}
		return (startInfo.m_startFace != null);
	}

	/**
	 * Commits the input strips by setting their m_experimentId to -1
	 * and adding to the allStrips vector.
	 * 
	 * @param allStrips
	 * @param strips
	 */
	protected void commitStrips(NvStripInfoVec allStrips, NvStripInfoVec strips)
	{
		// Iterate through strips.
		int numStrips = strips.size();
		for (int i = 0; i < numStrips; i++)
		{
			// Tell the strip that it is now real.
			NvStripInfo strip = strips.get(i);
			strip.m_experimentId = -1;
			// Add to the list of real strips.
			allStrips.add(strip);
			// Iterate through the faces of the strip.
			// Tell the faces of the strip that they belong to a real strip now.
			NvFaceInfoVec faces = strips.get(i).m_faces;
			int numFaces = faces.size();
			for (int j = 0; j < numFaces; j++)
				strip.markTriangle(faces.get(j));
		}
	}

	/**
	 * Finds the average strip size of the input vector of strips.
	 * 
	 * @param strips
	 * @return
	 */
	protected float avgStripSize(NvStripInfoVec strips)
	{
		int sizeAccum =0;
		int numStrips = strips.size();
		for (int i = 0; i < numStrips; i++)
		{
			NvStripInfo strip = strips.get(i);
			sizeAccum += strip.m_faces.size();
			sizeAccum -= strip.m_numDegenerates;
		}
		return ((float)sizeAccum) / ((float)numStrips);
	}

	/**
	 * Finds a good starting point, namely one which has only one neighbor.
	 * 
	 * @param faceInfos
	 * @param edgeInfos
	 * @return
	 */
	protected int findStartPoint(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos)
	{
		int bestCtr = -1;
		int bestIndex = -1;
		for (int i = 0; i < faceInfos.size(); i++)
		{
			int ctr = 0;
			if (findOtherFace(edgeInfos,faceInfos.get(i).m_v0, faceInfos.get(i).m_v1, faceInfos.get(i)) == null) ctr++;
			if (findOtherFace(edgeInfos,faceInfos.get(i).m_v1, faceInfos.get(i).m_v2, faceInfos.get(i)) == null) ctr++;
			if (findOtherFace(edgeInfos,faceInfos.get(i).m_v2, faceInfos.get(i).m_v0, faceInfos.get(i)) == null) ctr++;
			if (ctr > bestCtr)
			{
				bestCtr = ctr;
				bestIndex = i;
				// return i;
			}
		}
		// return -1;
		if (bestCtr == 0) return -1;
		else return bestIndex;
	}

	/**
	 * Updates the input vertex cache with this strip's vertices.
	 * 
	 * @param vcache
	 * @param strip
	 */
	protected void updateCacheStrip(VertexCache vcache, NvStripInfo strip)
	{
		for (int i = 0; i < strip.m_faces.size(); ++i)
		{
			if (! vcache.inCache(strip.m_faces.get(i).m_v0)) vcache.addEntry(strip.m_faces.get(i).m_v0);
			if (! vcache.inCache(strip.m_faces.get(i).m_v1)) vcache.addEntry(strip.m_faces.get(i).m_v1);
			if (! vcache.inCache(strip.m_faces.get(i).m_v2)) vcache.addEntry(strip.m_faces.get(i).m_v2);
		}
	}

	/**
	 * Updates the input vertex cache with this face's vertices.
	 * 
	 * @param vcache
	 * @param face
	 */
	protected void updateCacheFace(VertexCache vcache,NvFaceInfo face)
	{
		if (! vcache.inCache(face.m_v0)) vcache.addEntry(face.m_v0);
		if (! vcache.inCache(face.m_v1)) vcache.addEntry(face.m_v1);
		if (! vcache.inCache(face.m_v2)) vcache.addEntry(face.m_v2);

	}

	/**
	 * Returns the number of cache hits per face in the strip.
	 * 
	 * @param vcache
	 * @param strip
	 * @return
	 */
	protected float calcNumHitsStrip(VertexCache vcache, NvStripInfo strip)
	{
		int numHits = 0;
		int numFaces = 0;

		for (int i = 0; i < strip.m_faces.size(); i++)
		{
			if (vcache.inCache(strip.m_faces.get(i).m_v0)) ++numHits;
			if (vcache.inCache(strip.m_faces.get(i).m_v1)) ++numHits;
			if (vcache.inCache(strip.m_faces.get(i).m_v2)) ++numHits;
			numFaces++;
		}
		return ((float)numHits / (float)numFaces);
	}

	/**
	 * Returns the number of cache hits in the face.
	 * 
	 * @param vcache
	 * @param face
	 * @return
	 */
	protected int calcNumHitsFace(VertexCache vcache, NvFaceInfo face)
	{
		int numHits = 0;
		if (vcache.inCache(face.m_v0)) numHits++;
		if (vcache.inCache(face.m_v1)) numHits++;
		if (vcache.inCache(face.m_v2)) numHits++;
		return numHits;
	}

	/**
	 * Returns the number of neighbors that this face has.
	 * 
	 * @param face
	 * @param edgeInfoVec
	 * @return
	 */
	protected int numNeighbors(NvFaceInfo face,NvEdgeInfoVec edgeInfoVec)
	{
		int numNeighbors = 0;
		if (findOtherFace(edgeInfoVec,face.m_v0,face.m_v1,face) != null) numNeighbors++;
		if (findOtherFace(edgeInfoVec,face.m_v1,face.m_v2,face) != null) numNeighbors++;
		if (findOtherFace(edgeInfoVec,face.m_v2,face.m_v0,face) != null) numNeighbors++;
		return numNeighbors;
	}

	/**
	 * Builds the list of all face and edge infos.
	 * 
	 * @param faceInfos
	 * @param edgeInfos
	 * @param maxIndex
	 */
	protected void buildStripifyInfo(NvFaceInfoVec faceInfos, NvEdgeInfoVec edgeInfos, int maxIndex)
	{
		// Reserve space for the face infos, but do not resize them.
		int numIndices = m_indices.size();
		faceInfos.ensureCapacity(numIndices / 3);
		// We actually resize the edge infos, so we must initialize to NULL.
		for (int i = 0; i < maxIndex + 1; i++) edgeInfos.add(null);
		// Iterate through the triangles of the triangle list.
		int numTriangles = numIndices / 3;
		int index = 0;
		boolean faceUpdated[] = new boolean[3];
		for (int i = 0; i < numTriangles; i++)
		{
			boolean mightAlreadyExist = true;
			faceUpdated[0] = false;
			faceUpdated[1] = false;
			faceUpdated[2] = false;
			// Grab the indices
			int v0 = m_indices.get(index++);
			int v1 = m_indices.get(index++);
			int v2 = m_indices.get(index++);
			// We disregard degenerates.
			if (isDegenerate(v0,v1,v2)) continue;
			// Create the face info and add it to the list of faces, but only if this exact face doesn't already
			// exist in the list.
			NvFaceInfo faceInfo = new NvFaceInfo(v0,v1,v2);
			// Grab the edge infos, creating them if they do not already exist.
			NvEdgeInfo edgeInfo01 = findEdgeInfo(edgeInfos,v0,v1);
			if (edgeInfo01 == null)
			{
				// Since one of it's edges isn't in the edge data structure, it can't already exist in the face structure
				mightAlreadyExist = false;
				// Create the info.
				edgeInfo01 = new NvEdgeInfo(v0,v1);
				// Update the linked list on both.
				edgeInfo01.m_nextV0 = edgeInfos.get(v0);
				edgeInfo01.m_nextV1 = edgeInfos.get(v1);
				edgeInfos.set(v0,edgeInfo01);
				edgeInfos.set(v1,edgeInfo01);
				// Set face 0.
				edgeInfo01.m_face0 = faceInfo;
			}
			else
			{
				if (edgeInfo01.m_face1 == null)
				{
					edgeInfo01.m_face1 = faceInfo;
					faceUpdated[0] = true;
				}
				else System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
			}
			// Grab the edge infos, creating them if they do not already exist.
			NvEdgeInfo edgeInfo12 = findEdgeInfo(edgeInfos,v1,v2);
			if (edgeInfo12 == null)
			{
				mightAlreadyExist = false;
				// Create the info.
				edgeInfo12 = new NvEdgeInfo(v1,v2);
				// Update the linked list on both.
				edgeInfo12.m_nextV0 = edgeInfos.get(v1);
				edgeInfo12.m_nextV1 = edgeInfos.get(v2);
				edgeInfos.set(v1, edgeInfo12);
				edgeInfos.set(v2, edgeInfo12);
				// Set face 0.
				edgeInfo12.m_face0 = faceInfo;
			}
			else
			{
				if (edgeInfo12.m_face1 == null)
				{
					edgeInfo12.m_face1 = faceInfo;
					faceUpdated[1] = true;
				}
				else System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
			}
			// Grab the edge infos, creating them if they do not already exist.
			NvEdgeInfo edgeInfo20 = findEdgeInfo(edgeInfos,v2,v0);
			if (edgeInfo20 == null)
			{
				mightAlreadyExist = false;
				// Vreate the info.
				edgeInfo20 = new NvEdgeInfo(v2,v0);
				// Update the linked list on both
				edgeInfo20.m_nextV0 = edgeInfos.get(v2);
				edgeInfo20.m_nextV1 = edgeInfos.get(v0);
				edgeInfos.set(v2, edgeInfo20);
				edgeInfos.set(v0, edgeInfo20);
				// Set face 0.
				edgeInfo20.m_face0 = faceInfo;
			}
			else
			{
				if (edgeInfo20.m_face1 == null)
				{
					edgeInfo20.m_face1 = faceInfo;
					faceUpdated[2] = true;
				}
				else System.out.println("BuildStripifyInfo: > 2 triangles on an edge... uncertain consequences\n");
			}
			if (mightAlreadyExist)
			{
				if (alreadyExists(faceInfo,faceInfos))
				{
					// Cleanup pointers that point to this deleted face.
					if (faceUpdated[0]) edgeInfo01.m_face1 = null;
					if (faceUpdated[1])	edgeInfo12.m_face1 = null;
					if (faceUpdated[2])	edgeInfo20.m_face1 = null;
				}
				else faceInfos.add(faceInfo);
			}
			else faceInfos.add(faceInfo);
		}
	}

	protected boolean alreadyExists(NvFaceInfo faceInfo, NvFaceInfoVec faceInfos)
	{
		for (int i = 0; i < faceInfos.size(); ++i)
		{
			NvFaceInfo fi = faceInfos.get(i);
			if ((fi.m_v0 == faceInfo.m_v0) && (fi.m_v1 == faceInfo.m_v1) && (fi.m_v2 == faceInfo.m_v2)) return true;
		}
		return false;
	}

	// Returns the vertex unique to faceB
	public static int getUniqueVertexInB(NvFaceInfo faceA,NvFaceInfo faceB)
	{
		int facev0 = faceB.m_v0;
		if (facev0 != faceA.m_v0 && facev0 != faceA.m_v1 && facev0 != faceA.m_v2) return facev0;
		int facev1 = faceB.m_v1;
		if (facev1 != faceA.m_v0 && facev1 != faceA.m_v1 && facev1 != faceA.m_v2) return facev1;
		int facev2 = faceB.m_v2;
		if (facev2 != faceA.m_v0 && facev2 != faceA.m_v1 && facev2 != faceA.m_v2) return facev2;
		// Nothing is different.
		return -1;
	}

	public static void getSharedVertices(NvFaceInfo faceA,NvFaceInfo faceB,int vertex[])
	{
		vertex[0] = -1;
		vertex[1] = -1;
		int facev0 = faceB.m_v0;
		if (facev0 == faceA.m_v0 || facev0 == faceA.m_v1 || facev0 == faceA.m_v2)
		{
			if (vertex[0] != -1)
			{
				vertex[1] = facev0;
				return;
			}
			else vertex[0] = facev0;
		}
		int facev1 = faceB.m_v1;
		if (facev1 == faceA.m_v0 || facev1 == faceA.m_v1 || facev1 == faceA.m_v2)
		{
			if (vertex[0] != -1)
			{
				vertex[1] = facev1;
				return;
			}
			else vertex[0] = facev1;
		}
		int facev2 = faceB.m_v2;
		if (facev2 == faceA.m_v0 || facev2 == faceA.m_v1 || facev2 == faceA.m_v2)
		{
			if (vertex[0] != -1)
			{
				vertex[1] = facev2;
				return;
			}
			else vertex[0] = facev2;
		}
	}

	public static boolean isDegenerate(NvFaceInfo face)
	{
		if (face.m_v0 == face.m_v1) return true;
		else if (face.m_v0 == face.m_v2) return true;
		else if (face.m_v1 == face.m_v2) return true;
		else return false;
	}

	public static boolean isDegenerate(int v0,int v1,int v2)
	{
		if (v0 == v1) return true;
		else if (v0 == v2) return true;
		else if (v1 == v2) return true;
		else return false;
	}

	/**
	// Returns vertex of the input face which is "next" in the input index list.
	 * 
	 * @param indices
	 * @param face
	 * @return
	 */
	protected static int getNextIndex(IntVec indices, NvFaceInfo face)
	{
		int numIndices = indices.size();
		assert(numIndices >= 2);
		int v0 = indices.get(numIndices-2);
		int v1 = indices.get(numIndices-1);
		int fv0 = face.m_v0;
		int fv1 = face.m_v1;
		int fv2 = face.m_v2;
		if (fv0 != v0 && fv0 != v1)
		{
			if ((fv1 != v0 && fv1 != v1) || (fv2 != v0 && fv2 != v1))
			{
				System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
				System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
			}
			return fv0;
		}
		if (fv1 != v0 && fv1 != v1)
		{
			if ((fv0 != v0 && fv0 != v1) || (fv2 != v0 && fv2 != v1))
			{
				System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
				System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
			}
			return fv1;
		}
		if (fv2 != v0 && fv2 != v1)
		{
			if ((fv0 != v0 && fv0 != v1) || (fv1 != v0 && fv1 != v1))
			{
				System.out.println("GetNextIndex: Triangle doesn't have all of its vertices\n");
				System.out.println("GetNextIndex: Duplicate triangle probably got us derailed\n");
			}
			return fv2;
		}
		// Shouldn't get here, but let's try and fail gracefully.
		if ((fv0 == fv1) || (fv0 == fv2)) return fv0;
		else if ((fv1 == fv0) || (fv1 == fv2)) return fv1;
		else if ((fv2 == fv0) || (fv2 == fv1)) return fv2;
		else return -1;
	}

	/**
	 * Find the edge info for these two indices.
	 * 
	 * @param edgeInfos
	 * @param v0
	 * @param v1
	 * @return
	 */
	protected static NvEdgeInfo findEdgeInfo(NvEdgeInfoVec edgeInfos, int v0, int v1)
	{
		// We can get to it through either array
		// because the edge infos have a v0 and v1
		// and there is no order except how it was
		// first created.
		NvEdgeInfo infoIter = edgeInfos.get(v0);
		while (infoIter != null)
		{
			if (infoIter.m_v0 == v0)
			{
				if (infoIter.m_v1 == v1) return infoIter;
				else infoIter = infoIter.m_nextV0;
			}
			else
			{
				assert(infoIter.m_v1 == v0);
				if (infoIter.m_v0 == v1) return infoIter;
				else infoIter = infoIter.m_nextV1;
			}
		}
		return null;
	}

	/**
	 * Find the other face sharing these vertices exactly like the edge info above
	 * 
	 * @param edgeInfos
	 * @param v0
	 * @param v1
	 * @param faceInfo
	 * @return
	 */
	protected static NvFaceInfo findOtherFace(NvEdgeInfoVec edgeInfos, int v0, int v1, NvFaceInfo faceInfo)
	{
		NvEdgeInfo edgeInfo = findEdgeInfo(edgeInfos,v0,v1);
		// We've hit a degenerate.
		if ((edgeInfo == null) && (v0 == v1)) return null;
		assert(edgeInfo != null);
		return (edgeInfo.m_face0 == faceInfo ? edgeInfo.m_face1 : edgeInfo.m_face0);
	}
}