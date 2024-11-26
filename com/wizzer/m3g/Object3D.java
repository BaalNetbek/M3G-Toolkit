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
import java.util.logging.*;

public abstract class Object3D extends M3GObject
{
	// The user identifier.
	private int m_userID;
	// The collection of associated animation tracks.
	private ArrayList m_animationTracks;
	// The collection of user parameters.
	private Hashtable m_parameters;
	// The associated user object.
	private Object m_userObject;

    ////////// Methods part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 */
	protected Object3D()
	{
		m_animationTracks = new ArrayList();
		m_parameters = new Hashtable();
		m_userObject = m_parameters;
	}

	public final int animate(int time)
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "animate(int time)", "Not implemented");
		return 0;
	}

	public final Object3D duplicate()
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "duplicate()", "Not implemented");
		return null;
	}

	public Object3D find(int userID)
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "find(int userID)", "Not implemented");
		return null;
	}

	public int getReferences(Object3D references[])
	{
		//Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "getReferences(Object3D references[])", "Not implemented");
		return 0;
	}

	public void setUserID(int userID)
	{
		m_userID = userID;
	}

	public int getUserID()
	{
		return m_userID;
	}

	public void setUserObject(Object userObject)
	{
		m_userObject = userObject;
	}

	public Object getUserObject()
	{
		return m_userObject;
	}

	public void addAnimationTrack(AnimationTrack animationTrack)
	{
		// TODO: IllegalArgumentException
		if (animationTrack == null)
			throw new NullPointerException();
		
		m_animationTracks.add(animationTrack);
	}

	public AnimationTrack getAnimationTrack(int index)
	{
		return (AnimationTrack)m_animationTracks.get(index);
	}

	public void removeAnimationTrack(AnimationTrack animationTrack)
	{
		m_animationTracks.remove(animationTrack);
	}

	public int getAnimationTrackCount()
	{
		return m_animationTracks.size();
	}

    ////////// Methods not part of M3G Specification //////////
	
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
		// Read userID
		setUserID((int)(is.readUInt32() & 0xffffffff));
		// Read number of animationTracks
		long count = is.readUInt32();
		for (int i = 0; i < count; i++)
		{
			// Read animationTrack
			long index = is.readObjectIndex();
			M3GObject obj = getObjectAtIndex(table,index,ANIMATION_TRACK);
			if (obj != null)
				addAnimationTrack((AnimationTrack)obj);
			else
				throw new IOException("Object3D:track-index = " + index);
		}
		// Read userParameterCount
		count = is.readUInt32();
		for (int i = 0; i < count; i++)
		{
			// Read parameterID
			long id = is.readUInt32();
			// Read parameterValue
			byte value[] = new byte[(int)is.readUInt32()];
			is.read(value);
			m_parameters.put(new Long(id), value);
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
		// Write userID
		os.writeUInt32(m_userID);
		// Write number of animationTracks
		os.writeUInt32(getAnimationTrackCount());
		for (int i = 0; i < getAnimationTrackCount(); i++)
		{
			// Write animationTrack reference
			int index = table.indexOf(getAnimationTrack(i));
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Object3D:track-index = " + index);
		}
		// Write userParameterCount
		os.writeUInt32(m_parameters.size());
		for (Enumeration e = m_parameters.keys(); e.hasMoreElements(); )
		{
			Long id = (Long)e.nextElement();
			byte value[] = (byte[])m_parameters.get(id);
			// Write parameterID
			os.writeUInt32(id.longValue());
			// Write parameterValue
			os.write(value);
		}
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		for (int i = 0; i < m_animationTracks.size(); i++)
			((AnimationTrack)m_animationTracks.get(i)).buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}