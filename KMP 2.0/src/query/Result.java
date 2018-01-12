package query;

import java.util.HashMap;

public class Result {

	private HashMap<String, Object> selectorMappings = new HashMap<>();

	protected void put(String identifier, Object object) {
		selectorMappings.put(identifier, object);
	}

	public HashMap<String, Object> getSelectorMappings() {
		return selectorMappings;
	}
	
}
