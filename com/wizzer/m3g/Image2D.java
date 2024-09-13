package com.wizzer.m3g;

import com.sun.opengl.util.BufferUtil;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image2D extends Object3D {
   public static final byte ALPHA = 96;
   public static final byte LUMINANCE = 97;
   public static final byte LUMINANCE_ALPHA = 98;
   public static final byte RGB = 99;
   public static final byte RGBA = 100;
   private BufferedImage m_originalImage;
   private BufferedImage m_nativeImage;
   private int m_format;
   private boolean m_mutable;
   private int m_width;
   private int m_height;
   private byte[] m_image;
   private byte[] m_palette;

   public Image2D(int format, BufferedImage image) {
      if (format >= 96 && format <= 100) {
         this.m_format = format;
         this.setImage(image);
      } else {
         throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
      }
   }

   public Image2D(int format, int width, int height, byte[] image) {
      if (width <= 0) {
         throw new IllegalArgumentException("Image2D: width <= 0");
      } else if (height <= 0) {
         throw new IllegalArgumentException("Image2D: height <= 0");
      } else if (format >= 96 && format <= 100) {
         int bpp = 1;
         if (format == 98) {
            bpp = 2;
         } else if (format == 99) {
            bpp = 3;
         } else if (format == 100) {
            bpp = 4;
         }

         if (image.length < width * height * bpp) {
            throw new IllegalArgumentException("Image2D: image.length <  width*height*bpp");
         } else {
            this.m_format = format;
            this.m_width = width;
            this.m_height = height;
            this.m_image = image;
            this.m_palette = new byte[0];
            this.m_mutable = false;
            this.m_originalImage = this.createNativeImage();
            this.m_nativeImage = this.createNativeImage();
         }
      } else {
         throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
      }
   }

   public Image2D(int format, int width, int height, byte[] image, byte[] palette) {
      if (width <= 0) {
         throw new IllegalArgumentException("Image2D: width <= 0");
      } else if (height <= 0) {
         throw new IllegalArgumentException("Image2D: height <= 0");
      } else if (format >= 96 && format <= 100) {
         if (image.length < width * height) {
            throw new IllegalArgumentException("Image2D: image.length <  width*height");
         } else {
            int bpp = 1;
            if (format == 98) {
               bpp = 2;
            } else if (format == 99) {
               bpp = 3;
            } else if (format == 100) {
               bpp = 4;
            }

            if (palette.length < 256 * bpp && palette.length % bpp != 0) {
               throw new IllegalArgumentException("Image2D: (palette.length <  256*C) && ((palette.length % bpp) != 0), where C is the number of color components (for instance, 3 for RGB)");
            } else {
               this.m_format = format;
               this.m_width = width;
               this.m_height = height;
               this.m_image = image;
               if (palette.length > 256) {
                  this.m_palette = new byte[palette.length];
                  System.arraycopy(palette, 0, this.m_palette, 0, palette.length);
               } else {
                  this.m_palette = palette;
               }

               this.m_mutable = false;
               this.m_originalImage = this.createNativeImage();
               this.m_nativeImage = this.createNativeImage();
            }
         }
      } else {
         throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
      }
   }

   public Image2D(int format, int width, int height) {
      if (width <= 0) {
         throw new IllegalArgumentException("Image2D: width <= 0");
      } else if (height <= 0) {
         throw new IllegalArgumentException("Image2D: height <= 0");
      } else if (format >= 96 && format <= 100) {
         this.m_format = format;
         this.m_width = width;
         this.m_height = height;
         this.m_mutable = true;
         this.m_originalImage = this.m_nativeImage = new BufferedImage(width, height, 2);

         for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
               this.m_originalImage.setRGB(x, y, -1);
            }
         }

      } else {
         throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
      }
   }

   public void set(int x, int y, int width, int height, byte[] image) {
      if (image == null) {
         throw new NullPointerException("Image2D: image is null");
      } else if (!this.m_mutable) {
         throw new IllegalStateException("Image2D: image is immutable");
      } else if (x >= 0 && y >= 0 && width > 0 && height > 0) {
         if (x + width <= this.m_width && y + height <= this.m_height) {
            int bpp = 1;
            if (this.m_format == 98) {
               bpp = 2;
            } else if (this.m_format == 99) {
               bpp = 3;
            } else if (this.m_format == 100) {
               bpp = 4;
            }

            if (image.length < width * height * bpp) {
               throw new IllegalArgumentException("");
            } else {
               Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Image2D", "set(int x,int y,int width,int height,byte image[])", "Not implemented");
            }
         } else {
            throw new IllegalArgumentException("");
         }
      } else {
         throw new IllegalArgumentException("Image2D: x < 0 or y < 0 or width <= 0 or height <= 0");
      }
   }

   public boolean isMutable() {
      return this.m_mutable;
   }

   public int getFormat() {
      return this.m_format;
   }

   public int getWidth() {
      return this.m_width;
   }

   public int getHeight() {
      return this.m_height;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   Image2D() {
   }

   public void setFormat(int format) {
      if (format >= 96 && format <= 100) {
         this.m_format = format;
         this.setImage(this.m_originalImage);
      } else {
         throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
      }
   }

   public void setImage(BufferedImage image) {
      if (this.isMutable()) {
         throw new IllegalStateException("Image2D: this image is mutable");
      } else {
         int bpp = 1;
         if (this.m_format == 98) {
            bpp = 2;
         } else if (this.m_format == 99) {
            bpp = 3;
         } else if (this.m_format == 100) {
            bpp = 4;
         }

         this.m_width = image.getWidth();
         this.m_height = image.getHeight();
         ColorModel cm = image.getColorModel();
         int i;
         int y;
         int x;
         if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            this.m_palette = new byte[icm.getMapSize() * bpp];
            i = 0;

            for(y = 0; i < icm.getMapSize(); ++i) {
               if (this.m_format != 96 && this.m_format != 97 && this.m_format != 98) {
                  this.m_palette[y++] = (byte)icm.getRed(i);
                  this.m_palette[y++] = (byte)icm.getGreen(i);
                  this.m_palette[y++] = (byte)icm.getBlue(i);
                  if (this.m_format == 100) {
                     this.m_palette[y++] = (byte)icm.getAlpha(i);
                  }
               } else {
                  byte value = (byte)((icm.getRed(i) + icm.getGreen(i) + icm.getBlue(i)) / 3);
                  this.m_palette[y++] = value;
                  if (this.m_format == 98) {
                     this.m_palette[y++] = value;
                  }
               }
            }

            Raster raster = image.getData();
            this.m_image = new byte[this.m_width * this.m_height];

            for(y = 0; y < this.m_height; ++y) {
               for(x = 0; x < this.m_width; ++x) {
                  this.m_image[x + y * this.m_width] = (byte)raster.getSample(x, y, 0);
               }
            }
         } else {
            this.m_palette = new byte[0];
            this.m_image = new byte[this.m_width * this.m_height * bpp];
            int i = 0;

            for(i = 0; i < this.m_height; ++i) {
               for(y = 0; y < this.m_width; ++y) {
                  x = image.getRGB(y, i);
                  int a = x >> 24 & 255;
                  int r = x >> 16 & 255;
                  int g = x >> 8 & 255;
                  int b = x & 255;
                  if (this.m_format != 96 && this.m_format != 97 && this.m_format != 98) {
                     this.m_image[i++] = (byte)r;
                     this.m_image[i++] = (byte)g;
                     this.m_image[i++] = (byte)b;
                     if (this.m_format == 100) {
                        this.m_image[i++] = (byte)a;
                     }
                  } else {
                     byte value = (byte)((r + g + b) / 3);
                     this.m_image[i++] = value;
                     if (this.m_format == 98) {
                        this.m_image[i++] = value;
                     }
                  }
               }
            }
         }

         this.m_originalImage = image;
         this.m_nativeImage = this.createNativeImage();
      }
   }

   public BufferedImage getImage() {
      return this.m_nativeImage;
   }

   private BufferedImage createNativeImage() {
      BufferedImage nativeImage = null;
      int bpp = 1;
      if (this.m_format == 98) {
         bpp = 2;
      } else if (this.m_format == 99) {
         bpp = 3;
      } else if (this.m_format == 100) {
         bpp = 4;
      }

      int r;
      int g;
      int b;
      if (this.m_palette.length > 0) {
         byte[] r = new byte[this.m_palette.length / bpp];
         byte[] g = new byte[this.m_palette.length / bpp];
         byte[] b = new byte[this.m_palette.length / bpp];
         byte[] a = new byte[this.m_palette.length / bpp];
         Arrays.fill(a, (byte)-1);
         r = 0;

         for(g = 0; r < r.length; ++r) {
            if (this.m_format != 96 && this.m_format != 97 && this.m_format != 98) {
               r[r] = this.m_palette[g++];
               g[r] = this.m_palette[g++];
               b[r] = this.m_palette[g++];
               if (this.m_format == 100) {
                  a[r] = this.m_palette[g++];
               }
            } else {
               r[r] = g[r] = b[r] = this.m_palette[g++];
               if (this.m_format == 96 || this.m_format == 98) {
                  a[r] = r[r];
               }

               if (this.m_format == 98) {
                  ++g;
               }
            }
         }

         IndexColorModel icm = new IndexColorModel(8, r.length, r, g, b, a);
         nativeImage = new BufferedImage(this.getWidth(), this.getHeight(), 13, icm);

         for(g = 0; g < this.m_height; ++g) {
            for(b = 0; b < this.m_width; ++b) {
               int index = this.m_image[b + g * this.m_width] & 255;
               nativeImage.setRGB(b, g, (a[index] & 255) << 24 | (r[index] & 255) << 16 | (g[index] & 255) << 8 | b[index] & 255);
            }
         }
      } else {
         nativeImage = new BufferedImage(this.getWidth(), this.getHeight(), 2);
         int i = 0;

         for(int y = 0; y < this.m_height; ++y) {
            for(int x = 0; x < this.m_width; ++x) {
               int a = 255;
               int r = true;
               int g = true;
               int b = true;
               if (this.m_format != 96 && this.m_format != 97 && this.m_format != 98) {
                  r = this.m_image[i++] & 255;
                  g = this.m_image[i++] & 255;
                  b = this.m_image[i++] & 255;
                  if (this.m_format == 100) {
                     a = this.m_image[i++] & 255;
                  }
               } else {
                  r = g = b = this.m_image[i++] & 255;
                  if (this.m_format == 96 || this.m_format == 98) {
                     a = r;
                  }

                  if (this.m_format == 98) {
                     ++i;
                  }
               }

               nativeImage.setRGB(x, y, a << 24 | r << 16 | g << 8 | b);
            }
         }
      }

      return nativeImage;
   }

   public int getObjectType() {
      return 10;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_format = is.readByte();
      if (this.m_format >= 96 && this.m_format <= 100) {
         this.m_mutable = is.readBoolean();
         this.m_width = (int)is.readUInt32();
         this.m_height = (int)is.readUInt32();
         if (!this.m_mutable) {
            this.m_palette = new byte[(int)is.readUInt32()];
            is.read(this.m_palette);
            this.m_image = new byte[(int)is.readUInt32()];
            is.read(this.m_image);
         }

         this.m_originalImage = this.createNativeImage();
         this.m_nativeImage = this.createNativeImage();
      } else {
         throw new IOException("Image2D.format=" + this.m_format);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeByte(this.m_format);
      os.writeBoolean(this.m_mutable);
      os.writeUInt32((long)this.m_width);
      os.writeUInt32((long)this.m_height);
      if (!this.m_mutable) {
         os.writeUInt32((long)this.m_palette.length);
         os.write(this.m_palette);
         os.writeUInt32((long)this.m_image.length);
         os.write(this.m_image);
      }

   }

   ByteBuffer getPixels() {
      int bpp = this.getBytesPerPixel();
      ByteBuffer pixels = BufferUtil.newByteBuffer(this.m_width * this.m_height * bpp);
      if (this.m_palette.length > 0) {
         byte[] r = new byte[this.m_palette.length / bpp];
         byte[] g = new byte[this.m_palette.length / bpp];
         byte[] b = new byte[this.m_palette.length / bpp];
         byte[] a = new byte[this.m_palette.length / bpp];
         Arrays.fill(a, (byte)-1);
         int y = 0;

         int x;
         for(x = 0; y < r.length; ++y) {
            if (this.m_format != 96 && this.m_format != 97 && this.m_format != 98) {
               r[y] = this.m_palette[x++];
               g[y] = this.m_palette[x++];
               b[y] = this.m_palette[x++];
               if (this.m_format == 100) {
                  a[y] = this.m_palette[x++];
               }
            } else {
               r[y] = g[y] = b[y] = this.m_palette[x++];
               if (this.m_format == 96 || this.m_format == 98) {
                  a[y] = r[y];
               }

               if (this.m_format == 98) {
                  ++x;
               }
            }
         }

         for(y = 0; y < this.m_height; ++y) {
            for(x = 0; x < this.m_width; ++x) {
               int index = this.m_image[x + y * this.m_width] & 255;
               pixels.put((byte)(r[index] & 255));
               pixels.put((byte)(g[index] & 255));
               pixels.put((byte)(b[index] & 255));
               if (this.m_format == 100) {
                  pixels.put((byte)(a[index] & 255));
               }
            }
         }
      } else {
         pixels.put(this.m_image, 0, this.m_width * this.m_height * bpp);
      }

      pixels.flip();
      return pixels;
   }

   int getBytesPerPixel() {
      if (this.m_format == 100) {
         return 4;
      } else if (this.m_format == 99) {
         return 3;
      } else {
         return this.m_format == 98 ? 2 : 1;
      }
   }

   int getGLFormat() {
      if (this.m_format == 100) {
         return 6408;
      } else if (this.m_format == 99) {
         return 6407;
      } else if (this.m_format == 98) {
         return 6410;
      } else if (this.m_format == 97) {
         return 6409;
      } else if (this.m_format == 96) {
         return 6406;
      } else {
         throw new RuntimeException("Invalid format on image");
      }
   }
}
