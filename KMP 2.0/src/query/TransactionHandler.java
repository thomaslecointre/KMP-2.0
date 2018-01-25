package query;

import model.Relation;
import model.Subject;
import persistence.Database;
import persistence.EntryData;

/**
 * This class is an intermediary request handling class and communicates with
 * Database.
 */
public class TransactionHandler {

	private Database database;

	public TransactionHandler() {
		database = new Database();
	}

	/**
	 * Restores the database to its previous state.
	 */
	// TODO
	public void requestUndo() {

	}

	/**
	 * Restores the database to its initial state.
	 */
	public void requestReset() {
		database.reset();
		requestShow();
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
		EntryData entryData;
		boolean newEntryData = false;

		if (key == 0) {
			entryData = new EntryData();
			newEntryData = true;
			Subject subjectId = database.findSubject(id);

			if (subjectId != null) {
				entryData.setID(subjectId);
			} else {
				Subject newSubject = new Subject(id);
				entryData.setID(newSubject);
				database.addSubject(newSubject);
			}

		} else {
			entryData = database.getEntryData(key);
		}
		
		if (splitInsertion.length > 1) {
			for (int index = 1; index + 1 < splitInsertion.length; index++) {
				Relation relation = database.findRelation(splitInsertion[index]);

				if (relation == null) {
					relation = new Relation(splitInsertion[index]);
					database.addRelation(relation);
				}
				Subject subject = database.findSubject(splitInsertion[++index]);

				if (subject == null) {
					subject = new Subject(splitInsertion[index]);
					requestInsert(splitInsertion[index]);
				}

				entryData.put(relation, subject);
			}
		}
	
		if (newEntryData) {
			database.insert(entryData);
		}

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

}
