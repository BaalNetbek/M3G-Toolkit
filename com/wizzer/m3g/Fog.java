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

// Import JOGL classes.
import javax.media.opengl.*;

// Import M3G Toolkit classes.
import com.wizzer.m3g.toolkit.util.Color;

public class Fog extends Object3D
{
	public static final int EXPONENTIAL = 80;
	public static final int LINEAR      = 81;

	// The current color of the fog.
	private int m_color;
	// The current fog mode.
	private int m_mode;
	// The fog density.
	private float m_density;
	// The fog front distance.
	private float m_nearDistance;
	// The fog back distance
	private float m_farDistance;

	////////// Methods part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 * <p>
	 * Constructs a <code>Fog</code> object with default values.
	 * </p>
	 */
	public Fog()
	{
		m_mode = LINEAR;
		m_density = 1.0f;
		m_nearDistance = 0.0f;
		m_farDistance = 1.0f;
		m_color = 0x00000000;
	}

	public void setMode(int mode)
	{
		if (mode != LINEAR && mode != EXPONENTIAL)
			throw new IllegalArgumentException("Fog: mode is not LINEAR or EXPONENTIAL");
		
		m_mode = mode;
	}

	public int getMode()
	{
		return m_mode;
	}

	public void setLinear(float near,float far)
	{
		m_nearDistance = near;
		m_farDistance = far;
	}

	public float getNearDistance()
	{
		return m_nearDistance;
	}

	public float getFarDistance()
	{
		return m_farDistance;
	}

	public void setDensity(float density)
	{
		if (density < 0)
			throw new IllegalArgumentException("Fog: density < 0");
		
		m_density=density;
	}

	public float getDensity()
	{
		return m_density;
	}

	public void setColor(int RGB)
	{
		m_color = RGB;
	}

	public int getColor()
	{
		return m_color;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
	////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return FOG;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		setColor(is.readColorRGB());
		setMode(is.readByte());
		if (m_mode == LINEAR)
			setLinear(is.readFloat32(), is.readFloat32());
		else setDensity(is.readFloat32());
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeColorRGB(m_color);
		os.writeByte(m_mode);
		if (m_mode == LINEAR)
		{
			os.writeFloat32(m_nearDistance);
			os.writeFloat32(m_farDistance);
		}
		else if (m_mode == EXPONENTIAL) os.writeFloat32(m_density);
	}
	
	void setupGL(GL gl)
	{
        gl.glFogi(GL.GL_FOG_MODE,getGLFogMode(m_mode));			
        gl.glFogfv(GL.GL_FOG_COLOR, Color.intToFloatArray(m_color), 0);					
        gl.glFogf(GL.GL_FOG_DENSITY, m_density);						
        gl.glFogf(GL.GL_FOG_START, m_nearDistance);							
        gl.glFogf(GL.GL_FOG_END, m_farDistance);							
        gl.glEnable(GL.GL_FOG);	
	}
	
	int getGLFogMode(int mode)
	{
		switch(mode)
		{
			case EXPONENTIAL:
				return GL.GL_EXP;
			default:
				return GL.GL_LINEAR;
		}
	}
}
