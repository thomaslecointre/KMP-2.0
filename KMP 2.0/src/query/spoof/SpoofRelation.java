package query.spoof;

import java.util.HashSet;
import java.util.Set;

import model.Relation;

public class SpoofRelation extends SpoofData {
	
	private HashSet<Relation> relations;
	
	public SpoofRelation() {
		relations = new HashSet();
	}
	
	public SpoofRelation(int key, Set<Relation> relations) {
		this();
		this.key = key;
		this.relations.addAll(relations);
	}

	public HashSet<Relation> getRelations() {
		return relations;
	}
	
}
