package main;

import java.util.Scanner;

public class Console implements Runnable {

	private Scanner scanner;
	private Database database;

	private boolean active;

	private enum Modes {
		INSERT("insert"), QUERY("query"), HELP("help"), BACK("back"), UNDO("undo"), RESET("reset"), SHOW("show"), QUIT(
				"quit");

		private final String REPRESENTATION;

		Modes(String representation) {
			REPRESENTATION = representation;
		}

		void helpMessage() {
			switch (this) {
			case INSERT:
				System.out.println(
						"\nEach word seperated by whitespace is considered as a seperate object in the database.");
				System.out.println("Subjects are capitalized, relations aren't.");
				break;
			case QUERY:
				System.out.println(
						"\nEach wildcard token (?X) seperated by whitespace is used to identify objects in the database.");
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

	private void promptSymbol() {
		System.out.print("\n> ");
	}

	private boolean assignMode(String command) {
		for (Modes mode : Modes.values()) {
			if (command.contains(mode.REPRESENTATION)) {
				this.mode = mode;
				mode.helpMessage();
				return true;
			}
		}
		return false;
	}

	public Console() {
		database = new Database();
	}

	@Override
	public void run() {
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

	private String nextLine(Modes currentMode) {
		parentMode = currentMode;
		boolean illegal, back;
		String command;
		do {
			promptSymbol();
			command = scanner.nextLine();
			illegal = true;
			back = true;
			for (Modes mode : Modes.values()) {
				if (command.contains(mode.REPRESENTATION)) {
					switch (mode) {
					case INSERT:
					case QUERY:
					case SHOW:
						System.out.println("\nIllegal command!");
						illegal = true;
						break;
					case HELP:
						currentMode.helpMessage();
						break;
					case BACK:
						mode = Modes.BACK;
						back = true;
						command = null;
						break;
					case UNDO:
						if(currentMode == Modes.INSERT) {
							database.removeLastEntry();
							System.out.println(database);
						}
						if (currentMode == Modes.QUERY) {
							System.out.println("\nIllegal command!");
							illegal = true;
						}
						break;
					case RESET:
						if (currentMode == Modes.QUERY) {
							System.out.println("\nIllegal command!");
							promptSymbol();
							command = scanner.nextLine();
							illegal = true;
						}
						if(currentMode == Modes.INSERT) {
							database.reset();
							System.out.println(database);
						}
						mode.helpMessage();
						break;
					}
					break;
				}
			}
		} while (illegal && !back && command == null);
		return command;
	}

	private void prompt() {
		System.out.println("Welcome to Knowledge Management Platform");
		System.out.println("----------------------------------------");
		listOfCommands();
		while (active) {
			promptSymbol();
			processMode();
			if (mode != null) {
				switch (mode) {
				case INSERT:
					while(mode == Modes.INSERT) {
						String insertion = nextLine(Modes.INSERT);
						if (insertion != null) {
							database.insert(insertion);
							System.out.println(database);
						}
					}
					break;
				case QUERY:
					while(mode == Modes.QUERY) {
						String query = nextLine(Modes.QUERY);
						if (query != null)
							database.query(query);
					}
					break;
				case SHOW:
					System.out.println(database);
					break;
				case QUIT:
					active = false;
				}
			} else {
				System.out.println("\nIllegal command!");
			}

		}
	}

}
