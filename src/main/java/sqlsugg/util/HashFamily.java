package sqlsugg.util;

import java.util.Random;

public class HashFamily {
	public int numHashes;
	
	int a[];
	int b[];
	int c[];
	
	long universe;
	
	long prime;
	
	public HashFamily (int pNh, int universeSize) {
		numHashes = pNh;
		universe = universeSize;
		
		a = new int[numHashes];
		b = new int[numHashes];
		c = new int[numHashes];
		
		universe = (long)universeSize;
		
		Random r = new Random(11);
		
		for (int i = 0; i < numHashes; i ++) {
			a[i] = (int)r.nextInt(universeSize);
            b[i] = (int)r.nextInt(universeSize);
            c[i] = (int)r.nextInt(universeSize);
		}
		long tmp = universe * universe - 1;
		prime = getPrime(tmp);
	}
	
	public int getHash (int x, int index) {
		return QHash (x, a[index], b[index], c[index], prime);
	}
	
	static int QHash(int x, int a, int b, int c, long bound)
    {
        //Modify the hash family as per the size of possible elements in a Set
		//131071
        int hashValue = (int)((a * (x * x) + b * x + c) % bound);
        return Math.abs(hashValue);
    }
	
	private static long getPrime(long n) {
		//return 131071;
		n = Math.abs(n);
		while (!isPrime(n))
			n++;
		return n;
	}

	private static boolean isPrime(long n) {
		if (n <= 2)
			return n == 2;
		else if (n % 2 == 0)
			return false;
		for (int i = 3, end = (int) Math.sqrt(n); i <= end; i += 2)
			if (n % i == 0)
				return false;
		return true;
	}
	
	public static void main (String args[]) {
		try {
			HashFamily hashes = new HashFamily (2, 11);
			System.out.println(hashes.getHash(2, 0));
			System.out.println(hashes.getHash(2, 1));
			System.out.println(hashes.getHash(10, 0));
			System.out.println(hashes.getHash(10, 1));
			System.out.println(hashes.getHash(6, 0));
			System.out.println(hashes.getHash(6, 1));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
