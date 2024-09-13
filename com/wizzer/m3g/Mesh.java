package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class Mesh extends Node {
   private VertexBuffer m_vertexBuffer;
   private IndexBuffer[] m_indexBuffers;
   private Appearance[] m_appearances;

   public Mesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance) {
      this(vertices, new IndexBuffer[]{submesh}, new Appearance[]{appearance});
   }

   public Mesh(VertexBuffer vertices, IndexBuffer[] submeshes, Appearance[] appearances) {
      if (vertices == null) {
         throw new NullPointerException("Mesh: vertexBuffer is null");
      } else if (submeshes == null) {
         throw new NullPointerException("Mesh: indexBuffers is null");
      } else {
         for(int i = 0; i < submeshes.length; ++i) {
            if (submeshes[i] == null) {
               throw new NullPointerException("Mesh: any element in indexBuffers  is null");
            }
         }

         if (submeshes.length == 0) {
            throw new IllegalArgumentException("Mesh: indexBuffers is empty");
         } else if (appearances.length < submeshes.length) {
            throw new IllegalArgumentException("Mesh: appearances.length < indexBuffers.length");
         } else {
            this.m_vertexBuffer = vertices;
            this.m_indexBuffers = submeshes;
            this.m_appearances = appearances;
         }
      }
   }

   public void setAppearance(int index, Appearance appearance) {
      this.m_appearances[index] = appearance;
   }

   public Appearance getAppearance(int index) {
      return this.m_appearances[index];
   }

   public IndexBuffer getIndexBuffer(int index) {
      return this.m_indexBuffers[index];
   }

   public VertexBuffer getVertexBuffer() {
      return this.m_vertexBuffer;
   }

   public int getSubmeshCount() {
      return this.m_indexBuffers.length;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_vertexBuffer != null) {
         if (references != null) {
            references[numReferences] = this.m_vertexBuffer;
         }

         ++numReferences;
      }

      int i;
      for(i = 0; i < this.m_indexBuffers.length; ++i) {
         if (references != null) {
            references[numReferences] = this.m_indexBuffers[i];
         }

         ++numReferences;
      }

      for(i = 0; i < this.m_appearances.length; ++i) {
         if (references != null) {
            references[numReferences] = this.m_appearances[i];
         }

         ++numReferences;
      }

      return numReferences;
   }

   Mesh() {
   }

   public int getObjectType() {
      return 14;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      M3GObject obj = this.getObjectAtIndex(table, index, 21);
      if (obj == null) {
         throw new IOException("Mesh:vertexBuffer-index = " + index);
      } else {
         this.m_vertexBuffer = (VertexBuffer)obj;
         long submeshCount = is.readUInt32();
         this.m_indexBuffers = new IndexBuffer[(int)submeshCount];
         this.m_appearances = new Appearance[(int)submeshCount];

         for(int i = 0; (long)i < submeshCount; ++i) {
            index = is.readObjectIndex();
            obj = this.getObjectAtIndex(table, index, -1);
            if (obj == null || !(obj instanceof IndexBuffer)) {
               throw new IOException("Mesh:indexBuffers-index = " + index);
            }

            this.m_indexBuffers[i] = (IndexBuffer)obj;
            if ((index = is.readObjectIndex()) != 0L) {
               obj = this.getObjectAtIndex(table, index, 3);
               if (obj == null) {
                  throw new IOException("Mesh:appearances-index = " + index);
               }

               this.m_appearances[i] = (Appearance)obj;
            }
         }

      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_vertexBuffer);
      if (index <= 0) {
         throw new IOException("Mesh:vertexBuffer-index = " + index);
      } else {
         os.writeObjectIndex((long)index);
         os.writeUInt32((long)this.getSubmeshCount());

         for(int i = 0; i < this.getSubmeshCount(); ++i) {
            index = table.indexOf(this.m_indexBuffers[i]);
            if (index <= 0) {
               throw new IOException("Mesh:indexBuffers-index = " + index);
            }

            os.writeObjectIndex((long)index);
            if (this.m_appearances[i] != null) {
               index = table.indexOf(this.m_appearances[i]);
               if (index <= 0) {
                  throw new IOException("Mesh:appearances-index = " + index);
               }

               os.writeObjectIndex((long)index);
            } else {
               os.writeObjectIndex(0L);
            }
         }

      }
   }

   protected void buildReferenceTable(ArrayList table) {
      this.m_vertexBuffer.buildReferenceTable(table);

      int i;
      for(i = 0; i < this.getSubmeshCount(); ++i) {
         this.m_indexBuffers[i].buildReferenceTable(table);
      }

      for(i = 0; i < this.getSubmeshCount(); ++i) {
         if (this.m_appearances[i] != null) {
            this.m_appearances[i].buildReferenceTable(table);
         }
      }

      super.buildReferenceTable(table);
   }
}
