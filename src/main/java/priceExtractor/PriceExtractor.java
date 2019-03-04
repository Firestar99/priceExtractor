package priceExtractor;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.UnitValue;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.itextpdf.layout.property.UnitValue.createPercentValue;

public class PriceExtractor {
	
	private static final PdfFont FONT_TIMES_ROMAN;
	private static final PdfFont FONT_TIMES_BOLD;
	
	static {
		try {
			FONT_TIMES_ROMAN = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
			FONT_TIMES_BOLD = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final File input;
	private final File output;
	private final float margin = 50;
	
	private final Function<String, Text> fontTitle = s -> new Text(s).setFont(FONT_TIMES_BOLD).setFontSize(36);
	private final Function<String, Text> fontDesc = s -> new Text(s).setFont(FONT_TIMES_ROMAN).setFontSize(24);
	
	public PriceExtractor(File input, File output) {
		this.input = input;
		this.output = output;
	}
	
	public void run() throws IOException {
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(input))
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
						} catch (RuntimeException e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			
			try (PdfDocument pdf = new PdfDocument(new PdfWriter(output))) {
				try (Document document = new Document(pdf, PageSize.A4)) {
					Rectangle pageSize = document.getPageEffectiveArea(PageSize.A4);
					Rectangle outer = new Rectangle(pageSize.getLeft() + margin, pageSize.getBottom() + margin, pageSize.getRight() - margin, pageSize.getTop() - margin);
					
					for (Entry entry : entries) {
						
						Table table = new Table(1);
						table.setWidth(createPercentValue(100));
						
						table.addCell(createCell(outer, createPercentValue(1f / 6), fontTitle, entry.bezeichnung));
						table.addCell(createCell(outer, createPercentValue(2f / 6), fontDesc, entry.kisteInhalt, entry.kisteLiterpreis, entry.kistePreis, entry.kistePfand));
						table.addCell(createCell(outer, createPercentValue(2f / 6), fontDesc, entry.flascheInhalt, entry.flaschePreis, entry.flaschePfand));
						table.addCell(createCell(outer, createPercentValue(1f / 6), fontDesc, entry.weg.name()));
						
						document.add(table);
//						document.newPage();
					}
					
				}
			}
		}
	}
	
	private static Cell createCell(Rectangle outer, UnitValue height, Function<String, Text> font, String... text) {
		return createCell(outer, height, Arrays.stream(text).map(font).map(Paragraph::new).toArray(IBlockElement[]::new));
	}
	
	private static Cell createCell(Rectangle outer, UnitValue height, IBlockElement... elements) {
		Cell cell = new Cell();
		for (IBlockElement element : elements)
			cell.add(element);
		cell.setHeight(height);
		return cell;
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
		
		public Entry(String[] input) {
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