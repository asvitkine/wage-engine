/* $Header: /home/cvsroot/ResCafe/src/ResourceManager/ResourceModel.java,v 1.9 2009-08-11 02:36:11 gbsmith Exp $ */

package org.freeshell.gbsmith.rescafe.resourcemanager;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Set;

/*=======================================================================*/
/*
 * $Log: ResourceModel.java,v $
 * Revision 1.9  2009-08-11 02:36:11  gbsmith
 * Changed to MIT License
 *
 * Revision 1.8  2000/12/11 19:16:19  gbsmith
 * Added method to return list of types present as a String[],
 * Added a general contains(TYPE) method
 * Made getCountofType() safer, returning 0 if the type is absent
 *
 * Revision 1.7  2000/05/24 02:51:56  gbsmith
 * Removed superfluous void return type from constructors
 * after compiler warnings.
 *
 * Revision 1.6  1999/10/21 23:50:22  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.5  1999/10/17 20:39:30  gbsmith
 * Added 'become' method as a kind of midstream copy constructor to
 * replace old data with new data while still preserving the actual
 * instance which may be referenced in many places.
 *
 * Revision 1.4  1999/10/13 07:43:45  gbsmith
 * Added support for non-zero file seeks which are necessary to support
 * reading a full MacBinary file.
 *
 * Revision 1.3  1999/10/04 20:40:54  gbsmith
 * Added to package ResourceManager. Made class imports more explicit.
 * Added javadoc formatted comment to public classes and methods. With
 * new package arrangment, altered accesses of some intra-package
 * variable references. Changed 'print' methods to take PrintStream
 * params to write to.
 *
 * Revision 1.2  1999/09/30 05:30:47  gbsmith
 * Added getCountOfType method to ResourceModel
 *
 * Revision 1.1  1999/09/27 06:51:20  gbsmith
 * Initial revision
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
 * ResourceModel: A Data Model (as in MVC Model) for managing the Resources
 * stored in the Resource Fork of a Macintosh File. After reading, the
 * raw byte data of the loaded Resources can be accessed and interpreted
 * as a client sees fit.
 * @author Copyright (c) 1999 by George B. Smith
 */
public class ResourceModel extends Observable
{
   /*--- Data -----------------------------------------------------------*/
   String sourceFileName;

   ResourceHeader theHeader;
   ResourceMap theMap;
   Hashtable theTypes;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ResourceModel.java,v 1.9 2009-08-11 02:36:11 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   /**
    * Sets up a new ResourceModel
    */
   public ResourceModel() { init(); }

   /*----------------------------------------------------------------------*/
   /**
    * Sets up a new ResourceModel to load from the given filename
    * @param inName the new name of the file
    */
   public ResourceModel(String inName) { init(inName); }

   /*----------------------------------------------------------------------*/
   /**
    * Prepares the ResourceModel for loading. Can be used to reset the
    * model for reloading as well
    */
   public void init()
   {
      theHeader = new ResourceHeader();
      theMap    = new ResourceMap();
      theTypes  = new Hashtable();
   }

   /*----------------------------------------------------------------------*/
   /**
    * Prepares the ResourceModel for loading and sets the name of the file
    * being loaded. Can be used to reset the model for reloading as well
    * @param inName the new name of the file
    */
   public void init(String inName)
   {
      sourceFileName = new String(inName);
      theHeader = new ResourceHeader();
      theMap    = new ResourceMap();
      theTypes  = new Hashtable();
   }

   /*----------------------------------------------------------------------*/
   /**
    * A sort of copy constructor; allows an existing ResourceModel to take
    * over the data of another rather than instantiating a new one.
    * Necessary for some cases where a reference to particular instance
    * needs to be maintatined in various parts of a program.
    * @param become_this the ResourceModel whose data we will assume.
    */
   public void become(ResourceModel become_this)
   {
      sourceFileName = become_this.sourceFileName;
      theHeader = become_this.theHeader;
      theMap    = become_this.theMap;
      theTypes  = become_this.theTypes;

      setChanged();
      notifyObservers();
   }


   /*----------------------------------------------------------------------*/
   /**
    * Sets the name of the Mac Resource Fork file being managed
    *  @param inName the new name of the file
    */
   public void setFilename(String inName)
   {
      sourceFileName = new String(inName);
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the name of the Mac Resource Fork file being managed
    */
   public String getFilename() { return sourceFileName; }

   /*----------------------------------------------------------------------*/
   /**
    * Initiates a full read, parse and store of the Mac Resource Fork
    * specified by the given RandomAccessFile. It reads ALL types and
    * resources in the file into memory, eliminating the need for further
    * file access.
    * @param inraf the RandomAccessFile Object specifying the Resource Fork
    * to be loaded
    */
   public void read(RandomAccessFile inraf) throws IOException
   {
      read(inraf, 0); // Rather than have almost duplicate code...
   }

   /*----------------------------------------------------------------------*/
   /**
    * Initiates a full read, parse and store of the Mac Resource Fork
    * specified by the given RandomAccessFile. It reads ALL types and
    * resources in the file into memory, eliminating the need for further
    * file access.
    * @param inraf the RandomAccessFile Object specifying the Resource Fork
    * to be loaded
    * @param seekSet the file position of the beginning of the Resource Fork
    */
   public void read(RandomAccessFile inraf, long seekSet) throws IOException
   {
      ResourceType tmpRT;

      inraf.seek(seekSet);
      theHeader = new ResourceHeader();

      // MRHeader does no seeking so seekSet not needed
      theHeader.read(inraf);

      inraf.seek(seekSet + theHeader.mapOffset + 22);

      theMap.read(inraf);

      // Collect Type data
      inraf.seek(seekSet + theHeader.mapOffset + theMap.typeOffset + 2);
      for(int i = 0; i < theMap.numTypes; i++)
      {
         tmpRT = new ResourceType();

         // MRType.readType does no seeking so seekSet not needed
         tmpRT.readType(inraf);
         theTypes.put(tmpRT.id, tmpRT);
      }

      // Collect Resource data
      Enumeration keysRT = theTypes.keys();
      while( keysRT.hasMoreElements() )
      {
         tmpRT = (ResourceType)theTypes.get(keysRT.nextElement());
         tmpRT.readResources(inraf, theHeader, theMap, seekSet );
      }

      setChanged();
      notifyObservers();
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceModel information (not including data) to a Stream
    * @param ps the PrintStream to print the info
    */
   public void print(PrintStream ps )
   {
      ResourceType tmpRT;

      ps.println("File = " + sourceFileName);
      theHeader.print(ps);
      theMap.print(ps);

   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the ResourceModel information (not including data) to a Stream
    * as well as the info of all the Types it holds
    * @param ps the PrintStream to print the info
    */
   public void printAll(PrintStream ps )
   {
      ResourceType tmpRT;

      ps.println("File = " + sourceFileName);
      theHeader.print(ps);
      theMap.print(ps);

      int i = 0;
      Enumeration keysRT = theTypes.keys();
      while( keysRT.hasMoreElements() )
      {
         ps.print("" + i++ + ") ");
         tmpRT = (ResourceType)theTypes.get(keysRT.nextElement());
         tmpRT.printAll(ps);
      }
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns an Enumeration of all the Type keys of the Model to enable
    * iterative Hashtable access to all the ResourceType of this Model
    */
   public Enumeration getTypes()
   {
      if(theTypes == null) return null;
      if(theTypes.isEmpty()) return null;
      return theTypes.keys();
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns a String array of all the Types of the Model
    */
   public String[] getTypeArray()
   {
      String outArray[];
      Set    mykeys;

      if(theTypes == null || theTypes.isEmpty())
      {
         outArray = new String[0];
         return outArray;
      }

      mykeys = theTypes.keySet();
      outArray = new String[mykeys.size()];
      mykeys.toArray(outArray);
      return outArray;
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the single
    * ResourceType
    * keyed to the given ID. This is
    * Hashtable style access.
    * @param inType the 4-Letter ID Key of the desired ResourceType
    */
   public ResourceType getResourceType(String inType)
   {
      return (ResourceType)theTypes.get(inType);
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the single Resource keyed to the given numeric Resource ID
    * of the
    * ResourceType
    * keyed to the given 4-Letter Type ID.
    * This is Hashtable style access - at two levels. This method allows
    * the client to grab a single Resource in one operation
    * @param inType the 4-Letter ID Key of the desired ResourceType
    * @param idtoget the numeric ID Key of the desired Resource
    */
   public Resource getResource(String inType, Short idtoget)
   {
      return (Resource)
         ((ResourceType)theTypes.get(inType)).getResource(idtoget);
   }

   /*----------------------------------------------------------------------*/
   /**
    * A convenience method allowing access with a primitive short key in
    * addition to the Short Object method
    * @param inType the 4-Letter ID Key of the desired ResourceType
    * @param idtoget the numeric ID Key of the desired Resource
    */
   public Resource getResource(String inType, short idtoget)
   {
      return  getResource(inType, new Short(idtoget));
   }

   /*----------------------------------------------------------------------*/
   /**
    * A method to check whether any Resources of the given Type exist
    * @param inType the 4-Letter ID Key of the desired ResourceType
    */
   public boolean contains(String inType) 
   {
      return theTypes.containsKey(inType);
   }

   /*----------------------------------------------------------------------*/
   /**
    * A method to check whether a Resource of the given Type and ID exists
    * @param inType the 4-Letter ID Key of the desired ResourceType
    * @param idtocheck the numeric ID Key of the desired Resource
    */
   public boolean contains(String inType, Short idtocheck)
   {
      if(!theTypes.containsKey(inType)) return false;
      return ((ResourceType)theTypes.get(inType)).contains(idtocheck);
   }

   /*----------------------------------------------------------------------*/
   /**
    * A convenience method allowing a test with a primitive short key in
    * addition to the Short Object method
    * @param inType the 4-Letter ID Key of the desired ResourceType
    * @param idtocheck the numeric ID Key of the desired Resource
    */
   public boolean contains(String inType, short idtocheck)
   {
      return contains(inType, new Short(idtocheck));
   }

   /*----------------------------------------------------------------------*/
   /**
    * Returns the number of Resources of the given type
    * @param inType the 4-Letter ID Key of the desired ResourceType
    */
   public int getCountOfType(String inType)
   {
      if(theTypes.containsKey(inType))
         return ((ResourceType)theTypes.get(inType)).numItems;
      else return 0;
   }
}
