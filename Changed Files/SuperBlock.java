/*******************************************************************************
 * SuperBlock
 *
 * The first block in memory containing pointers to all others
 ******************************************************************************/
class SuperBlock {
   private int totalBlocks; // the number of disk blocks
   int totalInodes; // the number of inodes
   private int freeList;    // the block number of the free list's head

    /***************************************************************************
     * Superblock Constructor
     *
     * Takes in the size of the disk
     **************************************************************************/
   SuperBlock( int diskSize )
   {
        byte[] superBlock = new byte[Disk.blockSize];
	    short seek = 0; // offset variable
        //read first block to superBlock
        SysLib.rawread(0, superBlock);
        this.totalBlocks = SysLib.bytes2int(superBlock, seek);
	    seek += Integer.SIZE / Byte.SIZE;
        this.totalInodes = SysLib.bytes2int(superBlock, seek);
	    seek += Integer.SIZE / Byte.SIZE;
        this.freeList = SysLib.bytes2int(superBlock, seek);

        if (this.totalBlocks != diskSize || this.totalInodes < 0
                || this.freeList <= 2)
        {
            this.totalBlocks = diskSize;
            format(64);
        }
   }


    /***************************************************************************
     * format
     *
     * formats the superblock
     **************************************************************************/
    void format(int num)
    {

        this.totalInodes = num;

        for(int i = 0; i < this.totalInodes; i++)
        {
            Inode newInode = new Inode();
            newInode.flag = 0;
            newInode.toDisk((short)i);
        }

        this.freeList = 2 + this.totalInodes * 32 / 512;

        for(int i = this.freeList; i < this.totalBlocks; i++)
        {
            byte[] data = new byte[Disk.blockSize];

            for(int j = 0; j < Disk.blockSize; j++)
            {
                data[j] = 0;
            }

            SysLib.int2bytes(i + 1, data, 0);
            SysLib.rawwrite(i, data);
        }

        printFreeList();

        syncBlock();
    }

    /***************************************************************************
     * getNextBlock
     *
     * Returns the next free block
     **************************************************************************/
    int getNextBlock()
    {
	    if (this.freeList <= 0 || this.freeList > this.totalBlocks)
		    return -1;

        int free = this.freeList;

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(free, data); 

        this.freeList = SysLib.bytes2int(data, 0);

        SysLib.short2bytes((short)0, data, 0); 
        SysLib.rawwrite(free, data);

        printFreeList();

        return free;
    }

    /***************************************************************************
     * setBlock
     *
     * Sets a block to a given number
     **************************************************************************/
    boolean setBlock(int block)
    {
        if (block < 0) 
		    return false;
	
        byte[] data = new byte[Disk.blockSize];

        for (int i = 0; i < Disk.blockSize; i++)
        {
            data[i] = 0;
        }

        SysLib.short2bytes((short)0, data, 0);

        SysLib.rawwrite(block, data);

        this.freeList = block; // setting freelist to this block

        printFreeList();

        return true;
    }

    /***************************************************************************
     * syncBlock
     *
     * Syncs blocks between threads
     **************************************************************************/
    void syncBlock()
    {
        byte[] superBlock = new byte[Disk.blockSize];

        short seek = 0; // offset variable

        SysLib.int2bytes(this.totalBlocks, superBlock, seek);
        seek += Integer.SIZE / Byte.SIZE;
        SysLib.int2bytes(this.totalInodes, superBlock, seek);
        seek += Integer.SIZE / Byte.SIZE;
        SysLib.int2bytes(this.freeList, superBlock, seek);

        SysLib.rawwrite(0, superBlock);
    }

    /***************************************************************************
     * printFreeList
     *
     * Debug method in an attempt to resolve test18
     **************************************************************************/
    private void printFreeList()
    {
        //System.out.println("\nFreeList: " + this.freeList + "\n");
    }


}