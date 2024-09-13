package com.wizzer.m3g;

import com.wizzer.m3g.toolkit.png.PNGDecoder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class ExternalReference extends M3GObject {
   private String m_uri;
   private M3GObject m_reference;
   private static File g_cwd = null;

   ExternalReference() {
   }

   public ExternalReference(String uri) {
      this.m_uri = uri;
   }

   public static File getCwd() {
      return g_cwd;
   }

   public static void setCwd(File dir) throws IOException {
      if (dir.isDirectory() && dir.canRead()) {
         g_cwd = dir;
      } else {
         throw new IOException("ExternalReference: invalid directory");
      }
   }

   public static void setCwd(String dirname) throws IOException {
      File dir = new File(dirname);
      if (dir.isDirectory() && dir.canRead()) {
         g_cwd = dir;
      } else {
         throw new IOException("ExternalReference: invalid directory");
      }
   }

   public String getURI() {
      return this.m_uri;
   }

   public void setURI(String uri) {
      this.m_uri = uri;
   }

   public int getObjectType() {
      return 255;
   }

   public M3GObject getReference() {
      return this.m_reference;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      this.m_uri = is.readString();

      try {
         URI uri = new URI(this.m_uri);
         if (uri.getScheme() == null && getCwd() != null) {
            URI path = getCwd().toURI();
            uri = path.resolve(uri);
         }

         this.m_reference = this.resolvePNG(uri.normalize());
         if (this.m_reference == null) {
            throw new IOException("ExternalReference: external M3G file not implemented");
         }
      } catch (URISyntaxException var5) {
         throw new IOException(var5.getMessage());
      }
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      os.writeString(this.m_uri);
   }

   private Image2D resolvePNG(URI uri) {
      Image2D image2D = null;
      if (uri.getScheme() != null) {
         try {
            URL url = new URL(uri.toString());
            InputStream is = url.openStream();
            BufferedImage image = PNGDecoder.decode(is);
            int imageType = 99;
            if (image.getType() == 1) {
               imageType = 99;
            } else if (image.getType() == 2) {
               imageType = 100;
            }

            image2D = new Image2D(imageType, image);
         } catch (IOException var10) {
         }
      } else {
         String path = uri.getPath();
         File file = new File(path);
         if (file.getAbsoluteFile().exists()) {
            try {
               FileInputStream is = new FileInputStream(file);
               BufferedImage image = PNGDecoder.decode((InputStream)is);
               int imageType = 99;
               if (image.getType() == 1) {
                  imageType = 99;
               } else if (image.getType() == 2) {
                  imageType = 100;
               }

               image2D = new Image2D(imageType, image);
            } catch (FileNotFoundException var8) {
            } catch (IOException var9) {
            }
         }
      }

      return image2D;
   }
}
