import java.util.Vector;

/*******************************************************************************
 *
 ******************************************************************************/
public class FileTable {

    private Vector table; // the actual entity of this file table
    private Directory dir; // the root directory 

    /***************************************************************************
     *
     **************************************************************************/
    public FileTable(Directory directory) { // constructor
        table = new Vector(); // instantiate a file (structure) table
        dir = directory; // receive a reference to the Director
    } // from the file system

    // major public methods
    /***************************************************************************
     *
     **************************************************************************/
    public synchronized FileTableEntry falloc( String filename, String mode )
    {
        Inode inode = null;
        short iNumber = -1;
        while( true )
        {
            iNumber = ( filename.equals( "/" ) ? 0 : dir.namei( filename ) );
            if( iNumber >= 0 )
            {
                // Loads the existing inode from disk
                // into memory, for opening existing file
                inode = new Inode( iNumber );
                if( mode.equals( "r" ) )
                {
                    // file is available for reading?
                    if( inode.flag == 0 || inode.flag == 1 )
                    {
                        inode.flag = 1;
                        break;
                    }
                    else if( inode.flag == 2 )//being written
                        try
                        {
                            wait( );
                        }
                        catch( InterruptedException ie )
                        {
                            return null;
                        }
                    inode.flag = 1;
                }
                else if( mode.equals( "w" )
                        || mode.equals( "w+" )
                        || mode.equals( "a" ) )
                {
                    // file is available for writing?
                    if( inode.flag == 0 )
                    {
                        inode.flag = 2;
                        break;
                    }

                    if( inode.flag == 1 || inode.flag == 2 )
                        try
                        {
                            wait( );
                        }
                        catch( InterruptedException ie )
                        {
                            return null;
                        }

                    inode.flag = 2;
                }
            }
            else
            {
                // file does not exist to read!
                if( mode.equals( "r" ) )
                    return null;

                inode = new Inode( );
                iNumber = dir.ialloc( filename );

                // No more room for additional files/inodes?
                if( iNumber == -1 )
                    return null;
            }
        }

        inode.count++;

        inode.toDisk( iNumber );

        FileTableEntry fEnt = new FileTableEntry( inode, iNumber, mode );

        this.table.add( fEnt );

        return fEnt;

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