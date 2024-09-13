package com.wizzer.m3g.midlet;

import com.wizzer.m3g.midp.MIDPEmulator;

public abstract class MIDlet {
   private int state = 0;
   private MIDPEmulator emulator = MIDPEmulator.getInstance();

   protected MIDlet() {
   }

   public final int checkPermission(String permission) {
      return 1;
   }

   public String getAppProperty(String key) {
      return this.emulator.getProperty(key);
   }

   public void notifyDestroyed() {
      this.emulator.notifyDestroyed(this);
   }

   public void notifyPaused() {
      this.emulator.notifyPaused(this);
   }

   public void resumeRequest() {
      this.emulator.resumeRequest(this);
   }

   protected abstract void startApp() throws MIDletStateChangeException;

   protected abstract void pauseApp();

   protected abstract void destroyApp(boolean var1) throws MIDletStateChangeException;
}
