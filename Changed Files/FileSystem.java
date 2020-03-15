/*******************************************************************************
 * FileSystem
 *
 * The overarching fileSystem that controls everything
 ******************************************************************************/
public class FileSystem
{
   private SuperBlock superblock;
   private Directory directory;
   private FileTable filetable;

    /***************************************************************************
     * FileSystem Constructor
     *
     * create superblock, and format disk with 64 inodes in default
     **************************************************************************/
   FileSystem( int diskBlocks )
   {
      this.superblock = new SuperBlock( diskBlocks );

      // create directory, and register "/" in directory entry 0
      this.directory = new Directory( this.superblock.totalInodes );

      // file table is created, and store directory in the file table
      this.filetable = new FileTable( this.directory );

      // directory reconstruction
      FileTableEntry dirEnt = open( "/", "r" );
      int dirSize = fsize( dirEnt );
      if ( dirSize > 0 )
      {
         byte[] dirData = new byte[dirSize];
         read( dirEnt, dirData );
         this.directory.bytes2directory( dirData );
      }
      close( dirEnt );
   }

    /***************************************************************************
     * clearBlocks
     *
     * Clears the blocks in a fileTableEntry
     **************************************************************************/
    private boolean clearBlocks(FileTableEntry fileTableEntry )
    {
        if (fileTableEntry.inode.count != 1)
        {
            return false;
        }
        else
        {
            byte[] buffer = fileTableEntry.inode.deleteIndexBlock();
            if (buffer != null)
            {
                byte startPtr = 0;

                short seekPtr;
                while((seekPtr = SysLib.bytes2short(buffer, startPtr)) != -1)
                {
                    this.superblock.setBlock(seekPtr);
                }
            }

            int nextBlock = 0;

            while(true)
            {
                if (nextBlock >= Inode.directSize)
                {
                    fileTableEntry.inode.toDisk(fileTableEntry.iNumber);
                    return true;
                }

                if (fileTableEntry.inode.direct[nextBlock] != -1)
                {
                    this.superblock.setBlock(
                            fileTableEntry.inode.direct[nextBlock]);
                    fileTableEntry.inode.direct[nextBlock] = -1;
                }

                ++nextBlock;
            }
        }
    }

    /***************************************************************************
     * format
     *
     * Formats the file system
     **************************************************************************/
   boolean format( int files )
   {
       this.superblock.format(files);
       this.directory = new Directory(this.superblock.totalInodes);
       this.filetable = new FileTable(this.directory);
       return true;
   }

    /***************************************************************************
     * open
     *
     * Opens a file with a given mode.
     *
     * Modes:
     * • Read
     * • Write
     * • Read/Write
     * • Append
     *
     **************************************************************************/
   FileTableEntry open( String filename, String mode )
   {
       FileTableEntry fileTableEntry = this.filetable.falloc(filename, mode);

       if(mode.equals("w") && !this.clearBlocks(fileTableEntry))
       {
           return null;
       }
       else
       {
           return fileTableEntry;
       }
   }

    /***************************************************************************
     * close
     *
     * Closes a file
     **************************************************************************/
   synchronized boolean close( FileTableEntry fileTableEntry )
   {
       fileTableEntry.count--;

       return fileTableEntry.count > 0 || this.filetable.ffree(fileTableEntry);
   }

    /***************************************************************************
     * read
     *
     * Reads a buffer into the file system
     **************************************************************************/
   public synchronized int read( FileTableEntry fileTableEntry, byte[] buffer )
   {
       if (!fileTableEntry.mode.equals("w") && !fileTableEntry.mode.equals("a"))
       {
           int index = 0;
           int max = buffer.length;

           while(max > 0
                   && fileTableEntry.seekPtr < this.fsize(fileTableEntry))
           {
               int targetBlock =
                       fileTableEntry.inode
                               .getBlock(fileTableEntry.seekPtr);
               if (targetBlock == -1)
               {
                   break;
               }

               byte[] buffer2 = new byte[512];
               SysLib.rawread(targetBlock, buffer2);
               int block = fileTableEntry.seekPtr % 512;
               int offset = 512 - block;
               int offset2 =
                       this.fsize(fileTableEntry) - fileTableEntry.seekPtr;
               int minPos = Math.min(Math.min(offset, max), offset2);
               System.arraycopy(buffer2, block, buffer, index, minPos);
               fileTableEntry.seekPtr += minPos;
               index += minPos;
               max -= minPos;
           }

           return index;

       }
       else
       {
           return -1;
       }
   }

    /***************************************************************************
     * write
     *
     * Writes to a file
     **************************************************************************/
   synchronized int write( FileTableEntry fileTableEntry, byte[] buffer )
   {
       if (fileTableEntry.mode.equals("r"))
       {
           return -1;
       }
       else
       {
           int startPtr = 0;
           int indexPtr = buffer.length;

           while(indexPtr > 0)
           {
               int block = fileTableEntry.inode.
                       getBlock(fileTableEntry.seekPtr);
               if (block == -1)
               {
                   short nextBlock = (short)this.superblock.getNextBlock();
                   switch(fileTableEntry.inode.
                           makeBlock(fileTableEntry.seekPtr, nextBlock))
                   {
                       case -3:
                           short nextBlock2 =
                                   (short)this.superblock.getNextBlock();
                           if (!fileTableEntry.inode.
                                   makeIndexBlock(nextBlock2))
                           {
                               SysLib.cerr("Error while writing :(\n");
                               return -1;
                           }

                           if (fileTableEntry.inode.makeBlock(
                                   fileTableEntry.seekPtr, nextBlock) != 0)
                           {
                               SysLib.cerr("Error while writing :(\n");
                               return -1;
                           }
                       case 0:
                       default:
                           block = nextBlock;
                           break;
                       case -2:
                       case -1:
                           SysLib.cerr("Error while writing :(\n");
                           return -1;
                   }
               }

               byte[] buffer2 = new byte[512];
               if (SysLib.rawread(block, buffer2) == -1)
               {
                   System.exit(2);
               }

               int blockPtr = fileTableEntry.seekPtr % 512;
               int blockOffset = 512 - blockPtr;
               int minPtr = Math.min(blockOffset, indexPtr);
               System.arraycopy(
                       buffer, startPtr, buffer2, blockPtr, minPtr);
               SysLib.rawwrite(block, buffer2);
               fileTableEntry.seekPtr += minPtr;
               startPtr += minPtr;
               indexPtr -= minPtr;
               if (fileTableEntry.seekPtr > fileTableEntry.inode.length)
               {
                   fileTableEntry.inode.length = fileTableEntry.seekPtr;
               }
           }

           fileTableEntry.inode.toDisk(fileTableEntry.iNumber);
           return startPtr;

       }
   }

    /***************************************************************************
     * delete
     *
     * deletes a given file
     **************************************************************************/
   boolean delete( String fileName )
   {
       FileTableEntry fileTableEntry = this.open(fileName, "w");
       short iNumber = fileTableEntry.iNumber;
       return this.close(fileTableEntry) && this.directory.ifree(iNumber);
   }

    /***************************************************************************
     * seek
     *
     * Seeks a file in the file system
     **************************************************************************/
   public synchronized int seek( FileTableEntry fileTableEntry,
                                 int offset, int whence )
   {
       switch(whence)
       {
           case 0:
               if (offset >= 0 && offset <= this.fsize(fileTableEntry))
               {
                   fileTableEntry.seekPtr = offset;
                   break;
               }

               return -1;
           case 1:
               if (fileTableEntry.seekPtr + offset >= 0
                       && fileTableEntry.seekPtr + offset <=
                       this.fsize(fileTableEntry))
               {
                   fileTableEntry.seekPtr += offset;
                   break;
               }

               return -1;
           case 2:
               if (this.fsize(fileTableEntry) + offset < 0 ||
                       this.fsize(fileTableEntry) + offset >
                               this.fsize(fileTableEntry))
               {
                   return -1;
               }

               fileTableEntry.seekPtr = this.fsize(fileTableEntry) + offset;
       }

       return fileTableEntry.seekPtr;
   }

    /***************************************************************************
     * fsize
     *
     * Returns the size of a file
     **************************************************************************/
    synchronized int fsize( FileTableEntry fileTableEntry )
    {
        return fileTableEntry.inode.length;
    }
}
