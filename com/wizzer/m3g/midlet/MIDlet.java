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
package com.wizzer.m3g.midlet;

// Import M3G Toolkit classes.
import com.wizzer.m3g.midp.MIDPEmulator;

public abstract class MIDlet
{
	private int state = 0;
	private MIDPEmulator emulator = MIDPEmulator.getInstance();;
		
	protected MIDlet()
	{
	}
	
	public final int checkPermission(String permission)
	{
		return 1;
	}
	
	public String getAppProperty(String key)
	{
		return emulator.getProperty(key);
	}
	
	public void notifyDestroyed()
	{
		emulator.notifyDestroyed(this);
	}

	public void notifyPaused()
	{
		emulator.notifyPaused(this);
	}

	public void resumeRequest()
	{
		emulator.resumeRequest(this);
	}
	
	protected abstract void startApp() throws MIDletStateChangeException;
	protected abstract void pauseApp();
	protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;
	
}
