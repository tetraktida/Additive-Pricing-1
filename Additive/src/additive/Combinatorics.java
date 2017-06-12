package additive;

public class Combinatorics {
	static int choose(int n, int k)
	{
		int a=1;
		int b=1;
		for(int i=1;i<=k;i++) {
			a*=(n-i+1);
			b*=i;
		}
		
		return a/b;
	}
}
