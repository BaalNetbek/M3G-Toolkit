package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.Color;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Light extends Node {
   public static final int AMBIENT = 128;
   public static final int DIRECTIONAL = 129;
   public static final int OMNI = 130;
   public static final int SPOT = 131;
   private float m_constantAttenuation = 1.0F;
   private float m_linearAttenuation = 0.0F;
   private float m_quadraticAttenuation = 0.0F;
   private int m_color = 16777215;
   private int m_mode = 129;
   private float m_intensity = 1.0F;
   private float m_spotAngle = 45.0F;
   private float m_spotExponent = 0.0F;
   private int m_lightId = -1;

   public void setMode(int mode) {
      if (mode < 128 | mode > 131) {
         throw new IllegalArgumentException("Light: mode is not one of AMBIENT, DIRECTIONAL, OMNI, SPOT");
      } else {
         this.m_mode = mode;
      }
   }

   public int getMode() {
      return this.m_mode;
   }

   public void setIntensity(float intensity) {
      this.m_intensity = intensity;
   }

   public float getIntensity() {
      return this.m_intensity;
   }

   public void setColor(int RGB) {
      this.m_color = RGB;
   }

   public int getColor() {
      return this.m_color;
   }

   public void setSpotAngle(float angle) {
      if (!(angle < 0.0F) && !(angle > 90.0F)) {
         this.m_spotAngle = angle;
      } else {
         throw new IllegalArgumentException("Light: angle is not in [0, 90]");
      }
   }

   public float getSpotAngle() {
      return this.m_spotAngle;
   }

   public void setSpotExponent(float exponent) {
      if (!(exponent < 0.0F) && !(exponent > 128.0F)) {
         this.m_spotExponent = exponent;
      } else {
         throw new IllegalArgumentException("Light: exponent is not in [0, 128]");
      }
   }

   public float getSpotExponent() {
      return this.m_spotExponent;
   }

   public void setAttenuation(float constant, float linear, float quadratic) {
      if (!(constant < 0.0F) && !(linear < 0.0F) && !(quadratic < 0.0F)) {
         if (constant == 0.0F && linear == 0.0F && quadratic == 0.0F) {
            throw new IllegalArgumentException("Light: all of the parameter values are zero");
         } else {
            this.m_constantAttenuation = constant;
            this.m_linearAttenuation = linear;
            this.m_quadraticAttenuation = quadratic;
         }
      } else {
         throw new IllegalArgumentException("Light: any of the parameter values are negative");
      }
   }

   public float getConstantAttenuation() {
      return this.m_constantAttenuation;
   }

   public float getLinearAttenuation() {
      return this.m_linearAttenuation;
   }

   public float getQuadraticAttenuation() {
      return this.m_quadraticAttenuation;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 12;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setAttenuation(is.readFloat32(), is.readFloat32(), is.readFloat32());
      this.setColor(is.readColorRGB());
      this.setMode(is.readByte());
      this.setIntensity(is.readFloat32());
      this.setSpotAngle(is.readFloat32());
      this.setSpotExponent(is.readFloat32());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeFloat32(this.m_constantAttenuation);
      os.writeFloat32(this.m_linearAttenuation);
      os.writeFloat32(this.m_quadraticAttenuation);
      os.writeColorRGB(this.m_color);
      os.writeByte(this.m_mode);
      os.writeFloat32(this.m_intensity);
      os.writeFloat32(this.m_spotAngle);
      os.writeFloat32(this.m_spotExponent);
   }

   void setupGL(GL gl) {
      float[] col = (new Color(this.m_color)).toArray();
      col[0] *= this.m_intensity;
      col[1] *= this.m_intensity;
      col[2] *= this.m_intensity;
      col[3] *= this.m_intensity;
      if (this.m_mode == 128) {
         gl.glLightModelfv(2899, col, 0);
      } else {
         this.getFreeLightId(gl);
         if (this.m_lightId == -1) {
            return;
         }

         gl.glLightfv(this.m_lightId, 4609, col, 0);
         gl.glLightfv(this.m_lightId, 4608, new float[]{0.0F, 0.0F, 0.0F, 0.0F}, 0);
         gl.glLightfv(this.m_lightId, 4610, col, 0);
         if (this.m_mode == 130) {
            gl.glLightfv(this.m_lightId, 4611, new float[]{0.0F, 0.0F, 0.0F, 1.0F}, 0);
            gl.glLightf(this.m_lightId, 4614, 180.0F);
            gl.glLightf(this.m_lightId, 4613, 0.0F);
         } else if (this.m_mode == 131) {
            gl.glLightfv(this.m_lightId, 4611, new float[]{0.0F, 0.0F, 0.0F, 1.0F}, 0);
            gl.glLightf(this.m_lightId, 4614, this.m_spotAngle);
            gl.glLightf(this.m_lightId, 4613, this.m_spotExponent);
            gl.glLightfv(this.m_lightId, 4612, new float[]{0.0F, 0.0F, 1.0F}, 0);
         } else if (this.m_mode == 129) {
            gl.glLightfv(this.m_lightId, 4611, new float[]{0.0F, 0.0F, 1.0F, 0.0F}, 0);
            gl.glLightf(this.m_lightId, 4614, 180.0F);
            gl.glLightf(this.m_lightId, 4613, 0.0F);
         }

         gl.glLightf(this.m_lightId, 4615, this.m_constantAttenuation);
         gl.glLightf(this.m_lightId, 4616, this.m_linearAttenuation);
         gl.glLightf(this.m_lightId, 4617, this.m_quadraticAttenuation);
      }

   }

   private void getFreeLightId(GL gl) {
      this.m_lightId = -1;

      for(int i = 0; i < 8; ++i) {
         if (!gl.glIsEnabled(16384 + i)) {
            this.m_lightId = 16384 + i;
            gl.glEnable(this.m_lightId);
            return;
         }
      }

   }
}
