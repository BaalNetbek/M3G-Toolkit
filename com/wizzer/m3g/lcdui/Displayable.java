/*
 * Displayable.java
 * Created on Aug 29, 2008
 */

// COPYRIGHT_BEGIN
//
// Copyright (C) 2000-2008  Wizzer Works (msm@wizzerworks.com)
// 
// This file is part of the M3G Viewer.
//
// The M3GToolkit is free software; you can redistribute it and/or modify it
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
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.ArrayList;

// Import M3G Toolkit classes.
import com.wizzer.m3g.midp.*;

// Import JOGL classes.
import javax.media.opengl.*; 

/**
 * This class emulates the J2ME <code>javax.microedition.lcdui.Displayable</code>
 * class.
 * 
 * @author Mark Millard
 */
public abstract class Displayable
{
	protected Display currentDisplay;
	protected String title = null;
	protected Hashtable keyToCommand = new Hashtable();
	protected CommandListener cmdListener;

	protected MIDPEmulator emulator = MIDPEmulator.getInstance();
	
	protected Displayable()
	{
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
		emulator.setTitle(title);
	}
	
	public void addCommand(Command command)
	{
		if (command == null)
			throw new NullPointerException("command must not be null");
		mapCommand(command);
	}
	
	public void removeCommand(Command command)
	{
		if (command != null)
			keyToCommand.values().remove(command);
	}
	
	public void setCommandListener(CommandListener cmdListener)
	{
		this.cmdListener = cmdListener;
	}
	
	public int getWidth()
	{
		return emulator.getDevice().getScreenWidth();
	}
	
	public int getHeight()
	{
		return emulator.getDevice().getScreenHeight();
	}
	
	void processCommand(Command command)
	{
		if (command != null)
			cmdListener.commandAction(command, this);
	}

	Command getCommand(int keyCode)
	{
		return (Command)keyToCommand.get(new Integer(keyCode));
	}
	
	void callRepaint(int x, int y, int width, int height)
	{
		if (currentDisplay != null)
			currentDisplay.repaint(x, y, width, height);
	}
	
	void callRepaint()
	{
		callRepaint(0, 
				0, 
				emulator.getDevice().getScreenWidth(), 
				emulator.getDevice().getScreenHeight());
	}
	
	void callPaint(Graphics g)
	{
	}
	
	void setCurrentDisplay(Display display)
	{
		currentDisplay = display;
	}
	
	abstract GLCanvas getGLCanvas();

	private void mapCommand(Command cmd)
	{
		switch (cmd.getCommandType())
		{
			case Command.EXIT:
				keyToCommand.put(new Integer(KeyEvent.VK_ESCAPE), cmd);
			break;
		}
	}
}
