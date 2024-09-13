package com.wizzer.m3g.math;

public class Vector3 {
   public float x;
   public float y;
   public float z;

   public Vector3() {
      this.x = 0.0F;
      this.y = 0.0F;
      this.z = 0.0F;
   }

   public Vector3(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3(Vector3 v) {
      this.x = v.x;
      this.y = v.y;
      this.z = v.z;
   }

   public Vector3(float[] v) {
      if (v == null) {
         throw new NullPointerException();
      } else if (v.length < 3) {
         throw new IllegalArgumentException("Vector3: v must be of length 3 or larger");
      } else {
         this.x = v[0];
         this.y = v[1];
         this.z = v[2];
      }
   }

   public void set(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void set(Vector3 v) {
      this.x = v.x;
      this.y = v.y;
      this.z = v.z;
   }

   public void set(float[] v) {
      if (v == null) {
         throw new NullPointerException();
      } else if (v.length < 3) {
         throw new IllegalArgumentException("Vector3: v must be of length 3 or larger");
      } else {
         this.x = v[0];
         this.y = v[1];
         this.z = v[2];
      }
   }

   public void add(Vector3 v) {
      this.x += v.x;
      this.y += v.y;
      this.z += v.z;
   }

   public void add(float scalar) {
      this.x += scalar;
      this.y += scalar;
      this.z += scalar;
   }

   public void add(Vector3 v, float scalar) {
      this.x = v.x + scalar;
      this.y = v.y + scalar;
      this.z = v.z + scalar;
   }

   public void add(Vector3 v1, Vector3 v2) {
      this.x = v1.x + v2.x;
      this.y = v1.y + v2.y;
      this.z = v1.z + v2.z;
   }

   public void subtract(Vector3 v) {
      this.x -= v.x;
      this.y -= v.y;
      this.z -= v.z;
   }

   public void subtract(float scalar) {
      this.x -= scalar;
      this.y -= scalar;
      this.z -= scalar;
   }

   public void subtract(Vector3 v, float scalar) {
      this.x = v.x - scalar;
      this.y = v.y - scalar;
      this.z = v.z - scalar;
   }

   public void subtract(Vector3 v1, Vector3 v2) {
      this.x = v1.x - v2.x;
      this.y = v1.y - v2.y;
      this.z = v1.z - v2.z;
   }

   public void multiply(float scalar) {
      this.x *= scalar;
      this.y *= scalar;
      this.z *= scalar;
   }

   public void multiply(Vector3 v, float scalar) {
      this.x = v.x * scalar;
      this.y = v.y * scalar;
      this.z = v.z * scalar;
   }

   public float length() {
      return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z));
   }

   public float length2() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public void normalize() {
      float length = this.length();
      if (length < 1.0E-7F) {
         throw new ArithmeticException("Vector3: can't normalize zero length vector");
      } else {
         this.multiply(1.0F / length);
      }
   }

   public float dot(Vector3 v) {
      return this.x * v.x + this.y * v.y + this.z * v.z;
   }

   public void cross(Vector3 v) {
      float x = this.y * v.z - this.z * v.y;
      float y = this.z * v.z - this.z * v.z;
      this.z = this.x * v.y - this.y * v.x;
      this.x = x;
      this.y = y;
   }

   public void cross(Vector3 v1, Vector3 v2) {
      this.z = v1.x * v2.y - v1.y * v2.x;
      this.x = v1.y * v2.z - v1.z * v2.y;
      this.y = v1.z * v2.z - v1.z * v2.z;
   }

   public void rotate(float angle, Vector3 axis) {
      Vector3 c = new Vector3();
      Vector3 a = new Vector3(axis);
      a.normalize();
      c.cross(this, a);
      Vector3 result = this.getRejection(axis);
      result.multiply((float)Math.cos((double)angle));
      c.multiply((float)Math.sin((double)angle));
      result.add(c);
      result.add(this.getProjection(axis));
      this.set(result);
   }

   public Vector3 getPerpendicular() {
      Vector3 v = new Vector3();
      v.cross(this, new Vector3(1.0F, 0.0F, 0.0F));
      if (v.length() < 1.0E-7F) {
         v.cross(this, new Vector3(0.0F, 1.0F, 0.0F));
      }

      return v;
   }

   public Vector3 getProjection(Vector3 v) {
      Vector3 e = new Vector3(v);
      e.normalize();
      e.multiply(this.dot(e));
      return e;
   }

   public Vector3 getRejection(Vector3 v) {
      Vector3 u = new Vector3(this);
      u.subtract(this.getProjection(v));
      return u;
   }

   public Vector3 getReflection(Vector3 v) {
      Vector3 u = new Vector3(this);
      Vector3 w = this.getRejection(v);
      w.multiply(2.0F);
      u.subtract(w);
      return u;
   }

   public boolean equals(Vector3 v) {
      if (v == null) {
         throw new NullPointerException();
      } else {
         return this.x == v.x && this.y == v.y && this.z == v.z;
      }
   }

   public float[] toArray() {
      float[] a = new float[]{this.x, this.y, this.z};
      return a;
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }
}
