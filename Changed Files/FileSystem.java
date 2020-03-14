public class FileSystem {
   private SuperBlock superblock;
   private Directory directory;
   private FileTable filetable;

   public FileSystem( int diskBlocks ) {
      // create superblock, and format disk with 64 inodes in default
      superblock = new SuperBlock( diskBlocks );

      // create directory, and register "/" in directory entry 0
      directory = new Directory( superblock.inodeBlocks );

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
       this.directory = new Directory(this.superblock.inodeBlocks);
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

   }

   private boolean deallocAllBlocks( FileTableEntry ftEnt )
   {

   }

   public boolean delete( String filename )
   {

   }

   private final int SEEK_SET = 0;
   private final int SEEK_CUR = 1;
   private final int SEEK_END = 2;

   public int seek( FileTableEntry ftEnt, int offset, int whence )
   {

   }
}
