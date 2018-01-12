package query;

import java.util.ArrayList;
import java.util.HashSet;

import model.Relation;

public class Context {
	
	private ArrayList<ArrayList<Pair<String, Object>>> contextVariables = new ArrayList<>();
	private ArrayList<Pair<String, Object>> latestSubContext;

	public boolean containsKey(String identifier) {
		for(ArrayList<Pair<String, Object>> subContext : contextVariables) {
			for(Pair<String, Object> variable : subContext) {
				if (variable.left.equals(identifier)) {
					return true;
				}
			}
		}
		return false;
	}

	public HashSet<Relation> getRelations(String identifier) {
		for(ArrayList<Pair<String, Object>> subContext : contextVariables) {
			for(Pair<String, Object> variable : subContext) {
				if (variable.left.equals(identifier)) {
					return (HashSet<Relation>) variable.right;
				}
			}
		}
		return null;
	}

	public HashSet<SpoofResult> getSpoofResults(String identifier) {
		for(ArrayList<Pair<String, Object>> subContext : contextVariables) {
			for(Pair<String, Object> variable : subContext) {
				if (variable.left.equals(identifier)) {
					return (HashSet<SpoofResult>) variable.right;
				}
			}
		}
		return null;
	}

	public void addSubContext(ArrayList<Pair<String, Object>> updatedVariables) {
		contextVariables.add(updatedVariables);
		this.latestSubContext = updatedVariables;
	}

	// TODO
	public void clean() {
		
	}
	
	// TODO
	public Result generateResult(String[] selectStrings) {
		return null;
	}

}
