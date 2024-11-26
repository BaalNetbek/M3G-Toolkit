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
import java.lang.IllegalArgumentException;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.*;
import java.awt.image.BufferedImage;

// Import M3GToolkit classes.
import com.wizzer.m3g.AnimationController;
import com.wizzer.m3g.AnimationTrack;
import com.wizzer.m3g.Appearance;
import com.wizzer.m3g.Background;
import com.wizzer.m3g.Camera;
import com.wizzer.m3g.CompositingMode;
import com.wizzer.m3g.ExternalReference;
import com.wizzer.m3g.ExternalReferencesSection;
import com.wizzer.m3g.Fog;
import com.wizzer.m3g.Group;
import com.wizzer.m3g.HeaderObject;
import com.wizzer.m3g.HeaderSection;
import com.wizzer.m3g.Image2D;
import com.wizzer.m3g.IndexBuffer;
import com.wizzer.m3g.KeyframeSequence;
import com.wizzer.m3g.Light;
import com.wizzer.m3g.M3GFile;
import com.wizzer.m3g.M3GObject;
import com.wizzer.m3g.Material;
import com.wizzer.m3g.Mesh;
import com.wizzer.m3g.MorphingMesh;
import com.wizzer.m3g.Node;
import com.wizzer.m3g.Object3D;
import com.wizzer.m3g.PolygonMode;
import com.wizzer.m3g.SceneSection;
import com.wizzer.m3g.Section;
import com.wizzer.m3g.SkinnedMesh;
import com.wizzer.m3g.Sprite3D;
import com.wizzer.m3g.Texture2D;
import com.wizzer.m3g.Transform;
import com.wizzer.m3g.Transformable;
import com.wizzer.m3g.TriangleStripArray;
import com.wizzer.m3g.VertexArray;
import com.wizzer.m3g.VertexBuffer;
import com.wizzer.m3g.World;

/**
 * This class is the main entry point for dumping the contents of a
 * Mobile 3D Graphics binary file (.m3g extension).
 * 
 * @author Mark Millard
 */
public class DumpM3g
{
	/** A handle to the M3G file. */
	protected M3GFile m_m3gFile = null;
	/** A handle to a <code>PrintStream</code> to dump to. */
	protected PrintStream m_out = null;
	/** The M3G file header. */
	protected HeaderSection m_header = null;
	/** Be verbose. */
	protected boolean m_verbose = false;
	
	/**
	 * Hide the default constructor.
	 */
	private DumpM3g() {}

	/**
	 * Constructs a <code>DumpM3g</code> for the specified file name.
	 * 
	 * @param file The M3G file to to dump.
	 */
	public DumpM3g(M3GFile file)
	{
		m_m3gFile = file;
	}
	
	/**
	 * Set the verbosity.
	 * 
	 * @param flag If <b>true</b>, then the output will be verbose.
	 */
	public void setVerbose(boolean flag)
	{
		m_verbose = flag;
	}

	/**
	 * Redirect output of the dump to a stream.
	 * 
	 * @param stream The <code>OutputStream</code> to dump to.
	 * If set to <b>null</b> then the output will be dumped to stdout.
	 */
	public void setOutputStream(OutputStream stream)
	{
		if (stream == null)
			m_out = null;
		else
		    m_out = new PrintStream(stream);
	}

	/**
	 * Dump the Header Section.
	 */
	public void dumpHeaderSection() throws IOException
	{	
		m_header = m_m3gFile.getHeaderSection();
		dumpHeaderInfo(m_header);
	}
		
	/**
	 * Dump the common Section information to stdout.
	 * 
	 * @param section The <code>Section</code> to dump.
	 */
	protected void dumpSectionInfo(Section section)
	{
		byte[] data;
		println("************************************************************");
		println("******************* Section Information ********************");
		println("************************************************************");
		println("Compression Scheme: " + section.getCompressionScheme());
		data = intToByteArray(section.getTotalSectionLength());
		println("Total Section Length: " + Unsigned.readDWORD(data,0));
		data = intToByteArray(section.getUncompressedLength());
		println("Uncompressed Length: " + Unsigned.readDWORD(data,0));
		data = intToByteArray(section.getChecksum());
		println("Checksum: " + Unsigned.readDWORD(data,0) +
			" (0x" + Integer.toHexString(section.getChecksum()) + ")");
	}
	
	/**
	 * Dump the Header Section information to stdout.
	 * 
	 * @param header The header <code>Section</code> to dump.
	 */
	protected void dumpHeaderInfo(HeaderSection header)
	{
		if (m_verbose)
			dumpSectionInfo(header);

		byte data[];
		HeaderObject hObject = header.getHeaderObject();
		println("************************************************************");
		println("******************** Header Information ********************");
		println("************************************************************");
		println("Version Major Number: " + hObject.getVersionMajor());
		println("Version Minor Number: " + hObject.getVersionMinor());
		println("Has External References: " + hObject.isHasExternalReferences());
		data = intToByteArray((int)hObject.getTotalFileSize());
		println("Total File Size: " + Unsigned.readDWORD(data,0));
		data = intToByteArray((int)hObject.getApproximateContentSize());
		println("Approximate Content Size: " + Unsigned.readDWORD(data,0));
		println("Authoring Field: " + hObject.getAuthoringField());
		println("************************************************************");
	}
	
	/**
	 * Dump the External Reference Section information.
	 */
	public void dumpExternalReferenceSection()
	{
		ExternalReferencesSection reference;
		if (! m_header.getHeaderObject().isHasExternalReferences())
			return;
		else
		    reference = m_m3gFile.getExternalReferencesSection();
		
		if (m_verbose)
			dumpSectionInfo(reference);

		ExternalReference[] refs = reference.getExternalReferences();
		println("************************************************************");
		println("******************* External References ********************");
		println("************************************************************");
		println("Current Working Directory: " + ExternalReference.getCwd());
		for (int i = 0; i < refs.length; i++)
		{
			String uri = refs[i].getURI();
			println("Reference " + i + ": " + uri);
		}
		
		println("************************************************************");
	}
	
	/**
	 * Dump the Scene Sections.
	 */
	public void dumpSceneSections()
	{
		SceneSection[] scenes = m_m3gFile.getSceneSections();
		
		// There must be at lease 1 SceneSection.
		
		println("");
		println("##### Number of Scene Sections: " + scenes.length);
		for (int i = 0; i < scenes.length; i++)
		{
			println("##### Scene Section " + i);
			
			if (m_verbose)
				dumpSectionInfo(scenes[i]);

			Object3D[] objects = scenes[i].getObjects3D();
			for (int j = 0; j < objects.length; j++)
			{
				dumpObject(objects[j]);
			}
		}
	}
	
	/**
	 * Dump the Scene Section Object to stdout.
	 * 
	 * @param object The object to dump.
	 */
	protected void dumpObject(Object3D object)
	{
		println("");
		println("************************************************************");
		println("******************* Scene Section Object *******************");
		println("************************************************************");
		println("Instance Identifier: " + object);
		if (m_verbose)
		{
			// Dump references.
			// TODO - object.getReferences() not implemented yet.
		}
		int type = object.getObjectType();
		switch (type)
		{
			case M3GObject.ANIMATION_CONTROLLER:
				println("Object: AnimationController");
				dumpAnimationController((AnimationController) object);
				break;
			case M3GObject.ANIMATION_TRACK:
				println("Object: AnimationTrack");
				dumpAnimationTrack((AnimationTrack) object);
				break;
			case M3GObject.APPEARANCE:
				println("Object: Appearance");
				dumpAppearance((Appearance) object);
				break;
			case M3GObject.BACKGROUND:
				println("Object: Background");
				dumpBackground((Background) object);
				break;
			case M3GObject.CAMERA:
				println("Object: Camera");
				dumpCamera((Camera) object);
				break;
			case M3GObject.COMPOSITING_MODE:
				println("Object: CompositingMode");
				dumpCompositingMode((CompositingMode) object);
				break;
			case M3GObject.FOG:
				println("Object: Fog");
				dumpFog((Fog) object);
				break;
			case M3GObject.GROUP:
				println("Object: Group");
				dumpGroup((Group) object);
				break;
			case M3GObject.IMAGE2D:
				println("Object: Image2D");
				dumpImage2D((Image2D) object);
				break;
			case M3GObject.KEYFRAME_SEQUENCE:
				println("Object: KeyFrameSequence");
				dumpKeyframeSequence((KeyframeSequence) object);
				break;
			case M3GObject.LIGHT:
				println("Object: Light");
				dumpLight((Light) object);
				break;
			case M3GObject.MATERIAL:
				println("Object: Material");
				dumpMaterial((Material) object);
				break;
			case M3GObject.MESH:
				println("Object: Mesh");
				dumpMesh((Mesh) object);
				break;
			case M3GObject.MORPHING_MESH:
				println("Object: MorphingMesh");
				dumpMorphingMesh((MorphingMesh) object);
				break;
			case M3GObject.SKINNED_MESH:
				println("Object: SkinnedMesh");
				dumpSkinnedMesh((SkinnedMesh) object);
				break;
			case M3GObject.POLYGON_MODE:
				println("Object: PolygonMode");
				dumpPolygonMode((PolygonMode) object);
				break;
			case M3GObject.SPRITE3D:
				println("Object: Sprite3D");
				dumpSprite3D((Sprite3D) object);
				break;
			case M3GObject.TEXTURE2D:
				println("Object: Texture2D");
				dumpTexture2D((Texture2D) object);
				break;
			case M3GObject.TRIANGLE_STRIP_ARRAY:
				println("Object: TriangleStripArray");
				dumpTriangleStripArray((TriangleStripArray) object);
				break;
			case M3GObject.VERTEX_ARRAY:
				println("Object: VertexArray");
				dumpVertexArray((VertexArray) object);
				break;
			case M3GObject.VERTEX_BUFFER:
				println("Object: VertexBuffer");
				dumpVertexBuffer((VertexBuffer) object);
				break;
			case M3GObject.WORLD:
				println("Object: World");
				dumpWorld((World) object);
				break;
			default:
				println("Object: UKNOWN = " +  type);
		}
		
		println("************************************************************");
	}
	
	/**
	 * Dump the animation controller Object to stdout.
	 * 
	 * @param controller The animation controller <code>Object3D</code> to dump.
	 */
	protected void dumpAnimationController(AnimationController controller)
	{
		// Dump the Object3D state.
		dumpObject3D(controller);
		
		println("Speed: " + controller.getSpeed());
		println("Weight: " + controller.getWeight());
		println("Active Interval Start: " + controller.getActiveIntervalStart());
		println("Active Interval end: " + controller.getActiveIntervalEnd());
		println("Reference Sequence Time: " + controller.getRefSequenceTime());
		println("Reference World Time: " + controller.getRefWorldTime());
	}
	
	/**
	 * Dump the animation track Object to stdout.
	 * 
	 * @param track The animation track <code>Object3D</code> to dump.
	 */
	protected void dumpAnimationTrack(AnimationTrack track)
	{
		// Dump the Object3D state.
		dumpObject3D(track);
		
		println("Keyframe Sequence Reference: " + track.getKeyframeSequence());
		println("Animation Controller Reference: " + track.getController());
		byte[] data = intToByteArray(track.getTargetProperty());
		println("Property ID: " + Unsigned.readDWORD(data,0));
	}
	
	/**
	 * Dump the appearance Object to stdout.
	 * 
	 * @param appearance The appearance <code>Object3D</code> to dump.
	 */
	protected void dumpAppearance(Appearance appearance)
	{
		// Dump the Object3D state.
		dumpObject3D(appearance);
		
		int layer = appearance.getLayer();
		println("Layer: " + layer);
		CompositingMode cmode = appearance.getCompositingMode();
		println("Compositing Mode Reference: " + cmode);
		Fog fog = appearance.getFog();
		println("Fog Reference: " + fog);
		PolygonMode pmode = appearance.getPolygonMode();
		println("Polygon Mode Reference: " + pmode);
		Material material = appearance.getMaterial();
		println("Material Reference: " + material);
		Texture2D textures = appearance.getTexture(0);
	}
	
	/**
	 * Dump the background Object to stdout.
	 * 
	 * @param background The background <code>Object3D</code> to dump.
	 */
	protected void dumpBackground(Background background)
	{
		// Dump the Object3D state.
		dumpObject3D(background);
		
		int color = background.getColor();
		println("Color: 0x" +  Integer.toHexString(color));
		println("Background Image Reference: " + background.getImage());
		println("Background Image Mode x: " + background.getImageModeX());
		println("Background Image Mode y: " + background.getImageModeY());
		println("Crop x Location: " + background.getCropX());
		println("Crop y Location: " + background.getCropY());
		println("Crop Width: " + background.getCropWidth());
		println("Crop Height: " + background.getCropHeight());
		println("Depth Clear Enabled: " + background.isDepthClearEnabled());
		println("Color Clear Enabled: " + background.isColorClearEnabled());
	}
	
	/**
	 * Dump the camera Object to stdout.
	 * 
	 * @param camera The camera <code>Object3D</code> to dump.
	 */
	protected void dumpCamera(Camera camera)
	{
		// Dump the common node state.
		dumpNode(camera);
		
		float[] params = new float[4];
		int type = camera.getProjection(params);
		switch (type)
		{
			case Camera.PARALLEL:
				println("Projection Type: Parallel");
				println("fovy: " + params[0]);
				println("aspectRatio: " + params[1]);
				println("near: " + params[2]);
				println("far: " + params[3]);
				break;
			case Camera.PERSPECTIVE:
				println("Projection Type: Perspective");
				println("fovy: " + params[0]);
				println("aspectRatio: " + params[1]);
				println("near: " + params[2]);
				println("far: " + params[3]);
				break;
			case Camera.GENERIC:
				println("Projection Type: Generic");
				break;
			default:
				println("Projection Type: UKNOWN = " + type);
		}
	}
	
	/**
	 * Dump the compositing mode Object to stdout.
	 * 
	 * @param mode The mode <code>Object3D</code> to dump.
	 */
	protected void dumpCompositingMode(CompositingMode mode)
	{
		// Dump the Object3D state.
		dumpObject3D(mode);
		
		println("Depth Test Enabled: " + mode.isDepthTestEnabled());
		println("Depth Write Enabled: " + mode.isDepthWriteEnabled());
		println("Color Write Enabled: " + mode.isColorWriteEnabled());
		println("Alpha Write Enabled: " + mode.isAlphaWriteEnabled());
		println("Blending: " + mode.getBlending());
		println("Alpha Threshold: " + mode.getAlphaThreshold());
		println("Depth Offset Factor: " + mode.getDepthOffsetFactor());
		println("Depth Offset Units: " + mode.getDepthOffsetUnits());
	}
	
	/**
	 * Dump the fog Object to stdout.
	 * 
	 * @param fog The fog <code>Object3D</code> to dump.
	 */
	protected void dumpFog(Fog fog)
	{
		// Dump the Object3D state.
		dumpObject3D(fog);
		
		int color = fog.getColor();
		println("Color: 0x" + Integer.toHexString(color));
		int mode = fog.getMode();
		println("Mode: " + mode);
		if (mode == Fog.EXPONENTIAL)
			println("Density: " + fog.getDensity());
		else if (mode == Fog.LINEAR)
		{
			println("Near: " + fog.getNearDistance());
			println("Far: " + fog.getFarDistance());
		} else
		{
			println("Mode Type UNKNOWN");
		}
	}
	
	/**
	 * Dump the common goup information.
	 * 
	 * @param group The group <code>Node</code> to dump.
	 */
	protected void dumpGroup(Group group)
	{
		// Dump the common node state.
		dumpNode(group);
		
		println("####### Group Information ######");
		int count = group.getChildCount();
		println("Number of Children: " + count);
		for (int i = 0; i < count; i++)
		{
			Node child = group.getChild(i);
			println("Child Node Reference: " + child);
		}
		println("################################");
	}
	
	/**
	 * Dump the image Object to stdout.
	 * 
	 * @param image The image <code>Object3D</code> to dump.
	 */
	protected void dumpImage2D(Image2D image)
	{
		byte[] data;
		
		// Dump the Object3D state.
		dumpObject3D(image);
		
		println("Format: " + image.getFormat());
		boolean isMutable = image.isMutable();
		println("Is Mutable: " + isMutable);
		data = intToByteArray(image.getWidth());
		println("Width: " + Unsigned.readDWORD(data,0));
		data = intToByteArray(image.getHeight());
		println("Height: " + Unsigned.readDWORD(data,0));
		if (isMutable)
		{
			BufferedImage content = image.getImage();
			// TODO: Dump the palette and pixel content.
		}
	}
	
	/**
	 * Dump the keyframe sequence Object to stdout.
	 * 
	 * @param sequence The keyframe sequence <code>Object3D</code> to dump.
	 */
	protected void dumpKeyframeSequence(KeyframeSequence sequence)
	{
		byte[] data;
		
		// Dump the Object3D state.
		dumpObject3D(sequence);
		
		println("Interpolation: " + sequence.getInterpolationType());
		println("Repeat Mode: " + sequence.getRepeatMode());
		int encoding = sequence.getEncoding();
		println("Encoding: " + encoding);
		data = intToByteArray(sequence.getDuration());
		println("Duration: " + Unsigned.readDWORD(data,0));
		data = intToByteArray(sequence.getValidRangeFirst());
		println("Valid Range First: " + Unsigned.readDWORD(data,0));
		data = intToByteArray(sequence.getValidRangeLast());
		println("Valid Range Last: " + Unsigned.readDWORD(data,0));
		int componentCount = sequence.getComponentCount();
		data = intToByteArray(componentCount);
		println("Component Count: " + Unsigned.readDWORD(data,0));
		int keyframeCount = sequence.getKeyframeCount();
		data = intToByteArray(keyframeCount);
		println("Keyframe Count " + Unsigned.readDWORD(data,0));
		if (encoding == 0)
		{
			for (int i = 0; i < keyframeCount; i++)
			{
				println("Key Frame " + i + ": ");
				int time = sequence.getTimes()[i];
				data = intToByteArray(time);
				println("\tDuration: " + Unsigned.readDWORD(data,0));
				for (int j = 0; j < componentCount; j++)
				{
					println("\tVector Value: " + sequence.getKeyFrames()[j]);
				}
			}
		} else if (encoding == 1)
		{
			// TODO: implement this encoding scheme.
		} else if (encoding == 2)
		{
			// TODO: implement this encoding scheme.
		}
	}
	
	/**
	 * Dump the light Object to stdout.
	 * 
	 * @param light The light <code>Object3D</code> to dump.
	 */
	protected void dumpLight(Light light)
	{
		// Dump the common node state.
		dumpNode(light);

		println("Attenuation Constant: " + light.getConstantAttenuation());
		println("Attenuation Linear: " + light.getLinearAttenuation());
		println("Attenuation Quadratic: " + light.getQuadraticAttenuation());
		int color = light.getColor();
		println("Color: 0x" + Integer.toHexString(color));
		println("Mode: " + light.getMode());
		println("Intensity: " + light.getIntensity());
		println("Spot Angle:" + light.getSpotAngle());
		println("Spot Exponent:" + light.getSpotExponent());
	}
	
	/**
	 * Dump the material Object to stdout.
	 * 
	 * @param material The material <code>Object3D</code> to dump.
	 */
	protected void dumpMaterial(Material material)
	{
		// Dump the Object3D state.
		dumpObject3D(material);
		
		int ambient = material.getColor(Material.AMBIENT);
		println("Ambient Color: 0x" + Integer.toHexString(ambient));
		int diffuse = material.getColor(Material.DIFFUSE);
		println("Diffuse Color: 0x" + Integer.toHexString(diffuse));
		int emissive = material.getColor(Material.EMISSIVE);
		println("Emissive Color: 0x" + Integer.toHexString(emissive));
		int specular = material.getColor(Material.SPECULAR);
		println("Specular Color: 0x" + Integer.toHexString(specular));
		println("Shininess: " + material.getShininess());
		println("Vertex Color Tracking Enabled: " +
			material.isVertexColorTrackingEnabled());
	}
	
	/**
	 * Dump the mesh Object to stdout.
	 * 
	 * @param mesh The mesh <code>Object3D</code> to dump.
	 */
	protected void dumpMesh(Mesh mesh)
	{
		// Dump the common node state.
		dumpNode(mesh);

		VertexBuffer buffer = mesh.getVertexBuffer();
		println("Vertex Buffer Reference: " + buffer);
		int count = mesh.getSubmeshCount();
		println("Submesh Count: " + count);
		for (int i = 0; i < count; i++)
		{
			IndexBuffer indexBuffer = mesh.getIndexBuffer(i);
			println("Submesh " + i + " IndexBuffer Reference: " +
				indexBuffer);
			Appearance appearance = mesh.getAppearance(i);
			println("Submesh " + i + " Appearance Reference: " +
					appearance);
		}
	}
	
	/**
	 * Dump the mesh Object to stdout.
	 * 
	 * @param mesh The morphing mesh <code>Object3D</code> to dump.
	 */
	protected void dumpMorphingMesh(MorphingMesh mesh)
	{
		// Dump the base Mesh object.
		dumpMesh(mesh);
		
		int count = mesh.getMorphTargetCount();
		println("Morph Target count: " + count);
		float[] weights = new float[count];
		mesh.getWeights(weights);
		for (int i = 0; i < count; i++)
		{
			VertexBuffer morphTarget = mesh.getMorphTarget(i);
			println("Morph Target " + i + ": " + morphTarget);
			println("Initial Weight: " + weights[i]);
		}
	}
	
	/**
	 * Dump the mesh Object to stdout.
	 * 
	 * @param mesh The skinned mesh <code>Object3D</code> to dump.
	 */
	protected void dumpSkinnedMesh(SkinnedMesh mesh)
	{
		// Dump the base Mesh object.
		dumpMesh(mesh);
		
		println("Skeleton: " + mesh.getSkeleton());
		int count = mesh.getTransformReferenceCount();
		println("Transform Reference Count: " + count);
		for (int i = 0; i < count; i++)
		{
			Node bone = mesh.getTransformNode(i);
			println("Transform Node: " + bone);
			println("First Vertex: " + mesh.getFirstVertex(i));
			println("Vertex Count: " + mesh.getVertexCount(i));
			println("Weight: " + mesh.getWeight(i));
		}
	}
	
	/**
	 * Dump the common node state to stdout.
	 * 
	 * @param node The node <code>Object3D</code> to dump.
	 */
	protected void dumpNode(Node node)
	{
		// Dump the Transformable state.
		dumpTransformable(node);
		
		println("####### Node Information #######");
		println("Enable Rendering: " + node.isRenderingEnabled());
		println("Enable Picking: " + node.isPickingEnabled());
		println("Alpha Factor: " + node.getAlphaFactor());
		byte[] data = intToByteArray(node.getScope());
		println("Scope: " + Unsigned.readDWORD(data,0));
		println("Algnment: " + node.hasAlignment());
		if (node.hasAlignment())
		{
			println("z Target: " + node.getZTarget());
			println("y Target: " + node.getYTarget());
			println("z Reference: " + node.getZReference());
			println("y Reference: " + node.getYReference());
		}
		println("################################");
	}
	
	/**
	 * Dump the Object state to stdout.
	 * 
	 * @param obj The <code>Object3D</code> to dump.
	 */
	protected void dumpObject3D(Object3D obj)
	{
		println("##### Object3D Information #####");
		println("User Identifier: " + obj.getUserID());
		int trackCount = obj.getAnimationTrackCount();
		println("Number of Animation Tracks: " + trackCount);
		for (int i = 0; i < trackCount; i++)
		{
			AnimationTrack track = obj.getAnimationTrack(i);
			println("Animation Track " + i + " Reference: " + track);
		}
		// TODO: Dump user parameter information.
		println("################################");
	}
	
	/**
	 * Dump the polygon mode Object state to stdout.
	 * 
	 * @param mode The polygon mode <code>Object3D</code> to dump.
	 */
	protected void dumpPolygonMode(PolygonMode mode)
	{
		// Dump the Object3D state.
		dumpObject3D(mode);
		
		println("Culling: " + mode.getCulling());
		println("Shading: " + mode.getShading());
		println("Winding: " + mode.getWinding());
		println("Two-Sided Lighting Enabled: " + mode.isTwoSidedLightingEnabled());
		println("Local Camera Lighting Enabled: " + mode.isLocalCameraLightingEnabled());
		println("Perspective Correction Enabled: " + mode.isPerspectiveCorrectionEnabled());
	}
	
	protected void dumpSprite3D(Sprite3D sprite)
	{
		// Dump the common node state.
		dumpNode(sprite);
	}
	
	/**
	 * Dump the transformable state to stdout.
	 * 
	 * @param trans The transformable <code>Object3D</code> to dump.
	 */
	protected void dumpTransformable(Transformable trans)
	{
		// Dump the Object3D state.
		dumpObject3D(trans);
		
		println("## Transformable Information ###");
		println("Has Component Transform: " + trans.hasComponentTransform());
		println("Has General Transform: " + trans.hasGeneralTransform());
		if (trans.hasComponentTransform())
		{
			float translation[] = new float[3];
			trans.getTranslation(translation);
			StringBuffer buffer = new StringBuffer("x = ");
			buffer.append(translation[0]);
			buffer.append("; y = ");
			buffer.append(translation[1]);
			buffer.append("; z = ");
			buffer.append(translation[2]);
			println("Translation: " + buffer.toString());
			
			float scale[] = new float[3];
			trans.getScale(scale);
			buffer = new StringBuffer("x = ");
			buffer.append(scale[0]);
			buffer.append("; y = ");
			buffer.append(scale[1]);
			buffer.append("; z = ");
			buffer.append(scale[2]);
			println("Scale: " + buffer.toString());
			
			float[] angleAxis = new float[4];
			trans.getOrientation(angleAxis);
			buffer = new StringBuffer();
			buffer.append(angleAxis[0]);
			println("Orientation Angle: " + buffer.toString());
			
			angleAxis = new float[4];
			trans.getOrientation(angleAxis);
			buffer = new StringBuffer("x = ");
			buffer.append(angleAxis[1]);
			buffer.append("; y = ");
			buffer.append(angleAxis[2]);
			buffer.append("; z = ");
			buffer.append(angleAxis[3]);
			println("Orientation Axis: " + buffer.toString());
		}
		if (trans.hasGeneralTransform())
		{
			Transform transform = new Transform();
			trans.getTransform(transform);
			float[] matrix = new float[16];
			transform.get(matrix);
			StringBuffer buffer = new StringBuffer("[ ");
			for (int i = 0; i < 16; i++)
			{
				buffer.append(matrix[i]);
				buffer.append(" ");
			}
			buffer.append("]");
			println("General Transform: " + buffer.toString());
		}
		println("################################");
	}
	
	/**
	 * Dump the texture Object to stdout.
	 * 
	 * @param texture The texutre <code>Object3D</code> to dump.
	 */
	protected void dumpTexture2D(Texture2D texture)
	{
		// Dump the Tranformable state.
		dumpTransformable(texture);
		
		Image2D image = texture.getImage();
		println("Image Reference: " + image);
		int blendColor = texture.getBlendColor();
		println("Blend Color: 0x" + Integer.toHexString(blendColor));
		println("Blending: " + texture.getBlending());
		println("WrappingS: " + texture.getWrappingS());
		println("WrappingT: " + texture.getWrappingT());
		println("Level Filter: " + texture.getLevelFilter());
		println("Image Filter: " + texture.getImageFilter());
	}
	
	/**
	 * Dump the triangle strip array Object to stdout.
	 * 
	 * @param array The triangle strip array <code>Object3D</code> to dump.
	 */
	protected void dumpTriangleStripArray(TriangleStripArray array)
	{
		// Dump the Object3D state.
		dumpObject3D(array);
		
		int encoding = array.getEncoding();
		println("Encoding: " + encoding);
		if ((encoding >= 0) && (encoding < 3))
			println("StartIndex: " +  array.getStartIndex());
		else
		{
			int[] indices = array.getIndices();
			print("Indices: [ ");
			for (int i = 0; i < indices.length; i++)
			{
				if (i%16 == 0)
				{
					println(null);
					print("\t");
				}
				print(indices[i]);
				if (i != indices.length - 1)
					print(", ");
			}
			println(null);
			println("]");
		}
		int[] stripLengths = array.getStripLengths();
		print("Strip Lengths: [ ");
		for (int i = 0; i < stripLengths.length; i++)
		{
			if (i%16 == 0)
			{
				println(null);
				print("\t");
			}
			print(stripLengths[i]);
			if (i != stripLengths.length - 1)
				print(", ");
		}
		println(null);
		println("]");
	}
	
	/**
	 * Dump the vertex array Object to stdout.
	 * 
	 * @param array The vertex array <code>Object3D</code> to dump.
	 */
	protected void dumpVertexArray(VertexArray array)
	{
		// Dump the Object3D state.
		dumpObject3D(array);
		
		println("Component Size: " + array.getComponentSize());
		println("Component Count: " + array.getComponentCount());
		println("Encoding: " + array.getEncoding());
		println("Vertex Count: " + array.getVertexCount());
		if (array.getComponentSize() == 1)
		{
			ByteBuffer buffer = (ByteBuffer)array.getBuffer();
			int numElements = array.getVertexCount() * array.getComponentSize();
			byte[] vertices = new byte[numElements];
			buffer.get(vertices);
			print("Vertices: [ ");
			for (int i = 0; i < vertices.length; i++)
			{
				if (i%(array.getComponentCount()*5) == 0)
				{
					println(null);
					print("\t");
				}
				print(vertices[i]);
				if (i != vertices.length - 1)
					print(", ");
			}
			println(null);
			println("]");
			
			// Rewind the buffer in case we want to access it again.
			buffer.rewind();
		} else
		{
			ShortBuffer buffer = (ShortBuffer)array.getBuffer();
			int numElements = array.getVertexCount() * array.getComponentSize();
			short[] vertices = new short[numElements];
			buffer.get(vertices);
			print("Vertices: [ ");
			for (int i = 0; i < vertices.length; i++)
			{
				if (i%(array.getComponentCount()*5) == 0)
				{
					println(null);
					print("\t");
				}
				print(vertices[i]);
				if (i != vertices.length - 1)
					print(", ");
			}
			println(null);
			println("]");

			// Rewind the buffer in case we want to access it again.
			buffer.rewind();
		}
	}
	
	/**
	 * Dump the vertex buffer Object to stdout.
	 * 
	 * @param buffer The vertex buffer <code>Object3D</code> to dump.
	 */
	protected void dumpVertexBuffer(VertexBuffer buffer)
	{
		// Dump the Object3D state.
		dumpObject3D(buffer);
		
		int defaultColor = buffer.getDefaultColor();
		println("Default Color: 0x" + Integer.toHexString(defaultColor));
		float[] scaleBias = new float[4];
		int vCount = buffer.getVertexCount();
		println("Number of Vertices: " + vCount);
		VertexArray positions = buffer.getPositions(scaleBias);
		println("Position Array Reference: " + positions);
		println("Postion Scale :" + scaleBias[0]);
		println("Position X Bias :" + scaleBias[1]);
		println("Position Y Bias :" + scaleBias[2]);
		println("Position Z Bias :" + scaleBias[3]);
		VertexArray normals = buffer.getNormals();
		println("Normal Array Reference: " + normals);
		VertexArray colors = buffer.getColors();
		println("Color Array Reference: " + colors);
		int texcoordArrayCount = buffer.getTexcoordArrayCount();
		for (int i = 0; i < texcoordArrayCount; i++)
		{
			VertexArray texCoords = buffer.getTexCoords(i, scaleBias);
			println("Texture Coordinate Array Reference: " + texCoords);
			println("Texture Coord Scale :" + scaleBias[0]);
			println("Texture Coord X Bias :" + scaleBias[1]);
			println("Texture Coord Y Bias :" + scaleBias[2]);
			println("Texture Coord Z Bias :" + scaleBias[3]);
		}
	}
	
	/**
	 * Dump the world Object to stdout.
	 * 
	 * @param world The world <code>Object3D</code> to dump.
	 */
	protected void dumpWorld(World world)
	{
		// Dump the group information.
		dumpGroup(world);

		Camera camera = world.getActiveCamera();
		println("Active Camera Reference: " + camera);
		Background background = world.getBackground();
		println("Background Reference: " + background);
	}
	
	/**
	 * Print a string then terminate a line.
	 * <p>
	 * Be default, the string will be dumped to stdout.
	 * </p>
	 * 
	 * @param str The string to dump.
	 */
	protected void println(String str)
	{
		if (m_out != null)
		{
			if (str == null) m_out.println();
			else m_out.println(str);
			m_out.flush();
		} else
		{
			if (str == null) System.out.println();
			else System.out.println(str);			
		}
	}
	
	/**
	 * Print a string.
	 * <p>
	 * Be default, the string will be dumped to stdout.
	 * </p>
	 * 
	 * @param str The string to dump.
	 */
	protected void print(String str)
	{
		if (m_out != null)
		{
			m_out.print(str);
		} else
			System.out.print(str);
	}
	
	/**
	 * Print an integer.
	 * <p>
	 * Be default, the integer will be dumped to stdout.
	 * </p>
	 * 
	 * @param value The integer to dump.
	 */
	protected void print(int value)
	{
		if (m_out != null)
		{
			m_out.print(value);
		} else
			System.out.print(value);
	}

	/*
	 * Returns a byte array containing the two's-complement representation of the integer.<br>
	 * The byte array will be in big-endian byte-order with a fixes length of 4
	 * (the least significant byte is in the 4th element).<br>
	 * <br>
	 * <b>Example:</b><br>
	 * <code>intToByteArray(258)</code> will return { 0, 0, 1, 2 },<br>
	 * <code>BigInteger.valueOf(258).toByteArray()</code> returns { 1, 2 }. 
	 * @param integer The integer to be converted.
	 * @return The byte array of length 4.
	 */
	private byte[] intToByteArray (final int integer)
	{
		int byteNum = (40 - Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];
		
		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));
		
		return (byteArray);
	}
	
	// Parse the command-line arguments.
	private boolean parseArgs(String[] args) throws IllegalArgumentException
	{
		boolean retVal = false;
		
		
		return retVal;
	}
	
	/**
	 * Dump the contents of a Mobile 3D Graphics binary file.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args)
	{
		// The name of the M3G file.
		String filename = null;
		// The file handle.
		File file = null;
		// The handle to the M3G file.
		M3GFile m3gFile = null;
		// Be verbose.
		boolean isVerbose = false;
		
		// Parse the command-line arguments.
		if ((args == null) || (args.length <= 0) || (args.length >= 3))
		{
			System.err.println("DumpM3g: Unable to parse command-line arguments.");
			System.exit(-1);
		}
		
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if (arg.equals("-v"))
			{
				isVerbose = true;
				continue;
			} else
			{
				// The last argument should be the name of the M3G file.
				filename = arg;
			}			
		}

        // Check if file exists and can be read.	
		file = new File(filename);
		if ((! file.exists()) || (! file.canRead()))
		{
			System.err.println("DumpM3g: File " + filename + " can not be opened for reading.");
			System.exit(-1);
		}
		
		try
		{
			m3gFile = new M3GFile(file);
			if (file == null)
				throw new IOException("DumpM3g: File " + filename + " can not be opened for reading.");
		} catch (IOException ex)
		{
			System.err.println(ex.getMessage());
			System.exit(-1);
		}

		// Instantiate the control object.
		DumpM3g dumpM3g = new DumpM3g(m3gFile);
		dumpM3g.setVerbose(isVerbose);
		
		// Dump the M3G file contents.
		try
		{
			dumpM3g.dumpHeaderSection();
			dumpM3g.dumpExternalReferenceSection();
			dumpM3g.dumpSceneSections();
		} catch (IOException ex)
		{
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}

}
