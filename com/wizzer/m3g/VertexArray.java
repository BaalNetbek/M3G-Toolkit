package com.wizzer.m3g;

import com.sun.opengl.util.BufferUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.zip.Deflater;

public class VertexArray extends Object3D {
   int m_componentSize;
   int m_componentCount;
   int m_vertexCount;
   int m_encoding;
   private Buffer m_buffer;
   private FloatBuffer m_floatBuffer;

   public VertexArray(int numVertices, int numComponents, int componentSize) {
      if (numVertices >= 1 && numVertices <= 65535 && numComponents >= 2 && numComponents <= 4 && componentSize >= 1 && componentSize <= 2) {
         this.m_vertexCount = numVertices;
         this.m_componentCount = numComponents;
         this.m_componentSize = componentSize;
         int numElements = this.m_vertexCount * this.m_componentCount;
         if (componentSize == 1) {
            this.m_buffer = BufferUtil.newByteBuffer(numElements);
         } else {
            this.m_buffer = BufferUtil.newShortBuffer(numElements);
         }

         this.m_floatBuffer = BufferUtil.newFloatBuffer(numElements);
      } else {
         throw new IllegalArgumentException("VertexArray: any of the parameters are outside of their allowed ranges");
      }
   }

   public void set(int firstVertex, int numVertices, short[] values) {
      if (!(this.m_buffer instanceof ShortBuffer)) {
         throw new IllegalStateException("VertexArray: this is not a 16-bit VertexArray");
      } else if (numVertices < 0) {
         throw new IllegalArgumentException("VertexArray: numVertices < 0");
      } else if (values.length < numVertices * this.m_componentCount) {
         throw new IllegalArgumentException("VertexArray: values.length <  numVertices * numComponents");
      } else {
         int numElements = this.m_vertexCount * this.m_componentCount;
         ShortBuffer shortBuffer = (ShortBuffer)this.m_buffer;
         shortBuffer.position(firstVertex);
         shortBuffer.put(values, 0, numElements);
         this.m_floatBuffer.position(firstVertex);

         for(int i = 0; i < numElements; ++i) {
            this.m_floatBuffer.put((float)values[i]);
         }

         this.m_buffer.rewind();
         this.m_floatBuffer.rewind();
      }
   }

   public void set(int firstVertex, int numVertices, byte[] values) {
      if (!(this.m_buffer instanceof ByteBuffer)) {
         throw new IllegalStateException("VertexArray: this is not a 8-bit VertexArray");
      } else if (numVertices < 0) {
         throw new IllegalArgumentException("VertexArray: numVertices < 0");
      } else if (values.length < numVertices * this.m_componentCount) {
         throw new IllegalArgumentException("VertexArray: values.length <  numVertices * numComponents");
      } else {
         int numElements = this.m_vertexCount * this.m_componentCount;
         ByteBuffer byteBuffer = (ByteBuffer)this.m_buffer;
         byteBuffer.position(firstVertex);
         byteBuffer.put(values, 0, numElements);
         this.m_floatBuffer.position(firstVertex);

         for(int i = 0; i < numElements; ++i) {
            this.m_floatBuffer.put((float)values[i]);
         }

         this.m_buffer.rewind();
         this.m_floatBuffer.rewind();
      }
   }

   public void get(int firstVertex, int numVertices, short[] values) {
      int numElements = numVertices * this.m_componentCount;
      this.checkShortInput(firstVertex, numVertices, numElements, values);
      ShortBuffer shortBuffer = (ShortBuffer)this.m_buffer;
      shortBuffer.position(firstVertex);
      shortBuffer.get(values, 0, numElements);
   }

   public void get(int firstVertex, int numVertices, byte[] values) {
      int numElements = numVertices * this.m_componentCount;
      this.checkByteInput(firstVertex, numVertices, numElements, values);
      ByteBuffer byteBuffer = (ByteBuffer)this.m_buffer;
      byteBuffer.position(firstVertex);
      byteBuffer.get(values, 0, numElements);
   }

   private void checkShortInput(int firstVertex, int numVertices, int numElements, short[] values) {
      if (values == null) {
         throw new NullPointerException("VertexArray: values can not be null");
      } else if (this.m_componentSize != 2) {
         throw new IllegalStateException("VertexArray: vertexarray created as short array. can not get byte values");
      } else {
         this.checkInput(firstVertex, numVertices, numElements, values.length);
      }
   }

   private void checkByteInput(int firstVertex, int numVertices, int numElements, byte[] values) {
      if (values == null) {
         throw new NullPointerException("VertexArray: values can not be null");
      } else if (this.m_componentSize != 1) {
         throw new IllegalStateException("VertexArray: vertexarray created as short array. can not set byte values");
      } else {
         this.checkInput(firstVertex, numVertices, numElements, values.length);
      }
   }

   private void checkInput(int firstVertex, int numVertices, int numElements, int arrayLength) {
      if (numVertices < 0) {
         throw new IllegalArgumentException("VertexArray: numVertices must be > 0");
      } else if (arrayLength < numElements) {
         throw new IllegalArgumentException("VertexArray: number of elements i values does not match numVertices");
      } else if (firstVertex < 0 || firstVertex + numVertices > this.m_vertexCount) {
         throw new IndexOutOfBoundsException("VertexArray: index out of bounds");
      }
   }

   public int getComponentCount() {
      return this.m_componentCount;
   }

   public int getVertexCount() {
      return this.m_vertexCount;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   VertexArray() {
   }

   public int getComponentSize() {
      return this.m_componentSize;
   }

   public int getEncoding() {
      return this.m_encoding;
   }

   public int getObjectType() {
      return 20;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_componentSize = is.readByte();
      if (this.m_componentSize >= 1 && this.m_componentSize <= 2) {
         this.m_componentCount = is.readByte();
         if (this.m_componentCount >= 2 && this.m_componentCount <= 4) {
            int encoding = is.readByte();
            if (encoding >= 0 && encoding <= 1) {
               this.m_encoding = encoding;
               this.m_vertexCount = is.readUInt16();
               int numElements = this.m_vertexCount * this.m_componentCount;
               if (this.m_componentSize == 1) {
                  this.m_buffer = BufferUtil.newByteBuffer(numElements);
               } else {
                  this.m_buffer = BufferUtil.newShortBuffer(numElements);
               }

               this.m_floatBuffer = BufferUtil.newFloatBuffer(numElements);
               int i;
               int i;
               int c;
               int c;
               if (this.m_componentSize == 1) {
                  byte[] components = new byte[numElements];
                  if (encoding == 0) {
                     i = 0;

                     for(i = 0; i < this.m_vertexCount; ++i) {
                        for(c = 0; c < this.m_componentCount; ++c) {
                           short value = (short)is.readByte();
                           components[i++] = (byte)(value & 255);
                        }
                     }

                     this.set(0, this.m_vertexCount, (byte[])components);
                  } else if (encoding == 1) {
                     short[] prev = new short[this.m_componentCount];
                     i = 0;

                     for(c = 0; i < this.m_vertexCount; ++i) {
                        for(c = 0; c < this.m_componentCount; ++c) {
                           short value = (short)(is.readByte() + prev[c]);
                           components[c++] = (byte)(value & 255);
                           prev[c] = value;
                        }
                     }

                     this.set(0, this.m_vertexCount, (byte[])components);
                  }
               } else if (this.m_componentSize == 2) {
                  short[] components = new short[numElements];
                  if (encoding == 0) {
                     i = 0;

                     for(i = 0; i < this.m_vertexCount; ++i) {
                        for(c = 0; c < this.m_componentCount; ++c) {
                           c = is.readInt16();
                           components[i++] = (short)(c & '\uffff');
                        }
                     }

                     this.set(0, this.m_vertexCount, (short[])components);
                  } else if (encoding == 1) {
                     int[] prev = new int[this.m_componentCount];
                     i = 0;

                     for(c = 0; i < this.m_vertexCount; ++i) {
                        for(c = 0; c < this.m_componentCount; ++c) {
                           int value = is.readInt16() + prev[c];
                           components[c++] = (short)(value & '\uffff');
                           prev[c] = value;
                        }
                     }

                     this.set(0, this.m_vertexCount, (short[])components);
                  }
               }

            } else {
               throw new IOException("VertexArray:encoding = " + encoding);
            }
         } else {
            throw new IOException("VertexArray:componentCount = " + this.m_componentCount);
         }
      } else {
         throw new IOException("VertexArray:componentSize = " + this.m_componentSize);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int numElements;
      if (this.m_componentSize == 2) {
         ShortBuffer buffer = (ShortBuffer)this.m_buffer;
         numElements = this.m_vertexCount * this.m_componentCount;
         short[] components = new short[numElements];
         buffer.get(components);
         boolean toByte = true;

         for(int i = 0; i < components.length && toByte; ++i) {
            short value = components[i];
            if (value < -128 || value > 127) {
               toByte = false;
            }
         }

         if (toByte) {
            byte[] c = new byte[components.length];

            for(int i = 0; i < components.length; ++i) {
               c[i] = (byte)components[i];
            }

            this.write(os, this.m_vertexCount, this.m_componentCount, 1, c);
         } else {
            this.write(os, this.m_vertexCount, this.m_componentCount, this.m_componentSize, components);
         }
      } else if (this.m_componentSize == 1) {
         ByteBuffer buffer = (ByteBuffer)this.m_buffer;
         numElements = this.m_vertexCount * this.m_componentCount;
         byte[] components = new byte[numElements];
         buffer.get(components);
         this.write(os, this.m_vertexCount, this.m_componentCount, this.m_componentSize, components);
      }

      this.m_buffer.rewind();
   }

   private void write(M3GOutputStream os, int vertexCount, int componentCount, int componentSize, Object components) throws IOException {
      byte[] encoding0 = this.encode(0, vertexCount, componentCount, componentSize, components);
      byte[] encoding1 = this.encode(1, vertexCount, componentCount, componentSize, components);
      int length0 = this.getCompressedLength(encoding0);
      int length1 = this.getCompressedLength(encoding1);
      os.writeByte(componentSize);
      os.writeByte(componentCount);
      os.writeByte(length0 <= length1 ? 0 : 1);
      os.writeUInt16(vertexCount);
      os.write(length0 <= length1 ? encoding0 : encoding1);
   }

   private byte[] encode(int encoding, int vertexCount, int componentCount, int componentSize, Object _components) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      M3GOutputStream os = new M3GOutputStream(baos);
      int i;
      int i;
      int c;
      int c;
      if (componentSize == 1) {
         byte[] components = (byte[])_components;
         if (encoding == 0) {
            i = 0;

            for(i = 0; i < vertexCount; ++i) {
               for(c = 0; c < componentCount; ++c) {
                  os.writeByte(components[i++]);
               }
            }
         } else if (encoding == 1) {
            byte[] prev = new byte[componentCount];
            i = 0;

            for(c = 0; i < vertexCount; ++i) {
               for(c = 0; c < componentCount; ++c) {
                  byte value = components[c++];
                  os.writeByte(value - prev[c]);
                  prev[c] = value;
               }
            }
         }
      } else if (componentSize == 2) {
         short[] components = (short[])_components;
         if (encoding == 0) {
            i = 0;

            for(i = 0; i < vertexCount; ++i) {
               for(c = 0; c < componentCount; ++c) {
                  os.writeInt16(components[i++]);
               }
            }
         } else if (encoding == 1) {
            short[] prev = new short[componentCount];
            i = 0;

            for(c = 0; i < vertexCount; ++i) {
               for(c = 0; c < componentCount; ++c) {
                  short value = components[c++];
                  os.writeInt16(value - prev[c]);
                  prev[c] = value;
               }
            }
         }
      }

      return baos.toByteArray();
   }

   private int getCompressedLength(byte[] data) {
      Deflater deflater = new Deflater(9, false);
      deflater.setInput(data);
      deflater.finish();
      return deflater.deflate(new byte[data.length << 1]);
   }

   int getComponentTypeGL() {
      return this.m_componentSize == 1 ? 5120 : 5122;
   }

   public Buffer getBuffer() {
      return this.m_buffer;
   }

   public FloatBuffer getFloatBuffer() {
      return this.m_floatBuffer;
   }
}
