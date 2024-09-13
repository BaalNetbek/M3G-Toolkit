package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class Camera extends Node {
   public static final byte GENERIC = 48;
   public static final byte PARALLEL = 49;
   public static final byte PERSPECTIVE = 50;
   private int m_projectionType = 49;
   private Transform m_projectionMatrix;
   private float m_fovy;
   private float m_aspectRatio;
   private float m_near = 0.1F;
   private float m_far = 1.0F;

   public void setParallel(float fovy, float aspectRatio, float near, float far) {
      if (fovy <= 0.0F) {
         throw new IllegalArgumentException("Camera: fovy <= 0");
      } else if (aspectRatio <= 0.0F) {
         throw new IllegalArgumentException("Camera: aspectRatio <= 0");
      } else {
         this.m_projectionType = 49;
         this.m_fovy = fovy;
         this.m_aspectRatio = aspectRatio;
         this.m_near = near;
         this.m_far = far;
      }
   }

   public void setPerspective(float fovy, float aspectRatio, float near, float far) {
      if (fovy <= 0.0F) {
         throw new IllegalArgumentException("Camera: fovy <= 0");
      } else if (aspectRatio <= 0.0F) {
         throw new IllegalArgumentException("Camera: aspectRatio <= 0");
      } else if (near <= 0.0F) {
         throw new IllegalArgumentException("Camera: near <= 0");
      } else if (far <= 0.0F) {
         throw new IllegalArgumentException("Camera: far <= 0");
      } else if (fovy >= 180.0F) {
         throw new IllegalArgumentException("Camera: fovy >= 180");
      } else {
         this.m_projectionType = 50;
         this.m_fovy = fovy;
         this.m_aspectRatio = aspectRatio;
         this.m_near = near;
         this.m_far = far;
      }
   }

   public void setGeneric(Transform transform) {
      this.m_projectionType = 48;
      this.m_projectionMatrix = new Transform(transform);
   }

   public int getProjection(Transform transform) {
      if (transform != null) {
         if (this.m_projectionType == 48) {
            transform.set(this.m_projectionMatrix);
         } else if (this.m_projectionType == 49) {
            if (this.m_far == this.m_near) {
               throw new ArithmeticException("Camera: unable to compute projection matrix. Illegal parameters (near == far).");
            }

            float[] m = new float[16];
            m[1] = m[2] = m[3] = m[4] = m[6] = m[7] = m[8] = m[9] = m[12] = m[13] = m[14] = 0.0F;
            m[0] = 2.0F / (this.m_aspectRatio * this.m_fovy);
            m[5] = 2.0F / this.m_fovy;
            m[10] = -2.0F / (this.m_far - this.m_near);
            m[11] = -(this.m_near + this.m_far) / (this.m_far - this.m_near);
            m[15] = 1.0F;
            transform.set(m);
         } else if (this.m_projectionType == 50) {
            if (this.m_far == this.m_near) {
               throw new ArithmeticException("Camera: unable to compute projection matrix. Illegal parameters (near == far).");
            }

            float h = (float)Math.tan((double)(this.m_fovy * 0.017453292F / 2.0F));
            float[] m = new float[16];
            m[1] = m[2] = m[3] = m[4] = m[6] = m[7] = m[8] = m[9] = m[12] = m[13] = m[14] = 0.0F;
            m[0] = 1.0F / (this.m_aspectRatio * h);
            m[5] = 1.0F / h;
            m[10] = -(this.m_near + this.m_far) / (this.m_far - this.m_near);
            m[11] = -2.0F * this.m_near * this.m_far / (this.m_far - this.m_near);
            m[14] = -1.0F;
            m[15] = 0.0F;
            transform.set(m);
         }
      }

      return this.m_projectionType;
   }

   public int getProjection(float[] params) {
      if (params != null && this.m_projectionType != 48) {
         if (params.length < 4) {
            throw new IllegalArgumentException("Camera: (params != null) &&  (params.length < 4)");
         }

         params[0] = this.m_fovy;
         params[1] = this.m_aspectRatio;
         params[2] = this.m_near;
         params[3] = this.m_far;
      }

      return this.m_projectionType;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 5;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_projectionType = is.readByte();
      if (this.m_projectionType >= 48 && this.m_projectionType <= 50) {
         if (this.m_projectionType == 49) {
            this.setParallel(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
         } else if (this.m_projectionType == 50) {
            this.setPerspective(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
         } else {
            this.setGeneric(is.readMatrix());
         }

      } else {
         throw new IOException("Camera:projectionType = " + this.m_projectionType);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeByte(this.m_projectionType);
      if (this.m_projectionType != 48) {
         os.writeFloat32(this.m_fovy);
         os.writeFloat32(this.m_aspectRatio);
         os.writeFloat32(this.m_near);
         os.writeFloat32(this.m_far);
      } else {
         os.writeMatrix(this.m_projectionMatrix);
      }

   }
}
