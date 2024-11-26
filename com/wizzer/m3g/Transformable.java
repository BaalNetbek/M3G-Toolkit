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

// Import M3G Toolkit classes.
import com.wizzer.m3g.math.Vector3;


public abstract class Transformable extends Object3D
{
	private static float SCALE_DEFAULT[]        = new float[]{1f,1f,1f};
	private static float TRANSLATION_DEFAULT[]  = new float[]{0f,0f,0f};
	private static float ORIENTATION_DEFAULT[]  = new float[]{0f,0f,0f};
	private static float TRANSFORM_DEFAULT[]    = new float[]{1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f};

	private boolean m_hasComponentTransform;
	private boolean m_hasGeneralTransform;
	private float m_scale[];
	private float m_translation[];
	private Transform m_orientation;
	private Transform m_transform;

    ////////// Methods part of M3G Specification //////////
	
	protected Transformable()
	{
		m_scale = (float[])SCALE_DEFAULT.clone();
		m_translation = (float[])TRANSLATION_DEFAULT.clone();
		m_orientation = new Transform();
		m_transform = new Transform();
		setScale(1,1,1);
	}

	public void setOrientation(float angle, float ax, float ay, float az)
	{
		if (ax == 0 && ay == 0 && az == 0 && angle != 0)
			throw new IllegalArgumentException("Transformable: the rotation axis (ax ay az) is zero and angle is nonzero");
		
		m_orientation.setIdentity();
		m_orientation.postRotate(angle, ax, ay, az);
	}

	public void preRotate(float angle, float ax, float ay, float az)
	{
		Transform t = new Transform();
		t.postRotate(angle, ax, ay, az);
		t.postMultiply(m_orientation);
		m_orientation.set(t);
	}

	public void postRotate(float angle, float ax, float ay, float az)
	{
		m_orientation.postRotate(angle, ax, ay, az);
	}

	public void getOrientation(float angleAxis[])
	{
		if (angleAxis == null)
			throw new NullPointerException("Transformable: angleAxis can not be null");
		if (angleAxis.length < 4)
			throw new IllegalArgumentException("Transformable: length must be greater than 3");
		
		float[] m = new float[16];
        m_orientation.get(m);

		
		Vector3 axis = new Vector3(m[6]-m[9], m[8]-m[2], m[1]-m[4]);
		try {
			axis.normalize();
		} catch (ArithmeticException ex) {} // Axis may be zero vector
		
		float angle = (float)Math.acos(0.5*(m[0] + m[5] + m[10] - 1)); 
		
		angleAxis[0] = angle;
		angleAxis[1] = axis.x;
		angleAxis[2] = axis.y;
		angleAxis[3] = axis.z;
		
		// TODO: Handle singularities for angle = 0 and angle = 180 degrees
		if ((angle == 0) || (angle == 180))
			Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Transformable", "getOrientation(float angleAxis[])", "Singularities not implemented");
	}

	public void setScale(float sx,float sy,float sz)
	{
		m_scale[0] = sx;
		m_scale[1] = sy;
		m_scale[2] = sz;
	}

	public void scale(float sx,float sy,float sz)
	{
		m_scale[0] *= sx;
		m_scale[1] *= sy;
		m_scale[2] *= sz;
	}

	public void getScale(float xyz[])
	{
		if (xyz.length < 3)
			throw new IllegalArgumentException("xyz.length < 3");
		
		System.arraycopy(m_scale,0,xyz,0,m_scale.length);
	}

	public void setTranslation(float tx, float ty, float tz)
	{
		m_translation[0] = tx;
		m_translation[1] = ty;
		m_translation[2] = tz;
	}

	public void translate(float tx, float ty, float tz)
	{
		m_translation[0] += tx;
		m_translation[1] += ty;
		m_translation[2] += tz;
	}

	public void getTranslation(float xyz[])
	{
		if (xyz.length < 3)
			throw new IllegalArgumentException("Transformable: xyz.length < 3");
		
		System.arraycopy(m_translation, 0, xyz, 0, m_translation.length);
	}

	public void setTransform(Transform transform)
	{
		if (transform == null)
			throw new NullPointerException("Transformable: transform can not be null");
		
		this.m_transform.set(transform);
	}

	public void getTransform(Transform transform)
	{
		if (transform == null)
			throw new NullPointerException("Transformable: transform can not be null");

		transform.set(this.m_transform);
	}

	public void getCompositeTransform(Transform transform)
	{
		if (transform == null)
			throw new NullPointerException("Transformable: transform can not be null");
		
		// transform = T R S M
		
		// Combine translation and rotation (TR)
		float[] m = new float[16];
		m_orientation.get(m);
		m[3] = m_translation[0];  // x
		m[7] = m_translation[1];  // y
		m[11] = m_translation[2]; // z
		transform.set(m);
		
		// Apply scale (S)
		transform.postScale(m_scale[0], m_scale[1], m_scale[2]);
		
		// Apply custom (M)
		transform.postMultiply(this.m_transform);
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	public boolean hasComponentTransform()
	{
		return m_hasComponentTransform;
	}

	public boolean hasGeneralTransform()
	{
		return m_hasGeneralTransform;
	}

	// Note: there is no Object type for Transformable.

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		m_hasComponentTransform = is.readBoolean();
		if (m_hasComponentTransform)
		{
			setTranslation(is.readFloat32(), is.readFloat32(), is.readFloat32());
			setScale(is.readFloat32(), is.readFloat32(), is.readFloat32());
			setOrientation(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
		}
		m_hasGeneralTransform = is.readBoolean();
		if (m_hasGeneralTransform)
			m_transform.set(is.readMatrix());
	}

	protected void marshall(M3GOutputStream os,ArrayList table) throws IOException
	{
		super.marshall(os,table);

		float[] angleAxis = new float[4];
		float[] orientation = new float[3];
		getOrientation(angleAxis);
		orientation[0] = angleAxis[1];
		orientation[1] = angleAxis[2];
		orientation[2] = angleAxis[3];

		m_hasComponentTransform = ((angleAxis[0] != 0) ||
				!(Arrays.equals(m_translation,TRANSLATION_DEFAULT) &&
				Arrays.equals(m_scale,SCALE_DEFAULT) &&
				Arrays.equals(orientation,ORIENTATION_DEFAULT)));
		os.writeBoolean(m_hasComponentTransform);
		if (m_hasComponentTransform)
		{
			os.writeVector3D(m_translation);
			os.writeVector3D(m_scale);
			os.writeFloat32(angleAxis[0]);
			os.writeVector3D(orientation);
		}
		float f[] = new float[16];
		m_transform.get(f);
		m_hasGeneralTransform =! Arrays.equals(f,TRANSFORM_DEFAULT);
		os.writeBoolean(m_hasGeneralTransform);
		if (m_hasGeneralTransform) os.writeMatrix(m_transform);
	}
}