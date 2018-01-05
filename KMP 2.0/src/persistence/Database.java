package persistence;

import java.util.*;

import model.Data;
import model.Relation;
import model.Subject;

/**
 * This class is the lowest data layer. It is the only class that should have access to data stored on a machine.
 */
public class Database {

	private ArrayList<Data> objects;
	private HashMap<Integer, EntryData> table;
	private int primaryIndex = 1;

	/**
	 * Returns size of table field (HashMap<Integer, EntryData>) as an int.
	 * @return the size of the table
	 */
	public int tableSize() {
		return table.keySet().size();
	}

	Database() {
		table = new HashMap<>();
		objects = new ArrayList<>();
	}

	/**
	 * Inserts an instance of EntryData into table as the key is autoincremented.
	 * @param entryData instance of EntryData
	 */
	public void insert(EntryData entryData) {
		table.put(autoIncrementValue(), entryData);
	}

	/**
	 * Increments the key (primaryIndex) used in table.
	 * @return an int corresponding to an incremented primaryKey.
	 */
	private int autoIncrementValue() {
		return primaryIndex++;
	}

	/**
	 * Removes the last entry by destroying the association <Integer, EntryData>. The primaryIndex field is decremented.
	 */
	public void removeLastEntry() {
		if(table.containsKey(primaryIndex - 1)) {
			table.remove(primaryIndex - 1);
			primaryIndex--;
		}
	}

	/**
	 * Resets the table field by clearing the map and resetting primaryIndex to 0.
	 */
	public void reset() {
		table.clear();
		primaryIndex = 0;
	}

	/**
	 * Finds an instance of Subject using an id string.
	 * An id string is the textual representation of an ID value, which is present in every table entry.
	 * The function returns null if no table entry is found to having id as its "ID".
	 * @param id a string used to identify an ID.
	 * @return an instance of Subject.
	 */
	public Subject findSubject(String id) {
		for(Data data : objects) {
			if(data.getId().equals(id)) {
				return (Subject) data;
			}
		}
		return null;
	}

	/**
	 * Similar to Subject findSubject(...), it returns an instance of Relation identified by the id parameter.
	 * @param id a string used to identify an instance of Relation.
	 * @return an instance of Relation.
	 */
	public Relation findRelation(String id) {
		for(Data data : objects) {
			if(data.getId().equals(id)) {
				return (Relation) data;
			}
		}
		return null;
	}

	/**
	 * Adds an instance of Subject to the objects field.
	 * @param newSubject an instance of Subject.
	 */
	public void addSubject(Subject newSubject) {
		objects.add(newSubject);
	}

	/**
	 * Adds an instance of Relation to the objects field.
	 * @param newRelation an instance of Relation.
	 */
	public void addRelation(Relation newRelation) {
		objects.add(newRelation);
	}

	/**
	 * Returns an instance of EntryData by identifying the EntryData associated with databaseEntryNumber.
	 * @param databaseEntryNumber a int used to identify a key in table.
	 * @return an instance of EntryData.
	 */
	public EntryData getEntryData(int databaseEntryNumber) {
		return table.get(databaseEntryNumber);
	}

	/**
	 * Returns an int representing a key in the database by indentifying the id, an instance of String, in the associated EntryData instance.
	 * Returns 0 if no key is found.
	 * @param id a string used to identify the "ID" of an instance of EntryData.
	 * @return a key as an int.
	 */
	public int findKey(String id) {
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
			for(Relation relation : entryData.relations()) {
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


	/**
	 * Returns all the keys in table as a set.
	 * @return a set of keys.
	 */
	public Set<Integer> getAllKeys() {
		return table.keySet();
	}

	/**
	 * Returns all instances of EntryData in table as a collection.
	 * @return a collection of every EntryData instance.
	 */
	public Collection<EntryData> getAllEntries() {
	    return table.values();
	}

	/**
	 * Traverses the objects field and counts every instance of Relation, returning the result.
	 * @return an int representing the number of instances of Relation in objects.
	 */
    public int relationCount() {
	    int counter = 0;
	    for(Data data : objects) {
	        if (data instanceof Relation) {
	            counter++;
            }
        }
        return counter;
    }

	/**
	 * Returns a boolean representing the presence of a key in table.
	 * @param key an int representing a key.
	 * @return a boolean indicating the presence of a key.
	 */
	public boolean testKey(int key) {
	    return table.keySet().contains(key);
    }
}
