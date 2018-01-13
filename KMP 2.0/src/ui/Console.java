package ui;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import persistence.TransactionHandler;
import query.Result;

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
		INSERT("insert"), QUERY("query"), HELP("help"), BACK("back"), UNDO ("undo"), RESET("reset"), SHOW("show"), QUIT(
				"quit");

		private final String REPRESENTATION;

		Modes(String representation) {
			REPRESENTATION = representation;
		}

		void helpMessage() {
			switch (this) {
			case INSERT:
				System.out.println(
						"\nThe parity of a word's location matters. Odd indexed words are subjects. Even indexed words are entry headers.");
				System.out.println("\nAn entry is comprised of a primary key, headers and values. A triple is a combination of the primary key, a header and its corresponding value.");
				break;
			case QUERY:
				System.out.println("\nThe query engine uses a SPARQL inspired syntax and requires the user to input queries in the following format:");
				System.out.println("\n?X, ?Y : ?X is ?Z & ?Z has 5");
				System.out.println("\nAll variables prefixed by '?' located before the colon are those that will be displayed.");
				System.out.println("\nEverything written after the colon equates to the WHERE block in a SPARQL query. Each line is seperated by the '&' character.");
				break;
			case HELP:
				listOfCommands();
				break;
			case BACK:
				System.out.println("\nGoing back...");
				break;
			case UNDO:
				System.out.println("\nRemoving last entry...");
				break;
			case RESET:
				System.out.println("\nResetting database...");
				break;
			case QUIT:
				System.out.println("\nExiting application...");
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
				if(mode == Modes.UNDO || mode == Modes.RESET || mode == Modes.BACK) {
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
		mode = Modes.INSERT;
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
						case SHOW:
						illegalCommand();
						break;
					case HELP:
						currentMode.helpMessage();
						break;
					case BACK:
						this.mode = Modes.BACK;
						back = true;
						parentMode = null;
						listOfCommands();
						break;
					case UNDO:
						if (currentMode == Modes.INSERT) {
							transactionHandler.requestUndo();
							mode.helpMessage();
						}
						if (currentMode == Modes.QUERY) {
							illegalCommand();
						}
						break;
					case RESET:
						if (currentMode == Modes.INSERT) {
							transactionHandler.requestReset();
							mode.helpMessage();
						}
						if (currentMode == Modes.QUERY) {
							illegalCommand();
						}
						break;
					case QUIT:
						illegalCommand();
						break;
					}
					command = null;
					modeDetected = true;
					break;
				} 
			}
			if (!modeDetected) {
				if (currentMode == Modes.INSERT) {
					if (validateInsertion(command)) {
						validEntry = true;
					}
				}
				if (currentMode == Modes.QUERY) {
					if (validateQuery(command)) {
						validEntry = true;
					}
				}
			}
		} while ((!back && modeDetected) || !validEntry);
		
		return command;
	}

	/**
	 * Checks if the Modes enum are present in the string in parameter 
	 * @param s a text
	 * @return a boolean if a Modes enum is present in the string
	 */
	private static boolean isKeyWord(String s) {
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
	
	//TODO allow more characters
	/**
	 * Checks if the command is a valid insertion
	 * @param command a string written by the user 
	 * @return a boolean if the command is a valid insertion
	 */
	private static boolean validateInsertion(String command) {
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
		for (String token : tokens) {
			if (isKeyWord(token)) {
				System.out.println("Incorrect : keyword in the tokens");
				return false;
			}
		}
		return true;
	}

    //TODO allow more characters
	/**
	 * Checks if the command is a valid query
	 * @param command a string written by the user
	 * @return a boolean if the command is a valid query
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
				case SHOW:
					transactionHandler.requestShow();
					break;
				case QUIT:
					active = false;
					break;
				}
			} else {
				illegalCommand();
			}
			
		}
	}
	
	/*
	public static void main(String[] args) {
		//insertion
		System.out.println(validateInsertion("laurent"));
		System.out.println(validateInsertion("laurent isMarried Sophie"));
		System.out.println(validateInsertion("laurent isMarried Sophie worksfor ENSISA"));
		System.out.println(validateInsertion("laurent is Married Sophie"));     //false
		//query
		System.out.println(validateQuery("?x : ?x is man"));
		System.out.println(validateQuery("?x, ?y : ?x is man & ?x id ?y"));
		System.out.println(validateQuery("?x, ?y : ?x is man"));   //false
	}
	*/
}
