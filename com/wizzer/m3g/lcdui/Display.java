package com.wizzer.m3g.lcdui;

import com.wizzer.m3g.midlet.MIDlet;
import com.wizzer.m3g.midp.MIDPEmulator;
import java.util.Hashtable;

public class Display {
   private static Hashtable m_displays = new Hashtable();
   private static final Graphics m_screenGraphics = Graphics.getGraphics((Object)null);
   private MIDlet m_midlet;
   private Displayable m_current;
   private MIDPEmulator m_emulator = MIDPEmulator.getInstance();

   private Display(MIDlet midlet) {
      this.m_midlet = midlet;
   }

   public static Display getDisplay(MIDlet midlet) {
      Display display = (Display)m_displays.get(midlet);
      if (display == null) {
         display = new Display(midlet);
         m_displays.put(midlet, display);
      }

      return display;
   }

   public Displayable getCurrent() {
      return this.m_current;
   }

   public void setCurrent(Displayable nextDisplayable) {
      this.m_current = nextDisplayable;
      this.m_current.setCurrentDisplay(this);
      this.m_emulator.setCurrentCanvas(this.m_current.getGLCanvas());
   }

   public void repaint(int x1, int y1, int x2, int y2) {
      if (this.m_current != null) {
         this.m_emulator.setGraphicsToCanvas(m_screenGraphics, this.m_current.getGLCanvas());
         this.m_current.callPaint(m_screenGraphics);
      }

   }
}
