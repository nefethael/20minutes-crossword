package seek20min;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class SeekPDFInfo {
	
	static final int IND_PAGE_NUM = 0;
	static final int IND_DOC_NUM = 1;
	static final int INVALID_NUM = -1;
	
	static public int[] GetInfo(String pdfFile) {
		Pattern pattern = Pattern.compile("^.*?N°(\\d+).*$");
		PDDocument document = null;

		int[] result = new int[IND_DOC_NUM + 1];
		result[IND_PAGE_NUM] = INVALID_NUM;
		result[IND_DOC_NUM] = INVALID_NUM;

		try {
			document = PDDocument.load(new File(pdfFile));
			PDFTextStripper stripper = new PDFTextStripper();
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				stripper.setStartPage(i);
				stripper.setEndPage(i);
				String txt = stripper.getText(document);
				if (txt.contains("Horoscope")) {
					result[IND_PAGE_NUM] = i;
					String[] lines = txt.split("\\r?\\n");
					for (String line : lines) {
						if (line.contains("N°")) {
							Matcher matcher = pattern.matcher(line);
							if (matcher.find()) {
								result[IND_DOC_NUM] = Integer.parseInt(matcher.group(1));
							} else {
								result[IND_DOC_NUM] = INVALID_NUM;
							}
							break;
						}
					}
					break;
				}
			}
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
