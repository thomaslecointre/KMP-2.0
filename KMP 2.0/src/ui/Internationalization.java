package ui;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

public class Internationalization implements Serializable {
	
	public enum Languages implements Serializable {
		de_DE("de_DE"), en_UK("en_UK"), en_US("en_US"), es_ES("es_ES"), fr_CA("fr_CA"), fr_FR("fr_FR"), it_IT("it_IT"), nz_NZ("nz_NZ"), zh_CN("zh_CN");
		
		private final String LANGUAGE;
		
		private Languages(String language) {
			LANGUAGE = language;
		}

		public String getLanguage() {
			return LANGUAGE;
		}
		
		/**
		 * Generates a String composed of all the languages separated by a space
		 * @return a String composed of all the languages separated by a space
		 */
		public String getAllLanguage_Country() {
			StringBuilder s = new StringBuilder();
			for (Languages language : Languages.values())
				s.append(language);
			return s.toString();
		}
	}
	
	public enum Commands implements Serializable {
		INSERT("INSERT"), QUERY("QUERY"), INSPECT_RELATIONS("INSPECT_RELATIONS"), UNDO("UNDO"), REDO("REDO"), IMPORT("IMPORT"), EXPORT("EXPORT"), RESET("RESET"), SHOW("SHOW"), HELP("HELP"), LANGUAGE("LANGUAGE"), BACK("BACK"), QUIT("QUIT"), INSERT_TEXT("INSERT_TEXT"), QUERY_TEXT("QUERY_TEXT"), INSPECT_RELATIONS_TEXT("INSPECT_RELATIONS_TEXT"), UNDO_TEXT("UNDO_TEXT"), REDO_TEXT("REDO_TEXT"), IMPORT_TEXT("IMPORT_TEXT"), EXPORT_TEXT("EXPORT_TEXT"), RESET_TEXT("RESET_TEXT"), SHOW_TEXT("SHOW_TEXT"), HELP_TEXT("HELP_TEXT"), LANGUAGE_TEXT("LANGUAGE_TEXT"), BACK_TEXT("BACK_TEXT"), QUIT_TEXT("QUIT_TEXT"), WELCOME_MESSAGE("WELCOME_MESSAGE"), BACK_COMMAND("BACK_COMMAND"), REFLEXIVE("REFLEXIVE"), IRREFLEXIVE("IRREFLEXIVE"), SYMMETRIC("SYMMETRIC"), ANTISYMMETRIC("ANTISYMMETRIC"), ASYMMETRIC("ASYMMETRIC"), TRANSITIVE("TRANSITIVE"), IS("IS"), NOT("NOT"), SUBJECT("SUBJECT"), RELATION("RELATION"), DATABASE_QUESTION("DATABASE_QUESTION"), DATABASE_EMPTY("DATABASE_EMPTY"), DATABASE_SERIALIZED("DATABASE_SERIALIZED"), DATABASE_READ("DATABASE_READ"), FILE_NOT_FOUND("FILE_NOT_FOUND"), UNDO_EXCEPTION("UNDO_EXCEPTION"), EXPORT_SUCCEEDED("EXPORT_SUCCEEDED"), ILLEGAL_COMMAND("ILLEGAL_COMMAND"), INSPECT_RELATION_INCORRECT_NUMBER_WORDS("INSPECT_RELATION_INCORRECT_NUMBER_WORDS"), INSPECT_RELATION_INCORRECT_FIRST_TOKEN("INSPECT_RELATION_INCORRECT_FIRST_TOKEN"), INSPECT_RELATION_INCORRECT_SECOND_TOKEN("INSPECT_RELATION_INCORRECT_SECOND_TOKEN"), INSPECT_RELATION_INCORRECT_THIRD_TOKEN("INSPECT_RELATION_INCORRECT_THIRD_TOKEN"), INSERTION_INCORRECT_NUMBER_WORDS("INSERTION_INCORRECT_NUMBER_WORDS"), INSERTION_INCORRECT_KEYWORD_TOKEN("INSERTION_INCORRECT_KEYWORD_TOKEN"), INSERTION_INCORRECT_COMMAND_STRUCTURE("INSERTION_INCORRECT_COMMAND_STRUCTURE"), INSERTION_INCORRECT_ID("INSERTION_INCORRECT_ID"), QUERY_INCORRECT_STRUCTURE("QUERY_INCORRECT_STRUCTURE"), QUERY_INCORRECT_DATA("QUERY_INCORRECT_DATA"), QUERY_INCORRECT_DATA_NAME("QUERY_INCORRECT_DATA_NAME"), QUERY_INCORRECT_WHERE_STRUCTURE("QUERY_INCORRECT_WHERE_STRUCTURE"), QUERY_INCORRECT_ID("QUERY_INCORRECT_ID"), QUERY_INCORRECT_UNUSED_VARIABLE("QUERY_INCORRECT_UNUSED_VARIABLE"), QUERY_INCORRECT_RELATION_AT_EXTREMITY("QUERY_INCORRECT_RELATION_AT_EXTREMITY"), PATH_INCORRECT("PATH_INCORRECT"), LANGUAGE_INCORRECT("LANGUAGE_INCORRECT");
		
		private final String COMMAND;
		
		private Commands(String command) {
			COMMAND = command;
		}

		public String getCommand() {
			return COMMAND;
		}

		/**
		 * Generates a String composed of all the commands separated by a space
		 * @return a String composed of all the commands separated by a space
		 */
		public String getAllCommands() {
			StringBuilder s = new StringBuilder();
			for (Commands command : Commands.values())
				s.append(command);
			return s.toString();
		}
	}
	
	private Languages language = Languages.en_US;
	private final String languageFileName = "MessagesBundle";
	private final String directoryPath = System.getProperty("user.home") + System.getProperty("file.separator") + "KMP_dataSerialized";
	private final String path = System.getProperty("user.home") + System.getProperty("file.separator") + "KMP_dataSerialized" + System.getProperty("file.separator") + "config.kmpc";
		
	public Internationalization() {
		try {
			initCommand();
			//this.language = readLanguage();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\nrien à battre\n");
		}
	}
	
	public Internationalization(Languages language) {
		try {
			initCommand();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.language = language;
	}
	
	/**
	 * Builds the architecture of the file system and loads the previous config file built by the user if it exists 
	 * @return the database created or found
	 * @throws Exception 
	 */
	public void initCommand() throws Exception {
		if (!Files.exists(Paths.get(path))) {
        	Files.createDirectories(Paths.get(directoryPath));
        	setLanguage(Languages.en_US);
        	changeLanguage(getLanguage().getLanguage());
        } else {
        	setLanguage(readLanguage());
        }
	}

	public Languages getLanguage() {
		return language;
	}

	public void setLanguage(Languages language) {
		this.language = language;
	}

	public String getLanguageFileName() {
		return languageFileName;
	}

	public String getPath() {
		return path;
	}

	/**
	 * Generates the RessourceBundle from the languages available
	 * @return the RessourceBundle linked to the current language
	 */
	public ResourceBundle getRessourceBundle() {
		switch (language.getLanguage()) {
		case "de_DE":
			return ResourceBundle.getBundle("MessagesBundle", Locale.GERMANY);
		case "en_UK":
			return ResourceBundle.getBundle("MessagesBundle", Locale.UK);
		case "en_US":
			return ResourceBundle.getBundle("MessagesBundle", Locale.US);
		case "es_ES":
			return ResourceBundle.getBundle("MessagesBundle", new Locale("es", "ES"));
		case "fr_CA":
			return ResourceBundle.getBundle("MessagesBundle", Locale.CANADA_FRENCH);
		case "fr_FR":
			return ResourceBundle.getBundle("MessagesBundle", Locale.FRANCE);
		case "it_IT":
			return ResourceBundle.getBundle("MessagesBundle", Locale.ITALY);
		case "nz_NZ":
			return ResourceBundle.getBundle("MessagesBundle", new Locale("nz", "NZ"));
		case "zh_CN":
			return ResourceBundle.getBundle("MessagesBundle", Locale.CHINA);
		default:
			return ResourceBundle.getBundle("MessagesBundle", Locale.US);
		}
	}

	/**
	 * Gets the message according to the command and the language from the config file
	 * @param command a key corresponding to a command
	 * @return the message corresponding to the command in the language of the config file
	 */
	public String getMessage(Commands command) {
		return getRessourceBundle().getString(command.COMMAND);
	}
	
	/**
	 * read the language in the config file
	 * @return the language read in the config file
	 * @throws Exception
	 */
	public Languages readLanguage() throws Exception {
		if (Files.exists(Paths.get(path))) {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.language = (Languages) in.readObject();
			in.close();
			fileIn.close();
		}
		return language;
	}
	
	/**
	 * Modifies the config file, replacing the previous language by the new one
	 * @param language the new language for the config file
	 * @throws IOException
	 */
	public void changeLanguage(String language) throws IOException {
		if (Files.exists(Paths.get(path))) {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} else {
			Files.createDirectories(Paths.get(directoryPath));
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		}
	}
}