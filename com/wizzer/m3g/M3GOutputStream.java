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

// Import standard Java clases.
import java.io.*;
import java.util.zip.*;

/**
 * The <code>M3GOutputStream</code> is used to marshall binary data
 * to an output stream as described by the Mobile 3D Graphics Specification.
 * 
 * @author Mark Millard
 */
public class M3GOutputStream extends FilterOutputStream
{
	// The Adler-32 checksum of the input stream.
	private Adler32 m_adler32;
	// Flag indicating whether to block the checksum computation.
	private boolean m_blockAdler32;

	/**
	 * A constructor initializing the output stream.
	 * 
	 * @param os The output stream.
	 */
	public M3GOutputStream(OutputStream os)
	{
		super(os);
		m_adler32 = new Adler32();
	}

	/**
	 * Writes the specified byte of data to this output stream.
	 * 
	 * @param b The byte to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the byte.
	 */
	public void write(int b) throws IOException
	{
		super.write(b);
		if (! m_blockAdler32)
			m_adler32.update(b);
	}

	/**
	 * Writes the specified bytes to this output stream.
	 * 
	 * @param b The array of bytes to write.
	 * @param off The start offset of the data.
	 * @param len The maximum number of bytes to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void write(byte b[], int off, int len) throws IOException
	{
		m_blockAdler32 = true;
		super.write(b, off, len);
		m_adler32.update(b, off, len);
		m_blockAdler32 = false;
	}

	/**
	 * Write the specified byte value.
	 * 
	 * @param b The byte to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeByte(byte b) throws IOException
	{
		write(b);
	}

	/**
	 * Write the specified byte value.
	 * 
	 * @param b The byte to write. The upper 3 bytes are truncated.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeByte(int b) throws IOException
	{
		writeByte((byte)b);
	}

	/**
	 * Write the signed short value.
	 * 
	 * @param s The 2-byte value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeInt16(short s) throws IOException
	{
		writeByte((byte)(s & 0xff));
		writeByte((byte)((s >> 8) & 0xff));
	}

	/**
	 * Write the signed short value.
	 * 
	 * @param s The 2-byte value to write. The upper 2 bytes are truncated.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeInt16(int i) throws IOException
	{
		writeInt16((short)i);
	}

	/**
	 * Write the unsigned short value.
	 * 
	 * @param s The 2-byte value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeUInt16(short s) throws IOException
	{
		writeInt16(s);
	}

	/**
	 * Write the unsigned short value.
	 * 
	 * @param i The 2-byte value to write. The upper 2 bytes are truncated.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeUInt16(int i) throws IOException
	{
		writeInt16((short)i);
	}

	/**
	 * Write the signed integer value.
	 * 
	 * @param s The 4-byte value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeInt32(int i) throws IOException
	{
		writeInt16((short)(i & 0xffff));
		writeInt16((short)((i >> 16) & 0xffff));
	}

	/**
	 * Write the unsigned integer value.
	 * 
	 * @param i The 4-byte value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeUInt32(long i) throws IOException
	{
		writeInt32((int)(i & 0xffffffff));
	}

	/**
	 * Write the 32-bit floating-point value.
	 * 
	 * @param f The floating-point value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeFloat32(float f) throws IOException
	{
		writeInt32(Float.floatToIntBits(f));
	}

	/**
	 * Write a UTF-8 string value.
	 * 
	 * @param s The string to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeString(String s) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter w = new OutputStreamWriter(baos,"UTF-8");
		w.write(s);
		w.flush();

		baos.writeTo(this);
		write(0);
	}

	/**
	 * Write a boolean value.
	 * 
	 * @param b The boolean value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeBoolean(boolean b) throws IOException
	{
		writeByte(b ? 1 : 0);
	}

	/**
	 * Write a 3 element vector.
	 * 
	 * @param v The elements of the vector to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	 public void writeVector3D(float v[]) throws IOException
	{
		for (int i = 0; i < 3; i++)
			writeFloat32(v[i]);
	}

	/**
	 * Write a 16 element matrix.
	 * 
	 * @param m The <code>Transform</code> matrix to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	 public void writeMatrix(Transform m) throws IOException
	{
		float t[] = new float[16];
		m.get(t);
		for (int i = 0; i < 16; i++)
			writeFloat32(t[i]);
	}

    /**
	 * Write a RGB color value.
	 * 
	 * @param rgb The color value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeColorRGB(int rgb) throws IOException
	{
		writeByte((byte)((rgb >> 16) & 0xff));
		writeByte((byte)((rgb >> 8) & 0xff));
		writeByte((byte)(rgb & 0xff));
	}

	/**
	 * Write a RGB color value with alpha component.
	 * 
	 * @param The color value to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeColorRGBA(int argb) throws IOException
	{
		writeColorRGB(argb);
		writeByte((byte)((argb >> 24) & 0xff));
	}

	/**
	 * Write the index of a previously encountered object in the stream.
	 * 
	 * @param index The object index to write.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to write the data.
	 */
	public void writeObjectIndex(long index) throws IOException
	{
		writeUInt32(index);
	}

	/**
	 * Reset the Adler-32 checksum.
	 */
	public void resetAdler32()
	{
		m_adler32.reset();
	}

	/**
	 * Get the value of the Adler-32 checksum.
	 * 
	 * @return The value is returned as a <code>long</code>.
	 */
	public long getAdler32Value()
	{
		return m_adler32.getValue();
	}
}