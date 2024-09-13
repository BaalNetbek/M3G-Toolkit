package com.wizzer.m3g.toolkit.util;

import com.wizzer.m3g.Group;
import com.wizzer.m3g.Node;
import java.util.LinkedList;

public class Path {
   protected Node m_node;

   private Path() {
   }

   public Path(Node node) {
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.m_node = node;
      }
   }

   public Node[] getPathTo(Node target) {
      LinkedList stack = new LinkedList();
      boolean found = this.buildPathTo(this.m_node, target, stack, (Node)null);
      return found && !stack.isEmpty() ? (Node[])stack.toArray() : null;
   }

   private boolean buildPathTo(Node curNode, Node target, LinkedList stack, Node ignoreChild) {
      if (curNode.equals(target)) {
         stack.addFirst(curNode);
         return true;
      } else {
         if (curNode instanceof Group) {
            Group group = (Group)curNode;
            int numChildren = group.getChildCount();

            for(int i = 0; i < numChildren; ++i) {
               Node child = group.getChild(i);
               if (ignoreChild == null || !child.equals(ignoreChild)) {
                  boolean found = this.buildPathTo(child, target, stack, (Node)null);
                  if (found) {
                     stack.addFirst(curNode);
                     return true;
                  }
               }
            }
         }

         Node parent = curNode.getParent();
         if (parent != null) {
            boolean found = this.buildPathTo(parent, target, stack, curNode);
            if (found) {
               stack.addFirst(curNode);
               return true;
            }
         }

         return false;
      }
   }
}
