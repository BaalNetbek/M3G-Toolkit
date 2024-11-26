/*
 * Reflection.java
 * Created on Aug 27, 2008
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
package com.wizzer.m3g.midp;

// Import standard Java classes.
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A utility class for calling methods on a class using reflection.
 * 
 * @author Mark Millard
 */
public class Reflection
{
	public static Object callMethod(Object object, String methodName, Object[] args)
	{
		return callMethod(object, methodName, null, args);
	}

	public static Object callMethod(Object object, String methodName, Class[] methodArgTypes, Object[] args)
	{
		Object result = null;

		Method method = findMethod(object, methodName, methodArgTypes);
		if (method != null)
		{
			// Override access.
			boolean access = method.isAccessible();
			method.setAccessible(true);
		    try {
		    	 result = method.invoke(object, args);
		    } catch (Exception e) {
			       e.printStackTrace();
		    } finally {
		         method.setAccessible(access);
		    }
		}
		return result;
	}
	
	public static Method findMethod(Object object, String methodName, Class[] methodArgTypes)
	{
		Class c = object.getClass();
		Class objClass = null;

		try {
			objClass = Class.forName("java.lang.Object");
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		while (c != objClass)
		{
			try {
				Method method = c.getDeclaredMethod(methodName, methodArgTypes);
				return method;
			} catch (NoSuchMethodException e) {
				c = c.getSuperclass();
			}
		}
		return null;
	}
	
	public static Object getField(Object obj, String name)
	{
		Object result = null;
        Field f = findField(obj, name);

        if (f != null)
        {
			// overide accessablility
			boolean access = f.isAccessible();
	        f.setAccessible(true);
		    try {
		    	 result = f.get(obj);
		    } catch (Exception e) {
			       e.printStackTrace();
		    } finally {
		         f.setAccessible(access);
		    }
		}
        return result;
	}
	
	public static Field findField(Object obj, String field)
    {
		Class c = obj.getClass();
		Class objClass = null;

		try {
			objClass = Class.forName("java.lang.Object");
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		while (c != objClass)
		{
			try {
				Field f = c.getDeclaredField(field);
				return f;
			} catch (NoSuchFieldException e) {
				c = c.getSuperclass();
			}
		}
		return null;
    }
}
