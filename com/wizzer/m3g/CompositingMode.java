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

// Include standard Java classes.
import java.io.*;
import java.util.*;

// Import JOGL classes.
import javax.media.opengl.*;

public class CompositingMode extends Object3D
{
	public static final int ALPHA       = 64;
	public static final int ALPHA_ADD   = 65;
	public static final int MODULATE    = 66;
	public static final int MODULATE_X2 = 67;
	public static final int REPLACE     = 68;

	// Flag indicating depth test enabled.
	private boolean m_depthTestEnabled;
	// Flag indicating depth write enabled.
	private boolean m_depthWriteEnabled;
	// Flag indicating color write enabled.
	private boolean m_colorWriteEnabled;
	// Flag indicating alpha write enabled.
	private boolean m_alphaWriteEnabled;
	// The current frame buffer blending moce.
	private int m_blending;
	// The threshold value for alpha testing.
	private float m_alphaThreshold;
	// The value that is added to the screen space Z coordinate of a pixel
	// immediately before depth test and depth write.
	private float m_depthOffsetFactor;
	// The units of the depth offset.
	private float m_depthOffsetUnits;

    ////////// Methods part of M3G Specification //////////

	/**
	 * The default constructor.
	 * <p>
	 * Constructs a <code>CompositingMode</code> object with default values.
	 * </p>
	 */
	public CompositingMode()
	{
		m_blending = REPLACE;
		m_alphaThreshold = 0.0f;
		m_depthOffsetFactor = 0.0f;
		m_depthOffsetUnits = 0.0f;
		m_depthTestEnabled = true;
		m_depthWriteEnabled = true;
		m_colorWriteEnabled = true;
		m_alphaWriteEnabled = true;
	}

	public void setBlending(int mode)
	{
		if (mode < ALPHA || mode > REPLACE)
			throw new IllegalArgumentException("CompositingMode: mode is not one of the symbolic constants");
		
		m_blending = mode;
	}

	public int getBlending()
	{
		return m_blending;
	}

	public void setAlphaThreshold(float threshold)
	{
		if (threshold < 0 || threshold > 1)
			throw new IllegalArgumentException("CompositingMode: threshold is negative or greater than 1.0");
		
		m_alphaThreshold = threshold;
	}

	public float getAlphaThreshold()
	{
		return m_alphaThreshold;
	}

	public void setAlphaWriteEnable(boolean enable)
	{
		m_alphaWriteEnabled = enable;
	}

	public boolean isAlphaWriteEnabled()
	{
		return m_alphaWriteEnabled;
	}

	public void setColorWriteEnable(boolean enable)
	{
		m_colorWriteEnabled = enable;
	}

	public boolean isColorWriteEnabled()
	{
		return m_colorWriteEnabled;
	}

	public void setDepthWriteEnable(boolean enable)
	{
		m_depthWriteEnabled = enable;
	}

	public boolean isDepthWriteEnabled()
	{
		return m_depthWriteEnabled;
	}

	public void setDepthTestEnable(boolean enable)
	{
		m_depthTestEnabled = enable;
	}

	public boolean isDepthTestEnabled()
	{
		return m_depthTestEnabled;
	}

	public void setDepthOffset(float factor, float units)
	{
		m_depthOffsetFactor = factor;
		m_depthOffsetUnits = units;
	}

	public float getDepthOffsetFactor()
	{
		return m_depthOffsetFactor;
	}

	public float getDepthOffsetUnits()
	{
		return m_depthOffsetUnits;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}

	////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return COMPOSITING_MODE;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		setDepthTestEnable(is.readBoolean());
		setDepthWriteEnable(is.readBoolean());
		setColorWriteEnable(is.readBoolean());
		setAlphaWriteEnable(is.readBoolean());
		setBlending(is.readByte());
		setAlphaThreshold(is.readByte() / 255f);
		setDepthOffset(is.readFloat32(), is.readFloat32());
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);
		
		os.writeBoolean(m_depthTestEnabled);
		os.writeBoolean(m_depthWriteEnabled);
		os.writeBoolean(m_colorWriteEnabled);
		os.writeBoolean(m_alphaWriteEnabled);
		os.writeByte(m_blending);
		os.writeByte((byte)(m_alphaThreshold*255));
		os.writeFloat32(m_depthOffsetFactor);
		os.writeFloat32(m_depthOffsetUnits);
	}
	
	void setupGL(GL gl)
	{
		// TODO: move to one-time-initilize func
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glBlendEquation(GL.GL_FUNC_ADD);

		// Setup depth testing		
		if (m_depthTestEnabled)
			gl.glEnable(GL.GL_DEPTH_TEST);
		else
			gl.glDisable(GL.GL_DEPTH_TEST);

		// Setup depth and color writes
		gl.glDepthMask(m_depthWriteEnabled);
		gl.glColorMask(m_colorWriteEnabled, m_colorWriteEnabled, m_colorWriteEnabled, m_alphaWriteEnabled);
		
		// Setup alpha testing		
		if (m_alphaThreshold > 0)
		{
			gl.glAlphaFunc(GL.GL_GEQUAL, m_alphaThreshold);
			gl.glEnable(GL.GL_ALPHA_TEST);
		}
		else
			gl.glDisable(GL.GL_ALPHA_TEST);

		// Setup blending
		if (m_blending != REPLACE)
		{
			switch (m_blending)
			{
				case ALPHA_ADD:
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
					break;
				case ALPHA:
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case MODULATE:
					gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ZERO);
					break;
				case MODULATE_X2:
					gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_SRC_COLOR);
			}
			gl.glEnable(GL.GL_BLEND);
		}
		else
			gl.glDisable(GL.GL_BLEND);
		
		// Setup depth offset.
		if (m_depthOffsetFactor != 0 || m_depthOffsetUnits != 0)
		{
			gl.glPolygonOffset(m_depthOffsetFactor, m_depthOffsetUnits);
			gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		}
		else
			gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
	}
}