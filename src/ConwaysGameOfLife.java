import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConwaysGameOfLife {

	//**********************************************************************************//
	//                           @Author: Hristijan Marinkovski                         //
	//      Sequential and parallel solutions to Conway's Game of Life problem.         //
	//**********************************************************************************//
	
	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter the number of rows and columns for the game board matrix.");
		int rows = scanner.nextInt();
		int cols = scanner.nextInt();

		System.out.println("Enter the number of iterations you would like to run the game of life.");
		int numbOfIterations = scanner.nextInt();

		System.out.println("Enter the type of processing you would like to use. (sequential, parallel or distributed)");
		RunType runType = RunType.valueOf(scanner.next().toUpperCase());

		scanner.close();

		run(rows, cols, numbOfIterations, runType);
	}

	public static void run(int rows, int cols, int numbOfIterations, RunType runType) throws InterruptedException {
		if(runType == RunType.SEQUENTIAL) {
			runSequential(rows, cols, numbOfIterations);
		} else if(runType == RunType.PARALLEL){
			runParallel(rows, cols, numbOfIterations);
		} else {
			runDistributed(rows, cols, numbOfIterations);
		}
	}

	public static void runSequential(int rows, int cols, int numbOfIterations) {

		long startTime = System.nanoTime();

		int[][] matrix = new int[rows][cols];
		int[][] matrixUpd = new int[rows][cols];
		int[][] directions = new int[][]{{-1,-1}, {-1,0}, {-1,1},  {0,1}, {1,1},  {1,0},  {1,-1},  {0, -1}};
		int[][] neighbours = new int[rows][cols];
		boolean same = true, firstIter = true;

		// randomize matrix
		Random random = new Random();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = random.nextInt(2);
				matrixUpd[i][j] = matrix[i][j];
			}
		}

		while(true) {
			// print matrix to CLI
			String matrixUpdString = Arrays.stream(matrixUpd)
					.map(ints -> Arrays.stream(ints)
							.mapToObj(value -> value == 1 ? "*" : ".")
							.collect(Collectors.joining(" ", "|", "|"))
					)
					.collect(Collectors.joining("\n"));
			System.out.println(matrixUpdString + "\n ___");

			// if the number of desired iterations is reached, just stop the program
			if(numbOfIterations == 0) {
				System.out.println("You've reached the desired amount of iterations");
				break;
			}

			if(!firstIter) {
				// copy matrixUpd into matrix, reset neighbours and check if matrix has updated
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
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
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int[] direction : directions) {
						int ci = i + direction[0];
						int cj = j + direction[1];
						if(ci >=0 && ci < rows) {
							if(cj >= 0 && cj < cols) {
								if(matrix[ci][cj] == 1 ) {
									neighbours[i][j]++;
								}
							}
						}
					}
				}
			}

			// now we update the matrix in order to game rules
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
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

	public static void runParallel(int rows, int cols, int numbOfIterations) throws InterruptedException {

		long startTime = System.nanoTime();

		int[][] matrix = new int[rows][cols];
		int[][] matrixUpd = new int[rows][cols];
		int[][] neighbours = new int[rows][cols];
		AtomicBoolean same = new AtomicBoolean(true);
		boolean firstIter = true;

		int numbOfBlocks = Runtime.getRuntime().availableProcessors();
		int blockSize = rows/numbOfBlocks;
		int endRow;
		ExecutorService executor = Executors.newFixedThreadPool(numbOfBlocks);

		// randomize matrix
		CountDownLatch randomizeLatch = new CountDownLatch(numbOfBlocks);
		for (int i = 0; i < numbOfBlocks; i++) {
			endRow = (i == numbOfBlocks - 1) ? rows : (i+1) * blockSize;
			RandomizeThread randomizeThread = new RandomizeThread(matrix, matrixUpd, i*blockSize, endRow, randomizeLatch);
			executor.execute(randomizeThread);
		}
		randomizeLatch.await();

		while(true) {
			// print matrix to CLI
			String matrixUpdString = Arrays.stream(matrixUpd).parallel()
					.map(ints -> Arrays.stream(ints)
							.mapToObj(value -> value == 1 ? "*" : ".")
							.collect(Collectors.joining(" ", "|", "|"))
					)
					.collect(Collectors.joining("\n"));
			System.out.println(matrixUpdString + "\n ___");

			// if the number of desired iterations is reached, just stop the program
			if(numbOfIterations == 0) {
				System.out.println("You've reached the desired amount of iterations");
				break;
			}

			if(!firstIter) {
				// copy matrixUpd into matrix, reset neighbours and check if matrix has updated
				CountDownLatch copyAndCompareLatch = new CountDownLatch(numbOfBlocks);
				for (int i = 0; i < numbOfBlocks; i++) {
					endRow = (i == numbOfBlocks - 1) ? rows : (i+1) * blockSize;
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
			CountDownLatch countAndUpdateLatch = new CountDownLatch(numbOfBlocks);
			for(int i = 0; i < numbOfBlocks; i++){
				endRow = (i == numbOfBlocks - 1) ? rows : (i+1) * blockSize;
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

	// TODO: Distributed implementation
	public static void runDistributed(int n, int m, int numbOfIterations) {
		System.out.println("Distributed processing still not implemented. Try sequential and parallel!");
	}

}


