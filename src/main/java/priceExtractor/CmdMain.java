package priceExtractor;

import com.itextpdf.text.DocumentException;

import java.io.File;
import java.io.IOException;

public class CmdMain {
	
	public static void main(String[] args) throws IOException, DocumentException {
		new PriceExtractor(new File("test.csv")).run();
	}
	
}
