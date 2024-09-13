package com.wizzer.m3g.toolkit.png;

import java.io.UnsupportedEncodingException;

class PNGChunk {
   private byte[] m_type;
   private byte[] m_data;

   public PNGChunk(byte[] type, byte[] data) {
      this.m_type = type;
      this.m_data = data;
   }

   public String getTypeString() {
      try {
         return new String(this.m_type, "UTF8");
      } catch (UnsupportedEncodingException var2) {
         return "";
      }
   }

   public byte[] getData() {
      return this.m_data;
   }

   public long getUnsignedInt(int offset) {
      long value = 0L;

      for(int i = 0; i < 4; ++i) {
         value += (long)((this.m_data[offset + i] & 255) << (3 - i) * 8);
      }

      return value;
   }

   public short getUnsignedByte(int offset) {
      return (short)(this.m_data[offset] & 255);
   }
}
