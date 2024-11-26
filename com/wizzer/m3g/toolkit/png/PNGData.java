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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

class PNGData
{
	// The number of chunks in the file.
    private int m_numberOfChunks;
    // The actual chunk data.
    private PNGChunk[] m_chunks;

    public PNGData()
    {
	    m_numberOfChunks = 0;
	    m_chunks = new PNGChunk[10];
    }

    public void add(PNGChunk chunk)
    {
	    m_chunks[m_numberOfChunks++] = chunk;
	    if (m_numberOfChunks >= m_chunks.length)
	    {
	        PNGChunk[] largerArray = new PNGChunk[m_chunks.length + 10];
	        System.arraycopy(m_chunks, 0, largerArray, 0, m_chunks.length);
	        m_chunks = largerArray;
	    }
    }

    public long getWidth()
    {
        return getChunk("IHDR").getUnsignedInt(0);
    }

    public long getHeight()
    {
	    return getChunk("IHDR").getUnsignedInt(4);
    }

    public short getBitsPerPixel()
    {
	    return getChunk("IHDR").getUnsignedByte(8);
    }

    public short getColorType()
    {
	    return getChunk("IHDR").getUnsignedByte(9);
    }

    public short getCompression()
    {
	    return getChunk("IHDR").getUnsignedByte(10);
    }

    public short getFilter()
    {
	    return getChunk("IHDR").getUnsignedByte(11);
    }

    public short getInterlace()
    {
        return getChunk("IHDR").getUnsignedByte(12);
    }

    public ColorModel getColorModel()
    {
	    short colorType = getColorType();
	    int bitsPerPixel = getBitsPerPixel();
	
	    if (colorType == 3)
	    {
	        byte[] paletteData = getChunk("PLTE").getData();
	        int paletteLength = paletteData.length / 3;
	        return new IndexColorModel(bitsPerPixel, paletteLength,
	            paletteData, 0, false);
	    }
	    System.out.println("Unsupported color type: " + colorType);
	    return null;
    }

    public WritableRaster getRaster()
    {
	    int width = (int) getWidth();
	    int height = (int) getHeight();
	    int bitsPerPixel = getBitsPerPixel();
	    short colorType = getColorType();
	
	    if (colorType == 3)
	    {
	        byte[] imageData = getImageData();
	        DataBuffer db = new DataBufferByte(imageData, imageData.length);
	        WritableRaster raster = Raster.createPackedRaster(db, width,
	            height, bitsPerPixel, null);
	        return raster;
	    } else
	        System.out.println("Unsupported color type!");
	    return null;
    }

    public byte[] getImageData()
    {
	    try
	    {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        // Write all the IDAT data into the array.
	        for (int i = 0; i < m_numberOfChunks; i++)
	        {
	            PNGChunk chunk = m_chunks[i];
	            if (chunk.getTypeString().equals("IDAT"))
	            {
	                out.write(chunk.getData());
	            }
	        }
	        out.flush();
	        // Now deflate the data.
	        InflaterInputStream in = new InflaterInputStream(
	            new ByteArrayInputStream(out.toByteArray()));
	        ByteArrayOutputStream inflatedOut = new ByteArrayOutputStream();
	        int readLength;
	        byte[] block = new byte[8192];
	        while ((readLength = in.read(block)) != -1)
	            inflatedOut.write(block, 0, readLength);
	        inflatedOut.flush();
	        byte[] imageData = inflatedOut.toByteArray();
	        // Compute the real length.
	        int width = (int) getWidth();
	        int height = (int) getHeight();
	        int bitsPerPixel = getBitsPerPixel();
	        int length = width * height * bitsPerPixel / 8;
	
	        byte[] prunedData = new byte[length];
	
	        // We can only deal with non-interlaced images.
	        if (getInterlace() == 0)
	        {
	            int index = 0;
	            for (int i = 0; i < length; i++)
	            {
	                if ((i * 8 / bitsPerPixel) % width == 0)
	                {
	                    index++; // Skip the filter byte.
	                }
	                prunedData[i] = imageData[index++];
	            }
	        } else
	            System.out.println("Couldn't undo interlacing.");
	
	        return prunedData;
	    } catch (IOException ioe)
	    {
	    }
	    return null;
    }

    public PNGChunk getChunk(String type)
    {
        for (int i = 0; i < m_numberOfChunks; i++)
            if (m_chunks[i].getTypeString().equals(type))
                return m_chunks[i];
        return null;
    }
}
