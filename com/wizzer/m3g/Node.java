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

// Import M3G Toolkit classes.
import com.wizzer.m3g.toolkit.util.Path;

public abstract class Node extends Transformable
{
	public static final int NONE    = 144;
	public static final int ORIGIN 	= 145;
	public static final int X_AXIS 	= 146;
	public static final int Y_AXIS 	= 147;
	public static final int Z_AXIS  = 148;

	// Flag indicating whether rendering is enabled.
	private boolean m_renderingEnabled;
	// Flag indicating whether picking is enabled.
	private boolean m_pickingEnabled;
	// The alpha factor.
	private float m_alphaFactor;
	// The scope.
	private int m_scope;
	// Flag indicating alignment.
	private boolean m_hasAlignment;
	// The Z axis target for alignment.
	private int m_zTarget;
	// The Y axis target for alignment.
	private int m_yTarget;
	// The Z axis reference node for alignment.
	private Node m_zReference;
	// The Y axis reference node for alignment.
	private Node m_yReference;
	// The node's parent.
	Node m_parent;

    ////////// Methods not part of M3G Specification //////////

	public void setRenderingEnable(boolean enable)
	{
		m_renderingEnabled = enable;
	}

	public boolean isRenderingEnabled()
	{
		return m_renderingEnabled;
	}

	public void setPickingEnable(boolean enable)
	{
		m_pickingEnabled = enable;
	}

	public boolean isPickingEnabled()
	{
		return m_pickingEnabled;
	}

	public void setScope(int scope)
	{
		m_scope = scope;
	}

	public int getScope()
	{
		return m_scope;
	}

	public void setAlphaFactor(float alphaFactor)
	{
		if (alphaFactor < 0 || alphaFactor > 1)
			throw new IllegalArgumentException("Node: alphaFactor is negative or greater than 1.0");

		m_alphaFactor = alphaFactor;
	}

	public float getAlphaFactor()
	{
		return m_alphaFactor;
	}

	public Node getParent()
	{
		return m_parent;
	}

	public boolean getTransformTo(Node target, Transform transform)
		throws ArithmeticException
	{
		if (target == null)
			throw new NullPointerException("Node: target must not be null");
		if (transform == null)
			throw new NullPointerException("Node: transform must not be null");

		Node node = this;
		
		int nodeDepth = node.getDepth();
		int targetDepth = target.getDepth();
		
		Transform tmp = new Transform();
		Transform targetTransform = new Transform();
		Transform nodeTransform = new Transform();
		
		// Iterate up through the trees until the paths merge.
		while (node != target)
		{
			int nd = nodeDepth;
			if (nodeDepth >= targetDepth)
			{
				node.getCompositeTransform(tmp);
				nodeTransform.postMultiply(tmp);
				
				node = node.getParent();
				--nodeDepth;
			}
			
			if (targetDepth >= nd)
			{
				target.getCompositeTransform(tmp);
				tmp.postMultiply(targetTransform);
				targetTransform.set(tmp);
				
				target = target.getParent();
				--targetDepth;
			}
		}
		
		// Did we find a path? Note: if one is null, actually both
		// should be null.
		if ((node == null) || (target == null))
			return false;

		transform.set(nodeTransform);
		transform.postMultiply(targetTransform);
		return true;
	}

	public final void align(Node reference)
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Node", "align(Node reference)", "Not implemented");
	}

	public void setAlignment(Node zRef, int zTarget, Node yRef, int yTarget)
	{
		if (zTarget < NONE || zTarget > Z_AXIS || yTarget < NONE || yTarget > Z_AXIS)
			throw new IllegalArgumentException("Node: yTarget or zTarget is not one of the symbolic constants");
		if ((zRef == yRef) && ((zTarget == yTarget) && (yTarget != NONE)))
			throw new IllegalArgumentException("Node: (zRef == yRef) &&  (zTarget == yTarget != NONE)");
		if (zRef == this || yRef == this)
			throw new IllegalArgumentException("Node: zRef or yRef is this Node");

		m_zReference = zRef;
		this.m_zTarget = zTarget;
		m_yReference = yRef;
		this.m_yTarget = yTarget;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		// Note: the alignment reference nodes are excluded from this as called
		// out by the specification.
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	protected Node()
	{
		m_renderingEnabled = true;
		m_pickingEnabled = true;
		m_alphaFactor = 1.0f;
		m_scope = -1;
	}

	public boolean hasAlignment()
	{
		return m_hasAlignment;
	}
	
	public int getZTarget()
	{
		return m_zTarget;
	}
	
	public int getYTarget()
	{
		return m_yTarget;
	}
	
	public Node getZReference()
	{
		return m_zReference;
	}
	
	public Node getYReference()
	{
		return m_yReference;
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

		setRenderingEnable(is.readBoolean());
		setPickingEnable(is.readBoolean());
		setAlphaFactor(((float)is.readByte())/255f);
		setScope((int)is.readUInt32());
		m_hasAlignment = is.readBoolean();
		if (m_hasAlignment)
		{
			int zTarget = is.readByte();
			int yTarget = is.readByte();
			Node zReference = null, yReference = null;
			long index = is.readObjectIndex();
			if (index != 0)
			{
				M3GObject obj = getObjectAtIndex(table, index, -1);
				if (obj != null && obj instanceof Node) zReference = (Node)obj;
					else throw new IOException("Node:zReference-index = " + index);
			}
			index = is.readObjectIndex();
			if (index != 0)
			{
				M3GObject obj = getObjectAtIndex(table, index, -1);
				if (obj != null && obj instanceof Node) yReference = (Node)obj;
					else throw new IOException("Node:yReference-index = " + index);
			}
			setAlignment(zReference, zTarget, yReference, yTarget);
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

		os.writeBoolean(m_renderingEnabled);
		os.writeBoolean(m_pickingEnabled);
		os.writeByte((byte)(m_alphaFactor*255));
		os.writeUInt32(m_scope);
		m_hasAlignment = (m_zReference != null || m_yReference != null);
		os.writeBoolean(m_hasAlignment);
		if (m_hasAlignment)
		{
			os.writeByte(m_zTarget);
			os.writeByte(m_yTarget);
			int index = table.indexOf(m_zReference);
			if (index > 0) os.writeObjectIndex(index);
				else throw new IOException("Node:zReference-index = " + index);
			index = table.indexOf(m_yReference);
			if (index > 0) os.writeObjectIndex(index);
				else throw new IOException("Node:yReference-index = " + index);
		}
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		if (m_zReference != null) m_zReference.buildReferenceTable(table);
		if (m_yReference != null) m_yReference.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
	
	// Determine the depth of this node to the root of the scene graph.
	private int getDepth()
	{
		int depth = 0;
		Node node = this;
		while (node != null)
		{
			++depth;
			node = node.getParent();
		}
		return depth;
	}
}