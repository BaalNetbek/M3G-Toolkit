package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Node extends Transformable {
   public static final int NONE = 144;
   public static final int ORIGIN = 145;
   public static final int X_AXIS = 146;
   public static final int Y_AXIS = 147;
   public static final int Z_AXIS = 148;
   private boolean m_renderingEnabled = true;
   private boolean m_pickingEnabled = true;
   private float m_alphaFactor = 1.0F;
   private int m_scope = -1;
   private boolean m_hasAlignment;
   private int m_zTarget;
   private int m_yTarget;
   private Node m_zReference;
   private Node m_yReference;
   Node m_parent;

   public void setRenderingEnable(boolean enable) {
      this.m_renderingEnabled = enable;
   }

   public boolean isRenderingEnabled() {
      return this.m_renderingEnabled;
   }

   public void setPickingEnable(boolean enable) {
      this.m_pickingEnabled = enable;
   }

   public boolean isPickingEnabled() {
      return this.m_pickingEnabled;
   }

   public void setScope(int scope) {
      this.m_scope = scope;
   }

   public int getScope() {
      return this.m_scope;
   }

   public void setAlphaFactor(float alphaFactor) {
      if (!(alphaFactor < 0.0F) && !(alphaFactor > 1.0F)) {
         this.m_alphaFactor = alphaFactor;
      } else {
         throw new IllegalArgumentException("Node: alphaFactor is negative or greater than 1.0");
      }
   }

   public float getAlphaFactor() {
      return this.m_alphaFactor;
   }

   public Node getParent() {
      return this.m_parent;
   }

   public boolean getTransformTo(Node target, Transform transform) throws ArithmeticException {
      if (target == null) {
         throw new NullPointerException("Node: target must not be null");
      } else if (transform == null) {
         throw new NullPointerException("Node: transform must not be null");
      } else {
         Node node = this;
         int nodeDepth = this.getDepth();
         int targetDepth = target.getDepth();
         Transform tmp = new Transform();
         Transform targetTransform = new Transform();
         Transform nodeTransform = new Transform();

         while(node != target) {
            if (nodeDepth >= targetDepth) {
               node.getCompositeTransform(tmp);
               nodeTransform.postMultiply(tmp);
               node = node.getParent();
               --nodeDepth;
            }

            if (targetDepth >= nodeDepth) {
               target.getCompositeTransform(tmp);
               tmp.postMultiply(targetTransform);
               targetTransform.set(tmp);
               target = target.getParent();
               --targetDepth;
            }
         }

         if (node != null && target != null) {
            transform.set(nodeTransform);
            transform.postMultiply(targetTransform);
            return true;
         } else {
            return false;
         }
      }
   }

   public final void align(Node reference) {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Node", "align(Node reference)", "Not implemented");
   }

   public void setAlignment(Node zRef, int zTarget, Node yRef, int yTarget) {
      if (zTarget >= 144 && zTarget <= 148 && yTarget >= 144 && yTarget <= 148) {
         if (zRef == yRef && zTarget == yTarget && yTarget != 144) {
            throw new IllegalArgumentException("Node: (zRef == yRef) &&  (zTarget == yTarget != NONE)");
         } else if (zRef != this && yRef != this) {
            this.m_zReference = zRef;
            this.m_zTarget = zTarget;
            this.m_yReference = yRef;
            this.m_yTarget = yTarget;
         } else {
            throw new IllegalArgumentException("Node: zRef or yRef is this Node");
         }
      } else {
         throw new IllegalArgumentException("Node: yTarget or zTarget is not one of the symbolic constants");
      }
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   protected Node() {
   }

   public boolean hasAlignment() {
      return this.m_hasAlignment;
   }

   public int getZTarget() {
      return this.m_zTarget;
   }

   public int getYTarget() {
      return this.m_yTarget;
   }

   public Node getZReference() {
      return this.m_zReference;
   }

   public Node getYReference() {
      return this.m_yReference;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setRenderingEnable(is.readBoolean());
      this.setPickingEnable(is.readBoolean());
      this.setAlphaFactor((float)is.readByte() / 255.0F);
      this.setScope((int)is.readUInt32());
      this.m_hasAlignment = is.readBoolean();
      if (this.m_hasAlignment) {
         int zTarget = is.readByte();
         int yTarget = is.readByte();
         Node zReference = null;
         Node yReference = null;
         long index = is.readObjectIndex();
         M3GObject obj;
         if (index != 0L) {
            obj = this.getObjectAtIndex(table, index, -1);
            if (obj == null || !(obj instanceof Node)) {
               throw new IOException("Node:zReference-index = " + index);
            }

            zReference = (Node)obj;
         }

         index = is.readObjectIndex();
         if (index != 0L) {
            obj = this.getObjectAtIndex(table, index, -1);
            if (obj == null || !(obj instanceof Node)) {
               throw new IOException("Node:yReference-index = " + index);
            }

            yReference = (Node)obj;
         }

         this.setAlignment(zReference, zTarget, yReference, yTarget);
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeBoolean(this.m_renderingEnabled);
      os.writeBoolean(this.m_pickingEnabled);
      os.writeByte((byte)((int)(this.m_alphaFactor * 255.0F)));
      os.writeUInt32((long)this.m_scope);
      this.m_hasAlignment = this.m_zReference != null || this.m_yReference != null;
      os.writeBoolean(this.m_hasAlignment);
      if (this.m_hasAlignment) {
         os.writeByte(this.m_zTarget);
         os.writeByte(this.m_yTarget);
         int index = table.indexOf(this.m_zReference);
         if (index <= 0) {
            throw new IOException("Node:zReference-index = " + index);
         }

         os.writeObjectIndex((long)index);
         index = table.indexOf(this.m_yReference);
         if (index <= 0) {
            throw new IOException("Node:yReference-index = " + index);
         }

         os.writeObjectIndex((long)index);
      }

   }

   protected void buildReferenceTable(ArrayList table) {
      if (this.m_zReference != null) {
         this.m_zReference.buildReferenceTable(table);
      }

      if (this.m_yReference != null) {
         this.m_yReference.buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }

   private int getDepth() {
      int depth = 0;

      for(Node node = this; node != null; node = node.getParent()) {
         ++depth;
      }

      return depth;
   }
}
