import java.util.Vector;

public class FileTable {

    private Vector table; // the actual entity of this file table
    private Directory dir; // the root directory 

    public FileTable(Directory directory) { // constructor
        table = new Vector(); // instantiate a file (structure) table
        dir = directory; // receive a reference to the Director
    } // from the file system

    // major public methods
    public synchronized FileTableEntry falloc3(String fileName, String mode) {
        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry

        Inode newInode = null;

        short iNumber = 0;

        if (fileName.equals("/")) { // root
            iNumber = 0;
        } else {
            iNumber = this.dir.namei(fileName); // get iNumber from directory
        }

        while (true) {
            if (iNumber >= 0) {
                newInode = new Inode(iNumber);
                if (mode.equals("r")) {
                    if (newInode.flag == 0)
                        newInode.flag = 1; // if unused, change flag

                    else if (newInode.flag != 1) { // if not being read or written to?
                        try {
                            this.wait();
                        } catch (InterruptedException var7) {;
                        }
                        continue;
                    }
                    break;
                }

                if (newInode.flag != 0 && newInode.flag != 3) {
                    if (newInode.flag == 1 || newInode.flag == 2) {
                        newInode.flag = (short)(newInode.flag + 3);
                        newInode.toDisk(iNumber);
                    }

                    try {
                        this.wait();
                    } catch (InterruptedException var6) {;
                    }
                    continue;
                }

                newInode.flag = 2;
                break;
            }

            if (mode.equals("r")) {
                return null;
            }

            iNumber = this.dir.ialloc(fileName);
            newInode = new Inode();
            newInode.flag = 2;
            break;
        }

        newInode.count++;

        newInode.toDisk(iNumber);

        FileTableEntry newEntry = new FileTableEntry(newInode, iNumber, mode);
        this.table.addElement(newEntry);

        return newEntry;
    }

    public synchronized FileTableEntry falloc(String fileName, String mode) {
        Inode newINode = null;

        short iNumber;
        while(true) {
            if (fileName.equals("/")) {
                iNumber = 0;
            } else {
                iNumber = this.dir.namei(fileName);
            }

            if (iNumber >= 0) {
                newINode = new Inode(iNumber);
                if (mode.compareTo("r") == 0) {
                    if (newINode.flag != 0 && newINode.flag != 1) {
                        try {
                            this.wait();
                        } catch (InterruptedException var7) {
                            ;
                        }
                        continue;
                    }

                    newINode.flag = 1;
                    break;
                }

                if (newINode.flag != 0 && newINode.flag != 3) {
                    if (newINode.flag == 1 || newINode.flag == 2) {
                        newINode.flag = (short)(newINode.flag + 3);
                        newINode.toDisk(iNumber);
                    }

                    try {
                        this.wait();
                    } catch (InterruptedException var6) {
                        ;
                    }
                    continue;
                }

                newINode.flag = 2;
                break;
            }

            if (mode.compareTo("r") == 0) {
                return null;
            }

            iNumber = this.dir.ialloc(fileName);
            newINode = new Inode();
            newINode.flag = 2;
            break;
        }

        ++newINode.count;

        //Todo
        if(iNumber == -1)
        {
            iNumber = 3;
        }

        newINode.toDisk(iNumber);
        FileTableEntry var5 = new FileTableEntry(newINode, iNumber, mode);
        this.table.addElement(var5);
        return var5;
    }





























    public synchronized boolean ffree(FileTableEntry e) {
        // receive a file table entry reference
        // save the corresponding inode to the disk
        // free this file table entry.
        // return true if this file table entry found in my table

        if (this.table.removeElement(e)) {
            e.inode.count--; //remove inode
            e.inode.flag = 0;
            e.inode.toDisk(e.iNumber);
            e = null;
            this.notify();
            return true;
        }
        return false;
    }

    public synchronized boolean fempty() {
        return table.isEmpty(); // return if table is empty 
    } // should be called before starting a format
}