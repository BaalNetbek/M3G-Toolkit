/*
 * Device.java
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

/**
 * This class encapsulates the device configuration for the MIDP Emulator.
 * 
 * @author Mark Millard
 */
public class Device
{
	/** The default screen width. */
	public static int DEFAULT_SCREEN_WIDTH = 240;
	/** The default screen height. */
	public static int DEFAULT_SCREEN_HEIGHT = 320;
	
	private int m_width;
	private int m_height;
	
	/**
	 * The default constructor.
	 * <p>
	 * The screen is set to a default size of 240x320.
	 * </p>
	 */
	public Device()
	{
		m_width = DEFAULT_SCREEN_WIDTH;
		m_height = DEFAULT_SCREEN_HEIGHT;
	}
	
	/**
	 * Get the width of the device screen.
	 * 
	 * @return The width of the screen, in pixels, is returned.
	 */
	public int getScreenWidth()
	{
		return m_width;
	}
	
	/**
	 * Get the height of the device screen.
	 * 
	 * @return The height of the screen, in pixels, is returned.
	 */
	public int getScreenHeight()
	{
		return m_height;
	}

}
