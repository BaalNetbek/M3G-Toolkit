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
import java.util.ArrayList;
import java.util.zip.*;

// Import M3G Toolkit classes
import com.wizzer.m3g.math.*;
import com.wizzer.m3g.toolkit.util.ResourceRetriever;

public class Loader
{
	// The M3G file identifier.
	private static final byte[] IDENTIFIER = { (byte)0xAB, 0x4A, 0x53, 0x52, 0x31, 0x38, 0x34, (byte)0xBB, 0x0D, 0x0A, 0x1A, 0x0A };
	// The loader's input stream.
	private static DataInputStream m_in;
	// The list of referenced objects.
	private static ArrayList m_objects;
	
	public static Object3D[] load(String name)
	{
		m_objects = new ArrayList();
		try
		{
			m_in = new DataInputStream(ResourceRetriever.getResourceAsStream(name));
			
			byte[] identifier = new byte[12];
			int read = m_in.read(identifier, 0, 12);
			
			for (int i = 0; i < 12; ++i)
				if (identifier[i] != IDENTIFIER[i])
					throw new Exception("Invalid format");
			
			/*
			 * Byte                        CompressionScheme
			 * UInt32                      TotalSectionLength
			 * UInt32                      UncompressedLength
			 * Byte[TotalSectionLength-13] Objects
			 * UInt32                      Checksum
			 */
			
			while (m_in.available() > 0)
			{
				int compressionScheme =  readByte();
				int totalSectionLength = readInt();
				int uncompressedLength =  readInt();
				// System.out.println("compressionScheme: " + compressionScheme);
				// System.out.println("totalSectionLength: " + totalSectionLength);
				// System.out.println("uncompressedLength: " + uncompressedLength);
				
				byte[] uncompressedData = new byte[uncompressedLength];
				
				if (compressionScheme == 0)
				{
					m_in.readFully(uncompressedData);
				}
				else if (compressionScheme == 1)
				{
					int compressedLength = totalSectionLength - 13;
					byte[] compressedData = new byte[compressedLength];
					m_in.readFully(compressedData);
					
					Inflater decompresser = new Inflater();
					decompresser.setInput(compressedData, 0, compressedLength);
					int resultLength = decompresser.inflate(uncompressedData);
					decompresser.end();
					
					if (resultLength != uncompressedLength)
						throw new IOException("Unable to decompress data.");
				}
				else
					throw new IOException("Unknown compression scheme.");
				
				int checkSum = m_in.readInt();
				
				load(uncompressedData, 0);
			}
		} catch(Exception ex)
		{
			System.out.println("EXCEPTION!" + ex.getMessage());
		}
		
		// XXX - Only root nodes should be returned.
		Object3D[] obj = new Object3D[m_objects.size()];
		for (int i = 0; i < m_objects.size(); ++i)
			obj[i] = (Object3D)m_objects.get(i);
		return obj;
		
		//return (Object3D[]) objects.toArray(typeof(Object3D));
		//return new Object3D[] { (Object3D)objects.get(objects.size()-1) };
		//return null;
	}
	
	public static Object3D[] load(byte[] data, int offset)
	{
		DataInputStream old = m_in;
		m_in = new DataInputStream(new ByteArrayInputStream(data));
		
		try
		{
			while (m_in.available() > 0)
			{
				int objectType = readByte();
				int length = readInt();
				
				// System.out.println("objectType: " + objectType);
				// System.out.println("length: " + length);
				
				m_in.mark(Integer.MAX_VALUE);
				
				if (objectType == 0)
				{
					int versionHigh = readByte();
					int versionLow = readByte();
					boolean hasExternalReferences = readBoolean();
					int totalFileSize = readInt();
					int approximateContentSize = readInt();
					String authoringField = readString();
					
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 255)
				{
					// TODO: load external resource
					System.out.println("Loader: Loading external resources not implemented.");
					String uri = readString();
				}
				else if (objectType == 1)
				{
					System.out.println("Loader: AnimationController not implemented.");
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 2)
				{
					System.out.println("Loader: AnimationTrack not implemented.");
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 3)
				{
					// System.out.println("Appearance");
					
					Appearance appearance = new Appearance();
					loadObject3D(appearance);
					appearance.setLayer(readByte());
					appearance.setCompositingMode((CompositingMode)getObject(readInt()));
					appearance.setFog((Fog)getObject(readInt()));
					appearance.setPolygonMode((PolygonMode)getObject(readInt()));
					appearance.setMaterial((Material)getObject(readInt()));
					int numTextures = readInt();
					for (int i = 0; i < numTextures; ++i)
						appearance.setTexture(i, (Texture2D)getObject(readInt()));
					m_objects.add(appearance);
				}
				else if (objectType == 4)
				{
					// System.out.println("Background");
					
					Background background = new Background();
					loadObject3D(background);
					background.setColor(readRGBA());
					background.setImage((Image2D)getObject(readInt()));
					int modeX = readByte();
					int modeY = readByte();
					background.setImageMode(modeX, modeY);
					int cropX = readInt();
					int cropY = readInt();
					int cropWidth = readInt();
					int cropHeight = readInt();
					background.setCrop(cropX, cropY, cropWidth, cropHeight);
					background.setDepthClearEnable(readBoolean());
					background.setColorClearEnable(readBoolean());
					m_objects.add(background); // dummy
				}
				else if( objectType == 5)
				{
					// System.out.println("Camera");
					
					Camera camera = new Camera();
					loadNode(camera);
					
					int projectionType = readByte();
					if(projectionType == Camera.GENERIC)
					{
						Transform t = new Transform();
						t.set(readMatrix());
						camera.setGeneric(t);
					} else
					{
						float fovy = readFloat();
						float aspect = readFloat();
						float near = readFloat();
						float far = readFloat();
						if(projectionType == Camera.PARALLEL)
							camera.setParallel(fovy, aspect, near, far);
						else
							camera.setPerspective(fovy, aspect, near, far);
					}
					m_objects.add(camera);
				}
				else if (objectType == 6)
				{
					// System.out.println("CompositingMode");
					
					CompositingMode compositingMode = new CompositingMode();
					loadObject3D(compositingMode);
					compositingMode.setDepthTestEnable(readBoolean());
					compositingMode.setDepthWriteEnable(readBoolean());
					compositingMode.setColorWriteEnable(readBoolean());
					compositingMode.setAlphaWriteEnable(readBoolean());
					compositingMode.setBlending(readByte());
					compositingMode.setAlphaThreshold((float)readByte() / 255.0f);
					compositingMode.setDepthOffset(readFloat(), readFloat());
					m_objects.add(compositingMode);
				}
				else if (objectType == 7)
				{
					// System.out.println("Fog");
					
					Fog fog = new Fog();
					loadObject3D(fog);
					fog.setColor(readRGB());
					fog.setMode(readByte());
					if(fog.getMode() == Fog.EXPONENTIAL)
						fog.setDensity(readFloat());
					else
					{
						fog.setLinear(readFloat(), readFloat());
					}
					m_objects.add(fog);
				}
				else if (objectType == 9)
				{
					// System.out.println("Group");
					
					Group group = new Group();
					loadGroup(group);
					m_objects.add(group);
				}
				else if (objectType == 10)
				{
					// System.out.println("Image2D");
					
					Image2D image = null;
					loadObject3D(new Group()); // dummy
					int format = readByte();
					boolean isMutable = readBoolean();
					int width = readInt();
					int height = readInt();
					if (! isMutable)
					{
						// Read palette
						int paletteSize = readInt();
						byte[] palette = null;
						if (paletteSize > 0)
						{
							palette = new byte[paletteSize];
							m_in.readFully(palette);
						}
						// Read pixels
						int pixelSize = readInt();
						byte[] pixel = new byte[pixelSize];
						m_in.readFully(pixel);
						// Create image
						if (palette != null)
							image = new Image2D(format, width, height, pixel, palette);
						else
							image = new Image2D(format, width, height, pixel);
					}
					else
						image = new Image2D(format, width, height);
					
					m_in.reset();
					loadObject3D(image);
					
					m_objects.add(image);
				}
				else if (objectType == 19)
				{
					System.out.println("Loader: KeyframeSequence not implemented.");
					
					/*
					Byte          interpolation;
					Byte          repeatMode;
					Byte          encoding;
					UInt32        duration;
					UInt32        validRangeFirst;
					UInt32        validRangeLast;
					
					UInt32        componentCount;
					UInt32        keyframeCount;
					
					IF encoding == 0
					
					    FOR each key frame...
					
					        UInt32                  time;
					        Float32[componentCount] vectorValue;
					
					    END
					
					ELSE IF encoding == 1
					
					    Float32[componentCount] vectorBias;
					    Float32[componentCount] vectorScale;
					
					    FOR each key frame...
					
					        UInt32               time;
					        Byte[componentCount] vectorValue;
					
					    END
					
					ELSE IF encoding == 2
					
					    Float32[componentCount] vectorBias;
					    Float32[componentCount] vectorScale;
					
					    FOR each key frame...
					
					        UInt32                 time;
					        UInt16[componentCount] vectorValue;
					
					    END
					
					END
					*/
					
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 12)
				{
					// System.out.println("Light");
					Light light = new Light();
					loadNode(light);
					float constant = readFloat();
					float linear = readFloat();
					float quadratic = readFloat();
					light.setAttenuation(constant, linear, quadratic);
					light.setColor(readRGB());
					light.setMode(readByte());
					light.setIntensity(readFloat());
					light.setSpotAngle(readFloat());
					light.setSpotExponent(readFloat());
					m_objects.add(light);
				}
				else if (objectType == 13)
				{
					// System.out.println("Material");
					Material material = new Material();
					loadObject3D(material);
					material.setColor(Material.AMBIENT, readRGB());
					material.setColor(Material.DIFFUSE, readRGBA());
					material.setColor(Material.EMISSIVE, readRGB());
					material.setColor(Material.SPECULAR, readRGB());
					material.setShininess(readFloat());
					material.setVertexColorTrackingEnabled(readBoolean());
					m_objects.add(material);
				}
				else if (objectType == 14)
				{
					// System.out.println("Mesh");

					loadNode(new Group()); // dummy
					
					VertexBuffer vertices = (VertexBuffer)getObject(readInt());
					int submeshCount = readInt();
					
					IndexBuffer[] submeshes = new IndexBuffer[submeshCount];
					Appearance[] appearances = new Appearance[submeshCount];
					for (int i = 0; i < submeshCount; ++i)
					{
						submeshes[i] = (IndexBuffer)getObject(readInt());
						appearances[i] = (Appearance)getObject(readInt());
					}
					Mesh mesh = new Mesh(vertices, submeshes, appearances);
					
					m_in.reset();
					loadNode(mesh);
					
					m_objects.add(mesh);
				}
				else if (objectType == 15)
				{
					System.out.println("Loader: MorphingMesh not implemented.");
					
					/*
					UInt32        morphTargetCount;
					
					FOR each target buffer...
					
					    ObjectIndex   morphTarget;
					    Float32       initialWeight;
					
					END
					*/
					
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 8)
				{
					//System.out.println("PolygonMode");
					
					PolygonMode polygonMode = new PolygonMode();
					loadObject3D(polygonMode);
					polygonMode.setCulling(readByte());
					polygonMode.setShading(readByte());
					polygonMode.setWinding(readByte());
					polygonMode.setTwoSidedLightingEnable(readBoolean());
					polygonMode.setLocalCameraLightingEnable(readBoolean());
					polygonMode.setPerspectiveCorrectionEnable(readBoolean());
					m_objects.add(polygonMode);
				}
				else if (objectType == 16)
				{
					System.out.println("Loader: SkinnedMesh not implemented.");
					
					/*
					ObjectIndex   skeleton;
					
					UInt32        transformReferenceCount;
					
					FOR each bone reference...
					
					    ObjectIndex   transformNode;
					    UInt32        firstVertex;
					    UInt32        vertexCount;
					    Int32         weight;
					
					END
					*/
					
					m_objects.add(new Group()); // dummy
				}
				else if(objectType == 18)
				{
					System.out.println("Loader: Sprite not implemented.");
					
					/*
					ObjectIndex   image;
					ObjectIndex   appearance;
					
					Boolean       isScaled;
					
					Int32         cropX;
					Int32         cropY;
					Int32         cropWidth;
					Int32         cropHeight;
					*/
					
					m_objects.add(new Group()); // dummy
				}
				else if (objectType == 17)
				{
					// System.out.println("Texture2D");

					loadTransformable(new Group()); // dummy
					
					Texture2D texture = new Texture2D((Image2D)getObject(readInt()));
					texture.setBlendColor(readRGB());
					texture.setBlending(readByte());
					int wrapS = readByte();
					int wrapT = readByte();
					texture.setWrapping(wrapS, wrapT);
					int levelFilter = readByte();
					int imageFilter = readByte();
					texture.setFiltering(levelFilter, imageFilter);
					
					m_in.reset();
					loadTransformable(texture);
					
					m_objects.add(texture);
				}
				else if (objectType == 11)
				{
					// System.out.println("TriangleStripArray");
					
					loadObject3D(new Group()); // dummy
					
					int encoding = readByte();
					int firstIndex = 0;
					int[] indices = null;
					if (encoding == 0)
						firstIndex = readInt();
					else if( encoding == 1)
						firstIndex = readByte();
					else if( encoding == 2)
						firstIndex = readShort();
					else if (encoding == 128)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for (int i = 0; i < numIndices; ++i)
							indices[i] = readInt();
					}
					else if (encoding == 129)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for (int i = 0; i < numIndices; ++i)
							indices[i] = readByte();
					}
					else if (encoding == 130)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for (int i = 0; i < numIndices; ++i)
							indices[i] = readShort();
					}
					
					int numStripLengths = readInt();
					int[] stripLengths = new int[numStripLengths];
					for (int i = 0; i < numStripLengths; ++i)
						stripLengths[i] = readInt();
					
					m_in.reset();
					
					TriangleStripArray triStrip = null;
					if (indices == null)
						triStrip = new TriangleStripArray(firstIndex, stripLengths);
					else
						triStrip = new TriangleStripArray(indices, stripLengths);
					
					loadObject3D(triStrip);
					
					m_objects.add(triStrip);
				}
				else if (objectType == 20)
				{
					// System.out.println("VertexArray");
					
					loadObject3D(new Group()); // dummy
					
					int componentSize = readByte();
					int componentCount = readByte();
					int encoding = readByte();
					int vertexCount = readShort();
					
					VertexArray vertices = new VertexArray(vertexCount, componentCount, componentSize);
					
					if (componentSize == 1)
					{
						byte[] values = new byte[componentCount * vertexCount];
						if (encoding == 0)
							m_in.readFully(values);
						else
						{
							byte last = 0;
							for (int i = 0; i < vertexCount * componentCount; ++i)
							{								
								last += readByte();
								values[i] = last;
							}
						}
						vertices.set(0, vertexCount, values);
					}
					else
					{
						short last = 0;
						short[] values = new short[componentCount*vertexCount];
						for (int i = 0; i < componentCount*vertexCount; ++i)
						{
							if (encoding == 0)
								values[i] = (short)readShort();
							else
							{
								last += (short)readShort();
								values[i] = last;
							}
						}
						vertices.set(0, vertexCount, values);						
					}
					
					m_in.reset();
					loadObject3D(vertices);
					
					m_objects.add(vertices);
				}
				else if (objectType == 21)
				{
					// System.out.println("VertexBuffer");

					VertexBuffer vertices = new VertexBuffer();
					loadObject3D(vertices);
					
					vertices.setDefaultColor(readRGBA());
					
					VertexArray positions = (VertexArray)getObject(readInt());
					float[] bias = new float[3];
					bias[0] = readFloat();
					bias[1] = readFloat();
					bias[2] = readFloat();
					float scale = readFloat();
					vertices.setPositions(positions, scale, bias);
					
					vertices.setNormals((VertexArray)getObject(readInt()));
					vertices.setColors((VertexArray)getObject(readInt()));
					
					int texCoordArrayCount = readInt();
					for (int i = 0; i < texCoordArrayCount; ++i)
					{
						VertexArray texcoords = (VertexArray)getObject(readInt());
						bias[0] = readFloat();
						bias[1] = readFloat();
						bias[2] = readFloat();
						scale = readFloat();
						vertices.setTexCoords(i, texcoords, scale, bias);
					}
					
					m_objects.add(vertices);
				}
				else if (objectType == 22)
				{
					// System.out.println("World");

					World world = new World();					
					loadGroup(world);
					
					world.setActiveCamera((Camera)getObject(readInt()));
					world.setBackground((Background)getObject(readInt()));
					m_objects.add(world);
				}
				else
				{
					System.out.println("Loader: unsupported objectType " + objectType + ".");
				}
				
				m_in.reset();
				m_in.skipBytes(length);
			}
		} catch (Exception ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
		
		m_in = old;
		
		// XXX - must return an array of root nodes here.
		return null;
	}
	
	// Fundamental data types
	
	private static int readByte() throws IOException
	{
		return m_in.readUnsignedByte();
	}
	
	private static int readShort() throws IOException
	{
		int a = readByte();
		int b = readByte();
		return (b << 8) + a;
	}
	
	private static int readRGB() throws IOException
	{
		byte r = m_in.readByte();
		byte g = m_in.readByte();
		byte b = m_in.readByte();
		return (r << 16) + (g << 8) + b;
	}
	
	private static int readRGBA() throws IOException
	{
		byte r = m_in.readByte();
		byte g = m_in.readByte();
		byte b = m_in.readByte();
		byte a = m_in.readByte();
		return (a << 24) + (r << 16) + (g << 8) + b;
	}
	
	private static float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}
	
	private static int readInt() throws IOException
	{
		int a = m_in.readUnsignedByte();
		int b = m_in.readUnsignedByte();
		int c = m_in.readUnsignedByte();
		int d = m_in.readUnsignedByte();
		int i = (d << 24) | (c << 16) | (b << 8) | a; 
		return i;
	}
	
	private static boolean readBoolean() throws IOException
	{
		return readByte() == 1;
	}
	
	private static String readString() throws IOException
	{
		// TODO:
		return "";
	}
	
	// Compound data types
	
	private static float[] readMatrix() throws IOException
	{
		float[] m = new float[16];
		for (int i = 0; i < 16; ++i)
			m[i] = readFloat();
		return m;
	}
	
	// Other
	
	private static Object getObject(int index)
	{
		if (index == 0)
			return null;
		return m_objects.get(index - 1);
	}
	
	private static void loadObject3D(Object3D object) throws IOException
	{
		object.setUserID(readInt());
		
		int animationTracks = readInt();
		for (int i = 0; i < animationTracks; ++i)
			readInt();//object.addAnimationTrack((AnimationTrack)getObject(readInt()));
		
		int userParameterCount = readInt();
		for(int i = 0; i < userParameterCount; ++i)
		{
			int parameterID = readInt();
			int numBytes = readInt();
			byte[] parameterBytes = new byte[numBytes];
			m_in.readFully(parameterBytes);
		}
	}
	
	private static void loadTransformable(Transformable transformable) throws IOException
	{
		loadObject3D(transformable);
		if (readBoolean()) // hasComponentTransform
		{
			float tx = readFloat();
			float ty = readFloat();
			float tz = readFloat();
			transformable.setTranslation(tx, ty, tz);
			float sx = readFloat();
			float sy = readFloat();
			float sz = readFloat();
			transformable.setScale(sx, sy, sz);
			float angle = readFloat();
			float ax = readFloat();
			float ay = readFloat();
			float az = readFloat();
			transformable.setOrientation(angle, ax, ay, az);
		}
		if (readBoolean()) // hasGeneralTransform
		{
			Transform t = new Transform();
			t.set(readMatrix());
			transformable.setTransform(t);
		}
	}
	
	private static void loadNode(Node node) throws IOException
	{
		loadTransformable(node);
		node.setRenderingEnable(readBoolean());
		node.setPickingEnable(readBoolean());
		int alpha = readByte();
		node.setAlphaFactor( (float)alpha / 255.0f );
		node.setScope(readInt());
		if(readBoolean()) // hasAlignment
		{
			// TODO: set node alignment
			int zTarget = readByte();
			int yTarget = readByte();
			readInt(); // zReference
			readInt(); // yReference
		}
	}
	
	private static void loadGroup(Group group) throws IOException
	{
		loadNode(group);
		int count = readInt();
		for (int i = 0; i < count; ++i)
			group.addChild((Node)getObject(readInt()));
	}
}
