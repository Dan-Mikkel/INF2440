import java.util.*;
import java.util.concurrent.*;

public class Oblig1 {

	double times[];
	CyclicBarrier barrier;
	int j = 0;

	public static void main(String args[]) {

		int aSize = 0, oppgave = 0, nTimes = 0, k = Runtime.getRuntime().availableProcessors();

		if (args.length == 2) {

			aSize = Integer.parseInt(args[0]);
			nTimes = Integer.parseInt(args[1]);

		} else {return;}

		System.out.println("Running this program " + nTimes + " times on " + aSize + " elements");

		Oblig1 o = new Oblig1();
		o.createTimes(nTimes);

		for (int i = 0; i < nTimes; i++) {

			int a[] = o.createArray(aSize);
			int b[] = o.createArray(aSize);
			Arrays.sort(b);
			o.goSeq(a);
			//o.compareSort(a,b);
			o.goPara(a,k);
			//o.compareSort(a,b);
			
			//System.out.println();

		}

		o.calcTimes(nTimes);
		
	}

	private void compareSort(int a[], int b[]) {

		boolean alike = true;

		for (int i = 0; i < 50; i++) {

			if (a[i] != b[(b.length-1)-i]) alike = false; 

		}

		if (alike) {System.out.println("Arrays match");} else {System.out.println("Arrays does not match");}

	}

	private void calcTimes(int nTimes) {

		//Denne koden tar forbehold om at man kjører koden 9 ganger.

		double seqMed[] = new double[times.length/2], parMed[] = new double[times.length/2];
		
		int i = 0, j = 0;
		
		while (i < times.length) {

			seqMed[j] = times[i++];
			parMed[j++] = times[i++];

		}

		Arrays.sort(seqMed);
		Arrays.sort(parMed);

		System.out.println("Seq median time: " + seqMed[4]);
		System.out.println("Par median time: " + parMed[4]);
		System.out.println("Speedup: " + seqMed[5]/parMed[5]);
		System.out.println("\n--- PROGRAM TERMINATED ---\n");

	}

	private void createTimes(int n) {

		times = new double[n*2];

	}

	private void goSeq(int a[]) {

		barrier = new CyclicBarrier(2);
 
                double startTime = System.nanoTime();

                Thread t = new Thread(new Para(a, 0, 1));
                t.start();
 
                try{ barrier.await(); } catch (Exception ex) {ex.printStackTrace();}
 
		times[j] = (System.nanoTime()-startTime)/1000000.0; j++;
		//System.out.println("Seq-time: " + times[j++] + " ms. when working on " + a.length + " elements.");

	}

	private void goPara(int a[], int k) {

		barrier = new CyclicBarrier(k+1);

		double startTime = System.nanoTime();

		for (int i = 0; i < k; i++) {

			Thread t = new Thread(new Para(a, i, k));
			t.start();

		}

		try {barrier.await(); } catch (Exception ex) {ex.printStackTrace();}

		
		times[j] = ((System.nanoTime()-startTime)/1000000.0); j++;
		//System.out.println("Par-time: " + times[j++] + " ms. when working on " + a.length + " elements.");

	}

	private int[] createArray(int n) {

		//System.out.println("Generating array with " + n + " elements.");

		if (n < 50) return null;

		Random random = new Random(123);
		
		int a[] = new int[n];

		for (int i = 0; i < n; i++) {

			a[i] = random.nextInt(n);

		}

		//System.out.println("Array generated with " + n + " random elements\n");

		return a;

	}

	private class Para implements Runnable {

		private int a[], id, nThreads, ratio, aStart, aEnd;

		public Para(int a[], int id, int nThreads) {

			this.a = a;
			this.id = id;
			this.nThreads = nThreads;
			//System.out.println(id + " is alive");
			ratio = a.length/nThreads;
			aStart = id*ratio;
			aEnd = (id+1)*ratio;

		}

		public void run() {

			sortFiftyFirst();
			checkWithOthers();			
			//if (id == 0) printSort();

			try {barrier.await();} catch (Exception ex) {ex.printStackTrace();}

		}

		private synchronized void printSort() {

			for (int i = 0; i < a.length; i++) {

				System.out.print(a[i] + " | ");
				if (i == 50) System.out.println("\n");

			}

		}

		private void sortFiftyFirst() {

			for (int i  = aStart+1; i < aStart+50; i++) {

				int j = i;

				while ((j > 0) && (a[j-1] < a[j])) {

					//a[j] = a[(j--)-1];
					switchPlaces(j,j-1);
					j--;

				}

			}

		}

		private void switchPlaces(int i, int j) {

			a[i] = a[i] ^ a[j];
			a[j] = a[i] ^ a[j];
			a[i] = a[i] ^ a[j];
			return;	

		}

		private void checkWithOthers() {

			for (int i = aStart+50; i < aEnd; i++) {

				if (a[i] > a[49]) {

					switchPlaces(i,49);
					sortFiftyFirst();

				} 
			}
		}
	}
}
