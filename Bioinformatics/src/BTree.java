import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author Alex Acevedo
 * 
 */

public class BTree {
	private BTreeNode nodeOnMemory;
	private RandomAccessFile randomAccess;
	private BTreeNode root;
	Boolean bool = false;
	private int degree;	
	private int maxLoad;
	int cursor; 
	
	/**
	 * Constructor method for BTree
	 */
	public BTree(int degree, RandomAccessFile randomAccess) throws Exception {
		
		this.degree = degree;
		this.randomAccess = randomAccess;
		
		cursor = 0;
		maxLoad = (2*degree)-1;
		
		initialize();
		
		root = new BTreeNode(allocateNode(), maxLoad, randomAccess);
		
		root.isLeaf = true;
		
		BTreeNode x = root;
		
		nodeOnMemory = x;
		
		x.setIsLeaf(1);
		x.setNumObjects(0);
		x.diskWrite();
	}
	
	public BTree(RandomAccessFile randomAccess, boolean exists) throws IOException {
		
		nodeOnMemory = root;
		
		randomAccess.seek(0);
		this.degree= randomAccess.readInt();
		
		maxLoad = (2*degree)-1;
		root = new BTreeNode(4, maxLoad, randomAccess);
		root = root.diskReadRoot();
	}
	
	private void initialize() throws Exception {
		
		randomAccess.seek(0);
		randomAccess.writeInt(degree);
		cursor = 4;
		
		allocateNode();
	}
	
	private int allocateNode() throws Exception  {
		
		int x = cursor;
		
	    randomAccess.seek(cursor); // Go to byte at offset position 5.
	    randomAccess.writeInt(cursor);
	    randomAccess.writeInt(0);
	    randomAccess.writeInt(0);
	    cursor += 3*4;
	    
	    //Initialize all pointers to -1
	    for (int i = 1; i <= (2*degree + 1); i++) {
	    	
	    	randomAccess.writeInt(-1);
	    	cursor+=4;
	    }
	    
	    //Initialize all TreeObject values
	    for (int i = 1; i <= (2*degree - 1); i++) {
	    	
	    	randomAccess.writeLong(-1);
	    	randomAccess.writeInt(0);
	    	cursor += 12;
	    }
	    return x;
	}
	
	private boolean splitTree(BTreeNode node, int index, TreeObject input) throws Exception {
		
		BTreeNode tempNode = node.diskRead(index);
		int obj = tempNode.getNumObjects();
		
		for (int i = 1; i <= obj; i ++) {
			
			if (input.getValue() == tempNode.getObject(i).getValue()) {
				
				tempNode.getObject(i).incrFreq();
				tempNode.diskWrite();
				
				return false;
			}
		}
		return true;
	}
	
	private void splitChild(BTreeNode parentNode, int index) throws Exception {
		
		//Creates a new node that will house the data for the right child after the split
		BTreeNode newRightNode = new BTreeNode(allocateNode(), maxLoad, randomAccess);		
		
		//Pulls the full child and stores 
		BTreeNode newLeftNode = parentNode.diskRead(index);
		newRightNode.isLeaf = parentNode.getIsLeaf();
		
		//Puts objects from the full node to the right node 	
		for (int j = 1; j < degree; j++) {
			
			newRightNode.setObject(j, newLeftNode.getObject(j + degree));
		}
		
		if (!newLeftNode.getIsLeaf()) {
			
			//now moving child pointers
			for (int j = 1; j <= degree; j++) {
				
					newRightNode.setChild(j, newLeftNode.getChild(j + degree));
			}
		}
		
		//Moving the parent node child pointers to add the right node
		for (int j = parentNode.getNumObjects()+1; j > index; j--) {
			
			parentNode.setChild(j+1, parentNode.getChild(j));
		}
		
		//Adds right node as child for parent node
		parentNode.setChild(index + 1, newRightNode.byteOffset);
		
		//Creates the space for the median object to move up
		for (int j = parentNode.getNumObjects(); j >= index; j--) {
			
			parentNode.setObject(j+1, parentNode.getObject(j));
		}
		
		//Moving the median object from the left node to the parent
		parentNode.setObject(index, newLeftNode.getObject(degree));
		parentNode.setNumObjects(parentNode.getNumObjects()+1);
		
		for (int j = newLeftNode.getNumObjects(); j > degree; j--) {
			
			newLeftNode.removeObject(j);
		}
		
		if (!newLeftNode.getIsLeaf()) {
			
			for (int j = newLeftNode.getNumChldPtrs(); j > degree; j--) {
				
						newLeftNode.removeChild(j);
			}
		}	
		newLeftNode.removeObject(degree);
		
		//Updates all object counts for relevant nodes
		parentNode.setIsLeaf(0);
		newRightNode.setNumObjects(degree-1);
		newLeftNode.setNumObjects(degree-1);
		newLeftNode.diskWrite();
		newRightNode.diskWrite();		
		parentNode.diskWrite();		
	
	}
	
	public void insertNode(TreeObject input) throws Exception {
		
		BTreeNode tempRoot = root;
		
		if (root.getNumObjects() == maxLoad) { //When root node is full
			
			BTreeNode newRoot = new BTreeNode(allocateNode(), maxLoad, randomAccess);
			
			newRoot.isLeaf = false;
			newRoot.setNumObjects(0);
			newRoot.setChild(1, tempRoot.byteOffset);
			
			if (!splitTree(newRoot, 1, input) ) {
				
				return;
			}
				splitChild(newRoot, 1);
				insertNonfull(newRoot, input);
				
				root = newRoot;
				
		} else {
			
			insertNonfull (root, input);
		}
	}
	
	private void insertNonfull(BTreeNode ancestor, TreeObject input) throws Exception {
		
		int i = ancestor.getNumObjects();
		
		for (int j = 1; j <= i; j++) {
			
			if (input.getValue() == ancestor.getObject(j).getValue()) {
				
				ancestor.getObject(j).incrFreq();
				ancestor.diskWrite();
				
				return;
			}
		}
		
		//Will recurse to traverse the tree until a leaf is reach for insertion 
		if (ancestor.getIsLeaf()) {
			
			//Iterates through node until the insert position is located
			while (i >= 1 && input.getValue() < ancestor.getObject(i).getValue() ) {
				
					ancestor.setObject(i+1, ancestor.getObject(i));
					i--;		
			}
			
			ancestor.setObject(i+1, input);
			ancestor.setNumObjects(ancestor.getNumObjects()+1);
			ancestor.diskWrite();
			
		} else { //If relevant node is internal
			
			//Iterates through the node until the relevant child node is located and stores it to memory, will recurse on that child
			while (i >= 1 && input.getValue() < ancestor.getObject(i).getValue()) {
				
				i--;
			}
				i++;		
				
				nodeOnMemory = ancestor.diskRead(i);

				if (nodeOnMemory.getNumObjects() == maxLoad) {
				
					if (!splitTree(ancestor, i, input)) {
						
						return;
					}
					
					splitChild(ancestor, i);
					nodeOnMemory = ancestor.diskRead(i);
					
					//After the split will enter if input is larger than all objects in left node
					if (input.getValue() > ancestor.getObject(i).getValue()) {
						
						i++;
						nodeOnMemory = ancestor.diskRead(i);
					}
				} 
				
				insertNonfull(nodeOnMemory, input);
			}
	}
	
	public void finish() throws Exception {
		
		randomAccess.seek(4);
		root.diskWriteAsRoot();
	}
	
	public int search(long k) {
		
		return searchTree(root, k);
	}
	
	private int searchTree(BTreeNode x, long k) {
		try {
			
			int i = 1;
			
			while (i <= x.getNumObjects() && k > x.getObject(i).getValue()) {
				
				i++;
			}
			
			if (i <= x.getNumObjects() && k == x.getObject(i).getValue()) {
				
				return x.getObject(i).getFrequency();
			}
			
			else if (x.getIsLeaf()) {
				
				return 0;
			}
			else {
				
				BTreeNode y = x.diskRead(i);
				return searchTree(y, k);
			}
		}
		
		catch(Exception e) {
			
			e.printStackTrace();
			return 0;
		}
	}
	
	public void traverseTree(String file, int longValue, int subLength) throws Exception {
		
		FileWriter fileWriter = new FileWriter(new File(file+".btree.dump."+subLength));
		
		treeTraverse(root, fileWriter, subLength);
		
		fileWriter.close();
		
		int x = GeneBankCreateBTree.subSize;
		
		System.out.println("Results were stored in: " + file + ".btree.dump." + x);
		
	}
	
	private void treeTraverse(BTreeNode rootTraverse, FileWriter fileWriter, int subLength) throws Exception {
		
		int child = rootTraverse.getNumChldPtrs();
		int object = rootTraverse.getNumObjects();
		int value = subLength;
		
		for (int i = 1; i <= child; i++) {
			
			treeTraverse(rootTraverse.diskRead(i), fileWriter, value);
			
			if (object >= i) {
				
				String string = convertBackToString(rootTraverse.getObject(i).getValue(), rootTraverse.getObject(i).getFrequency(), value);
				fileWriter.write(string + "\n");
			}	
		}
		
		if (rootTraverse.getIsLeaf()) {
			
			for (int j = 1; j <= object; j++) {
				
				String string = convertBackToString(rootTraverse.getObject(j).getValue(), rootTraverse.getObject(j).getFrequency(), value);
				fileWriter.write(string + "\n");
			}
		}	
	}
	
	private String convertBackToString(long stream, int frequency, int stringLength) {
		
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
			
			String subString = val.substring(i, i+2);
			
			if (subString.equals("00")) {
				
				stringBuilder.append('a');
			}
			
			else if(subString.equals("01")) {
				
				stringBuilder.append('c');
			}
			
			else if(subString.equals("10")) {
				
				stringBuilder.append('g');
			}
			
			else if(subString.equals("11")) {
				
				stringBuilder.append('t');
			}
		}
		
		stringBuilder.append(": " + frequency);
		
		return stringBuilder.toString();
	}
}