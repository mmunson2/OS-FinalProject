public class CustomTest extends Thread
{
    private int fd;



    public void run()
    {
        //Tests fsize

        if( test1() )
        {
            SysLib.cout("Passed Test1!\n");
        }
        else
        {
            SysLib.cout("Test1 Failed");
        }

        if( test2() )
        {
            SysLib.cout("Passed Test2!");
        }
        else
        {
            SysLib.cout("Test2 Failed");
        }

    }

    //Test that fSize is 0 for empty file
    public boolean test1()
    {
        fd = SysLib.open( "css430", "w+" );
        if ( fd != 3 ) {
            SysLib.cout( "fd = " + fd + " (wrong)\n" );
            return false;
        }
        if ( SysLib.fsize(fd) != 0 ) {
            SysLib.cout( "fsize = " + SysLib.fsize(fd) + " (wrong)\n" );
            return false;
        }

        return true;
    }

    //Test that fSize is correct for filesize 16
    public boolean test2()
    {

        byte[] buffer = new byte[16];
        int result;

        for ( byte i = 0; i < 16; i++ )
        {
            buffer[i] = i;
        }

        SysLib.write( fd, buffer );

        if ( SysLib.fsize(fd) != 16  ) {
            SysLib.cout( "fsize = " + SysLib.fsize(fd) + " (wrong)\n" );
            return false;
        }

        return true;
    }



}
