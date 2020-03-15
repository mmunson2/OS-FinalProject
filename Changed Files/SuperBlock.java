class SuperBlock {
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head

   public SuperBlock( int diskSize ) {
        byte[] superBlock = new byte[Disk.blockSize];
	short seek = 0; // offset variable
        SysLib.rawread(0, superBlock); //read first block to superBlock
        totalBlocks = SysLib.bytes2int(superBlock, seek);
	seek += Integer.SIZE / Byte.SIZE;
        this.totalInodes = SysLib.bytes2int(superBlock, seek);
	seek += Integer.SIZE / Byte.SIZE;
        freeList = SysLib.bytes2int(superBlock, seek);

        if (totalBlocks == diskSize 
	&& this.totalInodes > 0
	&& freeList >= 2) {
            return;
        }
	else{
        totalBlocks = diskSize;
        format(64);
	}
   }


    public void format(int num) {

        totalInodes = num;

        for(int i = 0; i < totalInodes; i++) {
            Inode newInode = new Inode();
            newInode.flag = 0;
            newInode.toDisk((short)i);
        }

        //freeList = (Short.SIZE / Byte.SIZE) + (totalInodes * 32);
	    //freeList /= Disk.blockSize;

        this.freeList = 2 + this.totalInodes * 32 / 512;

        for(int i = freeList; i < totalBlocks; i++) {
            byte[] data = new byte[Disk.blockSize];

            for(int j = 0; j < Disk.blockSize; j++) {
                data[j] = 0;
            }

            SysLib.int2bytes(i + 1, data, 0);
            SysLib.rawwrite(i, data);
        }

        printFreeList();

        syncBlock();
    }

    public void syncBlock() {
        byte[] superBlock = new byte[Disk.blockSize];

	short seek = 0; // offset variable

        SysLib.int2bytes(totalBlocks, superBlock, seek);
	seek += Integer.SIZE / Byte.SIZE;
        SysLib.int2bytes(totalInodes, superBlock, seek);
	seek += Integer.SIZE / Byte.SIZE;
        SysLib.int2bytes(freeList, superBlock, seek);

        SysLib.rawwrite(0, superBlock);
    }

    public int getNextBlock() {
	if (freeList <= 0 || freeList > totalBlocks)
		return -1;

        int free = freeList;

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(free, data); 

        freeList = SysLib.bytes2int(data, 0);

        SysLib.short2bytes((short)0, data, 0); 
        SysLib.rawwrite(free, data);

        printFreeList();

        return free;
    }

    public boolean setBlock(int block) {
        if (block < 0) 
		    return false;
	
        byte[] data = new byte[Disk.blockSize];

	for (int i = 0; i < Disk.blockSize; i++) {
            data[i] = 0;
        }

        SysLib.short2bytes((short)0, data, 0);

        SysLib.rawwrite(block, data);

        freeList = block; // setting freelist to this block

        printFreeList();

        return true;
    }

    public void printFreeList()
    {
        //System.out.println("\nFreeList: " + this.freeList + "\n");
    }


}