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
import java.util.*;

// Import JOGL classes.
import javax.media.opengl.GL;

// Import M3G Toolkit classes.
import com.wizzer.m3g.toolkit.util.Color;

public class Background extends Object3D
{
	public static final byte BORDER = 32;
	public static final byte REPEAT = 33;

	// The current background color.
	private int m_color;
	// The current background image.
	private Image2D m_image;
	// The current background image repeat mode in the X dimension. 
	private int m_imageModeX;
	// The current background image repeat mode in the Y dimenstion.
	private int m_imageModeY;
	// The current cropping rectangle X offset.
	private int m_cropX;
	// The current cropping rectangle Y offset.
	private int m_cropY;
	// The current cropping rectangle width.
	private int m_cropWidth;
	// The current cropping rectangle height.
	private int m_cropHeight;
	// Flag indicating depth buffer clearing.
	private boolean m_depthClearEnabled;
	// Flag indicating color buffer clearing.
	private boolean m_colorClearEnabled;

	// Used for rendering to JOGL.
	private Texture2D m_backgroundTexture = null;
	
    ////////// Methods part of M3G Specification //////////

	public Background()
	{
		m_colorClearEnabled = true;
		m_depthClearEnabled = true;
		m_color = 0x00000000;
		m_image = null;
		m_imageModeX = m_imageModeY=BORDER;
		m_cropX = m_cropY = m_cropWidth = m_cropHeight = 0;
	}

	public void setColorClearEnable(boolean enable)
	{
		m_colorClearEnabled = enable;
	}

	public boolean isColorClearEnabled()
	{
		return m_colorClearEnabled;
	}

	public void setDepthClearEnable(boolean enable)
	{
		m_depthClearEnabled = enable;
	}

	public boolean isDepthClearEnabled()
	{
		return m_depthClearEnabled;
	}

	public void setColor(int ARGB)
	{
		m_color = ARGB;
	}

	public int getColor()
	{
		return m_color;
	}

	public void setImage(Image2D image)
	{
		if (image != null && image.getFormat() != Image2D.RGB && image.getFormat() != Image2D.RGBA)
			throw new IllegalArgumentException("Background: image is not in RGB or RGBA format");
		
		m_image = image;
		if (image != null)
		{
			setCrop(0, 0, image.getWidth(), image.getHeight());
			m_backgroundTexture = null;
		}
	}

	public Image2D getImage()
	{
		return m_image;
	}

	public void setImageMode(int modeX, int modeY)
	{
		if ((modeX != BORDER && modeX != REPEAT) || 
			(modeY != BORDER && modeY != REPEAT))
			throw new IllegalArgumentException("Background: modeX or modeY is not one of the enumerated values");

		m_imageModeX = modeX;
		m_imageModeY = modeY;
	}

	public int getImageModeX()
	{
		return m_imageModeX;
	}

	public int getImageModeY()
	{
		return m_imageModeY;
	}

	public void setCrop(int cropX, int cropY, int width, int height)
	{
		if (width < 0)
			throw new IllegalArgumentException("Background: width < 0");
		if (height < 0)
			throw new IllegalArgumentException("Background: height < 0");

		m_cropX = cropX;
		m_cropY = cropY;
		m_cropWidth = width;
		m_cropHeight = height;
	}

	public int getCropX()
	{
		return m_cropX;
	}

	public int getCropY()
	{
		return m_cropY;
	}

	public int getCropWidth()
	{
		return m_cropWidth;
	}

	public int getCropHeight()
	{
		return m_cropHeight;
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

	public int getObjectType()
	{
		return BACKGROUND;
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
		super.unmarshall(is, table);

		// Read backgroundColor
		setColor(is.readColorRGBA());
		// Read backgroundImage
		long index = is.readObjectIndex();
		if (index != 0)
		{
			M3GObject obj = getObjectAtIndex(table, index, IMAGE2D);
			if (obj != null)
				setImage((Image2D)obj);
			else
				throw new IOException("Background:image-index = " + index);
		}
		// Read backgroundImageModeX and backgroundImageModeY
		setImageMode(is.readByte(), is.readByte());
		// Read cropX, cropY, cropWidth and cropHeight
		setCrop((int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32());
		// Read depthClearEnabled
		setDepthClearEnable(is.readBoolean());
		// Read colorClearEnabled
		setColorClearEnable(is.readBoolean());
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

		// Write backgroundColor
		os.writeColorRGBA(m_color);
		// Write backgroundImage
		if (m_image != null)
		{
			int index = table.indexOf(m_image);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Background:image-index = " + index);
		}
		else os.writeObjectIndex(0);
		// Write backgroundImageModeX
		os.writeByte(m_imageModeX);
		// Write backgroundImageModeY
		os.writeByte(m_imageModeY);
		// Write cropX
		os.writeInt32(m_cropX);
		// Write cropY
		os.writeInt32(m_cropY);
		// Write cropWidth
		os.writeInt32(m_cropWidth);
		// Write cropHeight
		os.writeInt32(m_cropHeight);
		// Write depthClearEnabled
		os.writeBoolean(m_depthClearEnabled);
		// Write colorClearEnabled
		os.writeBoolean(m_colorClearEnabled);
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		if (m_image != null)
			m_image.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
	
	public void setupGL(GL gl)
	{
        int clearBits = 0;

		Color c = new Color(m_color);
        gl.glClearColor(c.m_r, c.m_g, c.m_b, c.m_a);
        
        if (m_colorClearEnabled)
        	clearBits |= GL.GL_COLOR_BUFFER_BIT;
        if (m_depthClearEnabled)
        	clearBits |= GL.GL_DEPTH_BUFFER_BIT;
	        
        if (clearBits != 0)
        	gl.glClear(clearBits);		

        if (m_image != null)
		{
			if (m_backgroundTexture == null)
			{
				m_backgroundTexture = new Texture2D(m_image);
				m_backgroundTexture.setFiltering(Texture2D.FILTER_LINEAR,
		                             Texture2D.FILTER_LINEAR);
				m_backgroundTexture.setWrapping(Texture2D.WRAP_CLAMP,
		                            Texture2D.WRAP_CLAMP);
				m_backgroundTexture.setBlending(Texture2D.FUNC_REPLACE);
			}
			
			gl.glMatrixMode (GL.GL_MODELVIEW);
			gl.glPushMatrix ();
			gl.glLoadIdentity ();
			gl.glMatrixMode (GL.GL_PROJECTION);
			gl.glPushMatrix ();
			gl.glLoadIdentity ();
			
			gl.glColorMask(true, true, true, true);
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_LIGHTING);
			gl.glDisable(GL.GL_CULL_FACE); 
			gl.glDisable(GL.GL_BLEND); 

			Graphics3D.getInstance().disableTextureUnits(); 

			gl.glActiveTexture(GL.GL_TEXTURE0);
			m_backgroundTexture.setupGL(gl, new float[] {1,0,0,0});
			
			// Calculate cropping.
			int w = Graphics3D.getInstance().getViewportWidth();
			int h = Graphics3D.getInstance().getViewportHeight();
			
			if (m_cropWidth <= 0)
				m_cropWidth = w;
			if (m_cropHeight <= 0)
				m_cropHeight = h;
			
			float u0 = (float)m_cropX / (float)w;
			float u1 = u0 + (float)m_cropWidth / (float)w;
			float v0 = (float)m_cropY / (float)h;
			float v1 = v0 + (float)m_cropHeight / (float)h;
			
	        gl.glBegin(GL.GL_QUADS);        // Draw A Quad
	        gl.glTexCoord2f(u0, u0);	
	        gl.glVertex3f(-1.0f, 1.0f, 0);	// Top Left
	        gl.glTexCoord2f(u1, v0);	
	        gl.glVertex3f(1.0f, 1.0f, 0);	// Top Right
	        gl.glTexCoord2f(u1, v1);	
	        gl.glVertex3f(1.0f, -1.0f, 0);	// Bottom Right
	        gl.glTexCoord2f(u0, v1);	
	        gl.glVertex3f(-1.0f, -1.0f, 0);	// Bottom Left
	        gl.glEnd();	

	        gl.glPopMatrix();
	        gl.glMatrixMode (GL.GL_MODELVIEW);
	        gl.glPopMatrix();
	        
	        gl.glDisable(GL.GL_TEXTURE_2D);
		}
	}
}
