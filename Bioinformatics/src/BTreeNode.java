import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Alex Acevedo
 * 
 */

public class BTreeNode {
	
	@SuppressWarnings("unused")
	private BTreeNode parent;
	private RandomAccessFile randomAccess;
	private List<BTreeNode> children;
	private List<TreeObject> objects;
	private List<Integer> childPointer;
	private int parentPoint;
	boolean isLeaf;
	private int numObjects;
	int byteOffset; //should point to the first byte of the node
	public int maxObjects;
	private int maxPointers;
	
	
	public BTreeNode(int byteOffset, int maxObjects, RandomAccessFile randomAccess) {
		
		this.byteOffset = byteOffset;
		this.randomAccess = randomAccess;
		
		objects = new ArrayList<TreeObject>();
		childPointer = new ArrayList<Integer>();
		objects.add(new TreeObject(-1, -1));
		childPointer.add(-1);
		
		this.maxObjects = maxObjects;
		this.maxPointers = maxObjects + 1;
		
		parentPoint = 0;
		children = new ArrayList<BTreeNode>();
		children.add(null);
		
		this.numObjects = 0;
	}
	
	public boolean containsObject(TreeObject input) 
	{
		if (objects.contains(input)) 
		{
			return true;
		}
		return false;
	}
	
	public void setParent(BTreeNode n) {
		
		this.parent = n;
	}
	
	public void setObjects(List<TreeObject> list) {
		
		this.objects = list;
	}
	
	public void setChildPointers(List<Integer> list) {
		
		this.childPointer = list;
	}
	
	public void setParentPointer(int p) {
		
		this.parentPoint = p;
	}
	
	public int getParentPointer() {
		
		return parentPoint;
	}

	public void setNumObjects(int x) {
		
		this.numObjects = x;
	}
	
	public int getNumChldPtrs() {
		
		return childPointer.size() - 1;
	}
	
	public int getNumObjects() {
		
		return this.numObjects;
	}
	
	public void setObject(int index, TreeObject object) {
		
		if (index < objects.size()) {
			
			objects.set(index, object);
		}
		
		else {
			
			objects.add(object);
		}
	}

	public void addObject(int index, TreeObject object) {
		
		objects.add(index, object);
	}
	
	public TreeObject getObject(int index) {
		
		TreeObject object = objects.get(index);
		
		return object;
	}
	
	public void setIsLeaf(int x) {
		
		if (x == 1) {
			
			isLeaf = true;
		}
		
		else isLeaf = false;
	}
	
	public boolean getIsLeaf() {
		
		if (childPointer.size() == 1) {
			
			return true;
		}
		
		else return false;
	}
	
	public void setChild(int index, int n) {
		
		if (index < childPointer.size()) {
			
			childPointer.set(index, n);
		}
		
		else if (index == childPointer.size()) {
			
			childPointer.add(n);
		}
	
	}
	
	public int getChild(int index) {
		
		return this.childPointer.get(index);
	}
	
	public void removeChild(int index) {
		
		childPointer.remove(index);
	}
	
	public void removeObject(int index) {
		
		objects.remove(index);
	}
	
	public void diskWrite() throws Exception {
		
	    randomAccess.seek(byteOffset); // traverse to byte offset
	    randomAccess.writeInt(byteOffset);
	    randomAccess.writeInt(getNumObjects());
	    
	    if (isLeaf) {
	    	
	    	randomAccess.writeInt(1);
	    }
	    
	    else randomAccess.writeInt(0);
	    
	    randomAccess.writeInt(parentPoint);
	    
	    int i, j;
	    
	    //Write all pointers. Unused pointers will be written as 0.
	    for (i = 1; i < childPointer.size(); i++) {
	    	
	    	if (i <= maxObjects+1) {
	    		
	    		randomAccess.writeInt(childPointer.get(i));
	    	}
	    }
	    for (j = i; j <= maxPointers; j++) {
	    	
	    	randomAccess.writeInt(-1);
	    }
	    
	    for (i = 1; i <= numObjects; i++) {
	    	
	    	if (i <= maxObjects) {
	    		
	    		randomAccess.writeLong(objects.get(i).getValue());
		    	randomAccess.writeInt(objects.get(i).getFrequency());
	    	}
	    }
	    
	    for (j = i; j <= maxObjects; j++) {
	    	
	    	randomAccess.writeLong(-1);
	    	randomAccess.writeInt(0);
	    }	    
	}
	
	public void diskWriteAsRoot() throws Exception {
		
	    randomAccess.seek(4); // Go to byte at offset position 4.
	    randomAccess.writeInt(byteOffset);
	    randomAccess.writeInt(getNumObjects());
	    
	    if (isLeaf) {
	    	
	    	randomAccess.writeInt(1);
	    }
	    
	    else randomAccess.writeInt(0);
	    
	    randomAccess.writeInt(parentPoint);
	    
	    int i, j;
	    
	    //Write all pointers. Unused pointers will be written as 0.
	    for (i = 1; i < childPointer.size(); i++) {
	    	
	    	if (i <= maxObjects+1) {
	    		
	    		randomAccess.writeInt(childPointer.get(i));
	    	}
	    }
	    
	    for (j = i; j <= maxPointers; j++) {
	    	
	    	randomAccess.writeInt(-1);
	    }
	    
	    for (i = 1; i <= numObjects; i++) {
	    	
	    	if (i <= maxObjects) {
	    		
	    		randomAccess.writeLong(objects.get(i).getValue());
		    	randomAccess.writeInt(objects.get(i).getFrequency());
	    	}
	    }
	    
	    for (j = i; j <= maxObjects; j++) {
	    	
	    	randomAccess.writeLong(-1);
	    	randomAccess.writeInt(0);
	    }	    
	}
	
	public BTreeNode diskRead(int childIndex) throws Exception {
		
		BTreeNode newNode = new BTreeNode(childPointer.get(childIndex), maxObjects, randomAccess);
		
	    randomAccess.seek(childPointer.get(childIndex));
	    randomAccess.readInt();
	    
	    newNode.setNumObjects(randomAccess.readInt());
	    newNode.setIsLeaf(randomAccess.readInt());
	    
	    randomAccess.readInt();
	    
	    newNode.setParentPointer(byteOffset);
	    
	    //set child pointers
	    List<Integer> pointers = new ArrayList<Integer>();
	    pointers.add(-1);
	    
	    for (int i = 0; i < maxPointers; i++) {
	    	
	    	int x = randomAccess.readInt();
	    	if (x != -1) {
	    		
	    		pointers.add(x);
	    	}
	    }
	    
	    newNode.setChildPointers(pointers);
	    
	    //set tree objects
	    List<TreeObject> treeObjects = new ArrayList<TreeObject>();
	    treeObjects.add(null);
	    
	    for (int i = 0; i < maxObjects; i++) {
	    	
	    	long x = randomAccess.readLong();
	    	int y = randomAccess.readInt();
	    	
	    	if (x != -1) {
	    		
	    		treeObjects.add(new TreeObject(x, y));
	    	}
	    }
	    
	    newNode.setObjects(treeObjects);
	    return newNode;
	}
	
	public BTreeNode diskReadRoot() throws IOException {
		
		BTreeNode newNode = new BTreeNode(4, maxObjects, randomAccess);
		
	    randomAccess.seek(4);
	    randomAccess.readInt();
	    
	    newNode.setNumObjects(randomAccess.readInt());
	    newNode.setIsLeaf(randomAccess.readInt());
	    
	    randomAccess.readInt();
	    
	    newNode.setParentPointer(byteOffset);
	    
	    //set child pointers
	    List<Integer> ptrs = new ArrayList<Integer>();
	    ptrs.add(-1);
	    
	    for (int i = 0; i < maxPointers; i++) {
	    	
	    	int x = randomAccess.readInt();
	    	
	    	if (x != -1) {
	    		
	    		ptrs.add(x);
	    	}
	    }
	    
	    newNode.setChildPointers(ptrs);
	    
	    //set tree objects
	    List<TreeObject> treeObjs = new ArrayList<TreeObject>();
	    
	    treeObjs.add(null);
	    
	    for (int i = 0; i < maxObjects; i++) {
	    	
	    	long x = randomAccess.readLong();
	    	int y = randomAccess.readInt();
	    	
	    	if (x != -1) {
	    		
	    		treeObjs.add(new TreeObject(x, y));
	    	}
	    }
	    
	    newNode.setObjects(treeObjs);
	    
	    return newNode;
	} 
}