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

class IntVec
{
	protected int m_data[];
	protected int m_size;

	public IntVec()
	{
		this(64);
	}

    public IntVec(int initialCapacity)
    {
	    m_data = new int[initialCapacity];
    }

	public void add(int value)
	{
		ensureCapacity(m_size+1);
	    m_data[m_size++] = value;
	}

	public void set(int index,int value)
	{
		m_data[index] = value;
	}

	public int get(int index)
	{
		return m_data[index];
	}

	public void clear()
	{
		Arrays.fill(m_data,0);
		m_size = 0;
	}

    public int size()
    {
        return m_size;
    }

    public void ensureCapacity(int minCapacity)
    {
	    if (minCapacity > m_data.length)
	    {
		    int newCapacity = ((m_data.length * 3) / 2) + 1;
		    if (newCapacity < minCapacity)
		    	newCapacity = minCapacity;
		    int temp[] = new int[newCapacity];
		    System.arraycopy(m_data,0,temp,0,m_size);
		    m_data = temp;
	    }
    }
}
