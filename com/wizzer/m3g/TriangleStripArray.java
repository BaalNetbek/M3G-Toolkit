package com.wizzer.m3g;

import com.wizzer.m3g.nvtristrip.NvTriStrip;
import com.wizzer.m3g.nvtristrip.PrimitiveGroup;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Deflater;

public class TriangleStripArray extends IndexBuffer {
   private int m_firstIndex;
   private int[] m_indices;
   private int[] m_stripLengths;
   private int m_encoding;

   public TriangleStripArray(int firstIndex, int[] stripLengths) {
      int sum = this.checkInput(stripLengths);
      if (firstIndex < 0) {
         throw new IndexOutOfBoundsException("TriangleStripArray: firstIndex < 0");
      } else if (firstIndex + sum > 65535) {
         throw new IndexOutOfBoundsException("TriangleStripArray: firstIndex + sum(stripLengths) > 65535");
      } else {
         this.m_firstIndex = firstIndex;
         this.m_stripLengths = stripLengths;
         this.fillIndexBuffer(sum, firstIndex, stripLengths);
      }
   }

   public TriangleStripArray(int[] indices, int[] stripLengths) {
      if (indices == null) {
         throw new NullPointerException("TriangleStripArray: indices is null");
      } else {
         int sum = this.checkInput(stripLengths);
         if (indices.length < sum) {
            throw new IndexOutOfBoundsException("TriangleStripArray: indices.length <  sum(stripLengths)");
         } else {
            for(int i = 0; i < indices.length; ++i) {
               if (indices[i] < 0) {
                  throw new IllegalArgumentException("TriangleStripArray: any element in indices is negative");
               }

               if (indices[i] > 65535) {
                  throw new IllegalArgumentException("TriangleStripArray: any element in indices is greater than 65535");
               }
            }

            this.m_indices = indices;
            this.m_stripLengths = stripLengths;
            this.fillIndexBuffer(sum, indices, stripLengths);
         }
      }
   }

   public void getIndices(int[] indices) {
      if (indices == null) {
         throw new NullPointerException("TriangleStripArray: indices is null");
      } else if (indices.length < this.getIndexCount()) {
         throw new IllegalArgumentException("TriangleStripArray: indices length not big enough");
      }
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   TriangleStripArray() {
   }

   public int getObjectType() {
      return 11;
   }

   public int getEncoding() {
      return this.m_encoding;
   }

   public int getStartIndex() {
      return this.m_firstIndex;
   }

   public int[] getIndices() {
      return this.m_indices;
   }

   public int[] getStripLengths() {
      return this.m_stripLengths;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      int encoding = is.readByte();
      int i;
      if ((encoding & 128) == 0) {
         if (encoding == 0) {
            this.m_firstIndex = (int)is.readUInt32();
         } else if (encoding == 1) {
            this.m_firstIndex = is.readByte();
         } else {
            if (encoding != 2) {
               throw new IOException("TriangleStripArray.encoding = " + encoding);
            }

            this.m_firstIndex = is.readUInt16();
         }
      } else {
         this.m_indices = new int[(int)is.readUInt32()];

         for(i = 0; i < this.m_indices.length; ++i) {
            if (encoding == 128) {
               this.m_indices[i] = (int)is.readUInt32();
            } else if (encoding == 129) {
               this.m_indices[i] = is.readByte();
            } else {
               if (encoding != 130) {
                  throw new IOException("TriangleStripArray.encoding = " + encoding);
               }

               this.m_indices[i] = is.readUInt16();
            }
         }
      }

      i = (int)is.readUInt32();
      int[] stripLengths = new int[i];

      int sum;
      for(sum = 0; sum < i; ++sum) {
         stripLengths[sum] = (int)is.readUInt32();
      }

      sum = this.checkInput(stripLengths);
      if (this.m_indices == null) {
         this.fillIndexBuffer(sum, this.m_firstIndex, stripLengths);
      } else {
         this.fillIndexBuffer(sum, this.m_indices, stripLengths);
      }

      this.m_encoding = encoding;
      this.m_stripLengths = stripLengths;
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      if (this.m_indices == null) {
         if (this.m_firstIndex < 256) {
            os.writeByte((int)1);
            os.writeByte(this.m_firstIndex);
         } else if (this.m_firstIndex < 65536) {
            os.writeByte((int)2);
            os.writeUInt16(this.m_firstIndex);
         } else {
            os.writeByte((int)0);
            os.writeUInt32((long)this.m_firstIndex);
         }

         os.writeUInt32((long)this.m_stripLengths.length);

         for(int i = 0; i < this.m_stripLengths.length; ++i) {
            os.writeUInt32((long)this.m_stripLengths[i]);
         }
      } else {
         byte[] data = this.encode(this.m_indices, this.m_stripLengths);
         int compressedLength = this.getCompressedLength(data);
         int[] faces = this.getRawFaces();

         for(int i = 0; i < 2; ++i) {
            for(int j = 1; j <= 10; ++j) {
               int[][] stripData = this.strip(faces, j, i == 0);
               byte[] data2 = this.encode(stripData[0], stripData[1]);
               int compressedLength2 = this.getCompressedLength(data2);
               if (compressedLength2 < compressedLength) {
                  data = data2;
                  compressedLength = compressedLength2;
               }
            }
         }

         os.write(data);
      }

   }

   private byte[] encode(int[] indices, int[] stripLengths) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      M3GOutputStream os = new M3GOutputStream(baos);
      int max = 0;

      int i;
      for(i = 0; i < indices.length; ++i) {
         max = Math.max(indices[i], max);
      }

      if (max < 256) {
         os.writeByte((int)129);
         os.writeUInt32((long)indices.length);

         for(i = 0; i < indices.length; ++i) {
            os.writeByte(indices[i]);
         }
      } else if (max < 65536) {
         os.writeByte((int)130);
         os.writeUInt32((long)indices.length);

         for(i = 0; i < indices.length; ++i) {
            os.writeUInt16(indices[i]);
         }
      } else {
         os.writeByte((int)128);
         os.writeUInt32((long)indices.length);

         for(i = 0; i < indices.length; ++i) {
            os.writeUInt32((long)indices[i]);
         }
      }

      os.writeUInt32((long)stripLengths.length);

      for(i = 0; i < stripLengths.length; ++i) {
         os.writeUInt32((long)stripLengths[i]);
      }

      return baos.toByteArray();
   }

   private int getCompressedLength(byte[] data) {
      Deflater deflater = new Deflater(9, false);
      deflater.setInput(data);
      deflater.finish();
      return deflater.deflate(new byte[data.length << 1]);
   }

   private int[] getRawFaces() {
      int nfaces = 0;

      for(int i = 0; i < this.m_stripLengths.length; ++i) {
         nfaces += this.m_stripLengths[i] - 2;
      }

      int[] faces = new int[nfaces * 3];
      nfaces = 0;
      int i = 0;

      for(int j = 0; i < this.m_stripLengths.length; j += 2) {
         for(int k = 0; k < this.m_stripLengths[i] - 2; ++j) {
            int v0 = this.m_indices[j];
            int v1 = this.m_indices[j + 1];
            int v2 = this.m_indices[j + 2];
            if (!this.isDegenerate(v0, v1, v2)) {
               if ((k & 1) == 0) {
                  faces[nfaces++] = v0;
                  faces[nfaces++] = v1;
                  faces[nfaces++] = v2;
               } else {
                  faces[nfaces++] = v2;
                  faces[nfaces++] = v1;
                  faces[nfaces++] = v0;
               }
            }

            ++k;
         }

         ++i;
      }

      int[] temp = new int[nfaces];
      System.arraycopy(faces, 0, temp, 0, nfaces);
      return temp;
   }

   private boolean isDegenerate(int v0, int v1, int v2) {
      if (v0 == v1) {
         return true;
      } else if (v0 == v2) {
         return true;
      } else {
         return v1 == v2;
      }
   }

   private int[][] strip(int[] faces, int minStripSize, boolean stitchStrips) {
      NvTriStrip strip = new NvTriStrip();
      strip.setStitchStrips(stitchStrips);
      strip.setMinStripSize(minStripSize);
      PrimitiveGroup[] groups = strip.generateStrips(faces);
      int nindices = 0;
      int nlengths = 0;

      int index1;
      for(int i = 0; i < groups.length; ++i) {
         PrimitiveGroup group = groups[i];
         if (group.m_numIndices > 0) {
            if (group.m_type == 0) {
               index1 = 0;

               while(index1 < group.m_numIndices) {
                  if (!this.isDegenerate(group.m_indices[index1++], group.m_indices[index1++], group.m_indices[index1++])) {
                     nindices += 3;
                     ++nlengths;
                  }
               }
            } else if (group.m_type == 1) {
               nindices += group.m_numIndices;
               ++nlengths;
            }
         }
      }

      int[][] data = new int[][]{new int[nindices], new int[nlengths]};
      int i = 0;
      index1 = 0;

      for(int var11 = 0; i < groups.length; ++i) {
         PrimitiveGroup group = groups[i];
         if (group.m_numIndices > 0) {
            if (group.m_type == 0) {
               int j = 0;

               while(j < group.m_numIndices) {
                  int a = groups[i].m_indices[j++];
                  int b = groups[i].m_indices[j++];
                  int c = groups[i].m_indices[j++];
                  if (!this.isDegenerate(a, b, c)) {
                     data[0][index1++] = a;
                     data[0][index1++] = b;
                     data[0][index1++] = c;
                     data[1][var11++] = 3;
                  }
               }
            } else if (group.m_type == 1) {
               System.arraycopy(group.m_indices, 0, data[0], index1, group.m_numIndices);
               data[1][var11++] = group.m_numIndices;
               index1 += group.m_numIndices;
            }
         }
      }

      return data;
   }

   private int checkInput(int[] stripLengths) {
      int sum = 0;
      if (stripLengths == null) {
         throw new NullPointerException("TriangleStripArray: stripLegths can not be null");
      } else {
         int l = stripLengths.length;
         if (l == 0) {
            throw new IllegalArgumentException("TriangleStripArray: stripLenghts can not be empty");
         } else {
            for(int i = 0; i < l; ++i) {
               if (stripLengths[i] < 3) {
                  throw new IllegalArgumentException("TriangleStripArray: stripLengths must not contain elemets less than 3");
               }

               sum += stripLengths[i];
            }

            return sum;
         }
      }
   }

   private void fillIndexBuffer(int sum, int firstIndex, int[] stripLengths) {
      this.allocate(sum + (stripLengths.length - 1) * 3);
      int index = firstIndex;

      for(int i = 0; i < stripLengths.length; ++i) {
         if (i != 0) {
            this.m_buffer.put(index - 1);
            this.m_buffer.put(index);
            if (this.m_buffer.position() % 2 == 1) {
               this.m_buffer.put(index);
            }
         }

         for(int s = 0; s < stripLengths[i]; ++s) {
            this.m_buffer.put(index++);
         }
      }

      this.m_buffer.flip();
   }

   private void fillIndexBuffer(int sum, int[] indices, int[] stripLengths) {
      this.allocate(sum + (stripLengths.length - 1) * 3);
      int index = 0;

      for(int i = 0; i < stripLengths.length; ++i) {
         if (i != 0) {
            this.m_buffer.put(indices[index - 1]);
            this.m_buffer.put(indices[index]);
            if (this.m_buffer.position() % 2 == 1) {
               this.m_buffer.put(indices[index]);
            }
         }

         for(int s = 0; s < stripLengths[i]; ++s) {
            this.m_buffer.put(indices[index++]);
         }
      }

      this.m_buffer.flip();
   }
}
