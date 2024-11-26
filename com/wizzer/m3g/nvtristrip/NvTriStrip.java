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

// Import standard Java classes.
import java.util.*;

// TODO: log.
public class NvTriStrip
{
	// GeForce1 and 2 cache size
	public static final int CACHESIZE_GEFORCE1_2    = 16;
	// GeForce3 cache size
	public static final int CACHESIZE_GEFORCE3      = 24;

	private int m_cacheSize = CACHESIZE_GEFORCE1_2;
	private boolean m_stitchStrips = true;
	private int m_minStripSize;
	private boolean m_listsOnly;
	private int m_restartVal;
	private boolean m_restart;

	public NvTriStrip()
	{
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// EnableRestart()
	//
	// For GPUs that support primitive restart, this sets a value as the restart index
	//
	// Restart is meaningless if strips are not being stitched together, so enabling restart
	//  makes NvTriStrip forcing stitching.  So, you'll get back one strip.
	//
	// Default value: disabled
	//
	public void enableRestart(int restartVal)
	{
		m_restartVal = restartVal;
		m_restart = true;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// DisableRestart()
	//
	// For GPUs that support primitive restart, this disables using primitive restart
	//
	public void disableRestart()
	{
		m_restart = false;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// SetListsOnly()
	//
	// If set to true, will return an optimized list, with no strips at all.
	//
	// Default value: false
	//
	public void setListsOnly(boolean listsOnly)
	{
		m_listsOnly = listsOnly;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// SetCacheSize()
	//
	// Sets the cache size which the stripfier uses to optimize the data.
	// Controls the length of the generated individual strips.
	// This is the "actual" cache size, so 24 for GeForce3 and 16 for GeForce1/2
	// You may want to play around with this number to tweak performance.
	//
	// Default value: 16
	//
	public void setCacheSize(int cacheSize)
	{
		m_cacheSize = cacheSize;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// SetStitchStrips()
	//
	// bool to indicate whether to stitch together strips into one huge strip or not.
	// If set to true, you'll get back one huge strip stitched together using degenerate
	//  triangles.
	// If set to false, you'll get back a large number of separate strips.
	//
	// Default value: true
	//
	public void setStitchStrips(boolean stitchStrips)
	{
		m_stitchStrips = stitchStrips;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// SetMinStripSize()
	//
	// Sets the minimum acceptable size for a strip, in triangles.
	// All strips generated which are shorter than this will be thrown into one big, separate list.
	//
	// Default value: 0
	//
	public void setMinStripSize(int minStripSize)
	{
		m_minStripSize = minStripSize;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//SameTriangle()
	//
	//Returns true if the two triangles defined by firstTri and secondTri are the same
	// The "same" is defined in this case as having the same indices with the same winding order
	//
	private boolean sameTriangle(int firstTri0, int firstTri1, int firstTri2, 
		int secondTri0, int secondTri1, int secondTri2)
	{
		boolean isSame = false;
		if (firstTri0 == secondTri0)
		{
			if (firstTri1 == secondTri1)
			{
				if (firstTri2 == secondTri2) isSame = true;
			}
		}
		else if (firstTri0 == secondTri1)
		{
			if (firstTri1 == secondTri2)
			{
				if (firstTri2 == secondTri0) isSame = true;
			}
		}
		else if (firstTri0 == secondTri2)
		{
			if (firstTri1 == secondTri0)
			{
				if (firstTri2 == secondTri1) isSame = true;
			}
		}
		return isSame;
	}

	private boolean testTriangle(int v0, int v1 ,int v2, NvFaceInfoVec in_bins[], int NUMBINS)
	{
		// Hash this triangle
		boolean isLegit = false;
		int ctr = v0 % NUMBINS;
		for (int k = 0; k < in_bins[ctr].size(); ++k)
		{
			// Check triangles in this bin.
			if (sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2))
			{
				isLegit = true;
				break;
			}
		}
		if (! isLegit)
		{
			ctr = v1 % NUMBINS;
			for (int k = 0; k < in_bins[ctr].size(); ++k)
			{
				// Check triangles in this bin.
				if (sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2))
				{
					isLegit = true;
					break;
				}
			}
			if (! isLegit)
			{
				ctr = v2 % NUMBINS;
				for (int k = 0; k < in_bins[ctr].size(); ++k)
				{
					// Check triangles in this bin.
					if (sameTriangle(in_bins[ctr].get(k).m_v0, in_bins[ctr].get(k).m_v1, in_bins[ctr].get(k).m_v2, v0, v1, v2))
					{
						isLegit = true;
						break;
					}
				}
			}
		}
		return isLegit;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// GenerateStrips()
	//
	// in_indices: input index list, the indices you would use to render
	// in_numIndices: number of entries in in_indices
	// primGroups: array of optimized/stripified PrimitiveGroups
	// numGroups: number of groups returned
	//
	// Be sure to call delete[] on the returned primGroups to avoid leaking mem
	//
	public PrimitiveGroup[] generateStrips(int in_indices[])
	{
		return generateStrips(in_indices,false);
	}

	public PrimitiveGroup[] generateStrips(int in_indices[], boolean validateEnabled)
	{
        int numGroups = 0;
        PrimitiveGroup primGroups[];
		// Put data in format that the stripifier likes.
		IntVec tempIndices = new IntVec(in_indices.length);
		int maxIndex = 0;
		int minIndex = Integer.MAX_VALUE;
		for (int i = 0; i < in_indices.length; i++)
		{
			tempIndices.add(in_indices[i]);
			if (in_indices[i] > maxIndex) maxIndex = in_indices[i];
			if (in_indices[i] < minIndex) minIndex = in_indices[i];
		}
		NvStripInfoVec tempStrips = new NvStripInfoVec();
		NvFaceInfoVec tempFaces = new NvFaceInfoVec();
		NvStripifier stripifier = new NvStripifier();
		// Do actual stripification.
		stripifier.stripify(tempIndices, m_cacheSize, m_minStripSize, maxIndex, tempStrips, tempFaces);
		// Stitch strips together.
		IntVec stripIndices = new IntVec();
		int numSeparateStrips = 0;
		if (m_listsOnly)
		{
			// If we're outputting only lists, we're done.
			numGroups = 1;
			primGroups = new PrimitiveGroup[] {new PrimitiveGroup()};
			PrimitiveGroup primGroupArray[] = primGroups;
			// Count the total number of indices.
			int numIndices = 0;
			for (int i = 0; i < tempStrips.size(); i++)
				numIndices += (tempStrips.get(i).m_faces.size() * 3);
			// Add in the list.
			numIndices += tempFaces.size() * 3;
			primGroupArray[0].m_type = PrimitiveGroup.PT_LIST;
			primGroupArray[0].m_numIndices = numIndices;
			primGroupArray[0].m_indices = new int[numIndices];
			// Do strips.
			int indexCtr = 0;
			for (int i = 0; i < tempStrips.size(); i++)
			{
				for (int j = 0; j < tempStrips.get(i).m_faces.size(); j++)
				{
					// Degenerates are of no use with lists.
					if (! NvStripifier.isDegenerate(tempStrips.get(i).m_faces.get(j)))
					{
						primGroupArray[0].m_indices[indexCtr++] = tempStrips.get(i).m_faces.get(j).m_v0;
						primGroupArray[0].m_indices[indexCtr++] = tempStrips.get(i).m_faces.get(j).m_v1;
						primGroupArray[0].m_indices[indexCtr++] = tempStrips.get(i).m_faces.get(j).m_v2;
					}
					else
					{
						// We've removed a tri, reduce the number of indices.
						primGroupArray[0].m_numIndices -= 3;
					}
				}
			}
			// Do lists.
			for (int i = 0; i < tempFaces.size(); i++)
			{
				primGroupArray[0].m_indices[indexCtr++] = tempFaces.get(i).m_v0;
				primGroupArray[0].m_indices[indexCtr++] = tempFaces.get(i).m_v1;
				primGroupArray[0].m_indices[indexCtr++] = tempFaces.get(i).m_v2;
			}
		}
		else
		{
			numSeparateStrips = stripifier.createStrips(tempStrips, stripIndices, m_stitchStrips, m_restart, m_restartVal);
			// If we're stitching strips together,we better get back only one strip from CreateStrips().
			assert((m_stitchStrips&&(numSeparateStrips == 1)) || ! m_stitchStrips);
			// Convert to output format
			numGroups = numSeparateStrips; // For the strips.
			if (tempFaces.size() != 0) numGroups++;  // We've got a list as well, increment.
			primGroups = new PrimitiveGroup[numGroups];
			for (int i = 0; i < numGroups; i++)
				primGroups[i] = new PrimitiveGroup();
			PrimitiveGroup primGroupArray[] = primGroups;
			// First, the strips.
			int startingLoc = 0;
			for (int stripCtr = 0; stripCtr < numSeparateStrips;stripCtr++)
			{
				int stripLength = 0;
				if (! m_stitchStrips)
				{
					int i = 0;
					// If we've got multiple strips, we need to figure out the correct length.
					for (i = startingLoc; i < stripIndices.size(); i++)
						if (stripIndices.get(i) == -1) break;
					stripLength = i - startingLoc;
				}
				else stripLength = stripIndices.size();
				primGroupArray[stripCtr].m_type = PrimitiveGroup.PT_STRIP;
				primGroupArray[stripCtr].m_indices = new int[stripLength];
				primGroupArray[stripCtr].m_numIndices = stripLength;
				int indexCtr = 0;
				for (int i = startingLoc; i < stripLength + startingLoc; i++)
					primGroupArray[stripCtr].m_indices[indexCtr++]=stripIndices.get(i);
				// We add 1 to account for the -1 separating strips.
				// This doesn't break the stitched case since we'll exit the loop.
				startingLoc += stripLength + 1;
			}
			// Next, the list.
			if (tempFaces.size() != 0)
			{
				int faceGroupLoc = numGroups - 1;    // The face group is the last one.
				primGroupArray[faceGroupLoc].m_type = PrimitiveGroup.PT_LIST;
				primGroupArray[faceGroupLoc].m_indices = new int[tempFaces.size() * 3];
				primGroupArray[faceGroupLoc].m_numIndices = tempFaces.size() * 3;
				int indexCtr = 0;
				for (int i = 0; i < tempFaces.size(); i++)
				{
					primGroupArray[faceGroupLoc].m_indices[indexCtr++] = tempFaces.get(i).m_v0;
					primGroupArray[faceGroupLoc].m_indices[indexCtr++] = tempFaces.get(i).m_v1;
					primGroupArray[faceGroupLoc].m_indices[indexCtr++] = tempFaces.get(i).m_v2;
				}
			}
		}
		// Validate generated data against input.
		if (validateEnabled)
		{
			int NUMBINS = 100;
			NvFaceInfoVec in_bins[] = new NvFaceInfoVec[NUMBINS];
			for (int i = 0; i < NUMBINS; i++)
				in_bins[i] = new NvFaceInfoVec();
			// Hash input indices on first index.
			for (int i = 0; i < in_indices.length; i += 3)
			{
				NvFaceInfo faceInfo = new NvFaceInfo(in_indices[i], in_indices[i+1], in_indices[i+2]);
				in_bins[in_indices[i] % NUMBINS].add(faceInfo);
			}
			for (int i = 0; i < numGroups; ++i)
			{
				switch (primGroups[i].m_type)
				{
					case PrimitiveGroup.PT_LIST:
					{
						for (int j = 0; j < primGroups[i].m_numIndices; j += 3)
						{
							int v0 = primGroups[i].m_indices[j];
							int v1 = primGroups[i].m_indices[j+1];
							int v2 = primGroups[i].m_indices[j+2];
							// Ignore degenerates.
							if (NvStripifier.isDegenerate(v0, v1, v2)) continue;
							if (! testTriangle(v0, v1, v2, in_bins, NUMBINS)) return null;
						}
						break;
					}
					case PrimitiveGroup.PT_STRIP:
					{
						//int brokenCtr=0;
						boolean flip = false;
						for (int j = 2; j < primGroups[i].m_numIndices; ++j)
						{
							int v0 = primGroups[i].m_indices[j - 2];
							int v1 = primGroups[i].m_indices[j - 1];
							int v2 = primGroups[i].m_indices[j];
							if (flip)
							{
								// Swap v1 and v2.
								int swap = v1;
								v1 = v2;
								v2 = swap;
							}
							// Ignore degenerates.
							if (NvStripifier.isDegenerate(v0, v1, v2))
							{
								flip =! flip;
								continue;
							}
							if (! testTriangle(v0, v1, v2, in_bins, NUMBINS)) return null;
							flip =! flip;
						}
						break;
					}
					case PrimitiveGroup.PT_FAN:
					default:
						break;
				}
			}
		}
		return primGroups;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// RemapIndices()
	//
	// Function to remap your indices to improve spatial locality in your vertex buffer.
	//
	// in_primGroups: array of PrimitiveGroups you want remapped
	// numGroups: number of entries in in_primGroups
	// numVerts: number of vertices in your vertex buffer, also can be thought of as the range
	//  of acceptable values for indices in your primitive groups.
	// remappedGroups: array of remapped PrimitiveGroups
	//
	// Note that, according to the remapping handed back to you, you must reorder your
	//  vertex buffer.
	//
	// Credit goes to the MS Xbox crew for the idea for this interface.
	//
	public PrimitiveGroup[] remapIndices(PrimitiveGroup in_primGroups[], int numVerts)
	{
		PrimitiveGroup remappedGroups[] = new PrimitiveGroup[in_primGroups.length];
		for (int i = 0; i < remappedGroups.length; i++)
			remappedGroups[i] = new PrimitiveGroup();
		// Caches oldIndex --> newIndex conversion.
		int indexCache[] = new int[numVerts];
		Arrays.fill(indexCache, -1);
		// Loop over primitive groups.
		int indexCtr = 0;
		for (int i = 0; i < in_primGroups.length; i++)
		{
			int numIndices = in_primGroups[i].m_numIndices;
			// Init remapped group.
			remappedGroups[i].m_type = in_primGroups[i].m_type;
			remappedGroups[i].m_numIndices = numIndices;
			remappedGroups[i].m_indices = new int[numIndices];
			for (int j = 0; j < numIndices; j++)
			{
				int cachedIndex = indexCache[in_primGroups[i].m_indices[j]];
				if (cachedIndex == -1) // We haven't seen this index before.
				{
					// Point to "last" vertex in VB.
					remappedGroups[i].m_indices[j] = indexCtr;
					// Add to index cache, increment.
					indexCache[in_primGroups[i].m_indices[j]] = indexCtr++;
				}
				else
				{
					// We've seen this index before.
					remappedGroups[i].m_indices[j] = cachedIndex;
				}
			}
		}
		return remappedGroups;
	}
}