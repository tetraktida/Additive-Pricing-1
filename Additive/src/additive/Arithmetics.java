package additive;

public class Arithmetics {
		private static int prec = 100000;
		
		static void setPrecision(int p)
		{
			if(p >= 0) {
				prec = 1;
				for(int i=0; i<p; ++i) {
					prec *= 10;
				}
			}
		}
		
		/*
		 * Remove trailing decimal 0s and fix infinite number of decimal 9s problem.
		 */
		static String sanitizeDouble(double num)
		{
			prec = 1000000;
			num = Math.floor(Math.abs(num)*prec)/prec;
			
			String number = Double.toString(num);
			
			String split[] = number.split("E");
			
			if(split.length>1) {
				int move = Integer.parseInt(split[1]);
				
				number = split[0];
				
				if(move > 0) {
					for(int i=0; i<move; i++)
						number = number + "0";
				
					number = number.replaceFirst("[.]", "");
				
					number = number.substring(0, move) + "." + number.substring(move, number.length());
				} else {
					for(int i=0; i<-move-1; i++)
						number = "0"+number;
				
					number = number.replaceFirst("[.]", "");
				
					number = 0 + "." + number;
				}
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
