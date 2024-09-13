package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class PolygonMode extends Object3D {
   public static final int CULL_BACK = 160;
   public static final int CULL_FRONT = 161;
   public static final int CULL_NONE = 162;
   public static final int SHADE_FLAT = 164;
   public static final int SHADE_SMOOTH = 165;
   public static final int WINDING_CCW = 168;
   public static final int WINDING_CW = 169;
   private int m_culling = 160;
   private int m_shading = 165;
   private int m_winding = 168;
   private boolean m_twoSidedLightingEnabled = false;
   private boolean m_localCameraLightingEnabled = false;
   private boolean m_perspectiveCorrectionEnabled = false;

   public void setCulling(int mode) {
      if (mode < 160 || mode > 162) {
         new IllegalArgumentException("PolygonMode: mode is not one of CULL_BACK, CULL_FRONT, CULL_NONE");
      }

      this.m_culling = mode;
   }

   public int getCulling() {
      return this.m_culling;
   }

   public void setWinding(int mode) {
      if (mode < 168 || mode > 169) {
         new IllegalArgumentException("PolygonMode: mode is not one of WINDING_CCW, WINDING_CW");
      }

      this.m_winding = mode;
   }

   public int getWinding() {
      return this.m_winding;
   }

   public void setShading(int mode) {
      if (mode < 164 || mode > 165) {
         new IllegalArgumentException("PolygonMode: mode is not one of SHADE_FLAT, SHADE_SMOOTH");
      }

      this.m_shading = mode;
   }

   public int getShading() {
      return this.m_shading;
   }

   public void setTwoSidedLightingEnable(boolean enable) {
      this.m_twoSidedLightingEnabled = enable;
   }

   public boolean isTwoSidedLightingEnabled() {
      return this.m_twoSidedLightingEnabled;
   }

   public void setLocalCameraLightingEnable(boolean enable) {
      this.m_localCameraLightingEnabled = enable;
   }

   public boolean isLocalCameraLightingEnabled() {
      return this.m_localCameraLightingEnabled;
   }

   public void setPerspectiveCorrectionEnable(boolean enable) {
      this.m_perspectiveCorrectionEnabled = enable;
   }

   public boolean isPerspectiveCorrectionEnabled() {
      return this.m_perspectiveCorrectionEnabled;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 8;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setCulling(is.readByte());
      this.setShading(is.readByte());
      this.setWinding(is.readByte());
      this.setTwoSidedLightingEnable(is.readBoolean());
      this.setLocalCameraLightingEnable(is.readBoolean());
      this.setPerspectiveCorrectionEnable(is.readBoolean());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeByte(this.m_culling);
      os.writeByte(this.m_shading);
      os.writeByte(this.m_winding);
      os.writeBoolean(this.m_twoSidedLightingEnabled);
      os.writeBoolean(this.m_localCameraLightingEnabled);
      os.writeBoolean(this.m_perspectiveCorrectionEnabled);
   }

   void setupGL(GL gl) {
      if (this.m_shading == 165) {
         gl.glShadeModel(7425);
      } else {
         gl.glShadeModel(7424);
      }

      if (this.m_culling == 162) {
         gl.glDisable(2884);
      } else {
         gl.glEnable(2884);
         if (this.m_culling == 160) {
            gl.glCullFace(1029);
         } else {
            gl.glCullFace(1028);
         }
      }

      if (this.m_winding == 168) {
         gl.glFrontFace(2305);
      } else {
         gl.glFrontFace(2304);
      }

   }

   int getLightTarget() {
      return this.isTwoSidedLightingEnabled() ? 1032 : 1028;
   }
}
