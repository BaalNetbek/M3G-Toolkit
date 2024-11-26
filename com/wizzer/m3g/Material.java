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

public class Material extends Object3D
{
	public static final int AMBIENT     = 1024;
	public static final int DIFFUSE 	= 2048;
	public static final int EMISSIVE 	= 4096;
	public static final int SPECULAR 	= 8192;

	// The material's ambient color.
	private int m_ambientColor;
	// The material's diffuse color.
	private int m_diffuseColor;
	// The material's emissive color.
	private int m_emissiveColor;
	// The material's specular color.
	private int m_specularColor;
	// The material's shininess.
	private float m_shininess;
	// Flag indicating whether vertex color tracking is enabled.
	private boolean m_vertexColorTrackingEnabled;

    ////////// Methods part of M3G Specification //////////
	
	/**
	 * The default constructor.
	 * <p>
	 * Creates a <code>Material</code> object with default values.
	 * </p>
	 */
	public Material()
	{
		m_vertexColorTrackingEnabled = false;
		m_ambientColor = 0x00333333;
		m_diffuseColor = 0xFFCCCCCC;
		m_emissiveColor = 0x00000000;
		m_specularColor = 0x00000000;
		m_shininess = 0.0f;
	}

	public void setColor(int target, int ARGB)
	{
		int ambient = (target & AMBIENT);
		int diffuse = (target & DIFFUSE);
		int emissive = (target & EMISSIVE);
		int specular = (target & SPECULAR);
		int total = (ambient | diffuse | emissive | specular);
		if (total == 0 || (total & target) != target)
			throw new IllegalArgumentException("Material: target has a value other than an inclusive OR of one or more of AMBIENT, DIFFUSE, EMISSIVE, SPECULAR");
		if (ambient != 0) m_ambientColor = ARGB;
		if (diffuse != 0) m_diffuseColor = ARGB;
		if (emissive != 0) m_emissiveColor = ARGB;
		if (specular != 0) m_specularColor = ARGB;
	}

	public int getColor(int target)
	{
		if (target == AMBIENT) return m_ambientColor;
		else if (target == DIFFUSE) return m_diffuseColor;
		else if (target == EMISSIVE) return m_emissiveColor;
		else if (target == SPECULAR) return m_specularColor;
		else throw new IllegalArgumentException("Material: target is not one of the symbolic constants");
	}

	public void setShininess(float shininess)
	{
		if (shininess < 0 || shininess > 128)
			throw new IllegalArgumentException("Material: shininess is not in [0, 128]");
		m_shininess=shininess;
	}

	public float getShininess()
	{
		return m_shininess;
	}

	public void setVertexColorTrackingEnabled(boolean enable)
	{
		m_vertexColorTrackingEnabled = enable;
	}

	public boolean isVertexColorTrackingEnabled()
	{
		return m_vertexColorTrackingEnabled;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////
	
	public int getObjectType()
	{
		return MATERIAL;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		setColor(AMBIENT,is.readColorRGB());
		setColor(DIFFUSE,is.readColorRGBA());
		setColor(EMISSIVE,is.readColorRGB());
		setColor(SPECULAR,is.readColorRGB());
		setShininess(is.readFloat32());
		setVertexColorTrackingEnabled(is.readBoolean());
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeColorRGB(m_ambientColor);
		os.writeColorRGBA(m_diffuseColor);
		os.writeColorRGB(m_emissiveColor);
		os.writeColorRGB(m_specularColor);
		os.writeFloat32(m_shininess);
		os.writeBoolean(m_vertexColorTrackingEnabled);
	}
	
	void setupGL(GL gl, int lightTarget)
	{
		gl.glEnable(GL.GL_LIGHTING);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, 
				GL.GL_EMISSION, 
				Color.intToFloatArray(m_emissiveColor), 
				0);
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, 
				GL.GL_AMBIENT, 
				Color.intToFloatArray(m_ambientColor), 
				0);

		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, 
				GL.GL_DIFFUSE, 
				Color.intToFloatArray(m_diffuseColor), 
				0);

		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, 
				GL.GL_SPECULAR, 
				Color.intToFloatArray(m_specularColor), 
				0);

		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, m_shininess);
	}
}
