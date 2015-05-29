package com;

import java.util.Hashtable;
import java.util.Map;

public class TwoWayHashMap<K extends Object, V extends Object> {
	  private Map<K,V> forward = new Hashtable<K, V>();
	  private Map<V,K> backward = new Hashtable<V, K>();

	  public synchronized boolean put(K key, V value) {
	    V t1=forward.put(key, value);
	    K t2=backward.put(value, key);
	    return (t1!=null)&(t2!=null);
	  }

	  public synchronized boolean remove(K key){
		  V v=forward.remove(key);
		  K k=backward.remove(v);
		  
		  return (k!=null)&(v!=null);
	  }
	  public synchronized V getForward(K key) {
	    return forward.get(key);
	  }

	  public synchronized K getBackward(V key) {
	    return backward.get(key);
	  }
}