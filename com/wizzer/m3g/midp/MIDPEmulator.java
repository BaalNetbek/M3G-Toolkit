package com.wizzer.m3g.midp;

import com.wizzer.m3g.lcdui.Display;
import com.wizzer.m3g.lcdui.Graphics;
import com.wizzer.m3g.midlet.MIDlet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MIDPEmulator {
   private static MIDPEmulator m_instance = null;
   public static int MIDLET_STATE_DESTROYED = 0;
   public static int MIDLET_STATE_ACTIVE = 1;
   public static int MIDLET_STATE_PAUSED = 2;
   public static int DEVICE_USE_SCREEN = 1;
   public static int DEVICE_USE_KEYPAD = 2;
   private static int DEFAULT_BACKGROUND_COLOR = 6758;
   private ArrayList m_midlets;
   private ArrayList m_inputListeners;
   private Hashtable m_graphicsToCanvas;
   private GLCanvas m_activeCanvas;
   private Container m_parent;
   private JPanel m_framePanel;
   private JPanel m_screenPanel;
   private JPanel m_buttonPanel;
   private int m_buttonPanelHeight;
   private int m_deviceSettings;
   private Device m_device;
   private GL m_gl;

   private MIDPEmulator() {
      this(new Device());
   }

   private MIDPEmulator(Device device) {
      this.m_midlets = new ArrayList();
      this.m_inputListeners = new ArrayList();
      this.m_graphicsToCanvas = new Hashtable();
      this.m_activeCanvas = null;
      this.m_buttonPanelHeight = 100;
      this.m_deviceSettings = 0;
      this.m_device = null;
      this.m_gl = null;
      this.m_device = device;
   }

   public static MIDPEmulator getInstance() {
      if (m_instance == null) {
         m_instance = new MIDPEmulator();
      }

      return m_instance;
   }

   public void addMidlet(MIDlet midlet) {
      if (!this.m_midlets.contains(midlet)) {
         this.m_midlets.add(midlet);
         Reflection.callMethod(midlet, "startApp", (Object[])null);
      }

   }

   public void removeMidlet(MIDlet midlet) {
      if (this.m_midlets.contains(midlet)) {
         Class[] argTypes = new Class[]{Boolean.TYPE};
         Object[] args = new Object[]{true};
         Reflection.callMethod(midlet, "destroyApp", argTypes, args);
         this.m_midlets.remove(midlet);
      }

   }

   public String getProperty(String key) {
      return "";
   }

   public void notifyDestroyed(MIDlet midlet) {
      this.exit();
   }

   public void notifyPaused(MIDlet midlet) {
   }

   public void resumeRequest(MIDlet midlet) {
   }

   public void exit() {
      System.exit(0);
   }

   public void setTitle(String title) {
      if (this.m_parent instanceof JFrame) {
         ((JFrame)this.m_parent).setTitle(title);
      } else {
         Logger.global.logp(Level.INFO, "com.wizzer.m3g.midp", "setTitle(String title)", "Not implimented");
      }

   }

   public Device getDevice() {
      return this.m_device;
   }

   public void setCurrentCanvas(GLCanvas canvas) {
      this.m_activeCanvas = canvas;
      this.m_screenPanel.removeAll();
      this.m_screenPanel.add(this.m_activeCanvas);
      this.m_activeCanvas.requestFocus();
   }

   public void setGraphicsToCanvas(Graphics g, GLCanvas canvas) {
      this.m_graphicsToCanvas.put(g, canvas);
   }

   public GLCanvas getRenderTarget(Graphics g) {
      return (GLCanvas)this.m_graphicsToCanvas.get(g);
   }

   public void setGL(GL gl) {
      this.m_gl = gl;
   }

   public GL getGL() {
      return this.m_gl;
   }

   public void addInputListener(ActionListener listener) {
      this.m_inputListeners.add(listener);
   }

   public void refreshDisplays() {
      for(int i = 0; i < this.m_midlets.size(); ++i) {
         Display display = Display.getDisplay((MIDlet)this.m_midlets.get(i));
         display.repaint(0, 0, 0, 0);
      }

   }

   public void init(Container parent, int screenWidth, int screenHeight, int flags) {
      int frameWidth = this.m_device.getScreenWidth();
      int frameHeight;
      if ((flags & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD) {
         frameHeight = this.m_device.getScreenHeight() + this.m_buttonPanelHeight;
      } else {
         frameHeight = this.m_device.getScreenHeight();
      }

      this.m_parent = parent;
      if (this.m_parent instanceof JFrame) {
         frameHeight += 36;
         this.m_parent.setBounds((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
      }

      this.m_framePanel = new JPanel();
      this.m_framePanel.setBackground(new Color(16711680));
      this.m_framePanel.setBounds((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
      Dimension size = new Dimension();
      size.width = frameWidth;
      size.height = frameHeight;
      this.m_framePanel.setPreferredSize(size);
      this.m_framePanel.setLayout(new BorderLayout());
      this.m_parent.add(this.m_framePanel);
      this.m_screenPanel = new JPanel();
      size.width = this.m_device.getScreenWidth();
      size.height = this.m_device.getScreenHeight();
      this.m_screenPanel.setPreferredSize(size);
      this.m_screenPanel.setBackground(new Color(DEFAULT_BACKGROUND_COLOR));
      this.m_framePanel.add(this.m_screenPanel, "Center");
      if ((flags & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD) {
         this.m_buttonPanel = new JPanel();
         size.width = this.m_device.getScreenWidth();
         size.height = this.m_buttonPanelHeight;
         this.m_buttonPanel.setPreferredSize(size);
         this.m_screenPanel.setBackground(new Color(DEFAULT_BACKGROUND_COLOR));
         this.m_buttonPanel.setLayout(new GridLayout(4, 3));
         this.m_framePanel.add(this.m_buttonPanel, "South");
         this.addButtons();
      }

      this.m_deviceSettings = flags;
      this.m_parent.setVisible(true);
   }

   public void reset() {
      this.m_screenPanel = null;
      this.m_buttonPanel = null;
      this.m_framePanel = null;
      this.m_parent = null;
   }

   public void resize(int screenWidth, int screenHeight) {
      int frameWidth = this.m_device.getScreenWidth();
      int frameHeight;
      if ((this.m_deviceSettings & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD) {
         frameHeight = this.m_device.getScreenHeight() + this.m_buttonPanelHeight + 4;
      } else {
         frameHeight = this.m_device.getScreenHeight();
      }

      if (!(this.m_parent instanceof JFrame)) {
         this.m_framePanel.setBounds((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
         Dimension size = new Dimension();
         size.width = frameWidth;
         size.height = frameHeight;
         this.m_framePanel.setPreferredSize(size);
         if ((this.m_deviceSettings & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD) {
            this.m_buttonPanel.repaint(0, 0, this.m_device.getScreenWidth(), this.m_buttonPanelHeight);
         }
      }

   }

   private void addButtons() {
      for(int i = 1; i < 10; ++i) {
         this.createButton(Integer.toString(i));
      }

      this.m_buttonPanel.add(new Panel());
      this.createButton("0");
   }

   private void createButton(String label) {
      Button btn = new Button(label);
      this.m_buttonPanel.add(btn);
      btn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MIDPEmulator.this.buttonPressed(e);
            if (MIDPEmulator.this.m_activeCanvas != null) {
               MIDPEmulator.this.m_activeCanvas.requestFocus();
            } else {
               Logger.global.logp(Level.WARNING, "com.wizzer.m3g.midp.MIDPEmulator", "actionPerformed(ActionEvent e)", "No active canvas");
            }

         }
      });
   }

   private void buttonPressed(ActionEvent e) {
      Iterator it = this.m_inputListeners.iterator();

      while(it.hasNext()) {
         ((ActionListener)it.next()).actionPerformed(e);
      }

   }
}
