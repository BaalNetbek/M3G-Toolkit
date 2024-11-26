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

/**
 * This class emulates the J2ME <code>javax.microedition.lcdui.Command</code>
 * class.
 * 
 * @author Mark Millard
 */
public class Command
{
	public static final int SCREEN 	= 1;
	public static final int BACK	= 2;
	public static final int CANCEL 	= 3;
	public static final int OK 		= 4;
	public static final int HELP	= 5;
	public static final int STOP	= 6;
	public static final int EXIT	= 7;
	public static final int ITEM	= 8;
	
	private int type 			= 0; 
	private String label 		= "";
	private String longLabel 	= "";
	private int priority 		= 0;
	
	public Command(String label, int commandType, int priority)
	{
		this.label = label;
		this.type = commandType;
		this.priority = priority;
	}
	
	public Command(String shortLabel, String longLabel, int commandType, int priority)
	{
		this(shortLabel, commandType, priority);
		this.longLabel = longLabel;
	}
	
	public int getCommandType()
	{
		return this.type;
	}
	
	public int getPriority()
	{
		return this.priority;
	}
	
	public String getLabel()
	{
		return this.label;
	}
	
	public String getLongLabel()
	{
		return this.longLabel;
	}
}
