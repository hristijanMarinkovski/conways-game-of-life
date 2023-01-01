import java.util.concurrent.CountDownLatch;

public class CountAndUpdateThread implements Runnable{

    private int[][] matrix;
    private int[][] matrixUpd;
    private int[][] neighbours;
    private final int[][] directions = new int[][]{{-1,-1}, {-1,0}, {-1,1},  {0,1}, {1,1},  {1,0},  {1,-1},  {0, -1}};
    private final int startRow;
    private final int endRow;
    private CountDownLatch latch;

    public CountAndUpdateThread(int[][] matrix,
                                int[][] matrixUpd,
                                int[][] neighbours,
                                int startRow,
                                int endRow,
                                CountDownLatch latch) {
        this.matrix = matrix;
        this.matrixUpd = matrixUpd;
        this.neighbours = neighbours;
        this.startRow = startRow;
        this.endRow = endRow;
        this.latch = latch;
    }

    @Override
    public void run() {
        // this gives us amount of elements surrounding each cell
        int rows = matrix.length, cols = matrix[0].length;
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < cols; j++) {
                for (int[] direction : directions) {
                    int ci = i + direction[0];
                    int cj = j + direction[1];
                    if (ci >= 0 && ci < rows) {
                        if (cj >= 0 && cj < cols) {
                            if (matrix[ci][cj] == 1) {
                                neighbours[i][j]++;
                            }
                        }
                    }
                }
            }
        }

        // now we update the matrix in order to game rules
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if ((neighbours[i][j] == 2 || neighbours[i][j] == 3) && matrix[i][j] == 1) {
                    matrixUpd[i][j] = 1; // cell survives
                } else if (neighbours[i][j] == 3 && matrix[i][j] == 0) {
                    matrixUpd[i][j] = 1; // cell becomes alive
                } else {
                    matrixUpd[i][j] = 0; // cell stays dead
                }
            }
        }

        latch.countDown();
    }
}
