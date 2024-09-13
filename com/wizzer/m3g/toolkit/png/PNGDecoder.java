package com.wizzer.m3g.toolkit.png;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.zip.CRC32;

public class PNGDecoder {
   public static BufferedImage decode(InputStream in) throws IOException {
      DataInputStream dataIn = new DataInputStream(in);
      return decode(dataIn);
   }

   public static BufferedImage decode(DataInputStream in) throws IOException {
      readSignature(in);
      PNGData chunks = readChunks(in);
      long widthLong = chunks.getWidth();
      long heightLong = chunks.getHeight();
      if (widthLong <= 2147483647L && heightLong <= 2147483647L) {
         int width = (int)widthLong;
         int height = (int)heightLong;
         ColorModel cm = chunks.getColorModel();
         WritableRaster raster = chunks.getRaster();
         BufferedImage image = new BufferedImage(cm, raster, false, (Hashtable)null);
         return image;
      } else {
         throw new IOException("That image is too wide or tall.");
      }
   }

   public static void readSignature(DataInputStream in) throws IOException {
      long signature = in.readLong();
      if (signature != -8552249625308161526L) {
         throw new IOException("PNG signature not found!");
      }
   }

   public static PNGData readChunks(DataInputStream in) throws IOException {
      PNGData chunks = new PNGData();
      boolean working = true;

      while(working) {
         try {
            int length = in.readInt();
            if (length < 0) {
               throw new IOException("Sorry, that file is too long.");
            }

            byte[] typeBytes = new byte[4];
            in.readFully(typeBytes);
            byte[] data = new byte[length];
            in.readFully(data);
            long crc = (long)in.readInt() & 4294967295L;
            if (!verifyCRC(typeBytes, data, crc)) {
               throw new IOException("That file appears to be corrupted.");
            }

            PNGChunk chunk = new PNGChunk(typeBytes, data);
            chunks.add(chunk);
         } catch (EOFException var9) {
            working = false;
         }
      }

      return chunks;
   }

   protected static boolean verifyCRC(byte[] typeBytes, byte[] data, long crc) {
      CRC32 crc32 = new CRC32();
      crc32.update(typeBytes);
      crc32.update(data);
      long calculated = crc32.getValue();
      return calculated == crc;
   }
}
