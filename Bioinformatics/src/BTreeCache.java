import java.util.NoSuchElementException;

/**
 * @author Alex Acevedo
 */

public class BTreeCache<T>{
	private DLLNode<T> front, rear;
	private double Hits = 0;
	private int size = 0;
	private int max_capacity;
	private double totalAccess = 0;
  

	public BTreeCache() {
		front = rear = null;
		max_capacity = 100;
	}
 
	
	public BTreeCache(int capacity) {
		front = rear = null;
		max_capacity = capacity;
	}
 
	
	public static <T> void main(String[] args) {
	}


	public T get(T target) {
		boolean found = false;
		DLLNode<T> current = front;
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
    
	
	public int size() { 
		return size;
	}

	
	public void clear() {

		front = null;  
		rear = null;
		size = 0;
	}

	
	
	public void add(T data) {
		
		DLLNode<T> member = new DLLNode<T>(data);
		
		if (isEmpty()) { 
			
			front = rear = member;
		} 
		else if (size >= max_capacity) { 
			
			removeLast(); 
			DLLNode<T> current = front;
			front = member;
			member.setNext(current);
			current.setPrev(member);

		} 
		else {
			
			DLLNode<T> current = front;  
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

	
	
	public void remove(T target) {
		
		if (isEmpty()) {
			
			throw new NoSuchElementException();
		}
		boolean found = false
				;
		DLLNode<T> current = front;
		
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

	
	
	public void write(T data) {
		
		if (isEmpty()) { 
			
			throw new NoSuchElementException();
		}
		boolean found = false;
		
		DLLNode<T> current = front;

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
	
	
	public double getHit() {
		
		return Hits;
	}
	
	
    public double getAccess() {
    	
    	return totalAccess;
    } 
  
	
}