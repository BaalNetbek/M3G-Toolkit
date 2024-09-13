package com.wizzer.m3g;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class M3GFile {
   public static final byte[] FILE_IDENTIFIER = new byte[]{-85, 74, 83, 82, 49, 56, 52, -69, 13, 10, 26, 10};
   private HeaderSection m_headerSection = new HeaderSection();
   private ExternalReferencesSection m_externalReferencesSection = new ExternalReferencesSection();
   private ArrayList m_sceneSections = new ArrayList();
   private String m_cwd = null;

   public M3GFile() {
      this.m_cwd = System.getProperty("user.dir");
   }

   public M3GFile(File file) throws IOException {
      ArrayList table = new ArrayList();
      table.add(Boolean.FALSE);
      this.m_cwd = file.getAbsolutePath();
      this.m_cwd = this.m_cwd.substring(0, this.m_cwd.lastIndexOf(System.getProperty("file.separator")));
      ExternalReference.setCwd(this.m_cwd);
      M3GInputStream is = new M3GInputStream(new FileInputStream(file));
      byte[] id = new byte[FILE_IDENTIFIER.length];
      is.read(id);
      if (!Arrays.equals(id, FILE_IDENTIFIER)) {
         throw new IOException("M3GFile: FILE_IDENTIFIER");
      } else {
         this.m_headerSection.unmarshall(is, table);
         if (this.m_headerSection.getHeaderObject().isHasExternalReferences()) {
            this.m_externalReferencesSection.unmarshall(is, table);
         }

         while(is.available() > 0) {
            SceneSection scene = new SceneSection();
            scene.unmarshall(is, table);
            this.m_sceneSections.add(scene);
         }

         is.close();
      }
   }

   public HeaderSection getHeaderSection() {
      return this.m_headerSection;
   }

   public ExternalReferencesSection getExternalReferencesSection() {
      return this.m_externalReferencesSection;
   }

   public void addSceneSection(SceneSection scene) {
      this.m_sceneSections.add(scene);
   }

   public SceneSection[] getSceneSections() {
      return (SceneSection[])this.m_sceneSections.toArray(new SceneSection[this.m_sceneSections.size()]);
   }

   public void removeSceneSection(SceneSection scene) {
      this.m_sceneSections.remove(scene);
   }

   public void removeSceneSections() {
      this.m_sceneSections.clear();
   }

   void setCwd(String cwd) {
      this.m_cwd = cwd;
   }

   public String getCwd() {
      return this.m_cwd;
   }

   public void marshall(OutputStream os) throws IOException {
      ArrayList table = this.buildReferenceTable();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      M3GOutputStream out = new M3GOutputStream(baos);
      if (this.m_externalReferencesSection.getExternalReferenceCount() > 0) {
         this.m_externalReferencesSection.marshall(out, table);
      }

      for(int i = 0; i < this.m_sceneSections.size(); ++i) {
         SceneSection ss = (SceneSection)this.m_sceneSections.get(0);
         ss.marshall(out, table);
      }

      this.m_headerSection.getHeaderObject().setHasExternalReferences(this.m_externalReferencesSection.getExternalReferenceCount() > 0);
      this.m_headerSection.getHeaderObject().setAuthoringField("M3GToolkit (www.wizzerworks.com)");
      byte[] header = this.getSectionBytes(this.m_headerSection, table);
      int size = FILE_IDENTIFIER.length + header.length + baos.size();
      this.m_headerSection.getHeaderObject().setTotalFileSize((long)size);
      this.m_headerSection.getHeaderObject().setApproximateContentSize((long)size);
      os.write(FILE_IDENTIFIER);
      os.write(this.getSectionBytes(this.m_headerSection, table));
      baos.writeTo(os);
      os.close();
   }

   public void marshall(File file) throws IOException {
      this.marshall((OutputStream)(new FileOutputStream(file)));
   }

   private byte[] getSectionBytes(Section section, ArrayList table) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      section.marshall(new M3GOutputStream(baos), table);
      return baos.toByteArray();
   }

   private ArrayList buildReferenceTable() {
      ArrayList table = new ArrayList();
      table.add(Boolean.FALSE);
      table.add(this.m_headerSection.getHeaderObject());

      for(int i = 0; i < this.m_sceneSections.size(); ++i) {
         SceneSection ss = (SceneSection)this.m_sceneSections.get(i);
         Object3D[] objects = ss.getObjects3D();

         for(int j = 0; j < objects.length; ++j) {
            objects[j].buildReferenceTable(table);
         }
      }

      return table;
   }
}
