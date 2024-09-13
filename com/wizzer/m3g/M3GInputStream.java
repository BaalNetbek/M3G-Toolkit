package com.wizzer.m3g;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;

public class M3GInputStream extends FilterInputStream {
   private Adler32 m_adler32 = new Adler32();
   private boolean m_blockAdler32;

   public M3GInputStream(InputStream in) {
      super(in);
   }

   public int read() throws IOException {
      int b = super.read();
      if (!this.m_blockAdler32) {
         this.m_adler32.update(b);
      }

      return b;
   }

   public int read(byte[] b, int off, int len) throws IOException {
      this.m_blockAdler32 = true;
      len = super.read(b, off, len);
      this.m_adler32.update(b, off, len);
      this.m_blockAdler32 = false;
      return len;
   }

   public int readByte() throws IOException {
      return this.read() & 255;
   }

   public int readInt16() throws IOException {
      int l = this.readByte();
      int h = this.readByte();
      return (short)(h << 8 | l & 255);
   }

   public int readUInt16() throws IOException {
      return this.readInt16() & '\uffff';
   }

   public long readInt32() throws IOException {
      int l = this.readUInt16();
      int h = this.readUInt16();
      return (long)(h << 16 | l & '\uffff');
   }

   public long readUInt32() throws IOException {
      return this.readInt32() & -1L;
   }

   public float readFloat32() throws IOException {
      return Float.intBitsToFloat((int)(this.readInt32() & -1L));
   }

   public String readString() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      boolean var2 = true;

      int i;
      while((i = this.read()) > 0) {
         baos.write(i);
      }

      return new String(baos.toByteArray(), "UTF-8");
   }

   public boolean readBoolean() throws IOException {
      return this.readByte() != 0;
   }

   public float[] readVector3D() throws IOException {
      float[] v = new float[3];

      for(int i = 0; i < 3; ++i) {
         v[i] = this.readFloat32();
      }

      return v;
   }

   public Transform readMatrix() throws IOException {
      float[] m = new float[16];

      for(int i = 0; i < 16; ++i) {
         m[i] = this.readFloat32();
      }

      Transform t = new Transform();
      t.set(m);
      return t;
   }

   public int readColorRGB() throws IOException {
      return -16777216 | this.readByte() << 16 | this.readByte() << 8 | this.readByte();
   }

   public int readColorRGBA() throws IOException {
      return this.readByte() << 16 | this.readByte() << 8 | this.readByte() | this.readByte() << 24;
   }

   public long readObjectIndex() throws IOException {
      return this.readUInt32();
   }

   public void resetAdler32() {
      this.m_adler32.reset();
   }

   public long getAdler32Value() {
      return this.m_adler32.getValue();
   }
}
