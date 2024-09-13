package com.wizzer.m3g.toolkit.png;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

class PNGData {
   private int m_numberOfChunks = 0;
   private PNGChunk[] m_chunks = new PNGChunk[10];

   public PNGData() {
   }

   public void add(PNGChunk chunk) {
      this.m_chunks[this.m_numberOfChunks++] = chunk;
      if (this.m_numberOfChunks >= this.m_chunks.length) {
         PNGChunk[] largerArray = new PNGChunk[this.m_chunks.length + 10];
         System.arraycopy(this.m_chunks, 0, largerArray, 0, this.m_chunks.length);
         this.m_chunks = largerArray;
      }

   }

   public long getWidth() {
      return this.getChunk("IHDR").getUnsignedInt(0);
   }

   public long getHeight() {
      return this.getChunk("IHDR").getUnsignedInt(4);
   }

   public short getBitsPerPixel() {
      return this.getChunk("IHDR").getUnsignedByte(8);
   }

   public short getColorType() {
      return this.getChunk("IHDR").getUnsignedByte(9);
   }

   public short getCompression() {
      return this.getChunk("IHDR").getUnsignedByte(10);
   }

   public short getFilter() {
      return this.getChunk("IHDR").getUnsignedByte(11);
   }

   public short getInterlace() {
      return this.getChunk("IHDR").getUnsignedByte(12);
   }

   public ColorModel getColorModel() {
      short colorType = this.getColorType();
      int bitsPerPixel = this.getBitsPerPixel();
      if (colorType == 3) {
         byte[] paletteData = this.getChunk("PLTE").getData();
         int paletteLength = paletteData.length / 3;
         return new IndexColorModel(bitsPerPixel, paletteLength, paletteData, 0, false);
      } else {
         System.out.println("Unsupported color type: " + colorType);
         return null;
      }
   }

   public WritableRaster getRaster() {
      int width = (int)this.getWidth();
      int height = (int)this.getHeight();
      int bitsPerPixel = this.getBitsPerPixel();
      short colorType = this.getColorType();
      if (colorType == 3) {
         byte[] imageData = this.getImageData();
         DataBuffer db = new DataBufferByte(imageData, imageData.length);
         WritableRaster raster = Raster.createPackedRaster(db, width, height, bitsPerPixel, (Point)null);
         return raster;
      } else {
         System.out.println("Unsupported color type!");
         return null;
      }
   }

   public byte[] getImageData() {
      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream();

         for(int i = 0; i < this.m_numberOfChunks; ++i) {
            PNGChunk chunk = this.m_chunks[i];
            if (chunk.getTypeString().equals("IDAT")) {
               out.write(chunk.getData());
            }
         }

         out.flush();
         InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(out.toByteArray()));
         ByteArrayOutputStream inflatedOut = new ByteArrayOutputStream();
         byte[] block = new byte[8192];

         int readLength;
         while((readLength = in.read(block)) != -1) {
            inflatedOut.write(block, 0, readLength);
         }

         inflatedOut.flush();
         byte[] imageData = inflatedOut.toByteArray();
         int width = (int)this.getWidth();
         int height = (int)this.getHeight();
         int bitsPerPixel = this.getBitsPerPixel();
         int length = width * height * bitsPerPixel / 8;
         byte[] prunedData = new byte[length];
         if (this.getInterlace() == 0) {
            int index = 0;

            for(int i = 0; i < length; ++i) {
               if (i * 8 / bitsPerPixel % width == 0) {
                  ++index;
               }

               prunedData[i] = imageData[index++];
            }
         } else {
            System.out.println("Couldn't undo interlacing.");
         }

         return prunedData;
      } catch (IOException var14) {
         return null;
      }
   }

   public PNGChunk getChunk(String type) {
      for(int i = 0; i < this.m_numberOfChunks; ++i) {
         if (this.m_chunks[i].getTypeString().equals(type)) {
            return this.m_chunks[i];
         }
      }

      return null;
   }
}
