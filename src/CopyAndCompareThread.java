import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CopyAndCompareThread implements Runnable {

	private int[][] matrix;
	private int[][] matrixUpd;
	private int[][] neighbours;
	private int startRow;
	private int endRow;
	private AtomicBoolean same;
	private boolean localSame;
	private CountDownLatch latch;

	public CopyAndCompareThread(int[][] matrix,
								int[][] matrixUpd,
								int[][] neighbours,
								int startRow,
								int endRow,
								AtomicBoolean same,
								CountDownLatch latch) {
		this.matrix = matrix;
		this.matrixUpd = matrixUpd;
		this.neighbours = neighbours;
		this.startRow = startRow;
		this.endRow = endRow;
		this.same = same;
		this.localSame = true;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		// copies matrixUpd into matrix, resets neighbours and checks if block has been updated
		for (int i = startRow; i < endRow; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] != matrixUpd[i][j]) {
					localSame = false; // this is to avoid constant locks on the atomic boolean harming performance
				}
				matrix[i][j] = matrixUpd[i][j];
				neighbours[i][j] = 0;
			}
		}


		if(!localSame){
			same.set(false);
		}

		latch.countDown();
	}
}
