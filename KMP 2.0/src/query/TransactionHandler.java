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
	private boolean relationPropertyApplicationShouldControlInsertion = false;

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
	 * Restores the database to its next state.
	 */
	public void requestRedo() {
		try {
			database = databaseSerializer.redoCommand();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Imports a database from the absolute path given
	 * @param pathFile absolute path of the file containing the database
	 */
	public void requestImport(String pathFile) {
		try {
			database = databaseSerializer.importCommand(pathFile);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Exports the database at the absolute path given
	 * @param pathFile absolute path to export the database
	 */
	public void requestExport(String pathFile) {
		try {
			databaseSerializer.exportCommand(pathFile);
		} catch (IOException e) {
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
				subjectId = newSubject;
			}

		} else {
			entryData = database.getEntryData(key);
			subjectId = entryData.getIDSubject();
		}

		boolean partialInsertionRequired = false;
		
		for (int index = 1; index + 1 < splitInsertion.length; index++) {
			
			Relation relation = database.findRelation(splitInsertion[index]);
			boolean existingRelationNeedsUpdating = false;
			
			if (relation == null) {
				relation = new Relation(splitInsertion[index]);
				database.addRelation(relation);
			} else {
				existingRelationNeedsUpdating = true;
				partialInsertionRequired = true;
			}

			Subject subject = database.findSubject(splitInsertion[++index]);

			if (subject == null) {
				subject = new Subject(splitInsertion[index]);
				requestInsert(splitInsertion[index]);
			}
			
			// Check whether or not the relation's properties validate the insertion.
			boolean canInsert = true;
			for (Relation.Properties property : Relation.Properties.values()) {
				switch (property) {
				case REFLEXIVE:
					if (relation.isPropertyActive(Relation.Properties.REFLEXIVE)) {
						if (!subjectId.equals(subject)) {
							canInsert = false;
						}
					} else {
						if (subjectId.equals(subject)) {
							canInsert = false;
						}
					}
					break;
				case IRREFLEXIVE:
					if (relation.isPropertyActive(Relation.Properties.IRREFLEXIVE)) {
						if (subjectId.equals(subject)) {
							canInsert = false;
						}
					} 
					break;
				case SYMMETRIC:
					break;
				case ANTISYMMETRIC:
					// if R(a,b) with a != b, then R(b,a) must not hold.
					if (relation.isPropertyActive(Relation.Properties.ANTISYMMETRIC)) {
						if (!subjectId.equals(subject)) {
							int subjectKey = database.findKey(subject);
							EntryData subjectEntryData = database.getEntryData(subjectKey);
							if (subjectEntryData.hasRelation(relation)) {
								if (subjectEntryData.getSubjects(relation).contains(subjectId)) {
									canInsert = false;
								}
							}
						}
					}
					break;
				case ASYMMETRIC:
					// In this case a and b can be the same or different.
					if (relation.isPropertyActive(Relation.Properties.ANTISYMMETRIC)) {
						int subjectKey = database.findKey(subject);
						EntryData subjectEntryData = database.getEntryData(subjectKey);
						if (subjectEntryData.hasRelation(relation)) {
							if (subjectEntryData.getSubjects(relation).contains(subjectId)) {
								canInsert = false;
							}
						}
					}
					break;
				
				case TRANSITIVE:
					
					break;
				}
			}
			
			if (canInsert) {
				entryData.put(relation, subject);
			}
			
			if (partialInsertionRequired) {
				if (key == 0) {
					database.insert(entryData);
				} else {
					database.replaceEntry(key, entryData);
				}
			}

			if (existingRelationNeedsUpdating && !relationPropertyApplicationShouldControlInsertion) {
				applyRelationProperties(relation);
			}
		}
		
		if (newEntryData && !partialInsertionRequired) {
			database.insert(entryData);
		}

	}

	private void applyRelationProperties(Relation relation) {
		
		relationPropertyApplicationShouldControlInsertion = true;
		
		for (Relation.Properties property : Relation.Properties.values()) {
			if (relation.isPropertyActive(property)) {
				switch (property) {
				case REFLEXIVE:
					applyReflexivity(relation);
					break;
				case IRREFLEXIVE:
					applyIrreflexivity(relation);
					break;
				case SYMMETRIC:
					applySymmetry(relation);
					break;
				case ANTISYMMETRIC:
					applyAntiSymmetry(relation);
					break;
				case ASYMMETRIC:
					applyAsymmetry(relation);
					break;
				case TRANSITIVE:
					applyTransitivity(relation);
					break;
				}
			} else {
				/*
				switch (property) {
				case REFLEXIVE:
					removeReflexivity(relation);
					break;
				case IRREFLEXIVE:
					removeIrreflexivity(relation);
					break;
				case SYMMETRIC:
					removeSymmetry(relation);
					break;
				case ANTISYMMETRIC:
					removeAntiSymmetry(relation);
					break;
				case ASYMMETRIC:
					removeAsymmetry(relation);
					break;
				case TRANSITIVE:
					removeTransitivity(relation);
					break;
				}
				*/
			}
		}
		
		relationPropertyApplicationShouldControlInsertion = false;
	}

	// TODO
	private void removeTransitivity(Relation relation) {
		
	}

	// TODO
	private void removeAsymmetry(Relation relation) {
		
	}

	// TODO
	private void removeAntiSymmetry(Relation relation) {
		
	}

	// TODO
	private void removeSymmetry(Relation relation) {
		// TODO Auto-generated method stub
		
	}

	// TODO
	private void removeIrreflexivity(Relation relation) {
		// TODO Auto-generated method stub
		
	}
	
	// TODO
	private void removeReflexivity(Relation relation) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Sets the property irreflexive to false and adds the subject to the relation
	 * @param relation the relation that has to be changed
	 */
	private void applyReflexivity(Relation relation) {
		relation.setProperty(Relation.Properties.IRREFLEXIVE, false);
		for (EntryData entryData : database.getAllEntries()) {
			if (entryData.hasRelation(relation)) {
				entryData.put(relation, entryData.getIDSubject());
			}
		}
	}

	/**
	 * Sets the property reflexive to false and removes the subject from the relation
	 * @param relation the relation that has to be changed
	 */
	private void applyIrreflexivity(Relation relation) {
		relation.setProperty(Relation.Properties.REFLEXIVE, false);
		for (EntryData entryData : database.getAllEntries()) {
			if (entryData.hasRelation(relation)) {
				entryData.removeIdFromRelation(relation);
			}
		}
	}

	/**
	 * Sets the property asymmetric to false and adds the symmetric relation to the database
	 * @param relation the relation that has to be changed
	 */
	private void applySymmetry(Relation relation) {
		
		relation.setProperty(Relation.Properties.ASYMMETRIC, false);
		
		int[] totalNumberOfEntries = new int[] { 0, 0 };
		int[] previousTotalNumberOfEntries = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
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

		} while (totalNumberOfEntries[0] != previousTotalNumberOfEntries[0]
				|| totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}
	
	// TODO	
	/**
	 * Sets the property symmetric to false, sets the property antisymmetric to true and deletes the relations symmetric 
	 * @param relation the relation that has to be changed
	 */
	private void applyAsymmetry(Relation relation) {
		
		relation.setProperty(Relation.Properties.SYMMETRIC, false);
		relation.setProperty(Relation.Properties.ANTISYMMETRIC, true); // An asymmetric relation is antisymmetric by default
		
		int[] totalNumberOfEntries = new int[] { 0, 0 };
		int[] previousTotalNumberOfEntries = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
		String query = "?X, ?Y : ?X " + relation + " ?Y";
		do {
			previousTotalNumberOfEntries = Arrays.copyOf(totalNumberOfEntries, totalNumberOfEntries.length);
			Result result = requestQuery(query);

			ArrayList<Data> first = result.getData("?X");
			totalNumberOfEntries[0] = first.size();
			ArrayList<Data> second = result.getData("?Y");
			totalNumberOfEntries[1] = second.size();

			for (int index = 0; index < result.size(); index++) {
				Subject x = (Subject) first.get(index);
				Subject y = (Subject) second.get(index);
				
				int keyX = database.findKey(x);
				int keyY = database.findKey(y);
				
				EntryData entryDataX = database.getEntryData(keyX);
				EntryData entryDataY = database.getEntryData(keyY);
				
				if (entryDataX.relationContainsSubject(relation, y) && entryDataY.relationContainsSubject(relation, x)) {
					entryDataX.removeSubjectFromRelation(relation, y);
					entryDataY.removeSubjectFromRelation(relation, x);
				}
			}

		} while (totalNumberOfEntries[0] != previousTotalNumberOfEntries[0]
				|| totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}

	// TODO
	/**
	 * Deletes the relations symmetric
	 * @param relation the relation that has to be changed
	 */
	private void applyAntiSymmetry(Relation relation) {
		int[] totalNumberOfEntries = new int[] { 0, 0 };
		int[] previousTotalNumberOfEntries = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
		String query = "?X, ?Y : ?X " + relation + " ?Y";
		do {
			previousTotalNumberOfEntries = Arrays.copyOf(totalNumberOfEntries, totalNumberOfEntries.length);
			Result result = requestQuery(query);

			ArrayList<Data> first = result.getData("?X");
			totalNumberOfEntries[0] = first.size();
			ArrayList<Data> second = result.getData("?Y");
			totalNumberOfEntries[1] = second.size();

			for (int index = 0; index < result.size(); index++) {
				Subject x = (Subject) first.get(index);
				Subject y = (Subject) second.get(index);
				
				int keyX = database.findKey(x);
				int keyY = database.findKey(y);
				
				EntryData entryDataX = database.getEntryData(keyX);
				EntryData entryDataY = database.getEntryData(keyY);
				
				if (entryDataX.relationContainsSubject(relation, y) && entryDataY.relationContainsSubject(relation, x) && x.equals(y)) {
					entryDataX.removeSubjectFromRelation(relation, y);
					entryDataY.removeSubjectFromRelation(relation, x);
				}
			}

		} while (totalNumberOfEntries[0] != previousTotalNumberOfEntries[0]
				|| totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}

	//TODO checks not to put reflexive relation 
	/**
	 * Adds the relations transitive
	 * @param relation the relation that has to be changed
	 */
	private void applyTransitivity(Relation relation) {
		int[] totalNumberOfEntries = new int[] { 0, 0 };
		int[] previousTotalNumberOfEntries = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
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

		} while (totalNumberOfEntries[0] != previousTotalNumberOfEntries[0]
				|| totalNumberOfEntries[1] != previousTotalNumberOfEntries[1]);
	}

	/**
	 * Prints the database on standard output.
	 */
	public void requestShow() {
		System.out.println(database);
	}

	/**
	 * Creates a context associated to the database and generates the result from the query given
	 * @param query the request made by the user to search for data
	 * @return the result of the query
	 */
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
	/**
	 * Parses the user input, updates the state of the property and applies the new property
	 * @param command the input of the user
	 */
	public void updateRelation(String command) {
		String[] splittedCommand = command.split(" ");
		Relation relation = database.findRelation(splittedCommand[0]);
		String qualifier = splittedCommand[1];
		String propertyString = splittedCommand[2];
		Relation.Properties property = Relation.Properties.valueOf(propertyString.toUpperCase());
		if (qualifier.equals("is")) {
			relation.setProperty(property, true);
		} else if (qualifier.equals("not")) {
			relation.setProperty(property, false);
		}
		applyRelationProperties(relation);
	}

	/**
	 * Generates a string composed of the relations and the properties
	 * @return the string composed of the relations and the properties
	 */
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

	/**
	 * Generates a string for pattern-matching to reconize a relation
	 * @return a string for pattern-matching to reconize a relation
	 */
	public String patternMatcherRelations() {
		ArrayList<Relation> relations = database.getAllRelations();
		StringBuilder keywords = new StringBuilder();
		for (int i = 0; i < relations.size() - 1; i++) {
			keywords.append(relations.get(i) + "|");
		}

		if (relations.size() > 0)
			keywords.append(relations.get(relations.size()-1));

		return keywords.toString();
	}

}
