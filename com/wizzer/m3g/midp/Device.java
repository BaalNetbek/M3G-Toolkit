package com.wizzer.m3g.midp;

public class Device {
   public static int DEFAULT_SCREEN_WIDTH = 240;
   public static int DEFAULT_SCREEN_HEIGHT = 320;
   private int m_width;
   private int m_height;

   public Device() {
      this.m_width = DEFAULT_SCREEN_WIDTH;
      this.m_height = DEFAULT_SCREEN_HEIGHT;
   }

   public int getScreenWidth() {
      return this.m_width;
   }

   public int getScreenHeight() {
      return this.m_height;
   }
}
