package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.util.ResourceRetriever;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class Loader {
   private static final byte[] IDENTIFIER = new byte[]{-85, 74, 83, 82, 49, 56, 52, -69, 13, 10, 26, 10};
   private static DataInputStream m_in;
   private static ArrayList m_objects;

   public static Object3D[] load(String name) {
      m_objects = new ArrayList();

      int i;
      try {
         m_in = new DataInputStream(ResourceRetriever.getResourceAsStream(name));
         byte[] identifier = new byte[12];
         i = m_in.read(identifier, 0, 12);

         int compressionScheme;
         for(compressionScheme = 0; compressionScheme < 12; ++compressionScheme) {
            if (identifier[compressionScheme] != IDENTIFIER[compressionScheme]) {
               throw new Exception("Invalid format");
            }
         }

         while(m_in.available() > 0) {
            compressionScheme = readByte();
            int totalSectionLength = readInt();
            int uncompressedLength = readInt();
            byte[] uncompressedData = new byte[uncompressedLength];
            int compressedLength;
            if (compressionScheme == 0) {
               m_in.readFully(uncompressedData);
            } else {
               if (compressionScheme != 1) {
                  throw new IOException("Unknown compression scheme.");
               }

               compressedLength = totalSectionLength - 13;
               byte[] compressedData = new byte[compressedLength];
               m_in.readFully(compressedData);
               Inflater decompresser = new Inflater();
               decompresser.setInput(compressedData, 0, compressedLength);
               int resultLength = decompresser.inflate(uncompressedData);
               decompresser.end();
               if (resultLength != uncompressedLength) {
                  throw new IOException("Unable to decompress data.");
               }
            }

            compressedLength = m_in.readInt();
            load(uncompressedData, 0);
         }
      } catch (Exception var11) {
         System.out.println("EXCEPTION!" + var11.getMessage());
      }

      Object3D[] obj = new Object3D[m_objects.size()];

      for(i = 0; i < m_objects.size(); ++i) {
         obj[i] = (Object3D)m_objects.get(i);
      }

      return obj;
   }

   public static Object3D[] load(byte[] data, int offset) {
      DataInputStream old = m_in;
      m_in = new DataInputStream(new ByteArrayInputStream(data));

      try {
         while(m_in.available() > 0) {
            int objectType = readByte();
            int length = readInt();
            m_in.mark(Integer.MAX_VALUE);
            int encoding;
            int firstIndex;
            boolean isMutable;
            int numIndices;
            int i;
            String triStrip;
            if (objectType == 0) {
               encoding = readByte();
               firstIndex = readByte();
               isMutable = readBoolean();
               numIndices = readInt();
               i = readInt();
               triStrip = readString();
               m_objects.add(new Group());
            } else {
               String image;
               if (objectType == 255) {
                  System.out.println("Loader: Loading external resources not implemented.");
                  image = readString();
               } else if (objectType == 1) {
                  System.out.println("Loader: AnimationController not implemented.");
                  m_objects.add(new Group());
               } else if (objectType == 2) {
                  System.out.println("Loader: AnimationTrack not implemented.");
                  m_objects.add(new Group());
               } else {
                  int encoding;
                  if (objectType == 3) {
                     Appearance appearance = new Appearance();
                     loadObject3D(appearance);
                     appearance.setLayer(readByte());
                     appearance.setCompositingMode((CompositingMode)getObject(readInt()));
                     appearance.setFog((Fog)getObject(readInt()));
                     appearance.setPolygonMode((PolygonMode)getObject(readInt()));
                     appearance.setMaterial((Material)getObject(readInt()));
                     firstIndex = readInt();

                     for(encoding = 0; encoding < firstIndex; ++encoding) {
                        appearance.setTexture(encoding, (Texture2D)getObject(readInt()));
                     }

                     m_objects.add(appearance);
                  } else {
                     int i;
                     if (objectType == 4) {
                        Background background = new Background();
                        loadObject3D(background);
                        background.setColor(readRGBA());
                        background.setImage((Image2D)getObject(readInt()));
                        firstIndex = readByte();
                        encoding = readByte();
                        background.setImageMode(firstIndex, encoding);
                        numIndices = readInt();
                        i = readInt();
                        i = readInt();
                        int cropHeight = readInt();
                        background.setCrop(numIndices, i, i, cropHeight);
                        background.setDepthClearEnable(readBoolean());
                        background.setColorClearEnable(readBoolean());
                        m_objects.add(background);
                     } else {
                        float linear;
                        float scale;
                        if (objectType == 5) {
                           Camera camera = new Camera();
                           loadNode(camera);
                           firstIndex = readByte();
                           if (firstIndex == 48) {
                              Transform t = new Transform();
                              t.set(readMatrix());
                              camera.setGeneric(t);
                           } else {
                              linear = readFloat();
                              scale = readFloat();
                              float near = readFloat();
                              float far = readFloat();
                              if (firstIndex == 49) {
                                 camera.setParallel(linear, scale, near, far);
                              } else {
                                 camera.setPerspective(linear, scale, near, far);
                              }
                           }

                           m_objects.add(camera);
                        } else if (objectType == 6) {
                           CompositingMode compositingMode = new CompositingMode();
                           loadObject3D(compositingMode);
                           compositingMode.setDepthTestEnable(readBoolean());
                           compositingMode.setDepthWriteEnable(readBoolean());
                           compositingMode.setColorWriteEnable(readBoolean());
                           compositingMode.setAlphaWriteEnable(readBoolean());
                           compositingMode.setBlending(readByte());
                           compositingMode.setAlphaThreshold((float)readByte() / 255.0F);
                           compositingMode.setDepthOffset(readFloat(), readFloat());
                           m_objects.add(compositingMode);
                        } else if (objectType == 7) {
                           Fog fog = new Fog();
                           loadObject3D(fog);
                           fog.setColor(readRGB());
                           fog.setMode(readByte());
                           if (fog.getMode() == 80) {
                              fog.setDensity(readFloat());
                           } else {
                              fog.setLinear(readFloat(), readFloat());
                           }

                           m_objects.add(fog);
                        } else if (objectType == 9) {
                           Group group = new Group();
                           loadGroup(group);
                           m_objects.add(group);
                        } else {
                           int i;
                           if (objectType == 10) {
                              image = null;
                              loadObject3D(new Group());
                              firstIndex = readByte();
                              isMutable = readBoolean();
                              numIndices = readInt();
                              i = readInt();
                              Image2D image;
                              if (!isMutable) {
                                 i = readInt();
                                 byte[] palette = (byte[])null;
                                 if (i > 0) {
                                    palette = new byte[i];
                                    m_in.readFully(palette);
                                 }

                                 i = readInt();
                                 byte[] pixel = new byte[i];
                                 m_in.readFully(pixel);
                                 if (palette != null) {
                                    image = new Image2D(firstIndex, numIndices, i, pixel, palette);
                                 } else {
                                    image = new Image2D(firstIndex, numIndices, i, pixel);
                                 }
                              } else {
                                 image = new Image2D(firstIndex, numIndices, i);
                              }

                              m_in.reset();
                              loadObject3D(image);
                              m_objects.add(image);
                           } else if (objectType == 19) {
                              System.out.println("Loader: KeyframeSequence not implemented.");
                              m_objects.add(new Group());
                           } else if (objectType == 12) {
                              Light light = new Light();
                              loadNode(light);
                              float constant = readFloat();
                              linear = readFloat();
                              scale = readFloat();
                              light.setAttenuation(constant, linear, scale);
                              light.setColor(readRGB());
                              light.setMode(readByte());
                              light.setIntensity(readFloat());
                              light.setSpotAngle(readFloat());
                              light.setSpotExponent(readFloat());
                              m_objects.add(light);
                           } else if (objectType == 13) {
                              Material material = new Material();
                              loadObject3D(material);
                              material.setColor(1024, readRGB());
                              material.setColor(2048, readRGBA());
                              material.setColor(4096, readRGB());
                              material.setColor(8192, readRGB());
                              material.setShininess(readFloat());
                              material.setVertexColorTrackingEnabled(readBoolean());
                              m_objects.add(material);
                           } else {
                              VertexBuffer vertices;
                              if (objectType == 14) {
                                 loadNode(new Group());
                                 vertices = (VertexBuffer)getObject(readInt());
                                 firstIndex = readInt();
                                 IndexBuffer[] submeshes = new IndexBuffer[firstIndex];
                                 Appearance[] appearances = new Appearance[firstIndex];

                                 for(i = 0; i < firstIndex; ++i) {
                                    submeshes[i] = (IndexBuffer)getObject(readInt());
                                    appearances[i] = (Appearance)getObject(readInt());
                                 }

                                 Mesh mesh = new Mesh(vertices, submeshes, appearances);
                                 m_in.reset();
                                 loadNode(mesh);
                                 m_objects.add(mesh);
                              } else if (objectType == 15) {
                                 System.out.println("Loader: MorphingMesh not implemented.");
                                 m_objects.add(new Group());
                              } else if (objectType == 8) {
                                 PolygonMode polygonMode = new PolygonMode();
                                 loadObject3D(polygonMode);
                                 polygonMode.setCulling(readByte());
                                 polygonMode.setShading(readByte());
                                 polygonMode.setWinding(readByte());
                                 polygonMode.setTwoSidedLightingEnable(readBoolean());
                                 polygonMode.setLocalCameraLightingEnable(readBoolean());
                                 polygonMode.setPerspectiveCorrectionEnable(readBoolean());
                                 m_objects.add(polygonMode);
                              } else if (objectType == 16) {
                                 System.out.println("Loader: SkinnedMesh not implemented.");
                                 m_objects.add(new Group());
                              } else if (objectType == 18) {
                                 System.out.println("Loader: Sprite not implemented.");
                                 m_objects.add(new Group());
                              } else if (objectType == 17) {
                                 loadTransformable(new Group());
                                 Texture2D texture = new Texture2D((Image2D)getObject(readInt()));
                                 texture.setBlendColor(readRGB());
                                 texture.setBlending(readByte());
                                 firstIndex = readByte();
                                 encoding = readByte();
                                 texture.setWrapping(firstIndex, encoding);
                                 numIndices = readByte();
                                 i = readByte();
                                 texture.setFiltering(numIndices, i);
                                 m_in.reset();
                                 loadTransformable(texture);
                                 m_objects.add(texture);
                              } else if (objectType == 11) {
                                 loadObject3D(new Group());
                                 encoding = readByte();
                                 firstIndex = 0;
                                 int[] indices = (int[])null;
                                 if (encoding == 0) {
                                    firstIndex = readInt();
                                 } else if (encoding == 1) {
                                    firstIndex = readByte();
                                 } else if (encoding == 2) {
                                    firstIndex = readShort();
                                 } else if (encoding == 128) {
                                    numIndices = readInt();
                                    indices = new int[numIndices];

                                    for(i = 0; i < numIndices; ++i) {
                                       indices[i] = readInt();
                                    }
                                 } else if (encoding == 129) {
                                    numIndices = readInt();
                                    indices = new int[numIndices];

                                    for(i = 0; i < numIndices; ++i) {
                                       indices[i] = readByte();
                                    }
                                 } else if (encoding == 130) {
                                    numIndices = readInt();
                                    indices = new int[numIndices];

                                    for(i = 0; i < numIndices; ++i) {
                                       indices[i] = readShort();
                                    }
                                 }

                                 numIndices = readInt();
                                 int[] stripLengths = new int[numIndices];

                                 for(i = 0; i < numIndices; ++i) {
                                    stripLengths[i] = readInt();
                                 }

                                 m_in.reset();
                                 triStrip = null;
                                 TriangleStripArray triStrip;
                                 if (indices == null) {
                                    triStrip = new TriangleStripArray(firstIndex, stripLengths);
                                 } else {
                                    triStrip = new TriangleStripArray(indices, stripLengths);
                                 }

                                 loadObject3D(triStrip);
                                 m_objects.add(triStrip);
                              } else if (objectType == 20) {
                                 loadObject3D(new Group());
                                 encoding = readByte();
                                 firstIndex = readByte();
                                 encoding = readByte();
                                 numIndices = readShort();
                                 VertexArray vertices = new VertexArray(numIndices, firstIndex, encoding);
                                 if (encoding == 1) {
                                    byte[] values = new byte[firstIndex * numIndices];
                                    if (encoding == 0) {
                                       m_in.readFully(values);
                                    } else {
                                       byte last = 0;

                                       for(i = 0; i < numIndices * firstIndex; ++i) {
                                          last = (byte)(last + readByte());
                                          values[i] = last;
                                       }
                                    }

                                    vertices.set(0, numIndices, (byte[])values);
                                 } else {
                                    short last = 0;
                                    short[] values = new short[firstIndex * numIndices];

                                    for(i = 0; i < firstIndex * numIndices; ++i) {
                                       if (encoding == 0) {
                                          values[i] = (short)readShort();
                                       } else {
                                          last += (short)readShort();
                                          values[i] = last;
                                       }
                                    }

                                    vertices.set(0, numIndices, (short[])values);
                                 }

                                 m_in.reset();
                                 loadObject3D(vertices);
                                 m_objects.add(vertices);
                              } else if (objectType == 21) {
                                 vertices = new VertexBuffer();
                                 loadObject3D(vertices);
                                 vertices.setDefaultColor(readRGBA());
                                 VertexArray positions = (VertexArray)getObject(readInt());
                                 float[] bias = new float[]{readFloat(), readFloat(), readFloat()};
                                 scale = readFloat();
                                 vertices.setPositions(positions, scale, bias);
                                 vertices.setNormals((VertexArray)getObject(readInt()));
                                 vertices.setColors((VertexArray)getObject(readInt()));
                                 i = readInt();

                                 for(i = 0; i < i; ++i) {
                                    VertexArray texcoords = (VertexArray)getObject(readInt());
                                    bias[0] = readFloat();
                                    bias[1] = readFloat();
                                    bias[2] = readFloat();
                                    scale = readFloat();
                                    vertices.setTexCoords(i, texcoords, scale, bias);
                                 }

                                 m_objects.add(vertices);
                              } else if (objectType == 22) {
                                 World world = new World();
                                 loadGroup(world);
                                 world.setActiveCamera((Camera)getObject(readInt()));
                                 world.setBackground((Background)getObject(readInt()));
                                 m_objects.add(world);
                              } else {
                                 System.out.println("Loader: unsupported objectType " + objectType + ".");
                              }
                           }
                        }
                     }
                  }
               }
            }

            m_in.reset();
            m_in.skipBytes(length);
         }
      } catch (Exception var14) {
         System.out.println("Exception: " + var14.getMessage());
      }

      m_in = old;
      return null;
   }

   private static int readByte() throws IOException {
      return m_in.readUnsignedByte();
   }

   private static int readShort() throws IOException {
      int a = readByte();
      int b = readByte();
      return (b << 8) + a;
   }

   private static int readRGB() throws IOException {
      byte r = m_in.readByte();
      byte g = m_in.readByte();
      byte b = m_in.readByte();
      return (r << 16) + (g << 8) + b;
   }

   private static int readRGBA() throws IOException {
      byte r = m_in.readByte();
      byte g = m_in.readByte();
      byte b = m_in.readByte();
      byte a = m_in.readByte();
      return (a << 24) + (r << 16) + (g << 8) + b;
   }

   private static float readFloat() throws IOException {
      return Float.intBitsToFloat(readInt());
   }

   private static int readInt() throws IOException {
      int a = m_in.readUnsignedByte();
      int b = m_in.readUnsignedByte();
      int c = m_in.readUnsignedByte();
      int d = m_in.readUnsignedByte();
      int i = d << 24 | c << 16 | b << 8 | a;
      return i;
   }

   private static boolean readBoolean() throws IOException {
      return readByte() == 1;
   }

   private static String readString() throws IOException {
      return "";
   }

   private static float[] readMatrix() throws IOException {
      float[] m = new float[16];

      for(int i = 0; i < 16; ++i) {
         m[i] = readFloat();
      }

      return m;
   }

   private static Object getObject(int index) {
      return index == 0 ? null : m_objects.get(index - 1);
   }

   private static void loadObject3D(Object3D object) throws IOException {
      object.setUserID(readInt());
      int animationTracks = readInt();

      int userParameterCount;
      for(userParameterCount = 0; userParameterCount < animationTracks; ++userParameterCount) {
         readInt();
      }

      userParameterCount = readInt();

      for(int i = 0; i < userParameterCount; ++i) {
         int parameterID = readInt();
         int numBytes = readInt();
         byte[] parameterBytes = new byte[numBytes];
         m_in.readFully(parameterBytes);
      }

   }

   private static void loadTransformable(Transformable transformable) throws IOException {
      loadObject3D(transformable);
      if (readBoolean()) {
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

      if (readBoolean()) {
         Transform t = new Transform();
         t.set(readMatrix());
         transformable.setTransform(t);
      }

   }

   private static void loadNode(Node node) throws IOException {
      loadTransformable(node);
      node.setRenderingEnable(readBoolean());
      node.setPickingEnable(readBoolean());
      int alpha = readByte();
      node.setAlphaFactor((float)alpha / 255.0F);
      node.setScope(readInt());
      if (readBoolean()) {
         int zTarget = readByte();
         int yTarget = readByte();
         readInt();
         readInt();
      }

   }

   private static void loadGroup(Group group) throws IOException {
      loadNode(group);
      int count = readInt();

      for(int i = 0; i < count; ++i) {
         group.addChild((Node)getObject(readInt()));
      }

   }
}
