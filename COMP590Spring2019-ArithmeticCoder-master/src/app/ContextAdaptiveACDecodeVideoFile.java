package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class ContextAdaptiveACDecodeVideoFile {
	
	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/context-adaptive-compressed-video-file.dat";
		String output_file_name = "data/reuncompressed.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[511];
		
		for (int i=0; i<symbols.length; i++) {
			symbols[i] = i-255;
		}

		// Create 256 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[511];
		
		for (int i=0; i<511; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		
//		int[] frames = new int[4096];
//		for (int i=0; i<frames.length; i++) {
//			model = models[i];
//			int next_symbol = decoder.decode(model, bit_source);
//			frames[i] = next_symbol;
//			fos.write(next_symbol);
//		}
		
		
		int[] original = new int[num_symbols];
		for (int i=0; i<num_symbols; i++) {
			int sym = decoder.decode(model, bit_source);
			if (i < 4096) {
				original[i] = sym;
			} else {
				original[i] = original[i - 4096] - sym;
			}
			
			model.addToCount(findIndex(symbols, sym));
			
			model = models[findIndex(symbols, sym)];
			
			fos.write(original[i]);
		}		

//		for (int i=0; i<num_symbols; i++) {
//			int sym = decoder.decode(model, bit_source);
//			fos.write(sym);
//			
//			// Update model used
//			model.addToCount(sym);
//			
//			// Set up next model to use.
//			model = models[sym];

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
	
	public static int findIndex(Integer[] symbols, int a) {
		for(int i=0; i < symbols.length; i++) {
			if (a == symbols[i]) {
				return i;
			}
		}
		return -256;
	}

}
