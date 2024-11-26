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

public class AnimationTrack extends Object3D
{
	public static final int ALPHA           = 256;
	public static final int AMBIENT_COLOR   = 257;
	public static final int COLOR           = 258;
	public static final int CROP            = 259;
	public static final int DENSITY         = 260;
	public static final int DIFFUSE_COLOR   = 261;
	public static final int EMISSIVE_COLOR  = 262;
	public static final int FAR_DISTANCE    = 263;
	public static final int FIELD_OF_VIEW   = 264;
	public static final int INTENSITY       = 265;
	public static final int MORPH_WEIGHTS   = 266;
	public static final int NEAR_DISTANCE   = 267;
	public static final int ORIENTATION     = 268;
	public static final int PICKABILITY     = 269;
	public static final int SCALE           = 270;
	public static final int SHININESS       = 271;
	public static final int SPECULAR_COLOR  = 272;
	public static final int SPOT_ANGLE      = 273;
	public static final int SPOT_EXPONENT   = 274;
	public static final int TRANSLATION     = 275;
	public static final int VISIBILITY      = 276;

	// The associated KeyframeSequence.
	private KeyframeSequence m_keyframeSequence;
	// The associated AnimationController.
	private AnimationController m_controller;
	// The associated target property.
	private long m_targetProperty;

    ////////// Methods part of M3G Specification //////////

	public AnimationTrack(KeyframeSequence sequence, int property)
	{
		if (sequence == null)
			throw new NullPointerException("AnimationTrack: sequence is null");
		if ((property < ALPHA) || (property > VISIBILITY))
			throw new IllegalArgumentException("AnimationTrack: property is an invalid value");
		// TODO: Throw an IllegalArgementException if the sequence is not compatible
		// with property.
		
		m_keyframeSequence = sequence;
		m_targetProperty = property;
	}

	public void setController(AnimationController controller)
	{
		m_controller = controller;
	}

	public AnimationController getController()
	{
		return m_controller;
	}

	public KeyframeSequence getKeyframeSequence()
	{
		return m_keyframeSequence;
	}

	public int getTargetProperty()
	{
		return (int)(m_targetProperty & 0xffffffff);
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_keyframeSequence != null)
		{
			if (references != null)
				references[numReferences] = m_keyframeSequence;
			++numReferences;
		}
		
		if (m_controller != null)
		{
			if (references != null)
				references[numReferences] = m_controller;
			++numReferences;
		}
				
		return numReferences;
	}


	////////// Methods not part of M3G Specification //////////
	
	AnimationTrack() {}

	public int getObjectType()
	{
		return ANIMATION_TRACK;
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

		// Read keyframeSequence
		long index = is.readObjectIndex();
		M3GObject obj = getObjectAtIndex(table, index, KEYFRAME_SEQUENCE);
		if (obj != null)
			m_keyframeSequence = (KeyframeSequence)obj;
		else
			throw new IOException("AnimationTrack:keyframeSequence-index = " + index);
		// Read animationConroller
		index = is.readObjectIndex();
		if (index != 0)
		{
			obj = getObjectAtIndex(table, index, ANIMATION_CONTROLLER);
			if (obj != null)
				setController((AnimationController)obj);
			else
				throw new IOException("AnimationTrack:controller-index = " + index);
		}
		// Read property
		m_targetProperty = is.readUInt32();
		if (m_targetProperty < ALPHA || m_targetProperty > VISIBILITY)
			throw new IOException("AnimationTrack:targetProperty = " + m_targetProperty);
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

		// Write keyframeSequence
		int index = table.indexOf(m_keyframeSequence);
		if (index > 0)
			os.writeObjectIndex(index);
		else
			throw new IOException("AnimationTrack:keyframeSequence-index = " + index);
		// Write animationController
		if (m_controller != null)
		{
			index = table.indexOf(m_controller);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("AnimationTrack:controller-index = " + index);
		}
		// Write propertyID
		os.writeUInt32(m_targetProperty);
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		m_keyframeSequence.buildReferenceTable(table);
		if (m_controller != null)
			m_controller.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
}