package com.wizzer.m3g.math;

public class Vector2 {
   public float x;
   public float y;

   public Vector2() {
      this.x = 0.0F;
      this.y = 0.0F;
   }

   public Vector2(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public Vector2(Vector2 v) {
      this.x = v.x;
      this.y = v.y;
   }

   public Vector2(float[] v) {
      if (v == null) {
         throw new NullPointerException();
      } else if (v.length < 2) {
         throw new IllegalArgumentException("Vector2: v must be of length 2 or larger");
      } else {
         this.x = v[0];
         this.y = v[1];
      }
   }

   public void set(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public void set(Vector2 v) {
      this.x = v.x;
      this.y = v.y;
   }

   public void set(float[] v) {
      if (v == null) {
         throw new NullPointerException("Vector2: v is null");
      } else if (v.length < 2) {
         throw new IllegalArgumentException("Vector2: v must be of length 2 or larger");
      } else {
         this.x = v[0];
         this.y = v[1];
      }
   }

   public void add(Vector2 v) {
      this.x += v.x;
      this.y += v.y;
   }

   public void add(float scalar) {
      this.x += scalar;
      this.y += scalar;
   }

   public void add(Vector2 v, float scalar) {
      this.x = v.x + scalar;
      this.y = v.y + scalar;
   }

   public void add(Vector2 v1, Vector2 v2) {
      this.x = v1.x + v2.x;
      this.y = v1.y + v2.y;
   }

   public void subtract(Vector2 v) {
      this.x -= v.x;
      this.y -= v.y;
   }

   public void subtract(float scalar) {
      this.x -= scalar;
      this.y -= scalar;
   }

   public void subtract(Vector2 v, float scalar) {
      this.x = v.x - scalar;
      this.y = v.y - scalar;
   }

   public void subtract(Vector2 v1, Vector2 v2) {
      this.x = v1.x - v2.x;
      this.y = v1.y - v2.y;
   }

   public void multiply(float scalar) {
      this.x *= scalar;
      this.y *= scalar;
   }

   public void multiply(Vector2 v, float scalar) {
      this.x = v.x * scalar;
      this.y = v.y * scalar;
   }

   public float length() {
      return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y));
   }

   public float length2() {
      return this.x * this.x + this.y * this.y;
   }

   public void normalize() {
      float length = this.length();
      if (length < 1.0E-7F) {
         throw new ArithmeticException("Vector2: can't normalize zero length vector");
      } else {
         this.multiply(1.0F / length);
      }
   }

   public float dot(Vector2 v) {
      return this.x * v.x + this.y * v.y;
   }

   public Vector2 getProjection(Vector2 v) {
      Vector2 e = new Vector2(v);
      e.normalize();
      e.multiply(this.dot(e));
      return e;
   }

   public Vector2 getRejection(Vector2 v) {
      Vector2 u = new Vector2(this);
      u.subtract(this.getProjection(v));
      return u;
   }

   public Vector2 getReflection(Vector2 v) {
      Vector2 u = new Vector2(this);
      Vector2 w = this.getRejection(v);
      w.multiply(2.0F);
      u.subtract(w);
      return u;
   }

   public boolean equals(Vector2 v) {
      if (v == null) {
         throw new NullPointerException();
      } else {
         return this.x == v.x && this.y == v.y;
      }
   }

   public float[] toArray() {
      float[] a = new float[]{this.x, this.y};
      return a;
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ")";
   }
}
