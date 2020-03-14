/*******************************************************************************
 * Test3
 *
 * A test thread that compares the performance between CPU and IO bound
 * processes.
 *
 ******************************************************************************/
class Test3 extends Thread {

    private int threadPair;

    public Test3(String[] var1) {

        this.threadPair = Integer.parseInt(var1[0]);
    }

    public void run() {

        System.out.println("--- Starting Test3 ---");

        long beginTime = System.nanoTime() / 1000000;

        for(int i = 0; i < this.threadPair; ++i) {
            SysLib.exec(SysLib.stringToArgs("TestThread3a"));
            SysLib.exec(SysLib.stringToArgs("TestThread3b"));
        }

        for(int i = 0; i < 2 * this.threadPair; ++i) {
            SysLib.join();
        }

        long endTime = System.nanoTime() / 1000000;
        SysLib.cout("Time Elapsed = " + (endTime - beginTime) + " msec.\n");
        SysLib.exit();
    }
}

