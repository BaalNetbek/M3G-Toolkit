package com.wizzer.m3g;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

public abstract class RenderEventListener implements GLEventListener {
   public void display(GLAutoDrawable glDrawable) {
      Graphics3D.getInstance().setGL(glDrawable.getGL());
      this.paint();
   }

   public void init(GLAutoDrawable glDrawable) {
      Graphics3D.getInstance().setGL(glDrawable.getGL());
      glDrawable.getGL().glShadeModel(7425);
      this.initialize();
   }

   public void reshape(GLAutoDrawable glDrawable, int i0, int i1, int i2, int i3) {
   }

   public void displayChanged(GLAutoDrawable glDrawable, boolean b, boolean b1) {
   }

   public abstract void paint();

   public abstract void initialize();
}
