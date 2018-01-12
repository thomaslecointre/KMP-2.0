package query;

import java.util.HashMap;
import java.util.HashSet;

import model.Relation;
import model.Subject;

public class Result {

	private HashMap<String, Object> selectorMappings = new HashMap<>();

	protected void put(String identifier, Object object) {
		selectorMappings.put(identifier, object);
	}

	public HashMap<String, Object> getSelectorMappings() {
		return selectorMappings;
	}

	@Override
	public String toString() {
		
		StringBuilder res = new StringBuilder();
		
		for (String identifier : selectorMappings.keySet()) {
			
			res.append("\n\n").append(identifier);
			Object object = selectorMappings.get(identifier);
			HashSet<SpoofResult> spoofResults = null;
			HashSet<Relation> relations = null;
			
			try {
				spoofResults = (HashSet<SpoofResult>) object;
			} catch (ClassCastException e) {
				relations = (HashSet<Relation>) object;
			} finally {
				res.append("\n[");
				if (spoofResults != null) {
					for (SpoofResult spoofResult : spoofResults) {
						res.append("\n\t").append(spoofResult.getKey()).append(", ");
						HashSet<Subject> subjects = spoofResult.getSubjects();
						if (subjects.size() > 1) {
							res.append("{ ");
							for(Subject subject : subjects) {
								res.append(subject).append(' ');
							}
							res.append('}');
						} else {
							res.append(subjects.iterator().next());
						}
					}
				}
				if (relations != null) {
					for (Relation relation : relations) {
						res.append("\n\t").append(relation);
					}
				}
				res.append("\n]");
			}
		}
		return res.toString();
	}
}
