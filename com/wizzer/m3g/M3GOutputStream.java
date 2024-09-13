package com.wizzer.m3g;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.Adler32;

public class M3GOutputStream extends FilterOutputStream {
   private Adler32 m_adler32 = new Adler32();
   private boolean m_blockAdler32;

   public M3GOutputStream(OutputStream os) {
      super(os);
   }

   public void write(int b) throws IOException {
      super.write(b);
      if (!this.m_blockAdler32) {
         this.m_adler32.update(b);
      }

   }

   public void write(byte[] b, int off, int len) throws IOException {
      this.m_blockAdler32 = true;
      super.write(b, off, len);
      this.m_adler32.update(b, off, len);
      this.m_blockAdler32 = false;
   }

   public void writeByte(byte b) throws IOException {
      this.write(b);
   }

   public void writeByte(int b) throws IOException {
      this.writeByte((byte)b);
   }

   public void writeInt16(short s) throws IOException {
      this.writeByte((byte)(s & 255));
      this.writeByte((byte)(s >> 8 & 255));
   }

   public void writeInt16(int i) throws IOException {
      this.writeInt16((short)i);
   }

   public void writeUInt16(short s) throws IOException {
      this.writeInt16(s);
   }

   public void writeUInt16(int i) throws IOException {
      this.writeInt16((short)i);
   }

   public void writeInt32(int i) throws IOException {
      this.writeInt16((short)(i & '\uffff'));
      this.writeInt16((short)(i >> 16 & '\uffff'));
   }

   public void writeUInt32(long i) throws IOException {
      this.writeInt32((int)(i & -1L));
   }

   public void writeFloat32(float f) throws IOException {
      this.writeInt32(Float.floatToIntBits(f));
   }

   public void writeString(String s) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStreamWriter w = new OutputStreamWriter(baos, "UTF-8");
      w.write(s);
      w.flush();
      baos.writeTo(this);
      this.write(0);
   }

   public void writeBoolean(boolean b) throws IOException {
      this.writeByte(b ? 1 : 0);
   }

   public void writeVector3D(float[] v) throws IOException {
      for(int i = 0; i < 3; ++i) {
         this.writeFloat32(v[i]);
      }

   }

   public void writeMatrix(Transform m) throws IOException {
      float[] t = new float[16];
      m.get(t);

      for(int i = 0; i < 16; ++i) {
         this.writeFloat32(t[i]);
      }

   }

   public void writeColorRGB(int rgb) throws IOException {
      this.writeByte((byte)(rgb >> 16 & 255));
      this.writeByte((byte)(rgb >> 8 & 255));
      this.writeByte((byte)(rgb & 255));
   }

   public void writeColorRGBA(int argb) throws IOException {
      this.writeColorRGB(argb);
      this.writeByte((byte)(argb >> 24 & 255));
   }

   public void writeObjectIndex(long index) throws IOException {
      this.writeUInt32(index);
   }

   public void resetAdler32() {
      this.m_adler32.reset();
   }

   public long getAdler32Value() {
      return this.m_adler32.getValue();
   }
}
