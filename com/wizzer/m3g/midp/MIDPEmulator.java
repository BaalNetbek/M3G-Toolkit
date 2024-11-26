/*
 * MIDPEmulator.java
 * Created on Aug 29, 2008
 */

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
package com.wizzer.m3g.midp;

// Import standard Java classes.
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Button;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// Import Swing classes.
import javax.swing.JFrame;
import javax.swing.JPanel;

// Import M3G Viewer classes.
import com.wizzer.m3g.midlet.MIDlet;
import com.wizzer.m3g.lcdui.Graphics;
import com.wizzer.m3g.lcdui.Display;

// Import JOGL classes.
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;

/**
 * This class emulates a MIDP device.
 * <p>
 * It implements a simple Swing interface consisting of a device
 * screen and ten input key buttons.
 * </p>
 * 
 * @author Mark S. Millard
 */
public class MIDPEmulator
{
    // The singleton instance of the MIDP Emulator.
	private static MIDPEmulator m_instance = null;

	/** The Midlet is destroyed. */
	public static int MIDLET_STATE_DESTROYED = 0;
	/** The Midlet is active. */
	public static int MIDLET_STATE_ACTIVE = 1;
	/** The Midlet is paused. */
	public static int MIDLET_STATE_PAUSED = 2;
	/** Use device screen configuration. */
	public static int DEVICE_USE_SCREEN = 0x00000001;
	/** Use device keypad configuratoin. */
	public static int DEVICE_USE_KEYPAD = 0x00000002;
	
	// The default background color for the screen.
	private static int DEFAULT_BACKGROUND_COLOR = 0x001A66;
	
	// The collection of Midlets installed on the emulator.
	private ArrayList m_midlets = new ArrayList();
	// The collection of input listeners for key and command events.
	private ArrayList m_inputListeners = new ArrayList();
	// The collection of Graphics to GLCanvas mappings.
	private Hashtable m_graphicsToCanvas = new Hashtable();
	// The currently activated GLCanvas.
	private GLCanvas m_activeCanvas = null;
	
	// The Swing Container widget.
	private Container m_parent;
	// The frame panel.
	private JPanel m_framePanel;
	// The Screen widget.
	private JPanel m_screenPanel;
	// The collection of Button widgets.
	private JPanel m_buttonPanel;
	// The height of the button panel.
	private int m_buttonPanelHeight = 100;
	// The emulator device flags.
	private int m_deviceSettings = 0x00000000;

	// The emulated device.
	private Device m_device = null;
    
	// The OpenGL manager for JNI calls.
    private GL m_gl = null;
    
    // Hide the default constructor.
	private MIDPEmulator()
	{
		this(new Device());
	}

	// A constructor that sets the associated device.
	private MIDPEmulator(Device device)
	{
		m_device = device;
	}
	
	/**
	 * Get the Singleton instance of the MIDP Emulator.
	 * 
	 * @return An instance of <code>MIDPEmulator</code> is returned.
	 */
	public static MIDPEmulator getInstance()
	{
		if (m_instance == null)
			m_instance = new MIDPEmulator();
		return m_instance;
	}
	
	/**
	 * Add a midlet to the emulator.
	 * <p>
	 * The midlet is started by calling the <code>startApp()</code>
	 * method on the midlet.
	 * </p>
	 * 
	 * @param midlet The <code>MIDlet</code> to add.
	 */
	public void addMidlet(MIDlet midlet)
	{
		if (! m_midlets.contains(midlet))
		{
			m_midlets.add(midlet);
			Reflection.callMethod(midlet, "startApp", null);
		}
	}
	
	/**
	 * Remove the midlet from the emulator.
	 * <p>
	 * The midlet is stopped by calling the <code>destroyApp()</code>
	 * method on the midlet.
	 * </p>
	 * 
	 * @param midlet The <code>MIDlet</code> to remove.
	 */
	public void removeMidlet(MIDlet midlet)
	{
		if (m_midlets.contains(midlet))
		{
			Class[] argTypes = new Class[1];
			argTypes[0] = boolean.class;
			Object[] args = new Object[1];
			args[0] = true;
			Reflection.callMethod(midlet, "destroyApp", argTypes, args);
			m_midlets.remove(midlet);
		}
	}
	
	/**
	 * Get a Java property associated with the specified key.
	 * <p>
	 * Not implemented yet.
	 * </p>
	 * 
	 * @param key The name of the Java property to retrieve.
	 * 
	 * @return The Java property value is returned.
	 */
	public String getProperty(String key)
	{
		return "";
	}
	
	/**
	 * Used by midlets to tell the emulator they want to destroy.
	 * 
	 * @param midlet The <code>MIDlet</code> requesting notification.
	 */
	public void notifyDestroyed(MIDlet midlet)
	{
		exit();
	}

	/**
	 * Used by midlets to tell the emulator they want to enter paused state.
	 * 
	 * @param midlet The <code>MIDlet</code> requesting notification.
	 */
	public void notifyPaused(MIDlet midlet)
	{
	}
	
	/**
	 * Used by midlets to tell the emulator they want to enter active state.
	 * 
	 * @param midlet The <code>MIDlet</code> requesting notification.
	 */
	public void resumeRequest(MIDlet midlet)
	{
	}

	/**
	 * The MIDP Emulator exit hook.
	 */
	public void exit()
	{
		System.exit(0);
	}
	
	/**
	 * Set the title of the main MIDP Emulator component.
	 * 
	 * @param title The title of the emulator.
	 */
	public void setTitle(String title)
	{
		if (m_parent instanceof JFrame)
			((JFrame)m_parent).setTitle(title);
		else
		Logger.global.logp(Level.INFO, "com.wizzer.m3g.midp",
			"setTitle(String title)", "Not implimented");
	}
	
	/**
	 * Get the associated device context for the MIDP Emulator.
	 * 
	 * @return The associated <code>Device</code> is returned.
	 */
	public Device getDevice()
	{
		return m_device;
	}

	/**
	 * Set the active JOGL canvas to the specified <i>canvas</i>.
	 * 
	 * @param canvas The JOGL canvas to make current.
	 */
	public void setCurrentCanvas(GLCanvas canvas)
	{
		m_activeCanvas = canvas;
		m_screenPanel.removeAll();
		m_screenPanel.add(m_activeCanvas);
		m_activeCanvas.requestFocus();
	}

	/**
	 * Map the M3G Toolkit graphics context to a JOGL canvas.
	 * 
	 * @param g The M3G Toolkit graphics context.
	 * @param canvas The JOGL canvas.
	 */
	public void setGraphicsToCanvas(Graphics g, GLCanvas canvas)
	{
		m_graphicsToCanvas.put(g, canvas);
	}
	
	/**
	 * Get the JOGL canvas associated with the specified M3G Toolkit graphics context.
	 * 
	 * @param g The M3G Toolkit graphics context.
	 * 
	 * @return The associated JOGL canvas is returned.
	 */
	public GLCanvas getRenderTarget(Graphics g)
	{
		return (GLCanvas)m_graphicsToCanvas.get(g);
	}
	
	/**
	 * Set the JOGL manager.
	 * 
	 * @param gl The JOGL manager utility.
	 */
	public void setGL(GL gl)
	{
		this.m_gl = gl;
	}
	
	/**
	 * Get the JOGL manager.
	 * 
	 * @return The associated <code>GL</code> is returned.
	 */
	public GL getGL()
	{
		return this.m_gl;
	}

	/**
	 * Add a listener for user input.
	 * 
	 * @param listener The listener interface for receiving actions events.
	 */
	public void addInputListener(ActionListener listener)
	{
		m_inputListeners.add(listener);
	}
	
	/**
	 * Refresh all displays that are associated with each registered
	 * midlet.
	 */
	public void refreshDisplays()
	{
		for (int i = 0; i < m_midlets.size(); i++)
		{
			Display display = Display.getDisplay((MIDlet)m_midlets.get(i));
			display.repaint(0, 0, 0, 0);
		}
	}
	
	/**
	 * Initialize the emulator.
	 * 
	 * @param parent The parent widget.
	 * @param screenWidth The width of the container's screen.
	 * @param screenHeight The height of the conatiner's screen.
	 */
	public void init(Container parent, int screenWidth, int screenHeight, int flags)
	{
		int frameWidth, frameHeight;
		
		frameWidth = m_device.getScreenWidth();
		if ((flags & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD)
		    frameHeight = m_device.getScreenHeight() + m_buttonPanelHeight;
		else
			frameHeight = m_device.getScreenHeight();

		m_parent = parent;
		
		// Check whether this is a standalone configuration.
		if (m_parent instanceof JFrame)
		{
			frameHeight += 36;
			m_parent.setBounds((screenWidth - frameWidth) / 2,
					(screenHeight - frameHeight) / 2,
					frameWidth, frameHeight);
		}

        // Create a top-level panel to manage the screen and button panels.
		m_framePanel = new JPanel();
		m_framePanel.setBackground(new java.awt.Color(0xff0000));
		m_framePanel.setBounds((screenWidth - frameWidth) / 2,
			(screenHeight - frameHeight) / 2,
			frameWidth, frameHeight);
		Dimension size = new Dimension();
		size.width = frameWidth;
		size.height = frameHeight;
		m_framePanel.setPreferredSize(size);
		m_framePanel.setLayout(new BorderLayout());
        m_parent.add(m_framePanel);

        m_screenPanel = new JPanel();
        //m_screenPanel.setSize(m_device.getScreenWidth(), m_device.getScreenHeight());
        size.width = m_device.getScreenWidth();
        size.height = m_device.getScreenHeight();
        m_screenPanel.setPreferredSize(size);
        //m_screenPanel.setBounds((screenWidth - frameWidth) / 2,
    	//	(screenHeight - frameHeight) / 2,
    	//	m_device.getScreenWidth(), m_device.getScreenHeight());
        m_screenPanel.setBackground(new java.awt.Color(DEFAULT_BACKGROUND_COLOR));
        m_framePanel.add(m_screenPanel, BorderLayout.CENTER);

        if ((flags & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD)
        {
	        m_buttonPanel = new JPanel();
	        //m_buttonPanel.setSize(m_device.getScreenWidth(), m_buttonPanelHeight);
	        size.width = m_device.getScreenWidth();
	        size.height = m_buttonPanelHeight;
	        m_buttonPanel.setPreferredSize(size);
	        //m_buttonPanel.setBounds((screenWidth - frameWidth) / 2,
	    	//	(screenHeight - frameHeight) / 2,
	    	//	m_device.getScreenWidth(), m_device.getScreenHeight());
	        m_screenPanel.setBackground(new java.awt.Color(DEFAULT_BACKGROUND_COLOR));
	        m_buttonPanel.setLayout(new GridLayout(4,3));
	        m_framePanel.add(m_buttonPanel, BorderLayout.SOUTH);
	        addButtons();
        }
        
        // Remember the device settings.
        m_deviceSettings = flags;
        // Show the parent widget.
        m_parent.setVisible(true);
	}
	
	/**
	 * Reset the emulator.
	 */
	public void reset()
	{
		m_screenPanel = null;
		m_buttonPanel = null;
		m_framePanel = null;
		m_parent = null;
	}
	
	/**
	 * Resize the emulator.
	 * 
	 * @param screenWidth The new width of the container.
	 * @param screenHeight The new height of the container.
	 */
	public void resize(int screenWidth, int screenHeight)
	{
		int frameWidth, frameHeight;
		
		frameWidth = m_device.getScreenWidth();
		if ((m_deviceSettings & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD)
			frameHeight = m_device.getScreenHeight() + m_buttonPanelHeight + 4;
		else
			frameHeight = m_device.getScreenHeight();

		if (m_parent instanceof JFrame)
		{
			/*
			int width = screenWidth;
			int height = screenHeight;
			
			if (screenWidth <= frameWidth)
				width = frameWidth;
		    m_framePanel.setSize(width, height);
		    */
		} else
		{
		    m_framePanel.setBounds((screenWidth - frameWidth) / 2,
		        (screenHeight - frameHeight) / 2,
			    frameWidth, frameHeight);
		    Dimension size = new Dimension();
			size.width = frameWidth;
			size.height = frameHeight;
			m_framePanel.setPreferredSize(size);
			if ((m_deviceSettings & DEVICE_USE_KEYPAD) == DEVICE_USE_KEYPAD)
			    m_buttonPanel.repaint(0, 0, m_device.getScreenWidth(), m_buttonPanelHeight);
		}
	}

	// Add the buttons.
	private void addButtons()
	{
		for (int i = 1; i < 10; i++)
			createButton(Integer.toString(i));		
		
		m_buttonPanel.add(new Panel());
		createButton("0");
	}

	// Create a button with the specified label.
	private void createButton(String label)
	{
		Button btn = new Button(label);
		m_buttonPanel.add(btn);
		btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				buttonPressed(e);
				if (m_activeCanvas != null)
				    m_activeCanvas.requestFocus();
				else
					Logger.global.logp(Level.WARNING, "com.wizzer.m3g.midp.MIDPEmulator",
						"actionPerformed(ActionEvent e)", "No active canvas");
					
			}
		});
	}
	
	// Handle a button pressed event.
	private void buttonPressed(ActionEvent e)
	{
		Iterator it = m_inputListeners.iterator();
		while (it.hasNext())
			((ActionListener)it.next()).actionPerformed(e);
		//System.out.println(((Button)e.getSource()).getLabel());
	}
}
