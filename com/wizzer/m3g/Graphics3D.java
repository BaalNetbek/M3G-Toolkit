/*
 * Graphics3D.java
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
package com.wizzer.m3g;

// Import standard Java classes.
import java.util.ArrayList;
import java.nio.*;

// Import JOGL classes.
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.*;

// Import M3g Toolkit classes.
import com.wizzer.m3g.lcdui.Graphics;
import com.wizzer.m3g.midp.MIDPEmulator;
import com.wizzer.m3g.midp.Reflection;

/**
 * A singleton 3D graphics context that can be bound to a
 * rendering target.
 * <p>
 * See the <i>Mobile 3D Graphics API Technical Specification</i>
 * Version 1.1 (JSR-184) for more detail.
 * </p>
 * 
 * @author Mark Millard
 */
public final class Graphics3D
{
	/** The maximum number of lights. */
	static final int MAX_LIGHT_COUNT = 8;
	
	/** Specify that antialiasing should be turned on. */
	public static final int ANTIALIAS = 0x00000000;
	/** Specify that dithering should be turned on. */
	public static final int DITHER = 0x00000000;
	/**
	 * Specify that the existing contents of the rendering
	 * target need not be preserved.
	 */
	public static final int OVERWRITE = 0x00000000;
	/** Specify that true color rendering should be turned on. */
	public static final int TRUE_COLOR = 0x00000000;

    // The singleton instance.
	private static Graphics3D m_instance = null;
	
	// The number of texture units.
	private int m_numTextureUnits = 8; // TODO: get from device caps.
	
	// The x location of the viewport
	private int m_viewportX = 0;
	// The y locatio of the viewport.
	private int m_viewportY = 0;
	// The viewport width.
	private int m_viewportWidth = 0;
	// The viewport height.
	private int m_viewportHeight = 0;
	
	// The GL context.
	private GL m_gl = null;
	// The GL Utility context.
	private GLU m_glu = null;
	
	// The object to render.
	private Object m_renderTarget = null;
	// The current GL canvas.
	private GLCanvas m_currentGLCanvas;
	// The current render event listener.
	private RenderEventListener m_currentRenderListener;
	// The depth buffer enabled flag.
	private boolean m_depthBufferEnabled = true;
	// The rendering hints.
	private int m_hints;
	
	// The camera.
	private Camera m_camera;
	// The camera's transform.
	private Transform m_cameraTransform;
	
	// The list of lights.
	private ArrayList m_lights = new ArrayList();
	
	// The default compositing mode.
	private CompositingMode m_defaultCompositingMode = new CompositingMode();
	// The default polygon mode.
	private PolygonMode m_defaultPolygonMode = new PolygonMode();
	
	// Hide the default constructor.
	private Graphics3D() {}
	
	/**
	 * Retrieves the singleton <code>Graphics3D</code> instance
	 * that is associated with this application.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
     * 
	 * @return The singleton instance is returned.
	 */
	public static Graphics3D getInstance()
	{
		if (m_instance == null)
			m_instance = new Graphics3D();
		return m_instance;
	}
	
	/**
	 * Binds the given <code>Graphics</code> or mutable
	 * <code>Image2D</code> as the rendering target of this
	 * <code>Graphics3D</code>.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
     * 
	 * @param target The <code>Image2D</code> or <code>Graphics</code>
	 * object to receive the rendered image.
	 * @param renderListener A rendering event listener.
	 * 
	 * @throws NullPointerException This exception is thrown if <i>target</i>
	 * is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> already has a rendering target.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is not a mutable <code>Image2D</code> object or a <code>Graphics</code>
	 * object is incompatible to the underlying Java profile.
	 * @throws IllegalArgumentException This exception is thrown if
	 * <code>(target.width > maxViewportWidth) || (target.height >
	 * maxViewportHieght)</code>.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is an <code>Image2D</code> with an internal format other than
	 * <b>RGB</b> or <b>RGBA</b>.
	 */
	public void bindTarget(Object target, RenderEventListener renderListener)
	    throws NullPointerException, IllegalStateException,
	           IllegalArgumentException
	{
		// Validate arguments.
		if (target == null)
			throw new NullPointerException("target is null");
		if (this.m_renderTarget != null)
			throw new IllegalStateException("rendering target already specified");
		if (target instanceof Image2D)
		{
			Image2D imageTarget = (Image2D)target;
			if (! imageTarget.isMutable())
				throw new IllegalArgumentException("target is not a mutable Image2D");
			if ((imageTarget.getGLFormat() != Image2D.RGB) ||
			    (imageTarget.getGLFormat() != Image2D.RGBA))
				throw new IllegalArgumentException("internal format must be RGB or RGBA");
		} else if (! (target instanceof Graphics))
			throw new IllegalArgumentException("target is not a Image2D or Graphics");
        // XXX - need to check if target.width > maxViewportWidth
		// or target.height > maxViewportHeight
		
		// Handle Graphics target
		if (target instanceof Graphics)
		{
			this.m_renderTarget = target;
			this.m_currentRenderListener = renderListener;
			GLCanvas canvas = MIDPEmulator.getInstance().getRenderTarget((Graphics)target);
			this.m_currentGLCanvas = canvas;

			canvas.addGLEventListener(renderListener);
			canvas.display();
		}
		
		// XXX - need to handle instance of Image2D
	}

	/**
	 * Binds the given <code>Graphics</code> or mutable
	 * <code>Image2D</code> as the rendering target of this
	 * <code>Graphics3D</code>.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
     * 
	 * @param target The <code>Image2D</code> or <code>Graphics</code>
	 * object to receive the rendered image.
	 * 
	 * @throws NullPointerException This exception is thrown if <i>target</i>
	 * is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> already has a rendering target.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is not a mutable <code>Image2D</code> object or a <code>Graphics</code>
	 * object is incompatible to the underlying Java profile.
	 * @throws IllegalArgumentException This exception is thrown if
	 * <code>(target.width > maxViewportWidth) || (target.height >
	 * maxViewportHieght)</code>.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is an <code>Image2D</code> with an internal format other than
	 * <b>RGB</b> or <b>RGBA</b>.
	 */
	public void bindTarget(Object target)
        throws NullPointerException, IllegalStateException,
        IllegalArgumentException
	{
		// Validate arguments.
		if (target == null)
			throw new NullPointerException("target is null");
		if (this.m_renderTarget != null)
			throw new IllegalStateException("rendering target already specified");
		if (target instanceof Image2D)
		{
			Image2D imageTarget = (Image2D)target;
			if (! imageTarget.isMutable())
				throw new IllegalArgumentException("target is not a mutable Image2D");
			if ((imageTarget.getGLFormat() != Image2D.RGB) ||
			    (imageTarget.getGLFormat() != Image2D.RGBA))
				throw new IllegalArgumentException("internal format must be RGB or RGBA");
		} else if (! (target instanceof Graphics))
			throw new IllegalArgumentException("target is not a Image2D or Graphics");
        // XXX - need to check if target.width > maxViewportWidth
		// or target.height > maxViewportHeight
		
		// Handle Graphics target
		if (target instanceof Graphics)
		{
			this.m_renderTarget = target;
			GLCanvas canvas = MIDPEmulator.getInstance().getRenderTarget((Graphics)target);
			this.m_currentGLCanvas = canvas;
			
			// Set the current GL object.
			setGL(canvas.getGL());

			// Set the GLContext of the canvas to be the current context
            int contextStatus = canvas.getContext().makeCurrent();
            
            // Set default viewport?
            if (this.m_viewportHeight == 0 || this.m_viewportWidth == 0)
            	setViewport(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		
		// XXX - need to handle instance of Image2D
	}

	/**
	 * Binds the given <code>Graphics</code> or mutable
	 * <code>Image2D</code> as the rendering target of this
	 * <code>Graphics3D</code>.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
     * 
	 * @param target The <code>Image2D</code> or <code>Graphics</code>
	 * object to receive the rendered image.
	 * @param depthBuffer Set <b>true</b> to enable depth buffering.
	 * Otherwise, use <b>false</b> to diable.
	 * @param hints An integer bitmask specifying which rendering
	 * hints to enable, or zero to disable all hints.
	 *
	 * @throws NullPointerException This exception is thrown if <i>target</i>
	 * is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> already has a rendering target.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is not a mutable <code>Image2D</code> object or a <code>Graphics</code>
	 * object is incompatible to the underlying Java profile.
	 * @throws IllegalArgumentException This exception is thrown if
	 * <code>(target.width > maxViewportWidth) || (target.height >
	 * maxViewportHieght)</code>.
	 * @throws IllegalArgumentException This exception is thrown if <i>target</i>
	 * is an <code>Image2D</code> with an internal format other than
	 * <b>RGB</b> or <b>RGBA</b>.
	 */
	public void bindTarget(Object target, boolean depthBuffer, int hints)
        throws NullPointerException, IllegalStateException,
        IllegalArgumentException
	{
		// Validate arguments.
		int bitmask = ANTIALIAS | DITHER | OVERWRITE | TRUE_COLOR;
		if (hints != 0)
			if ((hints & ~bitmask) != 0)
			    throw new IllegalArgumentException("invalid hints");
		
		// The remaining argument validation will be handled by
		// this call.
		bindTarget(target, null);
		
		m_depthBufferEnabled = depthBuffer;
		// XXX - set the depth buffer.
		
		m_hints = hints;
		// XXX - set the hints in GL.
	}
	
	/**
	 * Get the current rendering target.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The current rendering target is returned. It should
	 * either be a <code>Image2D</code> or <code>Graphics</code>
	 * object.
	 */
	public Object getTarget()
	{
		return this.m_renderTarget;
	}
	
	/**
	 * Flushes the rendered 3D image to the currently bound target
	 * and then releases the target.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 */
	public void releaseTarget()
	{
		if (this.m_currentGLCanvas != null)
		{
			if (this.m_currentRenderListener != null)
			{
				this.m_currentGLCanvas.removeGLEventListener(this.m_currentRenderListener);
				this.m_currentRenderListener = null;
			}
			else
			{
				// Hack into the current GLCanvas with reflection and use it's
				// internal drawable to swap the buffers
			    GLDrawable d = (GLDrawable)Reflection.getField(this.m_currentGLCanvas, "drawable");
			    d.swapBuffers();
			}
		}
		this.m_currentGLCanvas = null;
		this.m_renderTarget = null;
	}
	
	/**
	 * Clears the viewport as specified in the given <code>Background</code>
	 * object.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
     * 
	 * @param background A <code>Background</code> object defining
	 * which buffers to clear and how. If <b>null</b>, the default
	 * settings are used.
	 * 
	 * @throws IllegalArgumentException This exception is thrown if
	 * the background image in <i>background</i> is not in the same
	 * format as the currently bound rendering target.
	 * @throws IllegalStateException This exception is thrown if
	 * this <code>Graphics3D</code> does not have a rendering
	 * target.
	 */
	public void clear(Background background)
	    throws IllegalArgumentException, IllegalStateException
	{
		// Validate arguments.
		if (m_renderTarget == null)
			throw new IllegalStateException("no rendering target");
		if (background != null)
		{
			int imageFormat;
			if (m_renderTarget instanceof Image2D)
			{
				imageFormat = ((Image2D)m_renderTarget).getFormat();
				Image2D backgroundImage = background.getImage();
				if (imageFormat != backgroundImage.getFormat())
					throw new IllegalArgumentException("invalid background image format");
			}
	        // XXX - need to test imageFormat against Graphics case.
		}
		
		if (background != null)
			background.setupGL(m_gl);
		else
		{
			// Clear to black.
	        m_gl.glClearColor(0,0,0,0);
	        m_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}
	}
	
	/**
	 * Binds a <code>Light</code> to use in subsequent immediate mode
	 * rendering.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param light The <code>Light</code> to add at the end of the array
	 * of current lights.
	 * @param transform The transformation from the local coordinate system
	 * of <i>light</i> to world space. If <b>null</b>, use the identity
	 * matrix.
	 * 
	 * @return The index at which the light was inserted in the array.
	 */
	public int addLight(Light light, Transform transform)
	    throws NullPointerException
	{
		// Validate arguments.
		if (light == null)
			throw new NullPointerException("light must not be null");

		m_lights.add(light);
		int index = m_lights.size() - 1;

		// Limit the number of lights.
		if (index < MAX_LIGHT_COUNT)
		{
			m_gl.glPushMatrix();
			transform.multGL(m_gl); // TODO: should this really be setGL? I was thinking multGL...
			
			light.setupGL(m_gl);
			m_gl.glPopMatrix();
		}
		
		return index;
	}
	
	/**
	 * Replaces or modifies a <code>Light</code> currently bound
	 * for immediate mode rendering.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 *  
	 * @param index Index of the light to set.
	 * @param light The <code>Light</code> to set. If <b>null</b>,
	 * remove the light at the specified <i>index</i>.
	 * @param transform The transformation from the local coordinate
	 * system of <i>light</i> to world space. If <b>null</b>, then
	 * use the identity matrix.
	 */
	public void setLight(int index, Light light, Transform transform)
	    throws IndexOutOfBoundsException
	{
		if ((index < 0) || (index >= getLightCount()))
			throw new IndexOutOfBoundsException("index out of range");
		
		m_lights.set(index, light);
		// TODO: set transform and update light.
	}
	
	/**
	 * Clears the array of current lights.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 */
	public void resetLights()
	{
		m_lights.clear();
		
		for (int i = 0; i < MAX_LIGHT_COUNT; ++i)
			m_gl.glDisable(GL.GL_LIGHT0 + i);
	}
	
	/**
	 * Returns the size of the current light array.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The number of slots in the current light array.
	 */
	public int getLightCount()
	{
		return MAX_LIGHT_COUNT;
	}
	
	/**
	 * Returns a light in the current light array.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param index The index of the light to get.
	 * @param transform The transform to store the light transformation
	 * in. If <b>null</b>, then only get the <code>Light</code> object.
	 * 
	 * @return The light object at <i>index</i> is returned.
	 */
	public Light getLight(int index, Transform transform)
	    throws IndexOutOfBoundsException
	{
		if ((index < 0) || (index >= getLightCount()))
			throw new IndexOutOfBoundsException("index out of range");

		Light light;
		if (index < m_lights.size())
		    light = (Light)m_lights.get(index);
		else
			light = null;
		
		// XXX - process the transform here.
		
		return light;
	}
	
	/**
	 * Returns the rendering hints given for the current rendering
	 * target.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The current rendering hint bitmask is returned.
	 */
	public int getHints()
	{
		return m_hints;
	}
	
	/**
	 * Queries whether depth buffering is enabled for the current
	 * rendering target.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return <b>true</b> will be returned if depth buffering
	 * is returned. Otherwise <b>false</b> will be returned.
	 */
	public boolean isDepthBufferEnabled()
	{
		return m_depthBufferEnabled;
	}
	
	/**
	 * Specifies a rectangular viewport on the currently bound
	 * rendering target.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param x X coordinate of the viewport upper left corner, in pixels.
	 * @param y Y coordinate of the viewport upper left corner, in pixels.
	 * @param width The width of the viewport, in pixels.
	 * @param height The height of the viewport, in pixels.
	 * 
	 * @throws IllegalArgumentException This exception is thrown if
	 * <code>((width <= 0) || (height <= 0))</code>.
	 * @throws IllegalArgumentException This exception is thrown if
	 * <code>((width > maxViewportWidth) || (height > maxViewportHeight))</code>.
	 */
	public void setViewport(int x, int y, int width, int height)
	    throws IllegalArgumentException
	{
		// Validate arguments.
		if ((width < 0) || (height <= 0))
			throw new IllegalArgumentException("invalid width or height");
		// XXX - check for ((width > maxViewportWidth) || (height > maxViewportHeight))

		this.m_viewportX = x;
		this.m_viewportY = y;
		this.m_viewportWidth = width;
		this.m_viewportHeight = height;

		m_gl.glViewport(x, y, width, height);	
	}
	
	/**
	 * Returns the horizontal position of the viewport.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The X coordinate of the upper left corner, in pixels.
	 */
	public int getViewportX()
	{
		return this.m_viewportX;
	}

	/**
	 * Returns the vertical position of the viewport.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The Y coordinate of the upper left corner, in pixels.
	 */
	public int getViewportY()
	{
		return this.m_viewportY;
	}
	
	/**
	 * Returns the width of the viewport.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The width of the viewport, in pixels.
	 */
	public int getViewportWidth()
	{
		return this.m_viewportWidth;
	}
	
	/**
	 * Returns the height of the viewport.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return The height of the viewport, in pixels.
	 */
	public int getViewportHeight()
	{
		return this.m_viewportHeight;
	}
	
	/**
	 * Specifies the mapping of depth values from normalized device
	 * coordinates to window coordinates.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param near The distance to the near clipping plane, in
	 * window coordinates.
	 * @param far The distance to the far clipping plane, in
	 * window coordinates.
	 */
	public void setDepthRange(float near, float far)
	    throws IllegalArgumentException
	{
		if ((near < 0) || (near > 1))
			throw new IllegalArgumentException("near value out of range");
		if ((far < 0) || (far > 1))
			throw new IllegalArgumentException("far value out of range");

		// TODO
	}
	
	/**
	 * Returns the near distance of the depth range.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return Distance to the near clipping plane, in window
	 * coordinates.
	 */
	public float getDepthRangeNear()
	{
		// TODO
		return 0;
	}

	/**
	 * Returns the far distance of the depth range.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @return Distance to the far clipping plane, in window
	 * coordinates.
	 */
	public float getDepthRangeFar()
	{
		// TODO
		return 0;
	}

	/**
	 * Sets the <code>Camera</code> to use in subsequent immediate
	 * mode rendering.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param camera The <code>Camera</code> to bind for immediate
	 * mode rendering. If <b>null</b>, unbind the current camera.
	 * @param transform The transformation from the local coordinate
	 * system of <i>camera</i> to world space. If <b>null</b>, use
	 * the identity matrix.
	 * 
	 * @throws ArithmeticException This exception is thrown if
	 * <i>transform</i> is not invertible.
	 */
	public void setCamera(Camera camera, Transform transform)
	   throws ArithmeticException
	{
		// XXX - Validate that the transform can be inverted.
		
		this.m_camera = camera;
		this.m_cameraTransform = transform;
		
		Transform t = new Transform();
		
	    m_gl.glMatrixMode(GL.GL_PROJECTION);
	    camera.getProjection(t);
	    t.setGL(m_gl);
	    
	    m_gl.glMatrixMode(GL.GL_MODELVIEW);
		t.set(transform);
		t.invert();
		t.setGL(m_gl);
	}
	
	/**
	 * Returns the current camera.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param transform A <code>Transform</code> to store the current
	 * transformation in. If <b>null</b>, only get the camera.
	 * 
	 * @return The current camera is returned.
	 */
	public Camera getCamera(Transform transform)
	{
		if (transform != null)
			transform.set(this.m_cameraTransform);
		return m_camera;
	}
	
	/**
	 * Renders the given <code>Sprite3D</code>, <code>Mesh</code>, or
	 * <code>Group</code> node with the given transformation from
	 * local coordinates to world coordinates.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param node The <code>Sprite3D</code>, <code>Mesh</code>, or
	 * <code>Group</code> to render.
	 * @param transform The transformation from the local coordinate
	 * system of <i>node</i> to world space. If <b>null</b>, use
	 * the identity matrix.
	 * 
	 * @throws NullPointerException This exception is thrown if
	 * <i>node</i> is <b>null</b>.
	 * @throws IllegalArgumentException This exception is thrown if
	 * <i>node</i> is not a <code>Sprite3D</code>, <code>Mesh</code>, or
	 * <code>Group</code>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a rendering target.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a current camera.
	 * @throws IllegalStateException This exception is thrown if
	 * any  <code>Mesh</code> that is rendered violates the
	 * constraints in <code>Mesh</code>, <code>MorphingMesh</code>,
	 * <code>SkinnedMesh</code>, <code>VertexBuffer</code>,
	 * or <code>IndexBuffer</code>.
	 */
	public void render(Node node, Transform transform)
	    throws NullPointerException, IllegalArgumentException,
	           IllegalStateException
	{
		// Validate arguments.
		if (node == null)
			throw new NullPointerException("node == null");
		if (m_renderTarget == null)
			throw new IllegalStateException("no render target");
		if (m_camera == null)
			throw new IllegalStateException("no current camera");
		// XXX - check if any Mesh that is rendered violates the
		// constraints in Mesh, MorphingMesh, SkinnedMesh, VertexBuffer,
		// or IndexBuffer.
		
		if (node instanceof Mesh)
		{
			Mesh mesh = (Mesh)node;
			int subMeshes = mesh.getSubmeshCount();
			VertexBuffer vertices = mesh.getVertexBuffer();
			for (int i = 0; i < subMeshes; ++i)
				render(vertices, mesh.getIndexBuffer(i), mesh.getAppearance(i), transform);
		} else if (node instanceof Sprite3D)
		{
			Sprite3D sprite = (Sprite3D)node;
			sprite.render(m_gl, transform);
		} else if (node instanceof Group)
		{
		    // XXX - render Group node
	    }
		//else
		//    throw new IllegalArgumentException("invalid node type");
	}
	
	/**
	 * Renders the given submesh with the given scope and the given transformation
	 * from local coordinates to world coordinates.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param vertices A <code>VertexBuffer</code> defining the vertex attributes.
	 * @param triangles An <code>IndexBuffer</code> defining the triangle strips.
	 * @param appearance An <code>Appearance</code> defining the surface properties.
	 * @param transform The transformation from the local coordinate system if
	 * <i>vertices</i> to world space. If <b>null</b>, use the identity matrix. 
	 * 
	 * @throws NullPointerException This exception is thrown if
	 * <i>vertices</i> is <b>null</b>.
	 * @throws NullPointerException This exception is thrown if
	 * <i>triangles</i> is <b>null</b>.
	 * @throws NullPointerException This exception is thrown if
	 * <i>appearance</i> is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a rendering target.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a current camera.
	 * @throws IllegalStateException This exception is thrown if
	 * any <i>vertices</i> or <i>triangles</i> violates the constraints
	 * defined in <code>VertexBuffer</code> or <code>IndexBuffer</code>.
	 */
	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform)
        throws NullPointerException, IllegalStateException
	{
		if (vertices == null)
			throw new NullPointerException("vertices == null");
		if (triangles == null)
			throw new NullPointerException("triangles == null");
		if (appearance == null)
			throw new NullPointerException("appearance == null");
		if (m_renderTarget == null)
			throw new IllegalStateException("no render target");
		if (m_camera == null)
			throw new IllegalStateException("no current camera");
        // XXX - check if vertices or triangles violates the constraints
		// defined in VertexBuffer or IndexBuffer.

		float[] scaleBias = new float[4];
		VertexArray positions = vertices.getPositions(scaleBias);
		FloatBuffer pos = positions.getFloatBuffer();
		pos.position(0);
		m_gl.glVertexPointer(positions.getComponentCount(), GL.GL_FLOAT, 0, pos);
		m_gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		
		VertexArray normals = vertices.getNormals();
		if (normals != null)
		{
			FloatBuffer norm = normals.getFloatBuffer();
			norm.position(0);

			m_gl.glEnable(GL.GL_NORMALIZE);
			m_gl.glNormalPointer(GL.GL_FLOAT, 0, norm);
			m_gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		} else
		{
			m_gl.glDisable(GL.GL_NORMALIZE);
			m_gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		}
		
		VertexArray colors = vertices.getColors();
		if (colors != null)
		{
			Buffer buffer = colors.getBuffer();
			buffer.position(0);
			m_gl.glColorPointer(colors.getComponentCount(), colors.getComponentTypeGL(), 0, buffer);
			m_gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		} else 
			m_gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		
		for (int i = 0; i < 8; ++i)
		{
			float[] texScaleBias = new float[4];
			VertexArray texcoords = vertices.getTexCoords(i, texScaleBias);
			m_gl.glActiveTexture(GL.GL_TEXTURE0 + i);
			m_gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
			if (texcoords != null)
			{
				FloatBuffer tex = texcoords.getFloatBuffer();
				tex.position(0);

				if (appearance.getTexture(i) != null)
					appearance.getTexture(i).setupGL(m_gl, texScaleBias);
				else
					m_gl.glDisable(GL.GL_TEXTURE_2D);
				
				m_gl.glTexCoordPointer(texcoords.getComponentCount(), GL.GL_FLOAT, 0, tex);
				m_gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);

			} else
			{
				m_gl.glDisable(GL.GL_TEXTURE_2D);
				m_gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY); 
			}
		}
		
		setAppearance(appearance);			
		
		m_gl.glPushMatrix();
		transform.multGL(m_gl);
		
		m_gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
		m_gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);

        if (triangles instanceof TriangleStripArray)
		{	
			IntBuffer indices = triangles.getBuffer();
			indices.position(0);
			m_gl.glDrawElements(GL.GL_TRIANGLE_STRIP, triangles.getIndexCount(), GL.GL_UNSIGNED_INT, indices);
		}
		else
			m_gl.glDrawElements(GL.GL_TRIANGLES, triangles.getIndexCount(), GL.GL_UNSIGNED_INT, triangles.getBuffer());

        m_gl.glPopMatrix();
	}
	
	/**
	 * Renders the given submesh with the given scope and the given transformation
	 * from local coordinates to world coordinates.
	 * <p>
     * See the <i>Mobile 3D Graphics API Technical Specification</i>
     * Version 1.1 (JSR-184) for more detail.
     * </p>
	 * 
	 * @param vertices A <code>VertexBuffer</code> defining the vertex attributes.
	 * @param triangles An <code>IndexBuffer</code> defining the triangle strips.
	 * @param appearance An <code>Appearance</code> defining the surface properties.
	 * @param transform The transformation from the local coordinate system if
	 * <i>vertices</i> to world space. If <b>null</b>, use the identity matrix. 
	 * @param scope The scope of the submesh; this determines whether the submesh
	 * is rendered at all, and if it is, which lights are used; <b>-1</b> makes
	 * the scope as wide as possible.
	 * 
	 * @throws NullPointerException This exception is thrown if
	 * <i>vertices</i> is <b>null</b>.
	 * @throws NullPointerException This exception is thrown if
	 * <i>triangles</i> is <b>null</b>.
	 * @throws NullPointerException This exception is thrown if
	 * <i>appearance</i> is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a rendering target.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a current camera.
	 * @throws IllegalStateException This exception is thrown if
	 * any <i>vertices</i> or <i>triangles</i> violates the constraints
	 * defined in <code>VertexBuffer</code> or <code>IndexBuffer</code>.
	 */
	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform, int scope)
        throws NullPointerException, IllegalArgumentException,
               IllegalStateException
	{
		// TODO: check scope.
		render(vertices, triangles, appearance, transform);
	}
	
	/**
	 * Renders an image of <i>world</i> as viewed by the active camera
	 * of that <code>World</code>.
	 * 
	 * @param world The <code>World</code> to render.
	 * 
	 * @throws NullPointerException This exception is thrown if
	 * <i>world</i> is <b>null</b>.
	 * @throws IllegalStateException This exception is thrown if this
	 * <code>Graphics3D</code> does not have a rendering target.
	 * @throws IllegalStateException This exception is thrown if the
	 * <code>World</code> does not have a current camera, or the
	 * active camera is not in the world.
	 * @throws IllegalStateException This exception is thrown if
	 * the background image of <i>world</i> is not in the same
	 * format as the currently bound rendering target.
	 * @throws IllegalStateException This exception is thrown if
	 * any  <code>Mesh</code> that is rendered violates the
	 * constraints in <code>Mesh</code>, <code>MorphingMesh</code>,
	 * <code>SkinnedMesh</code>, <code>VertexBuffer</code>,
	 * or <code>IndexBuffer</code>.
	 * @throws IllegalStateException This exception is thrown if
	 * the transformation from the active camera of the <i>world</i>
	 * to the world space is uninvertible
	 */
	public void render(World world)
	    throws NullPointerException, IllegalStateException
	{
		// Validate arguments.
		if (world == null)
			throw new NullPointerException("world == null");
		if (m_renderTarget == null)
			throw new IllegalStateException("no render target");
		// XXX - check if any Mesh that is rendered violates the
		// constraints in Mesh, MorphingMesh, SkinnedMesh, VertexBuffer,
		// or IndexBuffer.
		// XXX - check if the world's background image is the same
		// format as the currently bound rendering target.
		// XXX - check if the transformation from the active camera
		// of the world to the world space can be inverted.

		// Clear using the world's background.
		clear(world.getBackground());
		
		Transform t = new Transform();
		
		// Setup camera
		Camera c = world.getActiveCamera();
		if (c == null)
			throw new IllegalStateException("World has no active camera.");
		if (! c.getTransformTo(world, t))
			throw new IllegalStateException("Camera is not in world.");
		setCamera(c, t);
		
		// Setup lights
		resetLights();
		populateLights(world, world);
		
		// Begin traversal of scene graph.
		renderDescendants(world, world);
	}
	
	// Populate the world with lights.
	private void populateLights(World world, Object3D obj)
	{
		int numReferences = obj.getReferences(null);
		if (numReferences > 0)
		{
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for (int i = 0; i < numReferences; ++i)
			{
				if (objArray[i] instanceof Light)
				{
					Transform t = new Transform();
					Light light = (Light)objArray[i];
					if (light.isRenderingEnabled() && light.getTransformTo(world, t))
						addLight(light, t);
				}
				populateLights(world, objArray[i]);
	    	}
	 	}		
	}
	
	// Render the world's descendants.
	private void renderDescendants(World world, Object3D obj)
	{
		int numReferences = obj.getReferences(null);
		if (numReferences > 0)
		{
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for (int i = 0; i < numReferences; ++i)
			{
				if (objArray[i] instanceof Node)
				{
					Transform t = new Transform();
					Node node = (Node)objArray[i];
					node.getTransformTo(world, t);
					render(node, t);
				}
	       		renderDescendants(world, objArray[i]);
	    	}
	 	}		
	}
	
	/**
	 * Set up the OpenGL appearance state.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 * 
	 * @param appearance The <code>Appearance</code> specifying the
	 * surface properties.
	 */
	void setAppearance(Appearance appearance)
	{
		if (appearance == null)
			throw new NullPointerException("appearance must not be null");

		// Polygon mode
		PolygonMode polyMode = appearance.getPolygonMode();
		if (polyMode == null)
			polyMode = m_defaultPolygonMode;
		polyMode.setupGL(m_gl);
		
		// Material
		if (appearance.getMaterial() != null)
			appearance.getMaterial().setupGL(m_gl, polyMode.getLightTarget());
		else
			m_gl.glDisable(GL.GL_LIGHTING);
			
		// Fog
		if (appearance.getFog() != null)
			appearance.getFog().setupGL(m_gl);
		else
			m_gl.glDisable(GL.GL_FOG);
		
		// Compositing mode
		if (appearance.getCompositingMode() != null)
			appearance.getCompositingMode().setupGL(m_gl);
		else
			m_defaultCompositingMode.setupGL(m_gl);
	}
	/**
	 * Set the OpenGL context.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 * 
	 * @param gl The OpenGL context to set.
	 */
	void setGL(GL gl)
	{
		this.m_gl = gl;
	}
	
	/**
	 * Get the OpenGL context.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 *
	 * @return The OpenGL context is returned.
	 */
	GL getGL()
	{
		if (this.m_gl != null)
			return this.m_gl;

		// Try to fetch a gl object from the Emulator environment instead!
		// NOTE: experimental, not working
		return MIDPEmulator.getInstance().getGL();
	}
	
	/**
	 * Get the OpenGL Utility context.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 * 
	 * @return The OpenGL Utility context is returned.
	 */
	GLU getGLU()
	{
		if (m_glu == null)
			m_glu = new GLU();
		return m_glu;
	}
	
	/**
	 * Get the number of texture units.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 * 
	 * @return the number of texture units is returned.
	 */
	int getTextureUnitCount()
	{
		return m_numTextureUnits;
	}
	
	/**
	 * Disable the texture units.
	 * <p>
	 * Note that this API is not part of the M3G specification.
	 * </p>
	 */
	void disableTextureUnits()
	{
		for (int i = 0; i < m_numTextureUnits; i++)
		{
			m_gl.glActiveTexture(GL.GL_TEXTURE0 + i);
			m_gl.glDisable(GL.GL_TEXTURE_2D);
		}
	}
}
