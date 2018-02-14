package ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Relation.Properties;
import query.Result;
import query.TransactionHandler;
import ui.Internationalization.Commands;
import ui.Internationalization.Languages;

/**
 * This class is the closest to the user. It interacts with TransactionHandler for requests.
 */
public class Console implements Runnable {

	private Scanner scanner;
	private TransactionHandler transactionHandler = new TransactionHandler();
	private static Internationalization internationalization = new Internationalization();
	private boolean active;

	/**
	 * Enumeration of all types of commands the user can use.
	 */
	private enum Modes {

		INSERT, QUERY, INSPECT_RELATIONS, UNDO, REDO, IMPORT, EXPORT, RESET, SHOW, HELP, LANGUAGE, BACK, QUIT;
		private static Internationalization internationalization = new Internationalization();
		
		String commandName(Modes mode) {
			switch (mode) {
			case INSERT:
				return internationalization.getMessage(Commands.INSERT);
			case INSPECT_RELATIONS:
				return internationalization.getMessage(Commands.INSPECT_RELATIONS);
			case QUERY:
				return internationalization.getMessage(Commands.QUERY);
			case UNDO:
				return internationalization.getMessage(Commands.UNDO);
			case REDO:
				return internationalization.getMessage(Commands.REDO);
			case IMPORT:
				return internationalization.getMessage(Commands.IMPORT);
			case EXPORT:
				return internationalization.getMessage(Commands.EXPORT);
			case RESET:
				return internationalization.getMessage(Commands.RESET);
			case SHOW:
				return internationalization.getMessage(Commands.SHOW);
			case HELP:
				return internationalization.getMessage(Commands.HELP);
			case LANGUAGE:
				return internationalization.getMessage(Commands.LANGUAGE);
			case BACK:
				return internationalization.getMessage(Commands.BACK);
			case QUIT:
				return internationalization.getMessage(Commands.QUIT);
			}
			return null;
		}

		void helpMessage() {
			switch (this) {
			case INSERT:
				System.out.println(internationalization.getMessage(Commands.INSERT_TEXT));
				break;
			case QUERY:
				System.out.println(internationalization.getMessage(Commands.QUERY_TEXT));
				break;
			case INSPECT_RELATIONS:
				System.out.println(internationalization.getMessage(Commands.INSPECT_RELATIONS_TEXT));
				break;
			case HELP:
				// listOfCommands();
				break;
			case BACK:
				System.out.println(internationalization.getMessage(Commands.BACK_TEXT));
				break;
			case UNDO:
				System.out.println(internationalization.getMessage(Commands.UNDO_TEXT));
				break;
			case REDO:
				System.out.println(internationalization.getMessage(Commands.REDO_TEXT));
				break;
			case IMPORT:
				System.out.println(internationalization.getMessage(Commands.IMPORT_TEXT));
				break;
			case EXPORT:
				System.out.println(internationalization.getMessage(Commands.EXPORT_TEXT));
				break;
			case RESET:
				System.out.println(internationalization.getMessage(Commands.RESET_TEXT));
				break;
			case LANGUAGE:
				System.out.println(internationalization.getMessage(Commands.LANGUAGE_TEXT));
				break;
			case QUIT:
				System.out.println(internationalization.getMessage(Commands.QUIT_TEXT));
				break;
			default:
				break;
			}
		}
	}

	private Modes mode;
	private Modes parentMode;
	private String promptMessage = "\n> ";
	
	public Console() {
	}
	
	/**
	 * Sets prompt message.
	 */
	private void setPromptMessage(Modes mode) {
		promptMessage = "\n" + mode.commandName(mode) + " > ";
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

	private String illegalCommand = internationalization.getMessage(Commands.ILLEGAL_COMMAND);

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
			if (command.contains(mode.commandName(mode))) {
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
	
	/**
	 * Changes the language of KMP
	 * @param language country code top-level domains available on : https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains#Country_code_top-level_domains
	 */
	public void requestLanguage(String language) {
		boolean isLanguage = false;
		for (Languages lang : Languages.values()) {
			if (language.contains(lang.getLanguage())) {
				isLanguage = true;
				internationalization.setLanguage(lang);
				try {					
					internationalization.changeLanguage(language);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!isLanguage)
			internationalization.getMessage(Commands.LANGUAGE_INCORRECT);
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
		System.out.println(internationalization.getMessage(Commands.SHOW_TEXT));
		for (Modes mode : Modes.values()) {
			System.out.println(mode.commandName(mode));
		}
		System.out.println(internationalization.getMessage(Commands.BACK_COMMAND));
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
			System.out.println(internationalization.getMessage(Commands.INSPECT_RELATION_INCORRECT_NUMBER_WORDS));
			return false;
		}
		
		//check if the first token is a relation
		if (!isKeyWordRelation(tokens[0])) {
			System.out.println(internationalization.getMessage(Commands.INSPECT_RELATION_INCORRECT_FIRST_TOKEN));
			return false;
		}
		
		//check if the second token is "is" or "not"
		if (!isKeyWordRelationIsNot(tokens[1])) {
			System.out.println(internationalization.getMessage(Commands.INSPECT_RELATION_INCORRECT_SECOND_TOKEN));
			return false;
		}
		
		//check if the third token is a property of a relation
		if (!isKeyWordRelationProperty(tokens[2])) {
			System.out.println(internationalization.getMessage(Commands.INSPECT_RELATION_INCORRECT_THIRD_TOKEN));
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
			System.out.println(internationalization.getMessage(Commands.INSERTION_INCORRECT_NUMBER_WORDS));
			return false;
		}
		
		//check the presence of keywords
		for (String token : tokens) {
			if (isKeyWord(token)) {
				System.out.println(internationalization.getMessage(Commands.INSERTION_INCORRECT_KEYWORD_TOKEN));
				return false;
			}
		}
		
		//check tokens valid a-zA-Z_0-9
		Pattern patternValidTokens = Pattern.compile("\\w+\\s+\\w+\\s+\\w+(\\s+\\w+\\s+\\w+)*");
		Matcher matcherValidTokens = patternValidTokens.matcher(command);
		if (!matcherValidTokens.matches()) {
			System.out.println(internationalization.getMessage(Commands.INSERTION_INCORRECT_COMMAND_STRUCTURE));
			return false;
		}
		
		//check ID : Spiderman ID PeterParker
		Pattern patternPresenceID = Pattern.compile("\\s+(id)\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcherPresenceID = patternPresenceID.matcher(command);
		if (matcherPresenceID.find()) {
			System.out.println(internationalization.getMessage(Commands.INSERTION_INCORRECT_ID));
			return false;
		}
		
		return true;
	}

	/**
	 * Checks if the command is a valid query
	 * @param command a query command
	 * @return if the command is a valid query
	 */
	private boolean validateQuery(String command) {
		//check the global structure
		if (command.split(":").length != 2) {
			System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_STRUCTURE));
			return false;
		}
		String data = command.split(":")[0], where = command.split(":")[1];
		
		//check the data structure
		Pattern patternDataStructure = Pattern.compile("\\?\\w+\\s*(,\\s*\\?\\w+\\s*)*");
		Matcher matcherDataStruture = patternDataStructure.matcher(data);
		if (!matcherDataStruture.matches()) {
			System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_DATA));
			return false;
		}
		
		//check that variables have different names : ?x ?y ?x
		Pattern patternDataVariable = Pattern.compile("\\\\?(\\w+)");
		Matcher matcherDataVariable = patternDataVariable.matcher(data);
		ArrayList<String> variables = new ArrayList<String>();
		while(matcherDataVariable.find()) {
            if (variables.contains(matcherDataVariable.group())) {
        		System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_DATA_NAME) + matcherDataVariable.group());
        		return false;
            }
            else variables.add(matcherDataVariable.group());
        }
		
		//check the where structure
		Pattern patternWhereStructure = Pattern.compile("(\\s+\\??\\w+){3}(\\s+&(\\s+\\??\\w+){3})*");
		Matcher matcherWhereStruture = patternWhereStructure.matcher(where);
		if (!matcherWhereStruture.matches()) {
			System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_WHERE_STRUCTURE));
			return false;
		}
		
		//check ID in where : ?x ID laurent
		Pattern patternWhereID = Pattern.compile("\\s+(id)\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcherWhereID = patternWhereID.matcher(where);
		if (matcherWhereID.find()) {
			System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_ID));
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
			System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_UNUSED_VARIABLE) + variables + " where : " + whereVariables);
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
				System.out.println(internationalization.getMessage(Commands.QUERY_INCORRECT_RELATION_AT_EXTREMITY));
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
	private boolean validateImport(String command) {
		Pattern patternPathStructure = Pattern.compile(".*\\.kmp");
		Matcher matcherPathStruture = patternPathStructure.matcher(command);
		if (!matcherPathStruture.matches()) {
			System.out.println(internationalization.getMessage(Commands.PATH_INCORRECT));
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the command is a valid export
	 * @param command a string written by the user
	 * @return if the command is a valid export
	 */
	private boolean validateExport(String command) {
		Pattern patternPathStructure = Pattern.compile(".*\\.kmp");
		Matcher matcherPathStruture = patternPathStructure.matcher(command);
		if (!matcherPathStruture.matches()) {
			System.out.println(internationalization.getMessage(Commands.PATH_INCORRECT));
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the command is a valid language
	 * @param command a string written by the user
	 * @return a boolean if the command is a valid language
	 */
	private boolean validateLanguage(String command) {
		Languages[] languages = Languages.values();
		StringBuilder s = new StringBuilder(languages[0].getLanguage());
		for (int i = 1; i < languages.length; i++) {
			s.append("|" + languages[i].getLanguage());
		}
		Pattern patternPathStructure = Pattern.compile(s.toString());
		Matcher matcherPathStruture = patternPathStructure.matcher(command);
		if (!matcherPathStruture.matches()) {
			System.out.println(internationalization.getMessage(Commands.LANGUAGE_INCORRECT));
			return false;
		}
		return true;
	}

	/**
	 * Top level user input handling function.
	 */
	private void prompt() {
		System.out.println(internationalization.getMessage(Commands.WELCOME_MESSAGE));
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
				case LANGUAGE:
					setPromptMessage(Modes.LANGUAGE);
					String language = nextCommand(Modes.LANGUAGE);
					if (language != null) {
						requestLanguage(language);
						transactionHandler.requestShow();
					}
					resetPromptMessage();
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
				if (command.contains(mode.commandName(mode))) {
					switch (mode) {
					case INSERT:
					case QUERY:
					case INSPECT_RELATIONS:
					case IMPORT:
					case EXPORT:
					case RESET:
					case LANGUAGE:
						illegalCommand();
						break;
					case SHOW:
						transactionHandler.requestShow();
						break;
					case HELP:
						currentMode.helpMessage();
						break;
					case BACK:
						if (currentMode == Modes.INSERT || currentMode == Modes.QUERY || currentMode == Modes.INSPECT_RELATIONS || currentMode == Modes.IMPORT || currentMode == Modes.EXPORT || currentMode == Modes.LANGUAGE) {
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
				case LANGUAGE:
					if (validateLanguage(command))
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
