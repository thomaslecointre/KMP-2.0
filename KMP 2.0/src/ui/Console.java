package ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Relation.Properties;
import query.Result;
import query.TransactionHandler;

/**
 * This class is the closest to the user. It interacts with TransactionHandler for requests.
 */
public class Console implements Runnable {

	private Scanner scanner;
	private TransactionHandler transactionHandler;
	private boolean active;

	/**
	 * Enumeration of all types of commands the user can use.
	 */
	private enum Modes {
		INSERT("insert"), QUERY("query"), INSPECT_RELATIONS("inspect relations"), UNDO("undo"), REDO("redo"), IMPORT("import"), EXPORT("export"), RESET("reset"), SHOW("show"), HELP("help"), BACK("back"), QUIT("quit");

		private final String REPRESENTATION;

		Modes(String representation) {
			REPRESENTATION = representation;
		}

		void helpMessage() {
			switch (this) {
			case INSERT:
				System.out.println(
						"\nThe parity of a word's index matters. Odd indexed words are subjects. Even indexed words are entry headers.");
				System.out.println("\nAn entry is comprised of a primary key, headers and values. A triple is a combination of the primary key, a header and its corresponding value.");
				break;
			case QUERY:
				System.out.println("\nThe query engine uses a SPARQL inspired syntax and requires the user to input queries in the following format:");
				System.out.println("\n?X, ?Y : ?X is ?Z & ?Z has 5");
				System.out.println("\nAll variables prefixed by '?' located before the colon are those that will be displayed.");
				System.out.println("\nEverything written after the colon equates to the WHERE block in a SPARQL query. Each line is seperated by the '&' character.");
				break;
			case INSPECT_RELATIONS:
				System.out.println("\nDecide which properties are appropriate for a particular relation.");
				System.out.println("\nThe database will automatically adjust itself in accordance with each property adjustment.");
				break;
			case HELP:
				listOfCommands();
				break;
			case BACK:
				System.out.println("\nGoing back...");
				break;
			case UNDO:
				System.out.println("\nLoading previous state...");
				break;
			case REDO:
				System.out.println("\nLoading next state...");
				break;
			case IMPORT:
				System.out.println("\nImporting Database...");
				break;
			case EXPORT:
				System.out.println("\nExporting Database...");
				break;
			case RESET:
				System.out.println("\nResetting database...");
				break;
			case QUIT:
				System.out.println("\nExiting application...");
				break;
			default:
				break;
			}
		}

	}

	private Modes mode;
	private Modes parentMode;
	private String promptMessage = "\n> ";
	
	/**
	 * Sets prompt message.
	 */
	private void setPromptMessage(Modes mode) {
		promptMessage = "\n" + mode.REPRESENTATION + " > ";
	}
	
	/**
	 * Resets prompt message.
	 */
	private void resetPromptMessage() {
		promptMessage = "\n> ";
	}

	/**
	 * Prints out the prompt to the standard output.
	 */
	private void promptMessage() {
		System.out.print(promptMessage);
	}

	private String illegalCommand = "\nIllegal command!";

	/**
	 * Prints out the illegal command message to the standard output.
	 */
	private void illegalCommand() {
		System.out.println(illegalCommand);
	}

	/**
	 * Checks for a valid mode change. When launching for the first time, UNDO and RESET are disabled.
	 * @param command a string used to identify a command.
	 * @return a boolean indicating that a command has been found.
	 */
	private boolean modeChanged(String command) {
		for (Modes mode : Modes.values()) {
			if (command.contains(mode.REPRESENTATION)) {
				if(mode == Modes.BACK) {
					return false;
				}
				this.mode = mode;
				mode.helpMessage();
				return true;
			}
		}
		return false;
	}

	public Console() {
	}

	@Override
	public void run() {
		transactionHandler = new TransactionHandler();
		scanner = new Scanner(System.in);
		active = true;
		prompt();
		scanner.close();
	}

	/**
	 * Assigns the current parent mode.
	 * The parent mode field is used to determine the correct function to call upon user entry.
	 */
	private void changeMode() {
		String ingestedLine = scanner.nextLine();
		Boolean modeChanged = modeChanged(ingestedLine);
		if (!modeChanged) {
			mode = parentMode;
		}
	}

	/**
	 * Lists all user commands.
	 */
	private static void listOfCommands() {
		System.out.println("Possible commands...\n");
		for (Modes mode : Modes.values()) {
			System.out.println(mode.REPRESENTATION);
		}
		System.out.println("\nUse 'back' to return to the main menu.");
	}

	/**
	 * Checks if a Mode is present in the string in parameter 
	 * @param s a text
	 * @return if a Mode is present in the string
	 */
	private boolean isKeyWord(String s) {
		Modes[] modes = Modes.values();
		StringBuilder keywords = new StringBuilder();
		for (int i = 0; i < modes.length-1; i++) {
			keywords.append(modes[i] + "|");
		}
		keywords.append(modes[modes.length-1]);
		
		Pattern pattern = Pattern.compile(keywords.toString(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }
	
	/**
	 * Checks if a Relation is present in the string in parameter
	 * @param s a text
	 * @return if a Relation is present in the string
	 */
	private boolean isKeyWordRelation(String s) {
		String keywords = transactionHandler.patternMatcherRelations();
		
		Pattern pattern = Pattern.compile(keywords.toString(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }
	
	/**
	 * Checks if 'is' or 'not' is present in the string in parameter
	 * @param s a text
	 * @return if 'is' or 'not' is present in the string
	 */
	private boolean isKeyWordRelationIsNot(String s) {
		String keywords = "is|not";
		
		Pattern pattern = Pattern.compile(keywords.toString(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }
	
	/**
	 * Checks if a Property is present in the string in parameter
	 * @param s a text
	 * @return Checks if a Property is present in the string
	 */
	private boolean isKeyWordRelationProperty(String s) {
		Properties[] properties = Properties.values();
		StringBuilder keywords = new StringBuilder();
		for (int i = 0; i < properties.length-1; i++) {
			keywords.append(properties[i] + "|");
		}
		keywords.append(properties[properties.length-1]);
		
		Pattern pattern = Pattern.compile(keywords.toString(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }
	
	/**
	 * Checks if the command is a valid inspect relation query
	 * @param command an inspect relation command
	 * @return if the command is a valid inspect relation query
	 */
	private boolean validateInspectRelation(String command) {
		String[] tokens = command.split(" ");
		
		//check the number of words
		if (tokens.length != 3) {
			System.out.println("Incorrect : number of words");
			return false;
		}
		
		//check if the first token is a relation
		if (!isKeyWordRelation(tokens[0])) {
			System.out.println("Incorrect : first token isn't a relation");
			return false;
		}
		
		//check if the second token is "is" or "not"
		if (!isKeyWordRelationIsNot(tokens[1])) {
			System.out.println("Incorrect : second token is different of 'is' or 'not'");
			return false;
		}
		
		//check if the third token is a property of a relation
		if (!isKeyWordRelationProperty(tokens[2])) {
			System.out.println("Incorrect : third token isn't a property of Relation");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the command is a valid insertion
	 * @param command an insertion command
	 * @return if the command is a valid insertion
	 */
	private boolean validateInsertion(String command) {
		String[] tokens = command.split(" ");
		
		//check the parity (number of words)
		if (tokens.length < 3 || tokens.length % 2 == 0) {
			System.out.println("Incorrect : number of words");
			return false;
		}
		
		//check the presence of keywords
		for (String token : tokens) {
			if (isKeyWord(token)) {
				System.out.println("Incorrect : keyword in the tokens");
				return false;
			}
		}
		
		//check tokens valid a-zA-Z_0-9
		Pattern patternValidTokens = Pattern.compile("\\w+\\s+\\w+\\s+\\w+(\\s+\\w+\\s+\\w+)*");
		Matcher matcherValidTokens = patternValidTokens.matcher(command);
		if (!matcherValidTokens.matches()) {
			System.out.println("Incorrect : command should be like 'a b c' or 'laurent is man is Married Sophie worksFor ENSISA'");
			return false;
		}
		
		//check ID : Spiderman ID PeterParker
		Pattern patternPresenceID = Pattern.compile("\\s+(id)\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcherPresenceID = patternPresenceID.matcher(command);
		if (matcherPresenceID.find()) {
			System.out.println("Incorrect : 'id' is not allowed");
			return false;
		}
		
		return true;
	}

	/**
	 * Checks if the command is a valid query
	 * @param command a query command
	 * @return if the command is a valid query
	 */
	private static boolean validateQuery(String command) {
		//check the global structure
		if (command.split(":").length != 2) {
			System.out.println("Incorrect : structure should be 'data : query' with no other ':'");
			return false;
		}
		String data = command.split(":")[0], where = command.split(":")[1];
		
		//check the data structure
		Pattern patternDataStructure = Pattern.compile("\\?\\w+\\s*(,\\s*\\?\\w+\\s*)*");
		Matcher matcherDataStruture = patternDataStructure.matcher(data);
		if (!matcherDataStruture.matches()) {
			System.out.println("Incorrect : data should be like '?x' or '?x, ?y, ?z'");
			return false;
		}
		
		//check that variables have different names : ?x ?y ?x
		Pattern patternDataVariable = Pattern.compile("\\\\?(\\w+)");
		Matcher matcherDataVariable = patternDataVariable.matcher(data);
		ArrayList<String> variables = new ArrayList<String>();
		while(matcherDataVariable.find()) {
            if (variables.contains(matcherDataVariable.group())) {
        		System.out.println("Incorrect : same name in data " + matcherDataVariable.group());
        		return false;
            }
            else variables.add(matcherDataVariable.group());
        }
		
		//check the where structure
		Pattern patternWhereStructure = Pattern.compile("(\\s+\\??\\w+){3}(\\s+&(\\s+\\??\\w+){3})*");
		Matcher matcherWhereStruture = patternWhereStructure.matcher(where);
		if (!matcherWhereStruture.matches()) {
			System.out.println("Incorrect : where should be like '?x ?y ?z' or '?laurent is man & laurent ?worksFor ENSISA'");
			return false;
		}
		
		//check ID in where : ?x ID laurent
		Pattern patternWhereID = Pattern.compile("\\s+(id)\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcherWhereID = patternWhereID.matcher(where);
		if (matcherWhereID.find()) {
			System.out.println("Incorrect : 'id' is not allowed");
			return false;
		}
		
		//check no variable unused : ?x, ?y : ?x is man
		ArrayList<String> whereVariables = new ArrayList<String>();
		Pattern patternUnusedVariable = Pattern.compile("\\\\?(\\w+)");
		Matcher matcherUnusedVariable = patternUnusedVariable.matcher(where);
		while(matcherUnusedVariable.find()) {
            whereVariables.add(matcherUnusedVariable.group());
        }
		if (!whereVariables.containsAll(variables)) {
			System.out.println("Incorrect : unused variable in the data, data : " + variables + " where : " + whereVariables);
			return false;
		}
		
		//check relation not placed at the extremity
		ArrayList<String> ids = new ArrayList<String>(), relations = new ArrayList<String>(), subjects = new ArrayList<String>();
		Pattern patternRelations = Pattern.compile("\\s*(\\??\\w+)\\s+(\\??\\w+)\\s+(\\??\\w+)(\\s+&)?");
		Matcher matcherRelations = patternRelations.matcher(where);
		while(matcherRelations.find()) {
            ids.add(matcherRelations.group(1));
			relations.add(matcherRelations.group(2));
			subjects.add(matcherRelations.group(3));
        }
		for (String relation : relations) {
			if (ids.contains(relation) || subjects.contains(relation)) {
				System.out.println("Incorrect : conflict between relations and IDs/subjects");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if the command is a valid import
	 * @param command a string written by the user
	 * @return if the command is a valid import
	 */
	private static boolean validateImport(String command) {
		Pattern patternPathStructure = Pattern.compile(".*\\.kmp");
		Matcher matcherPathStruture = patternPathStructure.matcher(command);
		if (!matcherPathStruture.matches()) {
			System.out.println("Incorrect : path sould finished by .kmp");
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the command is a valid export
	 * @param command a string written by the user
	 * @return if the command is a valid export
	 */
	private static boolean validateExport(String command) {
		Pattern patternPathStructure = Pattern.compile(".*\\.kmp");
		Matcher matcherPathStruture = patternPathStructure.matcher(command);
		if (!matcherPathStruture.matches()) {
			System.out.println("Incorrect : path sould finished by .kmp");
			return false;
		}
		return true;
	}
	
	/**
	 * Top level user input handling function.
	 */
	private void prompt() {
		System.out.println("Welcome to Knowledge Management Platform");
		System.out.println("----------------------------------------");
		listOfCommands();
		while (active) {
			promptMessage();
			changeMode();
			if (mode != null) {
				switch (mode) {
				case INSERT:
					setPromptMessage(Modes.INSERT);
					while (mode == Modes.INSERT) {
						String insertion = nextCommand(Modes.INSERT);
						if (insertion != null) {
							transactionHandler.requestInsert(insertion);
							try {
								transactionHandler.getDatabaseSerializer().insertCommand(transactionHandler.getDatabase());
							} catch (IOException e) {
								e.printStackTrace();
							}
							transactionHandler.requestShow();
						} else {
							resetPromptMessage();
						}
					}
					break;
				case QUERY:
					setPromptMessage(Modes.QUERY);
					while (mode == Modes.QUERY) {
						String query = nextCommand(Modes.QUERY);
						if (query != null) {
							Result result = transactionHandler.requestQuery(query);
							System.out.println(result);
						} else {
							resetPromptMessage();
						}
					}
					break;
				case INSPECT_RELATIONS:
					setPromptMessage(Modes.INSPECT_RELATIONS);
					while (mode == Modes.INSPECT_RELATIONS) {
						String relationString = transactionHandler.showRelations();
						System.out.println(relationString);
						String command = nextCommand(Modes.INSPECT_RELATIONS);
						if (command != null)  {
							transactionHandler.updateRelation(command);
							try {
								transactionHandler.getDatabaseSerializer().inspectRelationsCommand(transactionHandler.getDatabase());
							} catch (IOException e) {
								e.printStackTrace();
							}
							transactionHandler.requestShow();
						} else {
							resetPromptMessage();
						}
					}
					break;
				case HELP:
					listOfCommands();
					break;
				case IMPORT:
					setPromptMessage(Modes.IMPORT);
					String importPath = nextCommand(Modes.IMPORT);
					if (importPath != null) {
						transactionHandler.requestImport(importPath);
						transactionHandler.requestShow();
					}
					resetPromptMessage();
					//mode.helpMessage();
					break;
				case EXPORT:
					setPromptMessage(Modes.EXPORT);
					String exportPath = nextCommand(Modes.EXPORT);
					if (exportPath != null)
						transactionHandler.requestExport(exportPath);
					resetPromptMessage();
					//mode.helpMessage();
					break;
				case SHOW:
					transactionHandler.requestShow();
					break;
				case RESET:
					transactionHandler.requestReset();
					transactionHandler.requestShow();
					break;
				case QUIT:
					active = false;
					try {
						transactionHandler.getDatabaseSerializer().quitCommand();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case BACK:
				case UNDO:
				case REDO:
					illegalCommand();
					break;
				default:
					break;
				}
			} else {
				illegalCommand();
			}
			
		}
	}
	
	/**
	 * Determines the correct function to call in TransactionHandler depending on the currentMode parameter.
	 * @param currentMode a instance of the Modes enum.
	 * @return a string corresponding to the user entry, used by TransactionHandler either as an insertion or a query.
	 */
	private String nextCommand(Modes currentMode) {
		parentMode = currentMode;
		boolean modeDetected, back, validEntry = false;
		String command;
		do {
			promptMessage();
			command = scanner.nextLine();
			modeDetected = false;
			back = false;
			for (Modes mode : Modes.values()) {
				if (command.contains(mode.REPRESENTATION)) {
					switch (mode) {
					case INSERT:
					case QUERY:
					case INSPECT_RELATIONS:
					case IMPORT:
					case EXPORT:
					case RESET:
						illegalCommand();
						break;
					case SHOW:
						transactionHandler.requestShow();
						break;
					case HELP:
						currentMode.helpMessage();
						break;
					case BACK:
						if (currentMode == Modes.INSERT || currentMode == Modes.QUERY || currentMode == Modes.INSPECT_RELATIONS || currentMode == Modes.IMPORT || currentMode == Modes.EXPORT) {
							this.mode = null;
							parentMode = null;
							back = true;
							listOfCommands();
						} else illegalCommand();
						break;
					case UNDO:
						if (currentMode == Modes.INSERT || currentMode == Modes.QUERY || currentMode == Modes.INSPECT_RELATIONS) {
							transactionHandler.requestUndo();
							transactionHandler.requestShow();
						} else illegalCommand();
						break;
					case REDO:
						if (currentMode == Modes.INSERT || currentMode == Modes.QUERY || currentMode == Modes.INSPECT_RELATIONS) {
							transactionHandler.requestRedo();
							transactionHandler.requestShow();
						} else illegalCommand();
						break;
					case QUIT:
						illegalCommand();
						break;
					default:
						break;
					}
					command = null;
					modeDetected = true;
					break;
				} 
			}
			if (!modeDetected) {
				switch (currentMode) {				
				case INSERT:
					if (validateInsertion(command))
						validEntry = true;
					break;
				case QUERY:
					if (validateQuery(command))
						validEntry = true;
					break;
				case INSPECT_RELATIONS:
					if (validateInspectRelation(command))
						validEntry = true;
					break;
				case IMPORT:
					if (validateImport(command))
						validEntry = true;
					break;
				case EXPORT:
					if (validateExport(command))
						validEntry = true;
					break;
				default:
					break;
				}
			}
		} while (!back && !validEntry);
		
		return command;
	}
}
