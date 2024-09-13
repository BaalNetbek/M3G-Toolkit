package com.wizzer.m3g;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class Section {
   public static final int UNCOMPRESSED = 0;
   public static final int ZLIB = 1;
   private int m_compressionScheme = 1;
   private int m_totalSectionLength;
   private int m_uncompressedLength;
   private int m_checksum;

   public void setCompressionScheme(int compressionScheme) {
      this.m_compressionScheme = compressionScheme;
   }

   public int getCompressionScheme() {
      return this.m_compressionScheme;
   }

   public int getTotalSectionLength() {
      return this.m_totalSectionLength;
   }

   public int getUncompressedLength() {
      return this.m_uncompressedLength;
   }

   public int getChecksum() {
      return this.m_checksum;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      is.resetAdler32();
      this.m_compressionScheme = is.readByte();
      this.m_totalSectionLength = (int)is.readUInt32();
      this.m_uncompressedLength = (int)is.readUInt32();
      byte[] data = new byte[this.m_uncompressedLength];
      if (this.m_compressionScheme == 1) {
         byte[] compressed = new byte[this.m_totalSectionLength - 13];
         is.read(compressed);

         try {
            Inflater inflater = new Inflater(false);
            inflater.setInput(compressed);
            inflater.inflate(data);
            inflater.end();
         } catch (Exception var6) {
            throw new IOException("Section:ZLIB");
         }
      } else if (this.m_compressionScheme == 0) {
         is.read(data);
      }

      int checksum_is = (int)is.getAdler32Value();
      this.m_checksum = (int)is.readUInt32();
      if (this.getChecksum() != checksum_is) {
         throw new IOException("Section:checksum = " + this.m_checksum);
      } else {
         this.readObjects(new M3GInputStream(new ByteArrayInputStream(data)), table);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      this.writeObjects(new M3GOutputStream(baos), table);
      this.m_uncompressedLength = baos.size();
      byte[] data = baos.toByteArray();
      if (this.m_compressionScheme == 1) {
         Deflater deflater = new Deflater(9, false);
         deflater.setInput(data);
         deflater.finish();
         byte[] compressed = new byte[data.length << 1];
         int length = deflater.deflate(compressed);
         data = new byte[length];
         System.arraycopy(compressed, 0, data, 0, length);
         deflater.end();
      }

      os.resetAdler32();
      os.writeByte(this.m_compressionScheme);
      os.writeUInt32((long)(this.m_totalSectionLength = data.length + 13));
      os.writeUInt32((long)this.m_uncompressedLength);
      os.write(data);
      os.writeUInt32((long)(this.m_checksum = (int)os.getAdler32Value()));
   }

   protected abstract void readObjects(M3GInputStream var1, ArrayList var2) throws IOException;

   protected abstract void writeObjects(M3GOutputStream var1, ArrayList var2) throws IOException;
}
