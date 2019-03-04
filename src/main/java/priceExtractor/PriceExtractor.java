package priceExtractor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PriceExtractor {
	
	private final File file;
	
	public PriceExtractor(File file) {
		this.file = file;
	}
	
	public void run() throws IOException, DocumentException {
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
				.withCSVParser(new CSVParserBuilder()
						.withSeparator(';')
						.build())
				.build()) {
			
			List<String[]> lines = reader.readAll();
			List<Entry> entries = lines.stream()
					.sequential()
					.filter(strings -> strings.length != 0)
					.map(strings -> {
						try {
							return new Entry(strings);
						} catch (IOException e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream("export.pdf"));
			document.open();
			
			for (Entry entry : entries) {
				PdfPTable table = new PdfPTable(1);
				
				PdfPTable block1 = new PdfPTable(1);
				block1.addCell(entry.bezeichnung);
				table.addCell(block1);
				
				PdfPTable block2 = new PdfPTable(1);
				block2.addCell(entry.kisteInhalt);
				block2.addCell(entry.kisteLiterpreis);
				block2.addCell(entry.kistePreis);
				block2.addCell(entry.kistePfand);
				table.addCell(block2);
				
				PdfPTable block3 = new PdfPTable(1);
				block3.addCell(entry.flascheInhalt);
				block3.addCell(entry.flaschePreis);
				block3.addCell(entry.flaschePfand);
				table.addCell(block3);
				
				PdfPTable block4 = new PdfPTable(1);
				block4.addCell(entry.weg.name());
				table.addCell(block4);
				
				document.add(table);
				document.newPage();
			}
			
			document.close();
		}
	}
	
	public static class Entry {
		
		public final int arkitelNr;
		public final String bezeichnung;
		
		public final String kisteInhalt;
		public final String kisteLiterpreis;
		public final String kistePreis;
		public final String kistePfand;
		
		public final String flascheInhalt;
		public final String flaschePreis;
		public final String flaschePfand;
		
		public final Weg weg;
		
		public Entry(String[] input) throws IOException {
			try {
				arkitelNr = Integer.parseInt(input[0]);
				bezeichnung = input[1];
				
				kisteInhalt = input[2];
				kisteLiterpreis = input[3];
				kistePreis = input[4];
				kistePfand = input[5];
				
				flascheInhalt = input[6];
				flaschePreis = input[7];
				flaschePfand = input[8];
				
				weg = Weg.parse(input[9]);
			} catch (NumberFormatException e) {
				throw new IOException(e);
			}
		}
		
		@Override
		public String toString() {
			return "Entry{" +
					"arkitelNr=" + arkitelNr +
					", bezeichnung='" + bezeichnung + '\'' +
					'}';
		}
		
		@SuppressWarnings("unused")
		public enum Weg {
			
			MEHRWEG, EINWEG;
			
			public static Weg parse(String str) {
				char c = str.trim().charAt(0);
				switch (c) {
					case 'm':
					case 'M':
						return MEHRWEG;
					case 'e':
					case 'E':
						return EINWEG;
				}
				throw new IllegalArgumentException(str);
			}
			
		}
		
	}
	
}
