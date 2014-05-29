package com.myuplay.matb;

import java.util.ListIterator;


public abstract class PeekingIterator<T> implements ListIterator<T>{
	
	/**
	 * Returns true if there is an element before the current.
	 * @return
	 */
	public abstract boolean hasPrevious();
	
	/**
	 * Returns true if there exists an element at position i relative to
	 * current position.
	 * @param i Index
	 * @return
	 */
	public abstract boolean has(int i);
	
	/**
	 * Peeks the next element. Does not move the iterator.
	 * @return
	 */
	public abstract T peek();
	
	/**
	 * Peeks the x element in reference to the current position.
	 * Returns null if out of bounds. 0 would return the current
	 * element.
	 * @param i Index
	 * @return
	 */
	public abstract T peek(int i);

}
