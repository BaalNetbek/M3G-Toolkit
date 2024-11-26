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
 * The <code>ExternalReferenceSection</code> is a utility for reading/writing
 * an External Reference Section from/to a M3G file. It is not a runtime
 * construct called out by the M3G Specification.
 * <p>
 * This class is used to manage the external references section.
 * </p>
 * 
 * @author Mark Millard
 */
public class ExternalReferencesSection extends Section
{
	private ArrayList<ExternalReference> m_externalReferences;

	public ExternalReferencesSection()
	{
		m_externalReferences = new ArrayList<ExternalReference>();
	}

	public void addExternalReference(ExternalReference reference)
	{
		m_externalReferences.add(reference);
	}

	public ExternalReference[] getExternalReferences()
	{
		return (ExternalReference[])m_externalReferences.toArray(new ExternalReference[m_externalReferences.size()]);
	}

	public void removeExternalReference(ExternalReference reference)
	{
		m_externalReferences.remove(reference);
	}

	public void removeExternalReferences()
	{
		m_externalReferences.clear();
	}

	public int getExternalReferenceCount()
	{
		return m_externalReferences.size();
	}

	/**
	 * Read external object references.
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
			//byte type = (byte) is.readByte();
			short type = (short) is.readByte();
			if (type != M3GObject.EXTERNAL_REFERENCE)
				throw new IOException("ExternalReferencesSection:type = " + type);
			long length = is.readUInt32();
			ExternalReference reference = new ExternalReference();
			reference.unmarshall(is, table);
			m_externalReferences.add(reference);
			table.add(reference);
		}
	}

	/**
	 * Write external object references.
	 * 
	 * @param os The output stream to write to.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * writing the data.
	 */
	protected void writeObjects(M3GOutputStream os, ArrayList table) throws IOException
	{
		ExternalReference references[] = getExternalReferences();
		for (int i = 0; i < references.length; i++)
		{
			os.writeByte(M3GObject.EXTERNAL_REFERENCE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			references[i].marshall(new M3GOutputStream(baos),table);
			os.writeUInt32(baos.size());
			baos.writeTo(os);
		}
	}
}