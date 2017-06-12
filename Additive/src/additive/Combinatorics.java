package additive;

import java.math.BigInteger;

public class Combinatorics {
	static int choose(int n, int k)
	{
		BigInteger a=BigInteger.ONE;
		BigInteger b=BigInteger.ONE;
		for(int i=1;i<=k;i++) {
			a=a.multiply(BigInteger.valueOf(n-i+1));
			b=b.multiply(BigInteger.valueOf(i));
		}
		
		return a.divide(b).intValueExact();
	}
}
