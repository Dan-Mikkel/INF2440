//I have done as good as i could with the little time i had. Gonna fix it after delivery. Now i will celebrate the last 5 mins of my birthday

import java.util.*;
import java.util.concurrent.*;

public class Oblig2 {

	public static void main(String[] args) {

		int maxN = Integer.parseInt(args[0]);
		double startTime = System.nanoTime();
		Sieve sieve = new Sieve(maxN);
		double endTime = System.nanoTime();

		System.out.println("Generated prime-numbers <= " + maxN + " in " + (endTime-startTime)/1000000.0 + " ms.");
		
		//sieve.printAllPrimes();
		long maxM = (long) maxN * (long) maxN;

		startTime = System.nanoTime();	
	
		for (long i = maxM - 100L; i < maxM; i++) {

			//System.out.println("Factorizing " + i);
			ArrayList<Long> facts = sieve.factorize(i);
			boolean nyan = true;

			System.out.print(i + " = ");

			for (long l : facts) {

				if (nyan) {

					System.out.print(l);
					nyan = false;

				} else {

					System.out.print("*" + l);

				}

			}

			System.out.println();

		}

		endTime = System.nanoTime();

		System.out.println("Factorized 100 numbers in " + (endTime-startTime)/1000000.0 + " ms.");
		sieve.parallellSolution();

	}

}

class Sieve {

	private byte[] primeBits;
	private int maxN, nThreads = 4;
	private final int[] bitMask = {1,2,4,8,16,32,64};
	private final int[] bitMask2= {255-1,255-2,255-4,255-8,255-16,255-32,255-64};
	CyclicBarrier barrier;
	ArrayList<Long> paraFacts = new ArrayList<Long>();
	long facProd = (long) 0;

	public Sieve(int maxN) {

		//System.out.println("Initializing sieve.");
		this.maxN = maxN;
		primeBits = new byte[(maxN/14)+1];
		Arrays.fill(primeBits, (byte) 127);//Denne er litt raskere på min maskin
		//setAllPrimes();
		generatePrimes();
		
	}

	public void parallellSolution() {

		double startTime = System.nanoTime();
		Arrays.fill(primeBits, (byte) 127);

		barrier = new CyclicBarrier(nThreads+1);

		int ratio = (int) Math.sqrt(maxN)/nThreads;
		int aStart = 0, aEnd = 0;

		for (int i = 0; i < nThreads; i++) {

			aStart = aEnd;
			aEnd += ratio;
			
			if (i == nThreads-1) aEnd = (int) Math.sqrt(maxN);
			new Thread (new ParaGen(i,aStart, aEnd, primeBits)).start();

		}

		System.out.println("Generated prime numbers in parallell in " + ((System.nanoTime()-startTime)/1000000.0) + " ms.");

		try {barrier.await();} catch (Exception ex) {ex.printStackTrace();}
		//printAllPrimes();

		startTime = System.nanoTime();

		long maxM = maxN*maxN;

		for (long i = maxM-100; i < maxM; i++) {

			aStart = 0;
			aEnd = 0;

			for (int j = 0; j < 4; j++) {

				aStart = aEnd;
				aEnd += ratio;
				if (j == nThreads-1) aEnd = (int) Math.sqrt(maxN);

				new Thread(new ParaFac(j,aStart,aEnd,i)).start();

			}

			try {barrier.await();} catch (Exception ex) {}
			//try {barrier.await();} catch (Exception ex) {}

			long facProd = 0;
			
			for (long l : paraFacts) {

				if (facProd == 0) {

					facProd += l;

				} else {

					facProd *= l;

				}

			}

			if (facProd != i) {

				if (i%facProd == 0) paraFacts.add(facProd);

			}

			boolean nyan = true;

			System.out.print(i + " = ");

			for (long l : paraFacts) {

				if (nyan) {

					System.out.print(l);
					nyan = false;

				} else {

					System.out.print("*" + l);

				}


			}

			System.out.println();
			paraFacts.clear();

		}

		System.out.println("Factoring 100 numbers in " + (System.nanoTime()-startTime)/1000000.0 + " ms.");
		
	}

	public ArrayList<Long> factorize(long num) {

		ArrayList<Long> factors = new ArrayList<Long>();
		if (num >= (long) maxN * (long) maxN) return null;
		
		int primeNumber = 2;
		
		while (primeNumber < maxN) {

			while (num%primeNumber == 0)  {

				factors.add((long) primeNumber);
				num /= primeNumber;

			}

			primeNumber = nextPrime(primeNumber);

		}

		if (num > 1) factors.add(num);
		return factors;

	}

	public int nextPrime(int n) {

		//if (n%2 == 0) {n++;} else {n+=2;}

		n = ((n&1)==0) ? (n+1) : (n+2);

		while (!isPrime(n)) {

			n+=2;
			if (n > maxN) return maxN;

		}

		return n;

	}

	public void printAllPrimes() {

		System.out.println("\nPrinting prime-numbers");
		
		for (int i = 2; i < maxN; i++) {

			if (isPrime(i)) { System.out.println(i); }

		}

		System.out.println("\nI think this is correct, sir...\n\n");

	}

	private void generatePrimes() {

		//System.out.println("Generating prime-numbers");

		for (int i = 3; i <= Math.sqrt(maxN); i = nextPrime(i)) {

			int r;
			for (int j = 0; (r = i*i+i*j) <= maxN; j+=2) {

				crossOut(r);

			}

		}

		//printAllPrimes();

	}

	private void crossOut(int n) {

		primeBits[n/14] &= bitMask2[(n%14)>>1];

	}

	private boolean isPrime(int n) {

		if (n == 2) return true;
		if (n%2 == 0) return false;

		return (primeBits[n/14] & bitMask[(n%14)>>1]) != 0;
		
		//return (n == 2) ? true : !(n%2 == 0) || ((primeBits[n/14] & bitMask[(n%14)>>1]) != 0);

	}

	private void setAllPrimes() {

		//System.out.print("Setting bit-arrays");

		for (int i = 0; i < primeBits.length; i++) {

			primeBits[i] = (byte) 127;
			//System.out.print(".");		

		}
		
		//System.out.println();

	}

	private synchronized void merge(byte[] a) {

		for (int i = 0; i < primeBits.length; i++) primeBits[i] &= a[i];

	}

	private synchronized void mergeFactors(ArrayList<Long> f) {

		this.paraFacts.addAll(f);
		Collections.sort(paraFacts);

	}

	private class ParaFac implements Runnable {

		long id, aStart, aEnd, num;
		ArrayList<Long> factors = new ArrayList<Long>();
		
		public ParaFac(int id, int aStart, int aEnd, long num) {

			this.id = id;
			this.aStart = (aStart <= 2) ? 2 : ((aStart&1) == 0) ? aStart+1 : aStart;
			this.aEnd = aEnd;
			this.num = num;

		}

		public void run() {

			for (long i = aStart; i < aEnd; i = (long) nextPrime((int) i)) {

				while ((num%i) == 0) {

					factors.add(i);
					num /= i;

				}

			}

			mergeFactors(factors);

			try {barrier.await();} catch (Exception ex) {}

			//Does not work
			/*if (id == 0) {

				factors.clear();
				long facProd = 0;
				
				for (long l : paraFacts) {

					if (facProd == 0) {
						facProd += l;
					} else {facProd *= l;}
				}

				if (facProd == num) {

					factors.add(facProd);
					mergeFactors(factors);

				} else {

					factors.add(num/facProd);
					mergeFactors(factors);

				}

			}

			try {barrier.await();} catch (Exception ex) {}*/

		}

	}

	private class ParaGen implements Runnable {

		byte[] a;
		int id, aStart, aEnd;

		public ParaGen(int id, int aStart, int aEnd, byte[] a) {

			this.id = id;
			this.aStart = (aStart <= 2) ? 3 : ((aStart&1) == 0) ? aStart+1 : aStart;
			this.aEnd = aEnd;
			this.a = a;

		}

		public void run() {

			for (int i = aStart; i < aEnd; i = nextPrime(i)) {

				int r;
				for (int j = 0; (r = (i*i)+(i*j)) <= maxN; j+= 2) {

					if (r > (int)Math.sqrt(maxN)) a[r/14] &= bitMask2[(r%14)>>1];

				}

			}
			
			merge(a);

			try { barrier.await(); } catch (Exception ex) {ex.printStackTrace();}

		}

	}

}
