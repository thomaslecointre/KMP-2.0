package query;

import java.util.HashMap;
import java.util.HashSet;

import model.Relation;

public class Context {
	
	private HashMap<String, Object> contextVariables = new HashMap();
	
	public boolean containsKey(String identifier) {
		for (String variable : contextVariables.keySet()) {
			if (variable.equals(identifier)) {
				return true;
			}
		}
		return false;
	}

	public HashSet<Relation> getRelations(String identifier) {
		for (String variable : contextVariables.keySet()) {
			if (variable.equals(identifier)) {
				return (HashSet<Relation>) contextVariables.get(variable);
			}
		}
		return null;
	}

	public HashSet<SpoofResult> getSpoofResults(String identifier) {
		for (String variable : contextVariables.keySet()) {
			if (variable.equals(identifier)) {
				return (HashSet<SpoofResult>) contextVariables.get(variable);
			}
		}
		return null;
	}

	// TODO ?
	public void clean() {
		
	}
	
	// TODO
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
