package query;

import java.util.HashMap;
import java.util.HashSet;

import model.Relation;
import model.Subject;
import query.spoof.SpoofSubject;
import query.spoof.SpoofVariable;

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
			SpoofVariable spoofResults = null;
			HashSet<Relation> relations = null;
			
			try {
				spoofResults = (SpoofVariable) object;
			} catch (ClassCastException e) {
				relations = (HashSet<Relation>) object;
			} finally {
				res.append("\n[");
				if (spoofResults != null) {
					if (!spoofResults.usedOnTheRight()) {
						for (SpoofSubject spoofResult : spoofResults.getSpoofDataSet()) {
							res.append("\n\t").append("Key : ").append(spoofResult.getKey()).append(" => ");
							Subject id = spoofResult.getID();
							res.append("ID : ").append(id);
						}
					} else {
						if (!spoofResults.isIdsTransferred()) {
							for (SpoofSubject spoofResult : spoofResults.getSpoofDataSet()) {
								res.append("\n\t").append("Key : ").append(spoofResult.getKey()).append(" => ");
								HashSet<Subject> subjects = spoofResult.getSubjects();
								if (subjects.size() > 1) {
									res.append("Subjects : { ");
									for(Subject subject : subjects) {
										res.append(subject).append(' ');
									}
									res.append('}');
								} else if (subjects.size() == 1) {
									res.append("Subject : ").append(subjects.iterator().next());
								} else {
									res.append("Subjects : NO SUBJECTS FOUND! ");
								}
							}
						} else {
							for (SpoofSubject spoofResult : spoofResults.getSpoofDataSet()) {
								res.append("\n\t").append("Key : ").append(spoofResult.getKey()).append(" => ");
								Subject id = spoofResult.getID();
								res.append("ID : ").append(id);
							}
						}
					}
				}
				if (relations != null) {
					for (Relation relation : relations) {
						res.append("\n\t").append("Relation : ").append(relation);
					}
				}
				res.append("\n]");
			}
		}
		return res.toString();
	}
}
