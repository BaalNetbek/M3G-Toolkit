package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group extends Node {
   private ArrayList m_children = new ArrayList();

   public void addChild(Node child) {
      if (child == null) {
         throw new NullPointerException("Group: child is null");
      } else if (child == this) {
         throw new IllegalArgumentException("Group: child is this Group");
      } else if (child instanceof World) {
         throw new IllegalArgumentException("Group: child is a World node");
      } else if (child.m_parent != null) {
         throw new IllegalArgumentException("Group: child already has a parent");
      } else {
         for(Node parent = this.getParent(); parent != null; parent = parent.getParent()) {
            if (parent == child) {
               throw new IllegalArgumentException("Group: child is an ancestor of this Group");
            }
         }

         this.m_children.add(child);
         child.m_parent = this;
      }
   }

   public void removeChild(Node child) {
      if (this.m_children.remove(child)) {
         child.m_parent = null;
      }

   }

   public int getChildCount() {
      return this.m_children.size();
   }

   public Node getChild(int index) {
      return (Node)this.m_children.get(index);
   }

   public boolean pick(int scope, float ox, float oy, float oz, float dx, float dy, float dz, RayIntersection ri) {
      Logger.global.logp(Level.WARNING, "com.wizzwe.m3g.Group", "pick(int scope,float ox,float oy,float oz,float dx,float dy,float dz,RayIntersection ri)", "Not implemented");
      return false;
   }

   public boolean pick(int scope, float x, float y, Camera camera, RayIntersection ri) {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Group", "pick(int scope,float x,float y,Camera camera,RayIntersection ri)", "Not implemented");
      return false;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (references != null) {
         for(int i = 0; i < this.m_children.size(); ++i) {
            references[numReferences + i] = (Object3D)this.m_children.get(i);
         }
      }

      return numReferences + this.m_children.size();
   }

   public int getObjectType() {
      return 9;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long childs = is.readUInt32();

      for(int i = 0; (long)i < childs; ++i) {
         long index = is.readObjectIndex();
         M3GObject obj = this.getObjectAtIndex(table, index, -1);
         if (obj == null || !(obj instanceof Node)) {
            throw new IOException("Group:child-index = " + index);
         }

         this.addChild((Node)obj);
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeUInt32((long)this.getChildCount());

      for(int i = 0; i < this.getChildCount(); ++i) {
         int index = table.indexOf(this.getChild(i));
         if (index <= 0) {
            throw new IOException("Group:child-index = " + index);
         }

         os.writeObjectIndex((long)index);
      }

   }

   protected void buildReferenceTable(ArrayList table) {
      for(int i = 0; i < this.getChildCount(); ++i) {
         this.getChild(i).buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }
}
