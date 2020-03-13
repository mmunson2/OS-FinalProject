class Superblock {
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head
   
   public SuperBlock( int diskSize ) {
        byte[] superBlock = new byte[Disk.blockSize];
	short seek = 0; // offset variable
        SysLib.rawread(0, superBlock); //read first block to superBlock
        totalBlocks = SysLib.bytes2int(superBlock, seek);
	seek += Integer.SIZE;
        inodeBlocks = SysLib.bytes2int(superBlock, seek);
	seek += Integer.SIZE;
        freeList = SysLib.bytes2int(superBlock, seek);

        if (totalBlocks == diskSize 
	&& inodeBlocks > 0 
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

        for(int i = 0; i < totalInode; i++) {
            Inode newInode = new Inode();
            newInode.flag = 0;
            newInode.toDisk((short)i);
        }

        freeList = Short.SIZE + (totalInode * 32);
	freeList /= Disk.blockSize;

        for(int i = freeList; i < totalBlocks; i++) {
            byte[] data = new byte[Disk.blockSize];

            for(int j = 0; j < Disk.blockSize; j++) {
                data[j] = 0;
            }

            SysLib.int2bytes(i + 1, data, 0);
            SysLib.rawwrite(i, data);
        }

        sync();
    }

    public void sync() {
        byte[] superBlock = new byte[Disk.blockSize];

	short seek = 0; // offset variable

        SysLib.int2bytes(totalBlocks, block, seek);
	seek += Integer.SIZE;
        SysLib.int2bytes(totalInodes, block, seek);
	seek += Integer.SIZE;
        SysLib.int2bytes(freeList, block, seek);

        SysLib.rawwrite(0, block);
    }

    public int nextBlock() {
	if (freeList <= 0 || freeList > totalBlocks)
		return -1;

        int free = freeList;

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(free, data); 

        freeList = SysLib.bytes2int(data, 0);

        SysLib.short2bytes((short)0, data, 0); 
        SysLib.rawwrite(free, data);
      
        return free;
    }

    public int returnBlock(int block) {
        if (block < 0) 
		return false;
	
        byte[] data = new byte[Disk.blockSize];

	for (int i = 0; i < Disk.blockSize; i++) {
            data[i] = 0;
        }

        SysLib.short2bytes((short)0, data, 0);

        SysLib.rawwrite(block, data);

        freeList = block; // setting freelist to this block
        return true;
      
    }	
}