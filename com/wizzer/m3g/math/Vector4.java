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
package com.wizzer.m3g.math;

public class Vector4
{
	public float x, y, z, w;
		
	public Vector4()
	{
		x = 0;
		y = 0;
		z = 0;
		w = 0;
	}

	public Vector4(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector4(Vector4 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
		w = v.w;
	}
	
	public Vector4(float[] v)
	{
		if (v == null)
			throw new NullPointerException();
		if (v.length < 4)
			throw new IllegalArgumentException("Vector4: v must be of length 4 or grater");

		x = v[0];
		y = v[1];
		z = v[2];
		w = v[3];
	}

	public void set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void set(Vector4 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
		w = v.w;
	}

	public void set(float[] v)
	{
		if (v == null)
			throw new NullPointerException();
		if (v.length < 4)
			throw new IllegalArgumentException("Vector4: v must be of length 4 or greater");

		x = v[0];
		y = v[1];
		z = v[2];
		w = v[3];
	}

	public void add(Vector4 v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		w += v.w;
	}
	
	public void add(float scalar)
	{
		x += scalar;
		y += scalar;
		z += scalar;
		w += scalar;
	}
	
	public void add(Vector4 v, float scalar)
	{
		x = v.x + scalar;
		y = v.y + scalar;
		z = v.z + scalar;
		w = v.w + scalar;
	}
	
	public void add(Vector4 v1, Vector4 v2)
	{
		x = v1.x + v2.x;
		y = v1.y + v2.y;
		z = v1.z + v2.z;
		w = v1.w + v2.w;
	}

	public void subtract(Vector4 v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
		w -= v.w;
	}

	public void subtract(float scalar)
	{
		x -= scalar;
		y -= scalar;
		z -= scalar;
		w -= scalar;
	}

	public void subtract(Vector4 v,  float scalar)
	{
		x = v.x - scalar;
		y = v.y - scalar;
		z = v.z - scalar;
		w = v.w - scalar;
	}

	public void subtract(Vector4 v1, Vector4 v2)
	{
		x = v1.x - v2.x;
		y = v1.y - v2.y;
		z = v1.z - v2.z;
		w = v1.w - v2.w;
	}
	
	public void multiply(float scalar)
	{
		x *= scalar;
		y *= scalar;
		z *= scalar;
		w *= scalar;
	}

	public void multiply(Vector4 v, float scalar)
	{
		x = v.x * scalar;
		y = v.y * scalar;
		z = v.z * scalar;
		w = v.w * scalar;
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	public float length2()
	{
		return x*x + y*y + z*z + w*w;
	}

	public void normalize()
	{
		float length = length();
		if (length < Constants.EPSILON)
			throw new ArithmeticException("Vector4: can't normalize zero length vector");
		
		multiply(1.0f/length);
	}
	
	public float dot(Vector4 v)
	{
		return x*v.x + y*v.y + z*v.z + w*v.w;
	}
	
	public boolean equals(Vector4 v)
	{
		if (v == null)
			throw new NullPointerException();
		return (x == v.x && y == v.y && z==v.z && w == v.w);
	}
	
	public float[] toArray()
	{
		float[] a = {x,y,z,w};
		return a;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ", " + w+ ")";
	}
}

