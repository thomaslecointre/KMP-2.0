package query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import model.Data;
import model.Relation;
import model.Subject;
import persistence.Database;
import persistence.DatabaseSerializer;
import persistence.EntryData;

/**
 * This class is an intermediary request handling class and communicates with
 * Database.
 */
public class TransactionHandler {

	private Database database;
	private DatabaseSerializer databaseSerializer;
	private boolean fromRelationQualifierAdjustment = false;

	public TransactionHandler() {
		database = new Database();
		databaseSerializer = new DatabaseSerializer(database);
		try {
			database = databaseSerializer.initCommand();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restores the database to its previous state.
	 */
	public void requestUndo() {
		try {
			database = databaseSerializer.undoCommand();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restores the database to its initial state.
	 */
	public void requestReset() {
		database.reset();
		try {
			databaseSerializer.resetCommand(database);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles an insertion request.
	 * 
	 * @param insertion
	 *            a string corresponding to a user entry.
	 */
	public void requestInsert(String insertion) {
		
		String[] splitInsertion = insertion.split(" ");
		String id = splitInsertion[0];
		int key = database.findKey(id);
		Subject subjectId;
		EntryData entryData;
		boolean newEntryData = false;

		if (key == 0) {
			entryData = new EntryData();
			newEntryData = true;
			subjectId = database.findSubject(id);

			if (subjectId != null) {
				entryData.setID(subjectId);
			} else {
				Subject newSubject = new Subject(id);
				entryData.setID(newSubject);
				database.addSubject(newSubject);
			}

		} else {
			entryData = database.getEntryData(key);
			subjectId = entryData.getID().getSubject();
		}
		
		if (splitInsertion.length > 1) {
			for (int index = 1; index + 1 < splitInsertion.length; index++) {
				Relation relation = database.findRelation(splitInsertion[index]);
				boolean applyRelationProperties = false;
				if (relation == null) {
					relation = new Relation(splitInsertion[index]);
					database.addRelation(relation);
				} else {
					applyRelationProperties = true;
				}
				
				Subject subject = database.findSubject(splitInsertion[++index]);

				if (subject == null) {
					subject = new Subject(splitInsertion[index]);
					requestInsert(splitInsertion[index]);
				}

				if (relation.isPropertyActive(Relation.Properties.REFLEXIVE)) {
					if (entryData.containsRelation(relation)) {
						entryData.purgeRelation(relation);
						if (subjectId.equals(subject)) {
							entryData.put(relation, subject);
						}
					} else {
						if (subjectId.equals(subject)) {
							entryData.put(relation, subject);
						}
					}
				} else {
					if (!subjectId.equals(subject)) {
						entryData.put(relation, subject);
					}
				}
				
				
				if (applyRelationProperties && !fromRelationQualifierAdjustment) {
					fromRelationQualifierAdjustment = true;
					applyRelationProperties(relation);
				}
			}
		}
	
		if (newEntryData) {
			database.insert(entryData);
		}
		
	}

	private void applyRelationProperties(Relation relation) {
		for (Relation.Properties property : Relation.Properties.values()) {
			if (relation.isPropertyActive(property)) {
				switch (property) {
				case REFLEXIVE:
					applyReflexivity(relation);
					break;
				case SYMMETRIC:
					applySymmetry(relation);
					break;
				case TRANSITIVE:
					applyTransitivity(relation);
					break;
				}
			}
		}
		fromRelationQualifierAdjustment = false;
	}
	
	// TODO
	private void applyReflexivity(Relation relation) {
	
	}

	private void applySymmetry(Relation relation) {
		int[] totalNumberOfEntries = new int[]{0, 0};
		int[] previousTotalNumberOfEntries = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
		String query = "?X, ?Y : ?X " + relation + " ?Y"; 
		do {
			previousTotalNumberOfEntries = Arrays.copyOf(totalNumberOfEntries, totalNumberOfEntries.length);
			Result result = requestQuery(query);
			
			ArrayList<Data> first = result.getData("?X");
			totalNumberOfEntries[0] = first.size();
			ArrayList<Data> second = result.getData("?Y");
			totalNumberOfEntries[1] = second.size();
			
			for (int index = 0; index < result.size(); index++) {
				String insertion = second.get(index) + " " + relation + " " + first.get(index);
				requestInsert(insertion);
			}
			
		} while(totalNumberOfEntries[0] != previousTotalNumberOfEntries[0] || totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}
	
	private void applyTransitivity(Relation relation) {
		int[] totalNumberOfEntries = new int[]{0, 0};
		int[] previousTotalNumberOfEntries = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
		String query = "?X, ?Z : ?X " + relation + " ?Y & ?Y " + relation + " ?Z"; 
		do {
			previousTotalNumberOfEntries = Arrays.copyOf(totalNumberOfEntries, totalNumberOfEntries.length);
			Result result = requestQuery(query);
			
			ArrayList<Data> first = result.getData("?X");
			totalNumberOfEntries[0] = first.size();
			ArrayList<Data> second = result.getData("?Z");
			totalNumberOfEntries[1] = second.size();
			
			for (int index = 0; index < result.size(); index++) {
				String insertion = first.get(index) + " " + relation + " " + second.get(index);
				requestInsert(insertion);
			}
			
		} while(totalNumberOfEntries[0] != previousTotalNumberOfEntries[0] || totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}

	/**
	 * Prints the database on standard output.
	 */
	public void requestShow() {
		System.out.println(database);
	}

	public Result requestQuery(String query) {
		return new Context(database).generateResult(query);
	}

	public Database getDatabase() {
		return database;
	}

	public DatabaseSerializer getDatabaseSerializer() {
		return databaseSerializer;
	}

	// TODO
	public void updateRelation(String command) {
		String[] splittedCommand = command.split(" ");
		Relation relation = database.findRelation(splittedCommand[0]);
		String qualifier = splittedCommand[1];
		String propertyString = splittedCommand[2];
		Relation.Properties property = Relation.Properties.valueOf(propertyString.toUpperCase());
		if (qualifier.equals("is")) {
			relation.setProperty(property, true);
			applyRelationProperties(relation);
		} else if (qualifier.equals("not")) {
			relation.setProperty(property, false);
		}
	}
	
	public String showRelations() {
		ArrayList<Relation> relations = database.getAllRelations();
		StringBuilder res = new StringBuilder();
		res.append("\nRelations : ");
		for (Relation relation : relations) {
			res.append("\n\t").append(relation);
		}
		res.append("\nProperties : ");
		for (Relation.Properties property : Relation.Properties.values()) {
			res.append("\n\t").append(property.toString().toLowerCase());
		}
		return res.toString();
	}
	
	public String patternMatcherRelations() {
		ArrayList<Relation> relations = database.getAllRelations();
		StringBuilder keywords = new StringBuilder();
		for (int i = 0; i < relations.size()-1; i++) {
			keywords.append(relations.get(i) + "|");
		}
		keywords.append(relations.get(relations.size()-1));
		return keywords.toString();
	}

}
