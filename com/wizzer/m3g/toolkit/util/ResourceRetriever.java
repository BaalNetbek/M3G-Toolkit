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
package com.wizzer.m3g.toolkit.util;

// Import standard Java classes.
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Utility class that allows transparent reading of files from
 * the current working directory or from the classpath.
 * 
 * @author Mark Millard
 */
 public class ResourceRetriever
 {
	/**
	 * Get the specified resource.
	 * <p>
	 * First try to load the resource from the jar. If the resource is not
	 * found in the jar, then try to load it from the current working
	 * directory.
	 * </p>
	 * 
	 * @param filename The name of the file to retrieve.
	 * 
	 * @return A <code>URL</code> for the resource is returned. <b>null</b>
	 * will be returned if the resource can not be found.
	 * 
	 * @throws IOException This exception is thrown if an IO error
	 * occurs while attempting to retrieve the resource.
	 */
    public static URL getResource(final String filename) throws IOException
    {
        // Try to load resource from jar.
        URL url = ClassLoader.getSystemResource(filename);
        // If not found in jar, then load from disk.
        if (url == null)
        {
            return new URL("file", "localhost", filename);
        } else
        {
            return url;
        }
    }

    /**
     * Get the resource as an input stream.
     * 
	 * @param filename The name of the file to retrieve.
	 * 
	 * @return An <code>InputStream</code> for the resource is returned. <b>null</b>
	 * will be returned if the resource can not be found.
	 * 
	 * @throws IOException This exception is thrown if an IO error
	 * occurs while attempting to retrieve the resource.
     */
    public static InputStream getResourceAsStream(final String filename) throws IOException
    {
        // Try to load resource from jar.
        InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
        // If not found in jar, then load from disk.
        if (stream == null)
        {
            return new FileInputStream(filename);
        } else
        {
            return stream;
        }
    }
}