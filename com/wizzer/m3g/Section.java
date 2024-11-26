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
import java.util.*;

/**
 * The base class for all M3G file sections.
 * 
 * @author Mark Millard
 */
public abstract class Section
{
	public static final int UNCOMPRESSED    = 0;
	public static final int ZLIB            = 1;

	// The section's compression scheme.
	private int m_compressionScheme;
	// The total size of the section.
	private int m_totalSectionLength;
	// The uncompressed size of the section.
	private int m_uncompressedLength;
	// The checksum for the section.
	private int m_checksum;

	/**
	 * The default constructor.
	 */
	public Section()
	{
		m_compressionScheme = ZLIB;
	}

	/**
	 * Set the compression scheme.
	 * 
	 * @param compressionScheme The compression scheme for the section.
	 * Valid values include:
	 * <li>
	 * <ul>UNCOMPRESSED</ul>
	 * <ul>ZLIB</ul>
	 * </li>
	 */
	public void setCompressionScheme(int compressionScheme)
	{
		this.m_compressionScheme = compressionScheme;
	}

	/**
	 * Get the compression scheme.
	 * 
	 * @return  The compression scheme for the section is returned.
	 * Valid values include:
	 * <li>
	 * <ul>UNCOMPRESSED</ul>
	 * <ul>ZLIB</ul>
	 * </li>
	 */
	public int getCompressionScheme()
	{
		return m_compressionScheme;
	}

	public int getTotalSectionLength()
	{
		return m_totalSectionLength;
	}

	public int getUncompressedLength()
	{
		return m_uncompressedLength;
	}

	public int getChecksum()
	{
		return m_checksum;
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
		is.resetAdler32();
		m_compressionScheme = is.readByte();
		m_totalSectionLength = (int)is.readUInt32();
		m_uncompressedLength = (int)is.readUInt32();
		byte data[] = new byte[m_uncompressedLength];
		if (m_compressionScheme == ZLIB)
		{
			byte compressed[] = new byte[m_totalSectionLength - 13];
			is.read(compressed);
			try
			{
				Inflater inflater = new Inflater(false);
				inflater.setInput(compressed);
				inflater.inflate(data);
				inflater.end();
			}
			catch (Exception ex)
			{
				throw new IOException("Section:ZLIB");
			}
		}
		else if (m_compressionScheme == UNCOMPRESSED)
			is.read(data);

		int checksum_is = (int)is.getAdler32Value();
		m_checksum = (int)is.readUInt32();
		if (getChecksum() != checksum_is)
			throw new IOException("Section:checksum = " + m_checksum);

		readObjects(new M3GInputStream(new ByteArrayInputStream(data)), table);
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeObjects(new M3GOutputStream(baos),table);
		m_uncompressedLength = baos.size();
		byte data[] = baos.toByteArray();
		if (m_compressionScheme == ZLIB)
		{
			Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION,false);
			deflater.setInput(data);
			deflater.finish();
			byte compressed[] = new byte[data.length<<1];
			int length = deflater.deflate(compressed);
			data = new byte[length];
			System.arraycopy(compressed,0,data,0,length);
			deflater.end();
		}

		os.resetAdler32();
		os.writeByte(m_compressionScheme);
		os.writeUInt32(m_totalSectionLength=data.length+13);
		os.writeUInt32(m_uncompressedLength);
		os.write(data);
		os.writeUInt32(m_checksum=(int)os.getAdler32Value());
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
	protected abstract void readObjects(M3GInputStream is, ArrayList table) throws IOException;
	
	/**
	 * Write object references.
	 * 
	 * @param os The output stream to write to.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * writing the data.
	 */
	protected abstract void writeObjects(M3GOutputStream os, ArrayList table) throws IOException;
}