package com.wizzer.m3g;

import java.io.IOException;
import java.util.ArrayList;

public class HeaderObject extends M3GObject {
   private byte m_versionMajor = 1;
   private byte m_versionMinor = 0;
   private boolean m_hasExternalReferences;
   private long m_totalFileSize;
   private long m_approximateContentSize;
   private String m_authoringField = "";

   public byte getVersionMajor() {
      return this.m_versionMajor;
   }

   public void setVersionMajor(byte versionMajor) {
      this.m_versionMajor = versionMajor;
   }

   public byte getVersionMinor() {
      return this.m_versionMinor;
   }

   public void setVersionMinor(byte versionMinor) {
      this.m_versionMinor = versionMinor;
   }

   public boolean isHasExternalReferences() {
      return this.m_hasExternalReferences;
   }

   public void setHasExternalReferences(boolean hasExternalReferences) {
      this.m_hasExternalReferences = hasExternalReferences;
   }

   public long getTotalFileSize() {
      return this.m_totalFileSize;
   }

   public void setTotalFileSize(long totalFileSize) {
      this.m_totalFileSize = totalFileSize;
   }

   public long getApproximateContentSize() {
      return this.m_approximateContentSize;
   }

   public void setApproximateContentSize(long approximateContentSize) {
      this.m_approximateContentSize = approximateContentSize;
   }

   public String getAuthoringField() {
      return this.m_authoringField;
   }

   public void setAuthoringField(String authoringField) {
      this.m_authoringField = authoringField;
   }

   public int getObjectType() {
      return 0;
   }

   protected void unmarshall(M3GInputStream is, ArrayList table) throws IOException {
      this.m_versionMajor = (byte)is.readByte();
      this.m_versionMinor = (byte)is.readByte();
      this.m_hasExternalReferences = is.readBoolean();
      this.m_totalFileSize = is.readUInt32();
      this.m_approximateContentSize = is.readUInt32();
      this.m_authoringField = is.readString();
   }

   protected void marshall(M3GOutputStream os, ArrayList table) throws IOException {
      os.writeByte(this.m_versionMajor);
      os.writeByte(this.m_versionMinor);
      os.writeBoolean(this.m_hasExternalReferences);
      os.writeUInt32(this.m_totalFileSize);
      os.writeUInt32(this.m_approximateContentSize);
      os.writeString(this.m_authoringField);
   }
}
