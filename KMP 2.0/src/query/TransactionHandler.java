package query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private List<Integer> entriesToFlush;

	public TransactionHandler() {
		database = new Database();
		entriesToFlush = new ArrayList<>();
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

		for (int index = 1; index + 1 < splitInsertion.length; index++) {
			Relation relation = database.findRelation(splitInsertion[index]);

			if (relation == null) {
				relation = new Relation(splitInsertion[index]);
				database.addRelation(relation);
			}
			Subject subject = database.findSubject(splitInsertion[++index]);

			if (subject == null) {
				subject = new Subject(splitInsertion[index]);
				database.addSubject(subject);
			}

			entryData.put(relation, subject);
		}

		if (newEntryData) {
			database.insert(entryData);
		}

		requestShow();
	}

	/**
	 * Prints the database on standard output.
	 */
	public void requestShow() {
		System.out.println(database);
	}

	// TODO

	/**
	 * Method used to process queries written in a loosely based SPARQL-like
	 * language. The method splits the query in two, the left side corresponding to
	 * the SELECT phase, the right corresponding to the WHERE phase.
	 * 
	 * @param query
	 *            a string corresponding to a user entry.
	 */
	@SuppressWarnings("unchecked")
	public Result requestQuery(String query) {
		
		// Split the query into its SELECT phase and its WHERE phase
		String[] splitQuery = query.split(":");

		String selectStatement = splitQuery[0];
		String[] selectorStrings = selectStatement.split(",");
		// Remove unnecessary whitespace around each selector variable
		List<String> selectorList = Arrays.asList(selectorStrings).stream().map(String::trim)
				.collect(Collectors.toList());
		selectorStrings = selectorList.toArray(new String[selectorList.size()]);

		String whereStatement = splitQuery[1]; // where -> ?X is ?Y & ?X has ?Z ...
		// Split each WHERE substatement by '&'
		String[] conditionStatements = whereStatement.split("&"); // condition statement -> ?X is ?Y
		// Remove unnecessary whitespace around each condition statement
		List<String> conditionList = Arrays.asList(conditionStatements).stream().map(String::trim)
				.collect(Collectors.toList());
		conditionStatements = conditionList.toArray(new String[conditionList.size()]);

		Context context = new Context();

		// Iterate through all the conditions of the WHERE statement
		for (String conditionStatement : conditionStatements) {

			String[] conditionStrings = conditionStatement.split(" ");
			if (conditionStrings[0].isEmpty()) {
				conditionStrings = Arrays.copyOfRange(conditionStrings, 1, conditionStrings.length);
			}

			// Evaluating left side of the condition
			String left = conditionStrings[0];

			SpoofResults spoofResultsLeft;
			HashSet<SpoofResult> spoofResultSetLeft;
			boolean newVariableLeft = false;

			if (context.containsKey(left)) {
				spoofResultsLeft = context.getSpoofResults(left);
				spoofResultSetLeft = (HashSet<SpoofResult>) spoofResultsLeft.getSpoofResultSet().clone();
				if (spoofResultsLeft.usedOnTheRight()) {
					entriesToFlush.clear();
					spoofResultsLeft.transferIDs();
					for (int entry : entriesToFlush) {
						
					}
				}
			} else {
				spoofResultsLeft = new SpoofResults(this);
				spoofResultSetLeft = spoofResultsLeft.getSpoofResultSet();
				newVariableLeft = true;
			}

			if (left.charAt(0) == '?') {
				Set<Integer> keys = database.getAllKeys();
				for (Integer key : keys) {
					spoofResultSetLeft.add(new SpoofResult(key, database.getID(key)));
				}
			} else {
				int key = database.findKey(left);
				if (key != 0) {
					spoofResultSetLeft.add(new SpoofResult(key, database.getID(key)));
				}
			}

			// Only keep the intersection of current and previous results for this variable
			if (!newVariableLeft) {
				spoofResultSetLeft.retainAll(spoofResultsLeft.getSpoofResultSet());
			}

			// Evaluating the middle of the condition (relation)
			String middle = conditionStrings[1];

			HashSet<Relation> relations;
			boolean newVariableMiddle = false;

			if (context.containsKey(middle)) {
				relations = (HashSet<Relation>) context.getRelations(middle).clone();
			} else {
				relations = new HashSet<>();
				newVariableMiddle = true;
			}

			HashSet<SpoofResult> spoofResultSetLeftMarkedForRemoval;

			if (middle.charAt(0) == '?') {
				
				for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
					EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
					relations.addAll(entryData.relations());
				}
				
			} else {
				
				Relation relation = database.findRelation(middle);
				
				if (relation != null) {
					relations.add(relation);

					// Clean up spoof results on the left that do not satisfy this
					// condition
					spoofResultSetLeftMarkedForRemoval = (HashSet<SpoofResult>) spoofResultsLeft.getSpoofResultSet()
							.clone();
					for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
						EntryData entryDataLeft = database.getEntryData(spoofResultLeft.getKey());
						if (!entryDataLeft.containsRelation(relation)) {
							spoofResultSetLeftMarkedForRemoval.remove(spoofResultLeft);
						}
					}
					spoofResultSetLeft = spoofResultSetLeftMarkedForRemoval;
					
				}
			}

			if (!newVariableMiddle) {
				relations.retainAll(context.getRelations(middle));
			}

			// Evaluating the right side of the condition
			String right = conditionStrings[2];

			SpoofResults spoofResultsRight;
			HashSet<SpoofResult> spoofResultSetRight;
			boolean newVariableRight = false;

			if (context.containsKey(right)) {
				spoofResultsRight = context.getSpoofResults(right);
				spoofResultSetRight = (HashSet<SpoofResult>) spoofResultsRight.getSpoofResultSet().clone();
			} else {
				spoofResultsRight = new SpoofResults(this);
				spoofResultSetRight = spoofResultsRight.getSpoofResultSet();
				newVariableLeft = true;
			}
			
			spoofResultsRight.isUsedOnTheRight();

			HashSet<Relation> relationsMarkedForRemoval;

			if (right.charAt(0) == '?') {
				if (middle.charAt(0) == '?') {
					for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
						EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
						SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft);
						for (Relation relation : relations) {
							if (entryData.containsRelation(relation)) {
								spoofResultRight.addSubjects(entryData.getSubjects(relation));
							}
						}
						spoofResultSetRight.add(spoofResultRight);
					}
				} else {

					Relation relation = relations.iterator().next();
					if (relation != null) {
						for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
							EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
							HashSet<Subject> subjects = entryData.getSubjects(relation);
							SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft);
							spoofResultRight.addSubjects(subjects);
							spoofResultSetRight.add(spoofResultRight);
						}
					}
				}
				
			} else {
				Subject subject = database.findSubject(right);
				if (subject != null) {
					// Cleaning out bad relations and bad left hand spoof results
					spoofResultSetLeftMarkedForRemoval = (HashSet<SpoofResult>) spoofResultSetLeft.clone();
					relationsMarkedForRemoval = (HashSet<Relation>) relations.clone();

					if (middle.charAt(0) != '?') {
						for (Relation relation : relations) {
							boolean relationStillValid = false;
							for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
								EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
								SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft);
								if (entryData.containsRelation(relation)) {
									if (entryData.getSubjects(relation).contains(subject)) {
										spoofResultRight.addSubject(subject);
										spoofResultSetRight.add(spoofResultRight);
										relationStillValid = true;
										break;
									} else {
										spoofResultSetLeftMarkedForRemoval.remove(spoofResultLeft);
									}
								}
							}
							if (!relationStillValid) {
								relationsMarkedForRemoval.remove(relation);
							}
						}
					} else {
						Relation relation = relations.iterator().next();
						if (relation != null) {
							boolean relationStillValid = false;
							for (SpoofResult spoofResultLeft : spoofResultSetLeft) {
								EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
								SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft);
								if (entryData.containsRelation(relation)) {
									if (entryData.getSubjects(relation).contains(subject)) {
										spoofResultRight.addSubject(subject);
										spoofResultSetRight.add(spoofResultRight);
										relationStillValid = true;
										break;
									} else {
										spoofResultSetLeftMarkedForRemoval.remove(spoofResultLeft);
									}
								}
							}
							if (!relationStillValid) {
								relationsMarkedForRemoval.remove(relation);
							}
						}
					}
					spoofResultSetLeft = spoofResultSetLeftMarkedForRemoval;
					relations = relationsMarkedForRemoval;
				}
			}

			if (!newVariableRight) {
				spoofResultSetRight.retainAll(spoofResultsRight.getSpoofResultSet());
			}

			if (left.charAt(0) == '?') {
				context.put(left, spoofResultsLeft);
			}
			if (middle.charAt(0) == '?') {
				context.put(middle, relations);
			}
			if (right.charAt(0) == '?') {
				context.put(right, spoofResultsRight);
			}

		}

		return context.generateResult(selectorStrings);
	}

	protected Database getDatabase() {
		return database;
	}

	public void flushEntry(int key) {
		entriesToFlush.add(key);
	}

}
