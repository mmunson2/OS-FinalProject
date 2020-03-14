/*******************************************************************************
 * TestThread3a
 *
 * A Thread that performs a large amount of numerical computations
 *
 ******************************************************************************/
public class TestThread3a extends Thread {
    public void run() {

        int[][] array1 = new int[101][101];
        int[][] array2 = new int[101][101];

        for (int i = 1; i < 100; i++) {
            for (int j = 1; j < 100; j++)
            {
                array1[i][j] = i;
                array2[i][j] = j;
            }
        }

        for (int i = 1; i < 100; i++) {
            for (int j = 1; j < 100; j++)
            {
                array1[i][j] /= array2[i][j];
            }
        }

        SysLib.exit();
    }

}