package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class Appearance extends Object3D {
   private int m_layer = 0;
   private CompositingMode m_compositingMode;
   private Fog m_fog;
   private PolygonMode m_polygonMode;
   private Material m_material;
   private Texture2D[] m_textures = new Texture2D[256];

   public void setLayer(int layer) {
      if (layer >= -63 && layer <= 63) {
         this.m_layer = layer;
      } else {
         throw new IndexOutOfBoundsException("Appearance: layer is not in [-63, 63]");
      }
   }

   public int getLayer() {
      return this.m_layer;
   }

   public void setFog(Fog fog) {
      this.m_fog = fog;
   }

   public Fog getFog() {
      return this.m_fog;
   }

   public void setPolygonMode(PolygonMode polygonMode) {
      this.m_polygonMode = polygonMode;
   }

   public PolygonMode getPolygonMode() {
      return this.m_polygonMode;
   }

   public void setCompositingMode(CompositingMode compositingMode) {
      this.m_compositingMode = compositingMode;
   }

   public CompositingMode getCompositingMode() {
      return this.m_compositingMode;
   }

   public void setTexture(int index, Texture2D texture) {
      this.m_textures[index] = texture;
   }

   public Texture2D getTexture(int index) {
      return this.m_textures[index];
   }

   public void setMaterial(Material material) {
      this.m_material = material;
   }

   public Material getMaterial() {
      return this.m_material;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_material != null) {
         if (references != null) {
            references[numReferences] = this.m_material;
         }

         ++numReferences;
      }

      if (this.m_polygonMode != null) {
         if (references != null) {
            references[numReferences] = this.m_polygonMode;
         }

         ++numReferences;
      }

      if (this.m_compositingMode != null) {
         if (references != null) {
            references[numReferences] = this.m_compositingMode;
         }

         ++numReferences;
      }

      if (this.m_fog != null) {
         if (references != null) {
            references[numReferences] = this.m_fog;
         }

         ++numReferences;
      }

      for(int i = 0; i < this.m_textures.length; ++i) {
         if (this.m_textures[i] != null) {
            if (references != null) {
               references[numReferences] = this.m_textures[i];
            }

            ++numReferences;
         }
      }

      return numReferences;
   }

   public int getObjectType() {
      return 3;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setLayer(is.readByte());
      long index = 0L;
      M3GObject obj;
      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 6);
         if (obj == null) {
            throw new IOException("Appearance:compositingMode-index = " + index);
         }

         this.setCompositingMode((CompositingMode)obj);
      }

      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 7);
         if (obj == null) {
            throw new IOException("Appearance:fog-index = " + index);
         }

         this.setFog((Fog)obj);
      }

      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 8);
         if (obj == null) {
            throw new IOException("Appearance:polygonMode-index = " + index);
         }

         this.setPolygonMode((PolygonMode)obj);
      }

      if ((index = is.readObjectIndex()) != 0L) {
         obj = this.getObjectAtIndex(table, index, 13);
         if (obj == null) {
            throw new IOException("Appearance:material-index = " + index);
         }

         this.setMaterial((Material)obj);
      }

      long units = is.readUInt32();

      for(int i = 0; (long)i < units; ++i) {
         if ((index = is.readObjectIndex()) != 0L) {
            M3GObject obj = this.getObjectAtIndex(table, index, 17);
            if (obj == null) {
               throw new IOException("Appearance:texture-index = " + index);
            }

            this.setTexture(i, (Texture2D)obj);
         }
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeByte(this.m_layer);
      int units;
      if (this.m_compositingMode != null) {
         units = table.indexOf(this.m_compositingMode);
         if (units <= 0) {
            throw new IOException("Appearance:compositingMode-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      if (this.m_fog != null) {
         units = table.indexOf(this.m_fog);
         if (units <= 0) {
            throw new IOException("Appearance:fog-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      if (this.m_polygonMode != null) {
         units = table.indexOf(this.m_polygonMode);
         if (units <= 0) {
            throw new IOException("Appearance:polygonMode-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      if (this.m_material != null) {
         units = table.indexOf(this.m_material);
         if (units <= 0) {
            throw new IOException("Appearance:material-index = " + units);
         }

         os.writeObjectIndex((long)units);
      } else {
         os.writeObjectIndex(0L);
      }

      for(units = this.m_textures.length; units > 0 && this.m_textures[units - 1] == null; --units) {
      }

      os.writeUInt32((long)units);

      for(int i = 0; i < units; ++i) {
         if (this.m_textures[i] != null) {
            int index = table.indexOf(this.m_textures[i]);
            if (index <= 0) {
               throw new IOException("Appearance:texture-index = " + index);
            }

            os.writeObjectIndex((long)index);
         } else {
            os.writeObjectIndex(0L);
         }
      }

   }

   protected void buildReferenceTable(ArrayList table) {
      if (this.m_compositingMode != null) {
         this.m_compositingMode.buildReferenceTable(table);
      }

      if (this.m_fog != null) {
         this.m_fog.buildReferenceTable(table);
      }

      if (this.m_polygonMode != null) {
         this.m_polygonMode.buildReferenceTable(table);
      }

      if (this.m_material != null) {
         this.m_material.buildReferenceTable(table);
      }

      for(int i = 0; i < this.m_textures.length; ++i) {
         if (this.m_textures[i] != null) {
            this.m_textures[i].buildReferenceTable(table);
         }
      }

      super.buildReferenceTable(table);
   }
}
