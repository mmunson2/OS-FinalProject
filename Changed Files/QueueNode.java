import java.util.Vector;

/*******************************************************************************
 * QueueNode
 *
 * A helper class for the SyncQueue class. Stores a vector of pids and
 * allows a process to sleep or wake
 *
 ******************************************************************************/
public class QueueNode
{
    private Vector pids = null;

    /***************************************************************************
     * QueueNode Constructor
     *
     *
     **************************************************************************/
    QueueNode()
    {
        pids = new Vector();
    }

    /***************************************************************************
     * Sleep
     *
     * Calls wait if the pid list is empty. Returns index 0 from the pid
     * vector
     *
     **************************************************************************/
    public synchronized int sleep()
    {
        try
        {
            if(pids.isEmpty())
                wait();
        }
        catch(InterruptedException e)
        {
            /* Do nothing */
        }

        return (int)pids.remove(0);
    }


    /***************************************************************************
     * wakeUp
     *
     * Adds a pid to the end of the pids vector. Notifies one thread
     *
     **************************************************************************/
    public synchronized void wakeUp(int pid)
    {
        pids.add(pids.size(), pid);
        notify();
    }



}
