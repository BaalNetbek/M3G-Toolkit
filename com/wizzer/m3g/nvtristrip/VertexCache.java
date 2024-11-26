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

class VertexCache
{
	private int m_entries[];
	private int m_numEntries;

	public VertexCache()
	{
		this(16);
	}

	public VertexCache(int size)
	{
		m_entries = new int[m_numEntries = size];
		clear();
	}

	public boolean inCache(int entry)
	{
		for (int i = 0; i < m_entries.length; i++)
			if (m_entries[i] == entry) return true;
		return false;
	}

	public int addEntry(int entry)
	{
		int removed = m_entries[m_entries.length - 1];
		// Push everything right one.
		for (int i = m_numEntries - 2; i >= 0; i--)
			m_entries[i+1] = m_entries[i];
		m_entries[0] = entry;
		return removed;
	}

	public void clear()
	{
		Arrays.fill(m_entries, -1);
	}

	public void copy(VertexCache inVcache)
	{
		for (int i = 0; i < m_numEntries; i++)
			inVcache.set(i, m_entries[i]);
	}

	public int at(int index)
	{
		return m_entries[index];
	}

	public void set(int index,int value)
	{
		m_entries[index] = value;
	}
}
