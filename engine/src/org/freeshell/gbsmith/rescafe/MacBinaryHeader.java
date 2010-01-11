package org.freeshell.gbsmith.rescafe;

import info.svitkine.alexei.wage.MacRoman;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

/*=======================================================================*/
/* The MIT License

Copyright (c) 1999-2009 by G. Brannon Smith

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
/*=======================================================================*/


/*=======================================================================*/
/*
  Used the original
  "Macintosh Binary Transfer Format ('MacBinary') Standard Proposal"
  by Dennis F. Brothers - 13 March 1985
  Revision 3 - 6 May 1985
  to figure this out. There must be some more recent specs that would
  make this better. I also fudged a bit in places where my tests didn't
  seem right. Seems to work - at least on the samples I used:
     Finder.bin, System.bin, ResEdit 2.1.1.bin
*/
@SuppressWarnings("unused")
public class MacBinaryHeader
{
   /*--- Constants ------------------------------------------------------*/
   /* Info header data offsets */
   private static final int MBI_INFOHDR = 128;
   // 0 is a zero byte
   private static final int MBI_ZERO1      =  0; // 1 byte

   private static final int MBI_NAMELEN    =  1; // 1 bytes
   private static final int MBI_FILENAME   =  2; // <= 63 bytes

   /* Finder Info Record */
   private static final int MBI_FILETYPE   = 65; // 4 bytes
   private static final int MBI_FILECREATE = 69; // 4 bytes
   private static final int MBI_FLAGS      = 73; // 1 byte

   // 74 is a 0 byte?
   private static final int MBI_ZERO2      = 74; // 1 byte

   private static final int MBI_VERTWIN    = 75; // 2 bytes
   private static final int MBI_HORZWIN    = 77; // 2 bytes
   private static final int MBI_WIN_ID     = 79; // 2 bytes
   /* END  Finder Info Record */

   private static final int MBI_PROTECT    = 81; // 1 byte - low order bit

   // 82 is a 0 byte?
   private static final int MBI_ZERO3      = 82; // 1 byte

   private static final int MBI_DFLEN      = 83; // 4 bytes
   private static final int MBI_RFLEN      = 87; // 4 bytes
   private static final int MBI_CTIME      = 91; // 4 bytes
   private static final int MBI_MTIME      = 95; // 4 bytes
   private static final int MBI_ZEROFILL   = 99; // 27 bytes
   static final int MBI_OSID       = 126; // 2 bytes

   /*----------------------------------*/
   private static final int MAXNAMELEN = 63;
   private static final int BYTEMASK   = 0x000000FF;

   private static final int INITED    = 0x01;
   private static final int CHANGED   = 0x02;
   private static final int BUSY      = 0x04;
   private static final int BOZO      = 0x08;
   private static final int SYSTEM    = 0x10;
   private static final int BUNDLE    = 0x20;
   private static final int INVISIBLE = 0x40;
   private static final int LOCKED    = 0x40;

   /*--- Data -------------------------------------------------------------*/
   private byte info_header[];

   private int namelen;

   private String filename;
   private String filetype;
   private String filecreator;

   private byte fndr_flags;

   private long data_size, rsrc_size;
   private long data_size_pad, rsrc_size_pad;
   private long data_extra, rsrc_extra;

   private long filelen;
   /*----- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: MacBinaryHeader.java,v 1.4 2009-08-11 02:38:25 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public MacBinaryHeader()
   {
      info_header = new byte[MBI_INFOHDR];
   }

   /*----------------------------------------------------------------------*/
   public static void main( String args[] )
   {
      RandomAccessFile tmpRAFile;
      MacBinaryHeader mbh;
      if(args.length <= 0)
      {
         System.err.println("ERROR: No args given");
         System.exit(1);
      }

      mbh = new MacBinaryHeader();
      try
      {
         tmpRAFile = new RandomAccessFile(args[0], "r");
         mbh.read(tmpRAFile);
         mbh.print(System.out);
         System.out.println("mbh.validate() returns " + mbh.validate());
         System.out.println("mbh.getResForkOffset() returns " +
                            mbh.getResForkOffset());
      } catch(Exception ioe) { ioe.printStackTrace(); }
   }

   /*----------------------------------------------------------------------*/
   public void read(RandomAccessFile theFile) throws IOException
   {
      filelen = theFile.length();
      theFile.read(info_header);

      /* Pull out fork lengths */
      data_size = get4(MBI_DFLEN);
      rsrc_size = get4(MBI_RFLEN);

      data_size_pad = (((data_size + 127) >> 7) << 7);
      rsrc_size_pad = (((rsrc_size + 127) >> 7) << 7);

      data_extra = data_size_pad - data_size;
      rsrc_extra = rsrc_size_pad - rsrc_size;

      namelen     = (int)info_header[MBI_NAMELEN];
      filename    = MacRoman.toString(info_header, MBI_FILENAME,
                               info_header[MBI_NAMELEN]);
      filetype    = MacRoman.toString(info_header, MBI_FILETYPE, 4);
      filecreator = MacRoman.toString(info_header, MBI_FILECREATE, 4);

      fndr_flags  = info_header[MBI_FLAGS];
   }

   /*----------------------------------------------------------------------*/
   public boolean validate()
   {
      // Attempt to figure out if this is a valid MacBinary file...
      // Don't know how reliable this is

      // Following should be 0 bytes
      if(info_header[MBI_ZERO1] != 0) return false;
      if(info_header[MBI_ZERO2] != 0) return false;
      if(info_header[MBI_ZERO3] != 0) return false;

      // Filename has a length range
      if(info_header[MBI_NAMELEN] > MAXNAMELEN) return false;
      if(info_header[MBI_NAMELEN] < 0) return false;

      // Length check
      long sumlen =  MBI_INFOHDR + data_size_pad + rsrc_size_pad;
      if(sumlen != filelen) return false;

      return true;
   }

   /*----------------------------------------------------------------------*/
   public long getResForkOffset()
   {
      return MBI_INFOHDR + data_size_pad;
   }

   /*----------------------------------------------------------------------*/
   public void print(PrintStream ps)
   {
      // String details
      ps.println("      filename = " + filename);
      ps.println("       namelen = " + namelen);
      ps.println("      filetype = " + filetype);
      ps.println("   filecreator = " + filecreator );

      // Flags
      ps.println("");
      ps.print("  Finder flags = ");
      if((fndr_flags & INITED)    > 0) ps.print("INITED, ");
      if((fndr_flags & CHANGED)   > 0) ps.print("CHANGED, ");
      if((fndr_flags & BUSY)      > 0) ps.print("BUSY, ");
      if((fndr_flags & BOZO)      > 0) ps.print("BOZO, ");
      if((fndr_flags & SYSTEM)    > 0) ps.print("SYSTEM, ");
      if((fndr_flags & BUNDLE)    > 0) ps.print("BUNDLE, ");
      if((fndr_flags & INVISIBLE) > 0) ps.print("INVISIBLE, ");
      if((fndr_flags & LOCKED)    > 0) ps.print("LOCKED, ");
      ps.println("");

      // Sizes
      ps.println("");
      ps.println(" data_size_pad = " + data_size_pad);
      ps.println("     data_size = " + data_size);
      ps.println("    data_extra = " + data_extra);
      ps.println("");
      ps.println(" rsrc_size_pad = " + rsrc_size_pad);
      ps.println("     rsrc_size = " + rsrc_size);
      ps.println("    rsrc_extra = " + rsrc_extra);

      ps.println("");
      ps.println("       filelen = " + filelen);
   }

   /*----------------------------------------------------------------------*/
   private long get4(int offset)
   {
      int i;
      long value = 0;

      for(i = 0; i < 4; i++)
      {
         value <<= 8;
         value |= (info_header[offset + i] & BYTEMASK);
      }

      return value;
   }
}

