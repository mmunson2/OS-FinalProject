/***************************************************************************
 * Anemone Shell
 *
 * @author Matthew Munson
 * Date: 1/25/19
 *
 * This is the Shell (I named it Anemone) for my ThreadOS Operating
 * System. It can run multiple commands concurrently or sequentially.
 * Implementation consists of one public method and two private helper
 * methods.
 *
 **************************************************************************/

public class Shell extends Thread{

    int commandCount;

    public Shell()
    {
        commandCount = 0;
    }

    /***************************************************************************
     * Run
     *
     * Required ThreadOS call. Prints boilerplate info and the command counter.
     * Command counter is set to increment even when no command has been
     * entered. When a nonempty string is entered, processCommandSet is
     * called.
     *
     **************************************************************************/
    public void run()
    {
        String cmdLine = "";
        StringBuffer input;
        boolean exit = false;

        while(true)
        {
            input = new StringBuffer();

            SysLib.cout("Anemone[" + commandCount + "]: ");

            SysLib.cin(input);

            cmdLine = input.toString();

            if(!cmdLine.equals(""))
            {
                exit = processCommandSet(cmdLine);
            }
            else
            {
                commandCount++;
            }

            if(exit)
                break;
        }
    }

    /***************************************************************************
     * processCommandSet
     *
     *  Helper method for breaking the command line String, which may consist
     *  of multiple commands, into individual commands and arguments. The
     *  '&' symbol separates two commands and tells the shell to run them
     *  concurrently. The ';' symbol separates two commands and tells the
     *  shell to run them sequentially.
     *
     *  The last command in the line is treated slightly differently. If
     *  it's the only command entered, it's run as a blocking call. If the
     *  previous command was run in sequence it's also run as a blocking call.
     *  If the previous command was run in parallel, the final call is also
     *  run in parallel.
     *
     **************************************************************************/
    private boolean processCommandSet(String cmdLine)
    {
        String[] commands = SysLib.stringToArgs(cmdLine);

        int commandStartIndex = 0;
        boolean exit = false;

        for(int i = 0; i < commands.length; i++)
        {
            if(commands[i].equals(";") || commands[i].equals("&"))
            {
                String[] arguments = new String[i - commandStartIndex];

                //Can't use System.ArrayCopy =(
                for(int j = 0; j < arguments.length; j++)
                {
                    arguments[j] = commands[j + commandStartIndex];
                }

                processCommand(arguments, commands[i]);

                commandStartIndex = i + 1;
            }
            else if(i == commands.length - 1)
            {
                String[] arguments = new String[i - commandStartIndex + 1];
                
                for(int j = 0; j < arguments.length; j++)
                {
                    arguments[j] = commands[j + commandStartIndex];
                }

                //The final command always issues a blocking call
                exit = processCommand(arguments, ";");
            }

            if(exit)
                return true;
        }

        return false;
    }



    /***************************************************************************
     * processCommand
     *
     * Helper method for running individual commands. Tracks whether the
     * command contains "exit" and acts accordingly. Commands ordered to
     * run sequentially enter a blocking while loop until the operating
     * system informs the thread that the child has exited.
     *
     **************************************************************************/
    private boolean processCommand(String[] command, String order)
    {
        int returnCode;


        if(command[0].equalsIgnoreCase("exit"))
        {
            SysLib.exit();
            return true;
        }
        else if(order.equals("&"))
        {
            returnCode = SysLib.exec(command);
        }
        else if(order.equals(";"))
        {
            returnCode = SysLib.exec(command);

            while(SysLib.join() != returnCode)
            {}
        }

        commandCount++;
        return false;
    }
}
