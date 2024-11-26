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
import java.util.logging.*;

// Import JOGL classes.
import javax.media.opengl.*;

// Import M3G Toolkit classes.
import com.wizzer.m3g.math.Constants;
import com.wizzer.m3g.math.Vector3;
import com.wizzer.m3g.math.Vector4;

public class Transform
{
	// The matrix is stored in row-major format i.e
	//
	//  0  1  2  3
	//  4  5  6  7
	//  8  9 10 11
	// 12 13 14 15

	private float m_matrix[];
	private static float IDENTITY[] = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};

    ////////// Methods part of M3G Specification //////////
	
	public Transform()
	{
		m_matrix = new float[16];
		setIdentity();
	}

	public Transform(Transform transform)
	{
		m_matrix = new float[16];
		set(transform);
	}

	public void setIdentity()
	{
		set(IDENTITY);
	}

	public void set(Transform transform)
	{
		float matrix[] = new float[16];
		transform.get(matrix);
		set(matrix);
	}

	public void set(float matrix[])
	{
		if (matrix.length < 16)
			throw new IllegalArgumentException("Transform: matrix.length < 16");
		System.arraycopy(matrix, 0, this.m_matrix, 0, 16);
	}

	public void get(float matrix[])
	{
		if (matrix.length < 16)
			throw new IllegalArgumentException("Transform: matrix.length < 16");
		System.arraycopy(this.m_matrix, 0, matrix, 0, 16);
	}

	public void invert()
	{
		//	float det = determinant();
		//	if (det == 0)
		//		throw new ArithmeticException("This transform is not invertible");
			
		// This will only work for ON-matrices, but it's really fast!
		float[] n = {
				m_matrix[0], m_matrix[4], m_matrix[ 8], -m_matrix[0]*m_matrix[3] - m_matrix[4]*m_matrix[7] - m_matrix[ 8]*m_matrix[11],
				m_matrix[1], m_matrix[5], m_matrix[ 9], -m_matrix[1]*m_matrix[3] - m_matrix[5]*m_matrix[7] - m_matrix[ 9]*m_matrix[11],
				m_matrix[2], m_matrix[6], m_matrix[10], -m_matrix[2]*m_matrix[3] - m_matrix[6]*m_matrix[7] - m_matrix[10]*m_matrix[11],
				0, 0, 0, 1};
		set(n);
	}

	public void transpose()
	{
		float t = m_matrix[1];
		m_matrix[1] = m_matrix[4];
		m_matrix[4] = t;

		t = m_matrix[2];
		m_matrix[2] = m_matrix[8];
		m_matrix[8] = t;

		t = m_matrix[3];
		m_matrix[12] = m_matrix[3];
		m_matrix[3] = t;

		t = m_matrix[6];
		m_matrix[6] = m_matrix[9];
		m_matrix[9] = t;
		
		t = m_matrix[7];
		m_matrix[7] = m_matrix[13];
		m_matrix[13] = t;

		t = m_matrix[11];
		m_matrix[11] = m_matrix[14];
		m_matrix[14] = t;
	}

	public void postMultiply(Transform transform)
	{
		if (transform == null)
			throw new NullPointerException("Transform: transform can not be null");
		
		float[] l = new float[16];
		get(l);
		float[] r = transform.m_matrix;
		
		m_matrix[0] = l[0]*r[0] + l[1]*r[4] + l[2]*r[8] + l[3]*r[12];
		m_matrix[1] = l[0]*r[1] + l[1]*r[5] + l[2]*r[9] + l[3]*r[13];
		m_matrix[2] = l[0]*r[2] + l[1]*r[6] + l[2]*r[10] + l[3]*r[14];
		m_matrix[3] = l[0]*r[3] + l[1]*r[7] + l[2]*r[11] + l[3]*r[15];
		
		m_matrix[4] = l[4]*r[0] + l[5]*r[4] + l[6]*r[8] + l[7]*r[12];
		m_matrix[5] = l[4]*r[1] + l[5]*r[5] + l[6]*r[9] + l[7]*r[13];
		m_matrix[6] = l[4]*r[2] + l[5]*r[6] + l[6]*r[10] + l[7]*r[14];
		m_matrix[7] = l[4]*r[3] + l[5]*r[7] + l[6]*r[11] + l[7]*r[15];

		m_matrix[8] = l[8]*r[0] + l[9]*r[4] + l[10]*r[8] + l[11]*r[12];
		m_matrix[9] = l[8]*r[1] + l[9]*r[5] + l[10]*r[9] + l[11]*r[13];
		m_matrix[10] = l[8]*r[2] + l[9]*r[6] + l[10]*r[10] + l[11]*r[14];
		m_matrix[11] = l[8]*r[3] + l[9]*r[7] + l[10]*r[11] + l[11]*r[15];

		m_matrix[12] = l[12]*r[0] + l[13]*r[4] + l[14]*r[8] + l[15]*r[12];
		m_matrix[13] = l[12]*r[1] + l[13]*r[5] + l[14]*r[9] + l[15]*r[13];
		m_matrix[14] = l[12]*r[2] + l[13]*r[6] + l[14]*r[10] + l[15]*r[14];
		m_matrix[15] = l[12]*r[3] + l[13]*r[7] + l[14]*r[11] + l[15]*r[15];
	}

	public void postScale(float sx,float sy,float sz)
	{
		Transform t = new Transform();
		float[] m = t.m_matrix;
		m[0] = sx;
		m[5] = sy;
		m[10] = sz;
		
		postMultiply(t);
	}

	public void postRotate(float angle,float ax,float ay,float az)
	{
		Vector3 v = new Vector3(ax,ay,az);
		
		if (angle < 0.000001) // TODO: use constant
			return;
		
		if (ax == 0 && ay == 0 && az == 0)
			throw new IllegalArgumentException("Transform: length of rotation axis vector can not be 0");
		
		v.normalize();
		ax = v.x;
		ay = v.y;
		az = v.z;
		
		postMultiply(getRotationFromAngleAxis(angle, ax, ay, az));		
	}

	public void postRotateQuat(float qx,float qy,float qz,float qw)
	{
		if (qx == 0 && qy == 0 && qz == 0 && qw == 0)
			throw new IllegalArgumentException("Transform: at least one the components of the quaternion must be non zero");
		Vector4 v = new Vector4(qx, qy, qz, qw);
		v.normalize();
		
		qx = v.x;
		qy = v.y;
		qz = v.z;
		qw = v.w;
		
		postMultiply(getRotationFromQuaternion(qx, qy, qz, qw));
	}

	public void postTranslate(float tx,float ty,float tz)
	{
		Transform t = new Transform();
		float[] m = t.m_matrix;
		m[3] = tx;
		m[7] = ty;
		m[11] = tz;
		
		postMultiply(t);
	}

	public void transform(VertexArray in,float out[],boolean W)
	{
		if (in == null)
			throw new NullPointerException("in can not be null");
		if (out == null)
			throw new NullPointerException("out can not be null");
		if (out.length < in.getVertexCount()*4)
			throw new IllegalArgumentException("Transform: number of elements in out array must be at least vertexCount*4");
		
		int cc = in.getComponentCount();
		int vc = in.getVertexCount();
		
		if (in.getComponentSize() == 1)
		{
			byte[] values = new byte[vc*cc];
			in.get(0, vc, values);
			for (int i = 0, j = 0; i < vc*cc; i += cc, j += 4)
			{
				float x = values[i];
				float y = (cc >= 2 ? (float)values[i+1] : 0.0f);
				float z = (cc >= 3 ? (float)values[i+2] : 0.0f);
				float w = (cc >= 4 ? (float)values[i+3] : (W?1:0));

				out[i] = x*m_matrix[0] + y*m_matrix[1] + z*m_matrix[2] + w*m_matrix[3];
				out[i+1] = x*m_matrix[4] + y*m_matrix[5] + z*m_matrix[6] + w*m_matrix[7];
				out[i+2] = x*m_matrix[8] + y*m_matrix[9] + z*m_matrix[10] + w*m_matrix[11];
				out[i+3] = x*m_matrix[12] + y*m_matrix[13] + z*m_matrix[14] + w*m_matrix[15];
			}
		}
		else
		{
			short[] values = new short[vc*cc];
			in.get(0, vc, values);
			for (int i = 0, j = 0; i < vc*cc; i += cc, j += 4)
			{
				float x = values[i];
				float y = (cc >= 2 ? (float)values[i+1] : 0.0f);
				float z = (cc >= 3 ? (float)values[i+2] : 0.0f);
				float w = (cc >= 4 ? (float)values[i+3] : (W?1:0));

				out[j] = x*m_matrix[0] + y*m_matrix[1] + z*m_matrix[2] + w*m_matrix[3];
				out[j+1] = x*m_matrix[4] + y*m_matrix[5] + z*m_matrix[6] + w*m_matrix[7];
				out[j+2] = x*m_matrix[8] + y*m_matrix[9] + z*m_matrix[10] + w*m_matrix[11];
				out[j+3] = x*m_matrix[12] + y*m_matrix[13] + z*m_matrix[14] + w*m_matrix[15];
			}			
		}		
	}

	public void transform(float vectors[])
	{
		if (vectors == null)
			throw new NullPointerException("vectors can not be null");
		if ((vectors.length % 4) != 0)
			throw new IllegalArgumentException("Transform: number of elements in vector array must be a multiple of 4");
		
		int l = vectors.length;
		for (int i = 0; i < l; i += 4)
		{
			float x = vectors[i];
			float y = vectors[i+1];
			float z = vectors[i+2];
			float w = vectors[i+3];

			vectors[i] = x*m_matrix[0] + y*m_matrix[1] + z*m_matrix[2] + w*m_matrix[3];
			vectors[i+1] = x*m_matrix[4] + y*m_matrix[5] + z*m_matrix[6] + w*m_matrix[7];
			vectors[i+2] = x*m_matrix[8] + y*m_matrix[9] + z*m_matrix[10] + w*m_matrix[11];
			vectors[i+3] = x*m_matrix[12] + y*m_matrix[13] + z*m_matrix[14] + w*m_matrix[15];
		}
	}
	
	private Transform getRotationFromAngleAxis(float angle, float ax, float ay, float az)
	{
		Transform t = new Transform();
		float[] m = t.m_matrix;
		float c = (float)Math.cos(angle*Constants.TO_RADIANS);
		float s = (float)Math.sin(angle*Constants.TO_RADIANS);
		float nC = 1-c;
		
		float xy = ax*ay;
		float yz = ay*az;
		float xz = ax*az;
					
		float xs = ax*s;
		float zs = az*s;
		float ys = ay*s;
		
		m[0] = ax*ax*nC + c;
		m[1] = xy*nC - zs;
		m[2] = xz*nC + ys;
		
		m[4] = xy*nC + zs;
		m[5] = ay*ay*nC + c;
		m[6] = yz*nC - xs;

		m[8] = xz*nC - ys;
		m[9] = yz*nC + xs;
		m[10] = az*az*nC + c;
		
		return t;
	}
	
	private Transform getRotationFromQuaternion(float x, float y, float z, float w)
	{
		Transform t = new Transform();
		float[] m = t.m_matrix;

		float xx = 2*x*x;
		float yy = 2*y*y;
		float zz = 2*z*z;
		float xy = 2*x*y;
		float xz = 2*x*z;
		float xw = 2*x*w;
		float yz = 2*y*z;
		float yw = 2*y*w;
		float zw = 2*z*w;
		
		m[0] = 1 - yy + zz;
		m[1] = xy - zw;
		m[2] = xz + yw;
		
		m[4] = xy + zw;
		m[5] = 1 - xx + zz;
		m[6] = yz - xw;
		
		m[8] = xz - yw;
		m[9] = yz + xw;
		m[10] = 1 - xx + yy;
		
		return t;
	}
	
	void setGL(GL gl)
	{
		gl.glLoadTransposeMatrixf(m_matrix, 0);
	}
	
	void multGL(GL gl)
	{
		gl.glMultTransposeMatrixf(m_matrix,0);
	}
	
	void getGL(GL gl, int matrixMode)
	{
		gl.glGetFloatv(matrixMode, m_matrix, 0);
		transpose();
	}
	
	public String toString()
	{
		String ret = "{";
		for (int i = 0; i < 16; ++i)
		{
			if ((i%4) == 0 && i > 0)
				ret += "\n ";
			ret += m_matrix[i] + ", ";
		}
		return ret + "}";
	}
}