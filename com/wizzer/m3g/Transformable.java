package com.wizzer.m3g;

import com.wizzer.m3g.math.Vector3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Transformable extends Object3D {
   private static float[] SCALE_DEFAULT = new float[]{1.0F, 1.0F, 1.0F};
   private static float[] TRANSLATION_DEFAULT = new float[]{0.0F, 0.0F, 0.0F};
   private static float[] ORIENTATION_DEFAULT = new float[]{0.0F, 0.0F, 0.0F};
   private static float[] TRANSFORM_DEFAULT = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
   private boolean m_hasComponentTransform;
   private boolean m_hasGeneralTransform;
   private float[] m_scale;
   private float[] m_translation;
   private Transform m_orientation;
   private Transform m_transform;

   protected Transformable() {
      this.m_scale = (float[])SCALE_DEFAULT.clone();
      this.m_translation = (float[])TRANSLATION_DEFAULT.clone();
      this.m_orientation = new Transform();
      this.m_transform = new Transform();
      this.setScale(1.0F, 1.0F, 1.0F);
   }

   public void setOrientation(float angle, float ax, float ay, float az) {
      if (ax == 0.0F && ay == 0.0F && az == 0.0F && angle != 0.0F) {
         throw new IllegalArgumentException("Transformable: the rotation axis (ax ay az) is zero and angle is nonzero");
      } else {
         this.m_orientation.setIdentity();
         this.m_orientation.postRotate(angle, ax, ay, az);
      }
   }

   public void preRotate(float angle, float ax, float ay, float az) {
      Transform t = new Transform();
      t.postRotate(angle, ax, ay, az);
      t.postMultiply(this.m_orientation);
      this.m_orientation.set(t);
   }

   public void postRotate(float angle, float ax, float ay, float az) {
      this.m_orientation.postRotate(angle, ax, ay, az);
   }

   public void getOrientation(float[] angleAxis) {
      if (angleAxis == null) {
         throw new NullPointerException("Transformable: angleAxis can not be null");
      } else if (angleAxis.length < 4) {
         throw new IllegalArgumentException("Transformable: length must be greater than 3");
      } else {
         float[] m = new float[16];
         this.m_orientation.get(m);
         Vector3 axis = new Vector3(m[6] - m[9], m[8] - m[2], m[1] - m[4]);

         try {
            axis.normalize();
         } catch (ArithmeticException var5) {
         }

         float angle = (float)Math.acos(0.5D * (double)(m[0] + m[5] + m[10] - 1.0F));
         angleAxis[0] = angle;
         angleAxis[1] = axis.x;
         angleAxis[2] = axis.y;
         angleAxis[3] = axis.z;
         if (angle == 0.0F || angle == 180.0F) {
            Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Transformable", "getOrientation(float angleAxis[])", "Singularities not implemented");
         }

      }
   }

   public void setScale(float sx, float sy, float sz) {
      this.m_scale[0] = sx;
      this.m_scale[1] = sy;
      this.m_scale[2] = sz;
   }

   public void scale(float sx, float sy, float sz) {
      float[] var10000 = this.m_scale;
      var10000[0] *= sx;
      var10000 = this.m_scale;
      var10000[1] *= sy;
      var10000 = this.m_scale;
      var10000[2] *= sz;
   }

   public void getScale(float[] xyz) {
      if (xyz.length < 3) {
         throw new IllegalArgumentException("xyz.length < 3");
      } else {
         System.arraycopy(this.m_scale, 0, xyz, 0, this.m_scale.length);
      }
   }

   public void setTranslation(float tx, float ty, float tz) {
      this.m_translation[0] = tx;
      this.m_translation[1] = ty;
      this.m_translation[2] = tz;
   }

   public void translate(float tx, float ty, float tz) {
      float[] var10000 = this.m_translation;
      var10000[0] += tx;
      var10000 = this.m_translation;
      var10000[1] += ty;
      var10000 = this.m_translation;
      var10000[2] += tz;
   }

   public void getTranslation(float[] xyz) {
      if (xyz.length < 3) {
         throw new IllegalArgumentException("Transformable: xyz.length < 3");
      } else {
         System.arraycopy(this.m_translation, 0, xyz, 0, this.m_translation.length);
      }
   }

   public void setTransform(Transform transform) {
      if (transform == null) {
         throw new NullPointerException("Transformable: transform can not be null");
      } else {
         this.m_transform.set(transform);
      }
   }

   public void getTransform(Transform transform) {
      if (transform == null) {
         throw new NullPointerException("Transformable: transform can not be null");
      } else {
         transform.set(this.m_transform);
      }
   }

   public void getCompositeTransform(Transform transform) {
      if (transform == null) {
         throw new NullPointerException("Transformable: transform can not be null");
      } else {
         float[] m = new float[16];
         this.m_orientation.get(m);
         m[3] = this.m_translation[0];
         m[7] = this.m_translation[1];
         m[11] = this.m_translation[2];
         transform.set(m);
         transform.postScale(this.m_scale[0], this.m_scale[1], this.m_scale[2]);
         transform.postMultiply(this.m_transform);
      }
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public boolean hasComponentTransform() {
      return this.m_hasComponentTransform;
   }

   public boolean hasGeneralTransform() {
      return this.m_hasGeneralTransform;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_hasComponentTransform = is.readBoolean();
      if (this.m_hasComponentTransform) {
         this.setTranslation(is.readFloat32(), is.readFloat32(), is.readFloat32());
         this.setScale(is.readFloat32(), is.readFloat32(), is.readFloat32());
         this.setOrientation(is.readFloat32(), is.readFloat32(), is.readFloat32(), is.readFloat32());
      }

      this.m_hasGeneralTransform = is.readBoolean();
      if (this.m_hasGeneralTransform) {
         this.m_transform.set(is.readMatrix());
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      float[] angleAxis = new float[4];
      float[] orientation = new float[3];
      this.getOrientation(angleAxis);
      orientation[0] = angleAxis[1];
      orientation[1] = angleAxis[2];
      orientation[2] = angleAxis[3];
      this.m_hasComponentTransform = angleAxis[0] != 0.0F || !Arrays.equals(this.m_translation, TRANSLATION_DEFAULT) || !Arrays.equals(this.m_scale, SCALE_DEFAULT) || !Arrays.equals(orientation, ORIENTATION_DEFAULT);
      os.writeBoolean(this.m_hasComponentTransform);
      if (this.m_hasComponentTransform) {
         os.writeVector3D(this.m_translation);
         os.writeVector3D(this.m_scale);
         os.writeFloat32(angleAxis[0]);
         os.writeVector3D(orientation);
      }

      float[] f = new float[16];
      this.m_transform.get(f);
      this.m_hasGeneralTransform = !Arrays.equals(f, TRANSFORM_DEFAULT);
      os.writeBoolean(this.m_hasGeneralTransform);
      if (this.m_hasGeneralTransform) {
         os.writeMatrix(this.m_transform);
      }

   }
}
