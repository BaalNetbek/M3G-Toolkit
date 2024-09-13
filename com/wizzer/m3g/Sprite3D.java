package com.wizzer.m3g;

import com.wizzer.m3g.math.Vector3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.media.opengl.GL;

public class Sprite3D extends Node {
   private Image2D m_image;
   private Appearance m_appearance;
   private boolean m_scaled;
   private int m_cropX;
   private int m_cropY;
   private int m_cropWidth;
   private int m_cropHeight;
   private static Hashtable m_textureCache = new Hashtable();
   private Texture2D m_texture;

   public Sprite3D(boolean scaled, Image2D image, Appearance appearance) {
      this.m_scaled = scaled;
      this.setImage(image);
      this.m_appearance = appearance;
      this.setCrop(0, 0, image.getWidth(), image.getHeight());
   }

   public boolean isScaled() {
      return this.m_scaled;
   }

   public void setAppearance(Appearance appearance) {
      this.m_appearance = appearance;
   }

   public Appearance getAppearance() {
      return this.m_appearance;
   }

   public void setImage(Image2D image) {
      if (image == null) {
         throw new NullPointerException("Sprite3D: image is null");
      } else {
         this.m_image = image;
         this.setCrop(0, 0, image.getWidth(), image.getHeight());
         this.m_texture = (Texture2D)m_textureCache.get(image);
         if (this.m_texture == null) {
            this.m_texture = new Texture2D(image);
            this.m_texture.setFiltering(209, 209);
            this.m_texture.setWrapping(240, 240);
            this.m_texture.setBlending(228);
            m_textureCache.put(image, this.m_texture);
         }

      }
   }

   public Image2D getImage() {
      return this.m_image;
   }

   public void setCrop(int cropX, int cropY, int width, int height) {
      this.m_cropX = cropX;
      this.m_cropY = cropY;
      this.m_cropWidth = width;
      this.m_cropHeight = height;
   }

   public int getCropX() {
      return this.m_cropX;
   }

   public int getCropY() {
      return this.m_cropY;
   }

   public int getCropWidth() {
      return this.m_cropWidth;
   }

   public int getCropHeight() {
      return this.m_cropHeight;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_image != null) {
         if (references != null) {
            references[numReferences] = this.m_image;
         }

         ++numReferences;
      }

      if (this.m_appearance != null) {
         if (references != null) {
            references[numReferences] = this.m_appearance;
         }

         ++numReferences;
      }

      return numReferences;
   }

   Sprite3D() {
   }

   public void setScaled(boolean scaled) {
      this.m_scaled = scaled;
   }

   public int getObjectType() {
      return 18;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      if (index > 0L) {
         M3GObject obj = this.getObjectAtIndex(table, index, 10);
         if (obj != null) {
            this.m_image = (Image2D)obj;
            index = is.readObjectIndex();
            if (index != 0L) {
               obj = this.getObjectAtIndex(table, index, 3);
               if (obj == null) {
                  throw new IOException("Sprite3D:appearance-index = " + index);
               }

               this.m_appearance = (Appearance)obj;
            }

            this.m_scaled = is.readBoolean();
            this.setCrop((int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32());
         } else {
            throw new IOException("Sprite3D:image-index = " + index);
         }
      } else {
         throw new IOException("Sprite3D:image-index = " + index);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_image);
      if (index > 0) {
         os.writeObjectIndex((long)index);
         if (this.m_appearance != null) {
            index = table.indexOf(this.m_appearance);
            if (index <= 0) {
               throw new IOException("Sprite3D:appearance-index = " + index);
            }

            os.writeObjectIndex((long)index);
         } else {
            os.writeObjectIndex(0L);
         }

         os.writeBoolean(this.m_scaled);
         os.writeInt32(this.m_cropX);
         os.writeInt32(this.m_cropY);
         os.writeInt32(this.m_cropWidth);
         os.writeInt32(this.m_cropHeight);
      } else {
         throw new IOException("Sprite3D:image-index = " + index);
      }
   }

   protected void buildReferenceTable(ArrayList table) {
      this.m_image.buildReferenceTable(table);
      if (this.m_appearance != null) {
         this.m_appearance.buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }

   void render(GL gl, Transform t) {
      gl.glMatrixMode(5888);
      gl.glPushMatrix();
      t.multGL(gl);
      float[] m = new float[16];
      gl.glGetFloatv(2982, m, 0);
      Vector3 up = new Vector3(m[1], m[5], m[9]);
      up.normalize();
      Vector3 right = new Vector3(m[0], m[4], m[8]);
      right.normalize();
      float size = 1.0F;
      Vector3 rightPlusUp = new Vector3(right);
      rightPlusUp.add(up);
      rightPlusUp.multiply(size);
      Vector3 rightMinusUp = new Vector3(right);
      rightMinusUp.subtract(up);
      rightMinusUp.multiply(size);
      Vector3 topLeft = new Vector3(rightMinusUp);
      topLeft.multiply(-1.0F);
      Vector3 topRight = new Vector3(rightPlusUp);
      Vector3 bottomLeft = new Vector3(rightPlusUp);
      bottomLeft.multiply(-1.0F);
      Vector3 bottomRight = new Vector3(rightMinusUp);
      Graphics3D.getInstance().setAppearance(this.getAppearance());
      Graphics3D.getInstance().disableTextureUnits();
      gl.glActiveTexture(33984);
      this.m_texture.setupGL(gl, new float[]{1.0F, 0.0F, 0.0F, 0.0F});
      gl.glBegin(7);
      gl.glTexCoord2f(0.0F, 0.0F);
      gl.glVertex3f(topLeft.x, topLeft.y, topLeft.z);
      gl.glTexCoord2f(0.0F, 1.0F);
      gl.glVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z);
      gl.glTexCoord2f(1.0F, 1.0F);
      gl.glVertex3f(bottomRight.x, bottomRight.y, bottomRight.z);
      gl.glTexCoord2f(1.0F, 0.0F);
      gl.glVertex3f(topRight.x, topRight.y, topRight.z);
      gl.glEnd();
      gl.glPopMatrix();
      gl.glDisable(3553);
      gl.glDepthMask(true);
   }
}
