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

public class Light extends Node
{
	public static final int AMBIENT     = 128;
	public static final int DIRECTIONAL = 129;
	public static final int OMNI 	    = 130;
	public static final int SPOT 	    = 131;

	// The constant coefficient.
	private float m_constantAttenuation;
	// The linear coefficient.
	private float m_linearAttenuation;
	// The quadratic coefficient.
	private float m_quadraticAttenuation;
	// The color of the light.
	private int m_color;
	// The type of light.
	private int m_mode;
	// The current intensity.
	private float m_intensity;
	// The current spot angle.
	private float m_spotAngle;
	// The current spot exponenet.
	private float m_spotExponent;
	
	private int m_lightId = -1;

    ////////// Methods part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 * <p>
	 * Constructs a <code>Light</code> with default values.
	 * </p>
	 */
	public Light()
	{
		m_mode = DIRECTIONAL;
		m_color = 0x00FFFFFF;
		m_intensity = 1.0f;
		m_constantAttenuation = 1.0f;
		m_linearAttenuation = 0.0f;
		m_quadraticAttenuation = 0.0f;
		m_spotAngle = 45.0f;
		m_spotExponent = 0.0f;
	}

	public void setMode(int mode)
	{
		if (mode < AMBIENT | mode > SPOT)
			throw new IllegalArgumentException("Light: mode is not one of AMBIENT, DIRECTIONAL, OMNI, SPOT");

		m_mode = mode;
	}

	public int getMode()
	{
		return m_mode;
	}

	public void setIntensity(float intensity)
	{
		m_intensity = intensity;
	}

	public float getIntensity()
	{
		return m_intensity;
	}

	public void setColor(int RGB)
	{
		m_color = RGB;
	}

	public int getColor()
	{
		return m_color;
	}

	public void setSpotAngle(float angle)
	{
		if (angle < 0 || angle > 90)
			throw new IllegalArgumentException("Light: angle is not in [0, 90]");
		
		m_spotAngle = angle;
	}

	public float getSpotAngle()
	{
		return m_spotAngle;
	}

	public void setSpotExponent(float exponent)
	{
		if (exponent < 0 || exponent > 128)
			throw new IllegalArgumentException("Light: exponent is not in [0, 128]");
		
		m_spotExponent = exponent;
	}

	public float getSpotExponent()
	{
		return m_spotExponent;
	}


	public void setAttenuation(float constant,float linear,float quadratic)
	{
		if (constant < 0 || linear < 0 || quadratic < 0)
			throw new IllegalArgumentException("Light: any of the parameter values are negative");
		if (constant == 0 && linear == 0 && quadratic == 0)
			throw new IllegalArgumentException("Light: all of the parameter values are zero");
		
		m_constantAttenuation = constant;
		m_linearAttenuation = linear;
		m_quadraticAttenuation = quadratic;
	}

	public float getConstantAttenuation()
	{
		return m_constantAttenuation;
	}

	public float getLinearAttenuation()
	{
		return m_linearAttenuation;
	}

	public float getQuadraticAttenuation()
	{
		return m_quadraticAttenuation;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
    ////////// Methods part of M3G Specification //////////
	
	public int getObjectType()
	{
		return LIGHT;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		setAttenuation(is.readFloat32(), is.readFloat32(), is.readFloat32());
		setColor(is.readColorRGB());
		setMode(is.readByte());
		setIntensity(is.readFloat32());
		setSpotAngle(is.readFloat32());
		setSpotExponent(is.readFloat32());
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeFloat32(m_constantAttenuation);
		os.writeFloat32(m_linearAttenuation);
		os.writeFloat32(m_quadraticAttenuation);
		os.writeColorRGB(m_color);
		os.writeByte(m_mode);
		os.writeFloat32(m_intensity);
		os.writeFloat32(m_spotAngle);
		os.writeFloat32(m_spotExponent);
	}
	
	void setupGL(GL gl)
	{
		// TODO: color and intensity
		float[] col = (new Color(m_color)).toArray();
		
		col[0] *= m_intensity;
		col[1] *= m_intensity;
		col[2] *= m_intensity;
		col[3] *= m_intensity;
		
		if (m_mode == Light.AMBIENT)
			gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, col, 0);
		else
		{
			getFreeLightId(gl);
			
			if (m_lightId == -1)
				return;
			
			gl.glLightfv(m_lightId, GL.GL_DIFFUSE, col, 0);
			gl.glLightfv(m_lightId, GL.GL_AMBIENT, new float[] { 0,0,0,0 }, 0);
			gl.glLightfv(m_lightId, GL.GL_SPECULAR, col, 0);
			
			if (m_mode == Light.OMNI)
			{
				// Set position.
				gl.glLightfv(m_lightId, GL.GL_POSITION, new float[] { 0,0,0,1 }, 0);
				
				// Set default values for cutoff/exponent.
				gl.glLightf(m_lightId, GL.GL_SPOT_CUTOFF, 180.0f); // 0..90, 180
				gl.glLightf(m_lightId, GL.GL_SPOT_EXPONENT, 0.0f); // 0..128				
			}
			else if (m_mode == Light.SPOT)
			{
				// Set position.
				gl.glLightfv(m_lightId, GL.GL_POSITION, new float[] { 0,0,0,1 }, 0);
				
				// Set cutoff/exponent.
				gl.glLightf(m_lightId, GL.GL_SPOT_CUTOFF, m_spotAngle);
				gl.glLightf(m_lightId, GL.GL_SPOT_EXPONENT, m_spotExponent);
				
				// Set default spot direction.
				gl.glLightfv(m_lightId, GL.GL_SPOT_DIRECTION, new float[] { 0,0,1 }, 0);
			}
			else if (m_mode == Light.DIRECTIONAL)
			{
				// Set direction (w=0 meaning directional instead of positional).
				gl.glLightfv(m_lightId, GL.GL_POSITION, new float[] { 0,0,1,0 }, 0);
				
				// Set default values for cutoff/exponent.
				gl.glLightf(m_lightId, GL.GL_SPOT_CUTOFF, 180.0f);
				gl.glLightf(m_lightId, GL.GL_SPOT_EXPONENT, 0.0f);
			}
			
			gl.glLightf(m_lightId, GL.GL_CONSTANT_ATTENUATION, m_constantAttenuation);
			gl.glLightf(m_lightId, GL.GL_LINEAR_ATTENUATION, m_linearAttenuation);
			gl.glLightf(m_lightId, GL.GL_QUADRATIC_ATTENUATION, m_quadraticAttenuation);
		}
	}
	
	private void getFreeLightId(GL gl)
	{
		m_lightId = -1;
		for (int i = 0; i < Graphics3D.MAX_LIGHT_COUNT; i++)
		{
			if (! gl.glIsEnabled(GL.GL_LIGHT0 + i))
			{
				m_lightId = GL.GL_LIGHT0 + i;
				gl.glEnable(m_lightId);
				return;
			}
		}
	}
}
