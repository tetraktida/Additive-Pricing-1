package additive;

import java.text.DecimalFormat;
import java.util.Collections;

public class Arithmetics {
		private static int digits = 6;
		private static double eps = Math.pow(10, -digits);
		
		static void setPrecision(int p)
		{
			digits = p;
			eps = Math.pow(10, -digits);
		}
		
		/*
		 * Remove trailing decimal 0s and fix infinite number of decimal 9s problem.
		 */
		static String sanitizeDouble(double num)
		{
	
			num = Math.round(num / eps) * eps;
			String precisionString = "0."+String.join("", Collections.nCopies(digits, "#"));
			DecimalFormat df = new DecimalFormat(precisionString);
			
			return df.format(num);

		}
}
