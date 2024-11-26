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
package com.wizzer.m3g.toolkit.png;

// Import standard Java classes.
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class PNGDecoder
{
    public static BufferedImage decode(InputStream in) throws IOException
    {
	    DataInputStream dataIn = new DataInputStream(in);
	    return decode(dataIn);
    }

    public static BufferedImage decode(DataInputStream in) throws IOException
    {
	    readSignature(in);
	    PNGData chunks = readChunks(in);
	
	    long widthLong = chunks.getWidth();
	    long heightLong = chunks.getHeight();
	    if (widthLong > Integer.MAX_VALUE || heightLong > Integer.MAX_VALUE)
	      throw new IOException("That image is too wide or tall.");
	    int width = (int) widthLong;
	    int height = (int) heightLong;
	
	    ColorModel cm = chunks.getColorModel();
	    WritableRaster raster = chunks.getRaster();
	
	    BufferedImage image = new BufferedImage(cm, raster, false, null);
	
	    return image;
    }

    public static void readSignature(DataInputStream in) throws IOException
    {
	    long signature = in.readLong();
	    if (signature != 0x89504e470d0a1a0aL)
	        throw new IOException("PNG signature not found!");
    }

    public static PNGData readChunks(DataInputStream in) throws IOException
    {
	    PNGData chunks = new PNGData();
	
	    boolean working = true;
	    while (working)
	    {
	        try
	        {
		        // Read the length.
		        int length = in.readInt();
		        if (length < 0)
		            throw new IOException("Sorry, that file is too long.");
		        // Read the type.
		        byte[] typeBytes = new byte[4];
		        in.readFully(typeBytes);
		        // Read the data.
		        byte[] data = new byte[length];
		        in.readFully(data);
		        // Read the CRC.
		        long crc = in.readInt() & 0x00000000ffffffffL; // Make it
		        // unsigned.
		        if (verifyCRC(typeBytes, data, crc) == false)
		            throw new IOException("That file appears to be corrupted.");
		
		        PNGChunk chunk = new PNGChunk(typeBytes, data);
		        chunks.add(chunk);
	        } catch (EOFException eofe)
	        {
	            working = false;
	        }
	    }
	    return chunks;
    }

    protected static boolean verifyCRC(byte[] typeBytes, byte[] data, long crc)
    {
	    CRC32 crc32 = new CRC32();
	    crc32.update(typeBytes);
	    crc32.update(data);
	    long calculated = crc32.getValue();
	    return (calculated == crc);
    }
}
