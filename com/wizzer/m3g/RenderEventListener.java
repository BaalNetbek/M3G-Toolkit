// COPYRIGHT_BEGIN
//
// Copyright (C) 2000-2008  Wizzer Works (msm@wizzerworks.com)
// 
// This file is part of the M3G Toolkit.
//
// The M3G Toolkit is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
//
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// COPYRIGHT_END

// Declare package.
package com.wizzer.m3g;

// Import JOGL classes.
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

/**
 * An Event Listener for rendering the OpenGL canvas.
 * 
 * @author Mark Millard
 */
public abstract class RenderEventListener implements GLEventListener 
{
	public void display(GLAutoDrawable glDrawable) 
    {
		Graphics3D.getInstance().setGL(glDrawable.getGL());
		paint();
    }

    public void init(GLAutoDrawable glDrawable) 
    {
		Graphics3D.getInstance().setGL(glDrawable.getGL());
		glDrawable.getGL().glShadeModel(GL.GL_SMOOTH); // Enable Smooth Shading

		initialize();
    }

    public void reshape(GLAutoDrawable glDrawable, int i0, int i1, int i2, int i3) 
    {
    }

    public void displayChanged(GLAutoDrawable glDrawable, boolean b, boolean b1) 
    {
    }
    
    public abstract void paint();
    public abstract void initialize();
}
