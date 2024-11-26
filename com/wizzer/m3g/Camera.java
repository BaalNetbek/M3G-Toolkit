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

// Include M3G Toolkit classes.
import com.wizzer.m3g.math.Constants;

public class Camera extends Node
{
	public static final byte GENERIC        = 48;
	public static final byte PARALLEL       = 49;
	public static final byte PERSPECTIVE    = 50;

	// The projection type.
	private int m_projectionType;
	// The current projection matrix.
    private Transform m_projectionMatrix;
    // The field-of-view in the vertical direction.
	private float m_fovy;
	// The aspect ratio of the viewport.
	private float m_aspectRatio;
	// The distance to the front clipping plane.
	private float m_near;
	// The distance to the far clipping plane
	private float m_far;

    ////////// Methods part of M3G Specification //////////
	
	public Camera()
	{
		m_projectionType = PARALLEL;
		m_near = 0.1f;
		m_far = 1f;
	}

	public void setParallel(float fovy, float aspectRatio, float near, float far)
	{
		if (fovy <= 0)
			throw new IllegalArgumentException("Camera: fovy <= 0");
		if (aspectRatio <= 0)
			throw new IllegalArgumentException("Camera: aspectRatio <= 0");
		
		m_projectionType = PARALLEL;
		m_fovy = fovy;
		m_aspectRatio = aspectRatio;
		m_near = near;
		m_far = far;
	}

	public void setPerspective(float fovy, float aspectRatio, float near, float far)
	{
		if (fovy <= 0)
			throw new IllegalArgumentException("Camera: fovy <= 0");
		if (aspectRatio <= 0)
			throw new IllegalArgumentException("Camera: aspectRatio <= 0");
		if (near <= 0)
			throw new IllegalArgumentException("Camera: near <= 0");
		if (far <= 0)
			throw new IllegalArgumentException("Camera: far <= 0");
		if (fovy >= 180)
			throw new IllegalArgumentException("Camera: fovy >= 180");
	
		m_projectionType = PERSPECTIVE;
		m_fovy = fovy;
		m_aspectRatio = aspectRatio;
		m_near = near;
		m_far = far;
	}

	public void setGeneric(Transform transform)
	{
		m_projectionType = GENERIC;
		m_projectionMatrix = new Transform(transform);
	}

	public int getProjection(Transform transform)
	{
		if (transform != null)
		{
			if (m_projectionType == GENERIC)
			{
				transform.set(m_projectionMatrix);
			}
			else if (m_projectionType == PARALLEL)
			{
				if (m_far == m_near)
					throw new ArithmeticException("Camera: unable to compute projection matrix. Illegal parameters (near == far).");
					
				float[] m = new float[16];
				m[1] = m[2] = m[3] = m[4] = m[6] = m[7] = m[8] = m[9] = m[12] = m[13] = m[14] = 0;
				m[0] = 2 / (m_aspectRatio * m_fovy);
				m[5] = 2 / m_fovy;
				m[10] = -2 / (m_far - m_near);
				m[11] = -(m_near + m_far) / (m_far - m_near);
				m[15] = 1;
				transform.set(m);
			}
			else if (m_projectionType == PERSPECTIVE)
			{
				if (m_far == m_near)
					throw new ArithmeticException("Camera: unable to compute projection matrix. Illegal parameters (near == far).");
				
				float h = (float)Math.tan(m_fovy * Constants.TO_RADIANS / 2);
				
				float[] m = new float[16];
				m[1] = m[2] = m[3] = m[4] = m[6] = m[7] = m[8] = m[9] = m[12] = m[13] = m[14] = 0;
				m[0] = 1 / (m_aspectRatio * h);
				m[5] = 1 / h;
				m[10] = -(m_near + m_far) / (m_far - m_near);
				m[11] = -2 * m_near * m_far / (m_far - m_near);
				m[14] = -1;
				m[15] = 0;
				transform.set(m);
			}
		}
		return m_projectionType;
	}

	public int getProjection(float params[])
	{
		if (params != null && m_projectionType != GENERIC)
		{
			if (params.length < 4)
				throw new IllegalArgumentException("Camera: (params != null) &&  (params.length < 4)");
			
			params[0] = m_fovy;
			params[1] = m_aspectRatio;
			params[2] = m_near;
			params[3] = m_far;
		}
		return m_projectionType;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}

	////////// Methods not part of M3G Specification //////////

	public int getObjectType()
	{
		return CAMERA;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		m_projectionType = is.readByte();
		if (m_projectionType < GENERIC || m_projectionType > PERSPECTIVE)
			throw new IOException("Camera:projectionType = " + m_projectionType);
		if (m_projectionType == PARALLEL)
			setParallel(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
		else if (m_projectionType == PERSPECTIVE)
			setPerspective(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
		else setGeneric(is.readMatrix());
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		os.writeByte(m_projectionType);
		if (m_projectionType != GENERIC)
		{
			os.writeFloat32(m_fovy);
			os.writeFloat32(m_aspectRatio);
			os.writeFloat32(m_near);
			os.writeFloat32(m_far);
		}
		else os.writeMatrix(m_projectionMatrix);
	}
}
