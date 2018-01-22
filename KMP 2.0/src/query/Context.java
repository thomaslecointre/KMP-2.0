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

public class Context {

	private ArrayList<Data[]> globalMatrix;
	private HashMap<String, HashSet<Data>> globalVariables;
	
	private ArrayList<ArrayList<Data>> currentMatrix;
	private HashMap<String, HashSet<Data>> currentVariables;
	
	private HashMap<String, Integer> variableIndices;
	
	private int variableIndex = 0;
	private Database database;
	
	private void incrementVariableIndex() {
		variableIndex++;
	}
	public boolean containsKey(String identifier) {
		return globalVariables.containsKey(identifier);
	}

	public Context(Database database) {
		this.database = database;
		globalMatrix = new ArrayList<>();
		globalVariables = new HashMap<>();
		variableIndices = new HashMap<>();
	}

	// TODO Result needs to be regenerated
	public Result generateResult(String query) {

		Result result = new Result();

		return result;
	}

	/**
	 * Method used to process queries written in a loosely based SPARQL-like
	 * language. The method splits the query in two, the left side corresponding
	 * to the SELECT phase, the right corresponding to the WHERE phase.
	 * 
	 * @param query
	 *            a string corresponding to a user entry.
	 */
	private void requestQuery(String query) {

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
		String[] conditionStatements = whereStatement.split("&"); // condition
																	// statement
																	// -> ?X is
																	// ?Y
		// Remove unnecessary whitespace around each condition statement
		List<String> conditionList = Arrays.asList(conditionStatements).stream().map(String::trim)
				.collect(Collectors.toList());
		conditionStatements = conditionList.toArray(new String[conditionList.size()]);

		currentMatrix = new ArrayList<>();
		currentVariables = new HashMap<>();

		// Iterate through all the conditions of the WHERE statement
		for (String conditionStatement : conditionStatements) {

			String[] conditionStrings = conditionStatement.split(" ");
			if (conditionStrings[0].isEmpty()) {
				conditionStrings = Arrays.copyOfRange(conditionStrings, 1, conditionStrings.length);
			}

			// Evaluating left side of the condition
			String left = conditionStrings[0];

			// Fill current matrix
			if (left.charAt(0) == '?') {
				currentVariables.put(left, null);
				Set<Integer> keys;
				// Search for existing values for left variable
				if (globalVariables.containsKey(left)) {
					HashSet<Data> values = globalVariables.get(left);
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
						Set<Relation> relations;
						// Search for existing values for middle variable
						if (globalVariables.containsKey(middle)) {
							HashSet<Data> values = (HashSet<Data>) globalVariables.get(middle);
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
								Set<Subject> subjects;
								// Search for existing values for right variable
								if (globalVariables.containsKey(right)) {
									HashSet<Data> values = (HashSet<Data>) globalVariables.get(right);
									subjects = new HashSet<>();
									for (Data data : values) {
										subjects.add((Subject) data);
									}
								} else {
									subjects = entryData.getSubjects(relation);
								}
								for (Subject subject : subjects) {
									if (entryData.containsRelation(relation)) {
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
									if (entryData.containsRelation(relation)) {
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
							if (entryData.containsRelation(relation)) {
								String right = conditionStrings[2];
								if (right.charAt(0) == '?') {
									currentVariables.put(right, null);
									Set<Subject> subjects;
									// Search for existing values for right
									// variable
									if (globalVariables.containsKey(right)) {
										HashSet<Data> values = (HashSet<Data>) globalVariables.get(right);
										subjects = new HashSet<>();
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
										if (entryData.getSubjects(relation).contains(subject)) {
											ArrayList<Data> line = new ArrayList<>();
											line.add(database.getID(key));
											currentMatrix.add(line);
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
						Set<Relation> relations;
						// Search for existing values for middle variable
						if (globalVariables.containsKey(middle)) {
							HashSet<Data> values = (HashSet<Data>) globalVariables.get(middle);
							relations = new HashSet<>();
							for (Data data : values) {
								relations.add((Relation) data);
							}
						} else {
							relations = entryData.getRelations();
						}
						for (Relation relation : relations) {
							String right = conditionStrings[2];
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								Set<Subject> subjects;
								// Search for existing values for right variable
								if (globalVariables.containsKey(right)) {
									HashSet<Data> values = (HashSet<Data>) globalVariables.get(right);
									subjects = new HashSet<>();
									for (Data data : values) {
										subjects.add((Subject) data);
									}
								} else {
									subjects = entryData.getSubjects(relation);
								}
								for (Subject subject : subjects) {
									if (entryData.containsRelation(relation)) {
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
									if (entryData.containsRelation(relation)) {
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
							if (entryData.containsRelation(relation)) {
								String right = conditionStrings[2];
								if (right.charAt(0) == '?') {
									currentVariables.put(right, null);
									Set<Subject> subjects;
									// Search for existing values for right
									// variable
									if (globalVariables.containsKey(right)) {
										HashSet<Data> values = (HashSet<Data>) globalVariables.get(right);
										subjects = new HashSet<>();
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
			int column = 0;
			for (String variable : currentVariables.keySet()) {
				HashSet<Data> variableValues = new HashSet<>();
				for (ArrayList<Data> line : currentMatrix) {
					Data data = line.get(column);
					variableValues.add(data);
				}
				currentVariables.replace(variable, variableValues);
				column++;
			}

			// Integrate current variable values into the global variable values
			// pool
			ArrayList<String> newVariables = new ArrayList<>();
			for (String variable : currentVariables.keySet()) {
				if (globalVariables.containsKey(variable)) {
					globalVariables.get(variable).retainAll(currentVariables.get(variable));
				} else {
					globalVariables.put(variable, currentVariables.get(variable));
					variableIndices.put(variable, variableIndex);
					incrementVariableIndex();
					newVariables.add(variable);
				}
			}

			// Update the global matrix in accordance with global variable
			// values
			if (globalMatrix.size() > 0) {
				// Remove entries not in accordance with global variable values
				column = 0;
				for (String variable : globalVariables.keySet()) {
					ArrayList<Data[]> restrictedGlobalMatrix = (ArrayList<Data[]>) globalMatrix.clone();
					for (Data[] datafield : globalMatrix) {
						if (!globalVariables.get(variable).contains(datafield[column])) {
							restrictedGlobalMatrix.remove(datafield);
						}
					}
					globalMatrix = restrictedGlobalMatrix;
					column++;
				}
				// TODO Merge current matrix into global matrix
				switch (newVariables.size()) {
				case 0:
					// TODO 
					break;
				case 1:
					ArrayList<String> intersectingVariables = new ArrayList<>();
					for (String variable : currentVariables.keySet()) {
						if (!newVariables.contains(variable)) {
							intersectingVariables.add(variable);
						}
					}
					
					if (intersectingVariables.size() == 2) {
						// int index = variableIndices.get(intersectingVariable);
						ArrayList<Data[]> globalMatrixClone = (ArrayList<Data[]>) globalMatrix.clone();
						for (Data[] datafield : globalMatrix) {
							for (ArrayList<Data> line : currentMatrix) {
								if (datafield[index % datafield.length] == line.get(index % line.size())) {
									Data[] newDatafield = new Data[globalVariables.size()];
									int newIndex = 0;
									newDatafield[index % newDatafield.length] = datafield[index % datafield.length];
									for (Data data : datafield) {
										if (newIndex != index % newDatafield.length) {
											newDatafield[newIndex++] = data;
										} else {
											newIndex++;
										}
									}
									for (Data data : line) {
										if (newIndex != index % newDatafield.length) {
											newDatafield[newIndex++] = data;
										} else {
											newIndex++;
										}
									}
									globalMatrixClone.add(newDatafield);
								}
							}
						}
						globalMatrix = globalMatrixClone;
					}
					
					break;
				case 2:
					String intersectingVariable = null;
					for (String variable : currentVariables.keySet()) {
						if (!newVariables.contains(variable)) {
							intersectingVariable = variable;
							break;
						}
					}
					
					if (intersectingVariable != null) {
						int index = variableIndices.get(intersectingVariable);
						ArrayList<Data[]> globalMatrixClone = (ArrayList<Data[]>) globalMatrix.clone();
						for (Data[] datafield : globalMatrix) {
							for (ArrayList<Data> line : currentMatrix) {
								if (datafield[index % datafield.length] == line.get(index % line.size())) {
									Data[] newDatafield = new Data[globalVariables.size()];
									int newIndex = 0;
									newDatafield[index % newDatafield.length] = datafield[index % datafield.length];
									for (Data data : datafield) {
										if (newIndex != index % newDatafield.length) {
											newDatafield[newIndex++] = data;
										} else {
											newIndex++;
										}
									}
									for (Data data : line) {
										if (newIndex != index % newDatafield.length) {
											newDatafield[newIndex++] = data;
										} else {
											newIndex++;
										}
									}
									globalMatrixClone.add(newDatafield);
								}
							}
						}
						globalMatrix = globalMatrixClone;
					}
					
					break;
				case 3:
					ArrayList<Data[]> newGlobalMatrix = new ArrayList<>();
					for (Data[] dataField : globalMatrix) {
						
						for (ArrayList<Data> line : currentMatrix) {
							Data[] newDataField = new Data[line.size() + dataField.length];
							for (String variable : globalVariables.keySet()) {
								int index = variableIndices.get(variable);
								newDataField[index % newDataField.length] = dataField[index % dataField.length];
							}
							for (String variable : currentVariables.keySet()) {
								int index = variableIndices.get(variable);
								newDataField[index % newDataField.length] = line.get(index % line.size());
							}
							newGlobalMatrix.add(newDataField);
						}
					}
					globalMatrix = newGlobalMatrix;
					break;
				
				}
				// TODO Update global variable values

			} else { // New variable values arriving, the global matrix is empty
				for (ArrayList<Data> line : currentMatrix) {
					Data[] datafield = (Data[]) line.toArray();
					globalMatrix.add(datafield);
				}
			}
		}
	}

}
