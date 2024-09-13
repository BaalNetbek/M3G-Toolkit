package com.wizzer.m3g.math;

public class Vector4 {
   public float x;
   public float y;
   public float z;
   public float w;

   public Vector4() {
      this.x = 0.0F;
      this.y = 0.0F;
      this.z = 0.0F;
      this.w = 0.0F;
   }

   public Vector4(float x, float y, float z, float w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }

   public Vector4(Vector4 v) {
      this.x = v.x;
      this.y = v.y;
      this.z = v.z;
      this.w = v.w;
   }

   public Vector4(float[] v) {
      if (v == null) {
         throw new NullPointerException();
      } else if (v.length < 4) {
         throw new IllegalArgumentException("Vector4: v must be of length 4 or grater");
      } else {
         this.x = v[0];
         this.y = v[1];
         this.z = v[2];
         this.w = v[3];
      }
   }

   public void set(float x, float y, float z, float w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }

   public void set(Vector4 v) {
      this.x = v.x;
      this.y = v.y;
      this.z = v.z;
      this.w = v.w;
   }

   public void set(float[] v) {
      if (v == null) {
         throw new NullPointerException();
      } else if (v.length < 4) {
         throw new IllegalArgumentException("Vector4: v must be of length 4 or greater");
      } else {
         this.x = v[0];
         this.y = v[1];
         this.z = v[2];
         this.w = v[3];
      }
   }

   public void add(Vector4 v) {
      this.x += v.x;
      this.y += v.y;
      this.z += v.z;
      this.w += v.w;
   }

   public void add(float scalar) {
      this.x += scalar;
      this.y += scalar;
      this.z += scalar;
      this.w += scalar;
   }

   public void add(Vector4 v, float scalar) {
      this.x = v.x + scalar;
      this.y = v.y + scalar;
      this.z = v.z + scalar;
      this.w = v.w + scalar;
   }

   public void add(Vector4 v1, Vector4 v2) {
      this.x = v1.x + v2.x;
      this.y = v1.y + v2.y;
      this.z = v1.z + v2.z;
      this.w = v1.w + v2.w;
   }

   public void subtract(Vector4 v) {
      this.x -= v.x;
      this.y -= v.y;
      this.z -= v.z;
      this.w -= v.w;
   }

   public void subtract(float scalar) {
      this.x -= scalar;
      this.y -= scalar;
      this.z -= scalar;
      this.w -= scalar;
   }

   public void subtract(Vector4 v, float scalar) {
      this.x = v.x - scalar;
      this.y = v.y - scalar;
      this.z = v.z - scalar;
      this.w = v.w - scalar;
   }

   public void subtract(Vector4 v1, Vector4 v2) {
      this.x = v1.x - v2.x;
      this.y = v1.y - v2.y;
      this.z = v1.z - v2.z;
      this.w = v1.w - v2.w;
   }

   public void multiply(float scalar) {
      this.x *= scalar;
      this.y *= scalar;
      this.z *= scalar;
      this.w *= scalar;
   }

   public void multiply(Vector4 v, float scalar) {
      this.x = v.x * scalar;
      this.y = v.y * scalar;
      this.z = v.z * scalar;
      this.w = v.w * scalar;
   }

   public float length() {
      return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w));
   }

   public float length2() {
      return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
   }

   public void normalize() {
      float length = this.length();
      if (length < 1.0E-7F) {
         throw new ArithmeticException("Vector4: can't normalize zero length vector");
      } else {
         this.multiply(1.0F / length);
      }
   }

   public float dot(Vector4 v) {
      return this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w;
   }

   public boolean equals(Vector4 v) {
      if (v == null) {
         throw new NullPointerException();
      } else {
         return this.x == v.x && this.y == v.y && this.z == v.z && this.w == v.w;
      }
   }

   public float[] toArray() {
      float[] a = new float[]{this.x, this.y, this.z, this.w};
      return a;
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
   }
}
