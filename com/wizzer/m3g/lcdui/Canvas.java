package com.wizzer.m3g.lcdui;

import com.wizzer.m3g.midp.MIDPEmulator;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;

public abstract class Canvas extends Displayable {
   public static final int UP = 1;
   public static final int DOWN = 6;
   public static final int LEFT = 2;
   public static final int RIGHT = 5;
   public static final int FIRE = 8;
   public static final int GAME_A = 9;
   public static final int GAME_B = 10;
   public static final int GAME_C = 11;
   public static final int GAME_D = 12;
   public static final int KEY_NUM0 = 48;
   public static final int KEY_NUM1 = 49;
   public static final int KEY_NUM2 = 50;
   public static final int KEY_NUM3 = 51;
   public static final int KEY_NUM4 = 52;
   public static final int KEY_NUM5 = 53;
   public static final int KEY_NUM6 = 54;
   public static final int KEY_NUM7 = 55;
   public static final int KEY_NUM8 = 56;
   public static final int KEY_NUM9 = 57;
   public static final int KEY_STAR = 42;
   public static final int KEY_POUND = 35;
   private GLCanvas glCanvas = new GLCanvas(new GLCapabilities());
   private MIDPEmulator emulator = MIDPEmulator.getInstance();

   public Canvas() {
      this.glCanvas.setSize(this.emulator.getDevice().getScreenWidth(), this.emulator.getDevice().getScreenHeight());
      this.glCanvas.setIgnoreRepaint(true);
      this.glCanvas.addKeyListener(new Canvas.CanvasKeyAdapter((Canvas.CanvasKeyAdapter)null));
      this.emulator.setGL(this.glCanvas.getGL());
      int contextStatus = this.glCanvas.getContext().makeCurrent();
      this.emulator.addInputListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Canvas.this.emulatorInputEvent(e);
         }
      });
   }

   public int getKeyCode(int gameAction) {
      switch(gameAction) {
      case 1:
         return 38;
      case 2:
         return 37;
      case 5:
         return 39;
      case 6:
         return 40;
      case 8:
         return 32;
      case 42:
         return 106;
      case 48:
         return 96;
      case 49:
         return 97;
      case 50:
         return 98;
      case 51:
         return 99;
      case 52:
         return 100;
      case 53:
         return 101;
      case 54:
         return 102;
      case 55:
         return 103;
      case 56:
         return 104;
      case 57:
         return 105;
      default:
         return 0;
      }
   }

   public String getKeyName(int keyCode) {
      return "";
   }

   public int getGameAction(int keyCode) {
      switch(keyCode) {
      case 32:
         return 8;
      case 37:
         return 2;
      case 38:
         return 1;
      case 39:
         return 5;
      case 40:
         return 6;
      case 96:
         return 48;
      case 97:
         return 49;
      case 98:
         return 50;
      case 99:
         return 51;
      case 100:
         return 52;
      case 101:
         return 53;
      case 102:
         return 54;
      case 103:
         return 55;
      case 104:
         return 56;
      case 105:
         return 57;
      case 106:
         return 42;
      default:
         return 0;
      }
   }

   public final void repaint() {
      this.callRepaint();
   }

   public final void repaint(int x, int y, int width, int height) {
      this.callRepaint();
   }

   protected void keyPressed(int keyCode) {
      Logger.global.logp(Level.INFO, "com.wizzer.m3g.lcdui.Canvas", "keyPressed(int keyCode)", "Default key action does nothing");
   }

   protected void keyRepeated(int keyCode) {
      Logger.global.logp(Level.INFO, "com.wizzer.m3g.lcdui.Canvas", "keyRepeated(int keyCode)", "Default key action does nothing");
   }

   protected void keyReleased(int keyCode) {
      Logger.global.logp(Level.INFO, "com.wizzer.m3g.lcdui.Canvas", "keyReleased(int keyCode)", "Default key action does nothing");
   }

   protected void pointerPressed(int x, int y) {
      Logger.global.logp(Level.INFO, "com.wizzer.m3g.lcdui.Canvas", "pointerPressed(int x, int y)", "Default pointer action does nothing");
   }

   protected void pointerReleased(int x, int y) {
      Logger.global.logp(Level.INFO, "com.wizzer.m3g.lcdui.Canvas", "pointerReleased(int x, int y)", "Default pointer action does nothing");
   }

   protected abstract void paint(Graphics var1);

   private void emulatorInputEvent(ActionEvent e) {
      int key = Integer.parseInt(((Button)e.getSource()).getLabel());
      this.keyPressed(key + 96);
   }

   void callPaint(Graphics g) {
      this.paint(g);
   }

   GLCanvas getGLCanvas() {
      return this.glCanvas;
   }

   private class CanvasKeyAdapter extends KeyAdapter {
      private CanvasKeyAdapter() {
      }

      public void keyReleased(KeyEvent e) {
         Canvas.this.keyReleased(e.getKeyCode());
         Canvas.this.processCommand(Canvas.this.getCommand(e.getKeyCode()));
      }

      public void keyPressed(KeyEvent e) {
         Canvas.this.keyPressed(e.getKeyCode());
      }

      // $FF: synthetic method
      CanvasKeyAdapter(Canvas.CanvasKeyAdapter var2) {
         this();
      }
   }
}
