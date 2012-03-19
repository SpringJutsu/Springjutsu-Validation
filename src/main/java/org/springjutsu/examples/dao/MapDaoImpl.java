package org.springjutsu.examples.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MapDaoImpl<ID extends Serializable, T> {
	
	private Map<ID, T> daoMap = new HashMap<ID, T>();
	
	public T get(ID id) {
		if (!daoMap.containsKey(id)) {
			throw new IllegalArgumentException("No object with id: " + id);
		}
		return daoMap.get(id);
	}
	
	protected void save(ID id, T entity) {
		daoMap.put(id, entity);
	}

}
