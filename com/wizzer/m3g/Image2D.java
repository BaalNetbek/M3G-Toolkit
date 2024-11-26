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
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.nio.*;

// Import JOGL classes.
import javax.media.opengl.*;
import com.sun.opengl.util.BufferUtil;

public class Image2D extends Object3D
{
	public static final byte ALPHA              = 96;
	public static final byte LUMINANCE 	        = 97;
	public static final byte LUMINANCE_ALPHA    = 98;
	public static final byte RGB 	            = 99;
	public static final byte RGBA 	            = 100;

	// The image data.
	private BufferedImage m_originalImage, m_nativeImage;
	// The pixel format.
	private int m_format;
	// Flag indicating whether the image can be modified.
	private boolean m_mutable;
	// The width of the image.
	private int m_width;
	// The height of the image.
	private int m_height;
	// The image data.
	private byte m_image[];
	// The palette image.
	private byte m_palette[];

    ////////// Methods part of M3G Specification //////////
	
	public Image2D(int format, BufferedImage image)
	{
		if (format < ALPHA || format > RGBA)
			throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
		
		m_format = format;
		setImage(image);
	}

	public Image2D(int format, int width, int height, byte image[])
	{
		if (width <= 0)
			throw new IllegalArgumentException("Image2D: width <= 0");
		if (height <= 0)
			throw new IllegalArgumentException("Image2D: height <= 0");
		if (format < ALPHA || format > RGBA)
			throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
		int bpp = 1;
		if (format == LUMINANCE_ALPHA) bpp = 2;
		else if (format==RGB) bpp = 3;
		else if (format==RGBA) bpp = 4;
		if (image.length < (width * height * bpp))
			throw new IllegalArgumentException("Image2D: image.length <  width*height*bpp");
		
		m_format = format;
		m_width = width;
		m_height = height;
		m_image = image;
		m_palette = new byte[0];
		m_mutable = false;

		m_originalImage = createNativeImage();
		m_nativeImage = createNativeImage();
	}

	public Image2D(int format, int width, int height, byte image[], byte palette[])
	{
		if (width <= 0)
			throw new IllegalArgumentException("Image2D: width <= 0");
		if (height <= 0)
			throw new IllegalArgumentException("Image2D: height <= 0");
		if (format < ALPHA || format > RGBA)
			throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
		if (image.length < (width * height))
			throw new IllegalArgumentException("Image2D: image.length <  width*height");
		int bpp = 1;
		if (format == LUMINANCE_ALPHA) bpp = 2;
		else if (format == RGB) bpp = 3;
		else if (format == RGBA) bpp = 4;
		if (palette.length < (256 * bpp) && (palette.length % bpp) != 0)
			throw new IllegalArgumentException("Image2D: (palette.length <  256*C) && ((palette.length % bpp) != 0), where C is the number of color components (for instance, 3 for RGB)");

		m_format = format;
		m_width = width;
		m_height = height;
		m_image = image;
		if (palette.length > 256)
		{
			//m_palette = new byte[256];
			//System.arraycopy(palette, 0, this.m_palette, 0, 256);
			m_palette = new byte[palette.length];
			System.arraycopy(palette, 0, this.m_palette, 0, palette.length);
		}
		else m_palette = palette;
		m_mutable = false;

		m_originalImage = createNativeImage();
		m_nativeImage = createNativeImage();
	}

	public Image2D(int format,int width,int height)
	{
		if (width <= 0)
			throw new IllegalArgumentException("Image2D: width <= 0");
		if (height <= 0)
			throw new IllegalArgumentException("Image2D: height <= 0");
		if (format < ALPHA || format > RGBA)
			throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
		m_format = format;
		m_width = width;
		m_height = height;
		m_mutable = true;

		m_originalImage = m_nativeImage =
			new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				m_originalImage.setRGB(x, y, 0xffffffff);
	}

	public void set(int x, int y, int width, int height, byte image[])
	{
		if (image == null)
			throw new NullPointerException("Image2D: image is null");
		if (! m_mutable)
			throw new IllegalStateException("Image2D: image is immutable");
		if ((x < 0) || (y < 0) || (width <= 0) || (height <= 0))
			throw new IllegalArgumentException("Image2D: x < 0 or y < 0 or width <= 0 or height <= 0");
		if (((x + width) > m_width) || ((y + height) > m_height))
			throw new IllegalArgumentException("");
		int bpp = 1;
		if (m_format == LUMINANCE_ALPHA) bpp = 2;
		else if (m_format == RGB) bpp = 3;
		else if (m_format == RGBA) bpp = 4;
		if (image.length < width * height * bpp)
			throw new IllegalArgumentException("");
		
		Logger.global.logp(Level.WARNING, "com.wizzer.m3g.Image2D", "set(int x,int y,int width,int height,byte image[])", "Not implemented");
	}

	public boolean isMutable()
	{
		return m_mutable;
	}

	public int getFormat()
	{
		return m_format;
	}

	public int getWidth()
	{
		return m_width;
	}

	public int getHeight()
	{
		return m_height;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
    	return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	Image2D() {}

	public void setFormat(int format)
	{
		if (format < ALPHA || format > RGBA)
			throw new IllegalArgumentException("Image2D: format is not one of the symbolic constants");
		m_format = format;
		setImage(m_originalImage);
	}

	public void setImage(BufferedImage image)
	{
		if (isMutable())
			throw new IllegalStateException("Image2D: this image is mutable");
		int bpp = 1;
		if (m_format == LUMINANCE_ALPHA) bpp = 2;
		else if (m_format == RGB) bpp = 3;
		else if (m_format == RGBA) bpp = 4;
		m_width = image.getWidth();
		m_height = image.getHeight();
		ColorModel cm = image.getColorModel();

		if (cm instanceof IndexColorModel)
		{
			IndexColorModel icm = (IndexColorModel)cm;
			m_palette = new byte[icm.getMapSize()*bpp];
			for (int i = 0, ip = 0; i < icm.getMapSize(); i++)
			{
				if (m_format == ALPHA || m_format == LUMINANCE || m_format == LUMINANCE_ALPHA)
				{
					byte value = (byte)((icm.getRed(i) + icm.getGreen(i) + icm.getBlue(i)) / 3);
					m_palette[ip++] = value;
					if (m_format == LUMINANCE_ALPHA) m_palette[ip++]=value;
				}
				else
				{
					m_palette[ip++] = (byte)icm.getRed(i);
					m_palette[ip++] = (byte)icm.getGreen(i);
					m_palette[ip++] = (byte)icm.getBlue(i);
					if (m_format == RGBA)
						m_palette[ip++] = (byte)icm.getAlpha(i);
				}
			}

			Raster raster = image.getData();
			m_image = new byte[m_width*m_height];
			for (int y = 0; y < m_height; y++)
				for (int x = 0; x < m_width; x++)
					m_image[x + (y * m_width)] = (byte)raster.getSample(x, y, 0);
		}
		else
		{
			m_palette = new byte[0];
			m_image = new byte[m_width * m_height * bpp];
			int i = 0;
			for (int y = 0; y < m_height; y++)
			{
				for (int x = 0; x < m_width; x++)
				{
					int rgb = image.getRGB(x,y);
					int a = (rgb >> 24)&0xff;
					int r = (rgb >> 16)&0xff;
					int g = (rgb >> 8)&0xff;
					int b = rgb & 0xff;
					//
					if (m_format == ALPHA || m_format == LUMINANCE || m_format == LUMINANCE_ALPHA)
					{
						byte value = (byte)((r + g + b) / 3);
						m_image[i++] = value;
						if (m_format == LUMINANCE_ALPHA) m_image[i++] = value;
					}
					else
					{
						m_image[i++] = (byte)r;
						m_image[i++] = (byte)g;
						m_image[i++] = (byte)b;
						if (m_format==RGBA)
							m_image[i++]=(byte)a;
					}
				}
			}
		}

		m_originalImage = image;
		m_nativeImage = createNativeImage();
	}

	public BufferedImage getImage()
	{
		return m_nativeImage;
	}

	private BufferedImage createNativeImage()
	{
		BufferedImage nativeImage = null;
		// Bytes per pixel.
		int bpp = 1;
		if (m_format == LUMINANCE_ALPHA) bpp = 2;
		else if (m_format == RGB) bpp = 3;
		else if (m_format == RGBA) bpp = 4;

		if (m_palette.length > 0)
		{
			byte r[] = new byte[m_palette.length / bpp];
			byte g[] = new byte[m_palette.length / bpp];
			byte b[] = new byte[m_palette.length / bpp];
			byte a[] = new byte[m_palette.length / bpp];
			Arrays.fill(a, (byte)255);
			for (int i = 0, ip = 0; i < r.length; i++)
			{
				if (m_format == ALPHA || m_format == LUMINANCE || m_format == LUMINANCE_ALPHA)
				{
					r[i] = g[i] = b[i] = m_palette[ip++];
					if (m_format == ALPHA || m_format == LUMINANCE_ALPHA) a[i] = r[i];
					if (m_format == LUMINANCE_ALPHA) ip++;
				}
				else
				{
					r[i] = m_palette[ip++];
					g[i] = m_palette[ip++];
					b[i] = m_palette[ip++];
					if (m_format == RGBA)
						a[i] = m_palette[ip++];
				}
			}
			IndexColorModel icm = new IndexColorModel(8, r.length, r, g, b, a);
			nativeImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_INDEXED,icm);
			for (int y = 0; y < m_height; y++)
			{
				for (int x = 0; x < m_width; x++)
				{
					int index = m_image[x + (y * m_width)] & 0xff;
					nativeImage.setRGB(x, y, ((a[index] & 0xff) << 24) | ((r[index] & 0xff) << 16) | ((g[index] & 0xff) << 8) | (b[index] & 0xff));
				}
			}
		}
		else
		{
			nativeImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			int i = 0;
			for (int y = 0; y < m_height; y++)
			{
				for (int x = 0; x < m_width; x++)
				{
					int a = 255,r = 255,g = 255,b = 255;

					if (m_format == ALPHA || m_format == LUMINANCE || m_format == LUMINANCE_ALPHA)
					{
						r = g = b = (m_image[i++] & 0xff);
						if (m_format == ALPHA || m_format == LUMINANCE_ALPHA) a = r;
						if (m_format == LUMINANCE_ALPHA) i++;
					}
					else
					{
						r = (m_image[i++] & 0xff);
						g = (m_image[i++] & 0xff);
						b = (m_image[i++] & 0xff);
						if (m_format == RGBA) a = (m_image[i++] & 0xff);
					}

					nativeImage.setRGB(x, y, (a<<24)|(r<<16)|(g<<8)|b);
				}
			}
		}

		return nativeImage;
	}

	public int getObjectType()
	{
		return IMAGE2D;
	}

	/**
	 * Read field data.
	 * 
	 * @param is The input stream to read from.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * reading the data.
	 */
	protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException
	{
		super.unmarshall(is,table);

		// Read format
		m_format = is.readByte();
		if (m_format < ALPHA || m_format > RGBA)
			throw new IOException("Image2D.format=" + m_format);
		// Read isMutable
		m_mutable = is.readBoolean();
		// Read width
		m_width = (int)is.readUInt32();
		// Read hdight
		m_height = (int)is.readUInt32();
		if (! m_mutable)
		{
			// Read palette
			m_palette = new byte[(int)is.readUInt32()];
			is.read(m_palette);
			// Read pixels
			m_image = new byte[(int)is.readUInt32()];
			is.read(m_image);
		}

		m_originalImage = createNativeImage();
		m_nativeImage = createNativeImage();
	}

	/**
	 * Write field data.
	 * 
	 * @param os The output stream to write to.
	 * @param table The cache of referenced objects.
	 * 
	 * @throws IOException This exception is thrown if an error occurs
	 * writing the data.
	 */
	protected void marshall(M3GOutputStream os, ArrayList table) throws IOException
	{
		super.marshall(os,table);

		// Write format
		os.writeByte(m_format);
		// Write isMutable
		os.writeBoolean(m_mutable);
		// Write width
		os.writeUInt32(m_width);
		// Write height
		os.writeUInt32(m_height);
		if (! m_mutable)
		{
			// Write palette
			os.writeUInt32(m_palette.length);
			os.write(m_palette);
			// Write pixels
			os.writeUInt32(m_image.length);
			os.write(m_image);
		}
	}
	
	ByteBuffer getPixels()
	{
        int bpp = getBytesPerPixel();
        ByteBuffer pixels = BufferUtil.newByteBuffer(m_width * m_height * bpp);
        if (m_palette.length > 0)
        {
        	// Using a palette, image contains indices into palette.
			byte r[] = new byte[m_palette.length / bpp];
			byte g[] = new byte[m_palette.length / bpp];
			byte b[] = new byte[m_palette.length / bpp];
			byte a[] = new byte[m_palette.length / bpp];
			Arrays.fill(a, (byte)255);
			for (int i = 0, ip = 0; i < r.length; i++)
			{
				if (m_format == ALPHA || m_format == LUMINANCE || m_format == LUMINANCE_ALPHA)
				{
					r[i] = g[i] = b[i] = m_palette[ip++];
					if (m_format == ALPHA || m_format == LUMINANCE_ALPHA) a[i] = r[i];
					if (m_format == LUMINANCE_ALPHA) ip++;
				}
				else
				{
					r[i] = m_palette[ip++];
					g[i] = m_palette[ip++];
					b[i] = m_palette[ip++];
					if (m_format == RGBA)
						a[i] = m_palette[ip++];
				}
			}

			for (int y = 0; y < m_height; y++)
			{
				for (int x = 0; x < m_width; x++)
				{
					int index = m_image[x + (y * m_width)] & 0xff;
					pixels.put((byte)(r[index] & 0xff));
					pixels.put((byte)(g[index] & 0xff));
					pixels.put((byte)(b[index] & 0xff));
					if (m_format == RGBA)
						pixels.put((byte)(a[index] & 0xff));
				}
			}

        } else
        {
        	// Not using a palette, image contains RGBA values.
		    pixels.put(m_image, 0, m_width * m_height * bpp);
        }
		pixels.flip();
		
		return pixels;
	}

	int getBytesPerPixel()
	{
		if (m_format == RGBA)
			return 4;
		else if (m_format == RGB)
			return 3;
		else if (m_format == LUMINANCE_ALPHA)
			return 2;
		else
			return 1;
	}
	
	int getGLFormat()
	{
		if (m_format == RGBA)
			return GL.GL_RGBA;
		else if (m_format == RGB)
			return GL.GL_RGB;
		else if (m_format == LUMINANCE_ALPHA)
			return GL.GL_LUMINANCE_ALPHA;
		else if (m_format == LUMINANCE)
			return GL.GL_LUMINANCE;
		else if (m_format == ALPHA)
			return GL.GL_ALPHA;
		throw new RuntimeException("Invalid format on image");
	}
}