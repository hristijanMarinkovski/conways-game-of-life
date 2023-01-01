import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class RandomizeThread implements Runnable{

    private int[][] matrix;
    private int[][] matrixUpd;
    private final int startRow;
    private final int endRow;
    private CountDownLatch latch;

    public RandomizeThread(int[][] matrix,
                           int[][] matrixUpd,
                           int startRow,
                           int endRow,
                           CountDownLatch latch){
        this.matrix = matrix;
        this.matrixUpd = matrixUpd;
        this.startRow = startRow;
        this.endRow = endRow;
        this.latch = latch;
    }

    @Override
    public void run() {
        int cols = matrix[0].length;
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = new Random().nextInt(2);
                matrixUpd[i][j] = matrix[i][j];
            }
        }
        latch.countDown();
    }
}
