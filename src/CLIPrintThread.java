import java.util.concurrent.CountDownLatch;

public class CLIPrintThread implements Runnable{

    private int[][] matrixUpd;
    private CountDownLatch latch;
    private int startRow;
    private int endRow;

    // this is wrong, replace with stringbuilder
    public CLIPrintThread(int[][] matrixUpd, CountDownLatch latch, int startRow, int endRow) {
        this.matrixUpd = matrixUpd;
        this.latch = latch;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        int cols = matrixUpd[0].length;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < cols; j++) {
                if(matrixUpd[i][j] == 1) {
                    System.out.print("*  ");
                } else {
                    System.out.print(".  ");
                }
            }
            System.out.println("|");
        }

        latch.countDown();
    }
}
