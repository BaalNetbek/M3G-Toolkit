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
import java.util.zip.*;
import java.nio.*;

// Import JOGL classes.
import javax.media.opengl.GL;
import com.sun.opengl.util.*; // BufferUtils

public class VertexArray extends Object3D
{
	// The size of each component in the array.
	int m_componentSize;
	// The number of components.
	int m_componentCount;
	// The number of vertices in the array.
	int m_vertexCount;
	// The encoding mode.
	int m_encoding;
	// The actual vertex data.
	private Buffer m_buffer;
	private FloatBuffer m_floatBuffer;

    ////////// Methods part of M3G Specification //////////
	
	public VertexArray(int numVertices, int numComponents, int componentSize)
	{
		if (numVertices < 1 || numVertices > 65535 || 
			numComponents < 2 || numComponents > 4 ||
			componentSize < 1 || componentSize> 2 )
			throw new IllegalArgumentException("VertexArray: any of the parameters are outside of their allowed ranges");
		
		m_vertexCount = numVertices;
		m_componentCount = numComponents;
		m_componentSize = componentSize;
		
		int numElements = m_vertexCount * m_componentCount;
		if (componentSize == 1) m_buffer = BufferUtil.newByteBuffer(numElements);
		else m_buffer = BufferUtil.newShortBuffer(numElements);
		
		m_floatBuffer = BufferUtil.newFloatBuffer(numElements);
	}

	public void set(int firstVertex, int numVertices, short values[])
	{
		if (! (m_buffer instanceof ShortBuffer))
			throw new IllegalStateException("VertexArray: this is not a 16-bit VertexArray");
		if (numVertices < 0)
			throw new IllegalArgumentException("VertexArray: numVertices < 0");
		if (values.length < numVertices * m_componentCount)
			throw new IllegalArgumentException("VertexArray: values.length <  numVertices * numComponents");

		int numElements = m_vertexCount * m_componentCount;
		ShortBuffer shortBuffer = (ShortBuffer)m_buffer;
		shortBuffer.position(firstVertex);
		shortBuffer.put(values, 0, numElements);

		m_floatBuffer.position(firstVertex);
		for (int i = 0; i < numElements; i++)
			m_floatBuffer.put((float)values[i]);
		
		m_buffer.rewind();
		m_floatBuffer.rewind();
	}

	public void set(int firstVertex, int numVertices, byte values[])
	{
		if (! (m_buffer instanceof ByteBuffer))
			throw new IllegalStateException("VertexArray: this is not a 8-bit VertexArray");
		if (numVertices < 0)
			throw new IllegalArgumentException("VertexArray: numVertices < 0");
		if (values.length < numVertices * m_componentCount)
			throw new IllegalArgumentException("VertexArray: values.length <  numVertices * numComponents");

		int numElements = m_vertexCount * m_componentCount;
		ByteBuffer byteBuffer = (ByteBuffer)m_buffer;
		byteBuffer.position(firstVertex);
		byteBuffer.put(values, 0, numElements);

		m_floatBuffer.position(firstVertex);
		for (int i = 0; i < numElements; i++)
			m_floatBuffer.put((float)values[i]);

		m_buffer.rewind();
		m_floatBuffer.rewind();
	}

	public void get(int firstVertex, int numVertices, short[] values)
	{
		int numElements = numVertices * m_componentCount;
		checkShortInput(firstVertex, numVertices, numElements, values);
		
		ShortBuffer shortBuffer = (ShortBuffer)m_buffer;
		shortBuffer.position(firstVertex);
		shortBuffer.get(values, 0, numElements);
	}

	public void get(int firstVertex, int numVertices, byte[] values)
	{
		int numElements = numVertices * m_componentCount;
		checkByteInput(firstVertex, numVertices, numElements, values);

		ByteBuffer byteBuffer = (ByteBuffer)m_buffer;
		byteBuffer.position(firstVertex);
		byteBuffer.get(values, 0, numElements);
	}

	private void checkShortInput(int firstVertex, int numVertices, int numElements, short[] values)
	{
		if (values == null)
			throw new NullPointerException("VertexArray: values can not be null");
		if (m_componentSize != 2)
			throw new IllegalStateException("VertexArray: vertexarray created as short array. can not get byte values");
		checkInput(firstVertex, numVertices, numElements, values.length);
	}

	private void checkByteInput(int firstVertex, int numVertices, int numElements, byte[] values)
	{
		if (values == null)
			throw new NullPointerException("VertexArray: values can not be null");
		if (m_componentSize != 1)
			throw new IllegalStateException("VertexArray: vertexarray created as short array. can not set byte values");
		checkInput(firstVertex, numVertices, numElements, values.length);
	}
	
	private void checkInput(int firstVertex, int numVertices, int numElements, int arrayLength)
	{
		if (numVertices < 0)
			throw new IllegalArgumentException("VertexArray: numVertices must be > 0");
		if (arrayLength < numElements)
			throw new IllegalArgumentException("VertexArray: number of elements i values does not match numVertices");
		if (firstVertex < 0 || firstVertex + numVertices > this.m_vertexCount)
			throw new IndexOutOfBoundsException("VertexArray: index out of bounds");
	}

	public int getComponentCount()
	{
		return m_componentCount;
	}
	
	public int getVertexCount()
	{
		return m_vertexCount;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}

    ////////// Methods not part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 */
	VertexArray()
	{
		// Do nothing for now.
	}

	public int getComponentSize()
	{
		return m_componentSize;
	}

	public int getEncoding()
	{
		return m_encoding;
	}

	public int getObjectType()
	{
		return VERTEX_ARRAY;
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

		// Read componentSize
		m_componentSize = is.readByte();
		if (m_componentSize < 1 || m_componentSize > 2)
			throw new IOException("VertexArray:componentSize = " + m_componentSize);
		// Read componentCount
		m_componentCount = is.readByte();
		if (m_componentCount < 2 || m_componentCount > 4)
			throw new IOException("VertexArray:componentCount = " + m_componentCount);
		// Read encoding
		int encoding = is.readByte();
		if (encoding < 0 || encoding > 1)
			throw new IOException("VertexArray:encoding = " + encoding);
		else m_encoding = encoding;
		// Read vertexCount
		m_vertexCount = is.readUInt16();

		int numElements = m_vertexCount * m_componentCount;
		if (m_componentSize == 1) m_buffer = BufferUtil.newByteBuffer(numElements);
		else m_buffer = BufferUtil.newShortBuffer(numElements);
		
		m_floatBuffer = BufferUtil.newFloatBuffer(numElements);

		if (m_componentSize == 1)
		{
			byte components[] = new byte[numElements];
			if (encoding == 0)
			{
				for (int i = 0, index = 0; i < m_vertexCount; i++)
				{
					for (int c = 0; c < m_componentCount; c++)
					{
						short value = (short)is.readByte();
						components[index++] = (byte)(value & 0xff);
					}
				}
				this.set(0, m_vertexCount, components);
			}
			else if (encoding == 1)
			{
				short prev[] = new short[m_componentCount];
				for (int i = 0, index = 0; i < m_vertexCount; i++)
				{
					for (int c = 0; c < m_componentCount; c++)
					{
						short value = (short)(is.readByte() + prev[c]);
						components[index++] = (byte)(value & 0xff);
						prev[c] = value;
					}
				}
				this.set(0, m_vertexCount, components);
			}
		}
		else if (m_componentSize == 2)
		{
			short components[] = new short[numElements];
			if (encoding == 0)
			{
				for (int i = 0, index = 0; i < m_vertexCount; i++)
				{
					for (int c = 0; c < m_componentCount; c++)
					{
				        int value = (int)is.readInt16();
				        components[index++] = (short)(value & 0xffff);
					}
				}
				this.set(0, m_vertexCount, components);
			}
			else if (encoding == 1)
			{
				int prev[] = new int[m_componentCount];
				for (int i = 0, index = 0; i < m_vertexCount; i++)
				{
					for (int c = 0; c < m_componentCount; c++)
					{
						int value = (int)(is.readInt16() + prev[c]);
						components[index++] = (short)(value & 0xffff);
						prev[c] = value;
					}
				}
				this.set(0, m_vertexCount, components);
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

		if (m_componentSize == 2)
		{
			ShortBuffer buffer = (ShortBuffer)m_buffer;
			int numElements = m_vertexCount * m_componentCount;
			short[] components = new short[numElements];
			buffer.get(components);
			
			boolean toByte = true;
			for (int i = 0; i < components.length && toByte; i++)
			{
				short value = components[i];
				if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
					toByte = false;
			}

			if (toByte)
			{
				byte c[] = new byte[components.length];
				for (int i = 0; i < components.length; i++)
					c[i] = (byte)components[i];
				write(os, m_vertexCount, m_componentCount, 1, c);
			}
			else write(os, m_vertexCount, m_componentCount, m_componentSize, components);
		}
		else if (m_componentSize == 1)
		{
			ByteBuffer buffer = (ByteBuffer)m_buffer;
			int numElements = m_vertexCount * m_componentCount;
			byte[] components = new byte[numElements];
			buffer.get(components);
			write(os, m_vertexCount, m_componentCount, m_componentSize, components);
		}
		
		// Rewind the buffer in case we need to marshall it again.
		m_buffer.rewind();
	}

	private void write(M3GOutputStream os, int vertexCount, int componentCount, int componentSize, Object components) throws IOException
	{
		// Encode.
		byte encoding0[] = encode(0, vertexCount, componentCount, componentSize, components);
		byte encoding1[] = encode(1, vertexCount, componentCount, componentSize, components);

		int length0 = getCompressedLength(encoding0);
		int length1 = getCompressedLength(encoding1);

		os.writeByte(componentSize);
		os.writeByte(componentCount);
		os.writeByte(length0 <= length1 ? 0 : 1);
		os.writeUInt16(vertexCount);
		os.write(length0 <= length1 ? encoding0 : encoding1);
	}

	private byte[] encode(int encoding, int vertexCount, int componentCount, int componentSize, Object _components) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		M3GOutputStream os = new M3GOutputStream(baos);

		if (componentSize == 1)
		{
			byte components[] = (byte[])_components;
			//short components[] = (short[])_components;
			if (encoding == 0)
			{
				for (int i = 0, index = 0; i < vertexCount; i++)
					for (int c = 0; c < componentCount; c++)
						os.writeByte(components[index++]);
						//os.writeByte(components[index++] & 0xff);
			}
			else if (encoding == 1)
			{
				byte prev[] = new byte[componentCount];
				//short prev[] = new short[componentCount];
				for (int i = 0, index = 0; i < vertexCount; i++)
				{
					for (int c = 0; c < componentCount; c++)
					{
						byte value = components[index++];
						//short value = components[index++];
						os.writeByte(value - prev[c]);
						//os.writeByte((value - prev[c]) & 0xff);
						prev[c] = value;
					}
				}
			}
		}
		else if (componentSize == 2)
		{
			short components[] = (short[])_components;
			//int components[] = (int[])_components;
			if (encoding == 0)
			{
				for (int i = 0, index = 0; i < vertexCount; i++)
					for (int c = 0; c < componentCount; c++)
						os.writeInt16(components[index++]);
						//os.writeInt16(components[index++] & 0xffff);
			}
			else if (encoding == 1)
			{
				short prev[] = new short[componentCount];
				//int prev[] = new int[componentCount];
				for (int i = 0, index = 0; i < vertexCount; i++)
				{
					for (int c = 0; c < componentCount;c++)
					{
						short value = components[index++];
						//int value = components[index++];
						os.writeInt16(value - prev[c]);
						//os.writeInt16((value - prev[c]) & 0xffff);
						prev[c] = value;
					}
				}
			}
		}

		return baos.toByteArray();
	}

	private int getCompressedLength(byte data[])
	{
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION,false);
		deflater.setInput(data);
		deflater.finish();
		return deflater.deflate(new byte[data.length << 1]);
	}
	
	int getComponentTypeGL()
	{
		if (m_componentSize == 1)
			return GL.GL_BYTE;
		else
			return GL.GL_SHORT; 
	}
	
	/**
	 * Get vertex buffer.
	 * 
	 * @return Either a <code>ByteBuffer</code> or a <code>ShortBuffer</code>
	 * is returned, depending on the commponent size.
	 */ 
	public Buffer getBuffer()
	{
		return m_buffer;
	}

	/**
	 * Get vertex buffer.
	 * 
	 * @return A <code>FloatBuffer</code> is returned.
	 */
	public FloatBuffer getFloatBuffer()
	{
		return m_floatBuffer;
	}
}
