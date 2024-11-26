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

public abstract class M3GObject
{
	public static final int HEADER                  = 0;
	public static final int ANIMATION_CONTROLLER    = 1;
	public static final int ANIMATION_TRACK         = 2;
	public static final int APPEARANCE              = 3;
	public static final int BACKGROUND              = 4;
	public static final int CAMERA                  = 5;
	public static final int COMPOSITING_MODE        = 6;
	public static final int FOG                     = 7;
	public static final int POLYGON_MODE            = 8;
	public static final int GROUP                   = 9;
	public static final int IMAGE2D                 = 10;
	public static final int TRIANGLE_STRIP_ARRAY    = 11;
	public static final int LIGHT                   = 12;
	public static final int MATERIAL                = 13;
	public static final int MESH                    = 14;
	public static final int MORPHING_MESH           = 15;
	public static final int SKINNED_MESH            = 16;
	public static final int TEXTURE2D               = 17;
	public static final int SPRITE3D                = 18;
	public static final int KEYFRAME_SEQUENCE       = 19;
	public static final int VERTEX_ARRAY            = 20;
	public static final int VERTEX_BUFFER           = 21;
	public static final int WORLD                   = 22;
	public static final int EXTERNAL_REFERENCE      = 255;

	private boolean m_root = true;

	public boolean isRoot()
	{
		return m_root;
	}

	public abstract int getObjectType();

	protected abstract void unmarshall(M3GInputStream is, ArrayList table) throws IOException;

	protected abstract void marshall(M3GOutputStream os, ArrayList table) throws IOException;

	protected void buildReferenceTable(ArrayList table)
	{
		if (! table.contains(this)) table.add(this);
	}

	/**
	 * Get the object at the specified index.
	 * 
	 * @param table The cache of referenced objects.
	 * @param index The index into the reference object cache.
	 * @param type The type of object we are retrieving.
	 * 
	 * @return If the object exists at the specified index and it is of the correct
	 * type, then it is returned. Otherwise, <b>null</b> will be returned.
	 */
	protected M3GObject getObjectAtIndex(ArrayList table, long index, int type)
	{
		if (index > 0 && index < table.size())
		{
			Object obj = table.get((int)(index & 0xffffffff));
			if ((obj instanceof M3GObject) && (type == -1 || ((M3GObject) obj).getObjectType() == type))
			{
				((M3GObject) obj).m_root = false;
				return (M3GObject) obj;
			} else  if ((obj instanceof M3GObject) && ((M3GObject) obj).getObjectType() == M3GObject.EXTERNAL_REFERENCE)
			{
				// The object is an external reference.
				return ((ExternalReference) obj).getReference();
			}
			else return null;
		}
		else return null;
	}
}
