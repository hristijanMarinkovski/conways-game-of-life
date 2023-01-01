import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConwaysGameOfLife {
	
	//**********************************************************************************//
	//                           @Author: Hristijan Marinkovski                         //
	//      Sequential and parallel solutions to Conway's Game of Life problem.         //
	//                              And gui just for fun :)                             //
	//**********************************************************************************//
	
	public static void main(String[] args) throws InterruptedException {
		run(1000, 50, 100, RunType.SEQUENTIAL);
	}

	public static void run(int n, int m, int numbOfIterations, RunType runType) throws InterruptedException {
		if(runType == RunType.SEQUENTIAL) {
			runSequential(n, m, numbOfIterations);
		} else if(runType == RunType.PARALLEL){
			runParallel(n, m, numbOfIterations);
		} else {
			runDistributed(n, m, numbOfIterations);
		}
	}

	public static void runSequential(int n, int m, int numbOfIterations) {

		long startTime = System.nanoTime();

		int[][] matrix = new int[n][m];
		int[][] matrixUpd = new int[n][m];
		int[][] directions = new int[][]{{-1,-1}, {-1,0}, {-1,1},  {0,1}, {1,1},  {1,0},  {1,-1},  {0, -1}};
		int[][] neighbours = new int[n][m];
		boolean same = true, firstIter = true;

		// randomize matrix
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = new Random().nextInt(2);
				matrixUpd[i][j] = matrix[i][j];
			}
		}

		while(true) {

			/* prints matrix, gui for now
			for (int i = 0; i < matrixUpd.length; i++) {
				for (int j = 0; j < matrixUpd[0].length; j++) {
					if(matrixUpd[i][j] == 1) {
						System.out.print("* ");
					} else {
						System.out.print(". ");
					}
				}
				System.out.println();
			}
			System.out.println();
			*/

			// if the number of desired iterations is reached, just stop the program
			if(numbOfIterations == 0) {
				System.out.println("You've reached the desired amount of iterations");
				break;
			}

			if(!firstIter) {
				// copy matrixUpd into matrix, reset neighbours and check if matrix has updated
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < m; j++) {
						if(matrix[i][j] != matrixUpd[i][j]) {
							same = false;
						}
						matrix[i][j] = matrixUpd[i][j];
						neighbours[i][j] = 0;
					}
				}

				// if the matrix stopped updating, just stop the program since no changes are occurring
				if(same) {
					System.out.println("Matrix stopped updating");
					break;
				}
				same = true;
			} else {
				firstIter = false;
			}

			// this gives us amount of elements surrounding each cell
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					for (int[] direction : directions) {
						int ci = i + direction[0];
						int cj = j + direction[1];
						if(ci >=0 && ci < matrix.length) {
							if(cj >= 0 && cj < matrix[0].length) {
								if(matrix[ci][cj] == 1 ) {
									neighbours[i][j]++;
								}
							}
						}
					}
				}
			}

			// now we update the matrix in order to game rules
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					if ((neighbours[i][j]==2 || neighbours[i][j]==3) && matrix[i][j]==1) {
						matrixUpd[i][j] = 1; // cell survives
					} else if (neighbours[i][j]==3 && matrix[i][j]==0) {
						matrixUpd[i][j] = 1; // cell becomes alive
					} else {
						matrixUpd[i][j] = 0; // cell stays dead
					}
				}
			}

			numbOfIterations--;
		}

		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;

		System.out.println("The run took: ~"+ timeElapsed/1000000 + " ms");

	}

	public static void runParallel(int n, int m, int numbOfIterations) throws InterruptedException {

		long startTime = System.nanoTime();

		int[][] matrix = new int[n][m];
		int[][] matrixUpd = new int[n][m];
		int[][] neighbours = new int[n][m];
		AtomicBoolean same = new AtomicBoolean(true);
		boolean firstIter = true;

		int numbOfBlocks = Runtime.getRuntime().availableProcessors();
		int blockSize = matrix.length/numbOfBlocks;
		int endRow;
		ExecutorService executor = Executors.newFixedThreadPool(numbOfBlocks);
		CountDownLatch randomizeLatch = new CountDownLatch(numbOfBlocks);

		// randomize matrix
		for (int i = 0; i < numbOfBlocks; i++) {
			endRow = (i == numbOfBlocks - 1) ? matrix.length : (i+1) * blockSize;
			RandomizeThread randomizeThread = new RandomizeThread(matrix, matrixUpd, i*blockSize, endRow, randomizeLatch);
			executor.execute(randomizeThread);
		}
		randomizeLatch.await();

		while(true) {
			CountDownLatch copyAndCompareLatch = new CountDownLatch(numbOfBlocks);
			CountDownLatch countAndUpdateLatch = new CountDownLatch(numbOfBlocks);

			/* prints matrix, gui for now
			for (int i = 0; i < matrixUpd.length; i++) {
				for (int j = 0; j < matrixUpd[0].length; j++) {
					if(matrixUpd[i][j] == 1) {
						System.out.print("* ");
					} else {
						System.out.print(". ");
					}
				}
				System.out.println();
			}
			System.out.println();
			*/

			// if the number of desired iterations is reached, just stop the program
			if(numbOfIterations == 0) {
				System.out.println("You've reached the desired amount of iterations");
				break;
			}

			if(!firstIter) {
				// copy matrixUpd into matrix, reset neighbours and check if matrix has updated
				for (int i = 0; i < numbOfBlocks; i++) {
					endRow = (i == numbOfBlocks - 1) ? matrix.length : (i+1) * blockSize;
					CopyAndCompareThread copyAndCompareThread = new CopyAndCompareThread(matrix, matrixUpd, neighbours, i*blockSize, endRow, same, copyAndCompareLatch);
					executor.execute(copyAndCompareThread);
				}
				copyAndCompareLatch.await();

				// if the matrix stopped updating, just stop the program since no changes are occurring
				if(same.get()) {
					System.out.println("Matrix stopped updating");
					break;
				}
				same.set(true);
			} else {
				firstIter = false;
			}

			// count number of living neighbours and update to proper values
			for(int i = 0; i < numbOfBlocks; i++){
				endRow = (i == numbOfBlocks - 1) ? matrix.length : (i+1) * blockSize;
				CountAndUpdateThread countAndUpdateThread = new CountAndUpdateThread(matrix, matrixUpd, neighbours, i*blockSize, endRow, countAndUpdateLatch);
				executor.execute(countAndUpdateThread);
			}
			countAndUpdateLatch.await();

			numbOfIterations--;
		}

		executor.shutdown();
		while (!executor.isTerminated()){}

		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;

		System.out.println("Computations took: ~"+ timeElapsed/1000000 + " ms");
	}

	public static void runDistributed(int n, int m, int numbOfIterations) {
	}

}


