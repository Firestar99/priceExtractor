package priceExtractor;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {
	
	public static final String TEMPLATE_PATH = "./template.pdf";
	public static final String OUTPUT_PATH = "./output.pdf";
	public static final String CSV_PATH = "./test.csv";
	public static final String START_LINE_PATH = "./startLine";
	
	public static void main(String[] args) throws IOException, DocumentException {
		int startLine = Integer.parseInt(Files.readAllLines(new File(START_LINE_PATH).toPath()).get(0));
		
		replace(TEMPLATE_PATH, OUTPUT_PATH, CSV_PATH, startLine);
	}
	
	public static final Pattern findPattern = Pattern.compile("\\$\\{(.*?)}");
	
	public static void replace(String template, String output, String csv, int startLine) throws IOException, DocumentException {
		replace(template, output, csv, startLine, Integer.MAX_VALUE);
	}
	
	public static void replace(String templateFile, String outputFile, String csvFile, int startLine, int count) throws IOException, DocumentException {
		List<String[]> lines = readCsv(csvFile, startLine);
		String[] line = lines.get(0);
		
		PdfReader reader = new PdfReader(templateFile);
		try {
			PdfDictionary page = reader.getPageN(1);
			PdfObject pdfObject = page.getDirectObject(PdfName.CONTENTS);
			if (pdfObject instanceof PRStream) {
				PRStream prStream = (PRStream) pdfObject;
				String contentOriginal = new String(PdfReader.getStreamBytes(prStream));
				prStream.setData(replacePattern(line, contentOriginal).getBytes());
			}
			
			new PdfStamper(reader, new FileOutputStream(outputFile)).close();
		} finally {
			reader.close();
		}
	}
	
	private static List<String[]> readCsv(String csv, int startLine) throws IOException {
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(csv))
				.withCSVParser(new CSVParserBuilder()
						.withSeparator(';')
						.build())
				.withSkipLines(startLine)
				.build()) {
			
			return reader.readAll();
		}
	}
	
	private static String replacePattern(String[] line, String content) {
		Matcher matcher = findPattern.matcher(content);
		StringBuilder sb = new StringBuilder();
		
		int prevPos = 0;
		while (matcher.find()) {
			sb.append(content, prevPos, matcher.start())
					.append(line[computeIndex(matcher.group())]);
			prevPos = matcher.end();
		}
		sb.append(content, prevPos, content.length());
		return sb.toString();
	}
	
	private static int computeIndex(String column) {
		int index = 0;
		for (int i = 0; i < column.length(); i++)
			index = index * 26 + (column.charAt(i) - 'A' + 1);
		return index - 1;
	}
}
