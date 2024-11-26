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
import java.util.logging.*;

public class Group extends Node
{
	private ArrayList m_children;

	////////// Methods part of M3G Specification //////////

	/**
	 * The default constructor.
	 * <p>
	 * Constructs a <code>Group</code> node and initializes it with an empty
	 * list of children.
	 * </p>
	 */
	public Group()
	{
		m_children = new ArrayList();
	}

    public void addChild(Node child)
	{
    	if (child == null)
    		throw new NullPointerException("Group: child is null");
		if (child == this)
			throw new IllegalArgumentException("Group: child is this Group");
		else if (child instanceof World)
			throw new IllegalArgumentException("Group: child is a World node");
		else if (child.m_parent != null)
			throw new IllegalArgumentException("Group: child already has a parent");
		else
		{
			Node parent = getParent();
			while (parent != null)
			{
				if (parent == child)
					throw new IllegalArgumentException("Group: child is an ancestor of this Group");
				parent = parent.getParent();
			}
		}
		
		m_children.add(child);
		child.m_parent = this;
	}

	public void removeChild(Node child)
	{
		// TODO: IllegalArgumentException - if removing child would break a connection
		// between a SkinnedMesh node and one of its transform references.
		if (m_children.remove(child))
			child.m_parent = null;
	}

	public int getChildCount()
	{
		return m_children.size();
	}

	public Node getChild(int index)
	{
		return (Node)m_children.get(index);
	}

	public boolean pick(int scope, float ox, float oy, float oz,
			            float dx, float dy, float dz, RayIntersection ri)
	{
		Logger.global.logp(Level.WARNING, "com.wizzwe.m3g.Group", "pick(int scope,float ox,float oy,float oz,float dx,float dy,float dz,RayIntersection ri)", "Not implemented");
		return false;
	}

	public boolean pick(int scope, float x, float y, Camera camera, RayIntersection ri)
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Group", "pick(int scope,float x,float y,Camera camera,RayIntersection ri)", "Not implemented");
		return false;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		if (references != null)
			for (int i = 0; i < m_children.size(); ++i)
				references[numReferences + i] = (Object3D) m_children.get(i);
		return numReferences + m_children.size();
	}

	////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return GROUP;
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

		// Read the number of children
		long childs = is.readUInt32();
		for (int i = 0; i < childs; i++)
		{
			// Read the child reference
			long index = is.readObjectIndex();
			M3GObject obj = getObjectAtIndex(table, index,-1);
			if (obj != null && obj instanceof Node)
				addChild((Node)obj);
			else
				throw new IOException("Group:child-index = " + index);
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

		// Write the number of children
		os.writeUInt32(getChildCount());
		for (int i = 0; i < getChildCount(); i++)
		{
			// Write the child reference
			int index = table.indexOf(getChild(i));
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Group:child-index = " + index);
		}
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		for (int i = 0; i < getChildCount(); i++)
			getChild(i).buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}
