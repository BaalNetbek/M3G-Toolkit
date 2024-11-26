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
import java.io.UnsupportedEncodingException;

class PNGChunk
{
	// The chunk's type.
    private byte[] m_type;
    // The chenk's data.
    private byte[] m_data;

    public PNGChunk(byte[] type, byte[] data)
    {
        m_type = type;
        m_data = data;
    }

    public String getTypeString()
    {
        try
        {
            return new String(m_type, "UTF8");
        } catch (UnsupportedEncodingException uee)
        {
            return "";
        }
    }

    public byte[] getData()
    {
        return m_data;
    }

    public long getUnsignedInt(int offset)
    {
        long value = 0;
        for (int i = 0; i < 4; i++)
            value += (m_data[offset + i] & 0xff) << ((3 - i) * 8);
        return value;
    }

    public short getUnsignedByte(int offset)
    {
        return (short) (m_data[offset] & 0x00ff);
    }
}
