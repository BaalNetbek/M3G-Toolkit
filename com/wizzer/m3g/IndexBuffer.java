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
package com.wizzer.m3g;

// Import standard Java classes.
import java.nio.IntBuffer;
import com.sun.opengl.util.*;

public abstract class IndexBuffer extends Object3D
{
	// The buffer of indices.
	protected IntBuffer m_buffer = null;

	protected IndexBuffer()
	{
		// Do nothing.
	}
	
	public int getIndexCount()
	{
		if (m_buffer != null)
		    return m_buffer.limit();
		else
			return 0;
	}
	
	public abstract void getIndices(int[] indices);
	
	IntBuffer getBuffer()
	{
		return m_buffer;
	}
	
	protected void allocate(int numElements)
	{
		m_buffer = BufferUtil.newIntBuffer(numElements);
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
}
