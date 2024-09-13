package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.Color;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Fog extends Object3D {
   public static final int EXPONENTIAL = 80;
   public static final int LINEAR = 81;
   private int m_color = 0;
   private int m_mode = 81;
   private float m_density = 1.0F;
   private float m_nearDistance = 0.0F;
   private float m_farDistance = 1.0F;

   public void setMode(int mode) {
      if (mode != 81 && mode != 80) {
         throw new IllegalArgumentException("Fog: mode is not LINEAR or EXPONENTIAL");
      } else {
         this.m_mode = mode;
      }
   }

   public int getMode() {
      return this.m_mode;
   }

   public void setLinear(float near, float far) {
      this.m_nearDistance = near;
      this.m_farDistance = far;
   }

   public float getNearDistance() {
      return this.m_nearDistance;
   }

   public float getFarDistance() {
      return this.m_farDistance;
   }

   public void setDensity(float density) {
      if (density < 0.0F) {
         throw new IllegalArgumentException("Fog: density < 0");
      } else {
         this.m_density = density;
      }
   }

   public float getDensity() {
      return this.m_density;
   }

   public void setColor(int RGB) {
      this.m_color = RGB;
   }

   public int getColor() {
      return this.m_color;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 7;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setColor(is.readColorRGB());
      this.setMode(is.readByte());
      if (this.m_mode == 81) {
         this.setLinear(is.readFloat32(), is.readFloat32());
      } else {
         this.setDensity(is.readFloat32());
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeColorRGB(this.m_color);
      os.writeByte(this.m_mode);
      if (this.m_mode == 81) {
         os.writeFloat32(this.m_nearDistance);
         os.writeFloat32(this.m_farDistance);
      } else if (this.m_mode == 80) {
         os.writeFloat32(this.m_density);
      }

   }

   void setupGL(GL gl) {
      gl.glFogi(2917, this.getGLFogMode(this.m_mode));
      gl.glFogfv(2918, Color.intToFloatArray(this.m_color), 0);
      gl.glFogf(2914, this.m_density);
      gl.glFogf(2915, this.m_nearDistance);
      gl.glFogf(2916, this.m_farDistance);
      gl.glEnable(2912);
   }

   int getGLFogMode(int mode) {
      switch(mode) {
      case 80:
         return 2048;
      default:
         return 9729;
      }
   }
}
