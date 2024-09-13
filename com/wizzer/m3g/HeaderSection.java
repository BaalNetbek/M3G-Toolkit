package com.wizzer.m3g;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class HeaderSection extends Section {
   private HeaderObject m_headerObject = new HeaderObject();

   public HeaderSection() {
      this.setCompressionScheme(0);
   }

   public HeaderObject getHeaderObject() {
      return this.m_headerObject;
   }

   protected void readObjects(M3GInputStream is, ArrayList table) throws IOException {
      byte type = (byte)is.readByte();
      if (type != 0) {
         throw new IOException("HeaderSection:type = " + type);
      } else {
         long length = is.readUInt32();
         this.m_headerObject.unmarshall(is, table);
         table.add(this.m_headerObject);
      }
   }

   protected void writeObjects(M3GOutputStream os, ArrayList table) throws IOException {
      os.writeByte((int)0);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      this.m_headerObject.marshall(new M3GOutputStream(baos), table);
      os.writeUInt32((long)baos.size());
      baos.writeTo(os);
   }
}
