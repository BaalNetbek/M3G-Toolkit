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
import java.io.*;
import java.nio.*;
import java.util.*;

// Import JOGL classes.
import javax.media.opengl.*;

// Import M3g Toolkit classes.
import com.wizzer.m3g.toolkit.util.Color;

public class Texture2D extends Transformable
{
	public static final int FILTER_BASE_LEVEL   = 208;
	public static final int FILTER_LINEAR 	    = 209;
	public static final int FILTER_NEAREST 	    = 210;
	public static final int FUNC_ADD 	        = 224;
	public static final int FUNC_BLEND 	        = 225;
	public static final int FUNC_DECAL 	        = 226;
	public static final int FUNC_MODULATE 	    = 227;
	public static final int FUNC_REPLACE 	    = 228;
	public static final int WRAP_CLAMP 	        = 240;
	public static final int WRAP_REPEAT 	    = 241;

	// The texture blend color.
	private int m_blendColor;
	// The texture blend mode.
	private int m_blending;
	// The wrapping mode for the S texture coordinate.
	private int m_wrappingS;
	// The wrapping mode for the T texture coordinate.
	private int m_wrappingT;
	// The texture level filter.
	private int m_levelFilter;
	// The texture image filter.
	private int m_imageFilter;
	// The associated image.
	private Image2D m_image;
	// Flag indicating whether texture has been set.
	private boolean m_textureSet = false;
	
	int[] m_id = {0};

    ////////// Methods part of M3G Specification //////////
	
	public Texture2D(Image2D image)
	{
		setImage(image);
		m_wrappingS = WRAP_REPEAT;
		m_wrappingT = WRAP_REPEAT;
		m_levelFilter = FILTER_BASE_LEVEL;
		m_imageFilter = FILTER_NEAREST;
		m_blending = FUNC_MODULATE;
		m_blendColor = 0x00000000;
	}

	public void setImage(Image2D image)
	{
		if (image == null)
			throw new NullPointerException("Texture2D: image is null");
		if (! isPower2(image.getWidth()) || ! isPower2(image.getHeight()))
			throw new IllegalArgumentException("Texture2D: the width or height of image is not a positive power of two (1, 2, 4, 8, 16, etc.)");
		
		m_image = image;
		
		setTexture();
		/*
        GL gl = Graphics3D.getInstance().getGL();
		
		if (gl != null)
		{
			gl.glGenTextures(1, m_id, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, m_id[0]);

			Graphics3D.getInstance().getGLU().gluBuild2DMipmaps(
				GL.GL_TEXTURE_2D, 
				image.getBytesPerPixel(),
				image.getWidth(), 
				image.getHeight(), 
				image.getGLFormat(), 
				GL.GL_UNSIGNED_BYTE,
				image.getPixels());
			
			//gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, image.getWidth(), image.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, image.getPixels());
		}
		*/
	}

	public Image2D getImage()
	{
		return m_image;
	}

	public void setFiltering(int levelFilter,int imageFilter)
	{
		if (levelFilter < FILTER_BASE_LEVEL || levelFilter>FILTER_NEAREST)
			throw new IllegalArgumentException("Texture2D: levelFilter is not one of FILTER_BASE_LEVEL, FILTER_NEAREST, FILTER_LINEAR");
		if (imageFilter < FILTER_LINEAR || imageFilter>FILTER_NEAREST)
			throw new IllegalArgumentException("Texture2D: imageFilter is not one of FILTER_NEAREST, FILTER_LINEAR");
		
		m_levelFilter = levelFilter;
		m_imageFilter = imageFilter;
	}

	public int getImageFilter()
	{
		return m_imageFilter;
	}

	public int getLevelFilter()
	{
		return m_levelFilter;
	}

	public void setWrapping(int wrapS,int wrapT)
	{
		if (wrapS < WRAP_CLAMP || wrapS > WRAP_REPEAT || wrapT < WRAP_CLAMP || wrapT > WRAP_REPEAT)
			throw new IllegalArgumentException("Texture2D: wrapS or wrapT is not one of WRAP_CLAMP, WRAP_REPEAT");
		
		m_wrappingS = wrapS;
		m_wrappingT = wrapT;
	}

	public int getWrappingS()
	{
		return m_wrappingS;
	}

	public int getWrappingT()
	{
		return m_wrappingT;
	}

	public void setBlending(int func)
	{
		if (func < FUNC_ADD || func > FUNC_REPLACE)
			throw new IllegalArgumentException("Texture2D: func is not one of FUNC_REPLACE, FUNC_MODULATE, FUNC_DECAL, FUNC_BLEND, FUNC_ADD");
		
		m_blending = func;
	}

	public int getBlending()
	{
		return m_blending;
	}

	public void setBlendColor(int RGB)
	{
		m_blendColor = RGB;
	}

	public int getBlendColor()
	{
		return m_blendColor;
	}

	private boolean isPower2(int n)
	{
		for (int i = 1; i < 1024; i <<= 1)
			if (n == i) return true;
		return false;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int numReferences = super.getReferences(references);
		
		if (m_image != null)
		{
			if (references != null)
				references[numReferences] = m_image;
			++numReferences;
		}

		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	/**
	 * The default constructor.
	 */
	Texture2D() { /* do nothing. */ }

	public int getObjectType()
	{
		return TEXTURE2D;
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

		long index = is.readObjectIndex();
		M3GObject obj = getObjectAtIndex(table,index, IMAGE2D);
		if (obj != null)
			m_image = (Image2D)obj;
		else
			throw new IOException("Texture2D:image-index = " + index);
		setBlendColor(is.readColorRGB());
		setBlending(is.readByte());
		setWrapping(is.readByte(),is.readByte());
		setFiltering(is.readByte(),is.readByte());
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

		int index = table.indexOf(m_image);
		if (index > 0)
			os.writeObjectIndex(index);
		else
			throw new IOException("Texture2D:image-index = " + index);
		os.writeColorRGB(m_blendColor);
		os.writeByte(m_blending);
		os.writeByte(m_wrappingS);
		os.writeByte(m_wrappingT);
		os.writeByte(m_levelFilter);
		os.writeByte(m_imageFilter);
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		m_image.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
	
	void setupGL(GL gl, float[] scaleBias)
	{
		// Create OpenGL texture if not yet done.
		if (! m_textureSet)
			setTexture();

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, m_id[0]);
		
		// Set filtering.
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, getGLFilter(m_imageFilter));	// Linear Filtering
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, getGLFilter(m_imageFilter));	// Linear Filtering

		// Set wrap mode.
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, getGLWrap(m_wrappingS));
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, getGLWrap(m_wrappingT));

		// Set blend mode.
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, getGLBlend());
		gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, Color.intToFloatArray(m_blendColor),0);

		// Set texture scale.
		Transform t = new Transform();
		getCompositeTransform(t);

		gl.glMatrixMode(GL.GL_TEXTURE);
		t.setGL(gl);
		gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
		gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}
	
	int getGLFilter(int filter)
	{
		switch (filter)
		{
			case FILTER_LINEAR:
				return GL.GL_LINEAR_MIPMAP_LINEAR ;
			case FILTER_NEAREST:
				return GL.GL_NEAREST_MIPMAP_LINEAR;
		    default:
		    	return GL.GL_NEAREST;
		}
	}
	
	int getGLWrap(int wrap)
	{
		switch (wrap)
		{
			case Texture2D.WRAP_CLAMP:
				return GL.GL_CLAMP;
			default:
				return GL.GL_REPEAT;
		}
	}
	
	int getGLBlend()
	{
		switch (m_blending)
		{
			case FUNC_ADD:
				return GL.GL_ADD;
			case FUNC_MODULATE:
				return GL.GL_MODULATE;
			case FUNC_BLEND:
				return GL.GL_BLEND;
			case FUNC_REPLACE:
				return GL.GL_REPLACE;
			default:
				return GL.GL_DECAL;
		}
	}
	
	void setTexture()
	{
        GL gl = Graphics3D.getInstance().getGL();
		
		if (gl != null)
		{
			gl.glGenTextures(1, m_id, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, m_id[0]);

			ByteBuffer pixels = m_image.getPixels();
			Graphics3D.getInstance().getGLU().gluBuild2DMipmaps(
				GL.GL_TEXTURE_2D, 
				m_image.getBytesPerPixel(),
				m_image.getWidth(), 
				m_image.getHeight(), 
				m_image.getGLFormat(), 
				GL.GL_UNSIGNED_BYTE,
				pixels);
			
			//gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, m_image.getWidth(), m_image.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, m_image.getPixels());
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, m_image.getBytesPerPixel(),
				m_image.getWidth(), m_image.getHeight(), 0, m_image.getGLFormat(),
				GL.GL_UNSIGNED_BYTE, pixels);
			
			m_textureSet = true;
		}
	}
}
