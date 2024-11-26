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
 * The <code>HeaderSection</code> is a utility for reading/writing
 * the Header Section from/to a M3G file. It is not a runtime
 * construct called out by the M3G Specification.
 * <p>
 * This class is used to manage the M3G file header section.
 * </p>
 * 
 * @author Mark Millard
 */
public class HeaderSection extends Section
{
	// The associated header.
	private HeaderObject m_headerObject;

	public HeaderSection()
	{
		m_headerObject = new HeaderObject();
		setCompressionScheme(UNCOMPRESSED);
	}

	public HeaderObject getHeaderObject()
	{
		return m_headerObject;
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
		byte type = (byte)is.readByte();
		if (type != M3GObject.HEADER)
			throw new IOException("HeaderSection:type = " + type);
		long length = is.readUInt32();
		m_headerObject.unmarshall(is,table);
		table.add(m_headerObject);
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
		os.writeByte(M3GObject.HEADER);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		m_headerObject.marshall(new M3GOutputStream(baos),table);
		os.writeUInt32(baos.size());
		baos.writeTo(os);
	}
}
