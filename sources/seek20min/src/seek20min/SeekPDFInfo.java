package seek20min;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Analysis of the PDF file to retrieve the page and the grid number.
 * 
 * @author gpercherancier
 *
 */
public class SeekPDFInfo
{
    /** Index for the page number of the grid. */
    public static final int IND_PAGE_NUM = 0;

    /** Index for the grid number. */
    public static final int IND_GRID_NUM = 1;

    /** Invalid number value. */
    public static final int INVALID_NUM = -1;

    /** Time zone in use. */
    public static final String ZONE = "Europe/Paris";

    /** Logger. */
    protected static final Log LOG = LogFactory.getLog(SeekPDFInfo.class);

    /** Number of milliseconds for one day. */
    protected static final long MS_PER_DAY = 86400000L;

    /** Known matching number/date (28/10/2019 => 4842). */
    private static final int LAST_NUM[] =
    {
            INVALID_NUM, INVALID_NUM
    };

    /** Index for the last known grid number. */
    private static final int IND_LAST_NUM = 0;

    /** Index for the day of the last known grid number. */
    private static final int IND_LAST_DAY = 1;

    // Static initialization
    static
    {
        Calendar lastDate = Calendar.getInstance(TimeZone.getTimeZone(ZONE));
        lastDate.set(2019, 9, 28);
        LAST_NUM[IND_LAST_NUM] = 4842;
        LAST_NUM[IND_LAST_DAY] = (int) (lastDate.getTimeInMillis() / MS_PER_DAY);
    }

    /**
     * Getting information from file.
     * 
     * @param pdfFile
     *            PDF file name
     * @return Information array containing the page number and the grid number
     */
    public static int[] getInfo(String pdfFile)
    {
        Pattern pattern = Pattern.compile("^.*N°\\s*(\\d+).*$");
        int[] result = new int[]
        {
                INVALID_NUM, INVALID_NUM
        };

        try (PDDocument document = PDDocument.load(new File(pdfFile)))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int i = 0; i < document.getNumberOfPages(); i++)
            {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String txt = stripper.getText(document);
                if (txt.contains("Horoscope"))
                {
                    result[IND_PAGE_NUM] = i;
                    String[] lines = txt.split("\\r?\\n");
                    for (String line : lines)
                    {
                        if (line.contains("N°"))
                        {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find())
                            {
                                result[IND_GRID_NUM] = Integer.parseInt(matcher.group(1));
                            }
                            else
                            {
                                result[IND_GRID_NUM] = INVALID_NUM;
                            }
                            break;
                        }
                    }
                    if (result[IND_GRID_NUM] == INVALID_NUM)
                    {
                        LOG.info("Grid number not found: retrieving from last known one...");
                        int toDay = (int) (Calendar.getInstance(TimeZone.getTimeZone(ZONE)).getTimeInMillis()
                                / MS_PER_DAY);
                        result[IND_GRID_NUM] = LAST_NUM[IND_LAST_NUM] + Math.abs(toDay - LAST_NUM[IND_LAST_DAY]);
                    }
                    break;
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
}
