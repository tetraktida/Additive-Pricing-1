package additive;

public class Arithmetics {
		private static int mult = 0;
		private static String pad;
		private static int shift = 5;
		private static int prec = 100000;
		
		static int getShiftMultiplier()
		{
			if(mult == 0) {
				mult = 1;
				for(int i=0; i<shift; ++i) {
					mult *= 10;
				}
			}
			return mult;
		}
		
		static void setShift(int k)
		{
			if(k >= 0) {
				shift = k;
				mult = 0;
				pad = null;
			}
		}
		
		static void setPrecision(int p)
		{
			if(p >= 0) {
				prec = 1;
				for(int i=0; i<p; ++i) {
					prec *= 10;
				}
			}
		}
		
		static String getPadding()
		{
			if(pad == null) {
				pad = "";
				for(int i=0; i<shift; ++i) {
					pad = "0" + pad;
				}
			}
			return pad;
		}
		
		/*
		 * Remove trailing decimal 0s and fix infinite number of decimal 9s problem.
		 */
		static String sanitizeDouble(double num, boolean div)
		{
			
			num = Math.floor(Math.abs(num)*prec)/prec;
			
			String number = Double.toString(num);
			
			String split[] = number.split("E");
			
			if(split.length>1) {
				int move = Integer.parseInt(split[1]);
				
				number = split[0];
				for(int i=0; i<move; i++)
					number = number + "0";
				
				number = number.replaceFirst("[.]", "");
				
				number = number.substring(0, move+1) + "." + number.substring(move+1, number.length()); 
			}
			
			int n;
			
			if(div) {
				number = Arithmetics.getPadding() + number;
				n = number.length();
				
				int pos = number.indexOf('.');
				
				number = number.substring(0, pos - shift)
						+ "."
						+ number.substring(pos - shift,pos)
						+ number.substring(pos+1, n);
							
				number = number.replaceFirst("0*", "");
				
				if(number.charAt(0) == '.')
					number = '0' + number;
			}
			
			
			number = number + "A";
			
			number = number.replaceFirst("0+A", "B");
			
			if(number.contains("B")) {
				return number.replaceFirst("[.]B", "").replace("B", "");
			}
			
			number = number.replaceFirst("[.]A", "");
			number = number.replaceFirst("99+A", "");
			
			
			if(!number.contains("A") && number.contains(".")) {
				if(number.charAt(number.length()-1) != '.') {
					number = number.substring(0,number.length()-1) + ((char)(1+number.charAt(number.length()-1)));
				}
				else {
					number = "A" + number;
					String s[] = number.split("9*[.]");
					
					if(s[0].length() == 1) {
						number = "1" + number.replaceAll("9", "0"); 
					} else {
						number =  number.substring(0,s[0].length()-1)
								+ ((char)(1+number.charAt(s[0].length()-1)))
								+ number.substring(s[0].length(), number.length()).replaceAll("9", "0").replace('.','\0');
					}
						
				}
			}

			return number.replaceFirst("A", "").replace(" ","");
		}
}
