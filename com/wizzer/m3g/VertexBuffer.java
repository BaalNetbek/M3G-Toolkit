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

public class VertexBuffer extends Object3D
{
	// The default color.
	private int m_defaultColor;
	// The array of vertex positions.
	private VertexArray m_positions;
	// The uniform scale factor common to all vertex positions.
	private float m_positionScale;
	// A constant (x, y, z) offset to add to the vertex position after scaling.
	private float m_positionBias[];
	// The array of vertex normals.
	private VertexArray m_normals;
	// The array of vertex colors.
	private VertexArray m_colors;
	// The array of vertex texture coordinates.
    private VertexArray m_texCoords[];
    // The texture coordinate scales.
	private float m_texCoordScale[];
	// The texture coordinate biases.
	private float m_texCoordBias[][];

    ////////// Methods part of M3G Specification //////////
	
	public VertexBuffer()
	{
		m_defaultColor = 0xffffffff;
		m_positionBias = new float[3];
		m_texCoords = new VertexArray[256];
		m_texCoordScale = new float[256];
		m_texCoordBias = new float[256][3];
	}

	public int getVertexCount()
	{
		VertexArray va = m_positions;
		if (va == null) va = m_normals;
		if (va == null) va = m_colors;
		for (int i = 0; i < m_texCoords.length && va == null; i++)
			if (m_texCoords[i] != null)
				va = m_texCoords[i];
		return (va != null ? va.m_vertexCount : 0);
	}

	public void setPositions(VertexArray positions,float scale,float bias[])
	{
		if (bias == null) bias = new float[3];
		if (positions != null && positions.m_componentCount != 3)
			throw new IllegalArgumentException("VertexBuffer: positions.numComponents != 3");
		if (positions != null && positions.m_vertexCount != getVertexCount() && getVertexCount() > 0)
			throw new IllegalArgumentException("VertexBuffer: (positions.numVertices != getVertexCount) && (getVertexCount > 0)");
		if (positions != null && bias.length < 3)
			throw new IllegalArgumentException("VertexBuffer: (positions != null) &&  (bias.length < 3)");
		
		m_positions = positions;
		m_positionScale = scale;
		m_positionBias = new float[]{bias[0],bias[1],bias[2]};
	}

	public VertexArray getPositions(float scaleBias[])
	{
		if (scaleBias != null)
		{
			if (scaleBias.length < 4)
				throw new IllegalArgumentException("VertexBuffer: scaleBias.length < 4");
			scaleBias[0] = m_positionScale;
			System.arraycopy(m_positionBias,0,scaleBias,1,m_positionBias.length);
		}
		return m_positions;
	}

	public void setTexCoords(int index,VertexArray texCoords,float scale,float bias[])
	{
		if (bias == null)
			bias = new float[3];
		if (texCoords != null && texCoords.m_componentCount < 2 || texCoords.m_componentCount > 3)
			throw new IllegalArgumentException("VertexBuffer: texCoords.numComponents != [2,3]");
		if (texCoords != null && texCoords.m_vertexCount != getVertexCount() && getVertexCount() > 0)
			throw new IllegalArgumentException("VertexBuffer: (texCoords.numVertices != getVertexCount) && (getVertexCount > 0)");
		if (texCoords != null && bias.length < texCoords.m_componentCount)
			throw new IllegalArgumentException("(VertexBuffer: texCoords != null) &&  (bias.length < texCoords.numComponents)");

		m_texCoords[index] = texCoords;
		m_texCoordScale[index] = scale;
		m_texCoordBias[index] = new float[]{bias[0],bias[1],0};
		if (bias.length == 3)
			m_texCoordBias[index][2] = bias[2];
	}

	public VertexArray getTexCoords(int index,float scaleBias[])
	{
		if (scaleBias != null)
		{
			if (scaleBias.length < 4)
				throw new IllegalArgumentException("VertexBuffer: scaleBias.length <  numComponents+1");
			scaleBias[0] = m_texCoordScale[index];
			System.arraycopy(m_texCoordBias[index], 0, scaleBias, 1, m_texCoordBias[index].length);
		}
		return m_texCoords[index];
	}

	public void setNormals(VertexArray normals)
	{
		if (normals != null && normals.m_componentCount != 3)
			throw new IllegalArgumentException("VertexBuffer: normals.numComponents != 3");
		if (normals != null && normals.m_vertexCount != getVertexCount() && getVertexCount() > 0)
			throw new IllegalArgumentException("VertexBuffer: (normals.numVertices != getVertexCount) && (getVertexCount > 0)");
		
		m_normals = normals;
	}

	public VertexArray getNormals()
	{
		return m_normals;
	}

	public void setColors(VertexArray colors)
	{
		if (colors != null && colors.m_componentCount != 1)
			throw new IllegalArgumentException("VertexBuffer: colors.numComponents != 1");
		if (colors != null && (colors.m_componentCount < 3 || colors.m_componentCount > 4))
			throw new IllegalArgumentException("VertexBuffer: colors.numComponents != [3,4]");
		if (colors != null && (colors.m_vertexCount != getVertexCount() && getVertexCount() > 0))
			throw new IllegalArgumentException("VertexBuffer: (colors.numVertices != getVertexCount) && (getVertexCount > 0)");

		m_colors = colors;
	}

	public VertexArray getColors()
	{
		return m_colors;
	}

	public void setDefaultColor(int ARGB)
	{
		m_defaultColor = ARGB;
	}

	public int getDefaultColor()
	{
		return m_defaultColor;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_positions != null)
		{
			if (references != null)
				references[numReferences] = m_positions;
			++numReferences;
		}
		
		if (m_normals != null)
		{
			if (references != null)
				references[numReferences] = m_normals;
			++numReferences;
		}
		
		if (m_colors != null)
		{
			if (references != null)
				references[numReferences] = m_colors;
			++numReferences;
		}
		
		for (int i = 0; i < m_texCoords.length; ++i)
		{
			if (m_texCoords[i] != null)
			{
				if (references != null)
					references[numReferences] = m_texCoords[i];
				++numReferences;
			}
		}
		
		return numReferences;
	}

    ////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return VERTEX_BUFFER;
	}
	
	// Remember state of texture coordinate array count.
	private int m_texcoordArrayCount;
	
	public int getTexcoordArrayCount()
	{
		return m_texcoordArrayCount;
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

		m_defaultColor = is.readColorRGBA();
		long index = 0;
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table,index,VERTEX_ARRAY);
			if (obj != null)
				m_positions = (VertexArray)obj;
			else
				throw new IOException("VertexBuffer:positions-index = " + index);
		}
		for (int i = 0; i < 3; i++)
			m_positionBias[i] = is.readFloat32();
		m_positionScale = is.readFloat32();
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table,index,VERTEX_ARRAY);
			if (obj != null)
				m_normals = (VertexArray)obj;
			else
				throw new IOException("VertexBuffer:normals-index = " + index);
		}
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table,index,VERTEX_ARRAY);
			if (obj != null)
				m_colors = (VertexArray)obj;
			else
				throw new IOException("VertexBuffer:colors-index = " + index);
		}
		long units = is.readUInt32();
		for (int i = 0; i < units; i++)
		{
			if ((index = is.readObjectIndex()) > 0)
			{
				M3GObject obj = getObjectAtIndex(table,index,VERTEX_ARRAY);
				if (obj != null)
					m_texCoords[i] = (VertexArray)obj;
				else
					throw new IOException("VertexBuffer:texCoords-index = " + index);
			}
			for (int j = 0; j < 3; j++)
				m_texCoordBias[i][j] = is.readFloat32();
			m_texCoordScale[i] = is.readFloat32();
		}
		
		// Cache texture coordinate array count for dumping state.
		m_texcoordArrayCount = (int)units;
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
	protected void marshall(M3GOutputStream os,ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeColorRGBA(m_defaultColor);
		if (m_positions!=null)
		{
			int index = table.indexOf(m_positions);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("VertexBuffer:positions-index = " + index);
		}
		else os.writeObjectIndex(0);
		for (int i = 0; i < 3; i++)
			os.writeFloat32(m_positionBias[i]);
		os.writeFloat32(m_positionScale);
		if (m_normals != null)
		{
			int index = table.indexOf(m_normals);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("VertexBuffer:normals-index = " + index);
		}
		else os.writeObjectIndex(0);
		if (m_colors != null)
		{
			int index = table.indexOf(m_colors);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("VertexBuffer:colors-index = " + index);
		}
		else os.writeObjectIndex(0);
		int units = m_texCoords.length;
		while (units > 0 && m_texCoords[units - 1] == null)
			units--;
		os.writeUInt32(units);
		for (int i = 0; i < units; i++)
		{
			if (m_texCoords[i] != null)
			{
				int index = table.indexOf(m_texCoords[i]);
				if (index > 0)
					os.writeObjectIndex(index);
				else
					throw new IOException("VertexBuffer:texCoords-index = " + index);
				for (int j = 0; j < 3; j++)
					os.writeFloat32(m_texCoordBias[i][j]);
				os.writeFloat32(m_texCoordScale[i]);
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
		if (m_positions != null) m_positions.buildReferenceTable(table);
		if (m_normals != null) m_normals.buildReferenceTable(table);
		if (m_colors != null) m_colors.buildReferenceTable(table);
		for (int i = 0; i < m_texCoords.length; i++)
			if (m_texCoords[i] != null)
				m_texCoords[i].buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}
