package seek20min;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

public class SeekSolutionInfo {
	public static List<String> GetSolutions(String solStr) {
		List<String> dico = new LinkedList<String>();
		try {
			Document doc = Jsoup.connect(solStr).get();
			Elements table = doc.select("table");
			String prevTitle = "";

			for (Element tr : table.select("> tbody > tr")) {
				for (Element td : tr.select("> td")) {
					Element title = td.select("> a").first();
					if (title != null && !title.text().isEmpty()) {
						prevTitle = title.text();
					} else {
						if (td.textNodes().size() > 0 && !td.textNodes().get(0).text().trim().isEmpty()
								&& !prevTitle.isEmpty()) {
							dico.add(prevTitle + ": " + td.textNodes().get(0).text().trim());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dico;
	}

	public static void SavePDFPage(String fileStr, List<String> solMap) {

		PDDocument doc = new PDDocument();
		PDPage page = new PDPage();
		doc.addPage(page);

		PDRectangle mediabox = page.getMediaBox();
		float margin = 72;
		float startX = mediabox.getLowerLeftX() + margin;
		float startY = mediabox.getUpperRightY() - margin;

		try {
			PDPageContentStream contents = new PDPageContentStream(doc, page);
			contents.beginText();
			PDFont font = PDType1Font.HELVETICA;
			float fontSize = 8;
			float leading = 1.5f * fontSize;
			contents.setFont(font, fontSize);

			contents.newLineAtOffset(startX, startY);

			for (String line : solMap) {
				contents.showText(line);
				contents.newLineAtOffset(0, -leading);
			}

			contents.endText();
			contents.close();
			doc.save(fileStr);
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
