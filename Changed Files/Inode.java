/****************************************************************************
 *Inode Class
 ***************************************************************************/


public class Inode {
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int iNodesPerBlock = 16;
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   /****************************************************************************
    * Inode noArg Constructor
    ***************************************************************************/
   Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   /****************************************************************************
    * Inode Disk Constructor
    ***************************************************************************/
   Inode( short iNumber ) // retrieving inode from disk
   {
      int blockNumber = (iNumber / iNodesPerBlock) + 1;

      byte[] buffer = new byte[512];

      SysLib.rawread(blockNumber, buffer); //Read the entire block into buffer

      int seek = iNumber % iNodesPerBlock * iNodeSize;

      //Read in the file's length (Integer)
      this.length = SysLib.bytes2int(buffer, seek);

      seek += (Integer.SIZE / Byte.SIZE);

      //Read in the file's count (Short)
      this.count = SysLib.bytes2short(buffer, seek);

      seek += (Short.SIZE / Byte.SIZE);

      //Read in the file's flag (Short)
      this.flag = SysLib.bytes2short(buffer, seek);

      seek += (Short.SIZE / Byte.SIZE);


      //Read in the direct pointers
      for(int i = 0; i < directSize; i++)
      {
         this.direct[i] = SysLib.bytes2short(buffer, seek);

         seek += (Short.SIZE / Byte.SIZE);
      }

      //Read in the indirect pointer
      this.indirect = SysLib.bytes2short(buffer, seek);

      seek += (Short.SIZE / Byte.SIZE);
   }

   /****************************************************************************
    * toDisk
    ***************************************************************************/
   int toDisk( short iNumber ) // save to disk as the i-th inode
   {
      int blockNumber = (iNumber / iNodesPerBlock) + 1;

      byte[] buffer = new byte[512];
      SysLib.rawread(blockNumber, buffer); //Read the entire block into buffer
      int seek = iNumber % iNodesPerBlock * iNodeSize;

      SysLib.int2bytes(this.length, buffer, seek);
      seek += (Integer.SIZE / Byte.SIZE);

      SysLib.short2bytes(this.count, buffer, seek);
      seek += (Short.SIZE / Byte.SIZE);

      SysLib.short2bytes(this.flag, buffer, seek);
      seek += (Short.SIZE / Byte.SIZE);

      for(int i = 0; i < directSize; i++)
      {
         SysLib.short2bytes(this.direct[i], buffer, seek);

         seek += (Short.SIZE / Byte.SIZE);
      }

      SysLib.short2bytes(this.indirect, buffer, seek);
      seek += (Short.SIZE / Byte.SIZE);


      SysLib.rawwrite(blockNumber, buffer);
      return 1;
   }


   /****************************************************************************
    * getBlock
    *
    * //Todo
    ***************************************************************************/
   int getBlock(int seekPointer) {
      int var2 = seekPointer / 512;
      if (var2 < 11) {
         return this.direct[var2];
      } else if (this.indirect < 0) {
         return -1;
      } else {
         byte[] var3 = new byte[512];
         SysLib.rawread(this.indirect, var3);
         int var4 = var2 - 11;
         return SysLib.bytes2short(var3, var4 * 2);
      }
   }

   /****************************************************************************
    * makeBlock
    *
    * //Todo
    ***************************************************************************/
   int makeBlock(int seekPtr, short blockNumber) {
      int var3 = seekPtr / 512;
      if (var3 < 11) {
         if (this.direct[var3] >= 0) {
            return -1;
         } else if (var3 > 0 && this.direct[var3 - 1] == -1) {
            return -2;
         } else {
            this.direct[var3] = blockNumber;
            return 0;
         }
      } else if (this.indirect < 0) {
         return -3;
      } else {
         byte[] var4 = new byte[512];
         SysLib.rawread(this.indirect, var4);
         int var5 = var3 - 11;
         if (SysLib.bytes2short(var4, var5 * 2) > 0) {
            SysLib.cerr("indexBlock, indirectNumber = " + var5 + " contents = " + SysLib.bytes2short(var4, var5 * 2) + "\n");
            return -1;
         } else {
            SysLib.short2bytes(blockNumber, var4, var5 * 2);
            SysLib.rawwrite(this.indirect, var4);
            return 0;
         }
      }
   }

   /****************************************************************************
    * makeIndexBlock
    *
    * //Todo
    ***************************************************************************/
   boolean makeIndexBlock(short blockNumber) {
      for(int var2 = 0; var2 < 11; ++var2) {
         if (this.direct[var2] == -1) {
            return false;
         }
      }

      if (this.indirect != -1) {
         return false;
      } else {
         this.indirect = blockNumber;
         byte[] var4 = new byte[512];

         for(int var3 = 0; var3 < 256; ++var3) {
            SysLib.short2bytes((short)-1, var4, var3 * 2);
         }

         SysLib.rawwrite(blockNumber, var4);
         return true;
      }
   }


   /****************************************************************************
    * deleteIndexBlock
    *
    * //Todo
    ***************************************************************************/
   byte[] deleteIndexBlock() {
      if (this.indirect >= 0) {
         byte[] var1 = new byte[512];
         SysLib.rawread(this.indirect, var1);
         this.indirect = -1;
         return var1;
      } else {
         return null;
      }
   }

}