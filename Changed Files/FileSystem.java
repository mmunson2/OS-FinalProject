public class FileSystem {
   private SuperBlock superblock;
   private Directory directory;
   private FileTable filetable;

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

   public void sync( )
   {
       FileTableEntry fileTable = this.open("/", "w");
       byte[] buffer = this.directory.directory2bytes();
       this.write(fileTable, buffer);
       this.close(fileTable);
       this.superblock.sync();
   }

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

   public FileTableEntry open( String filename, String mode )
   {
       FileTableEntry fileTableEntry = this.filetable.falloc(filename, mode);

       if(mode == "w" && !this.deallocAllBlocks(fileTableEntry))
       {
           return null;
       }
       else
       {
           return fileTableEntry;
       }
   }

   public synchronized boolean close( FileTableEntry ftEnt )
   {
       ftEnt.count--;

       return ftEnt.count > 0 || this.filetable.ffree(ftEnt);
   }

   public synchronized int fsize( FileTableEntry ftEnt )
   {
       return ftEnt.inode.length;
   }

   public int read( FileTableEntry ftEnt, byte[] buffer )
   {
       if (ftEnt.mode != "w" && ftEnt.mode != "a") {
           int index = 0;
           int max = buffer.length;

           synchronized(ftEnt) {
               while(max > 0 && ftEnt.seekPtr < this.fsize(ftEnt)) {
                   int targetBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                   if (targetBlock == -1) {
                       break;
                   }

                   byte[] buffer2 = new byte[512];
                   SysLib.rawread(targetBlock, buffer2);
                   int block = ftEnt.seekPtr % 512;
                   int var9 = 512 - block;
                   int var10 = this.fsize(ftEnt) - ftEnt.seekPtr;
                   int var11 = Math.min(Math.min(var9, max), var10);
                   System.arraycopy(buffer2, block, buffer, index, var11);
                   ftEnt.seekPtr += var11;
                   index += var11;
                   max -= var11;
               }

               return index;
           }
       } else {
           return -1;
       }
   }

   public int write( FileTableEntry ftEnt, byte[] buffer )
   {
       if (ftEnt.mode == "r") {
           return -1;
       } else {
           synchronized(ftEnt) {
               int var4 = 0;
               int var5 = buffer.length;

               while(var5 > 0) {
                   int var6 = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                   if (var6 == -1) {
                       short var7 = (short)this.superblock.nextBlock();
                       switch(ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, var7)) {
                           case -3:
                               short var8 = (short)this.superblock.nextBlock();
                               if (!ftEnt.inode.registerIndexBlock(var8)) {
                                   SysLib.cerr("ThreadOS: panic on write\n");
                                   return -1;
                               }

                               if (ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, var7) != 0) {
                                   SysLib.cerr("ThreadOS: panic on write\n");
                                   return -1;
                               }
                           case 0:
                           default:
                               var6 = var7;
                               break;
                           case -2:
                           case -1:
                               SysLib.cerr("ThreadOS: filesystem panic on write\n");
                               return -1;
                       }
                   }

                   byte[] var13 = new byte[512];
                   if (SysLib.rawread(var6, var13) == -1) {
                       System.exit(2);
                   }

                   int var14 = ftEnt.seekPtr % 512;
                   int var9 = 512 - var14;
                   int var10 = Math.min(var9, var5);
                   System.arraycopy(buffer, var4, var13, var14, var10);
                   SysLib.rawwrite(var6, var13);
                   ftEnt.seekPtr += var10;
                   var4 += var10;
                   var5 -= var10;
                   if (ftEnt.seekPtr > ftEnt.inode.length) {
                       ftEnt.inode.length = ftEnt.seekPtr;
                   }
               }

               ftEnt.inode.toDisk(ftEnt.iNumber);
               return var4;
           }
       }
   }
/*
    public int write( FileTableEntry ftEnt, byte[] buffer )
    {
        if (ftEnt.mode == "r") {
            return -1;
        } else {
            synchronized(ftEnt) {
                int startPtr = 0;
                int bufferIndex = buffer.length;

                while(bufferIndex > 0) {
                    int targetBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                    if (targetBlock == -1) {
                        short nextBlock = (short)this.superblock.nextBlock();
                        switch(ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, nextBlock)) {
                            case -3:
                                short var8 = (short)this.superblock.nextBlock();
                                if (!ftEnt.inode.registerIndexBlock(var8)) {
                                    SysLib.cerr("ThreadOS: panic on write\n");
                                    return -1;
                                }

                                if (ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, nextBlock) != 0) {
                                    SysLib.cerr("ThreadOS: panic on write\n");
                                    return -1;
                                }
                            case 0:
                            default:
                                targetBlock = nextBlock;
                                break;
                            case -2:
                            case -1:
                                SysLib.cerr("ThreadOS: filesystem panic on write\n");
                                return -1;
                        }
                    }

                    byte[] readBuffer = new byte[512];
                    if (SysLib.rawread(targetBlock, readBuffer) == -1) {
                        System.exit(2);
                    }

                    int blockPtr = ftEnt.seekPtr % 512;
                    int blockOffset = 512 - blockPtr;
                    int length = Math.min(blockOffset, bufferIndex);
                    System.arraycopy(readBuffer, startPtr, readBuffer, blockPtr, length);
                    SysLib.rawwrite(targetBlock, readBuffer);
                    ftEnt.seekPtr += length;
                    startPtr += length;
                    bufferIndex -= length;
                    if (ftEnt.seekPtr > ftEnt.inode.length) {
                        ftEnt.inode.length = ftEnt.seekPtr;
                    }
                }

                ftEnt.inode.toDisk(ftEnt.iNumber);
                return startPtr;
            }
        }
    }
   */


   private boolean deallocAllBlocks( FileTableEntry ftEnt )
   {
       if (ftEnt.inode.count != 1) {
           return false;
       } else {
           byte[] var2 = ftEnt.inode.unregisterIndexBlock();
           if (var2 != null) {
               byte var3 = 0;

               short var4;
               while((var4 = SysLib.bytes2short(var2, var3)) != -1) {
                   this.superblock.returnBlock(var4);
               }
           }

           int var5 = 0;

           while(true) {
               Inode var10001 = ftEnt.inode;
               if (var5 >= 11) {
                   ftEnt.inode.toDisk(ftEnt.iNumber);
                   return true;
               }

               if (ftEnt.inode.direct[var5] != -1) {
                   this.superblock.returnBlock(ftEnt.inode.direct[var5]);
                   ftEnt.inode.direct[var5] = -1;
               }

               ++var5;
           }
       }
   }

   public boolean delete( String fileName )
   {
       FileTableEntry var2 = this.open(fileName, "w");
       short var3 = var2.iNumber;
       return this.close(var2) && this.directory.ifree(var3);
   }

   private final int SEEK_SET = 0;
   private final int SEEK_CUR = 1;
   private final int SEEK_END = 2;

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
