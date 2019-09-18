package seek20min;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

public class TwentyMinutes {
	private static final String CROSS_FMT = "https://pdf.20mn.fr/%s/quotidien/%s_PAR.pdf";
	private static final String SOLUTION_FMT = "http://www.courbis.fr/Solution-de-la-grille-No-%d.html";
	private static final String PDF_FMT = "./%s_PAR.pdf";
	private static final String SOL_FMT = "./%s_solution.pdf";
	private static final String MRG_FMT = "./%s_merge.pdf";
	private static final String ZONE = "Europe/Paris";

	private static void PrintPDFPage(String fileStr, int pageNum, int nbCopie) throws IOException, PrinterException {
		PrintService service = PrintServiceLookup.lookupDefaultPrintService();
		File file = new File(fileStr);
		PDDocument document = null;
		document = PDDocument.load(file);
		// Create the job
		PrinterJob job = PrinterJob.getPrinterJob();
		// Set unique name to each file
		job.setJobName(fileStr);
		job.setPrintService(service);
		PDFPrintable printable = new PDFPrintable(document, Scaling.SCALE_TO_FIT);
		job.setPrintable(printable);

		PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

		attr.add(new Copies(nbCopie));
		if (pageNum != SeekPDFInfo.INVALID_NUM) {
			PageRanges pageRng = new PageRanges(pageNum, pageNum);
			attr.add(pageRng);
			attr.add(Sides.ONE_SIDED);
		} else {
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
		Boolean isOk = false;
		try {
			File file = new File(fileStr);
			if (!file.exists()) {
				// Allow all SSL certificates
				SSLContext context = SSLContext.getInstance("SSL");

				context.init(null, new TrustManager[] { new X509TrustManager() {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
						// Auto-generated method stub
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
						// Auto-generated method stub
					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						// Auto-generated method stub
						return null;
					}
				} }, new SecureRandom());

				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

				HttpsURLConnection connection = (HttpsURLConnection) (new URL(urlStr)).openConnection();

				FileUtils.copyURLToFile(connection.getURL(), new File(fileStr));
			}
			isOk = true;
		} catch (FileNotFoundException e) {
			System.err.println("File " + urlStr + " not found");
		} catch (Exception e) {
			System.err.println("Can't download " + urlStr + " to " + fileStr + ": " + e.getMessage());
		}

		int[] infos = null;
		if (isOk) {
			// Retrieve infos from PDF
			infos = SeekPDFInfo.GetInfo(fileStr);
		}

		// Download solutions
		if (needSol) {
			if (infos != null && infos[SeekPDFInfo.IND_DOC_NUM] != SeekPDFInfo.INVALID_NUM) {
				String solStr = String.format(SOLUTION_FMT, infos[SeekPDFInfo.IND_DOC_NUM]);
				List<String> solMap = SeekSolutionInfo.GetSolutions(solStr);
				if (!solMap.isEmpty()) {
					SeekSolutionInfo.SavePDFPage(solFileStr, solMap);
				} else {
					System.err.println("Problem retrieving solutions");
					infos[SeekPDFInfo.IND_DOC_NUM] = SeekPDFInfo.INVALID_NUM;
				}
			} else {
				System.err.println("No solutions found");
				needSol = false;
			}
		}

		try {
			PDDocument pdDoc = null;

			if (infos != null) {
				PDDocument document = new PDDocument();
				if (infos[SeekPDFInfo.IND_PAGE_NUM] != SeekPDFInfo.INVALID_NUM) {
					pdDoc = PDDocument.load(new File(fileStr));
					document.importPage(pdDoc.getDocumentCatalog().getPages().get(infos[SeekPDFInfo.IND_PAGE_NUM] - 1));
				}
				if (needSol && infos[SeekPDFInfo.IND_DOC_NUM] != SeekPDFInfo.INVALID_NUM) {
					pdDoc = PDDocument.load(new File(solFileStr));
					document.addPage(pdDoc.getDocumentCatalog().getPages().get(0));
				}
				document.save(mrgFileStr);
				document.close();
				PrintPDFPage(mrgFileStr, SeekPDFInfo.INVALID_NUM, nbC);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
