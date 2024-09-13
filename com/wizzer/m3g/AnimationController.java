package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnimationController extends Object3D {
   private float m_speed;
   private float m_weight;
   private int m_activeIntervalStart;
   private int m_activeIntervalEnd;
   private float m_sequenceTime;
   private int m_worldTime;

   public AnimationController() {
      this.m_activeIntervalStart = this.m_activeIntervalEnd = 0;
      this.m_weight = 1.0F;
      this.m_speed = 1.0F;
      this.m_sequenceTime = (float)(this.m_worldTime = 0);
   }

   public void setActiveInterval(int start, int end) {
      if (start > end) {
         throw new IllegalArgumentException("AnimationController: start > end");
      } else {
         this.m_activeIntervalStart = start;
         this.m_activeIntervalEnd = end;
      }
   }

   public int getActiveIntervalStart() {
      return this.m_activeIntervalStart;
   }

   public int getActiveIntervalEnd() {
      return this.m_activeIntervalEnd;
   }

   public void setSpeed(float speed, int worldTime) {
      this.m_speed = speed;
   }

   public float getSpeed() {
      return this.m_speed;
   }

   public void setPosition(float sequenceTime, int worldTime) {
      this.m_sequenceTime = sequenceTime;
      this.m_worldTime = worldTime;
   }

   public float getPosition(int worldTime) {
      Logger.global.logp(Level.WARNING, "com.wizzer.m3g.AnimationController", "getPosition(int worldTime)", "Not implemented");
      return 0.0F;
   }

   public void setWeight(float weight) {
      if (weight < 0.0F) {
         throw new IllegalArgumentException("weight < 0");
      } else {
         this.m_weight = weight;
      }
   }

   public float getWeight() {
      return this.m_weight;
   }

   public int getRefWorldTime() {
      return this.m_worldTime;
   }

   public int getReferences(Object3D[] references) throws IllegalArgumentException {
      int numReferences = super.getReferences(references);
      return numReferences;
   }

   public float getRefSequenceTime() {
      return this.m_sequenceTime;
   }

   public int getObjectType() {
      return 1;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      super.unmarshall(is, table);
      this.setSpeed(is.readFloat32(), 0);
      this.setWeight(is.readFloat32());
      this.setActiveInterval((int)is.readInt32(), (int)is.readInt32());
      this.setPosition(is.readFloat32(), (int)is.readInt32());
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      super.marshall(os, table);
      os.writeFloat32(this.m_speed);
      os.writeFloat32(this.m_weight);
      os.writeInt32(this.m_activeIntervalStart);
      os.writeInt32(this.m_activeIntervalEnd);
      os.writeFloat32(this.m_sequenceTime);
      os.writeInt32(this.m_worldTime);
   }
}
