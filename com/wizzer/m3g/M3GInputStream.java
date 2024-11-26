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
import java.util.zip.*;

/**
 * The <code>M3GInputStream</code> is used to unmarshall binary data
 * from an input stream as described by the Mobile 3D Graphics Specification.
 * 
 * @author Mark Millard
 */
public class M3GInputStream extends FilterInputStream
{
	// The Adler-32 checksum of the input stream.
	private Adler32 m_adler32;
	// Flag indicating whether to block the checksum computation.
	private boolean m_blockAdler32;

	/**
	 * A constructor initializing the input stream.
	 * 
	 * @param in The input stream.
	 */
	public M3GInputStream(InputStream in)
	{
		super(in);
		m_adler32 = new Adler32();
	}

	/**
	 * Reads the next byte of data from this input stream.
	 * <p>
	 * The value byte is returned as an int in the range 0 to 255. If no byte
	 * is available because the end of the stream has been reached, the value -1 is returned.
	 * </p><p>
	 * This method blocks until input data is available, the end of the stream is detected,
	 * or an exception is thrown.
	 * </p>
	 * 
	 * @return The byte is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the next byte.
	 */
	public int read() throws IOException
	{
		int b = super.read();
		if (! m_blockAdler32)
			m_adler32.update(b);
		return b;
	}

	/**
	 * Reads up to <i>len</i> bytes of data from this input stream into an array of bytes.
	 * This method blocks until some input is available.
	 * 
	 * @param b The buffer into which the data is read.
	 * @param off The start offset of the data.
	 * @param len The maximum number of bytes to read.
	 * 
	 * @return The total number of bytes read into the buffer, or -1 if there
	 * is no more data because the end of the stream has been reached.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int read(byte b[], int off, int len) throws IOException
	{
		m_blockAdler32 = true;
		len = super.read(b,off,len);
		m_adler32.update(b,off,len);
		m_blockAdler32 = false;
		return len;
	}

	/**
	 * Read the next byte in the stream.
	 * 
	 * @return The byte is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int readByte() throws IOException
	{
		return (read() & 0xff);
	}

	/**
	 * Read a signed 2-byte integer value.
	 * 
	 * @return The short integer is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int readInt16() throws IOException
	{
		int l = readByte();
		int h = readByte();
		return (short)((h << 8) | (l & 0xff));
	}

	/**
	 * Read an unsigned 2-byte integer value.
	 * 
	 * @return The short integer is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int readUInt16() throws IOException
	{
		return (readInt16() & 0xffff);
	}

	/**
	 * Read a signed 4-byte integer value.
	 * 
	 * @return The integer is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public long readInt32() throws IOException
	{
		int l = readUInt16();
		int h = readUInt16();
		return (h << 16) | (l & 0xffff);
	}

	/**
	 * Read an unsigned 4-byte integer value.
	 * 
	 * @return The integer is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public long readUInt32() throws IOException
	{
		return (readInt32() & 0xffffffff);
	}

	/**
	 * Read a 32-bit floating-point value.
	 * 
	 * @return A floating-point value is returned.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public float readFloat32() throws IOException
	{
		return Float.intBitsToFloat((int)(readInt32() & 0xffffffff));
	}

	/**
	 * Read a string value.
	 * 
	 * @return A UTF-8 <code>String</code> is returned.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public String readString() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = -1; (i = read()) > 0;)
			baos.write(i);
		return new String(baos.toByteArray(),"UTF-8");
	}

	/**
	 * Read a boolean value.
	 * 
	 * @return <b>true</b> is returned if the read value is true. Otherwise,
	 * <b>false</b> will be returned.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public boolean readBoolean() throws IOException
	{
		return (readByte() != 0);
	}

	/**
	 * Read a three element vector.
	 * 
	 * @return An array of three floating-point values is returned.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public float[] readVector3D() throws IOException
	{
		float v[] = new float[3];
		for (int i = 0; i < 3; i++)
			v[i] = readFloat32();
		return v;
	}

	/**
	 * Read a 16 element matrix.
	 * 
	 * @return A <code>Transform</code> is returned containing the matrix data.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public Transform readMatrix() throws IOException
	{
		float m[] = new float[16];
		for (int i = 0; i < 16; i++)
			m[i] = readFloat32();
		Transform t = new Transform();
		t.set(m);
		return t;
	}

	/**
	 * Read a RGB color value.
	 * 
	 * @return The color value is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int readColorRGB() throws IOException
	{
		return (0xff000000 | (readByte() << 16) | (readByte() << 8) | readByte());
	}

	/**
	 * Read a RGB color value with alpha component.
	 * 
	 * @return The color value is returned as an <code>int</code>.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public int readColorRGBA() throws IOException
	{
		return ((readByte() << 16) | (readByte() << 8) | readByte() | (readByte() << 24));
	}

	/**
	 * Read the index of a previously encountered object in the stream.
	 * 
	 * @return The object index is returned.
	 * 
	 * @throws IOException This exception is thrown if an error occurs while
	 * attempting to read the data.
	 */
	public long readObjectIndex() throws IOException
	{
		return readUInt32();
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