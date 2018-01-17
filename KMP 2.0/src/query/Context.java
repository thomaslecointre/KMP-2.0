package query;

import java.util.HashMap;

import query.spoof.SpoofVariable;

public class Context {
	
	private HashMap<String, SpoofVariable> contextVariables = new HashMap();
	
	public boolean containsKey(String identifier) {
		return contextVariables.containsKey(identifier);
	}

	public SpoofVariable getRelations(String identifier) {
		return (SpoofVariable) contextVariables.get(identifier);
	}

	public SpoofVariable getSpoofResults(String identifier) {
		return (SpoofVariable) contextVariables.get(identifier);
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

	public void put(String identifier, SpoofVariable object) {
		contextVariables.put(identifier, object);
	}

}
