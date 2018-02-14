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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Languages implements Serializable {
		de_DE, en_UK, en_US, es_ES, fr_CA, fr_FR, it_IT, nz_NZ, zh_CN;
		
		/**
		 * Generates the string corresponding to the language
		 * @return the string corresponding to the language
		 */
		public String getLanguage() {
			switch (this) {
			case de_DE:
				return "de_DE";
			case en_UK:
				return "en_UK";
			case en_US:
				return "en_US";
			case es_ES:
				return "es_ES";
			case fr_CA:
				return "fr_CA";
			case fr_FR:
				return "fr_FR";
			case it_IT:
				return "it_IT";
			case nz_NZ:
				return "nz_NZ";
			case zh_CN:
				return "zh_CN";
			default:
				return "en_US";
			}
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

		public Languages findLanguage(String lang) {
			switch (lang) {
            case "de_DE":
				return de_DE;
			case "en_UK":
				return en_UK;
			case "en_US":
				return en_US;
			case "es_ES":
				return es_ES;
			case "fr_CA":
				return fr_CA;
			case "fr_FR":
				return fr_FR;
			case "it_IT":
				return it_IT;
			case "nz_NZ":
				return nz_NZ;
			case "zh_CN":
				return zh_CN;
			default:
				return en_US;
			}
		}
	}
	
	public enum Commands implements Serializable {
		INSERT, QUERY, INSPECT_RELATIONS, UNDO, REDO, IMPORT, EXPORT, RESET, SHOW, HELP, LANGUAGE, BACK, QUIT, INSERT_TEXT, QUERY_TEXT, INSPECT_RELATIONS_TEXT, UNDO_TEXT, REDO_TEXT, IMPORT_TEXT, EXPORT_TEXT, 
		RESET_TEXT, SHOW_TEXT, HELP_TEXT, LANGUAGE_TEXT, BACK_TEXT, QUIT_TEXT, WELCOME_MESSAGE, BACK_COMMAND, REFLEXIVE, IRREFLEXIVE, SYMMETRIC, ANTISYMMETRIC, ASYMMETRIC, TRANSITIVE, IS, NOT, SUBJECT, RELATION, 
		DATABASE_QUESTION, DATABASE_EMPTY, DATABASE_INDEX, DATABASE_ID, DATABASE_SERIALIZED, DATABASE_READ, FILE_NOT_FOUND, UNDO_EXCEPTION, EXPORT_SUCCEEDED, ILLEGAL_COMMAND, INSPECT_RELATION_INCORRECT_NUMBER_WORDS, INSPECT_RELATION_INCORRECT_FIRST_TOKEN, 
		INSPECT_RELATION_INCORRECT_SECOND_TOKEN, INSPECT_RELATION_INCORRECT_THIRD_TOKEN, INSERTION_INCORRECT_NUMBER_WORDS, INSERTION_INCORRECT_KEYWORD_TOKEN, INSERTION_INCORRECT_COMMAND_STRUCTURE, INSERTION_INCORRECT_ID, 
		QUERY_INCORRECT_STRUCTURE, QUERY_INCORRECT_DATA, QUERY_INCORRECT_DATA_NAME, QUERY_INCORRECT_WHERE_STRUCTURE, QUERY_INCORRECT_ID, QUERY_INCORRECT_UNUSED_VARIABLE, QUERY_INCORRECT_RELATION_AT_EXTREMITY, 
		LOADING_PREVIOUS_STATE, ENTRYDATA_ID, RESULT_KEY, RESULT_RELATION, PATH_INCORRECT, LANGUAGE_INCORRECT, TRANSACTION_HANDLER_RELATIONS, TRANSACTION_HANDLER_PROPERTIES, LANGUAGE_CHOOSE;
		
		/**
		 * Generates the string corresponding to the command
		 * @return the string corresponding to the command
		 */
		public String getCommand() {
			switch (this) {
			case INSERT:
				return "INSERT";
			case QUERY:
				return "QUERY";
			case INSPECT_RELATIONS:
				return "INSPECT_RELATIONS";
			case UNDO:
				return "UNDO";
			case REDO:
				return "REDO";
			case IMPORT:
				return "IMPORT";
			case EXPORT:
				return "EXPORT";
			case RESET:
				return "RESET";
			case SHOW:
				return "SHOW";
			case HELP:
				return "HELP";
			case LANGUAGE:
				return "LANGUAGE";
			case BACK:
				return "BACK";
			case QUIT:
				return "QUIT";
			case INSERT_TEXT:
				return "INSERT_TEXT";
			case QUERY_TEXT:
				return "QUERY_TEXT";
			case INSPECT_RELATIONS_TEXT:
				return "INSPECT_RELATIONS_TEXT";
			case UNDO_TEXT:
				return "UNDO_TEXT";
			case REDO_TEXT:
				return "REDO_TEXT";
			case IMPORT_TEXT:
				return "IMPORT_TEXT";
			case EXPORT_TEXT:
				return "EXPORT_TEXT";
			case RESET_TEXT:
				return "RESET_TEXT";
			case SHOW_TEXT:
				return "SHOW_TEXT";
			case HELP_TEXT:
				return "HELP_TEXT";
			case LANGUAGE_TEXT:
				return "LANGUAGE_TEXT";
			case BACK_TEXT:
				return "BACK_TEXT";
			case QUIT_TEXT:
				return "QUIT_TEXT";
			case WELCOME_MESSAGE:
				return "WELCOME_MESSAGE";
			case BACK_COMMAND:
				return "BACK_COMMAND";
			case REFLEXIVE:
				return "REFLEXIVE";
			case IRREFLEXIVE:
				return "IRREFLEXIVE";
			case SYMMETRIC:
				return "SYMMETRIC";
			case ANTISYMMETRIC:
				return "ANTISYMMETRIC";
			case ASYMMETRIC:
				return "ASYMMETRIC";
			case TRANSITIVE:
				return "TRANSITIVE";
			case IS:
				return "IS";
			case NOT:
				return "NOT";
			case SUBJECT:
				return "SUBJECT";
			case RELATION:
				return "RELATION";
			case DATABASE_QUESTION:
				return "DATABASE_QUESTION";
			case DATABASE_EMPTY:
				return "DATABASE_EMPTY";
			case DATABASE_INDEX:
				return "DATABASE_INDEX";
			case DATABASE_ID:
				return "DATABASE_ID";
			case DATABASE_SERIALIZED:
				return "DATABASE_SERIALIZED";
			case DATABASE_READ:
				return "DATABASE_READ";
			case FILE_NOT_FOUND:
				return "FILE_NOT_FOUND";
			case UNDO_EXCEPTION:
				return "UNDO_EXCEPTION";
			case EXPORT_SUCCEEDED:
				return "EXPORT_SUCCEEDED";
			case ILLEGAL_COMMAND:
				return "ILLEGAL_COMMAND";
			case INSPECT_RELATION_INCORRECT_NUMBER_WORDS:
				return "INSPECT_RELATION_INCORRECT_NUMBER_WORDS";
			case INSPECT_RELATION_INCORRECT_FIRST_TOKEN:
				return "INSPECT_RELATION_INCORRECT_FIRST_TOKEN";
			case INSPECT_RELATION_INCORRECT_SECOND_TOKEN:
				return "INSPECT_RELATION_INCORRECT_SECOND_TOKEN";
			case INSPECT_RELATION_INCORRECT_THIRD_TOKEN:
				return "INSPECT_RELATION_INCORRECT_THIRD_TOKEN";
			case INSERTION_INCORRECT_NUMBER_WORDS:
				return "INSERTION_INCORRECT_NUMBER_WORDS";
			case INSERTION_INCORRECT_KEYWORD_TOKEN:
				return "INSERTION_INCORRECT_KEYWORD_TOKEN";
			case INSERTION_INCORRECT_COMMAND_STRUCTURE:
				return "INSERTION_INCORRECT_COMMAND_STRUCTURE";
			case INSERTION_INCORRECT_ID:
				return "INSERTION_INCORRECT_ID";
			case QUERY_INCORRECT_STRUCTURE:
				return "QUERY_INCORRECT_STRUCTURE";
			case QUERY_INCORRECT_DATA:
				return "QUERY_INCORRECT_DATA";
			case QUERY_INCORRECT_DATA_NAME:
				return "QUERY_INCORRECT_DATA_NAME";
			case QUERY_INCORRECT_WHERE_STRUCTURE:
				return "QUERY_INCORRECT_WHERE_STRUCTURE";
			case QUERY_INCORRECT_ID:
				return "QUERY_INCORRECT_ID";
			case QUERY_INCORRECT_UNUSED_VARIABLE:
				return "QUERY_INCORRECT_UNUSED_VARIABLE";
			case QUERY_INCORRECT_RELATION_AT_EXTREMITY:
				return "QUERY_INCORRECT_RELATION_AT_EXTREMITY";
			case ENTRYDATA_ID:
				return "ENTRYDATA_ID";
			case LOADING_PREVIOUS_STATE:
				return "LOADING_PREVIOUS_STATE";
			case RESULT_KEY:
				return "RESULT_KEY";
			case RESULT_RELATION:
				return "RESULT_RELATION";
			case PATH_INCORRECT:
				return "PATH_INCORRECT";
			case LANGUAGE_INCORRECT:
				return "LANGUAGE_INCORRECT";
			case TRANSACTION_HANDLER_RELATIONS:
				return "TRANSACTION_HANDLER_RELATIONS";
			case TRANSACTION_HANDLER_PROPERTIES:
				return "TRANSACTION_HANDLER_PROPERTIES";
			case LANGUAGE_CHOOSE:
				return "LANGUAGE_CHOOSE";
			default:
				return null;
			}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public void setLanguage(Languages language) {
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
			createConfigFile(language.getLanguage());
		} else {
			setLanguage(readLanguage());
		}
	}
	
	/**
	 * Creates the config file with the language by default
	 * @param language the language by default
	 * @throws IOException
	 */
	public void createConfigFile(String language) throws IOException {
		Files.createDirectories(Paths.get(directoryPath));
		FileOutputStream fileOut = new FileOutputStream(path);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public String getAllLanguages() {
		return language.getAllLanguage_Country();
	}
	
	/**
	 * Generates the RessourceBundle from the languages available
	 * @return the RessourceBundle linked to the current language
	 */
	public ResourceBundle getRessourceBundle() {
		switch (language.getLanguage()) {
		case "de_DE":
			return ResourceBundle.getBundle(languageFileName, Locale.GERMANY);
		case "en_UK":
			return ResourceBundle.getBundle(languageFileName, Locale.UK);
		case "en_US":
			return ResourceBundle.getBundle(languageFileName, Locale.US);
		case "es_ES":
			return ResourceBundle.getBundle(languageFileName, new Locale("es", "ES"));
		case "fr_CA":
			return ResourceBundle.getBundle(languageFileName, Locale.CANADA_FRENCH);
		case "fr_FR":
			return ResourceBundle.getBundle(languageFileName, Locale.FRANCE);
		case "it_IT":
			return ResourceBundle.getBundle(languageFileName, Locale.ITALY);
		case "nz_NZ":
			return ResourceBundle.getBundle(languageFileName, new Locale("nz", "NZ"));
		case "zh_CN":
			return ResourceBundle.getBundle(languageFileName, Locale.CHINA);
		default:
			return ResourceBundle.getBundle(languageFileName, Locale.US);
		}
	}
	
	/**
	 * Gets the message according to the command and the language from the config file
	 * @param command a key corresponding to a command
	 * @return the message corresponding to the command in the language of the config file
	 */
	public String getMessage(Commands command) {
		return getRessourceBundle().getString(command.getCommand());
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
			Internationalization internationalization = (Internationalization) in.readObject();
			in.close();
			fileIn.close();
			return internationalization.language;
		}
		return null;
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
		} else
			System.out.println("Can't change language because file doesn't exist !");
	}
}