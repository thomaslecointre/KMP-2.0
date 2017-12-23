package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.Data;
import model.Relation;
import model.Subject;


public class Database {

	private HashMap<Integer, EntryData> table;
	private int primaryIndex = 1;

	private ArrayList<Data> objects;

	
	public Database() {
		table = new HashMap<>();
		objects = new ArrayList<>();
	}
	
	public void insert(EntryData entryData) {
		table.put(autoIncrementValue(), entryData);
	}

	private Integer autoIncrementValue() {
		return primaryIndex++;
	}

	public void removeLastEntry() {
		if(table.containsKey(primaryIndex - 1)) {
			table.remove(primaryIndex - 1);
			primaryIndex--;
		}
	}

	public void reset() {
		table.clear();
		primaryIndex = 0;
	}

	public Subject findSubject(String id) {
		for(Data data : objects) {
			if(data.getId().equals(id)) {
				return (Subject) data;
			}
		}
		return null;
	}

	public Relation findRelation(String id) {
		for(Data data : objects) {
			if(data.getId().equals(id)) {
				return (Relation) data;
			}
		}
		return null;
	}

	public void addSubject(Subject newSubject) {
		objects.add(newSubject);
	}

	public void addRelation(Relation relation) {
		objects.add(relation);
	}

	public EntryData getEntryData(int databaseEntryNumber) {
		return table.get(databaseEntryNumber);
	}

	public int queryKey(String id) {
		for(Integer key : table.keySet()) {
			if(table.get(key).getIdAsString().equals(id)) {
				return key;
			}
		}
		return 0;
	}

	@Override
	public String toString() {

		StringBuilder res = new StringBuilder();
		res.append("\nWhat's in the database?\n");
		for(Integer key : table.keySet()) {
			res.append("\nindex => ").append(key).append(" | ");
			EntryData entryData = table.get(key);
			res.append("id => ").append(entryData.getIdAsString()).append(" | ");
			for(Relation relation : entryData.keySet()) {
				res.append(relation.getId()).append(" => ");
				HashSet<Subject> subjects = entryData.get(relation);
				if(subjects.size() > 1) {
					res.append("{ ");
					for(Subject subject : subjects) {
						res.append(subject.getId()).append(' ');
					}
					res.append("} | ");
				} else if(subjects.size() == 1) {
					res.append(((Subject)subjects.toArray()[0]).getId());
					res.append(" | ");
				}
			}
		}
		return res.toString();
	}



}
