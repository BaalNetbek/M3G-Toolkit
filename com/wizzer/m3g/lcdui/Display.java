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
package com.wizzer.m3g.lcdui;

// Import standard Java classes.
import java.util.Hashtable;

// Import M3G Toolkit classes.
import com.wizzer.m3g.lcdui.Displayable;
import com.wizzer.m3g.lcdui.Graphics;
import com.wizzer.m3g.midlet.MIDlet;
import com.wizzer.m3g.midp.MIDPEmulator;

/**
 * This class emulates the J2ME <code>javax.microedition.lcdui.Display</code>
 * class.
 * 
 * @author Mark Millard
 */
public class Display
{
	private static Hashtable m_displays = new Hashtable();
	
    // Singleton Graphics object.
    private static final Graphics m_screenGraphics =  Graphics.getGraphics(null);
    // MIDlet for this display.
    private MIDlet m_midlet;
    // Current displayable instance.
    private Displayable m_current;
    // Singleton MIDP Emulator object.
	private MIDPEmulator m_emulator = MIDPEmulator.getInstance();
	
	private Display(MIDlet midlet)
	{
		this.m_midlet = midlet;
	}
	
	public static Display getDisplay(MIDlet midlet)
	{
		Display display = (Display)m_displays.get(midlet);
		if (display == null)
		{
			display = new Display(midlet);
			m_displays.put(midlet, display);
		}
		return display;
	}

	public Displayable getCurrent()
	{
		return m_current;
	}
	
	public void setCurrent(Displayable nextDisplayable)
	{
		this.m_current = nextDisplayable;
		this.m_current.setCurrentDisplay(this);
		m_emulator.setCurrentCanvas(this.m_current.getGLCanvas());
	}
	
	/**
	 * Repaint the current displayable.
	 * <p>
	 * Note that this is not part of the <code>javax.microedition.lcdui.Display</code>
	 * API.
	 * </p>
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void repaint(int x1, int y1, int x2, int y2) 
	{
		// Make sure not to attempt to paint a null display.
		if (m_current != null)
		{
			m_emulator.setGraphicsToCanvas(m_screenGraphics, m_current.getGLCanvas());
			m_current.callPaint(m_screenGraphics);
		}
	}
	
}
