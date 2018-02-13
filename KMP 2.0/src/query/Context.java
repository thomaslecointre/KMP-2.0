package query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.Data;
import model.Relation;
import model.Subject;
import persistence.Database;
import persistence.EntryData;
/**
 * This class is used to parse the query of the user, finds all the values of a variable and generates a result.
 */
public class Context {

	private ArrayList<Data[]> globalMatrix;
	private HashMap<String, ArrayList<Data>> globalVariables;

	private ArrayList<ArrayList<Data>> currentMatrix;
	private HashMap<String, ArrayList<Data>> currentVariables;

	private HashMap<String, Integer> currentVariableIndices;
	private HashMap<String, Integer> globalVariableIndices;

	enum DataTypes {
		SUBJECT, RELATION
	}

	private HashMap<String, DataTypes> currentVariableTypes;
	private HashMap<String, DataTypes> globalVariableTypes;

	private int globalVariableIndex = 0;
	private Database database;

	private void incrementVariableIndex() {
		globalVariableIndex++;
	}
	
	private boolean associatedView = false;

	public Context(Database database) {
		this.database = database;
		currentMatrix = new ArrayList<>();
		currentVariables = new HashMap<>();
		currentVariableIndices = new HashMap<>();
		currentVariableTypes = new HashMap<>();
		globalMatrix = new ArrayList<>();
		globalVariables = new HashMap<>();
		globalVariableIndices = new HashMap<>();
		globalVariableTypes = new HashMap<>();
	}

	/**
	 * Method used to process queries written in a loosely based SPARQL-like
	 * language. The method splits the query in two, the left side corresponding to
	 * the SELECT phase, the right corresponding to the WHERE phase.
	 *
	 * @param query
	 *            a string corresponding to a user entry.
	 * @return an instance of Result.
	 */
	public Result generateResult(String query) {

		// Split the query into its SELECT phase and its WHERE phase
		String[] splitQuery = query.split(":");

		String selectStatement = splitQuery[0];
		String[] selectorStrings = selectStatement.split(",");
		// Remove unnecessary whitespace around each selector variable
		List<String> selectorList = Arrays.asList(selectorStrings).stream().map(String::trim)
				.collect(Collectors.toList());
		selectorStrings = selectorList.toArray(new String[selectorList.size()]);

		String whereStatement = splitQuery[1]; // where -> ?X is ?Y & ?X has ?Z
		// ...
		// Split each WHERE substatement by '&'
		String[] conditionStatements = whereStatement.split("&"); // condition statement -> ?X is ?Y
		// Remove unnecessary whitespace around each condition statement
		conditionStatements = Arrays.asList(conditionStatements).stream().map(String::trim).toArray(String[]::new);

		// Iterate through all the conditions of the WHERE statement
		for (String conditionStatement : conditionStatements) {

			// Clear previous variables
			currentMatrix.clear();
			currentVariables.clear();
			currentVariableIndices.clear();
			currentVariableTypes.clear();

			String[] conditionStrings = conditionStatement.split(" ");
			if (conditionStrings[0].isEmpty()) {
				conditionStrings = Arrays.copyOfRange(conditionStrings, 1, conditionStrings.length);
			}

			// Evaluating left side of the condition
			String left = conditionStrings[0];

			// Fill current matrix
			if (left.charAt(0) == '?') {
				currentVariables.put(left, null);
				currentVariableTypes.put(left, DataTypes.SUBJECT);
				Set<Integer> keys;
				// Search for existing values for left variable
				if (globalVariables.containsKey(left)) {
					ArrayList<Data> values = globalVariables.get(left);
					keys = new HashSet<>();
					for (Data data : values) {
						Subject subject = (Subject) data;
						int key = database.findKey(subject);
						keys.add(key);
					}
				} else {
					keys = database.getAllKeys();
				}
				for (int key : keys) {
					EntryData entryData = database.getEntryData(key);
					String middle = conditionStrings[1];
					if (middle.charAt(0) == '?') {
						currentVariables.put(middle, null);
						currentVariableTypes.put(middle, DataTypes.RELATION);
						Set<Relation> relations;
						// Search for existing values for middle variable
						if (globalVariables.containsKey(middle)) {
							ArrayList<Data> values = globalVariables.get(middle);
							relations = new HashSet<>();
							for (Data data : values) {
								relations.add((Relation) data);
							}
						} else {
							relations = entryData.getRelations();
						}
						String right = conditionStrings[2];
						for (Relation relation : relations) {
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								currentVariableTypes.put(right, DataTypes.SUBJECT);
								ArrayList<Subject> subjects;
								// Search for existing values for right variable
								if (globalVariables.containsKey(right)) {
									ArrayList<Data> values = globalVariables.get(right);
									subjects = new ArrayList<>();
									for (Data data : values) {
										subjects.add((Subject) data);
									}
								} else {
									subjects = entryData.getSubjects(relation);
								}
								for (Subject subject : subjects) {
									if (entryData.hasRelation(relation)) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(database.getID(key));
											line.add(relation);
											line.add(subject);
											currentMatrix.add(line);
										}
									}
								}
							} else {
								Subject subject = database.findSubject(right);
								if (subject != null) {
									if (entryData.hasRelation(relation)) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(database.getID(key));
											line.add(relation);
											currentMatrix.add(line);
										}
									}
								}
							}
						}
					} else {
						Relation relation = database.findRelation(middle);
						if (relation != null) {
							if (entryData.hasRelation(relation)) {
								String right = conditionStrings[2];
								if (right.charAt(0) == '?') {
									currentVariables.put(right, null);
									currentVariableTypes.put(right, DataTypes.SUBJECT);
									ArrayList<Subject> subjects;
									// Search for existing values for right
									// variable
									if (globalVariables.containsKey(right)) {
										ArrayList<Data> values = globalVariables.get(right);
										subjects = new ArrayList<>();
										for (Data data : values) {
											subjects.add((Subject) data);
										}
									} else {
										subjects = entryData.getSubjects(relation);
									}
									for (Subject subject : subjects) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(database.getID(key));
											line.add(subject);
											currentMatrix.add(line);
										}
									}
								} else {
									Subject subject = database.findSubject(right);
									if (subject != null) {
										ArrayList<Subject> subjects = entryData.getSubjects(relation);
										if (subjects.contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(database.getID(key));
											currentMatrix.add(line);
										} else {
											// System.out.println("EntryData did not contain subject");
										}
									}
								}
							}
						}
					}
				}
			} else {
				int key = database.findKey(left);
				if (key != 0) {
					EntryData entryData = database.getEntryData(key);
					String middle = conditionStrings[1];
					if (middle.charAt(0) == '?') {
						currentVariables.put(middle, null);
						currentVariableTypes.put(middle, DataTypes.RELATION);
						Set<Relation> relations;
						// Search for existing values for middle variable
						if (globalVariables.containsKey(middle)) {
							ArrayList<Data> values = globalVariables.get(middle);
							relations = new HashSet<>();
							for (Data data : values) {
								relations.add((Relation) data);
							}
						} else {
							relations = entryData.getRelations();
							boolean modeDetected, back;
							String command;
						}
						for (Relation relation : relations) {
							String right = conditionStrings[2];
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								currentVariableTypes.put(right, DataTypes.SUBJECT);
								ArrayList<Subject> subjects;
								// Search for existing values for right variable
								if (globalVariables.containsKey(right)) {
									ArrayList<Data> values = globalVariables.get(right);
									subjects = new ArrayList<>();
									for (Data data : values) {
										subjects.add((Subject) data);
									}
								} else {
									subjects = entryData.getSubjects(relation);
								}
								for (Subject subject : subjects) {
									if (entryData.hasRelation(relation)) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(relation);
											line.add(subject);
											currentMatrix.add(line);
										}
									}
								}
							} else {
								Subject subject = database.findSubject(right);
								if (subject != null) {
									if (entryData.hasRelation(relation)) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(relation);
											currentMatrix.add(line);
										}
									}
								}
							}
						}
					} else {
						Relation relation = database.findRelation(middle);
						if (relation != null) {
							if (entryData.hasRelation(relation)) {
								String right = conditionStrings[2];
								if (right.charAt(0) == '?') {
									currentVariables.put(right, null);
									currentVariableTypes.put(right, DataTypes.SUBJECT);
									ArrayList<Subject> subjects;
									// Search for existing values for right
									// variable
									if (globalVariables.containsKey(right)) {
										ArrayList<Data> values = globalVariables.get(right);
										subjects = new ArrayList<>();
										for (Data data : values) {
											subjects.add((Subject) data);
										}
									} else {
										subjects = entryData.getSubjects(relation);
									}
									for (Subject subject : subjects) {
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(subject);
											currentMatrix.add(line);
										}
									}
								} else {
									// ?
								}
							}
						}
					}
				}
			}

			// Aggregate current variable values
			int currentVariableIndex = 0;
			for (String variable : currentVariables.keySet()) {
				ArrayList<Data> variableValues = new ArrayList<>();
				for (ArrayList<Data> line : currentMatrix) {
					Data data = line.get(currentVariableIndex);
					variableValues.add(data);
				}
				currentVariables.replace(variable, variableValues);
				currentVariableIndex++;
			}
			
			// Determine whether or not an associated view is required. The first occurence of intertwined conditional variables sets the field to true.
			if (currentVariables.size() > 1) {
				associatedView = true;
			}

			// Integrate current variable values into the global variable values
			// pool
			ArrayList<String> newVariables = new ArrayList<>();
			// Clear previous indices
			int currentIndex = 0;
			for (String variable : currentVariables.keySet()) {
				if (globalVariables.containsKey(variable)) {
					globalVariables.get(variable).retainAll(currentVariables.get(variable));
				} else {
					globalVariables.put(variable, currentVariables.get(variable));
					globalVariableIndices.put(variable, globalVariableIndex);
					incrementVariableIndex();
					newVariables.add(variable);
				}
				currentVariableIndices.put(variable, currentIndex++);
			}

			// Udpate global variables with types
			for (String variable : currentVariableTypes.keySet()) {
				globalVariableTypes.put(variable, currentVariableTypes.get(variable));
			}

			// Update the global matrix in accordance with global variable
			// values
			if (globalMatrix.size() > 0) {
				
				// Integrate new entries
				ArrayList<Data[]> newGlobalMatrix;
				
				ArrayList<String> intersectingVariables = new ArrayList<>();
				for (String variable : currentVariables.keySet()) {
					if (!newVariables.contains(variable)) {
						intersectingVariables.add(variable);
					}
				}
				
				switch (intersectingVariables.size()) {
				
				// Cartesian product
				case 0:
					newGlobalMatrix = new ArrayList<>();
					for (Data[] datafield : globalMatrix) {
						for (ArrayList<Data> line : currentMatrix) {
							Data[] newDatafield = new Data[globalVariableIndices.size()];
							for (String variable : globalVariables.keySet()) {
								int globalVariableIndex_ = globalVariableIndices.get(variable);
								if (datafield.length > globalVariableIndex_) { // Check if globalVariableIndex_ is applicable to the current globalMatrix
									newDatafield[globalVariableIndex_] = datafield[globalVariableIndex_];
								}
							}
							for (String variable : currentVariables.keySet()) {
								int globalVariableIndex_ = globalVariableIndices.get(variable);
								int currentVariableIndex_ = currentVariableIndices.get(variable);
								newDatafield[globalVariableIndex_] = line.get(currentVariableIndex_);
							}
							newGlobalMatrix.add(newDatafield);
						}
					}
					globalMatrix = newGlobalMatrix;
					break;
				
				case 1:
					String intersectingVariable = intersectingVariables.get(0);
					
					if (intersectingVariable != null) {
						int globalVariableIndex_ = globalVariableIndices.get(intersectingVariable);
						int currentVariableIndex_ = currentVariableIndices.get(intersectingVariable);
						newGlobalMatrix = new ArrayList<>();
						for (Data[] datafield : globalMatrix) {
							for (ArrayList<Data> line : currentMatrix) {
								if (datafield[globalVariableIndex_].equals(line.get(currentVariableIndex_))) {
									Data[] newDatafield = new Data[globalVariables.size()];
									for (String variable : globalVariables.keySet()) {
										if (!newVariables.contains(variable)) {
											int globalVariableIndex__ = globalVariableIndices.get(variable);
											newDatafield[globalVariableIndex__] = datafield[globalVariableIndex__];
										}
									}
									for (String variable : currentVariables.keySet()) {
										int globalVariableIndex__ = globalVariableIndices.get(variable);
										int currentVariableIndex__ = currentVariableIndices.get(variable);
										newDatafield[globalVariableIndex__] = line.get(currentVariableIndex__);
									}
									newGlobalMatrix.add(newDatafield);
								}
							}
						}
						globalMatrix = newGlobalMatrix;
					}
					break;
				case 2:
					if (intersectingVariables.size() == 2) {
						newGlobalMatrix = new ArrayList<>();
						for (Data[] datafield : globalMatrix) {
							for (ArrayList<Data> line : currentMatrix) {
								int variablesValid = 0;
								for (String variable : intersectingVariables) {
									int globalVariableIndex_ = globalVariableIndices.get(variable);
									int currentVariableIndex_ = currentVariableIndices.get(variable);
									if (datafield[globalVariableIndex_].equals(line.get(currentVariableIndex_))) {
										variablesValid++;
									}
								}
								if (variablesValid == 2) {
									Data[] newDatafield = new Data[globalVariables.size()];
									for (String variable : globalVariables.keySet()) {
										if (!newVariables.contains(variable)) {
											int globalVariableIndex_ = globalVariableIndices.get(variable);
											newDatafield[globalVariableIndex_] = datafield[globalVariableIndex_];
										}
									}
									for (String variable : currentVariables.keySet()) {
										int globalVariableIndex_ = globalVariableIndices.get(variable);
										int currentVariableIndex_ = currentVariableIndices.get(variable);
										newDatafield[globalVariableIndex_] = line.get(currentVariableIndex_);
									}
									newGlobalMatrix.add(newDatafield);
								}
							}
						}
						globalMatrix = newGlobalMatrix;
					}
					
					break;
				case 3:
					newGlobalMatrix = new ArrayList<>();
					for (Data[] datafield : globalMatrix) {
						for (ArrayList<Data> line : currentMatrix) {
							int variablesValid = 0;
							for (String variable : currentVariables.keySet()) {
								int globalVariableIndex_ = globalVariableIndices.get(variable);
								int currentVariableIndex_ = currentVariableIndices.get(variable);
								if (datafield[globalVariableIndex_].equals(line.get(currentVariableIndex_))) {
									variablesValid++;
								}
							}
							if (variablesValid == 3) {
								newGlobalMatrix.add(datafield);
							}
						}
					}
					globalMatrix = newGlobalMatrix;
					break;

				}
				
				
			} else { // New variable values arriving, the global matrix is empty
				for (ArrayList<Data> line : currentMatrix) {
					Data[] datafield = line.toArray(new Data[line.size()]);
					globalMatrix.add(datafield);
				}
			}
			
			// Update global variables according to global matrix
			for (String variable : globalVariables.keySet()) {
				int globalVariableIndex_ = globalVariableIndices.get(variable);
				ArrayList<Data> newVariableValues = new ArrayList<>();
				for (Data[] datafield : globalMatrix) {
					newVariableValues.add(datafield[globalVariableIndex_]);
				}
				globalVariables.replace(variable, newVariableValues);
			}
		}

		Result result = new Result(database, associatedView);
		for (String selectorString : selectorStrings) {
			ArrayList<Data> datafield = globalVariables.get(selectorString);
			result.putData(selectorString, datafield);
			result.putDataType(selectorString, globalVariableTypes.get(selectorString));
		}

		return result;

	}

}
