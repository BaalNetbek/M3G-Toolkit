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

import java.util.*;

class NvFaceInfoVec
{
	protected NvFaceInfo data[];
	protected int size;

	/*************************************************************************/
	/*************************************************************************/

	public NvFaceInfoVec()
	{
		this(64);
	}

    public NvFaceInfoVec(int initialCapacity)
    {
	    data=new NvFaceInfo[initialCapacity];
    }

	/*************************************************************************/
	/*************************************************************************/

	public void add(NvFaceInfo value)
	{
		ensureCapacity(size+1);
	    data[size++]=value;
	}

	public void set(int index,NvFaceInfo value)
	{
		data[index]=value;
	}

	public NvFaceInfo get(int index)
	{
		return data[index];
	}

	public void clear()
	{
		Arrays.fill(data,null);
		size=0;
	}

    public int size()
    {
        return size;
    }

	public boolean contains(NvFaceInfo value)
	{
		for (int i=0;i<data.length;i++)
			if (data[i]==value) return true;
		return false;
	}

    public void ensureCapacity(int minCapacity)
    {
	    if (minCapacity>data.length)
	    {
		    int newCapacity=((data.length*3)/2)+1;
		    if (newCapacity<minCapacity) newCapacity=minCapacity;
		    NvFaceInfo temp[]=new NvFaceInfo[newCapacity];
		    System.arraycopy(data,0,temp,0,size);
		    data=temp;
	    }
    }
}