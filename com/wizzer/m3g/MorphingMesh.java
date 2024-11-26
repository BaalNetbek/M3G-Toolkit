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
import java.io.IOException;
import java.util.ArrayList;

public class MorphingMesh extends Mesh
{
	// The collection of morph targets.
	private VertexBuffer m_morphTargets[];
	// The number of morph targets.
	private int m_morphTargetCount;
	// The collection of weights for all morph targets in this mesh.
	private float[] m_weights;

    ////////// Methods part of M3G Specification //////////
	
	public int getMorphTargetCount()
	{
		return m_morphTargetCount;
	}
	
	public VertexBuffer getMorphTarget(int index)
	{
		if ((index < 0) || index >= m_morphTargetCount)
			throw new IndexOutOfBoundsException("MorphingMesh: index is either < 0 or > getMorphTargetCount()");
			
		return m_morphTargets[index];
	}
	
	public void getWeights(float[] weights)
	{
		if (weights == null)
			throw new NullPointerException("MorphingMesh: weights is null");
		if (weights.length < m_morphTargetCount)
			throw new IllegalArgumentException("MorphingMesh: size of weights not big enough.");
		
		for (int i = 0; i < m_morphTargetCount; i++)
			weights[i] = m_weights[i];
	}
	
	public void setWeights(float[] weights)
	{
		if (weights == null)
			throw new NullPointerException("MorphingMesh: weights is null");
		if (weights.length < m_morphTargetCount)
			throw new IllegalArgumentException("MorphingMesh: size of weights not big enough.");
		
		for (int i = 0; i < m_morphTargetCount; i++)
			m_weights[i] = weights[i];
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		for (int i = 0; i < m_morphTargets.length; ++i)
		{
			if (references != null)
				references[numReferences] = (Object3D) m_morphTargets[i];
			++numReferences;
		}
		
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return MORPHING_MESH;
	}

	/**
	 * Read field data.
	 * 
	 * @param is The input stream to read from.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * reading the data.
	 */
	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		m_morphTargetCount = (int)is.readUInt32();
		m_morphTargets = new VertexBuffer[m_morphTargetCount];
		m_weights = new float[m_morphTargetCount];
		for (int i = 0; i < m_morphTargetCount; i++)
		{
			long index = is.readObjectIndex();
			M3GObject obj = getObjectAtIndex(table,index,VERTEX_BUFFER);
			if ((obj != null) && (obj instanceof VertexBuffer))
			    m_morphTargets[i] = (VertexBuffer)obj;
			else
				throw new IOException("MorphingMesh:morphTarget-index = " + index);
			float initialWeight = is.readFloat32();
			m_weights[i] = initialWeight;
		}
	}

	/**
	 * Write field data.
	 * 
	 * @param os The output stream to write to.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * writing the data.
	 */
	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeUInt32(m_morphTargetCount);
		for (int i = 0; i < m_morphTargetCount; i++)
		{
			int index = table.indexOf(m_morphTargets[i]);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("MorphingMesh:morphTarget-index = " + index);
            os.writeFloat32(m_weights[i]);
		}
	}
}
