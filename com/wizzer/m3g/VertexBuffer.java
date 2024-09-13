package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class VertexBuffer extends Object3D {
   private int m_defaultColor = -1;
   private VertexArray m_positions;
   private float m_positionScale;
   private float[] m_positionBias = new float[3];
   private VertexArray m_normals;
   private VertexArray m_colors;
   private VertexArray[] m_texCoords = new VertexArray[256];
   private float[] m_texCoordScale = new float[256];
   private float[][] m_texCoordBias = new float[256][3];
   private int m_texcoordArrayCount;

   public int getVertexCount() {
      VertexArray va = this.m_positions;
      if (va == null) {
         va = this.m_normals;
      }

      if (va == null) {
         va = this.m_colors;
      }

      for(int i = 0; i < this.m_texCoords.length && va == null; ++i) {
         if (this.m_texCoords[i] != null) {
            va = this.m_texCoords[i];
         }
      }

      return va != null ? va.m_vertexCount : 0;
   }

   public void setPositions(VertexArray positions, float scale, float[] bias) {
      if (bias == null) {
         bias = new float[3];
      }

      if (positions != null && positions.m_componentCount != 3) {
         throw new IllegalArgumentException("VertexBuffer: positions.numComponents != 3");
      } else if (positions != null && positions.m_vertexCount != this.getVertexCount() && this.getVertexCount() > 0) {
         throw new IllegalArgumentException("VertexBuffer: (positions.numVertices != getVertexCount) && (getVertexCount > 0)");
      } else if (positions != null && bias.length < 3) {
         throw new IllegalArgumentException("VertexBuffer: (positions != null) &&  (bias.length < 3)");
      } else {
         this.m_positions = positions;
         this.m_positionScale = scale;
         this.m_positionBias = new float[]{bias[0], bias[1], bias[2]};
      }
   }

   public VertexArray getPositions(float[] scaleBias) {
      if (scaleBias != null) {
         if (scaleBias.length < 4) {
            throw new IllegalArgumentException("VertexBuffer: scaleBias.length < 4");
         }

         scaleBias[0] = this.m_positionScale;
         System.arraycopy(this.m_positionBias, 0, scaleBias, 1, this.m_positionBias.length);
      }

      return this.m_positions;
   }

   public void setTexCoords(int index, VertexArray texCoords, float scale, float[] bias) {
      if (bias == null) {
         bias = new float[3];
      }

      if ((texCoords == null || texCoords.m_componentCount >= 2) && texCoords.m_componentCount <= 3) {
         if (texCoords != null && texCoords.m_vertexCount != this.getVertexCount() && this.getVertexCount() > 0) {
            throw new IllegalArgumentException("VertexBuffer: (texCoords.numVertices != getVertexCount) && (getVertexCount > 0)");
         } else if (texCoords != null && bias.length < texCoords.m_componentCount) {
            throw new IllegalArgumentException("(VertexBuffer: texCoords != null) &&  (bias.length < texCoords.numComponents)");
         } else {
            this.m_texCoords[index] = texCoords;
            this.m_texCoordScale[index] = scale;
            this.m_texCoordBias[index] = new float[]{bias[0], bias[1], 0.0F};
            if (bias.length == 3) {
               this.m_texCoordBias[index][2] = bias[2];
            }

         }
      } else {
         throw new IllegalArgumentException("VertexBuffer: texCoords.numComponents != [2,3]");
      }
   }

   public VertexArray getTexCoords(int index, float[] scaleBias) {
      if (scaleBias != null) {
         if (scaleBias.length < 4) {
            throw new IllegalArgumentException("VertexBuffer: scaleBias.length <  numComponents+1");
         }

         scaleBias[0] = this.m_texCoordScale[index];
         System.arraycopy(this.m_texCoordBias[index], 0, scaleBias, 1, this.m_texCoordBias[index].length);
      }

      return this.m_texCoords[index];
   }

   public void setNormals(VertexArray normals) {
      if (normals != null && normals.m_componentCount != 3) {
         throw new IllegalArgumentException("VertexBuffer: normals.numComponents != 3");
      } else if (normals != null && normals.m_vertexCount != this.getVertexCount() && this.getVertexCount() > 0) {
         throw new IllegalArgumentException("VertexBuffer: (normals.numVertices != getVertexCount) && (getVertexCount > 0)");
      } else {
         this.m_normals = normals;
      }
   }

   public VertexArray getNormals() {
      return this.m_normals;
   }

   public void setColors(VertexArray colors) {
      if (colors != null && colors.m_componentCount != 1) {
         throw new IllegalArgumentException("VertexBuffer: colors.numComponents != 1");
      } else if (colors != null && (colors.m_componentCount < 3 || colors.m_componentCount > 4)) {
         throw new IllegalArgumentException("VertexBuffer: colors.numComponents != [3,4]");
      } else if (colors != null && colors.m_vertexCount != this.getVertexCount() && this.getVertexCount() > 0) {
         throw new IllegalArgumentException("VertexBuffer: (colors.numVertices != getVertexCount) && (getVertexCount > 0)");
      } else {
         this.m_colors = colors;
      }
   }

   public VertexArray getColors() {
      return this.m_colors;
   }

   public void setDefaultColor(int ARGB) {
      this.m_defaultColor = ARGB;
   }

   public int getDefaultColor() {
      return this.m_defaultColor;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_positions != null) {
         if (references != null) {
            references[numReferences] = this.m_positions;
         }

         ++numReferences;
      }

      if (this.m_normals != null) {
         if (references != null) {
            references[numReferences] = this.m_normals;
         }

         ++numReferences;
      }

      if (this.m_colors != null) {
         if (references != null) {
            references[numReferences] = this.m_colors;
         }

         ++numReferences;
      }

      for(int i = 0; i < this.m_texCoords.length; ++i) {
         if (this.m_texCoords[i] != null) {
            if (references != null) {
               references[numReferences] = this.m_texCoords[i];
            }

            ++numReferences;
         }
      }

      return numReferences;
   }

   public int getObjectType() {
      return 21;
   }

   public int getTexcoordArrayCount() {
      return this.m_texcoordArrayCount;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.m_defaultColor = is.readColorRGBA();
      long index = 0L;
      M3GObject obj;
      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 20);
         if (obj == null) {
            throw new IOException("VertexBuffer:positions-index = " + index);
         }

         this.m_positions = (VertexArray)obj;
      }

      for(int i = 0; i < 3; ++i) {
         this.m_positionBias[i] = is.readFloat32();
      }

      this.m_positionScale = is.readFloat32();
      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 20);
         if (obj == null) {
            throw new IOException("VertexBuffer:normals-index = " + index);
         }

         this.m_normals = (VertexArray)obj;
      }

      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 20);
         if (obj == null) {
            throw new IOException("VertexBuffer:colors-index = " + index);
         }

         this.m_colors = (VertexArray)obj;
      }

      long units = is.readUInt32();

      for(int i = 0; (long)i < units; ++i) {
         if ((index = is.readObjectIndex()) > 0L) {
            M3GObject obj = this.getObjectAtIndex(table, index, 20);
            if (obj == null) {
               throw new IOException("VertexBuffer:texCoords-index = " + index);
            }

            this.m_texCoords[i] = (VertexArray)obj;
         }

         for(int j = 0; j < 3; ++j) {
            this.m_texCoordBias[i][j] = is.readFloat32();
         }

         this.m_texCoordScale[i] = is.readFloat32();
      }

      this.m_texcoordArrayCount = (int)units;
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeColorRGBA(this.m_defaultColor);
      int units;
      if (this.m_positions != null) {
         units = table.indexOf(this.m_positions);
         if (units <= 0) {
            throw new IOException("VertexBuffer:positions-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      for(units = 0; units < 3; ++units) {
         os.writeFloat32(this.m_positionBias[units]);
      }

      os.writeFloat32(this.m_positionScale);
      if (this.m_normals != null) {
         units = table.indexOf(this.m_normals);
         if (units <= 0) {
            throw new IOException("VertexBuffer:normals-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      if (this.m_colors != null) {
         units = table.indexOf(this.m_colors);
         if (units <= 0) {
            throw new IOException("VertexBuffer:colors-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      for(units = this.m_texCoords.length; units > 0 && this.m_texCoords[units - 1] == null; --units) {
      }

      os.writeUInt32((long)units);

      for(int i = 0; i < units; ++i) {
         if (this.m_texCoords[i] == null) {
            os.writeObjectIndex(0L);
         } else {
            int index = table.indexOf(this.m_texCoords[i]);
            if (index <= 0) {
               throw new IOException("VertexBuffer:texCoords-index = " + index);
            }

            os.writeObjectIndex((long)index);

            for(int j = 0; j < 3; ++j) {
               os.writeFloat32(this.m_texCoordBias[i][j]);
            }

            os.writeFloat32(this.m_texCoordScale[i]);
         }
      }

   }

   protected void buildReferenceTable(ArrayList table) {
      if (this.m_positions != null) {
         this.m_positions.buildReferenceTable(table);
      }

      if (this.m_normals != null) {
         this.m_normals.buildReferenceTable(table);
      }

      if (this.m_colors != null) {
         this.m_colors.buildReferenceTable(table);
      }

      for(int i = 0; i < this.m_texCoords.length; ++i) {
         if (this.m_texCoords[i] != null) {
            this.m_texCoords[i].buildReferenceTable(table);
         }
      }

      super.buildReferenceTable(table);
   }
}
