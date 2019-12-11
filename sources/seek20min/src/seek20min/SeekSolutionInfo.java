package seek20min;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Analysis of the solution file to generate PDF solution file.
 * 
 * @author gpercherancier
 *
 */
public class SeekSolutionInfo
{
    /** Logger. */
    protected static final Log LOG = LogFactory.getLog(SeekSolutionInfo.class);

    /** Solution URL format. */
    private static final String SOLUTION_FMT = "http://www.courbis.fr/Solution-de-la-grille-No-%d.html";

    /**
     * Getting solutions from URL.
     * 
     * @param gridNum
     *            Grid number to format URL
     * @return List of the solutions.
     */
    private static List<String> getSolutions(final int gridNum)
    {
        List<String> dico = new LinkedList<>();
        String urlStr = String.format(SOLUTION_FMT, gridNum);
        try
        {
            Document doc = Jsoup.connect(urlStr).get();
            Elements table = doc.select("table");
            String prevTitle = "";

            for (Element tr : table.select("> tbody > tr"))
            {
                for (Element td : tr.select("> td"))
                {
                    Element title = td.select("> a").first();
                    if (title != null && !title.text().isEmpty())
                    {
                        prevTitle = title.text();
                    }
                    else
                    {
                        if (!td.textNodes().isEmpty() && !td.textNodes().get(0).text().trim().isEmpty()
                                && !prevTitle.isEmpty())
                        {
                            dico.add(prevTitle + ": " + td.textNodes().get(0).text().trim());
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return dico;
    }

    /**
     * Save solutions in a PDF file.
     * 
     * @param gridNum
     *            Grid number
     * @param fileName
     *            PDF file name
     * @return true if success, false otherwise
     */
    public static boolean savePDFPage(final int gridNum, final String fileName)
    {
        boolean isOk = false;
        List<String> solMap = getSolutions(gridNum);

        if (!solMap.isEmpty())
        {
            try (PDDocument doc = new PDDocument())
            {
                PDPage page = new PDPage();
                PDRectangle mediabox = page.getMediaBox();
                float margin = 72;
                float startX = mediabox.getLowerLeftX() + margin;
                float startY = mediabox.getUpperRightY() - margin;

                try (PDPageContentStream contents = new PDPageContentStream(doc, page))
                {
                    PDFont font = PDType1Font.HELVETICA;
                    float fontSize = 8;
                    float leading = 1.5f * fontSize;

                    contents.beginText();
                    contents.setFont(font, fontSize);
                    contents.newLineAtOffset(startX, startY);

                    for (String line : solMap)
                    {
                        contents.showText(line);
                        contents.newLineAtOffset(0, -leading);
                    }
                    contents.endText();
                }
                doc.addPage(page);
                doc.save(fileName);
                isOk = true;
            }
            catch (IOException e)
            {
                LOG.error(e.getMessage(), e);
            }
        }
        return isOk;
    }
}
