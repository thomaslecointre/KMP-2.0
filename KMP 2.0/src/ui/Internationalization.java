package ui;
import java.util.Locale;
import java.util.ResourceBundle;

public class Internationalization {

    public static void main(String[] args) {
    	ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", Locale.US);
        ResourceBundle bundleDE = ResourceBundle.getBundle("MessagesBundle", Locale.GERMANY);
        ResourceBundle bundleUK = ResourceBundle.getBundle("MessagesBundle", Locale.US);
        ResourceBundle bundleUS = ResourceBundle.getBundle("MessagesBundle", Locale.UK);
        ResourceBundle bundleES = ResourceBundle.getBundle("MessagesBundle", new Locale("es", "ES"));
        ResourceBundle bundleCA = ResourceBundle.getBundle("MessagesBundle", Locale.CANADA_FRENCH);
        ResourceBundle bundleFR = ResourceBundle.getBundle("MessagesBundle", Locale.FRANCE);
        ResourceBundle bundleIT = ResourceBundle.getBundle("MessagesBundle", Locale.ITALY);
        ResourceBundle bundleNZ = ResourceBundle.getBundle("MessagesBundle", new Locale("nz", "NZ"));
        ResourceBundle bundleCN = ResourceBundle.getBundle("MessagesBundle", Locale.CHINA);
        
        printMessages(bundle);
        printMessages(bundleFR);
        printMessages(bundleNZ);
    }

    private static void printMessages(ResourceBundle bundle) {
        System.out.println(bundle.getString("INSERT") + "\n");
        System.out.println(bundle.getString("INSERT_TEXT"));
    }

    public String getAllLanguage_Country() {
    	return "de_DE en_US en_US es_ES fr_CA fr_FR it_IT nz_NZ zh_CN";
    }
    
    public String getAllKeyWords() {
    	return "INSERT QUERY INSPECT_RELATIONS UNDO REDO IMPORT EXPORT RESET SHOW HELP LANGUAGE BACK QUIT "
    			+ "INSERT_TEXT INSPECT_RELATIONS_TEXT UNDO_TEXT REDO_TEXT IMPORT_TEXT EXPORT_TEXT RESET_TEXT SHOW_TEXT HELP_TEXT LANGUAGE_TEXT BACK_TEXT QUIT_TEXT "
    			+ "WELCOME_MESSAGE BACK_COMMAND "
    			+ "REFLEXIVE IRREFLEXIVE SYMMETRIC ANTISYMMETRIC ASYMMETRIC TRANSITIVE "
    			+ "IS NOT "
    			+ "SUBJECT RELATION "
    			+ "DATABASE_QUESTION DATABASE_EMPTY DATABASE_SERIALIZED DATABASE_READ "
    			+ "FILE_NOT_FOUND UNDO_EXCEPTION EXPORT_SUCCEEDED ILLEGAL_COMMAND "
    			+ "INSPECT_RELATION_INCORRECT_NUMBER_WORDS INSPECT_RELATION_INCORRECT_FIRST_TOKEN INSPECT_RELATION_INCORRECT_SECOND_TOKEN INSPECT_RELATION_INCORRECT_THIRD_TOKEN "
    			+ "INSERTION_INCORRECT_NUMBER_WORDS INSERTION_INCORRECT_KEYWORD_TOKEN INSERTION_INCORRECT_COMMAND_STRUCTURE INSERTION_INCORRECT_ID"
    			+ "QUERY_INCORRECT_STRUCTURE QUERY_INCORRECT_DATA QUERY_INCORRECT_DATA_NAME QUERY_INCORRECT_WHERE_STRUCTURE QUERY_INCORRECT_ID QUERY_INCORRECT_UNUSED_VARIABLE QUERY_INCORRECT_RELATION_AT_EXTREMITY "
    			+ "PATH_INCORRECT LANGUAGE_INCORRECT";
    }
}