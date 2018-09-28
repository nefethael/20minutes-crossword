package seek20min;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

public class TwentyMinutes {
	private static final String CROSS_FMT = "http://pdf.20mn.fr/%s/quotidien/%s_PAR.pdf";
	private static final String SOLUTION_FMT = "http://www.courbis.fr/Solution-de-la-grille-No-%d.html";
	private static final String PDF_FMT = "./%s_PAR.pdf";
	private static final String SOL_FMT = "./%s_solution.pdf";
	private static final String MRG_FMT = "./%s_merge.pdf";
	private static final String ZONE = "Europe/Paris";
	
	static private void PrintPDFPage(String fileStr, int pageNum, int nbCopie) throws IOException, PrinterException{
		PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        File file = new File(fileStr);
        PDDocument document = null;
        document = PDDocument.load(file);
        // create the job
        PrinterJob job = PrinterJob.getPrinterJob();
        // set unique name to each file
        job.setJobName(fileStr);
        job.setPrintService(service);
        PDFPrintable printable = new PDFPrintable(document, Scaling.SCALE_TO_FIT);
        job.setPrintable(printable);
        
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet(); 
        
        attr.add(new Copies(nbCopie));
        if(pageNum != -1) {
        	PageRanges pageRng = new PageRanges( pageNum , pageNum);
        	attr.add(pageRng);
            attr.add(Sides.ONE_SIDED);
        }else {
            attr.add(Sides.TWO_SIDED_LONG_EDGE);
        }
        attr.add(MediaSizeName.ISO_A4); // <<< supposedly prints in A4 format      
        job.print(attr);  
	}
	
	public TwentyMinutes(int nbC, boolean needSol) {
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("uuuuMMdd");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("uuuu");
		OffsetDateTime now = OffsetDateTime.now(ZoneId.of(ZONE));
		String yearStr = now.format(formatter2);
		String dateStr = now.format(formatter1);
		String urlStr = String.format(CROSS_FMT, yearStr, dateStr);
		String fileStr = String.format(PDF_FMT, dateStr);
		String solFileStr = String.format(SOL_FMT, dateStr);
		String mrgFileStr = String.format(MRG_FMT, dateStr);
		
		// Download PDF file
		Boolean isOk = true;
		try {			
			File file = new File(fileStr);
			if (!file.exists()) {
				FileUtils.copyURLToFile(new URL(urlStr), new File(fileStr));
			}
		} catch (Exception e) {			
			System.err.println("Can't download " + urlStr + " to " + fileStr);
			isOk = false;
		}
		
		int[] infos = null;
		if ( isOk ) {
		// Retreive infos from PDF
			infos = SeekPDFInfo.GetInfo(fileStr);
		}
			
				
		// Download solutions
		if (needSol) {
			if (infos != null && infos[1] != -1) {
				String solStr = String.format(SOLUTION_FMT, infos[1]);
				List<String> solMap = SeekSolutionInfo.GetSolutions(solStr);
				if ( solMap.size() > 0) {
					SeekSolutionInfo.SavePDFPage(solFileStr, solMap);
				} else {
					System.err.println("Problem retreiving solutions");
					infos[1] = -1;
				}			
			} else {
				System.err.println("No solutions found");
			}
		}
		
		try {
			PDDocument pdDoc = null;
			PDDocument pdDoc2 = null; 
			
			if (infos != null) {
				PDDocument document = new PDDocument();
				if (infos[0] != -1) {
					pdDoc = PDDocument.load(new File(fileStr));
					document.addPage((PDPage) pdDoc.getDocumentCatalog().getPages().get(infos[0]-1));
				}
				if (needSol && infos[1] != -1) {
					pdDoc2 = PDDocument.load(new File(solFileStr)); 
					document.addPage((PDPage) pdDoc2.getDocumentCatalog().getPages().get(0));
				}
				document.save(mrgFileStr);
				document.close();
				PrintPDFPage(mrgFileStr, -1, nbC);
			}		       		      
		      
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
