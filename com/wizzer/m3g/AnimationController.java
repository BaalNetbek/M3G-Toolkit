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

public class AnimationController extends Object3D
{
	// The playback speed.
	private float m_speed;
	// The blending weight.
	private float m_weight;
	// The starting time of the current active interval.
	private int m_activeIntervalStart;
	// The ending time of the current active interval.
	private int m_activeIntervalEnd;
	// The current reference local time.
	private float m_sequenceTime;
	// The current reference world time.
	private int m_worldTime;

    ////////// Methods part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 * <p>
	 * Creates a new <code>AnimationController</code> object.
	 * </p>
	 */
	public AnimationController()
	{
		m_activeIntervalStart = m_activeIntervalEnd = 0;
		m_weight = 1.0f;
		m_speed = 1.0f;
		m_sequenceTime = m_worldTime = 0;
	}

	public void setActiveInterval(int start, int end)
	{
		if (start > end)
			throw new IllegalArgumentException("AnimationController: start > end");
		
		m_activeIntervalStart = start;
		m_activeIntervalEnd = end;
	}

	public int getActiveIntervalStart()
	{
		return m_activeIntervalStart;
	}

	public int getActiveIntervalEnd()
	{
		return m_activeIntervalEnd;
	}

	public void setSpeed(float speed, int worldTime)
	{
		this.m_speed = speed;
	}

	public float getSpeed()
	{
		return m_speed;
	}

	public void setPosition(float sequenceTime, int worldTime)
	{
		this.m_sequenceTime = sequenceTime;
		this.m_worldTime = worldTime;
	}

	public float getPosition(int worldTime)
	{
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.AnimationController", "getPosition(int worldTime)", "Not implemented");
		return 0;
	}

	public void setWeight(float weight)
	{
		if (weight < 0)
			throw new IllegalArgumentException("weight < 0");
		this.m_weight = weight;
	}

	public float getWeight()
	{
		return m_weight;
	}
	
	public int getRefWorldTime()
	{
		return m_worldTime;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}

	////////// Methods not part of M3G Specification //////////

	public float getRefSequenceTime()
	{
		return m_sequenceTime;
	}
	
	public int getObjectType()
	{
		return ANIMATION_CONTROLLER;
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
		super.unmarshall(is, table);

		// Read speed
		setSpeed(is.readFloat32(), 0);
		// Read weight
		setWeight(is.readFloat32());
		// Read activeIntervalStart and activeIntervalEnd
		setActiveInterval((int)is.readInt32(), (int)is.readInt32());
		// Read referenceSequenceTime and referenceWorldTime
		setPosition(is.readFloat32(), (int)is.readInt32());
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

		// Write speed
		os.writeFloat32(m_speed);
		// Write weight
		os.writeFloat32(m_weight);
		// Write activeIntervalStart
		os.writeInt32(m_activeIntervalStart);
		// Write activeIntervalEnd
		os.writeInt32(m_activeIntervalEnd);
		// Write referenceSequenceTime
		os.writeFloat32(m_sequenceTime);
		// Write referenceWorldTime
		os.writeInt32(m_worldTime);
	}
}
