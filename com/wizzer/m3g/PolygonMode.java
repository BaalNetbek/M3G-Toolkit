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

public class PolygonMode extends Object3D
{
	public static final int CULL_BACK       = 160;
	public static final int CULL_FRONT      = 161;
	public static final int CULL_NONE       = 162;
	public static final int SHADE_FLAT      = 164;
	public static final int SHADE_SMOOTH    = 165;
	public static final int WINDING_CCW     = 168;
	public static final int WINDING_CW      = 169;

	// The current polygon culling mode.
	private int m_culling;
	// The current polygon shading mode.
	private int m_shading;
	// The current polygon winding mode.
	private int m_winding;
	// Flag indicating two-sided lighting is enabled.
	private boolean m_twoSidedLightingEnabled;
	// Flag indicating local camera lighting is enabled.
	private boolean m_localCameraLightingEnabled;
	// Flag indicating perspective correction is enabled.
	private boolean m_perspectiveCorrectionEnabled;

    ////////// Methods part of M3G Specification //////////
	
	public PolygonMode()
	{
		m_culling = CULL_BACK;
		m_winding = WINDING_CCW;
		m_shading = SHADE_SMOOTH;
		m_twoSidedLightingEnabled = false;
		m_localCameraLightingEnabled = false;
		m_perspectiveCorrectionEnabled = false;
	}

	public void setCulling(int mode)
	{
		if (mode < CULL_BACK || mode > CULL_NONE)
			new IllegalArgumentException("PolygonMode: mode is not one of CULL_BACK, CULL_FRONT, CULL_NONE");
		
		m_culling = mode;
	}

	public int getCulling()
	{
		return m_culling;
	}

	public void setWinding(int mode)
	{
		if (mode < WINDING_CCW || mode > WINDING_CW)
			new IllegalArgumentException("PolygonMode: mode is not one of WINDING_CCW, WINDING_CW");
		
		m_winding = mode;
	}

	public int getWinding()
	{
		return m_winding;
	}

	public void setShading(int mode)
	{
		if (mode < SHADE_FLAT || mode > SHADE_SMOOTH)
			new IllegalArgumentException("PolygonMode: mode is not one of SHADE_FLAT, SHADE_SMOOTH");

		m_shading = mode;
	}

	public int getShading()
	{
		return m_shading;
	}

	public void setTwoSidedLightingEnable(boolean enable)
	{
		m_twoSidedLightingEnabled = enable;
	}

	public boolean isTwoSidedLightingEnabled()
	{
		return m_twoSidedLightingEnabled;
	}

	public void setLocalCameraLightingEnable(boolean enable)
	{
		m_localCameraLightingEnabled = enable;
	}

	public boolean isLocalCameraLightingEnabled()
	{
		return m_localCameraLightingEnabled;
	}

	public void setPerspectiveCorrectionEnable(boolean enable)
	{
		m_perspectiveCorrectionEnabled = enable;
	}

	public boolean isPerspectiveCorrectionEnabled()
	{
		return m_perspectiveCorrectionEnabled;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////
	
	public int getObjectType()
	{
		return POLYGON_MODE;
	}

	protected void unmarshall(M3GInputStream is,ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		setCulling(is.readByte());
		setShading(is.readByte());
		setWinding(is.readByte());
		setTwoSidedLightingEnable(is.readBoolean());
		setLocalCameraLightingEnable(is.readBoolean());
		setPerspectiveCorrectionEnable(is.readBoolean());
	}

	protected void marshall(M3GOutputStream os,ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeByte(m_culling);
		os.writeByte(m_shading);
		os.writeByte(m_winding);
		os.writeBoolean(m_twoSidedLightingEnabled);
		os.writeBoolean(m_localCameraLightingEnabled);
		os.writeBoolean(m_perspectiveCorrectionEnabled);
	}
	
	void setupGL(GL gl)
	{
		// Setup shading.
		if (m_shading == SHADE_SMOOTH)
			gl.glShadeModel(GL.GL_SMOOTH);
		else
			gl.glShadeModel(GL.GL_FLAT);

		// Setup culling.
		if (m_culling == CULL_NONE)
			gl.glDisable(GL.GL_CULL_FACE);
		else
		{
			gl.glEnable(GL.GL_CULL_FACE);
			if (m_culling == CULL_BACK)
				gl.glCullFace(GL.GL_BACK);
			else
				gl.glCullFace(GL.GL_FRONT);
		}

		// Setup winding.
		if (m_winding == WINDING_CCW)
			gl.glFrontFace(GL.GL_CCW);
		else
			gl.glFrontFace(GL.GL_CW);
	}
	
	int getLightTarget()
	{
		if (isTwoSidedLightingEnabled())
			return GL.GL_FRONT_AND_BACK;
		else
			return GL.GL_FRONT;
	}
}
