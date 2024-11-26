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
import java.nio.IntBuffer;
import java.util.*;
import java.util.zip.*;

// Import JOGL classes.
import com.sun.opengl.util.*;

// Import Wizzer Works M3G Toolkit classes.
import com.wizzer.m3g.nvtristrip.*;

/**
 * The TriangleStrip Array defines an array of <i>triangle strips</i>.
 * 
 * @author Mark Millard
 */
public class TriangleStripArray extends IndexBuffer
{
	// The index of the initial vertex of the first strip.
	private int m_firstIndex;
	// An array of indices.
	private int m_indices[];
	// An array of per-strip vertex counts.
	private int m_stripLengths[];

    ////////// Methods part of M3G Specification //////////

	public TriangleStripArray(int firstIndex, int stripLengths[])
	{
		int sum = checkInput(stripLengths);
		if (firstIndex < 0)
			throw new IndexOutOfBoundsException("TriangleStripArray: firstIndex < 0");
		if (firstIndex + sum > 65535)
			throw new IndexOutOfBoundsException("TriangleStripArray: firstIndex + sum(stripLengths) > 65535");

		m_firstIndex = firstIndex;
		m_stripLengths = stripLengths;
		
		// Fill IndexBuffer.
		fillIndexBuffer(sum, firstIndex, stripLengths);
	}

	public TriangleStripArray(int indices[], int stripLengths[])
	{
		if (indices == null)
			throw new NullPointerException("TriangleStripArray: indices is null");
		int sum = checkInput(stripLengths);		
		if (indices.length < sum)
			throw new IndexOutOfBoundsException("TriangleStripArray: indices.length <  sum(stripLengths)");
		for (int i = 0; i < indices.length; i++)
		{
			if (indices[i] < 0)
				throw new IllegalArgumentException("TriangleStripArray: any element in indices is negative");
			else if (indices[i] > 65535)
				throw new IllegalArgumentException("TriangleStripArray: any element in indices is greater than 65535");
		}

		m_indices = indices;
		m_stripLengths = stripLengths;

		// Fill index buffer.
		fillIndexBuffer(sum, indices, stripLengths);
	}
	
	public void getIndices(int[] indices)
	{
		if (indices == null)
			throw new NullPointerException("TriangleStripArray: indices is null");
		if (indices.length < getIndexCount())
			throw new IllegalArgumentException("TriangleStripArray: indices length not big enough");
		
		// XXX - Fill indices with triangle data.
		//System.arraycopy(m_indices, 0, indices, 0, m_indices.length);
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}

    ////////// Methods not part of M3G Specification //////////
	
	TriangleStripArray() {}

	public int getObjectType()
	{
		return TRIANGLE_STRIP_ARRAY;
	}
	
	// Remember state of encoding Byte.
	private int m_encoding;
	
	public int getEncoding()
	{
		return m_encoding;
	}
	
	public int getStartIndex()
	{
		return m_firstIndex;
	}
	
	public int[] getIndices()
	{
		return m_indices;
	}
	
	public int[] getStripLengths()
	{
		return m_stripLengths;
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

		int encoding = is.readByte();
		if ((encoding & 0x80) == 0)
		{
			if (encoding == 0) m_firstIndex = (int)is.readUInt32();
			else if (encoding == 1) m_firstIndex = is.readByte();
			else if (encoding == 2) m_firstIndex = is.readUInt16();
			else throw new IOException("TriangleStripArray.encoding = " + encoding);
		}
		else
		{
			m_indices = new int[(int)is.readUInt32()];
			for (int i = 0; i < m_indices.length;i++)
			{
				if (encoding == 128) m_indices[i] = (int)is.readUInt32();
				else if (encoding == 129) m_indices[i] = is.readByte();
				else if (encoding == 130) m_indices[i] = is.readUInt16();
				else throw new IOException("TriangleStripArray.encoding = " + encoding);
			}
		}
		int numStripLengths = (int)is.readUInt32();
		int[] stripLengths = new int[numStripLengths];
		for (int i = 0; i < numStripLengths; i++)
			stripLengths[i] = (int)is.readUInt32();
		int sum = checkInput(stripLengths);
		if (m_indices == null)
			fillIndexBuffer(sum, m_firstIndex, stripLengths);
        else
			fillIndexBuffer(sum, m_indices, stripLengths);
		
		// Cache encoding value for dumping state.
		m_encoding = encoding;
		m_stripLengths = stripLengths;
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

		if (m_indices == null)
		{
			// Write indices.
			if (m_firstIndex < 256)
			{
				os.writeByte(1);  // encoding
				os.writeByte(m_firstIndex);
			}
			else if (m_firstIndex < 65536)
			{
				os.writeByte(2); // encoding
				os.writeUInt16(m_firstIndex);
			}
			else
			{
				os.writeByte(0); // encoding
				os.writeUInt32(m_firstIndex);
			}
			// Write stripLengths.
			
			os.writeUInt32(m_stripLengths.length);
			for (int i = 0; i < m_stripLengths.length;i++)
				os.writeUInt32(m_stripLengths[i]);
		}
		else
		{
			byte data[] = encode(m_indices, m_stripLengths);
			int compressedLength = getCompressedLength(data);

			int faces[] = getRawFaces();
			// TriangleStripArray.
			for (int i = 0; i < 2; i++)
			{
				for (int j = 1; j <= 10; j++)
				{
					int stripData[][] = strip(faces, j, i == 0);
					byte data2[] = encode(stripData[0], stripData[1]);
					int compressedLength2 = getCompressedLength(data2);
					if (compressedLength2 < compressedLength)
					{
						data = data2;
						compressedLength = compressedLength2;
					}
				}
			}

			os.write(data);
		}
	}

	private byte[] encode(int indices[], int stripLengths[]) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		M3GOutputStream os = new M3GOutputStream(baos);
		// Write indices.
		int max=0;
		for (int i = 0; i < indices.length; i++)
			max = Math.max(indices[i], max);
		if (max < 256)
		{
			os.writeByte(129);
			os.writeUInt32(indices.length);
			for (int i = 0; i < indices.length; i++)
				os.writeByte(indices[i]);
		}
		else if (max < 65536)
		{
			os.writeByte(130);
			os.writeUInt32(indices.length);
			for (int i = 0; i < indices.length; i++)
				os.writeUInt16(indices[i]);
		}
		else
		{
			os.writeByte(128);
			os.writeUInt32(indices.length);
			for (int i = 0; i < indices.length; i++)
				os.writeUInt32(indices[i]);
		}
		// Write stripLengths.
		os.writeUInt32(stripLengths.length);
		for (int i = 0; i < stripLengths.length; i++)
			os.writeUInt32(stripLengths[i]);

		return baos.toByteArray();
	}

	private int getCompressedLength(byte data[])
	{
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION,false);
		deflater.setInput(data);
		deflater.finish();
		return deflater.deflate(new byte[data.length << 1]);
	}

	private int[] getRawFaces()
	{
		// Calculate the number of faces.
		int nfaces = 0;
		for (int i = 0; i < m_stripLengths.length; i++)
			nfaces += m_stripLengths[i] - 2;
		int faces[] = new int[nfaces * 3];
		nfaces = 0;
		// Record the different strips.
		for (int i = 0, j = 0; i < m_stripLengths.length; i++, j += 2)
		{
			for (int k = 0; k < m_stripLengths[i] - 2; k++, j++)
			{
				int v0 = m_indices[j];
				int v1 = m_indices[j+1];
				int v2 = m_indices[j+2];
				if (! isDegenerate(v0, v1, v2))
				{
					if ((k&1) == 0)
					{
						faces[nfaces++] = v0;
						faces[nfaces++] = v1;
						faces[nfaces++] = v2;
					}
					else
					{
						faces[nfaces++] = v2;
						faces[nfaces++] = v1;
						faces[nfaces++] = v0;
					}
				}
			}
		}

		int temp[] = new int[nfaces];
		System.arraycopy(faces,0,temp,0,nfaces);
		return temp;
	}

	private boolean isDegenerate(int v0,int v1,int v2)
	{
		if (v0 == v1) return true;
		else if (v0 == v2) return true;
		else if (v1 == v2) return true;
		else return false;
	}

	private int[][] strip(int faces[],int minStripSize,boolean stitchStrips)
	{
		NvTriStrip strip = new NvTriStrip();
		strip.setStitchStrips(stitchStrips);
		strip.setMinStripSize(minStripSize);
		PrimitiveGroup groups[] = strip.generateStrips(faces);

		// Calculate the total number of indices for the length of each strip.
		int nindices = 0, nlengths = 0;
		for (int i = 0; i < groups.length; i++)
		{
			PrimitiveGroup group = groups[i];
			if (group.m_numIndices > 0)
			{
				if (group.m_type == PrimitiveGroup.PT_LIST)
				{
					for (int j = 0; j < group.m_numIndices;)
					{
						if (! isDegenerate(group.m_indices[j++] ,group.m_indices[j++], group.m_indices[j++]))
						{
							nindices += 3;
							nlengths++;
						}
					}
				}
				else if (group.m_type == PrimitiveGroup.PT_STRIP)
				{
					nindices += group.m_numIndices;
					nlengths++;
				}
			}
		}

		int data[][] = new int[2][];
		data[0] = new int[nindices];
		data[1] = new int[nlengths];
		for (int i = 0, index1 = 0, index2 = 0; i < groups.length; i++)
		{
			PrimitiveGroup group = groups[i];
			if (group.m_numIndices > 0)
			{
				if (group.m_type == PrimitiveGroup.PT_LIST)
				{
					for (int j = 0; j < group.m_numIndices;)
					{
						int a = groups[i].m_indices[j++];
						int b = groups[i].m_indices[j++];
						int c = groups[i].m_indices[j++];
						if (! isDegenerate(a,b,c))
						{
							data[0][index1++] = a;
							data[0][index1++] = b;
							data[0][index1++] = c;
							data[1][index2++] = 3;
						}
					}
				}
				else if (group.m_type == PrimitiveGroup.PT_STRIP)
				{
					System.arraycopy(group.m_indices, 0, data[0],index1, group.m_numIndices);
					data[1][index2++] = group.m_numIndices;
					index1 += group.m_numIndices;
				}
			}
		}

		return data;
	}
	
	// Validate the strip lengths, returning the sum, total length.
	private int checkInput(int[] stripLengths)
	{
		int sum = 0;
		if (stripLengths == null)
			throw new NullPointerException("TriangleStripArray: stripLegths can not be null");
		int l = stripLengths.length;
		if (l == 0) 
			throw new IllegalArgumentException("TriangleStripArray: stripLenghts can not be empty");
		for (int i = 0; i < l; i++)
		{
			if (stripLengths[i] < 3)
				throw new IllegalArgumentException("TriangleStripArray: stripLengths must not contain elemets less than 3");
			
			sum += stripLengths[i];
		}
		return sum;
	}
	
	private void fillIndexBuffer(int sum, int firstIndex, int[] stripLengths)
	{
		// Fill IndexBuffer.
		allocate(sum + (stripLengths.length - 1) * 3);
		int index = firstIndex;
		for (int i = 0; i < stripLengths.length; i++)
		{
			if (i != 0)
			{
				// If this is not the first strip, we need to connect the strips.
				m_buffer.put(index - 1);
				m_buffer.put(index);
				// May need extra index for correct winding.
				if ((m_buffer.position() % 2) == 1)
					m_buffer.put(index);
			}
			for (int s = 0; s < stripLengths[i]; ++s)
			{
				m_buffer.put(index++);
			}
		}
		
		// Reset position and set limit.
		m_buffer.flip();
	}
	
	private void fillIndexBuffer(int sum, int[] indices, int[] stripLengths)
	{
		// Fill index buffer.
		allocate(sum + (stripLengths.length - 1) * 3);
		int index = 0;
		for (int i = 0; i < stripLengths.length; i++)
		{
			if (i != 0)
			{
				// If this is not the first strip, we need to connect the strips.
				m_buffer.put(indices[index - 1]);
				m_buffer.put(indices[index]);
				// May need extra index for correct winding.
				if ((m_buffer.position() % 2) == 1)
					m_buffer.put(indices[index]);
			}
			for (int s = 0; s < stripLengths[i]; ++s)
			{
				m_buffer.put(indices[index++]);
			}
		}
		// Reset position and set limit.
		m_buffer.flip();
	}
}