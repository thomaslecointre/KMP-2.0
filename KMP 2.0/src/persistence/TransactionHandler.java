package persistence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import model.Relation;
import model.Subject;
import query.Context;
import query.Result;
import query.SpoofResult;

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
	public Result requestQuery(String query) {
		// Split the query into its SELECT phase and its WHERE phase
		String[] splitQuery = query.split(":");

		String selectStatement = splitQuery[0];
		String[] selectorStrings = selectStatement.split(",");
		Arrays.asList(selectorStrings).stream().map(String::trim).collect(Collectors.toList());

		String whereStatement = splitQuery[1];
		// Split each WHERE substatement by '&'
		String[] conditionStatements = whereStatement.split("&");

		Context context = new Context();

		// Iterate through all the conditions of the WHERE statement
		for (String conditionStatement : conditionStatements) {

			String[] conditionStrings = conditionStatement.split(" ");
			if (conditionStrings[0].isEmpty()) {
				conditionStrings = Arrays.copyOfRange(conditionStrings, 1, conditionStrings.length);
			}

			// Evaluating left side of the condition
			String left = conditionStrings[0];

			HashSet<SpoofResult> spoofResultsLeft;
			boolean newVariableLeft = false;

			if (context.containsKey(left)) {
				spoofResultsLeft = (HashSet<SpoofResult>) context.getSpoofResults(left).clone();
			} else {
				spoofResultsLeft = new HashSet<>();
				newVariableLeft = true;
			}

			if (left.charAt(0) == '?') {
				Set<Integer> keys = database.getAllKeys();
				for (Integer key : keys) {
					spoofResultsLeft.add(new SpoofResult(key));
				}
			} else {
				// Test if left is an id or an int value (key)
				int key = 0;
				try {
					key = Integer.parseInt(left);
				} catch (NumberFormatException e) {
					key = database.findKey(left);
				} finally {
					if (key != 0) {
						spoofResultsLeft.add(new SpoofResult(key));
					}
				}
			}

			// Only keep the intersection of current and previous results for this variable
			if (!newVariableLeft) {
				spoofResultsLeft.retainAll(context.getSpoofResults(left));
			}

			// Evaluating the middle of the condition (relation)
			String middle = conditionStrings[1];

			HashSet<Relation> relations;
			boolean newVariableMiddle = false;

			if (context.containsKey(middle)) {
				relations = (HashSet<Relation>) context.getRelations(middle);
			} else {
				relations = new HashSet<>();
				newVariableMiddle = true;
			}

			if (middle.charAt(0) == '?') {
				for (SpoofResult spoofResultLeft : spoofResultsLeft) {
					EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
					relations.addAll(entryData.relations());
				}
			} else {
				Relation relation = database.findRelation(middle);
				if (relation != null) {
					relations.add(relation);
				}
				// Clean up spoof results on the left that do not satisfy this
				// condition
				HashSet<SpoofResult> spoofResultsLeftMarkedForRemoval = (HashSet<SpoofResult>) spoofResultsLeft.clone();
				for (SpoofResult spoofResultLeft : spoofResultsLeft) {
					EntryData entryDataLeft = database.getEntryData(spoofResultLeft.getKey());
					if (!entryDataLeft.containsRelation(relation)) {
						spoofResultsLeftMarkedForRemoval.remove(spoofResultLeft);
					}
				}
				spoofResultsLeft = spoofResultsLeftMarkedForRemoval;
			}

			if (!newVariableMiddle) {
				relations.retainAll(context.getRelations(middle));
			}

			// Evaluating the right side of the condition
			String right = conditionStrings[2];

			HashSet<SpoofResult> spoofResultsRight;
			boolean newVariableRight = false;

			if (context.containsKey(right)) {
				spoofResultsRight = (HashSet<SpoofResult>) context.getSpoofResults(right).clone();
			} else {
				spoofResultsRight = new HashSet<>();
				newVariableRight = true;
			}

			if (right.charAt(0) == '?') {
				if (middle.charAt(0) == '?') {
					for (SpoofResult spoofResultLeft : spoofResultsLeft) {
						EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
						SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.getKey());
						for (Relation relation : relations) {
							if (entryData.containsRelation(relation)) {
								spoofResultRight.addSubjects(entryData.getSubjects(relation));
							}
						}
						spoofResultsRight.add(spoofResultRight);
					}
				} else {
					Relation relation = relations.iterator().next();
					for (SpoofResult spoofResultLeft : spoofResultsLeft) {
						EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
						HashSet<Subject> subjects = entryData.getSubjects(relation);
						SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.getKey());
						spoofResultRight.addSubjects(subjects);
						spoofResultsRight.add(spoofResultRight);
					}
				}
			} else {
										
				Subject subject = database.findSubject(right);
				// Cleaning out bad relations and bad left hand spoof results
				HashSet<SpoofResult> spoofResultsLeftMarkedForRemoval = (HashSet<SpoofResult>) spoofResultsLeft.clone();
				HashSet<Relation> relationsMarkedForRemoval = (HashSet<Relation>) relations.clone();

				if (middle.charAt(0) != '?') {
					for (Relation relation : relations) {
						boolean relationStillValid = false;
						for (SpoofResult spoofResultLeft : spoofResultsLeft) {
							EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
							SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.getKey());
							if (entryData.containsRelation(relation)) {
								if (entryData.getSubjects(relation).contains(subject)) {
									spoofResultRight.addSubject(subject);
									spoofResultsRight.add(spoofResultRight);
									relationStillValid = true;
									break;
								} else {
									spoofResultsLeftMarkedForRemoval.remove(spoofResultLeft);
								}
							}
						}
						if (!relationStillValid) {
							relationsMarkedForRemoval.remove(relation);
						}
					}
				} else {
					Relation relation = relations.iterator().next();
					boolean relationStillValid = false;
					for (SpoofResult spoofResultLeft : spoofResultsLeft) {
						EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
						SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.getKey());
						if (entryData.containsRelation(relation)) {
							if (entryData.getSubjects(relation).contains(subject)) {
								spoofResultRight.addSubject(subject);
								spoofResultsRight.add(spoofResultRight);
								relationStillValid = true;
								break;
							} else {
								spoofResultsLeftMarkedForRemoval.remove(spoofResultLeft);
							}
						}
					}
					if (!relationStillValid) {
						relationsMarkedForRemoval.remove(relation);
					}
				}
				spoofResultsLeft = spoofResultsLeftMarkedForRemoval;
				relations = relationsMarkedForRemoval;
			}

			if (!newVariableRight) {
				spoofResultsRight.retainAll(context.getRelations(right));
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

}
