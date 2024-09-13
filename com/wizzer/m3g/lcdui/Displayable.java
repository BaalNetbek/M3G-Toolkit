package com.wizzer.m3g.lcdui;

import com.wizzer.m3g.midp.MIDPEmulator;
import java.util.Hashtable;
import javax.media.opengl.GLCanvas;

public abstract class Displayable {
   protected Display currentDisplay;
   protected String title = null;
   protected Hashtable keyToCommand = new Hashtable();
   protected CommandListener cmdListener;
   protected MIDPEmulator emulator = MIDPEmulator.getInstance();

   protected Displayable() {
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
      this.emulator.setTitle(title);
   }

   public void addCommand(Command command) {
      if (command == null) {
         throw new NullPointerException("command must not be null");
      } else {
         this.mapCommand(command);
      }
   }

   public void removeCommand(Command command) {
      if (command != null) {
         this.keyToCommand.values().remove(command);
      }

   }

   public void setCommandListener(CommandListener cmdListener) {
      this.cmdListener = cmdListener;
   }

   public int getWidth() {
      return this.emulator.getDevice().getScreenWidth();
   }

   public int getHeight() {
      return this.emulator.getDevice().getScreenHeight();
   }

   void processCommand(Command command) {
      if (command != null) {
         this.cmdListener.commandAction(command, this);
      }

   }

   Command getCommand(int keyCode) {
      return (Command)this.keyToCommand.get(new Integer(keyCode));
   }

   void callRepaint(int x, int y, int width, int height) {
      if (this.currentDisplay != null) {
         this.currentDisplay.repaint(x, y, width, height);
      }

   }

   void callRepaint() {
      this.callRepaint(0, 0, this.emulator.getDevice().getScreenWidth(), this.emulator.getDevice().getScreenHeight());
   }

   void callPaint(Graphics g) {
   }

   void setCurrentDisplay(Display display) {
      this.currentDisplay = display;
   }

   abstract GLCanvas getGLCanvas();

   private void mapCommand(Command cmd) {
      switch(cmd.getCommandType()) {
      case 7:
         this.keyToCommand.put(new Integer(27), cmd);
      default:
      }
   }
}
