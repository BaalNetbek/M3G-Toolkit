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

/**
 * The <code>SceneSection</code> is a utility for reading/writing
 * the Scene Section from/to a M3G file. It is not a runtime
 * construct called out by the M3G Specification.
 * <p>
 * This class is used to manage the M3G file scene sections.
 * </p>
 * 
 * @author Mark Millard
 */
public class SceneSection extends Section
{
	private ArrayList<Object3D> m_objects3D;

	public SceneSection()
	{
		m_objects3D = new ArrayList<Object3D>();
	}

	public void addObject3D(Object3D object)
	{
		m_objects3D.add(object);
	}

	public Object3D[] getObjects3D()
	{
		return (Object3D[])m_objects3D.toArray(new Object3D[m_objects3D.size()]);
	}

	public void removeObject3D(Object3D object)
	{
		m_objects3D.remove(object);
	}

	public void removeObjects3D()
	{
		m_objects3D.clear();
	}

	/**
	 * Read object references.
	 * 
	 * @param is The input stream to read from.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * reading the data.
	 */
	protected void readObjects(M3GInputStream is, ArrayList table) throws IOException
	{
		while (is.available() > 0)
		{
			byte type = (byte)is.readByte();
			long length = is.readUInt32();
			Object3D object = null;
			if (type == M3GObject.ANIMATION_CONTROLLER) object = new AnimationController();
			else if (type == M3GObject.ANIMATION_TRACK) object = new AnimationTrack();
			else if (type == M3GObject.APPEARANCE) object = new Appearance();
			else if (type == M3GObject.BACKGROUND) object = new Background();
			else if (type == M3GObject.CAMERA) object = new Camera();
			else if (type == M3GObject.COMPOSITING_MODE) object = new CompositingMode();
			else if (type == M3GObject.FOG) object = new Fog();
			else if (type == M3GObject.POLYGON_MODE) object = new PolygonMode();
			else if (type == M3GObject.GROUP) object = new Group();
			else if (type == M3GObject.IMAGE2D) object = new Image2D();
			else if (type == M3GObject.TRIANGLE_STRIP_ARRAY) object = new TriangleStripArray();
			else if (type == M3GObject.LIGHT) object = new Light();
			else if (type == M3GObject.MATERIAL) object = new Material();
			else if (type == M3GObject.MESH) object = new Mesh();
			else if (type == M3GObject.MORPHING_MESH) object = null;
			else if (type == M3GObject.SKINNED_MESH) object = new SkinnedMesh();
			else if (type == M3GObject.TEXTURE2D) object = new Texture2D();
			else if (type == M3GObject.SPRITE3D) object = new Sprite3D();
			else if (type == M3GObject.KEYFRAME_SEQUENCE) object = new KeyframeSequence();
			else if (type == M3GObject.VERTEX_ARRAY) object = new VertexArray();
			else if (type == M3GObject.VERTEX_BUFFER) object = new VertexBuffer();
			else if (type == M3GObject.WORLD) object = new World();
			else throw new IOException("SceneSection.type=" + type);

			object.unmarshall(is,table);
			m_objects3D.add(object);
			table.add(object);
		}
	}

	/**
	 * Write object references.
	 * 
	 * @param os The output stream to write to.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * writing the data.
	 */
	protected void writeObjects(M3GOutputStream os, ArrayList table) throws IOException
	{
		for (int i = 0; i < table.size(); i++)
		{
			Object obj = table.get(i);

			if (obj instanceof Object3D)
			{
				Object3D o3d = (Object3D)obj;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				o3d.marshall(new M3GOutputStream(baos),table);
				os.writeByte(o3d.getObjectType());
				os.writeUInt32(baos.size());
				baos.writeTo(os);
			}
		}
	}

	//////////////////////// DEBUG ////////////////////////
	
	// TODO: readObjectsDebug.
	protected void readObjects2(M3GInputStream is, ArrayList table) throws IOException
	{
		String names[] = { "HEADER", "ANIMATION_CONTROLLER", "ANIMATION_TRACK",
				"APPEARANCE", "BACKGROUND", "CAMERA", "COMPOSITING_MODE", "FOG",
				"POLYGON_MODE", "GROUP", "IMAGE2D", "TRIANGLE_STRIP_ARRAY",
				"LIGHT", "MATERIAL", "MESH", "MORPHING_MESH", "SKINNED_MESH",
				"TEXTURE2D", "SPRITE3D", "KEYFRAME_SEQUENCE", "VERTEX_ARRAY",
				"VERTEX_BUFFER", "WORLD" };
		ArrayList datas = new ArrayList();

		while (is.available() > 0)
		{
			byte type = (byte)is.readByte();
			long length = is.readUInt32();
			byte data[] = new byte[(int)length];
			is.read(data);
			Object3D object = null;
			if (type == M3GObject.ANIMATION_CONTROLLER) object = new AnimationController();
			else if (type == M3GObject.ANIMATION_TRACK) object = new AnimationTrack();
			else if (type == M3GObject.APPEARANCE) object = new Appearance();
			else if (type == M3GObject.BACKGROUND) object = new Background();
			else if (type == M3GObject.CAMERA) object = new Camera();
			else if (type == M3GObject.COMPOSITING_MODE) object = new CompositingMode();
			else if (type == M3GObject.FOG) object = new Fog();
			else if (type == M3GObject.POLYGON_MODE) object = new PolygonMode();
			else if (type == M3GObject.GROUP) object = new Group();
			else if (type == M3GObject.IMAGE2D) object = new Image2D();
			else if (type == M3GObject.TRIANGLE_STRIP_ARRAY) object = new TriangleStripArray();
			else if (type == M3GObject.LIGHT) object = new Light();
			else if (type == M3GObject.MATERIAL) object = new Material();
			else if (type == M3GObject.MESH) object = new Mesh();
			else if (type == M3GObject.MORPHING_MESH) object = null;
			else if (type == M3GObject.SKINNED_MESH) object = new SkinnedMesh();
			else if (type == M3GObject.TEXTURE2D) object = new Texture2D();
			else if (type == M3GObject.SPRITE3D) object = new Sprite3D();
			else if (type == M3GObject.KEYFRAME_SEQUENCE) object = new KeyframeSequence();
			else if (type == M3GObject.VERTEX_ARRAY) object = new VertexArray();
			else if (type == M3GObject.VERTEX_BUFFER) object = new VertexBuffer();
			else if (type == M3GObject.WORLD) object = new World();
			else throw new IOException("SceneSection.type=" + type);

			object.unmarshall(new M3GInputStream(new ByteArrayInputStream(data)), table);
			m_objects3D.add(object);
			table.add(object);

			datas.add(object);
			datas.add(data);
		}

		for (int i = 0; i < datas.size(); i += 2)
		{
			Object3D object = (Object3D)datas.get(i);
			byte data[] = (byte[])datas.get(i+1);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			object.marshall(new M3GOutputStream(baos), table);
			byte data2[] = baos.toByteArray();

			System.out.println(names[object.getObjectType()] + " (" +
				data.length + "/" + data2.length + "/" + Arrays.equals(data,data2) + ")");
			if (! Arrays.equals(data,data2))
			{
				System.out.println("");
				System.out.println("--");
				for (int u = 0; u < data.length; u++)
					System.out.print(Integer.toHexString(data[u] & 0xff) + ",");
				System.out.println("\n--\n");
				for (int u = 0; u < data2.length; u++)
					System.out.print(Integer.toHexString(data2[u] & 0xff) + ",");
				System.out.println("");
				System.out.println("--");
				System.out.println("");
			}
		}
	}
}