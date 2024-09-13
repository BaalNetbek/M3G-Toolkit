package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class CompositingMode extends Object3D {
   public static final int ALPHA = 64;
   public static final int ALPHA_ADD = 65;
   public static final int MODULATE = 66;
   public static final int MODULATE_X2 = 67;
   public static final int REPLACE = 68;
   private boolean m_depthTestEnabled = true;
   private boolean m_depthWriteEnabled = true;
   private boolean m_colorWriteEnabled = true;
   private boolean m_alphaWriteEnabled = true;
   private int m_blending = 68;
   private float m_alphaThreshold = 0.0F;
   private float m_depthOffsetFactor = 0.0F;
   private float m_depthOffsetUnits = 0.0F;

   public void setBlending(int mode) {
      if (mode >= 64 && mode <= 68) {
         this.m_blending = mode;
      } else {
         throw new IllegalArgumentException("CompositingMode: mode is not one of the symbolic constants");
      }
   }

   public int getBlending() {
      return this.m_blending;
   }

   public void setAlphaThreshold(float threshold) {
      if (!(threshold < 0.0F) && !(threshold > 1.0F)) {
         this.m_alphaThreshold = threshold;
      } else {
         throw new IllegalArgumentException("CompositingMode: threshold is negative or greater than 1.0");
      }
   }

   public float getAlphaThreshold() {
      return this.m_alphaThreshold;
   }

   public void setAlphaWriteEnable(boolean enable) {
      this.m_alphaWriteEnabled = enable;
   }

   public boolean isAlphaWriteEnabled() {
      return this.m_alphaWriteEnabled;
   }

   public void setColorWriteEnable(boolean enable) {
      this.m_colorWriteEnabled = enable;
   }

   public boolean isColorWriteEnabled() {
      return this.m_colorWriteEnabled;
   }

   public void setDepthWriteEnable(boolean enable) {
      this.m_depthWriteEnabled = enable;
   }

   public boolean isDepthWriteEnabled() {
      return this.m_depthWriteEnabled;
   }

   public void setDepthTestEnable(boolean enable) {
      this.m_depthTestEnabled = enable;
   }

   public boolean isDepthTestEnabled() {
      return this.m_depthTestEnabled;
   }

   public void setDepthOffset(float factor, float units) {
      this.m_depthOffsetFactor = factor;
      this.m_depthOffsetUnits = units;
   }

   public float getDepthOffsetFactor() {
      return this.m_depthOffsetFactor;
   }

   public float getDepthOffsetUnits() {
      return this.m_depthOffsetUnits;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 6;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setDepthTestEnable(is.readBoolean());
      this.setDepthWriteEnable(is.readBoolean());
      this.setColorWriteEnable(is.readBoolean());
      this.setAlphaWriteEnable(is.readBoolean());
      this.setBlending(is.readByte());
      this.setAlphaThreshold((float)is.readByte() / 255.0F);
      this.setDepthOffset(is.readFloat32(), is.readFloat32());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeBoolean(this.m_depthTestEnabled);
      os.writeBoolean(this.m_depthWriteEnabled);
      os.writeBoolean(this.m_colorWriteEnabled);
      os.writeBoolean(this.m_alphaWriteEnabled);
      os.writeByte(this.m_blending);
      os.writeByte((byte)((int)(this.m_alphaThreshold * 255.0F)));
      os.writeFloat32(this.m_depthOffsetFactor);
      os.writeFloat32(this.m_depthOffsetUnits);
   }

   void setupGL(GL gl) {
      gl.glDepthFunc(515);
      gl.glBlendEquation(32774);
      if (this.m_depthTestEnabled) {
         gl.glEnable(2929);
      } else {
         gl.glDisable(2929);
      }

      gl.glDepthMask(this.m_depthWriteEnabled);
      gl.glColorMask(this.m_colorWriteEnabled, this.m_colorWriteEnabled, this.m_colorWriteEnabled, this.m_alphaWriteEnabled);
      if (this.m_alphaThreshold > 0.0F) {
         gl.glAlphaFunc(518, this.m_alphaThreshold);
         gl.glEnable(3008);
      } else {
         gl.glDisable(3008);
      }

      if (this.m_blending != 68) {
         switch(this.m_blending) {
         case 64:
            gl.glBlendFunc(770, 771);
            break;
         case 65:
            gl.glBlendFunc(770, 1);
            break;
         case 66:
            gl.glBlendFunc(774, 0);
            break;
         case 67:
            gl.glBlendFunc(774, 768);
         }

         gl.glEnable(3042);
      } else {
         gl.glDisable(3042);
      }

      if (this.m_depthOffsetFactor == 0.0F && this.m_depthOffsetUnits == 0.0F) {
         gl.glDisable(32823);
      } else {
         gl.glPolygonOffset(this.m_depthOffsetFactor, this.m_depthOffsetUnits);
         gl.glEnable(32823);
      }

   }
}
