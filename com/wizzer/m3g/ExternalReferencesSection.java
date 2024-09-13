package com.wizzer.m3g;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExternalReferencesSection extends Section {
   private ArrayList<ExternalReference> m_externalReferences = new ArrayList();

   public void addExternalReference(ExternalReference reference) {
      this.m_externalReferences.add(reference);
   }

   public ExternalReference[] getExternalReferences() {
      return (ExternalReference[])this.m_externalReferences.toArray(new ExternalReference[this.m_externalReferences.size()]);
   }

   public void removeExternalReference(ExternalReference reference) {
      this.m_externalReferences.remove(reference);
   }

   public void removeExternalReferences() {
      this.m_externalReferences.clear();
   }

   public int getExternalReferenceCount() {
      return this.m_externalReferences.size();
   }

   protected void readObjects(M3GInputStream is, ArrayList table) throws IOException {
      while(is.available() > 0) {
         short type = (short)is.readByte();
         if (type != 255) {
            throw new IOException("ExternalReferencesSection:type = " + type);
         }

         long length = is.readUInt32();
         ExternalReference reference = new ExternalReference();
         reference.unmarshall(is, table);
         this.m_externalReferences.add(reference);
         table.add(reference);
      }

   }

   protected void writeObjects(M3GOutputStream os, ArrayList table) throws IOException {
      ExternalReference[] references = this.getExternalReferences();

      for(int i = 0; i < references.length; ++i) {
         os.writeByte((int)255);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         references[i].marshall(new M3GOutputStream(baos), table);
         os.writeUInt32((long)baos.size());
         baos.writeTo(os);
      }

   }
}
