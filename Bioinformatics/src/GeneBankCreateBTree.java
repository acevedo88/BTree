import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * 
 * @author Alex Acevedo
 * 
 */

public class GeneBankCreateBTree {
	static BTree tree;
	static File file;
	static File file1;
	static File dump;
	static int blockSize = 4096;
	static boolean debug = false;
	static int subSize = 0; 
	static int degree = 0;

	@SuppressWarnings({ "unused", "resource" })
	public static void main(String args[]) {

		try {

			if (args.length < 4 || args.length > 6) {

				System.err.println("Incorrect number of arguments");
				printUsage();

			}

			if (Integer.parseInt(args[0]) == 0 || Integer.parseInt(args[0]) == 1) {

				if (Integer.parseInt(args[0]) == 1) {

					//TODO Need Cache Code
				} 
				
				else {
					//TODO Code for not using Cache 
				}
			} 
			
			else {

				System.err.println("Cache argument must be 0 or 1");
				printUsage();
			}
			
			if (args.length > 4) {
				//TODO Add code to set cache size
				//TODO Does not handle incorrect input for cache size

				if (args.length > 5) {

					if (Integer.parseInt(args[5]) == 1 || Integer.parseInt(args[5]) == 0) {

						if (Integer.parseInt(args[5]) == 1) {

							debug = true;
						}
					} 
					else {

						System.err.println("Debug argument must be 0 or 1");
						printUsage();
					}
				}
			}

			//Sets the degree size for the nodes
			if (Integer.parseInt(args[1]) == 0) {

				degree = (blockSize-4)/32;
			} 
			
			else {

				degree = Integer.parseInt(args[1]); // takes in degree t
			}
			
			try {

				file1 = new File(args[2]);
			} 
			
			catch(Exception e) {

				System.err.println("File does not exist");
				printUsage();
			}
			
			if (Integer.parseInt(args[3]) >= 1 || Integer.parseInt(args[3]) <= 31) {

				subSize = Integer.parseInt(args[3]); // Substring length
			} 
			
			else {

				System.err.println("The sequence length must be between 1 and 31 inclusively");
				printUsage();
			}
			
			file = new File(args[2]+".btree.data."+subSize+"."+degree); 
			
			RandomAccessFile randomAccess = new RandomAccessFile(file, "rw");

			tree = new BTree(degree, randomAccess);
			boolean foundStart = false;
			
			String subString = "";
			TreeObject object;
			System.out.println("Generating BTree, please wait.....");
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader input = new BufferedReader(new FileReader(file1)); 
			String lineToken;
			
			while ((lineToken = input.readLine()) != null)  {
				
				Scanner lineScan = new Scanner(lineToken);
				String string2 = lineToken.replaceAll("\\s", "");
				String string = string2.replaceAll("\\d", "");
				
				if (string.equals("ORIGIN")) {
					
					foundStart = true;
				} 
				
				else if (lineToken.equals("//")) {
					
					foundStart = false;
					stringBuilder = new StringBuilder();
				}
				
				else if (foundStart == true) {
					
					for (int i =0; i < string.length(); i++) {
						
						char token = string.charAt(i);	
						
						if (token == 'n' || token == 'N') {
							
							stringBuilder = new StringBuilder();
						} 
						
						else if (token == 'a' || token == 't' || token == 'c' || token == 'g' || token == 'A' || token == 'T' || token == 'C' || token == 'G') {	
							
							stringBuilder.append(Character.toLowerCase(token));
						}	
						
						if (stringBuilder.length() > subSize) {
							
							String st = stringBuilder.toString();
							
							stringBuilder = new StringBuilder();
							stringBuilder.append(st.substring(1,subSize +1));
						}
						
						if (subSize == stringBuilder.length()) {
							
							long stream = toLong(stringBuilder.toString());						
							object = new TreeObject(stream);
							
							tree.insertNode(object);
							object = new TreeObject(stream);
						}												
					}					
				}
				
				lineScan.close();
			}	
			
			tree.finish();
			System.out.println("BTree Generated");
			
			if (debug) {
				
				tree.traverseTree(args[2], degree, subSize);
			}
			
		} 
		
		catch (FileNotFoundException f) {
			
			System.err.println("File does not exist");
			printUsage();
			
		} 
		
		catch (Exception e) {
			
			e.printStackTrace();
			System.out.println(
					"java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		} 
	}
	
	private static long toLong(String subString) {
		
		String binaryString = "";
		
		for (int i = 0 ; i < subSize ; i++) {
			
			if(subString.charAt(i) == 'a'||subString.charAt(i) == 'A') {
				
				binaryString += "00";
				continue;
			}
			
			else if(subString.charAt(i) == 't'|| subString.charAt(i) == 'T') {
				
				binaryString += "11";
				continue;	
			} 
			
			else if(subString.charAt(i) == 'c'|| subString.charAt(i) == 'C') {
				
				binaryString += "01";
				continue;
			} 
			
			else if(subString.charAt(i) == 'g'|| subString.charAt(i) == 'G') {
				
				binaryString += "10";
				continue;
			}				
		}
		
		long stream = 0;
		int factor = 1;
		
		for (int i = binaryString.length()-1; i >= 0; i--) {
			
			stream += ((int) binaryString.charAt(i) - 48) * factor;
			factor = factor*2;
		}
		
		return stream;
	}
	
	private static void printUsage() {
		
		System.out.println("java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(1);
	}
}