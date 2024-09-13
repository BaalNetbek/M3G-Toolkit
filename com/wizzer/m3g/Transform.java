package com.wizzer.m3g;

import com.wizzer.m3g.math.Vector3;
import com.wizzer.m3g.math.Vector4;
import javax.media.opengl.GL;

public class Transform {
   private float[] m_matrix = new float[16];
   private static float[] IDENTITY = new float[]
   {
      1.0F, 0.0F, 0.0F, 0.0F,
      0.0F, 1.0F, 0.0F, 0.0F,
      0.0F, 0.0F, 1.0F, 0.0F,
      0.0F, 0.0F, 0.0F, 1.0F
   };

   public Transform() {
      this.setIdentity();
   }

   public Transform(Transform transform) {
      this.set(transform);
   }

   public void setIdentity() {
      this.set(IDENTITY);
   }

   public void set(Transform transform) {
      float[] matrix = new float[16];
      transform.get(matrix);
      this.set(matrix);
   }

   public void set(float[] matrix) {
      if (matrix.length < 16) {
         throw new IllegalArgumentException("Transform: matrix.length < 16");
      } else {
         System.arraycopy(matrix, 0, this.m_matrix, 0, 16);
      }
   }

   public void get(float[] matrix) {
      if (matrix.length < 16) {
         throw new IllegalArgumentException("Transform: matrix.length < 16");
      } else {
         System.arraycopy(this.m_matrix, 0, matrix, 0, 16);
      }
   }

   public void invert() {
      float[] n = new float[]
      {
         this.m_matrix[0], this.m_matrix[4], this.m_matrix[8], -this.m_matrix[0] * this.m_matrix[3] - this.m_matrix[4] * this.m_matrix[7] - this.m_matrix[8] * this.m_matrix[11],
         this.m_matrix[1], this.m_matrix[5], this.m_matrix[9], -this.m_matrix[1] * this.m_matrix[3] - this.m_matrix[5] * this.m_matrix[7] - this.m_matrix[9] * this.m_matrix[11],
         this.m_matrix[2], this.m_matrix[6], this.m_matrix[10], -this.m_matrix[2] * this.m_matrix[3] - this.m_matrix[6] * this.m_matrix[7] - this.m_matrix[10] * this.m_matrix[11],
         0.0F, 0.0F, 0.0F, 1.0F
      };
      this.set(n);
   }

   public void transpose() {
      float t = this.m_matrix[1];
      this.m_matrix[1] = this.m_matrix[4];
      this.m_matrix[4] = t;
      t = this.m_matrix[2];
      this.m_matrix[2] = this.m_matrix[8];
      this.m_matrix[8] = t;
      t = this.m_matrix[3];
      this.m_matrix[12] = this.m_matrix[3];
      this.m_matrix[3] = t;
      t = this.m_matrix[6];
      this.m_matrix[6] = this.m_matrix[9];
      this.m_matrix[9] = t;
      t = this.m_matrix[7];
      this.m_matrix[7] = this.m_matrix[13];
      this.m_matrix[13] = t;
      t = this.m_matrix[11];
      this.m_matrix[11] = this.m_matrix[14];
      this.m_matrix[14] = t;
   }

   public void postMultiply(Transform transform) {
      if (transform == null) {
         throw new NullPointerException("Transform: transform can not be null");
      } else {
         float[] l = new float[16];
         this.get(l);
         float[] r = transform.m_matrix;
         this.m_matrix[0] = l[0] * r[0] + l[1] * r[4] + l[2] * r[8] + l[3] * r[12];
         this.m_matrix[1] = l[0] * r[1] + l[1] * r[5] + l[2] * r[9] + l[3] * r[13];
         this.m_matrix[2] = l[0] * r[2] + l[1] * r[6] + l[2] * r[10] + l[3] * r[14];
         this.m_matrix[3] = l[0] * r[3] + l[1] * r[7] + l[2] * r[11] + l[3] * r[15];
         this.m_matrix[4] = l[4] * r[0] + l[5] * r[4] + l[6] * r[8] + l[7] * r[12];
         this.m_matrix[5] = l[4] * r[1] + l[5] * r[5] + l[6] * r[9] + l[7] * r[13];
         this.m_matrix[6] = l[4] * r[2] + l[5] * r[6] + l[6] * r[10] + l[7] * r[14];
         this.m_matrix[7] = l[4] * r[3] + l[5] * r[7] + l[6] * r[11] + l[7] * r[15];
         this.m_matrix[8] = l[8] * r[0] + l[9] * r[4] + l[10] * r[8] + l[11] * r[12];
         this.m_matrix[9] = l[8] * r[1] + l[9] * r[5] + l[10] * r[9] + l[11] * r[13];
         this.m_matrix[10] = l[8] * r[2] + l[9] * r[6] + l[10] * r[10] + l[11] * r[14];
         this.m_matrix[11] = l[8] * r[3] + l[9] * r[7] + l[10] * r[11] + l[11] * r[15];
         this.m_matrix[12] = l[12] * r[0] + l[13] * r[4] + l[14] * r[8] + l[15] * r[12];
         this.m_matrix[13] = l[12] * r[1] + l[13] * r[5] + l[14] * r[9] + l[15] * r[13];
         this.m_matrix[14] = l[12] * r[2] + l[13] * r[6] + l[14] * r[10] + l[15] * r[14];
         this.m_matrix[15] = l[12] * r[3] + l[13] * r[7] + l[14] * r[11] + l[15] * r[15];
      }
   }

   public void postScale(float sx, float sy, float sz) {
      Transform t = new Transform();
      float[] m = t.m_matrix;
      m[0] = sx;
      m[5] = sy;
      m[10] = sz;
      this.postMultiply(t);
   }

   public void postRotate(float angle, float ax, float ay, float az) {
      Vector3 v = new Vector3(ax, ay, az);
      if (!((double)angle < 1.0E-6D)) {
         if (ax == 0.0F && ay == 0.0F && az == 0.0F) {
            throw new IllegalArgumentException("Transform: length of rotation axis vector can not be 0");
         } else {
            v.normalize();
            ax = v.x;
            ay = v.y;
            az = v.z;
            this.postMultiply(this.getRotationFromAngleAxis(angle, ax, ay, az));
         }
      }
   }

   public void postRotateQuat(float qx, float qy, float qz, float qw) {
      if (qx == 0.0F && qy == 0.0F && qz == 0.0F && qw == 0.0F) {
         throw new IllegalArgumentException("Transform: at least one the components of the quaternion must be non zero");
      } else {
         Vector4 v = new Vector4(qx, qy, qz, qw);
         v.normalize();
         qx = v.x;
         qy = v.y;
         qz = v.z;
         qw = v.w;
         this.postMultiply(this.getRotationFromQuaternion(qx, qy, qz, qw));
      }
   }

   public void postTranslate(float tx, float ty, float tz) {
      Transform t = new Transform();
      float[] m = t.m_matrix;
      m[3] = tx;
      m[7] = ty;
      m[11] = tz;
      this.postMultiply(t);
   }

   public void transform(VertexArray in, float[] out, boolean W) {
      if (in == null) {
         throw new NullPointerException("in can not be null");
      } else if (out == null) {
         throw new NullPointerException("out can not be null");
      } else if (out.length < in.getVertexCount() * 4) {
         throw new IllegalArgumentException("Transform: number of elements in out array must be at least vertexCount*4");
      } else {
         int cc = in.getComponentCount();
         int vc = in.getVertexCount();
         int i;
         int j;
         float x;
         float y;
         float z;
         float w;
         if (in.getComponentSize() == 1) {
            byte[] values = new byte[vc * cc];
            in.get(0, vc, (byte[])values);
            i = 0;

            for(j = 0; i < vc * cc; j += 4) {
               x = (float)values[i];
               y = cc >= 2 ? (float)values[i + 1] : 0.0F;
               z = cc >= 3 ? (float)values[i + 2] : 0.0F;
               w = cc >= 4 ? (float)values[i + 3] : (float)(W ? 1 : 0);
               out[i] = x * this.m_matrix[0] + y * this.m_matrix[1] + z * this.m_matrix[2] + w * this.m_matrix[3];
               out[i + 1] = x * this.m_matrix[4] + y * this.m_matrix[5] + z * this.m_matrix[6] + w * this.m_matrix[7];
               out[i + 2] = x * this.m_matrix[8] + y * this.m_matrix[9] + z * this.m_matrix[10] + w * this.m_matrix[11];
               out[i + 3] = x * this.m_matrix[12] + y * this.m_matrix[13] + z * this.m_matrix[14] + w * this.m_matrix[15];
               i += cc;
            }
         } else {
            short[] values = new short[vc * cc];
            in.get(0, vc, (short[])values);
            i = 0;

            for(j = 0; i < vc * cc; j += 4) {
               x = (float)values[i];
               y = cc >= 2 ? (float)values[i + 1] : 0.0F;
               z = cc >= 3 ? (float)values[i + 2] : 0.0F;
               w = cc >= 4 ? (float)values[i + 3] : (float)(W ? 1 : 0);
               out[j] = x * this.m_matrix[0] + y * this.m_matrix[1] + z * this.m_matrix[2] + w * this.m_matrix[3];
               out[j + 1] = x * this.m_matrix[4] + y * this.m_matrix[5] + z * this.m_matrix[6] + w * this.m_matrix[7];
               out[j + 2] = x * this.m_matrix[8] + y * this.m_matrix[9] + z * this.m_matrix[10] + w * this.m_matrix[11];
               out[j + 3] = x * this.m_matrix[12] + y * this.m_matrix[13] + z * this.m_matrix[14] + w * this.m_matrix[15];
               i += cc;
            }
         }

      }
   }

   public void transform(float[] vectors) {
      if (vectors == null) {
         throw new NullPointerException("vectors can not be null");
      } else if (vectors.length % 4 != 0) {
         throw new IllegalArgumentException("Transform: number of elements in vector array must be a multiple of 4");
      } else {
         int l = vectors.length;

         for(int i = 0; i < l; i += 4) {
            float x = vectors[i];
            float y = vectors[i + 1];
            float z = vectors[i + 2];
            float w = vectors[i + 3];
            vectors[i] = x * this.m_matrix[0] + y * this.m_matrix[1] + z * this.m_matrix[2] + w * this.m_matrix[3];
            vectors[i + 1] = x * this.m_matrix[4] + y * this.m_matrix[5] + z * this.m_matrix[6] + w * this.m_matrix[7];
            vectors[i + 2] = x * this.m_matrix[8] + y * this.m_matrix[9] + z * this.m_matrix[10] + w * this.m_matrix[11];
            vectors[i + 3] = x * this.m_matrix[12] + y * this.m_matrix[13] + z * this.m_matrix[14] + w * this.m_matrix[15];
         }

      }
   }

   private Transform getRotationFromAngleAxis(float angle, float ax, float ay, float az) {
      Transform t = new Transform();
      float[] m = t.m_matrix;
      float c = (float)Math.cos((double)(angle * 0.017453292F));
      float s = (float)Math.sin((double)(angle * 0.017453292F));
      float nC = 1.0F - c;
      float xy = ax * ay;
      float yz = ay * az;
      float xz = ax * az;
      float xs = ax * s;
      float zs = az * s;
      float ys = ay * s;
      m[0] = ax * ax * nC + c;
      m[1] = xy * nC - zs;
      m[2] = xz * nC + ys;
      m[4] = xy * nC + zs;
      m[5] = ay * ay * nC + c;
      m[6] = yz * nC - xs;
      m[8] = xz * nC - ys;
      m[9] = yz * nC + xs;
      m[10] = az * az * nC + c;
      return t;
   }

   private Transform getRotationFromQuaternion(float x, float y, float z, float w) {
      Transform t = new Transform();
      float[] m = t.m_matrix;
      float xx = 2.0F * x * x;
      float yy = 2.0F * y * y;
      float zz = 2.0F * z * z;
      float xy = 2.0F * x * y;
      float xz = 2.0F * x * z;
      float xw = 2.0F * x * w;
      float yz = 2.0F * y * z;
      float yw = 2.0F * y * w;
      float zw = 2.0F * z * w;
      m[0] = 1.0F - yy + zz;
      m[1] = xy - zw;
      m[2] = xz + yw;
      m[4] = xy + zw;
      m[5] = 1.0F - xx + zz;
      m[6] = yz - xw;
      m[8] = xz - yw;
      m[9] = yz + xw;
      m[10] = 1.0F - xx + yy;
      return t;
   }

   void setGL(GL gl) {
      gl.glLoadTransposeMatrixf(this.m_matrix, 0);
   }

   void multGL(GL gl) {
      gl.glMultTransposeMatrixf(this.m_matrix, 0);
   }

   void getGL(GL gl, int matrixMode) {
      gl.glGetFloatv(matrixMode, this.m_matrix, 0);
      this.transpose();
   }

   public String toString() {
      String ret = "{";

      for(int i = 0; i < 16; ++i) {
         if (i % 4 == 0 && i > 0) {
            ret = ret + "\n ";
         }

         ret = ret + this.m_matrix[i] + ", ";
      }

      return ret + "}";
   }
}
