package com;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransferMap<K,V> implements Map<K,V>{
	  private final HashMap<K,V> backingMap = new HashMap<K,V>();

	  private final Object lock = new Object();
	  public V getAndWait(Object key, long milli){
		  V value = null;
		  int attempt=0;
	     synchronized(lock){
	         do{
	            value = backingMap.get(key);
	            attempt++;
	            if(value != null){
	            	return value;
	            }
	            try {
					lock.wait(milli);
				} catch (InterruptedException e) {
					return null;
					//e.printStackTrace();
				}
	         }while(attempt<6); 
	      }
		return value;
	  }
	  
	   public V put(K key, V value){
	      synchronized(lock){
	         V value1 = backingMap.put(key,value);
	         lock.notifyAll();
	      }
	     return value;
	   }
	   
	   @Override
	   public V putIfAbsent(K key, V value){
		   V value1=null;
		   synchronized(lock){
		        value1 = backingMap.putIfAbsent(key,value);
		         lock.notifyAll();
		      }
		   return value1;
	   }

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V get(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public V remove(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}
	  }