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

class NvFaceInfo
{
	public int m_v0, m_v1, m_v2;
	public int m_stripId;         // real strip Id
	public int m_testStripId;     // strip Id in an experiment
	public int m_experimentId;    // in what experiment was it given an experiment Id?
	public boolean m_isFake;      // if true, will be deleted when the strip it's in is deleted

	public NvFaceInfo(int v0,int v1,int v2)
	{
		this(v0,v1,v2,false);
	}

	public NvFaceInfo(int v0,int v1,int v2,boolean isFake)
	{
		m_v0 = v0;
		m_v1 = v1;
		m_v2 = v2;
		m_isFake = isFake;
		m_stripId = m_testStripId = m_experimentId = -1;
	}

    public void set(NvFaceInfo face)
	{
		m_v0 = face.m_v0;
		m_v1 = face.m_v1;
		m_v2 = face.m_v2;
		m_stripId = face.m_stripId;
		m_testStripId = face.m_testStripId;
		m_experimentId = face.m_experimentId;
	}
}
