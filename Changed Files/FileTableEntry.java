/*******************************************************************************
 * FileTableEntry
 *
 * An entry in the file table
 ******************************************************************************/
class FileTableEntry {  // Each table entry should have
    int seekPtr;        //    a file seek pointer
    final Inode inode;  //    a reference to an inode
    final short iNumber;//    this inode number
    int count;          //    a count to maintain #threads sharing this
    final String mode;  //    "r", "w", "w+", or "a"

    /***************************************************************************
     * FileTableEntry Constructor
     *
     * Takes in an iNode, number, and String representing the name
     **************************************************************************/
    FileTableEntry ( Inode i, short inumber, String m )
    {
        this.seekPtr = 0;     // the seek pointer is set to the file top.
        this.inode = i;
        this.iNumber = inumber;
        this.count = 1;       // at least one thread is using this entry.
        this.mode = m;        // once file access mode is set, it never changes.

        if ( this.mode.compareTo( "a" ) == 0 )
            this.seekPtr = inode.length;
    }
}
