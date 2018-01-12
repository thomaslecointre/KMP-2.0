package query;

import java.util.HashMap;
import java.util.HashSet;

import model.Relation;

public class Context {
	
	private HashMap<String, Object> contextVariables = new HashMap();
	
	public boolean containsKey(String identifier) {
		return contextVariables.containsKey(identifier);
	}

	public HashSet<Relation> getRelations(String identifier) {
		return (HashSet<Relation>) contextVariables.get(identifier);
	}

	public HashSet<SpoofResult> getSpoofResults(String identifier) {
		return (HashSet<SpoofResult>) contextVariables.get(identifier);
	}
	
	public Result generateResult(String[] selectStrings) {
		
		Result result = new Result();
		
		for (String identifier : contextVariables.keySet()) {
			for(String selector : selectStrings) {
				if (identifier.equals(selector)) {
					result.put(selector, contextVariables.get(identifier));
				}
			}
		}
		
		return result;
	}

	public void put(String identifier, Object object) {
		contextVariables.put(identifier, object);
	}

}
