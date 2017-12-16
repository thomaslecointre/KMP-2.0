package ui;

import persistence.TransactionHandler;

import java.util.Scanner;

public class Console implements Runnable {

	private Scanner scanner;
	private TransactionHandler transactionHandler;
	private boolean active;

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
				System.out.println(
						"\nEach wildcard token (?X) separated by whitespace is used to identify objects in the database.");
				System.out.println("?X ?Y ?Z identifies all triples in the database.");
				System.out.println("?X greaterThan ?Z identifies all triples that use greaterThan relation.");
				System.out.println(
						"2 ?Y 1 identifies all triples that establish a relation between 2 and 1, such as '2 greaterThan 1'");
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
	private void promptMessage() {
		System.out.print(promptMessage);
	}
	private String illegalCommand = "\nIllegal command!";
	private void illegalCommand() {
		System.out.println(illegalCommand);
	}

	private boolean assignMode(String command) {
		for (Modes mode : Modes.values()) {
			if (command.contains(mode.REPRESENTATION)) {
				if(mode == Modes.UNDO || mode == Modes.RESET) {
					illegalCommand();
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

	private void processMode() {
		String ingestedLine = scanner.nextLine();
		Boolean modeChanged = assignMode(ingestedLine);
		if (!modeChanged) {
			mode = parentMode;
		}
	}

	private static void listOfCommands() {
		System.out.println("Possible commands...\n");
		for (Modes mode : Modes.values()) {
			System.out.println(mode.REPRESENTATION);
		}
		System.out.println("\nUse 'back' to return to the main menu.");
	}

	private String nextCommand(Modes currentMode) {
		parentMode = currentMode;
		boolean modeDetected, back;
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
					}
					command = null;
					modeDetected = true;
					break;
				} 
			}
		} while (!back && modeDetected);
		
		return command;
	}

	private void prompt() {
		System.out.println("Welcome to Knowledge Management Platform");
		System.out.println("----------------------------------------");
		listOfCommands();
		while (active) {
			promptMessage();
			processMode();
			if (mode != null) {
				switch (mode) {
				case INSERT:
					while (mode == Modes.INSERT) {
						String insertion = nextCommand(Modes.INSERT);
						if (insertion != null) {
							transactionHandler.requestInsert(insertion);
						}
					}
					break;
				case QUERY:
					while (mode == Modes.QUERY) {
						String query = nextCommand(Modes.QUERY);
						if (query != null) {
							transactionHandler.requestQuery(query);
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

}
