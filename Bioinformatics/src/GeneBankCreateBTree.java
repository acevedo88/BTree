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
	static BTreeCache cache;

	@SuppressWarnings({ "unused", "resource" })
	public static void main(String args[]) {

		try {

			if (args.length < 4 || args.length > 6) 
			{
				System.err.println("Incorrect number of arguments");
				printUsage();
			}
			if (Integer.parseInt(args[0]) == 0 || Integer.parseInt(args[0]) == 1) 
			{
				//if no cache requested, set cache size to zero for it to be ignored.

				if (Integer.parseInt(args[0]) == 0) 
				{
					cache = new BTreeCache(0);
				}
				//If cache is specified, do not create a cache until size specified. 
				else{}
			}  

			else 
			{
				System.err.println("Cache argument must be 0 or 1");
				printUsage();
			}

			if (args.length > 4) 
			{
				if (args.length == 5) 
				{
					//If the cache has not been initialized yet, that means the cache argument was not 0. 
					if (cache==null)
					{
						cache = new BTreeCache(Integer.parseInt(args[4]));
					}

					//If cache was previously initialized to 0, then that means that we have no cache size argument,
					//Treat this argument as debug argument.
					if (cache.capacity()==0&&(Integer.parseInt(args[4]) == 1 || Integer.parseInt(args[4]) == 0)) 
					{
						if (Integer.parseInt(args[4]) == 1)
						{
							debug = true;
						}
					}

					//Handle incorrect debug argument input.
					else if(cache.capacity()==0&&(Integer.parseInt(args[4])!=1||Integer.parseInt(args[4])!=0)) 
					{
						System.err.println("Debug argument must be 0 or 1");
						printUsage();
					}
				}

				//Cache requested with mandatory size and debug argument fed in.
				if (args.length == 6)
				{
					cache = new BTreeCache(Integer.parseInt(args[4]));

					if (Integer.parseInt(args[5])==1)
					{
						debug = true;
					}
				}
			}

			//Sets the degree size for the nodes as the optimal given our memory structure with a zero argument.
			if (Integer.parseInt(args[1]) == 0) 
			{
				degree = (blockSize-4)/32;
			} 

			//If degree specified as other than zero, then set degree to that argument.
			else 
			{
				degree = Integer.parseInt(args[1]); // takes in degree t
			}

			try 
			{
				file1 = new File(args[2]);
			} 
			catch(Exception e) 
			{
				System.err.println("File does not exist");
				printUsage();
			}

			//Take in argument for substring length.
			if (Integer.parseInt(args[3]) >= 1 || Integer.parseInt(args[3]) <= 31) 
			{
				subSize = Integer.parseInt(args[3]); // Substring length
			} 

			else 
			{
				System.err.println("The sequence length must be between 1 and 31 inclusively");
				printUsage();
			}

			file = new File(args[2]+".btree.data."+subSize+"."+degree); 

			RandomAccessFile randomAccess = new RandomAccessFile(file, "rw");

			tree = new BTree(degree, randomAccess);
			boolean foundStart = false;

			String subString = "";
			TreeObject object;
			System.out.print("Generating BTree, please wait.....");
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader input = new BufferedReader(new FileReader(file1)); 
			String lineToken;
			int wait = 0;
			if (cache.capacity()>=1) 
			{
					tree.setCacheUse(cache);
			}
			

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

						else if (token == 'a' || token == 't' || token == 'c' || token == 'g' || token == 'A' || token == 'T' || token == 'C' || token == 'G') 
						{	

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

							wait++;
							if (wait%100==0)
							{
								System.out.print(".");
							}
							if (wait%2000==0)
							{
								System.out.print("\n");
							}
							
							tree.insertNode(object);
						}												
					}					
				}

				lineScan.close();
			}	

			tree.finish();
			System.out.println("\nBTree Generated");
			
			if (debug) {

				tree.traverseTree(args[2], degree, subSize);
			}

		} 

		catch (FileNotFoundException f) {

			System.err.println("\nFile does not exist");
			printUsage();

		} 

		catch (Exception e) {

			e.printStackTrace();
			System.out.println(
					"\njava GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		} 
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

	private static void printUsage() {

		System.out.println("java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(1);
	}
}