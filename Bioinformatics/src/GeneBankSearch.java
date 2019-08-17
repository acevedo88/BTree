import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * 
 * @author Alex Acevedo
 * 
*/

public class GeneBankSearch {

	private static String bTreeFile;
	private static String queryFile;
	private static String fileName;
	private static File file;
	private static int withCache;
	private static int subSize;
	private static int cacheSize;
	private static int debugLevel = 1;
	public static void main(String args[]) throws Exception {
		
		try {
			
			withCache = Integer.parseInt(args[0]);
			bTreeFile = args[1];
			queryFile = args[2];
			
			File file = new File(bTreeFile);
			File file2 = new File(queryFile);
			
			if (!file.exists() || !file2.exists()) {
				
				throw new FileNotFoundException();
			}
			
			if (withCache == 1) {
				
				cacheSize = Integer.parseInt(args[3]); 
				
				if (args.length == 5) {
					
					debugLevel = Integer.parseInt(args[4]);
				}
			}
			
			else if (args.length == 5) {
				
				debugLevel = Integer.parseInt(args[4]);
			}
			
			else if (args.length == 4) {
				
				debugLevel = Integer.parseInt(args[3]);
			}
			
			if (debugLevel > 1 || debugLevel < 0) {
				
				throw new Exception();
			}
		}
		catch (FileNotFoundException e) {
			
			System.err.println("One or more of your input files was not recognized.");
			System.exit(1);
		}
		
		catch (Exception e) {
			
			printUsageError();
			System.exit(1);
		}
		
		int i;
		
		for (i = 0; i < bTreeFile.length(); i++) {
			
			if (args[1].charAt(i) == '.') {
				
				break;
			}
			if (i == args[1].length()-1) {
				
				throw new Exception();
			}
		}
		
		fileName = bTreeFile.substring(0, i);
		 
		int firstDot = 0;
		int secondDot = 0;
		
		for (int j = bTreeFile.length()-1; j >= 0; j--) {
			
			if (bTreeFile.charAt(j) == '.') {
				
				if (firstDot != 0 && secondDot == 0) {
					
					secondDot = j;
					break;
				}
				
				if (firstDot == 0) {
					
					firstDot = j;
				}
			}
		}
		
		subSize = Integer.parseInt(bTreeFile.substring(secondDot+1, firstDot));
		
		String newFileName = fileName+ "_query"+subSize+"_result";
		
		file = new File(newFileName); // takes in file name
	
		System.out.print("Searching BTree...");
		
		beginSearch(subSize);
		
		System.out.println("\nSearch complete. Query results were stored in " + newFileName);
	}
	
	private static void beginSearch(int k) throws Exception {
		
		RandomAccessFile raf = new RandomAccessFile(new File(bTreeFile), "rw");
		BTree tree = new BTree(raf, true);
		FileWriter fileWriter = new FileWriter(file);
		File file = new File(queryFile); 
		Scanner scanner = new Scanner(file);
		String token;
		int wait = 0;
		
		if (withCache==1) 
		{
			BTreeCache inCache = new BTreeCache(cacheSize); 
			tree.setCacheUse(inCache);
		}
		
		while (scanner.hasNextLine()) {

			wait++;
			if (wait%100==0) 
			{
				System.out.print(".");
			}
			if (wait%2000==0)
			{
				System.out.print("\n");
			}
			
			token = scanner.nextLine().toLowerCase();
			int freq = tree.search(toLong(token));
			
			if (freq != 0) {
				
				String str = backToString(toLong(token), freq, subSize);
				
				fileWriter.write(str + "\n");
				
				if (debugLevel == 0) {
					
					System.out.println(str);
				}
			}
		}
		
		scanner.close();
		fileWriter.close();
		
	}
	
	private static long toLong(String subString) {	
		
		String bineString = "";
		
		for (int i = 0 ; i < subSize ; i++) {
			
			if(subString.charAt(i) == 'a'||subString.charAt(i) == 'A') {
				
				bineString += "00";
				continue;
			} 
			
			else if(subString.charAt(i) == 't'|| subString.charAt(i) == 'T') {
				
				bineString += "11";
				continue;
			} 
			
			else if(subString.charAt(i) == 'c'|| subString.charAt(i) == 'C') {
				
				bineString += "01";
				continue;
			} 
			
			else if(subString.charAt(i) == 'g'|| subString.charAt(i) == 'G') {
				
				bineString += "10";
				continue;
			}				
		}
		
		long stream = 0;
		int factor = 1;
		
		for (int i = bineString.length()-1; i >= 0; i--) {
			
			stream += ((int) bineString.charAt(i) - 48) * factor;
			factor = factor*2;
		}
		
		return stream;
	}
	
	private static String backToString(long stream, int frequency, int stringLength) {
		
		String string = Long.toBinaryString(stream);
		StringBuilder stringBuilder = new StringBuilder();
		
		int x = Math.abs(string.length()-(2*stringLength));
		
		for (int i = 0; i < x; i++) {
			
			stringBuilder.append('0');
		}
		
		stringBuilder.append(string);
		
		String val = stringBuilder.toString();
		
		stringBuilder = new StringBuilder();
		
		for (int i = 0; i < val.length(); i+=2) {
			
			String substr = val.substring(i, i+2);
			
			if (substr.equals("00")) {
				
				stringBuilder.append('a');
			}
			
			else if(substr.equals("01")) {
				
				stringBuilder.append('c');
			}
			
			else if(substr.equals("10")) {
				
				stringBuilder.append('g');
			}
			
			else if(substr.equals("11")) {
				
				stringBuilder.append('t');
			}
		}
		
		stringBuilder.append(": " + frequency);
		return stringBuilder.toString();
	}
	
	private static void printUsageError() {
		
		System.out.println("<0/1(no/with Cache)> <btree file> <query file> [<cache size>] [<debug level>]");
		System.err.println("There was an error with your input. Use the following format: ");
	}

}
