package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class World extends Group {
   private Camera m_activeCamera;
   private Background m_background;

   public void setBackground(Background background) {
      this.m_background = background;
   }

   public Background getBackground() {
      return this.m_background;
   }

   public void setActiveCamera(Camera camera) {
      if (camera == null) {
         throw new NullPointerException("World: camera is null");
      } else {
         this.m_activeCamera = camera;
      }
   }

   public Camera getActiveCamera() {
      return this.m_activeCamera;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_activeCamera != null) {
         if (references != null) {
            references[numReferences] = this.m_activeCamera;
         }

         ++numReferences;
      }

      if (this.m_background != null) {
         if (references != null) {
            references[numReferences] = this.m_background;
         }

         ++numReferences;
      }

      return numReferences;
   }

   public int getObjectType() {
      return 22;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      M3GObject obj = this.getObjectAtIndex(table, index, 5);
      if (obj != null) {
         this.m_activeCamera = (Camera)obj;
         if ((index = is.readObjectIndex()) != 0L) {
            obj = this.getObjectAtIndex(table, index, 4);
            if (obj == null) {
               throw new IOException("World:background-index = " + index);
            }

            this.m_background = (Background)obj;
         }

      } else {
         throw new IOException("World:activeCamera-index = " + index);
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_activeCamera);
      if (index > 0) {
         os.writeObjectIndex((long)index);
         if (this.m_background != null) {
            index = table.indexOf(this.m_background);
            if (index <= 0) {
               throw new IOException("World:background-index = " + index);
            }

            os.writeObjectIndex((long)index);
         } else {
            os.writeObjectIndex(0L);
         }

      } else {
         throw new IOException("World:activeCamera-index = " + index);
      }
   }

   protected void buildReferenceTable(ArrayList table) {
      if (this.m_activeCamera == null) {
         throw new NullPointerException("World:activeCamera is null");
      } else {
         this.m_activeCamera.buildReferenceTable(table);
         if (this.m_background != null) {
            this.m_background.buildReferenceTable(table);
         }

         super.buildReferenceTable(table);
      }
   }
}
