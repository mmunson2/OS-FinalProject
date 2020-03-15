/*******************************************************************************
 *
 ******************************************************************************/
public class FileSystem {
   private SuperBlock superblock;
   private Directory directory;
   private FileTable filetable;

    /***************************************************************************
     *
     **************************************************************************/
   public FileSystem( int diskBlocks ) {
      // create superblock, and format disk with 64 inodes in default
      superblock = new SuperBlock( diskBlocks );

      // create directory, and register "/" in directory entry 0
      directory = new Directory( superblock.totalInodes );

      // file table is created, and store directory in the file table
      filetable = new FileTable( directory );

      // directory reconstruction
      FileTableEntry dirEnt = open( "/", "r" );
      int dirSize = fsize( dirEnt );
      if ( dirSize > 0 ) {
         byte[] dirData = new byte[dirSize];
         read( dirEnt, dirData );
         directory.bytes2directory( dirData );
      }
      close( dirEnt );
   }

    /***************************************************************************
     *
     **************************************************************************/
   public void sync( )
   {
       FileTableEntry fileTable = this.open("/", "w");
       byte[] buffer = this.directory.directory2bytes();
       this.write(fileTable, buffer);
       this.close(fileTable);
       this.superblock.syncBlock();
   }

    /***************************************************************************
     *
     **************************************************************************/
   boolean format( int files )
   {
       while(!this.filetable.fempty())
       {
           /* Do Nothing */
       }

       this.superblock.format(files);
       this.directory = new Directory(this.superblock.totalInodes);
       this.filetable = new FileTable(this.directory);
       return true;
   }

    /***************************************************************************
     *
     **************************************************************************/
   public FileTableEntry open( String filename, String mode )
   {
       FileTableEntry fileTableEntry = this.filetable.falloc(filename, mode);

       if(mode == "w" && !this.clearBlocks(fileTableEntry))
       {
           return null;
       }
       else
       {
           return fileTableEntry;
       }
   }

    /***************************************************************************
     *
     **************************************************************************/
   public synchronized boolean close( FileTableEntry ftEnt )
   {
       ftEnt.count--;

       return ftEnt.count > 0 || this.filetable.ffree(ftEnt);
   }

    /***************************************************************************
     *
     **************************************************************************/
   public synchronized int fsize( FileTableEntry ftEnt )
   {
       return ftEnt.inode.length;
   }

    /***************************************************************************
     *
     **************************************************************************/
   public int read( FileTableEntry ftEnt, byte[] buffer )
   {
       if (ftEnt.mode != "w" && ftEnt.mode != "a") {
           int index = 0;
           int max = buffer.length;

           synchronized(ftEnt) {
               while(max > 0 && ftEnt.seekPtr < this.fsize(ftEnt)) {
                   int targetBlock = ftEnt.inode.getBlock(ftEnt.seekPtr);
                   if (targetBlock == -1) {
                       break;
                   }

                   byte[] buffer2 = new byte[512];
                   SysLib.rawread(targetBlock, buffer2);
                   int block = ftEnt.seekPtr % 512;
                   int offset = 512 - block;
                   int offset2 = this.fsize(ftEnt) - ftEnt.seekPtr;
                   int minPos = Math.min(Math.min(offset, max), offset2);
                   System.arraycopy(buffer2, block, buffer, index, minPos);
                   ftEnt.seekPtr += minPos;
                   index += minPos;
                   max -= minPos;
               }

               return index;
           }
       } else {
           return -1;
       }
   }

    /***************************************************************************
     *
     **************************************************************************/
   public int write( FileTableEntry ftEnt, byte[] buffer )
   {
       if (ftEnt.mode == "r") {
           return -1;
       } else {
           synchronized(ftEnt) {
               int startPtr = 0;
               int indexPtr = buffer.length;

               while(indexPtr > 0) {
                   int block = ftEnt.inode.getBlock(ftEnt.seekPtr);
                   if (block == -1) {
                       short nextBlock = (short)this.superblock.getNextBlock();
                       switch(ftEnt.inode.makeBlock(ftEnt.seekPtr, nextBlock)) {
                           case -3:
                               short nextBlock2 = (short)this.superblock.getNextBlock();
                               if (!ftEnt.inode.makeIndexBlock(nextBlock2)) {
                                   SysLib.cerr("ThreadOS: panic on write\n");
                                   return -1;
                               }

                               if (ftEnt.inode.makeBlock(ftEnt.seekPtr, nextBlock) != 0) {
                                   SysLib.cerr("ThreadOS: panic on write\n");
                                   return -1;
                               }
                           case 0:
                           default:
                               block = nextBlock;
                               break;
                           case -2:
                           case -1:
                               SysLib.cerr("ThreadOS: filesystem panic on write\n");
                               return -1;
                       }
                   }

                   byte[] buffer2 = new byte[512];
                   if (SysLib.rawread(block, buffer2) == -1) {
                       System.exit(2);
                   }

                   int blockPtr = ftEnt.seekPtr % 512;
                   int blockOffset = 512 - blockPtr;
                   int minPtr = Math.min(blockOffset, indexPtr);
                   System.arraycopy(buffer, startPtr, buffer2, blockPtr, minPtr);
                   SysLib.rawwrite(block, buffer2);
                   ftEnt.seekPtr += minPtr;
                   startPtr += minPtr;
                   indexPtr -= minPtr;
                   if (ftEnt.seekPtr > ftEnt.inode.length) {
                       ftEnt.inode.length = ftEnt.seekPtr;
                   }
               }

               ftEnt.inode.toDisk(ftEnt.iNumber);
               return startPtr;
           }
       }
   }

    /***************************************************************************
     *
     **************************************************************************/
   private boolean clearBlocks(FileTableEntry ftEnt )
   {
       if (ftEnt.inode.count != 1) {
           return false;
       } else {
           byte[] buffer = ftEnt.inode.deleteIndexBlock();
           if (buffer != null) {
               byte startPtr = 0;

               short seekPtr;
               while((seekPtr = SysLib.bytes2short(buffer, startPtr)) != -1) {
                   this.superblock.setBlock(seekPtr);
               }
           }

           int nextBlock = 0;

           while(true) {
               if (nextBlock >= 11) {
                   ftEnt.inode.toDisk(ftEnt.iNumber);
                   return true;
               }

               if (ftEnt.inode.direct[nextBlock] != -1) {
                   this.superblock.setBlock(ftEnt.inode.direct[nextBlock]);
                   ftEnt.inode.direct[nextBlock] = -1;
               }

               ++nextBlock;
           }
       }
   }

    /***************************************************************************
     *
     **************************************************************************/
   public boolean delete( String fileName )
   {
       FileTableEntry fileTableEntry = this.open(fileName, "w");
       short iNumber = fileTableEntry.iNumber;
       return this.close(fileTableEntry) && this.directory.ifree(iNumber);
   }

   private final int SEEK_SET = 0;
   private final int SEEK_CUR = 1;
   private final int SEEK_END = 2;

    /***************************************************************************
     *
     **************************************************************************/
   public int seek( FileTableEntry ftEnt, int offset, int whence )
   {
       synchronized(ftEnt) {
           switch(whence) {
               case 0:
                   if (offset >= 0 && offset <= this.fsize(ftEnt)) {
                       ftEnt.seekPtr = offset;
                       break;
                   }

                   return -1;
               case 1:
                   if (ftEnt.seekPtr + offset >= 0 && ftEnt.seekPtr + offset <= this.fsize(ftEnt)) {
                       ftEnt.seekPtr += offset;
                       break;
                   }

                   return -1;
               case 2:
                   if (this.fsize(ftEnt) + offset < 0 || this.fsize(ftEnt) + offset > this.fsize(ftEnt)) {
                       return -1;
                   }

                   ftEnt.seekPtr = this.fsize(ftEnt) + offset;
           }

           return ftEnt.seekPtr;
       }
   }
}
