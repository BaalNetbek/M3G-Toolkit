package com.wizzer.m3g.toolkit.util;

public class Color {
   public float m_a = 0.0F;
   public float m_r = 0.0F;
   public float m_g = 0.0F;
   public float m_b = 0.0F;

   public Color(float a, float r, float g, float b) {
      this.m_a = a;
      this.m_r = r;
      this.m_g = g;
      this.m_b = b;
   }

   public Color(int color) {
      this.m_a = (float)(color >> 24) / 255.0F;
      this.m_r = (float)((color & 16711680) >> 16) / 255.0F;
      this.m_g = (float)((color & '\uff00') >> 8) / 255.0F;
      this.m_b = (float)(color & 255) / 255.0F;
   }

   public float[] toArray() {
      float[] c = new float[]{this.m_r, this.m_g, this.m_b, this.m_a};
      return c;
   }

   public static float[] intToFloatArray(int color) {
      Color c = new Color(color);
      return c.toArray();
   }

   public String toString() {
      return "{" + this.m_r + ", " + this.m_g + ", " + this.m_b + ", " + this.m_a + "}";
   }
}
