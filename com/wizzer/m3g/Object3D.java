package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Object3D extends M3GObject {
   private int m_userID;
   private ArrayList m_animationTracks = new ArrayList();
   private Hashtable m_parameters = new Hashtable();
   private Object m_userObject;

   protected Object3D() {
      this.m_userObject = this.m_parameters;
   }

   public final int animate(int time) {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "animate(int time)", "Not implemented");
      return 0;
   }

   public final Object3D duplicate() {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "duplicate()", "Not implemented");
      return null;
   }

   public Object3D find(int userID) {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Object3D", "find(int userID)", "Not implemented");
      return null;
   }

   public int getReferences(Object3D[] references) {
      return 0;
   }

   public void setUserID(int userID) {
      this.m_userID = userID;
   }

   public int getUserID() {
      return this.m_userID;
   }

   public void setUserObject(Object userObject) {
      this.m_userObject = userObject;
   }

   public Object getUserObject() {
      return this.m_userObject;
   }

   public void addAnimationTrack(AnimationTrack animationTrack) {
      if (animationTrack == null) {
         throw new NullPointerException();
      } else {
         this.m_animationTracks.add(animationTrack);
      }
   }

   public AnimationTrack getAnimationTrack(int index) {
      return (AnimationTrack)this.m_animationTracks.get(index);
   }

   public void removeAnimationTrack(AnimationTrack animationTrack) {
      this.m_animationTracks.remove(animationTrack);
   }

   public int getAnimationTrackCount() {
      return this.m_animationTracks.size();
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      this.setUserID((int)(is.readUInt32() & -1L));
      long count = is.readUInt32();

      int i;
      long id;
      for(i = 0; (long)i < count; ++i) {
         id = is.readObjectIndex();
         M3GObject obj = this.getObjectAtIndex(table, id, 2);
         if (obj == null) {
            throw new IOException("Object3D:track-index = " + id);
         }

         this.addAnimationTrack((AnimationTrack)obj);
      }

      count = is.readUInt32();

      for(i = 0; (long)i < count; ++i) {
         id = is.readUInt32();
         byte[] value = new byte[(int)is.readUInt32()];
         is.read(value);
         this.m_parameters.put(new Long(id), value);
      }

   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      os.writeUInt32((long)this.m_userID);
      os.writeUInt32((long)this.getAnimationTrackCount());

      for(int i = 0; i < this.getAnimationTrackCount(); ++i) {
         int index = table.indexOf(this.getAnimationTrack(i));
         if (index <= 0) {
            throw new IOException("Object3D:track-index = " + index);
         }

         os.writeObjectIndex((long)index);
      }

      os.writeUInt32((long)this.m_parameters.size());
      Enumeration e = this.m_parameters.keys();

      while(e.hasMoreElements()) {
         Long id = (Long)e.nextElement();
         byte[] value = (byte[])this.m_parameters.get(id);
         os.writeUInt32(id);
         os.write(value);
      }

   }

   protected void buildReferenceTable(ArrayList table) {
      for(int i = 0; i < this.m_animationTracks.size(); ++i) {
         ((AnimationTrack)this.m_animationTracks.get(i)).buildReferenceTable(table);
      }

      super.buildReferenceTable(table);
   }
}
