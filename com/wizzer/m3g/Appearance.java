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

public class Appearance extends Object3D
{
	// The associated rendering layer.
	private int m_layer;
	// The associated CompositingMode.
	private CompositingMode m_compositingMode;
	// The associated Fog.
	private Fog m_fog;
	// The associated PolygonMode.
	private PolygonMode m_polygonMode;
	// The associated Material.
	private Material m_material;
	// The associated Texture2D.
	private Texture2D m_textures[];

    ////////// Methods part of M3G Specification //////////

	/**
	 * The default constructor.
	 * <p>
	 * Constructs an <code>Appearance</code> object with default values.
	 * </p>
	 */
	public Appearance()
	{
		m_layer = 0;
		m_textures = new Texture2D[256];
	}

	public void setLayer(int layer)
	{
		if (layer < -63 || layer > 63)
			throw new IndexOutOfBoundsException("Appearance: layer is not in [-63, 63]");
		m_layer = layer;
	}

	public int getLayer()
	{
		return m_layer;
	}

	public void setFog(Fog fog)
	{
		m_fog = fog;
	}

	public Fog getFog()
	{
		return m_fog;
	}

	public void setPolygonMode(PolygonMode polygonMode)
	{
		m_polygonMode = polygonMode;
	}

	public PolygonMode getPolygonMode()
	{
		return m_polygonMode;
	}

	public void setCompositingMode(CompositingMode compositingMode)
	{
		m_compositingMode = compositingMode;
	}

	public CompositingMode getCompositingMode()
	{
		return m_compositingMode;
	}

	public void setTexture(int index,Texture2D texture)
	{
		m_textures[index] = texture;
	}

	public Texture2D getTexture(int index)
	{
		return m_textures[index];
	}

	public void setMaterial(Material material)
	{
		m_material = material;
	}

	public Material getMaterial()
	{
		return m_material;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_material != null)
		{
			if (references != null)
				references[numReferences] = m_material;
			++numReferences;
		}
		
		if (m_polygonMode != null)
		{
			if (references != null)
				references[numReferences] = m_polygonMode;
			++numReferences;
		}
		
		if (m_compositingMode != null)
		{
			if (references != null)
				references[numReferences] = m_compositingMode;
			++numReferences;
		}
		
		if (m_fog != null)
		{
			if (references != null)
				references[numReferences] = m_fog;
			++numReferences;
		}

		for (int i = 0; i < m_textures.length; ++i)
		{
			if (m_textures[i] != null)
			{
				if (references != null)
					references[numReferences] = (Object3D) m_textures[i];
				++numReferences;
			}
		}
		return numReferences;
	}

	////////// Methods not part of M3G Specification //////////
	
	public int getObjectType()
	{
		return APPEARANCE;
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

		// Read layer
		setLayer(is.readByte());
		long index = 0;
		// Read compositingMode
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table, index, COMPOSITING_MODE);
			if (obj != null)
				setCompositingMode((CompositingMode)obj);
			else
				throw new IOException("Appearance:compositingMode-index = " + index);
		}
		// Read fog
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table, index, FOG);
			if (obj != null)
				setFog((Fog)obj);
			else
				throw new IOException("Appearance:fog-index = " + index);
		}
		// Read polygonMode
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table, index, POLYGON_MODE);
			if (obj != null)
				setPolygonMode((PolygonMode)obj);
			else
				throw new IOException("Appearance:polygonMode-index = " + index);
		}
		// Read material
		if ((index = is.readObjectIndex()) != 0)
		{
			M3GObject obj = getObjectAtIndex(table,index,MATERIAL);
			if (obj != null)
				setMaterial((Material)obj);
			else
				throw new IOException("Appearance:material-index = " + index);
		}
		// Read number of textures
		long units = is.readUInt32(); // XXX - not in specification!
		for (int i = 0; i < units; i++)
		{
			// Read texture
			if ((index = is.readObjectIndex()) != 0)
			{
				M3GObject obj = getObjectAtIndex(table,index,TEXTURE2D);
				if (obj != null)
					setTexture(i,(Texture2D)obj);
				else
					throw new IOException("Appearance:texture-index = " + index);
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
		super.marshall(os, table);

		// Write layer
		os.writeByte(m_layer);
		// Write compositingMode
		if (m_compositingMode != null)
		{
			int index = table.indexOf(m_compositingMode);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Appearance:compositingMode-index = " + index);
		}
		else os.writeObjectIndex(0);
		// Write fog
		if (m_fog != null)
		{
			int index = table.indexOf(m_fog);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Appearance:fog-index = " + index);
		}
		else os.writeObjectIndex(0);
		// Write polygonMode
		if (m_polygonMode != null)
		{
			int index = table.indexOf(m_polygonMode);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Appearance:polygonMode-index = " + index);
		}
		else os.writeObjectIndex(0);
		// Write material
		if (m_material != null)
		{
			int index = table.indexOf(m_material);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Appearance:material-index = " + index);
		}
		else os.writeObjectIndex(0);
		// Write number of textures
		int units = m_textures.length;
		while (units > 0 && m_textures[units-1] == null) units--;
		os.writeUInt32(units);
		for (int i = 0; i < units; i++)
		{
			// Write texture
			if (m_textures[i] != null)
			{
				int index = table.indexOf(m_textures[i]);
				if (index > 0)
					os.writeObjectIndex(index);
				else
					throw new IOException("Appearance:texture-index = " + index);
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
		if (m_compositingMode != null)
			m_compositingMode.buildReferenceTable(table);
		if (m_fog != null)
			m_fog.buildReferenceTable(table);
		if (m_polygonMode != null)
			m_polygonMode.buildReferenceTable(table);
		if (m_material != null)
			m_material.buildReferenceTable(table);
		for (int i = 0; i < m_textures.length; i++)
			if (m_textures[i] != null)
				m_textures[i].buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}