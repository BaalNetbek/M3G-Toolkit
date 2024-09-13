package com.wizzer.m3g.toolkit.util;

public class Unsigned {
   public static final int writeMAC(char[] data, int offset, long value) {
      value &= 281474976710655L;
      data[offset + 0] = (char)((int)(value >> 40 & 255L));
      data[offset + 1] = (char)((int)(value >> 32 & 255L));
      data[offset + 2] = (char)((int)(value >> 24 & 255L));
      data[offset + 3] = (char)((int)(value >> 16 & 255L));
      data[offset + 4] = (char)((int)(value >> 8 & 255L));
      data[offset + 5] = (char)((int)(value & 255L));
      return 6;
   }

   public static final int writeMAC(byte[] data, int offset, long value) {
      value &= 281474976710655L;
      data[offset + 0] = (byte)((int)(value >> 40 & 255L));
      data[offset + 1] = (byte)((int)(value >> 32 & 255L));
      data[offset + 2] = (byte)((int)(value >> 24 & 255L));
      data[offset + 3] = (byte)((int)(value >> 16 & 255L));
      data[offset + 4] = (byte)((int)(value >> 8 & 255L));
      data[offset + 5] = (byte)((int)(value & 255L));
      return 6;
   }

   public static final int writeWORD(char[] data, int offset, int value) {
      data[offset + 0] = (char)(value >> 8 & 255);
      data[offset + 1] = (char)(value & 255);
      return 2;
   }

   public static final int writeBYTE(char[] data, int offset, int value) {
      data[offset] = (char)(value & 255);
      return 1;
   }

   public static final int writeQWORD(byte[] data, int offset, long value) {
      data[offset + 0] = (byte)((int)(value >> 56 & 255L));
      data[offset + 1] = (byte)((int)(value >> 48 & 255L));
      data[offset + 2] = (byte)((int)(value >> 40 & 255L));
      data[offset + 3] = (byte)((int)(value >> 32 & 255L));
      data[offset + 4] = (byte)((int)(value >> 24 & 255L));
      data[offset + 5] = (byte)((int)(value >> 16 & 255L));
      data[offset + 6] = (byte)((int)(value >> 8 & 255L));
      data[offset + 7] = (byte)((int)(value >> 0 & 255L));
      return 8;
   }

   public static final int writeDWORD(char[] data, int offset, long value) {
      data[offset + 0] = (char)((int)(value >> 24 & 255L));
      data[offset + 1] = (char)((int)(value >> 16 & 255L));
      data[offset + 2] = (char)((int)(value >> 8 & 255L));
      data[offset + 3] = (char)((int)(value & 255L));
      return 4;
   }

   public static final int writeDWORD(byte[] data, int offset, long value) {
      data[offset + 0] = (byte)((int)(value >> 24 & 255L));
      data[offset + 1] = (byte)((int)(value >> 16 & 255L));
      data[offset + 2] = (byte)((int)(value >> 8 & 255L));
      data[offset + 3] = (byte)((int)(value & 255L));
      return 4;
   }

   public static final int writeWORD(byte[] data, int offset, int value) {
      data[offset + 0] = (byte)(value >> 8 & 255);
      data[offset + 1] = (byte)(value & 255);
      return 2;
   }

   public static final int writeBYTE(byte[] data, int offset, int value) {
      data[offset] = (byte)(value & 255);
      return 1;
   }

   public static final short readBYTE(byte[] data, int offset) {
      short value = (short)(data[offset] & 255);
      return value;
   }

   public static final int readWORD(byte[] data, int offset) {
      int value = (data[offset] & 255) << 8 | data[offset + 1] & 255;
      return value;
   }

   public static final long readDWORD(byte[] data, int offset) {
      long value = ((long)data[offset] & 255L) << 24 | (long)((data[offset + 1] & 255) << 16) | (long)((data[offset + 2] & 255) << 8) | (long)(data[offset + 3] & 255);
      return value;
   }

   public static final long readQWORD(byte[] data, int offset) {
      long value = ((long)data[offset + 0] & 255L) << 56 | ((long)data[offset + 1] & 255L) << 48 | ((long)data[offset + 2] & 255L) << 40 | ((long)data[offset + 3] & 255L) << 32 | ((long)data[offset + 4] & 255L) << 24 | ((long)data[offset + 5] & 255L) << 16 | ((long)data[offset + 6] & 255L) << 8 | ((long)data[offset + 7] & 255L) << 0;
      return value;
   }

   public static final long readMAC(byte[] data, int offset) {
      long low = ((long)data[offset + 2] & 255L) << 24 | (long)((data[offset + 3] & 255) << 16) | (long)((data[offset + 4] & 255) << 8) | (long)(data[offset + 5] & 255);
      long hi = ((long)data[offset + 0] & 255L) << 8 | (long)(data[offset + 1] & 255);
      long value = hi << 32 | low;
      return value;
   }
}
