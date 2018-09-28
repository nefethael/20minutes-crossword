package seek20min;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class Main{
   public static void main(String args[]) {      
	   PrintService service = PrintServiceLookup.lookupDefaultPrintService();
	   System.out.println(service.getName());
	   
	   int nbC = 1;
	   try {
		   nbC = Integer.valueOf(args[0]);
	   }catch(Exception e) {	
			nbC = 1;
			System.out.println("No nb of copies set, default to 1");
		}
		
		boolean needSol = true;
		try {
			needSol = Boolean.valueOf(args[1]);
		}catch(Exception e) {
			needSol = true;
			System.out.println("Solution is printed by default");
		}
	   
	   new TwentyMinutes(nbC, needSol);
   }
}
