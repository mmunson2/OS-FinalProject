/*******************************************************************************
 * TestThread3b
 *
 * A test thread that performs a large amount of disk accesses
 *
 ******************************************************************************/
import java.util.Random;

public class TestThread3b extends Thread {
    public void run() {

        byte[] data = new byte[512];

        Random random = new Random();

        java.util.Arrays.fill(data, (byte) 1);

        for (int i = 0; i < 100; i++) {


            SysLib.rawwrite(random.nextInt(1000), data);


            SysLib.rawread(random.nextInt(1000), data);
        }


        SysLib.exit();
    }
}
