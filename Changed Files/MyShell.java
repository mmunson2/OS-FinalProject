

import java.io.*;
import java.util.*;


public class MyShell extends Thread
{
    private String cmdLine;

    public MyShell()
    {
       cmdLine = "";

    }


    public void run()
    {
        SysLib.cout("My Shell!");

        cmdLine = "PingPong abc 100";
        String[] args = SysLib.stringToArgs(cmdLine);
        int tid = SysLib.exec(args);

        SysLib.cout("started thread tid = " + tid + "\n");
        SysLib.join();


        SysLib.exit();
    }



}
