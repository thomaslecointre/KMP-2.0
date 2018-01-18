package query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.Data;
import model.Relation;
import model.Subject;
import persistence.Database;
import persistence.EntryData;

public class Context {

	private ArrayList<ArrayList<Data>> globalMatrix;
	private ArrayList<ArrayList<Data>> currentMatrix;
	private HashMap<String, ArrayList<Data>> globalVariables;
	private HashMap<String, ArrayList<Data>> currentVariables;
	private Database database;

	public boolean containsKey(String identifier) {
		return globalVariables.containsKey(identifier);
	}

	public Context(Database database) {
		this.database = database;
		globalMatrix = new ArrayList<>();
		globalVariables = new HashMap<>();
	}

	// TODO Result needs to be regenerated
	public Result generateResult(String query) {

		Result result = new Result();

		return result;
	}

	/**
	 * Method used to process queries written in a loosely based SPARQL-like
	 * language. The method splits the query in two, the left side corresponding to
	 * the SELECT phase, the right corresponding to the WHERE phase.
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

		String whereStatement = splitQuery[1]; // where -> ?X is ?Y & ?X has ?Z ...
		// Split each WHERE substatement by '&'
		String[] conditionStatements = whereStatement.split("&"); // condition statement -> ?X is ?Y
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

			if (left.charAt(0) == '?') {
				currentVariables.put(left, null);				
				Set<Integer> keys = database.getAllKeys();
				for (int key : keys) {
					EntryData entryData = database.getEntryData(key);
					String middle = conditionStrings[1];
					if (middle.charAt(0) == '?') {
						currentVariables.put(middle, null);
						String right = conditionStrings[2];
						if (right.charAt(0) == '?') {
							currentVariables.put(right, null);
							for (Relation relation : entryData.getRelations()) {
								for (Subject subject : entryData.getSubjects(relation)) {
									ArrayList<Data> line = new ArrayList<>();
									line.add(database.getID(key));
									line.add(relation);
									line.add(subject);
									currentMatrix.add(line);
								}
							}
						} else {
							Subject subject = database.findSubject(right);
							if (subject != null) {
								for (Relation relation : entryData.getRelations()) {
									if (entryData.getSubjects(relation).contains(subject)) {
										ArrayList<Data> line = new ArrayList<>();
										line.add(database.getID(key));
										line.add(relation);
										currentMatrix.add(line);
									}
								}
							}
						}
					} else {
						Relation relation = database.findRelation(middle);
						if (relation != null) {
							String right = conditionStrings[2];
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								for (Subject subject : entryData.getSubjects(relation)) {
									ArrayList<Data> line = new ArrayList<>();
									line.add(database.getID(key));
									line.add(subject);
									currentMatrix.add(line);
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
			} else {
				int key = database.findKey(left);
				if (key != 0) {
					EntryData entryData = database.getEntryData(key);
					String middle = conditionStrings[1];
					if (middle.charAt(0) == '?') {
						currentVariables.put(middle, null);
						for (Relation relation : entryData.getRelations()) {
							String right = conditionStrings[2];
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								for (Subject subject : entryData.getSubjects(relation)) {
									ArrayList<Data> line = new ArrayList<>();
									line.add(relation);
									line.add(subject);
									currentMatrix.add(line);
								}
							} else {
								Subject subject = database.findSubject(right);
								if (subject != null) {
									if (entryData.getSubjects(relation).contains(subject)) {
										ArrayList<Data> line = new ArrayList<>();
										line.add(relation);
										currentMatrix.add(line);
									}
								}
							}
						}
					} else {
						Relation relation = database.findRelation(middle);
						if (relation != null) {
							String right = conditionStrings[2];
							if (right.charAt(0) == '?') {
								currentVariables.put(right, null);
								if (entryData.containsRelation(relation)) {
									for (Subject subject : entryData.getSubjects(relation)) {
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
			
			int column = 0;
			for (String variable : currentVariables.keySet()) {
				ArrayList<Data> variableValues = new ArrayList<>();
				for (ArrayList<Data> line : currentMatrix) {
					Data data = line.get(column);
					variableValues.add(data);
				}
				currentVariables.replace(variable, variableValues);
			}
			
			for (String variable : currentVariables.keySet()) {
				if (globalVariables.containsKey(variable)) {
					globalVariables.get(variable).retainAll(currentVariables.get(variable));
				} else {
					globalVariables.put(variable, currentVariables.get(variable));
				}
			}

		}
	}

}
