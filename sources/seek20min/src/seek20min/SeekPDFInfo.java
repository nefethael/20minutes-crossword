package seek20min;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class SeekPDFInfo {
	static public int[] GetInfo(String pdfFile) {
		 Pattern pattern = Pattern.compile("^.*?N�(\\d+).*$");
	      PDDocument document = null;
	      
	      int[] result = new int[2];
	      result[0] = -1;
	      result[1] = -1;
	      
	      try {
	           document = PDDocument.load(new File( pdfFile ));
	           PDFTextStripper stripper = new PDFTextStripper();
	           for (int i = 0; i < document.getNumberOfPages(); i++) {
	        	   stripper.setStartPage( i );
	        	   stripper.setEndPage( i );
	        	   String txt = stripper.getText(document);
	        	   if (txt.contains("Horoscope")) {
	        		   result[0] = i;
	        		   String[] lines = txt.split("\\r?\\n");
	        	        for (String line : lines) {
	        	        	if ( line.contains("N�")) {
	        	        		Matcher matcher = pattern.matcher(line);
	        	        		if (matcher.find()) {
	        	        			result[1] = Integer.parseInt(matcher.group(1));
	        	        		} else {
	        	        			result[1] = -1;
	        	        		}
	        	        		break;
	        	        	}
	        	        }
	        		   break;
	        	   }
	           }           
	           document.close();
	       }catch(Exception e) {
	    	   e.printStackTrace();
	       }
	       return result;
	}
}
