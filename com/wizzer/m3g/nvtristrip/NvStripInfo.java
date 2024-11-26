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

// This is a summary of a strip that has been built.
class NvStripInfo
{
	public NvStripStartInfo m_startInfo;
	public NvFaceInfoVec m_faces;
	public int m_stripId;
	public int m_experimentId;
	public boolean m_visited;
	public int m_numDegenerates;

	public NvStripInfo(NvStripStartInfo startInfo, int stripId)
	{
		this(startInfo,stripId,-1);
	}

	public NvStripInfo(NvStripStartInfo startInfo,int stripId,int experimentId)
	{
		m_startInfo = startInfo;
		m_stripId = stripId;
		m_experimentId = experimentId;
		m_faces = new NvFaceInfoVec();
		m_visited = false;
		m_numDegenerates = 0;
	}

	// This is an experiment if the experiment id is>=0.
	public boolean isExperiment()
	{
		return (m_experimentId >= 0);
	}

	public boolean isInStrip(NvFaceInfo faceInfo)
	{
		if (faceInfo == null) return false;
		return (m_experimentId >= 0 ? faceInfo.m_testStripId == m_stripId : faceInfo.m_stripId == m_stripId);
	}

	// Returns true if the input face and the current strip share an edge.
	public boolean sharesEdge(NvFaceInfo faceInfo, NvEdgeInfoVec edgeInfos)
	{
		// Check v0->v1 edge
		NvEdgeInfo currEdge=NvStripifier.findEdgeInfo(edgeInfos,faceInfo.m_v0, faceInfo.m_v1);
		if (isInStrip(currEdge.m_face0) || isInStrip(currEdge.m_face1)) return true;
		// Check v1->v2 edge
		currEdge = NvStripifier.findEdgeInfo(edgeInfos, faceInfo.m_v1, faceInfo.m_v2);
		if (isInStrip(currEdge.m_face0)||isInStrip(currEdge.m_face1)) return true;
		// Check v2->v0 edge
		currEdge = NvStripifier.findEdgeInfo(edgeInfos,faceInfo.m_v2, faceInfo.m_v0);
		if (isInStrip(currEdge.m_face0) || isInStrip(currEdge.m_face1)) return true;
		return false;
	}

	// Combines the two input face vectors and puts the result into m_faces.
	// take the given forward and backward strips and combine them together.
	public void combine(NvFaceInfoVec forward,NvFaceInfoVec backward)
	{
		// Add backward faces.
		int numFaces =backward.size();
		for (int i = numFaces - 1; i >= 0; i--) m_faces.add(backward.get(i));
		// Add forward faces.
		numFaces = forward.size();
		for (int i = 0; i < numFaces; i++) m_faces.add(forward.get(i));
	}

	// Returns true if the face is "unique", i.e. has a vertex which doesn't exist in the faceVec.
	public boolean unique(NvFaceInfoVec faceVec, NvFaceInfo face)
	{
		boolean v0 = false, v1 = false, v2 = false; //bools to indicate whether a vertex is in the faceVec or not.
		for (int i = 0; i < faceVec.size(); i++)
		{
			NvFaceInfo faceInfo = faceVec.get(i);
			if (! v0)
			{
				if ((faceInfo.m_v0 == face.m_v0) || (faceInfo.m_v1 == face.m_v0) || (faceInfo.m_v2 == face.m_v0)) v0 = true;
			}
			if (! v1)
			{
				if ((faceInfo.m_v0 == face.m_v1) || (faceInfo.m_v1 == face.m_v1) || (faceInfo.m_v2 == face.m_v1)) v1 = true;
			}
			if (! v2)
			{
				if ((faceInfo.m_v0 == face.m_v2) || (faceInfo.m_v1 == face.m_v2) || (faceInfo.m_v2 == face.m_v2)) v2 = true;
			}
			// The face is not unique, all it's vertices exist in the face vector.
			if (v0 && v1 && v2)	return false;
		}
		// If we get out here, it's unique.
		return true;
	}

	// If either the faceInfo has a real strip index because it is
	// already assigned to a committed strip OR it is assigned in an
	// experiment and the experiment index is the one we are building
	// for, then it is marked and unavailable.
	// Mark the triangle as taken by this strip.
	public boolean isMarked(NvFaceInfo faceInfo)
	{
		return (faceInfo.m_stripId >= 0) || (isExperiment() && faceInfo.m_experimentId == m_experimentId);
	}

	// Marks the face with the current strip ID.
	public void markTriangle(NvFaceInfo faceInfo)
	{
		assert(! isMarked(faceInfo));
		if (isExperiment())
		{
			faceInfo.m_experimentId = m_experimentId;
			faceInfo.m_testStripId = m_stripId;
		}
		else
		{
			assert(faceInfo.m_stripId == -1);
			faceInfo.m_experimentId = -1;
			faceInfo.m_stripId = m_stripId;
		}
	}

	// Builds a strip forward as far as we can go, then builds backwards, and joins the two lists.
	public void build(NvEdgeInfoVec edgeInfos,NvFaceInfoVec faceInfos)
	{
		// Used in building the strips forward and backward.
		IntVec scratchIndices = new IntVec();
		// build forward... start with the initial face.
		NvFaceInfoVec forwardFaces = new NvFaceInfoVec(),backwardFaces=new NvFaceInfoVec();
		forwardFaces.add(m_startInfo.m_startFace);
		markTriangle(m_startInfo.m_startFace);
		int v0 = (m_startInfo.m_toV1?m_startInfo.m_startEdge.m_v0:m_startInfo.m_startEdge.m_v1);
		int v1 = (m_startInfo.m_toV1?m_startInfo.m_startEdge.m_v1:m_startInfo.m_startEdge.m_v0);
		// Easiest way to get v2 is to use this function which requires the
		// other indices to already be in the list.
		scratchIndices.add(v0);
		scratchIndices.add(v1);
		int v2 = NvStripifier.getNextIndex(scratchIndices,m_startInfo.m_startFace);
		scratchIndices.add(v2);
		// build the forward list
		int nv0 = v1;
		int nv1 = v2;
		NvFaceInfo nextFace = NvStripifier.findOtherFace(edgeInfos, nv0 ,nv1, m_startInfo.m_startFace);
		while (nextFace != null && ! isMarked(nextFace))
		{
			// Check to see if this next face is going to cause us to die soon.
			int testnv0 = nv1;
			int testnv1 = NvStripifier.getNextIndex(scratchIndices, nextFace);
			NvFaceInfo nextNextFace = NvStripifier.findOtherFace(edgeInfos, testnv0, testnv1, nextFace);
			if ((nextNextFace == null) || (isMarked(nextNextFace)))
			{
				//Uh, oh, we're following a dead end, try swapping.
				NvFaceInfo testNextFace = NvStripifier.findOtherFace(edgeInfos, nv0, testnv1 ,nextFace);
				if (((testNextFace != null) && ! isMarked(testNextFace)))
				{
					// We only swap if it buys us something
					// Add a "fake" degenerate face.
					NvFaceInfo tempFace = new NvFaceInfo(nv0, nv1, nv0,true);
					forwardFaces.add(tempFace);
					markTriangle(tempFace);
					scratchIndices.add(nv0);
					testnv0 = nv0;
					++m_numDegenerates;
				}
			}
			// Add this to the strip.
			forwardFaces.add(nextFace);
			markTriangle(nextFace);
			// Add the index.
			//nv0 = nv1;
			//nv1 = NvStripifier::GetNextIndex(scratchIndices, nextFace);
			scratchIndices.add(testnv1);
			// and get the next face.
			nv0 = testnv0;
			nv1 = testnv1;
			nextFace = NvStripifier.findOtherFace(edgeInfos, nv0, nv1, nextFace);
		}
		// tempAllFaces is going to be forwardFaces + backwardFaces
		// it's used for Unique()
		NvFaceInfoVec tempAllFaces = new NvFaceInfoVec();
		for (int i = 0; i < forwardFaces.size(); i++)
			tempAllFaces.add(forwardFaces.get(i));
		// Reset the indices for building the strip backwards and do so.
		scratchIndices.clear();
		scratchIndices.add(v2);
		scratchIndices.add(v1);
		scratchIndices.add(v0);
		nv0 = v1;
		nv1 = v0;
		nextFace = NvStripifier.findOtherFace(edgeInfos, nv0, nv1, m_startInfo.m_startFace);
		while (nextFace != null && ! isMarked(nextFace))
		{
			// This tests to see if a face is "unique", meaning that its vertices aren't already in the list
			// so, strips which "wrap-around" are not allowed.
			if (! unique(tempAllFaces,nextFace)) break;
			// Check to see if this next face is going to cause us to die soon.
			int testnv0 = nv1;
			int testnv1 = NvStripifier.getNextIndex(scratchIndices, nextFace);
			NvFaceInfo nextNextFace = NvStripifier.findOtherFace(edgeInfos, testnv0, testnv1, nextFace);
			if ((nextNextFace == null) || (isMarked(nextNextFace)))
			{
				// Uh, oh, we're following a dead end, try swapping.
				NvFaceInfo testNextFace = NvStripifier.findOtherFace(edgeInfos, nv0, testnv1, nextFace);
				if (((testNextFace != null) && ! isMarked(testNextFace)))
				{
					// We only swap if it buys us something
					// Add a "fake" degenerate face
					NvFaceInfo tempFace = new NvFaceInfo(nv0, nv1, nv0,true);
					backwardFaces.add(tempFace);
					markTriangle(tempFace);
					scratchIndices.add(nv0);
					testnv0 = nv0;
					++m_numDegenerates;
				}
			}
			// Add this to the strip.
			backwardFaces.add(nextFace);
			// This is just so Unique() will work.
			tempAllFaces.add(nextFace);
			markTriangle(nextFace);
			// Add the index.
			//nv0 = nv1;
			//nv1 = NvStripifier::GetNextIndex(scratchIndices, nextFace);
			scratchIndices.add(testnv1);
			// and get the next face.
			nv0 = testnv0;
			nv1 = testnv1;
			nextFace = NvStripifier.findOtherFace(edgeInfos, nv0, nv1, nextFace);
		}
		// Combine the forward and backwards stripification lists and put into our own face vector.
		combine(forwardFaces,backwardFaces);
	}
}
