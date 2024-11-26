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

// The standard Java classes.
import java.io.*;
import java.util.*;

public class World extends Group
{
	// The active camera.
	private Camera m_activeCamera;
	// The background.
	private Background m_background;

	/**
	 * The default constructor.
	 * <p>
	 * Creates an empty <code>World</code> with default values.
	 * The defaults are:
	 * <ul>
	 * <li>background : null (clear to black)</li>
	 * <li>active camera : null (the word is not renderable</li>
	 * </ul>
	 */
	public World()
	{
		// Do nothing extra.
	}

	/**
	 * Set the <code>Background</code> for this <code>World</code>.
	 * 
	 * @param background Attributes for clearing the frame buffer,
	 * or <b>null</b> to use defaults.
	 */
	public void setBackground(Background background)
	{
		m_background = background;
	}

	/**
	 * Retrieves the background settings of the <code>World</code>.
	 * 
	 * @return The current attributes for clearing the frame buffer
	 * are returned.
	 */
	public Background getBackground()
	{
		return m_background;
	}

	/**
	 * Set the <code>Camera</code> to use when rendering this
	 * <code>World</code>.
	 * 
	 * @param camera The <code>Camera</code> object to set as the
	 * active camera.
	 * 
	 * @throws NullPointerException This exception is thrown if
	 * <i>camera</i> is <b>null</b>.
	 */
	public void setActiveCamera(Camera camera)
	{
		if (camera == null)
			throw new NullPointerException("World: camera is null");
		
		m_activeCamera = camera;
	}

	/**
	 * Get the currently active camera.
	 * 
	 * @return The camera that is currently used to render this
	 * <code>World</code>.
	 */
	public Camera getActiveCamera()
	{
		return m_activeCamera;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_activeCamera != null)
		{
			if (references != null)
				references[numReferences] = m_activeCamera;
			++numReferences;
		}
		
		if (m_background != null)
		{
			if (references != null)
				references[numReferences] = m_background;
			++numReferences;
		}
		
		return numReferences;
	}

	
    ////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return WORLD;
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

		long index = is.readObjectIndex();
		M3GObject obj = getObjectAtIndex(table, index, CAMERA);
		if (obj != null)
			m_activeCamera=(Camera)obj;
		else
			throw new IOException("World:activeCamera-index = " + index);
		if ((index = is.readObjectIndex()) != 0)
		{
			obj = getObjectAtIndex(table, index, BACKGROUND);
			if (obj != null)
				m_background = (Background)obj;
			else
				throw new IOException("World:background-index = " + index);
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

		int index = table.indexOf(m_activeCamera);
		if (index > 0)
			os.writeObjectIndex(index);
		else
			throw new IOException("World:activeCamera-index = " + index);
		if (m_background != null)
		{
			index = table.indexOf(m_background);
			if (index>0)
				os.writeObjectIndex(index);
			else
				throw new IOException("World:background-index = " + index);
		}
		else os.writeObjectIndex(0);
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		if (m_activeCamera == null)
			throw new NullPointerException("World:activeCamera is null");
		
		m_activeCamera.buildReferenceTable(table);
		if (m_background != null)
			m_background.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}
