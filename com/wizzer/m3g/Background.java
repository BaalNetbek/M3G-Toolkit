package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.Color;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Background extends Object3D {
   public static final byte BORDER = 32;
   public static final byte REPEAT = 33;
   private int m_color = 0;
   private Image2D m_image = null;
   private int m_imageModeX;
   private int m_imageModeY;
   private int m_cropX;
   private int m_cropY;
   private int m_cropWidth;
   private int m_cropHeight;
   private boolean m_depthClearEnabled = true;
   private boolean m_colorClearEnabled = true;
   private Texture2D m_backgroundTexture = null;

   public Background() {
      this.m_imageModeX = this.m_imageModeY = 32;
      this.m_cropX = this.m_cropY = this.m_cropWidth = this.m_cropHeight = 0;
   }

   public void setColorClearEnable(boolean enable) {
      this.m_colorClearEnabled = enable;
   }

   public boolean isColorClearEnabled() {
      return this.m_colorClearEnabled;
   }

   public void setDepthClearEnable(boolean enable) {
      this.m_depthClearEnabled = enable;
   }

   public boolean isDepthClearEnabled() {
      return this.m_depthClearEnabled;
   }

   public void setColor(int ARGB) {
      this.m_color = ARGB;
   }

   public int getColor() {
      return this.m_color;
   }

   public void setImage(Image2D image) {
      if (image != null && image.getFormat() != 99 && image.getFormat() != 100) {
         throw new IllegalArgumentException("Background: image is not in RGB or RGBA format");
      } else {
         this.m_image = image;
         if (image != null) {
            this.setCrop(0, 0, image.getWidth(), image.getHeight());
            this.m_backgroundTexture = null;
         }

      }
   }

   public Image2D getImage() {
      return this.m_image;
   }

   public void setImageMode(int modeX, int modeY) {
      if ((modeX == 32 || modeX == 33) && (modeY == 32 || modeY == 33)) {
         this.m_imageModeX = modeX;
         this.m_imageModeY = modeY;
      } else {
         throw new IllegalArgumentException("Background: modeX or modeY is not one of the enumerated values");
      }
   }

   public int getImageModeX() {
      return this.m_imageModeX;
   }

   public int getImageModeY() {
      return this.m_imageModeY;
   }

   public void setCrop(int cropX, int cropY, int width, int height) {
      if (width < 0) {
         throw new IllegalArgumentException("Background: width < 0");
      } else if (height < 0) {
         throw new IllegalArgumentException("Background: height < 0");
      } else {
         this.m_cropX = cropX;
         this.m_cropY = cropY;
         this.m_cropWidth = width;
         this.m_cropHeight = height;
      }
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

      return numReferences;
   }

   public int getObjectType() {
      return 4;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setColor(is.readColorRGBA());
      long index = is.readObjectIndex();
      if (index != 0L) {
         M3GObject obj = this.getObjectAtIndex(table, index, 10);
         if (obj == null) {
            throw new IOException("Background:image-index = " + index);
         }

         this.setImage((Image2D)obj);
      }

      this.setImageMode(is.readByte(), is.readByte());
      this.setCrop((int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32());
      this.setDepthClearEnable(is.readBoolean());
      this.setColorClearEnable(is.readBoolean());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeColorRGBA(this.m_color);
      if (this.m_image != null) {
         int index = table.indexOf(this.m_image);
         if (index <= 0) {
            throw new IOException("Background:image-index = " + index);
         }

         os.writeObjectIndex((long)index);
      } else {
         os.writeObjectIndex(0L);
      }

      os.writeByte(this.m_imageModeX);
      os.writeByte(this.m_imageModeY);
      os.writeInt32(this.m_cropX);
      os.writeInt32(this.m_cropY);
      os.writeInt32(this.m_cropWidth);
      os.writeInt32(this.m_cropHeight);
      os.writeBoolean(this.m_depthClearEnabled);
      os.writeBoolean(this.m_colorClearEnabled);
   }

   protected void buildReferenceTable(ArrayList table) {
      if (this.m_image != null) {
         this.m_image.buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }

   public void setupGL(GL gl) {
      int clearBits = 0;
      Color c = new Color(this.m_color);
      gl.glClearColor(c.m_r, c.m_g, c.m_b, c.m_a);
      if (this.m_colorClearEnabled) {
         clearBits |= 16384;
      }

      if (this.m_depthClearEnabled) {
         clearBits |= 256;
      }

      if (clearBits != 0) {
         gl.glClear(clearBits);
      }

      if (this.m_image != null) {
         if (this.m_backgroundTexture == null) {
            this.m_backgroundTexture = new Texture2D(this.m_image);
            this.m_backgroundTexture.setFiltering(209, 209);
            this.m_backgroundTexture.setWrapping(240, 240);
            this.m_backgroundTexture.setBlending(228);
         }

         gl.glMatrixMode(5888);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         gl.glMatrixMode(5889);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         gl.glColorMask(true, true, true, true);
         gl.glDepthMask(false);
         gl.glDisable(2896);
         gl.glDisable(2884);
         gl.glDisable(3042);
         Graphics3D.getInstance().disableTextureUnits();
         gl.glActiveTexture(33984);
         this.m_backgroundTexture.setupGL(gl, new float[]{1.0F, 0.0F, 0.0F, 0.0F});
         int w = Graphics3D.getInstance().getViewportWidth();
         int h = Graphics3D.getInstance().getViewportHeight();
         if (this.m_cropWidth <= 0) {
            this.m_cropWidth = w;
         }

         if (this.m_cropHeight <= 0) {
            this.m_cropHeight = h;
         }

         float u0 = (float)this.m_cropX / (float)w;
         float u1 = u0 + (float)this.m_cropWidth / (float)w;
         float v0 = (float)this.m_cropY / (float)h;
         float v1 = v0 + (float)this.m_cropHeight / (float)h;
         gl.glBegin(7);
         gl.glTexCoord2f(u0, u0);
         gl.glVertex3f(-1.0F, 1.0F, 0.0F);
         gl.glTexCoord2f(u1, v0);
         gl.glVertex3f(1.0F, 1.0F, 0.0F);
         gl.glTexCoord2f(u1, v1);
         gl.glVertex3f(1.0F, -1.0F, 0.0F);
         gl.glTexCoord2f(u0, v1);
         gl.glVertex3f(-1.0F, -1.0F, 0.0F);
         gl.glEnd();
         gl.glPopMatrix();
         gl.glMatrixMode(5888);
         gl.glPopMatrix();
         gl.glDisable(3553);
      }

   }
}
