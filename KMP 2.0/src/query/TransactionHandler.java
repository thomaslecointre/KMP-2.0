package query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.Relation;
import model.Subject;
import persistence.Database;
import persistence.EntryData;
import query.spoof.SpoofData;
import query.spoof.SpoofRelation;
import query.spoof.SpoofSubject;
import query.spoof.SpoofVariable;

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

			SpoofVariable spoofVariableLeft;
			HashSet<SpoofData> spoofSubjectsLeft;
			boolean newVariableLeft = false;

			if (context.containsKey(left)) {
				spoofVariableLeft = context.getSpoofResults(left);
				spoofSubjectsLeft = (HashSet<SpoofData>) spoofVariableLeft.getSpoofDataSet().clone();
				if (spoofVariableLeft.usedOnTheRight()) {
					spoofVariableLeft.transferIDs();
				}
			} else {
				spoofVariableLeft = new SpoofVariable(this);
				spoofSubjectsLeft = spoofVariableLeft.getSpoofDataSet();
				newVariableLeft = true;
			}

			if (left.charAt(0) == '?') {
				Set<Integer> keys = database.getAllKeys();
				for (Integer key : keys) {
					spoofSubjectsLeft.add(new SpoofSubject(key, database.getID(key)));
				}
			} else {
				int key = database.findKey(left);
				if (key != 0) {
					spoofSubjectsLeft.add(new SpoofSubject(key, database.getID(key)));
				}
			}

			// Only keep the intersection of current and previous results for this variable
			if (!newVariableLeft) {
				spoofSubjectsLeft.retainAll(spoofVariableLeft.getSpoofDataSet());
			}

			// Evaluating the middle of the condition (relation)
			String middle = conditionStrings[1];

			SpoofVariable spoofVariableMiddle;
			HashSet<SpoofData> spoofRelationsMiddle;
			boolean newVariableMiddle = false;

			if (context.containsKey(middle)) {
				spoofVariableMiddle = context.getRelations(middle);
				spoofRelationsMiddle = (HashSet<SpoofData>) spoofVariableMiddle.getSpoofDataSet().clone();
			} else {
				spoofRelationsMiddle = new HashSet<>();
				newVariableMiddle = true;
			}

			HashSet<SpoofData> spoofSubjectsLeftMarkedForRemoval;

			if (middle.charAt(0) == '?') {

				for (SpoofData spoofDataLeft : spoofSubjectsLeft) {
					SpoofSubject spoofSubjectLeft = (SpoofSubject) spoofDataLeft;
					EntryData entryDataLeft = database.getEntryData(spoofSubjectLeft.getKey());
					spoofRelationsMiddle.add(new SpoofRelation(spoofSubjectLeft.getKey(), entryDataLeft.getRelations()));
				}

			} else {

				Relation relation = database.findRelation(middle);

				if (relation != null) {

					spoofSubjectsLeftMarkedForRemoval = (HashSet<SpoofData>) spoofVariableLeft.getSpoofDataSet()
							.clone();
					for (SpoofData spoofDataLeft : spoofSubjectsLeft) {
						SpoofSubject spoofSubjectLeft = (SpoofSubject) spoofDataLeft;
						EntryData entryDataLeft = database.getEntryData(spoofSubjectLeft.getKey());
						if (entryDataLeft.containsRelation(relation)) {
							spoofRelationsMiddle
									.add(new SpoofRelation(spoofSubjectLeft.getKey(), entryDataLeft.getRelations()));
						} else {
							// Clean left variable if it doesn't have this relation
							spoofSubjectsLeftMarkedForRemoval.remove(spoofSubjectLeft);
						}
					}
					spoofSubjectsLeft = spoofSubjectsLeftMarkedForRemoval;
				}
			}

			if (!newVariableMiddle) {
				spoofRelationsMiddle.retainAll(spoofVariableMiddle.getSpoofDataSet());
			}

			// Evaluating the right side of the condition
			String right = conditionStrings[2];

			SpoofVariable spoofVariableRight;
			HashSet<SpoofData> spoofSubjectsRight;
			boolean newVariableRight = false;

			if (context.containsKey(right)) {
				spoofVariableRight = context.getSpoofResults(right);
				spoofSubjectsRight = (HashSet<SpoofData>) spoofVariableRight.getSpoofDataSet().clone();
			} else {
				spoofVariableRight = new SpoofVariable(this);
				spoofSubjectsRight = spoofVariableRight.getSpoofDataSet();
				newVariableLeft = true;
			}

			spoofVariableRight.isUsedOnTheRight();

			HashSet<SpoofData> spoofRelationsMiddleMarkedForRemoval;

			if (right.charAt(0) == '?') {
				if (middle.charAt(0) == '?') {
					for (SpoofData spoofDataLeft : spoofSubjectsLeft) {
						SpoofSubject spoofSubjectLeft = (SpoofSubject) spoofDataLeft;
						EntryData entryData = database.getEntryData(spoofSubjectLeft.getKey());
						SpoofSubject spoofSubjectRight = new SpoofSubject(spoofSubjectLeft);
						for (SpoofData spoofDataMiddle : spoofVariableMiddle.getSpoofDataSet()) {
							SpoofRelation spoofRelationMiddle = (SpoofRelation) spoofDataMiddle;
							for (Relation relation : spoofRelationMiddle.getRelations()) {
								if (entryData.containsRelation(relation)) {
									spoofSubjectRight.addSubjects(entryData.getSubjects(relation));
								}
							}
						}
						spoofSubjectsRight.add(spoofSubjectRight);
					}
				} else {
					Relation relation = database.findRelation(middle);
					if (relation != null) {
						for (SpoofData spoofDataMiddle : spoofRelationsMiddle) {
							SpoofRelation spoofRelationMiddle = (SpoofRelation) spoofDataMiddle;
							for (SpoofData spoofDataLeft : spoofSubjectsLeft) {
								SpoofSubject spoofSubjectLeft = (SpoofSubject) spoofDataLeft;
								EntryData entryData = database.getEntryData(spoofSubjectLeft.getKey());
								HashSet<Subject> subjects = entryData.getSubjects(relation);
								SpoofSubject spoofResultRight = new SpoofSubject(spoofSubjectLeft);
								spoofResultRight.addSubjects(subjects);
								spoofSubjectsRight.add(spoofResultRight);
							}
						}
					}
				}

			} else {
				Subject subject = database.findSubject(right);
				
				if (subject != null) {
					// Cleaning out left and middle spoof variables
					spoofSubjectsLeftMarkedForRemoval = (HashSet<SpoofData>) spoofSubjectsLeft.clone();
					spoofRelationsMiddleMarkedForRemoval = (HashSet<SpoofData>) spoofVariableMiddle.getSpoofDataSet().clone();

					if (middle.charAt(0) != '?') {
						for (SpoofData spoofDataMiddle : spoofRelationsMiddle) {
							SpoofRelation spoofRelationMiddle = (SpoofRelation) spoofDataMiddle;
							boolean relationStillValid = false;
							EntryData entryData = database.getEntryData(spoofDataMiddle.getKey());
							SpoofSubject spoofResultRight = new SpoofSubject(spoofDataMiddle.getKey(), entryData.getID().getSubject());
							for (Relation relation : spoofRelationMiddle.getRelations()) {
								
							}
							if (entryData.getSubjects(spoofDataMiddle).contains(subject)) {
								spoofResultRight.addSubject(subject);
								spoofSubjectsRight.add(spoofResultRight);
								relationStillValid = true;
								break;
							} else {
								spoofSubjectsLeftMarkedForRemoval.remove(spoofResultLeft);
							}
							
							if (!relationStillValid) {
								spoofRelationsMiddleMarkedForRemoval.remove(spoofDataMiddle);
							}
						}
					} else {
						Relation relation = database.findRelation(middle);
						if (relation != null) {
							boolean relationStillValid = false;
							for (SpoofSubject spoofResultLeft : spoofSubjectsLeft) {
								EntryData entryData = database.getEntryData(spoofResultLeft.getKey());
								SpoofSubject spoofResultRight = new SpoofSubject(spoofResultLeft);
								if (entryData.containsRelation(relation)) {
									if (entryData.getSubjects(relation).contains(subject)) {
										spoofResultRight.addSubject(subject);
										spoofSubjectsRight.add(spoofResultRight);
										relationStillValid = true;
										break;
									} else {
										spoofSubjectsLeftMarkedForRemoval.remove(spoofResultLeft);
									}
								}
							}
							if (!relationStillValid) {
								spoofRelationsMiddleMarkedForRemoval.remove(relation);
							}
						}
					}
					spoofSubjectsLeft = spoofSubjectsLeftMarkedForRemoval;
					spoofVariableMiddle = spoofRelationsMiddleMarkedForRemoval;
				}
			}

			if (!newVariableRight) {
				spoofSubjectsRight.retainAll(spoofVariableRight.getSpoofDataSet());
			}

			if (left.charAt(0) == '?') {
				context.put(left, spoofVariableLeft);
			}
			if (middle.charAt(0) == '?') {
				context.put(middle, spoofVariableMiddle);
			}
			if (right.charAt(0) == '?') {
				context.put(right, spoofVariableRight);
			}

		}

		return context.generateResult(selectorStrings);
	}

	public Database getDatabase() {
		return database;
	}

}
