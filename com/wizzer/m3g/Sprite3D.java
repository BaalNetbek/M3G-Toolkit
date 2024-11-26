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
import javax.media.opengl.*;

// Import M3G Toolkit classes.
import com.wizzer.m3g.math.*;

public class Sprite3D extends Node
{
	// The associated image.
	private Image2D m_image;
	// The associated appearance.
	private Appearance m_appearance;
	// Flag indicating whether image is scaled.
	private boolean m_scaled;
	// The current cropping rectangle X offset.
	private int m_cropX;
	// The current cropping rectangle Y offset.
	private int m_cropY;
	// The current cropping rectangle width.
	private int m_cropWidth;
	// The current cropping rectangle height.
	private int m_cropHeight;

	// Used for JOGL rendering.
	private static Hashtable m_textureCache = new Hashtable();
	private Texture2D m_texture;
	
    ////////// Methods not part of M3G Specification //////////
	
	public Sprite3D(boolean scaled,Image2D image,Appearance appearance)
	{
		m_scaled = scaled;
		setImage(image);
		m_appearance = appearance;
		setCrop(0, 0, image.getWidth(), image.getHeight());
	}

	public boolean isScaled()
	{
		return m_scaled;
	}

	public void setAppearance(Appearance appearance)
	{
		m_appearance = appearance;
	}

	public Appearance getAppearance()
	{
		return m_appearance;
	}

	public void setImage(Image2D image)
	{
		if (image == null)
			throw new NullPointerException("Sprite3D: image is null");
		
		m_image = image;
		setCrop(0, 0, image.getWidth(), image.getHeight());
		
		m_texture = (Texture2D)m_textureCache.get(image); 
		
		if (m_texture == null)
		{
			m_texture = new Texture2D(image);
			m_texture.setFiltering(Texture2D.FILTER_LINEAR,
	                               Texture2D.FILTER_LINEAR);
			m_texture.setWrapping(Texture2D.WRAP_CLAMP,
	                              Texture2D.WRAP_CLAMP);
			m_texture.setBlending(Texture2D.FUNC_REPLACE);

			// Cache texture.
			m_textureCache.put(image, m_texture);
		}
	}

	public Image2D getImage()
	{
		return m_image;
	}

	public void setCrop(int cropX, int cropY, int width, int height)
	{
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
		
		if (m_appearance != null)
		{
			if (references != null)
				references[numReferences] = m_appearance;
			++numReferences;
		}
				
		return numReferences;
	}
	
    ////////// Methods not part of M3G Specification //////////

	/**
	 * The default constructor.
	 */
	Sprite3D() { /* Do nothing. */ }
	
	public void setScaled(boolean scaled)
	{
		m_scaled = scaled;
	}

	public int getObjectType()
	{
		return SPRITE3D;
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
		if (index > 0)
		{
			M3GObject obj = getObjectAtIndex(table,index, IMAGE2D);
			if (obj != null)
				m_image = (Image2D)obj;
			else
				throw new IOException("Sprite3D:image-index = " + index);
		}
		else throw new IOException("Sprite3D:image-index = " + index);
		index = is.readObjectIndex();
		if (index != 0)
		{
			M3GObject obj = getObjectAtIndex(table, index, APPEARANCE);
			if (obj != null)
				m_appearance = (Appearance)obj;
			else
				throw new IOException("Sprite3D:appearance-index = " + index);
		}
		m_scaled = is.readBoolean();
		setCrop((int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32(), (int)is.readInt32());
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
			throw new IOException("Sprite3D:image-index = " + index);
		if (m_appearance != null)
		{
			index = table.indexOf(m_appearance);
			if (index > 0)
				os.writeObjectIndex(index);
			else
				throw new IOException("Sprite3D:appearance-index = " + index);
		}
		else os.writeObjectIndex(0);
		os.writeBoolean(m_scaled);
		os.writeInt32(m_cropX);
		os.writeInt32(m_cropY);
		os.writeInt32(m_cropWidth);
		os.writeInt32(m_cropHeight);
	}

	/**
	 * Build the reference table cache.
	 * 
	 * @param table The reference table cache.
	 */
	protected void buildReferenceTable(ArrayList table)
	{
		m_image.buildReferenceTable(table);
		if (m_appearance != null)
			m_appearance.buildReferenceTable(table);

		super.buildReferenceTable(table);
	}
	
	void render(GL gl, Transform t)
	{
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		t.multGL(gl);
		
		// Get current modelview matrix.
		float[] m = new float[16];
		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, m, 0);
		
		// Get up and right vector, used to create a camera-facing quad.
		Vector3 up = new Vector3(m[1], m[5], m[9]);
		up.normalize();
		Vector3 right = new Vector3(m[0], m[4], m[8]);
		right.normalize();

		float size = 1;
		Vector3 rightPlusUp = new Vector3(right);
		rightPlusUp.add(up);
		rightPlusUp.multiply(size);
		Vector3 rightMinusUp = new Vector3(right);
		rightMinusUp.subtract(up);
		rightMinusUp.multiply(size);
		
		Vector3 topLeft = new Vector3(rightMinusUp);
		topLeft.multiply(-1);

		Vector3 topRight = new Vector3(rightPlusUp);
		
		Vector3 bottomLeft = new Vector3(rightPlusUp);
		bottomLeft.multiply(-1);

		Vector3 bottomRight = new Vector3(rightMinusUp);
		
		Graphics3D.getInstance().setAppearance(getAppearance());
		Graphics3D.getInstance().disableTextureUnits(); 
		gl.glActiveTexture(GL.GL_TEXTURE0);
		m_texture.setupGL(gl, new float[] {1,0,0,0});

		// Draw sprite
        gl.glBegin(GL.GL_QUADS);       
        
        gl.glTexCoord2f(0, 0);	
        gl.glVertex3f(topLeft.x, topLeft.y, topLeft.z);				// Top Left
        
        gl.glTexCoord2f(0, 1);	
        gl.glVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z);	// Bottom Left
        
        gl.glTexCoord2f(1, 1);	
        gl.glVertex3f(bottomRight.x, bottomRight.y, bottomRight.z);	// Bottom Right

        gl.glTexCoord2f(1, 0);	
        gl.glVertex3f(topRight.x, topRight.y, topRight.z);			// Top Right

        gl.glEnd();			

		gl.glPopMatrix();
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        // HACK: for some reason, the depth write flag of other object destroyed 
        // after rendering a sprite.
        // This ensures that it's defaulted back to true.
        // TODO: find error and fix it!
        gl.glDepthMask(true);
	}
}
