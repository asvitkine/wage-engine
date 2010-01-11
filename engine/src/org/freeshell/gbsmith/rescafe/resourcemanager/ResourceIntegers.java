package org.freeshell.gbsmith.rescafe.resourcemanager;

import java.io.IOException;
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
/**
 * ResourceIntegers: A set of static methods for properly parsing the
 * byte data of a Resource Fork into bigger types. Some of this stuff is
 * already done in the (later discovered ;-) ) class DataInputStream
 * but some is a little strange
 * @author Copyright (c) 1999 by George B. Smith
 */
class ResourceIntegers
{
   /*--- This is a Data-less class --------------------------------------*/

   /*--- Methods --------------------------------------------------------*/
   public static long readResLong(RandomAccessFile inraf) throws IOException
   {
      byte tmpbytes[] = new byte[4];
      long outval = 0;

      inraf.read(tmpbytes); // Read in 4 bytes

      /* This funny business is necessary to turn the:
           - raw bytes from the file
              which are read into
           - an array of signed bytes (the only kind Java has)
             into
           - an unsigned integer range (the type we are after)
             which must be held in a variable of
           - type signed long  - the only convenient Java type available to
             hold the potential positive range
      */
      for(int j=0; j < tmpbytes.length; j++)
      {
         outval = (outval << 8); // Shift prev byte left
         for (int k=1; k <= 128; k*=2)
            if ((tmpbytes[j] & k) != 0)
               outval += k;
      }

      return outval;
   }

   /*----------------------------------------------------------------------*/
   public static long readRes3Byte(RandomAccessFile inraf) throws IOException
   {
      byte tmpbytes[] = new byte[3];
      long outval = 0;

      inraf.read(tmpbytes); // Read in 3 bytes

      // This funny business... same as above
      for(int j=0; j < tmpbytes.length; j++)
      {
         outval = (outval << 8); // Shift prev byte left
         for (int k=1; k <= 128; k*=2)
            if ((tmpbytes[j] & k) != 0)
               outval += k;
      }

      return outval;
   }

   /*----------------------------------------------------------------------*/
   public static int readResUnsignedByte(RandomAccessFile inraf) throws IOException
   {
      byte tmpbyte;
      int outval = 0;

      tmpbyte = inraf.readByte();

      for (int k=1; k <= 128; k*=2)
         if ((tmpbyte & k) != 0)
            outval += k;

      return outval;
   }
}
