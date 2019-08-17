import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author Alex Acevedo
 */

public class BTreeCache
{
	private DLLNode<BTreeNode> front, rear;
	private LinkedList<BTreeNode> thisCache;
	private double Hits = 0;
	private int size = 0;
	private int max_capacity;
	private double totalAccess = 0;
	
	//Default constructor - Unused as if use cache capacity is mandatory argument.
	public BTreeCache() 
	{
		front = rear = null;
		max_capacity = 100;
		thisCache = new LinkedList<BTreeNode>();

	}

	//Constructor for specified capacity
	public BTreeCache(int capacity) 
	{
		thisCache = new LinkedList<BTreeNode>();
		front = rear = null;
		max_capacity = capacity;
	}
	
	//
	/**
	 * Walks through Nodes stored in cache, returning true once it finds a node which contains 
	 * a value matching that objects value. 
	 * @param input - Value to search for an equivalence of
	 * @return True or false pending results of search
	 */
	public Boolean checkCache(TreeObject input) 
	{
		//Check number of nodes we have to check first.
		int i = size;

		//Check every node for presence of object.
		for (int j = 1; j <= i; j++) 
		{
			BTreeNode toCheck = thisCache.get(j);
			int l = toCheck.getNumObjects();

			//Walk through node and compare input to objects stored in the node.
			for (int k = 1; k <= l; k++) 
			{
				if (input.getValue() == toCheck.getObject(k).getValue()) 
				{
					addToCache(toCheck);
					return true;
				}
			}
		}
		return false;
	}
	
	public BTreeNode returnFront() 
	{
		return thisCache.getFirst();
	}
	
	public void addToCache(BTreeNode objectToAdd) 
	{
		//Error check
		if (thisCache.size() < max_capacity)
		{
			//Before adding, check to see if we are merely moving to top of list and must remove.
			if (thisCache.contains(objectToAdd)) 
			{
				thisCache.remove(objectToAdd);
			}
			//And now that we have checked, we add.
			thisCache.addFirst(objectToAdd);
		}
		//We are exceeding size, and have work to do.
		else 
		{
			//Before adding, check to see if we are merely moving to top of list and must remove.
			if (thisCache.contains(objectToAdd)) 
			{
				thisCache.remove(objectToAdd);
				thisCache.addFirst(objectToAdd);
			}
			//If it did not contain it, then we must remove the last object to make room. 
			else 
			{
				thisCache.removeLast();
				//And now we add.
				thisCache.addFirst(objectToAdd);
			}
		}
	}
	
	public int capacity() 
	{
		return max_capacity;
	}
	
	public int size() { 
		return size;
	}

	
	public BTreeNode get(BTreeNode target) {
		boolean found = false;
		DLLNode<BTreeNode> current = front;
		while (current != null && !found) {
			if (target.equals(current.getElement())) { 
				found = true;
				Hits++;

			} else {
				current = current.getNext();
			}
		}
		totalAccess++;
		if (!found) {
			add(target);  
			return null;
		}
		if (size() == 1) { 

			front = rear = current;
		} 
		else if (current == rear) { 

			rear = current.getPrev();
			current.setPrev(null);
			rear.setNext(null);
			current.setNext(front);
			front.setPrev(current);
			front = current;
		} 
		else if (current == front) {

			current = front;
		} 
		else { 

			current.getPrev().setNext(current.getNext());
			current.getNext().setPrev(current.getPrev());
			current.setNext(front);
			front.setPrev(current);
			front = current;
		}

		return current.getElement();
	}


	

	public void clear() {

		front = null;  
		rear = null;
		size = 0;
	}

	public void add(BTreeNode data) {

		DLLNode<BTreeNode> member = new DLLNode<BTreeNode>(data);

		if (isEmpty()) { 

			front = rear = member;
		} 
		else if (size >= max_capacity) { 

			removeLast(); 
			DLLNode<BTreeNode> current = front;
			front = member;
			member.setNext(current);
			current.setPrev(member);

		} 
		else {

			DLLNode<BTreeNode> current = front;  
			front = member;
			member.setNext(current);
			current.setPrev(member);

		}
		size++;
	}

	public void removeLast() {

		if (isEmpty()) { 

			throw new IllegalStateException();
		}
		if (size > 1) 
		{ 
			rear = rear.getPrev();
			rear.setNext(null);
		} 
		else {

			front = rear = null;
		}
		size--;
	}

	public void remove(BTreeNode target) {

		if (isEmpty()) {

			throw new NoSuchElementException();
		}
		boolean found = false;
		DLLNode<BTreeNode> current = front;

		while (current != null && !found) {

			if (target.equals(current.getElement())) { 

				found = true;
				Hits++;
			}
			else {

				current = current.getNext(); 
			}
		}

		if (!found) {
			throw new NoSuchElementException();
		}

		if (size() == 1) { 

			front = rear = null;
		} 
		else if (current == front) { 

			front = current.getNext();
		} 
		else if (current == rear) { 

			rear = current.getPrev();
			current.setPrev(null);
			rear.setNext(null);
		} 

		else {    

			current.getPrev().setNext(current.getNext());
			current.getNext().setPrev(current.getPrev());
		}
		size--;
	}

	public void write(BTreeNode data) {

		if (isEmpty()) { 

			throw new NoSuchElementException();
		}
		boolean found = false;

		DLLNode<BTreeNode> current = front;

		while (current != null && !found) {

			if (data.equals(current.getElement())) { 

				found = true;
			} 

			else {
				current = current.getNext();   
			}
		}

		if (!found) {

			add(data);   
			throw new NoSuchElementException();
		}

		if (size() == 1) { 

			front = rear = current;
		} 
		else if (current == front) {  

			current = front;
		} 
		else if (current == rear) { 
			rear = current.getPrev();
			rear.setNext(null);
			current.setPrev(null);
			current.setNext(front);
			front.setPrev(current);
			front = current;
		} 
		else {   
			current.getPrev().setNext(current.getNext());
			current.getNext().setPrev(current.getPrev());
			current.setNext(front);
			front.setPrev(current);
			front = current;
		}

	}

	public double getHitRate() {

		double hitRate = 0;

		if (totalAccess != 0) {  
			hitRate = Hits / totalAccess;
		} 
		else {  
			return 0.00;  
		}
		return hitRate;
	}

	public double getMissRate() {

		double missRate = 0;
		missRate = 1 - getHitRate(); 

		return missRate;
	}

	public boolean isEmpty() {

		if (size == 0) { 
			return true;
		} 
		else {
			return false;
		}
	}
}