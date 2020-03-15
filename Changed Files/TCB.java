/*******************************************************************************
 * TCB
 *
 * Thread Control Block - modified and commented for final assignment
 ******************************************************************************/
public class TCB {
    private Thread thread = null;
    private int tid = 0;
    private int pid = 0;
    private boolean terminated = false;
    private int sleepTime = 0;
    public FileTableEntry[] ftEnt = null; // added for the file system

	/***************************************************************************
	 * TCB Constructor
	 *
	 * Takes in a thread, thread ID, and the thread's parent
	 **************************************************************************/
    public TCB( Thread newThread, int myTid, int parentTid )
	{
		thread = newThread;
		tid = myTid;
		pid = parentTid;
		terminated = false;

		ftEnt = new FileTableEntry[32];    // added for the file system

		for ( int i = 0; i < 32; i++ )
			 ftEnt[i] = null;         // all entries initialized to null
			 // fd[0], fd[1], and fd[2] are kept null.

		System.err.println( "threadOS: a new thread (thread=" + thread +
					" tid=" + tid +
					" pid=" + pid + ")");
    }

	/***************************************************************************
	 * getThread
	 **************************************************************************/
    public synchronized Thread getThread( ) {
	return thread;
    }

	/***************************************************************************
	 * getTid
	 **************************************************************************/
    public synchronized int getTid( ) {
	return tid;
    }

	/***************************************************************************
	 * getPid
	 **************************************************************************/
    public synchronized int getPid( ) {
	return pid;
    }

	/***************************************************************************
	 * setTerminated
	 **************************************************************************/
    public synchronized boolean setTerminated( )
	{
		terminated = true;
		return terminated;
    }

	/***************************************************************************
	 * getTerminated
	 **************************************************************************/
    public synchronized boolean getTerminated( ) {
	return terminated;
    }

	/***************************************************************************
	 * getFd
	 *
	 * Gets the thread's file descriptor
	 *
	 * • added for the file system
	 **************************************************************************/
    public synchronized int getFd( FileTableEntry entry )
	{
		if ( entry == null )
			return -1;
		for ( int i = 3; i < 32; i++ ) {
			if ( ftEnt[i] == null ) {
			ftEnt[i] = entry;
			return i;
			}
		}
		return -1;
    }

	/***************************************************************************
	 * returnFd
	 *
	 * Returns the threads' FileTableEntry
	 *
	 * • added for the file system
	 **************************************************************************/
    public synchronized FileTableEntry returnFd( int fd )
	{
		if ( fd >= 3 && fd < 32 ) {
			FileTableEntry oldEnt = ftEnt[fd];
			ftEnt[fd] = null;
			return oldEnt;
		}
		else
			return null;
    }

	/***************************************************************************
	 * getFtEnt
	 *
	 * Gets the file table entry
	 *
	 * • added for the file system
	 **************************************************************************/
    public synchronized FileTableEntry getFtEnt( int fd )
	{
		if ( fd >= 3 && fd < 32 )
			return ftEnt[fd];
		else
			return null;
    }
}
