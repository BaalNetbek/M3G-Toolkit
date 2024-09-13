package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.Color;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Material extends Object3D {
   public static final int AMBIENT = 1024;
   public static final int DIFFUSE = 2048;
   public static final int EMISSIVE = 4096;
   public static final int SPECULAR = 8192;
   private int m_ambientColor = 3355443;
   private int m_diffuseColor = -3355444;
   private int m_emissiveColor = 0;
   private int m_specularColor = 0;
   private float m_shininess = 0.0F;
   private boolean m_vertexColorTrackingEnabled = false;

   public void setColor(int target, int ARGB) {
      int ambient = target & 1024;
      int diffuse = target & 2048;
      int emissive = target & 4096;
      int specular = target & 8192;
      int total = ambient | diffuse | emissive | specular;
      if (total != 0 && (total & target) == target) {
         if (ambient != 0) {
            this.m_ambientColor = ARGB;
         }

         if (diffuse != 0) {
            this.m_diffuseColor = ARGB;
         }

         if (emissive != 0) {
            this.m_emissiveColor = ARGB;
         }

         if (specular != 0) {
            this.m_specularColor = ARGB;
         }

      } else {
         throw new IllegalArgumentException("Material: target has a value other than an inclusive OR of one or more of AMBIENT, DIFFUSE, EMISSIVE, SPECULAR");
      }
   }

   public int getColor(int target) {
      if (target == 1024) {
         return this.m_ambientColor;
      } else if (target == 2048) {
         return this.m_diffuseColor;
      } else if (target == 4096) {
         return this.m_emissiveColor;
      } else if (target == 8192) {
         return this.m_specularColor;
      } else {
         throw new IllegalArgumentException("Material: target is not one of the symbolic constants");
      }
   }

   public void setShininess(float shininess) {
      if (!(shininess < 0.0F) && !(shininess > 128.0F)) {
         this.m_shininess = shininess;
      } else {
         throw new IllegalArgumentException("Material: shininess is not in [0, 128]");
      }
   }

   public float getShininess() {
      return this.m_shininess;
   }

   public void setVertexColorTrackingEnabled(boolean enable) {
      this.m_vertexColorTrackingEnabled = enable;
   }

   public boolean isVertexColorTrackingEnabled() {
      return this.m_vertexColorTrackingEnabled;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public int getObjectType() {
      return 13;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setColor(1024, is.readColorRGB());
      this.setColor(2048, is.readColorRGBA());
      this.setColor(4096, is.readColorRGB());
      this.setColor(8192, is.readColorRGB());
      this.setShininess(is.readFloat32());
      this.setVertexColorTrackingEnabled(is.readBoolean());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeColorRGB(this.m_ambientColor);
      os.writeColorRGBA(this.m_diffuseColor);
      os.writeColorRGB(this.m_emissiveColor);
      os.writeColorRGB(this.m_specularColor);
      os.writeFloat32(this.m_shininess);
      os.writeBoolean(this.m_vertexColorTrackingEnabled);
   }

   void setupGL(GL gl, int lightTarget) {
      gl.glEnable(2896);
      gl.glMaterialfv(1032, 5632, Color.intToFloatArray(this.m_emissiveColor), 0);
      gl.glMaterialfv(1032, 4608, Color.intToFloatArray(this.m_ambientColor), 0);
      gl.glMaterialfv(1032, 4609, Color.intToFloatArray(this.m_diffuseColor), 0);
      gl.glMaterialfv(1032, 4610, Color.intToFloatArray(this.m_specularColor), 0);
      gl.glMaterialf(1032, 5633, this.m_shininess);
   }
}
