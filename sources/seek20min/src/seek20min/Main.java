package seek20min;

import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;

  //private static char[] nbCA = { 'M', 'o', 'T', 's', ' ', 'F', 'L', 65475, 65417, 'c', 'h', 65475, 65417, 's', ' ', ' ', 'N', 65474, 65456 };
  //private static String nbStr = new String(nbCA);


public class Main{
   public static void main(String args[]) {
       PDDocument document = null;     
       try {
    	   String pdfFile = args[0];
           document = PDDocument.load(new File( pdfFile ));
           PDFTextStripper stripper = new PDFTextStripper();
           for (int i = 0; i < document.getNumberOfPages(); i++) {
        	   stripper.setStartPage( i );
        	   stripper.setEndPage( i );
        	   String txt = stripper.getText(document);
        	   if (txt.contains("Horoscope")) {
        		   System.out.print(i);
        		   String[] lines = txt.split("\\r?\\n");
        	        for (String line : lines) {
        	        	if ( line.contains("N°")) {
        	        		System.out.print(" " + line.replaceAll("N°", "").replaceAll(" ", "").replaceAll("Force.*$", ""));
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
   }
}
