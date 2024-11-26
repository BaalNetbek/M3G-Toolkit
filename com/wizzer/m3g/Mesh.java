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
import java.io.*;
import java.util.*;

public class Mesh extends Node
{
	// The associated vertex buffer.
	private VertexBuffer m_vertexBuffer;
	// The associated index buffers.
	private IndexBuffer m_indexBuffers[];
	// The associated appearances.
	private Appearance m_appearances[];
	
    ////////// Methods part of M3G Specification //////////

	public Mesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance)
	{
		this(vertices, new IndexBuffer[] {submesh}, new Appearance[] {appearance});
	}

	public Mesh(VertexBuffer vertices, IndexBuffer submeshes[], Appearance appearances[])
	{
		if (vertices == null)
			throw new NullPointerException("Mesh: vertexBuffer is null");
		if (submeshes == null)
			throw new NullPointerException("Mesh: indexBuffers is null");
		for (int i = 0; i < submeshes.length; i++)
			if (submeshes[i] == null)
				throw new NullPointerException("Mesh: any element in indexBuffers  is null");
		if (submeshes.length == 0)
			throw new IllegalArgumentException("Mesh: indexBuffers is empty");
		if (appearances.length < submeshes.length)
			throw new IllegalArgumentException("Mesh: appearances.length < indexBuffers.length");
		
		m_vertexBuffer = vertices;
		m_indexBuffers = submeshes;
		m_appearances = appearances;
	}

	public void setAppearance(int index,Appearance appearance)
	{
		m_appearances[index] = appearance;
	}

	public Appearance getAppearance(int index)
	{
		return m_appearances[index];
	}

	public IndexBuffer getIndexBuffer(int index)
	{
		return m_indexBuffers[index];
	}

	public VertexBuffer getVertexBuffer()
	{
		return m_vertexBuffer;
	}

	public int getSubmeshCount()
	{
		return m_indexBuffers.length;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_vertexBuffer != null)
		{
			if (references != null)
				references[numReferences] = m_vertexBuffer;
			++numReferences;
		}
		
		for (int i = 0; i < m_indexBuffers.length; ++i)
		{
			if (references != null)
				references[numReferences] = (Object3D) m_indexBuffers[i];
			++numReferences;
		}
		
		for (int i = 0; i < m_appearances.length; ++i)
		{
			if (references != null)
				references[numReferences] = (Object3D) m_appearances[i];
			++numReferences;
		}
		
		return numReferences;
	}


    ////////// Methods not part of M3G Specification //////////
	
	Mesh() {}

	public int getObjectType()
	{
		return MESH;
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

		// Read vertexBuffer
		long index = is.readObjectIndex();
		M3GObject obj = getObjectAtIndex(table,index,VERTEX_BUFFER);
		if (obj != null)
			m_vertexBuffer = (VertexBuffer)obj;
		else
			throw new IOException("Mesh:vertexBuffer-index = " + index);
		// Read submeshCount
		long submeshCount = is.readUInt32();
		m_indexBuffers = new IndexBuffer[(int)submeshCount];
		m_appearances = new Appearance[(int)submeshCount];
		for (int i = 0; i < submeshCount; i++)
		{
			// Read indexBuffer
			index = is.readObjectIndex();
			obj = getObjectAtIndex(table, index, -1);
			if (obj != null && obj instanceof IndexBuffer)
				m_indexBuffers[i] = (IndexBuffer)obj;
			else
				throw new IOException("Mesh:indexBuffers-index = " + index);
			// Read appearance
			if ((index = is.readObjectIndex()) != 0)
			{
				obj = getObjectAtIndex(table,index,APPEARANCE);
				if (obj != null)
					m_appearances[i] = (Appearance)obj;
				else
					throw new IOException("Mesh:appearances-index = "+index);
			}
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

		// Write vertexBuffer
		int index = table.indexOf(m_vertexBuffer);
		if (index > 0)
			os.writeObjectIndex(index);
		else
			throw new IOException("Mesh:vertexBuffer-index = " + index);
		// Write submeshCount
		os.writeUInt32(getSubmeshCount());
		for (int i = 0; i < getSubmeshCount(); i++)
		{
			// Write indexBuffer
			index = table.indexOf(m_indexBuffers[i]);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Mesh:indexBuffers-index = "+index);
			// Write appearance
			if (m_appearances[i] != null)
			{
				index = table.indexOf(m_appearances[i]);
				if (index > 0)
					os.writeObjectIndex(index);
				else
					throw new IOException("Mesh:appearances-index = "+index);

			}
			else os.writeObjectIndex(0);
		}
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		m_vertexBuffer.buildReferenceTable(table);
		for (int i = 0; i < getSubmeshCount(); i++)
			m_indexBuffers[i].buildReferenceTable(table);
		for (int i = 0; i < getSubmeshCount(); i++)
		{
			if (m_appearances[i] != null)
				m_appearances[i].buildReferenceTable(table);
		}

		super.buildReferenceTable(table);
	}
}
