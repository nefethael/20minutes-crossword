package seek20min;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

/**
 * Class processing 20 minutes PDF file.
 * 
 * @author gpercherancier
 *
 */
public class TwentyMinutes
{
    /** Logger. */
    protected static final Log LOG = LogFactory.getLog(TwentyMinutes.class);

    /** PDF URL format. */
    private static final String CROSS_FMT = "https://pdf.20mn.fr/%s/quotidien/%s_PAR.pdf";

    /** PDF file local format. */
    private static final String PDF_FMT = "./%s_PAR.pdf";

    /** Solution file local format. */
    private static final String SOL_FMT = "./%s_solution.pdf";

    /** Merged PDF + solution file local format. */
    private static final String MRG_FMT = "./%s_merge.pdf";

    /** Printer capabilities. */
    private PrintService mPrintService;

    /**
     * Constructor.
     * 
     * @param printService
     *            Printer capabilities
     */
    public TwentyMinutes(PrintService printService)
    {
        mPrintService = printService;
    }

    /**
     * Processing function.
     * 
     * @param nbC
     *            Number of copies
     * @param needSol
     *            true if solutions required, false otherwise
     */
    public void run(int nbC, boolean needSol)
    {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(SeekPDFInfo.ZONE));
        String yearStr = now.format(DateTimeFormatter.ofPattern("uuuu"));
        String dateStr = now.format(DateTimeFormatter.ofPattern("uuuuMMdd"));
        String solStr = String.format(SOL_FMT, dateStr);
        String fileStr = String.format(PDF_FMT, dateStr);
        String mrgFileStr = String.format(MRG_FMT, dateStr);
        int[] infos = null;

        // Download PDF file
        if (downloadPdf(fileStr, String.format(CROSS_FMT, yearStr, dateStr)))
        {
            // Retrieve info from PDF
            infos = SeekPDFInfo.getInfo(fileStr);

            if (infos != null)
            {
                // Download solutions if needed
                if (needSol)
                {
                    if (infos[SeekPDFInfo.IND_GRID_NUM] != SeekPDFInfo.INVALID_NUM)
                    {
                        if (!SeekSolutionInfo.savePDFPage(infos[SeekPDFInfo.IND_GRID_NUM], solStr))
                        {
                            LOG.warn("Problem retrieving solutions from " + solStr);
                            infos[SeekPDFInfo.IND_GRID_NUM] = SeekPDFInfo.INVALID_NUM;
                        }
                    }
                    else
                    {
                        LOG.warn("No solutions found!");
                    }
                }
                // Print pages (grid and/or solutions)
                try (PDDocument document = new PDDocument())
                {
                    PDDocument pdDoc = null;
                    // Get grid page
                    if (infos[SeekPDFInfo.IND_PAGE_NUM] != SeekPDFInfo.INVALID_NUM)
                    {
                        pdDoc = PDDocument.load(new File(fileStr));
                        document.importPage(
                                pdDoc.getDocumentCatalog().getPages().get(infos[SeekPDFInfo.IND_PAGE_NUM] - 1));
                    }
                    // Get solution page
                    if (infos[SeekPDFInfo.IND_GRID_NUM] != SeekPDFInfo.INVALID_NUM)
                    {
                        pdDoc = PDDocument.load(new File(solStr));
                        document.addPage(pdDoc.getDocumentCatalog().getPages().get(0));
                    }
                    document.save(mrgFileStr);
                    printPDFPage(mrgFileStr, SeekPDFInfo.INVALID_NUM, nbC);
                }
                catch (Exception e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Perform PDF file download.
     * 
     * @param fileName
     *            Local file name
     * @param urlName
     *            URL to be connected
     * @return true if download succeeds, false otherwise
     */
    private boolean downloadPdf(final String fileName, final String urlName)
    {
        Boolean isOk = false;

        try
        {
            File file = new File(fileName);

            // Download PDF file if not done yet
            if (!file.exists())
            {
                // Allow all SSL certificates
                SSLContext context = SSLContext.getInstance("SSL");

                context.init(null, new TrustManager[]
                {
                    new X509TrustManager()
                    {
                        @Override
                        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                                throws CertificateException
                        {
                            // Auto-generated method stub
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                                throws CertificateException
                        {
                            // Auto-generated method stub
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers()
                        {
                            // Auto-generated method stub
                            return null;
                        }
                    }
                }, new SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

                HttpsURLConnection connection = (HttpsURLConnection) (new URL(urlName)).openConnection();

                FileUtils.copyURLToFile(connection.getURL(), new File(fileName));
            }
            isOk = true;
        }
        catch (FileNotFoundException e)
        {
            LOG.fatal("File " + urlName + " not found", e);
        }
        catch (Exception e)
        {
            LOG.fatal("Can't download " + urlName + " to " + fileName + ": " + e.getMessage(), e);
        }
        return isOk;
    }

    /**
     * Send required pages to the printer.
     * 
     * @param fileName
     *            PDF file name
     * @param pageNum
     *            Page number containing the grid
     * @param nbCopies
     *            Number of copies to be printed
     * @throws IOException
     * @throws PrinterException
     */
    private void printPDFPage(String fileName, int pageNum, int nbCopies) throws IOException, PrinterException
    {
    	// No print to be performed if a printer was previously found
    	if (mPrintService != null)
    	{
	        // Create the job
	        PrinterJob job = PrinterJob.getPrinterJob();
	        // Set unique name to each file
	        job.setJobName(fileName);
	        job.setPrintService(mPrintService);
	        job.setPrintable(new PDFPrintable(PDDocument.load(new File(fileName)), Scaling.SCALE_TO_FIT));
	
	        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
	
	        attr.add(new Copies(nbCopies));
	        if (pageNum != SeekPDFInfo.INVALID_NUM)
	        {
	            attr.add(new PageRanges(pageNum, pageNum));
	            attr.add(Sides.ONE_SIDED);
	        }
	        else
	        {
	            attr.add(Sides.TWO_SIDED_LONG_EDGE);
	        }
	        attr.add(MediaSizeName.ISO_A4); // <<< supposedly prints in A4 format
	        job.print(attr);
	        LOG.info("Sent " + nbCopies + " grids onto printer \"" + mPrintService.getName() + "\"");
    	}
    }
}
