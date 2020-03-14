/*******************************************************************************
 * SyncQueue
 *
 * A monitor that controls thread sleeps and wakes
 *
 ******************************************************************************/
public class SyncQueue
{

    private QueueNode[] queue;


    /***************************************************************************
     * SyncQueue Constructor
     *
     * No-Arg, sets the size of the queue to 10 by default
     *
     **************************************************************************/
    public SyncQueue()
    {
        this(10);
    }


    /***************************************************************************
     * SyncQueue Constructor - Int overload
     *
     * Allows the user to set the size of the queue. Initializes all QueueNode
     * entries in the queue
     *
     **************************************************************************/
    public SyncQueue( int condMax )
    {
        queue = new QueueNode[condMax];

        for(int i = 0; i < condMax; i++)
        {
            queue[i] = new QueueNode();
        }
    }


    /***************************************************************************
     * enqueueAndSleep
     *
     * Checks if the given condition is within the queue size, if not returns
     * -1. Returns the result of the sleep call.
     *
     **************************************************************************/
    public int enqueueAndSleep( int condition)
    {
        if(condition >= 0 && condition <= queue.length)
        {
            return queue[condition].sleep();
        }
        else
        {
            return -1;
        }
    }


    /***************************************************************************
     * dequeueAndWakeup
     *
     * Single int overload, sets tid to zero and calls the main overload
     *
     **************************************************************************/
    public void dequeueAndWakeup(int condition)
    {
        dequeueAndWakeup(condition, 0);
    }


    /***************************************************************************
     * dequeueAndWakeup
     *
     * Checks if the condition is inside the queue, does nothing if not.
     * Returns the result of wakeup(tid)
     *
     **************************************************************************/
    public void dequeueAndWakeup(int condition, int tid)
    {
        if(condition >= 0 && condition <= queue.length)
        {
            queue[condition].wakeUp(tid);
        }
    }




}
