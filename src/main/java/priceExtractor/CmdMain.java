package priceExtractor;

import java.io.File;
import java.io.IOException;

public class CmdMain {
	
	public static void main(String[] args) throws IOException {
		new PriceExtractor(new File("test.csv"), new File("export.pdf")).run();
	}
	
}
