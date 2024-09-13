package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class SkinnedMesh extends Mesh {
   private Group m_skeleton;
   private ArrayList m_bones = new ArrayList();

   public SkinnedMesh(VertexBuffer vertices, IndexBuffer[] submeshes, Appearance[] appearances, Group skeleton) {
      super(vertices, submeshes, appearances);
      this.m_skeleton = skeleton;
   }

   public SkinnedMesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance, Group skeleton) {
      super(vertices, submesh, appearance);
      this.m_skeleton = skeleton;
   }

   public Group getSkeleton() {
      return this.m_skeleton;
   }

   public void addTransform(Node bone, int weight, int firstVertex, int numVertices) throws ArithmeticException {
      if (bone == null) {
         throw new NullPointerException("SkinnedMesh: bone is null");
      } else if (!this.isDescendant(bone)) {
         throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
      } else if (weight <= 0) {
         throw new IllegalArgumentException("SkinnedMesh: weight <= 0");
      } else if (numVertices <= 0) {
         throw new IllegalArgumentException("SkinnedMesh: numVertices <= 0");
      } else if (firstVertex >= 0 && firstVertex + numVertices <= 65535) {
         Transform transform = new Transform();
         boolean valid = this.getTransformTo(bone, transform);
         if (!valid) {
            throw new ArithmeticException();
         } else {
            SkinnedMesh.BoneNode node = new SkinnedMesh.BoneNode();
            node.m_transformNode = bone;
            node.m_firstVertex = new Long((long)firstVertex);
            node.m_vertexCount = new Long((long)numVertices);
            node.m_weight = new Long((long)weight);
            node.m_atRestTransformation = transform;
            this.m_bones.add(node);
         }
      } else {
         throw new IndexOutOfBoundsException("SkinnedMesh: (firstVertex < 0) or range of vertices not in [0, 65536]");
      }
   }

   public void getBoneTransform(Node bone, Transform transform) {
      if (bone != null && transform != null) {
         if (!this.isDescendant(bone)) {
            throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
         } else {
            for(int i = 0; i < this.m_bones.size(); ++i) {
               SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(i);
               if (node.m_transformNode.equals(bone)) {
                  transform.set(node.m_atRestTransformation);
                  break;
               }
            }

         }
      } else {
         throw new NullPointerException("SkinnedMesh: bone or transform is null");
      }
   }

   public int getBoneVertices(Node bone, int[] indices, float[] weights) {
      long numVertices = 0L;
      if (bone == null) {
         throw new NullPointerException("SkinnedMesh: bone is null");
      } else if (!this.isDescendant(bone)) {
         throw new IllegalArgumentException("SkinnedMesh: bone is not a descendant of the skeleton group");
      } else {
         int i = 0;

         while(true) {
            if (i < this.m_bones.size()) {
               SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(i);
               if (!node.m_transformNode.equals(bone)) {
                  ++i;
                  continue;
               }

               long firstVertex = node.m_firstVertex;
               long vertexCount = node.m_vertexCount;
               if (indices != null && weights != null && ((long)indices.length < vertexCount || (long)weights.length < vertexCount)) {
                  throw new IllegalArgumentException();
               }

               numVertices = vertexCount;
            }

            return (int)numVertices;
         }
      }
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      if (this.m_skeleton != null) {
         if (references != null) {
            references[numReferences] = this.m_skeleton;
         }

         ++numReferences;
      }

      return numReferences;
   }

   SkinnedMesh() {
   }

   public void setSkeleton(Group skeleton) {
      this.m_skeleton = skeleton;
   }

   public int getTransformReferenceCount() {
      int count = this.m_bones.size();
      return count;
   }

   public Node getTransformNode(int index) {
      SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(index);
      return node.m_transformNode;
   }

   public long getFirstVertex(int index) {
      SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(index);
      Long value = node.m_firstVertex;
      return (long)value.intValue();
   }

   public long getVertexCount(int index) {
      SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(index);
      Long value = node.m_vertexCount;
      return (long)value.intValue();
   }

   public int getWeight(int index) {
      SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(index);
      Long value = node.m_weight;
      return value.intValue();
   }

   public int getObjectType() {
      return 16;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      long index = is.readObjectIndex();
      M3GObject obj = this.getObjectAtIndex(table, index, 9);
      if (obj == null) {
         throw new IOException("SkinnedMesh:skeleton-index = " + index);
      } else {
         this.m_skeleton = (Group)obj;
         long transformReferenceCount = is.readUInt32();

         for(int i = 0; (long)i < transformReferenceCount; ++i) {
            SkinnedMesh.BoneNode node = new SkinnedMesh.BoneNode();
            index = is.readObjectIndex();
            obj = this.getObjectAtIndex(table, index, -1);
            if (obj == null || !(obj instanceof Node)) {
               throw new IOException("SkinnedMesh:transformNode-index = " + index);
            }

            node.m_transformNode = (Node)obj;
            node.m_firstVertex = new Long(is.readUInt32());
            node.m_vertexCount = new Long(is.readUInt32());
            node.m_weight = new Long(is.readInt32());
            Transform t = new Transform();
            this.getTransformTo((Node)obj, t);
            node.m_atRestTransformation = t;
            this.m_bones.add(node);
         }

      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      int index = table.indexOf(this.m_skeleton);
      if (index <= 0) {
         throw new IOException("SkinnedMesh:skeleton-index = " + index);
      } else {
         os.writeObjectIndex((long)index);
         int transformReferenceCount = this.m_bones.size();
         os.writeUInt32((long)transformReferenceCount);

         for(int i = 0; i < this.m_bones.size(); ++i) {
            index = table.indexOf(((SkinnedMesh.BoneNode)this.m_bones.get(i)).m_transformNode);
            if (index <= 0) {
               throw new IOException("SkinnedMesh:transformNode-index = " + index);
            }

            os.writeObjectIndex((long)index);
            os.writeUInt32((long)((SkinnedMesh.BoneNode)this.m_bones.get(i)).m_firstVertex.intValue());
            os.writeUInt32((long)((SkinnedMesh.BoneNode)this.m_bones.get(i)).m_vertexCount.intValue());
            os.writeInt32(((SkinnedMesh.BoneNode)this.m_bones.get(i)).m_weight.intValue());
         }

      }
   }

   protected void buildReferenceTable(ArrayList table) {
      this.m_skeleton.buildReferenceTable(table);

      for(int i = 0; i < this.m_bones.size(); ++i) {
         SkinnedMesh.BoneNode node = (SkinnedMesh.BoneNode)this.m_bones.get(i);
         node.m_transformNode.buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }

   private boolean isDescendant(Node bone) {
      if (bone.equals(this.m_skeleton)) {
         return true;
      } else {
         Node parent = bone.getParent();
         return parent == null ? false : this.isDescendant(parent);
      }
   }

   protected class BoneNode {
      public Node m_transformNode;
      public Long m_firstVertex;
      public Long m_vertexCount;
      public Long m_weight;
      public Transform m_atRestTransformation;
   }
}
