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

public class SkinnedMesh extends Mesh
{
	// The skeleton.
	private Group m_skeleton;
	// The collection of bones.
	private ArrayList m_bones = new ArrayList();
	
	/**
	 * Helper class for managing transform nodes (bones).
	 * 
	 * @author Mark Millard
	 */
	protected class BoneNode
	{
		public Node m_transformNode;
		public Long m_firstVertex;
		public Long m_vertexCount;
		public Long m_weight;
		public Transform m_atRestTransformation;
	};

    ////////// Methods part of M3G Specification //////////

	public SkinnedMesh(VertexBuffer vertices, IndexBuffer submeshes[], Appearance appearances[], Group skeleton)
	{
		super(vertices, submeshes, appearances);
		m_skeleton = skeleton;
	}

	public SkinnedMesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance, Group skeleton)
	{
		super(vertices, submesh, appearance);
		m_skeleton = skeleton;
	}

	public Group getSkeleton()
	{
		return m_skeleton;
	}
	
	public void addTransform(Node bone, int weight, int firstVertex, int numVertices)
	    throws ArithmeticException
	{
		if (bone == null)
			throw new NullPointerException("SkinnedMesh: bone is null");
		// Validate that the bone is either the skeleton Group or one of its descendants.
		if (! isDescendant(bone))
			throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
		if (weight <= 0)
			throw new IllegalArgumentException("SkinnedMesh: weight <= 0");
		if (numVertices <= 0)
			throw new IllegalArgumentException("SkinnedMesh: numVertices <= 0");
		if ((firstVertex < 0) || (firstVertex + numVertices > 65535))
			throw new IndexOutOfBoundsException("SkinnedMesh: (firstVertex < 0) or range of vertices not in [0, 65536]");
		
		// Calculate the at-rest transformation.
		Transform transform = new Transform();
		boolean valid = getTransformTo(bone, transform);
		if (! valid)
			throw new ArithmeticException();
		
		BoneNode node = new BoneNode();
		node.m_transformNode = bone;
		node.m_firstVertex = new Long(firstVertex);
		node.m_vertexCount = new Long(numVertices);
		node.m_weight = new Long(weight);
		node.m_atRestTransformation = transform;
		m_bones.add(node);
	}
	
	public void getBoneTransform(Node bone, Transform transform)
	{
		if ((bone == null) || (transform == null))
			throw new NullPointerException("SkinnedMesh: bone or transform is null");
		// Validate that the bone is either the skeleton Group or one of its descendants.
		if (! isDescendant(bone))
			throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
		
		for (int i = 0; i < m_bones.size(); i++)
		{
			BoneNode node = (BoneNode)m_bones.get(i);
			if (node.m_transformNode.equals(bone))
			{
				transform.set(node.m_atRestTransformation);
				break;
			}
		}
	}
	
	public int getBoneVertices(Node bone, int[] indices, float[] weights)
	{
		long numVertices = 0;
		
		if (bone == null)
			throw new NullPointerException("SkinnedMesh: bone is null");
		// Validate that the bone is either the skeleton Group or one of its descendants.
		if (! isDescendant(bone))
			throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
		
		for (int i = 0; i < m_bones.size(); i++)
		{
			BoneNode node = (BoneNode)m_bones.get(i);
			if (node.m_transformNode.equals(bone))
			{
				long firstVertex = node.m_firstVertex;
				long vertexCount = node.m_vertexCount;
				if (((indices != null) && (weights != null)) &&
					((indices.length < vertexCount) || (weights.length < vertexCount)))
				    throw new IllegalArgumentException();
				
				// XXX - complete filling out indices and weights arrays.
				
				numVertices = vertexCount;
                break;
			}
		}
		
		return (int)numVertices;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_skeleton != null)
		{
			if (references != null)
				references[numReferences] = m_skeleton;
			++numReferences;
		}
		
		// XXX - not sure if "bone" transforms should be included here.
				
		return numReferences;
	}

	////////// Methods not part of M3G Specification //////////
	
	SkinnedMesh()
	{
		// Do nothing extra.
	}

	public void setSkeleton(Group skeleton)
	{
		m_skeleton = skeleton;
	}

	public int getTransformReferenceCount()
	{
		int count = m_bones.size();
		return count;
	}
	
	public Node getTransformNode(int index)
	{
		BoneNode node = (BoneNode)m_bones.get(index);
		return node.m_transformNode;
	}
	
	public long getFirstVertex(int index)
	{
		BoneNode node = (BoneNode)m_bones.get(index);
		Long value = node.m_firstVertex;
		return value.intValue();
	}

	public long getVertexCount(int index)
	{
		BoneNode node = (BoneNode)m_bones.get(index);
		Long value = node.m_vertexCount;
		return value.intValue();
	}

	public int getWeight(int index)
	{
		BoneNode node = (BoneNode)m_bones.get(index);
		Long value = node.m_weight;
		return value.intValue();
	}

	public int getObjectType()
	{
		return SKINNED_MESH;
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
		M3GObject obj = getObjectAtIndex(table,index,GROUP);
		if (obj != null)
			m_skeleton = (Group) obj;
		else
			throw new IOException("SkinnedMesh:skeleton-index = " + index);
		long transformReferenceCount = is.readUInt32();
		for (int i = 0; i < transformReferenceCount; i++)
		{
			BoneNode node = new BoneNode();
			index = is.readObjectIndex();
			obj = getObjectAtIndex(table,index,-1);
			if (obj != null && obj instanceof Node)
				node.m_transformNode = (Node)obj;
			else
				throw new IOException("SkinnedMesh:transformNode-index = " + index);
			node.m_firstVertex = new Long(is.readUInt32());
            node.m_vertexCount = new Long(is.readUInt32());
            node.m_weight = new Long(is.readInt32());
            // Set at-rest transformation.
            Transform t = new Transform();
            this.getTransformTo((Node)obj, t);
            node.m_atRestTransformation = t;
			m_bones.add(node);
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
	protected void marshall(M3GOutputStream os,ArrayList table) throws IOException
	{
		super.marshall(os,table);

		int index = table.indexOf(m_skeleton);
		if (index > 0)
			os.writeObjectIndex(index);
		else
			throw new IOException("SkinnedMesh:skeleton-index = " + index);
		int transformReferenceCount = m_bones.size();
		os.writeUInt32(transformReferenceCount);
		for (int i = 0; i < m_bones.size(); i++)
		{
			index = table.indexOf(((BoneNode)m_bones.get(i)).m_transformNode);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("SkinnedMesh:transformNode-index = " + index);
			os.writeUInt32(((BoneNode)m_bones.get(i)).m_firstVertex.intValue());
			os.writeUInt32(((BoneNode)m_bones.get(i)).m_vertexCount.intValue());
			os.writeInt32(((BoneNode)m_bones.get(i)).m_weight.intValue());
		}
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		m_skeleton.buildReferenceTable(table);
		for (int i = 0; i < m_bones.size(); i++)
		{
			BoneNode node = ((BoneNode)m_bones.get(i));
			((M3GObject) node.m_transformNode).buildReferenceTable(table);
		}

		super.buildReferenceTable(table);
	}
	
	// Determine whether the specified bone is a descendant of m_skeleton.
	private boolean isDescendant(Node bone)
	{
		if (bone.equals(m_skeleton))
			return true;

		Node parent = bone.getParent();
		if (parent == null)
			return false;
		else
			return isDescendant(parent);
	}
}