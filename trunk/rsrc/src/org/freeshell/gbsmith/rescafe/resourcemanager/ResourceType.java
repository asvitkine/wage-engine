/* $Header: /home/cvsroot/ResCafe/src/ResourceManager/ResourceType.java,v 1.5 2009-08-11 02:36:11 gbsmith Exp $ */

package org.freeshell.gbsmith.rescafe.resourcemanager;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;

/*=======================================================================*/
/*
 * $Log: ResourceType.java,v $
 * Revision 1.5  2009-08-11 02:36:11  gbsmith
 * Changed to MIT License
 *
 * Revision 1.4  2000/11/27 20:05:34  gbsmith
 * Added ident tags for ResourceHeader and ResourceMap classes.
 *
 * Revision 1.3  1999/10/21 23:52:11  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/13 07:43:18  gbsmith
 * Added support for non-zero file seeks which are necessary to support
 * reading a full MacBinary file.
 *
 * Revision 1.1  1999/10/04 20:16:19  gbsmith
 * Initial revision
 *
 */

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
 * ResourceType: ResourceType reads and holds all the Resources of a
 * single type (e.g. 'ICON', 'MENU'). It allows both individual Hashtable
 * access and en masse Array access to the Resources.
 * Public accessor methods are provided to access additional Type data
 * such as the ID of the type and the number of Resources
 * @author Copyright (c) 1999 by George B. Smith
 */

public class ResourceType
{
   /*--- Data -----------------------------------------------------------*/
   String id; // 4 char string
   short numItems;
   short offset;

   Resource resArray[]; // Resources of this Type
   Hashtable resHash;   // Keep array for backward compat

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: ResourceType.java,v 1.5 2009-08-11 02:36:11 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   void readType(RandomAccessFile inraf) throws IOException
   {
      byte tmpbytes[] = new byte[4];

      inraf.read(tmpbytes); // Read in 4 bytes
      id = new String( tmpbytes );
      numItems  = inraf.readShort();
      numItems++;
      offset    = inraf.readShort();

      resHash  = new Hashtable(numItems);
      resArray = new Resource[numItems]; // Keep array for backward compat
   }

   /*----------------------------------------------------------------------*/
   void readResources(RandomAccessFile inraf, ResourceHeader rhdr,
                      ResourceMap rmap) throws IOException
   {
      // Rather than have almost duplicate code...
      readResources(inraf, rhdr, rmap, 0);
   }

   /*----------------------------------------------------------------------*/
   void readResources(RandomAccessFile inraf, ResourceHeader rhdr,
                      ResourceMap rmap, long seekSet) throws IOException
   {
      Resource currentRes;

      inraf.seek(seekSet + rhdr.mapOffset + rmap.typeOffset + offset);
      for(int i = 0; i < numItems; i++)
      {
         currentRes = new Resource();
         currentRes.readInfo(inraf);
         resHash.put(new Short(currentRes.id), currentRes);
         resArray[i] = currentRes; // Keep array for backward compat
      }

      for(int i = 0; i < numItems; i++)
      {
         // Use existing 'offset' param to carry seekSet displacement
         resArray[i].readName(inraf, seekSet + rhdr.mapOffset + rmap.nameOffset);
         resArray[i].readData(inraf, seekSet + rhdr.dataOffset);
      }
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceType information (not including data) to a Stream
    * @param ps the PrintStream to print the info
    */
   public void print(PrintStream ps)
   {
      ps.println("Resource Type");
      ps.println("\tid       = " + id );
      ps.println("\tnumItems = " + numItems );
      ps.println("\toffset   = " + offset );
      ps.println("--------------------------------------------------");
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceType information (not including data) to a Stream
    * as well as the info of all the Resources it holds
    * @param ps the PrintStream to print the info
    */
   public void printAll(PrintStream ps)
   {
      ps.println("Resource Type");
      ps.println("\tid       = " + id );
      ps.println("\tnumItems = " + numItems );
      ps.println("\toffset   = " + offset );
      ps.println("--------------------------------------------------");

      for(int i = 0; i < resArray.length; i++)
      {
         ps.print("" + i + ") ");
         resArray[i].print(ps);
      }
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the number of Items of this ResourceType
    */
   public short size()       { return numItems; }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the Type Code of this ResourceType, e.g. 'icl8', 'BNDL'
    */
   public String getID()            { return id;       }

   /*----------------------------------------------------------------------*/
   /**
    * Returns all the Resources of this ResourceType in a standard Array.
    * Some clients may find Array access (0, 1, 2, etc.) more useful for
    * some applications than Hash access based on the Resource ID
    */
   public Resource[] getResArray() { return resArray;   }

   /*----------------------------------------------------------------------*/
   // Access via HASHES
   // public Hashtable getResHash() { return resHash;   }
   // Hmmm... Can just access hash-like with below methods...

   /*----------------------------------------------------------------------*/
   // Access Individually
   /**
    * Returns the single Resource keyed to the given ID. This is Hashtable
    * style access and is easier for some applications, when trying to
    * access a particular Resource.
    * @param idtoget the ID Key of the desired Resource
    */
   public Resource getResource(Short idtoget)
   {
      return (Resource)resHash.get(idtoget);
      //return null;
   }

   /*----------------------------------------------------------------------*/
   /**
    * A convenience method allowing access with a primitive short key in
    * addition to the Short Object method
    * @param idtoget the ID Key of the desired Resource
    */
   public Resource getResource(short idtoget)
   {
      return  getResource(new Short(idtoget));
   }

   /*----------------------------------------------------------------------*/
   /**
    * Test whether a Resource of the given ID exists or not
    * @param idtocheck the Resource ID in question
    */
   public boolean contains(Short idtocheck)
   {
      return resHash.containsKey(idtocheck);
   }

   /*----------------------------------------------------------------------*/
   /**
    * A convenience method allowing a test with a primitive short key in
    * addition to the Short Object method
    * @param idtocheck  the Resource ID in question
    */
   public boolean contains(short idtocheck)
   {
      return contains(new Short(idtocheck));
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns an Enumeration of all the ID keys of the type to enable
    * iterative Hashtable access to all the Resources of this type
    */
   public Enumeration getResourceIDs()
   {
      return resHash.keys();
   }
}


/*=========================================================================*/
class ResourceHeader
{
   long dataOffset;
   long mapOffset;
   long dataLength;
   long mapLength;

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: ResourceType.java,v 1.5 2009-08-11 02:36:11 gbsmith Exp $";

   /*----------------------------------------------------------------------*/
   void read(RandomAccessFile inraf) throws IOException
   {
      dataOffset = ResourceIntegers.readResLong(inraf);
      mapOffset  = ResourceIntegers.readResLong(inraf);
      dataLength = ResourceIntegers.readResLong(inraf);
      mapLength  = ResourceIntegers.readResLong(inraf);
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceHeader information (not including data) to a Stream
    * @param ps the PrintStream to print the info
    */
   public void print(PrintStream ps)
   {
      ps.println("Resource Header");
      ps.println("\tdataOffset = " + dataOffset );
      ps.println("\tmapOffset  = " + mapOffset  );
      ps.println("\tdataLength = " + dataLength );
      ps.println("\tmapLength  = " + mapLength  );
      ps.println("--------------------------------------------------");
   }
}


/*=========================================================================*/
class ResourceMap
{
   short resAttr;
   short typeOffset;
   short nameOffset;
   short numTypes;

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: ResourceType.java,v 1.5 2009-08-11 02:36:11 gbsmith Exp $";

   /*----------------------------------------------------------------------*/
   void read(RandomAccessFile inraf) throws IOException
   {
      resAttr    = inraf.readShort();
      typeOffset = inraf.readShort();
      nameOffset = inraf.readShort();
      numTypes   = inraf.readShort();
      numTypes++;
   }

   /*----------------------------------------------------------------------*/
   void read(RandomAccessFile inraf, long offset ) throws IOException
   {
      inraf.seek(offset);
      resAttr    = inraf.readShort();
      typeOffset = inraf.readShort();
      nameOffset = inraf.readShort();
      numTypes   = inraf.readShort();
      numTypes++;
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceMap information (not including data) to a Stream
    * @param ps the PrintStream to print the info
    */
   public void print(PrintStream ps)
   {
      ps.println("Resource Map");
      ps.println("\tresAttr    = " + resAttr    );
      ps.println("\ttypeOffset = " + typeOffset );
      ps.println("\tnameOffset = " + nameOffset );
      ps.println("\tnumTypes   = " + numTypes   );
      ps.println("--------------------------------------------------");
   }
}
