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

// Import standard Java classes.
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

// Import M3G Toolkit classes.
import com.wizzer.m3g.toolkit.png.PNGDecoder;

/**
 * The <code>ExternalReference</code> is a utility for reading/writing
 * an External Reference from/to a M3G file. It is not a runtime
 * construct called out by the M3G Specification.
 * <p>
 * This class is used to encapsulate the external reference URI.
 * </p>
 * 
 * @author Mark Millard
 */
public class ExternalReference extends M3GObject
{
	// The Uniform Resource Identifier
	private String m_uri;
	// The referenced M3GObject.
	private M3GObject m_reference;
	// The current working directory.
	private static File g_cwd = null;

	ExternalReference()
	{
	}

	public ExternalReference(String uri)
	{
		m_uri = uri;
	}
	
	public static File getCwd()
	{
		return g_cwd;
	}
	
	public static void setCwd(File dir) throws IOException
	{
		if ((! dir.isDirectory()) || (! dir.canRead()))
			throw new IOException("ExternalReference: invalid directory");
		else
			g_cwd = dir;
	}
	
	public static void setCwd(String dirname) throws IOException
	{
		File dir = new File(dirname);
		if ((! dir.isDirectory()) || (! dir.canRead()))
		{
			throw new IOException("ExternalReference: invalid directory");
		} else
			g_cwd = dir;
	}

	public String getURI()
	{
		return m_uri;
	}

	public void setURI(String uri)
	{
		m_uri = uri;
	}

	public int getObjectType()
	{
		return EXTERNAL_REFERENCE;
	}
	
	public M3GObject getReference()
	{
		return m_reference;
	}

	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		m_uri = is.readString();
		
		// If the uri is a PNG file, create an Image2D object.
		// else, if the URI is a M3G file, create a sub-graph,
		// else, throw an IO exception.
		try
		{
			URI uri = new URI(m_uri);
			if (uri.getScheme() == null)
			{
				// Not a complete URL, attempt to qualify location using cwd.
				if (getCwd() != null)
				{
					URI path = getCwd().toURI();
					uri = path.resolve(uri);
				}
			}
			m_reference = resolvePNG(uri.normalize());
			if (m_reference == null)
			{
				// Attempt to open reference as another M3G file.
				throw new IOException("ExternalReference: external M3G file not implemented");
			} else return;
		} catch (URISyntaxException ex)
		{
			throw new IOException(ex.getMessage());
		}
		
		//throw new IOException("Invalid external reference.");
	}

	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		os.writeString(m_uri);
	}

	private Image2D resolvePNG(URI uri)
	{
		Image2D image2D = null;

		if (uri.getScheme() != null)
		{
			// The URI has a scheme, so try to resolve it as a URL.
			try
			{
				URL url = new URL(uri.toString());
	
				InputStream is = url.openStream();
				BufferedImage image = PNGDecoder.decode(is);
				int imageType = Image2D.RGB;
				if (image.getType() == BufferedImage.TYPE_INT_RGB)
					imageType = Image2D.RGB;
				else if (image.getType() == BufferedImage.TYPE_INT_ARGB)
					// XXX - PNG file is an ARGB format, we need an RGBA format.
					imageType = Image2D.RGBA;
				image2D = new Image2D(imageType, image);
			} catch (IOException ex) {}
		} else
		{
			// Try to resolve the URI as a local file.
			String path = uri.getPath();
			File file = new File(path);
			if (file.getAbsoluteFile().exists())
			{
				try
				{
					FileInputStream is = new FileInputStream(file);
					BufferedImage image = PNGDecoder.decode(is);
					int imageType = Image2D.RGB;
					if (image.getType() == BufferedImage.TYPE_INT_RGB)
						imageType = Image2D.RGB;
					else if (image.getType() == BufferedImage.TYPE_INT_ARGB)
						// XXX - PNG file is an ARGB format, we need an RGBA format.
						imageType = Image2D.RGBA;
					image2D = new Image2D(imageType, image);
				} catch (FileNotFoundException ex) {
				} catch (IOException ex) {}
			}
		}
		
		return image2D;
	}
}
