package persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ui.Internationalization;
import ui.Internationalization.Commands;
/**
 * This class is used to create the architecture files to save the database.
 */
public class DatabaseSerializer {

	private Database db;
	private final String databasePath = System.getProperty("user.home") + System.getProperty("file.separator") + "KMP_dataSerialized" + System.getProperty("file.separator");
	private final String databaseFileName = "kmp_database.kmp";
	private final String temporaryDataBasePath = databasePath + "tmp" + System.getProperty("file.separator");
	private final String temporaryDirectory = "tmp" + System.getProperty("file.separator");
	private int numberTemporaryDatabase = 0;
	
	public DatabaseSerializer() {
		
	}
	
	public DatabaseSerializer(Database db) {
		this.db = db;
	}
	
	public Database getDb() {
		return db;
	}

	public String getDatabasePath() {
		return databasePath;
	}

	public String getDatabaseFileName() {
		return databaseFileName;
	}

	public String getTemporaryDataBasePath() {
		return temporaryDataBasePath;
	}

	public String getTemporaryDirectory() {
		return temporaryDirectory;
	}

	public int getNumberTemporaryDatabase() {
		return numberTemporaryDatabase;
	}

	/**
	 * Checks if an anterior version of the current version exists
	 * @return the relative path of the file
	 */
	public String getPreviousTemporaryFileName() {
		String path = temporaryDirectory + "tmp" + (numberTemporaryDatabase-2) + ".kmp"; 
		if ((numberTemporaryDatabase < 2) && (!Files.exists(Paths.get(databasePath + path)))) {
			System.out.println("File doesn't exist !");
			return null;
		}
		else return path;
	}
	
	/**
	 * Creates the path of the next temporary file  
	 * @return the relative path of the file
	 */
	public String getTemporaryFileName() {
		return temporaryDirectory + "tmp" + numberTemporaryDatabase + ".kmp";
	}
	
	/**
	 * Checks if a future version of the current version exists
	 * @return the relative path of the file
	 */
	public String getNextTemporaryFileName() {
		String path = temporaryDirectory + "tmp" + numberTemporaryDatabase + ".kmp"; 
		if (!Files.exists(Paths.get(databasePath + path))) {
			System.out.println("File doesn't exist !");
			return null;
		}
		else return path;
	}
	
	public void incrementNumberTemporaryDatabase() {
		numberTemporaryDatabase++;
	}
	
	/**
	 * Checks that the numberTemporaryDatabase is positive and decrement it
	 */
	public void decrementNumberTemporaryDatabase() {
		if (numberTemporaryDatabase < 1) {
			Internationalization i = new Internationalization();
			System.out.println(i.getMessage(Commands.UNDO_EXCEPTION));
		} else numberTemporaryDatabase--;
	}
	
	/**
	 * Builds the architecture of the file system and loads the previous database built by the user if it exists 
	 * @return the database created or found
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Database initCommand() throws ClassNotFoundException, IOException {
		if (!Files.exists(Paths.get(databasePath + databaseFileName))) {
        	Files.createDirectories(Paths.get(databasePath));
        	db.writeObject(databasePath + databaseFileName);
        } else {
        	//Internationalization i = new Internationalization();
        	//System.out.println(i.getMessage(Commands.LOADING_PREVIOUS_STATE));
        	db = db.readObject(db, databasePath + databaseFileName);
        	System.out.println(db.toString());
        }
		Files.createDirectories(Paths.get(temporaryDataBasePath));
    	db.writeObject(databasePath + getTemporaryFileName());
    	incrementNumberTemporaryDatabase();
		return db;
	}

	/**
	 * Creates a new file containing the database serialized 
	 * @param db the current database used by the user
	 * @throws IOException
	 */
	public void insertCommand(Database db) throws IOException {
		this.db = db;
		db.writeObject(databasePath + getTemporaryFileName());
		db.writeObject(databasePath + databaseFileName);
    	incrementNumberTemporaryDatabase();
	}
	
	/**
	 * Creates a new file containing the database serialized 
	 * @param db the current database used by the user
	 * @throws IOException
	 */
	public void inspectRelationsCommand(Database db) throws IOException {
		this.db = db;
		db.writeObject(databasePath + getTemporaryFileName());
		db.writeObject(databasePath + databaseFileName);
    	incrementNumberTemporaryDatabase();
	}
	
	/**
	 * Checks if a file containing the previous database exists and loads it
	 * @return the previous database 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Database undoCommand() throws ClassNotFoundException, IOException {
		String path = getPreviousTemporaryFileName();
		if (path != null) {
			decrementNumberTemporaryDatabase();
			db = db.readObject(db, databasePath + path);
			db.writeObject(databasePath + databaseFileName);
		}
		return db;
	}
	
	/**
	 * Checks if a file containing the future database exists and loads it
	 * @return the future database
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Database redoCommand() throws ClassNotFoundException, IOException {
		String path = getNextTemporaryFileName();
		if (path != null) {
			db = db.readObject(db, databasePath + path);
			db.writeObject(databasePath + databaseFileName);
			incrementNumberTemporaryDatabase();
		}
		return db;
	}
	
	/**
	 * Checks if there is a file containing a database at the path given, loads it and creates a new temporary file
	 * @param path the path of the file containing a database
	 * @return the database at the path given
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Database importCommand(String path) throws ClassNotFoundException, IOException {
		if (!Files.exists(Paths.get(path))) {
			Internationalization i = new Internationalization();
			System.out.println(i.getMessage(Commands.FILE_NOT_FOUND));
		} else {
			db = db.readObject(db, path);
			db.writeObject(databasePath + databaseFileName);
			db.writeObject(databasePath + getTemporaryFileName());
			incrementNumberTemporaryDatabase();
		}
		return db;
	}
	
	/**
	 * Creates folders and a file containing the current database
	 * @param path the path of the file where the database has to be saved
	 * @throws IOException
	 */
	public void exportCommand(String path) throws IOException {
		if (!Files.exists(Paths.get(path))) {
        	Files.createDirectories(Paths.get(path));
        }
    	db.writeObject(path);
    	Internationalization i = new Internationalization();
    	System.out.println(i.getMessage(Commands.EXPORT_SUCCEEDED));
	}

	/**
	 * Changes the current database with the database given in parameter and creates a temporary file
	 * @param db the new database to be used
	 * @throws IOException
	 */
	public void resetCommand(Database db) throws IOException {		
		this.db = db;
		db.writeObject(databasePath + databaseFileName);
		db.writeObject(databasePath + getTemporaryFileName());
		incrementNumberTemporaryDatabase();
	}

	/**
	 * Deletes all the temporary files and the folder
	 */
	public void deleteTemporaryDatabaseDirectory() {
	    File file = new File(temporaryDataBasePath);
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) f.delete();
		}
	    file.delete();
	}
	
	/**
	 * Saves the current database and deletes the temporary files
	 * @throws IOException
	 */
	public void quitCommand() throws IOException {
		deleteTemporaryDatabaseDirectory();
	}
}