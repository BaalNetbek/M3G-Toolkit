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
 * The <code>M3GFile</code> class manages file I/O for the Mobile 3D Graphics
 * file format (.m3g extension).
 * 
 * @author Mark Millard
 */
public class M3GFile
{
	/** The file identifier for the M3G file. */
	public static final byte FILE_IDENTIFIER[]={(byte)0xAB,0x4A,0x53,0x52,0x31,0x38,0x34,(byte)0xBB,0x0D,0x0A,0x1A,0x0A};

	// The Header Section.
	private HeaderSection m_headerSection;
	// The External References Section.
	private ExternalReferencesSection m_externalReferencesSection;
	// The collection of Scene Sections.
	private ArrayList m_sceneSections;
	
	// The "current" working directory for this M3G file.
	private String m_cwd = null;

	public M3GFile()
	{
		m_headerSection = new HeaderSection();
		m_externalReferencesSection = new ExternalReferencesSection();
		m_sceneSections = new ArrayList();
		
		// Set the current working directory.
		m_cwd = System.getProperty("user.dir");
	}

	public M3GFile(File file) throws IOException
	{
		m_headerSection = new HeaderSection();
		m_externalReferencesSection = new ExternalReferencesSection();
		m_sceneSections = new ArrayList();
		
		ArrayList table = new ArrayList();
		table.add(Boolean.FALSE);
		
		// Set the current working directory.
		m_cwd = file.getAbsolutePath();
		m_cwd = m_cwd.substring(0,m_cwd.lastIndexOf(System.getProperty("file.separator")));

		// Set the current working directory for external references.
		// This must be done for URIs that are not URLs.
		ExternalReference.setCwd(m_cwd);

		M3GInputStream is = new M3GInputStream(new FileInputStream(file));
		// File Identifier.
		byte id[] = new byte[FILE_IDENTIFIER.length];
		is.read(id);
		if (! Arrays.equals(id,FILE_IDENTIFIER))
			throw new IOException("M3GFile: FILE_IDENTIFIER");
		// Header Section.
		m_headerSection.unmarshall(is,table);
		// External References Section.
		if (m_headerSection.getHeaderObject().isHasExternalReferences())
			m_externalReferencesSection.unmarshall(is, table);
		// Scene Sections.
		while (is.available() > 0)
		{
			SceneSection scene = new SceneSection();
			scene.unmarshall(is, table);
			m_sceneSections.add(scene);
		}
		is.close();
	}

	public HeaderSection getHeaderSection()
	{
		return m_headerSection;
	}

	public ExternalReferencesSection getExternalReferencesSection()
	{
		return m_externalReferencesSection;
	}

	public void addSceneSection(SceneSection scene)
	{
		m_sceneSections.add(scene);
	}

	public SceneSection[] getSceneSections()
	{
		return (SceneSection[])m_sceneSections.toArray(new SceneSection[m_sceneSections.size()]);
	}

	public void removeSceneSection(SceneSection scene)
	{
		m_sceneSections.remove(scene);
	}

	public void removeSceneSections()
	{
		m_sceneSections.clear();
	}
	
	void setCwd(String cwd)
	{
		m_cwd = cwd;
	}
	
	public String getCwd()
	{
		return m_cwd;
	}

	public void marshall(OutputStream os) throws IOException
	{
		// Create a table of references..
		ArrayList table = buildReferenceTable();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		M3GOutputStream out = new M3GOutputStream(baos);
		if (m_externalReferencesSection.getExternalReferenceCount() > 0)
			m_externalReferencesSection.marshall(out,table);
		for (int i = 0; i < m_sceneSections.size(); i++)
		{
			SceneSection ss = (SceneSection)m_sceneSections.get(0);
			ss.marshall(out, table);
		}
		
		// Header Section.
		m_headerSection.getHeaderObject().setHasExternalReferences(m_externalReferencesSection.getExternalReferenceCount() > 0);
		m_headerSection.getHeaderObject().setAuthoringField("M3GToolkit (www.wizzerworks.com)");
		byte header[] = getSectionBytes(m_headerSection,table);
		int size = FILE_IDENTIFIER.length + header.length + baos.size();
		m_headerSection.getHeaderObject().setTotalFileSize(size);
		m_headerSection.getHeaderObject().setApproximateContentSize(size);

		os.write(FILE_IDENTIFIER);
		os.write(getSectionBytes(m_headerSection,table));
		baos.writeTo(os);
		os.close();
	}

	public void marshall(File file) throws IOException
	{
		marshall(new FileOutputStream(file));
	}

	private byte[] getSectionBytes(Section section, ArrayList table) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		section.marshall(new M3GOutputStream(baos), table);
		return baos.toByteArray();
	}

	private ArrayList buildReferenceTable()
	{
		ArrayList table = new ArrayList();
		table.add(Boolean.FALSE);
		table.add(m_headerSection.getHeaderObject());
		// TODO: Create the table of external references.
		//if (externalReferences.hasExternalReferences()) externalReferences.write(os);
		for (int i = 0; i < m_sceneSections.size(); i++)
		{
			SceneSection ss = (SceneSection)m_sceneSections.get(i);
			Object3D objects[] = ss.getObjects3D();
			for (int j = 0; j < objects.length; j++)
				objects[j].buildReferenceTable(table);
		}
		return table;
	}
}