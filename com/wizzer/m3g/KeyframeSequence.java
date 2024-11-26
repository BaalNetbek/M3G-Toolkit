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

public class KeyframeSequence extends Object3D
{
	public static final int LINEAR     = 176;
	public static final int SLERP      = 177;
	public static final int SPLINE     = 178;
	public static final int SQUAD      = 179;
	public static final int STEP       = 180;
	public static final int CONSTANT   = 192;
	public static final int LOOP       = 193;

	// The type of interpolation.
	private int m_interpolation;
	// The current repeat mode.
	private int m_repeatMode;
	// The encoding scheme for keyframe data.
	private int m_encoding;
	// The duration of the sequence.
	private int m_duration;
	// The range of keyframes that are included in the animation.
	private int m_validRangeFirst;
	// The last keyframe of the current valid range for the sequence.
	private int m_validRangeLast;
	// The number of components.
	private int m_componentCount;
	// The number of keyframes.
	private int m_keyframeCount;
	// The collection of keyframes.
	private float m_keyframes[];
	// The collection of durations (per keyframe);
	private int m_times[];

    ////////// Methods part of M3G Specification //////////

	public KeyframeSequence(int numKeyframes, int numComponents ,int interpolation)
	{
		if (numKeyframes < 1)
			throw new IllegalArgumentException("KeyframeSequence: numKeyframes < 1");
		if (numComponents < 1)
			throw new IllegalArgumentException("KeyframeSequence: numComponents < 1");
		if (interpolation < LINEAR || interpolation > STEP)
			throw new IllegalArgumentException("KeyframeSequence: interpolation is not one of LINEAR, SLERP, SPLINE, SQUAD, STEP");
		if ((interpolation == SLERP || interpolation == SQUAD) && numComponents != 4)
			throw new IllegalArgumentException("KeyframeSequence: interpolation is not a valid interpolation mode for keyframes of size numComponents");

		m_keyframeCount = numKeyframes;
		m_keyframes = new float[numKeyframes*numComponents];
		m_times = new int[numKeyframes];
		m_componentCount = numComponents;
		m_interpolation = interpolation;
		m_repeatMode = CONSTANT;
		m_duration = 0;
		m_validRangeFirst = 0;
		m_validRangeLast = m_keyframeCount - 1;
	}

	public void setKeyframe(int index, int time, float value[])
	{
		if (value.length < m_componentCount)
			throw new IllegalArgumentException("KeyframeSequence: value.length <  numComponents");
		if (time < 0)
			throw new IllegalArgumentException("KeyframeSequence: time < 0");
		
		System.arraycopy(value, 0, m_keyframes, index * m_componentCount, value.length);
		m_times[index] = time;
	}

	public void setValidRange(int first, int last)
	{
		if (first < 0 || first >= m_keyframeCount)
			throw new IllegalArgumentException("KeyframeSequence: (first < 0) || (first >= numKeyframes)");
		if (last < 0 || last >= m_keyframeCount)
			throw new IllegalArgumentException("KeyframeSequence: (last < 0) || (last >= numKeyframes)");
		
		m_validRangeFirst = first;
		m_validRangeLast = last;
	}

	public void setDuration(int duration)
	{
		if (duration <= 0)
			throw new IllegalArgumentException("KeyframeSequence: duration <= 0");
		
		m_duration = duration;
	}

	public int getDuration()
	{
		return m_duration;
	}

	public void setRepeatMode(int mode)
	{
		if (mode != CONSTANT && mode != LOOP)
			throw new IllegalArgumentException("KeyframeSequence: mode is not one of CONSTANT, LOOP");
		
		m_repeatMode = mode;
	}

	public int getRepeatMode()
	{
		return m_repeatMode;
	}

	
	public int getInterpolationType()
	{
		return m_interpolation;
	}

	public int getValidRangeFirst()
	{
		return m_validRangeFirst;
	}

	public int getValidRangeLast()
	{
		return m_validRangeLast;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	KeyframeSequence()
	{
	}

	public int getObjectType()
	{
		return KEYFRAME_SEQUENCE;
	}
	
	public int getEncoding()
	{
		return m_encoding;
	}
	
	public int getComponentCount()
	{
		return m_componentCount;
	}
	
	public int getKeyframeCount()
	{
		return m_keyframeCount;
	}
	
	public int[] getTimes()
	{
		return m_times;
	}
	
	public float[] getKeyFrames()
	{
		return m_keyframes;
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

		// Read interpolation
		m_interpolation = is.readByte();
		if (m_interpolation < LINEAR || m_interpolation > STEP)
			throw new IOException("KeyframeSequence:interpolation = " + m_interpolation);
		// Read repeatMode
		setRepeatMode(is.readByte());
		// Read encoding
		m_encoding = is.readByte();
		// Read duration
		m_duration = (int)is.readUInt32();
		// Read validRangeFirst
		m_validRangeFirst = (int)is.readUInt32();
		// Read validRangeLast
		m_validRangeLast = (int)is.readUInt32();
		// Read componentCount
		m_componentCount = (int)is.readUInt32();
		// Read keyframeCount
		m_keyframeCount = (int)is.readUInt32();
		m_keyframes = new float[m_keyframeCount * m_componentCount];
		m_times = new int[m_keyframeCount];
		if (m_encoding == 0)
		{
			for (int i = 0, index = 0; i < m_keyframeCount; i++)
			{
				// Read time
				m_times[i] = (int)is.readInt32();
				// Read vectorValue
				for (int c = 0; c < m_componentCount; c++)
					m_keyframes[index++] = is.readFloat32();
			}
		}
		// TODO: implement encoding 1 and 2.
		else throw new IOException("KeyframeSequence:encoding = " + m_encoding);
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

		os.writeByte(m_interpolation);
		os.writeByte(m_repeatMode);
		// TODO: implement encoding 1 and 2.
		int encoding = 0;
		os.writeByte(encoding);
		os.writeUInt32(m_duration);
		os.writeUInt32(m_validRangeFirst);
		os.writeUInt32(m_validRangeLast);
		os.writeUInt32(m_componentCount);
		os.writeUInt32(m_keyframeCount);
		if (encoding == 0)
		{
			for (int i = 0, index = 0; i < m_keyframeCount; i++)
			{
				os.writeInt32(m_times[i]);
				for (int c = 0; c < m_componentCount; c++)
					os.writeFloat32(m_keyframes[index++]);
			}
		}
	}
}
