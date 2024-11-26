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
 * The <code>HeaderObject</code> is a utility for reading/writing
 * the Header from/to a M3G file. It is not a runtime
 * construct called out by the M3G Specification.
 * <p>
 * This class is used to encapsulate the contents of the M3G file header.
 * </p>
 * 
 * @author Mark Millard
 */
public class HeaderObject extends M3GObject
{
	private byte m_versionMajor;
	private byte m_versionMinor;
	private boolean m_hasExternalReferences;
	private long m_totalFileSize;
	private long m_approximateContentSize;
	private String m_authoringField;

	public HeaderObject()
	{
		m_versionMajor = 1;
		m_versionMinor = 0;
		m_authoringField = "";
	}

	public byte getVersionMajor()
	{
		return m_versionMajor;
	}

	public void setVersionMajor(byte versionMajor)
	{
		m_versionMajor = versionMajor;
	}

	public byte getVersionMinor()
	{
		return m_versionMinor;
	}

	public void setVersionMinor(byte versionMinor)
	{
		m_versionMinor = versionMinor;
	}

	public boolean isHasExternalReferences()
	{
		return m_hasExternalReferences;
	}

	public void setHasExternalReferences(boolean hasExternalReferences)
	{
		m_hasExternalReferences = hasExternalReferences;
	}

	public long getTotalFileSize()
	{
		return m_totalFileSize;
	}

	public void setTotalFileSize(long totalFileSize)
	{
		m_totalFileSize = totalFileSize;
	}

	public long getApproximateContentSize()
	{
		return m_approximateContentSize;
	}

	public void setApproximateContentSize(long approximateContentSize)
	{
		m_approximateContentSize = approximateContentSize;
	}

	public String getAuthoringField()
	{
		return m_authoringField;
	}

	public void setAuthoringField(String authoringField)
	{
		m_authoringField = authoringField;
	}
	
	public int getObjectType()
	{
		return HEADER;
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
		m_versionMajor = (byte)is.readByte();
		m_versionMinor = (byte)is.readByte();
		m_hasExternalReferences = is.readBoolean();
		m_totalFileSize = is.readUInt32();
		m_approximateContentSize = is.readUInt32();
		m_authoringField = is.readString();
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
		os.writeByte(m_versionMajor);
		os.writeByte(m_versionMinor);
		os.writeBoolean(m_hasExternalReferences);
		os.writeUInt32(m_totalFileSize);
		os.writeUInt32(m_approximateContentSize);
		os.writeString(m_authoringField);
	}
}