package persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseSerializer {

	private Database db;
	private final String databasePath = System.getProperty("user.home") + System.getProperty("file.separator") + "KMP_dataSerialized" + System.getProperty("file.separator");
	private final String databaseFileName = "kmp_database.txt";
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

	public String getPreviousTemporaryFileName() {
		String path = temporaryDirectory + "tmp" + (numberTemporaryDatabase-1) + ".txt"; 
		if ((numberTemporaryDatabase < 1) && (!Files.exists(Paths.get(databasePath + path)))) {
			System.out.println("File doesn't exist !");
			return null;
		}
		else return path;
	}
	
	public String getTemporaryFileName() {
		return temporaryDirectory + "tmp" + numberTemporaryDatabase + ".txt";
	}
	
	public void incrementNumberTemporaryDatabase() {
		numberTemporaryDatabase++;
	}
	
	public void decrementNumberTemporaryDatabase() {
		if (numberTemporaryDatabase < 0) System.out.println("No anterior temporary file !");
		else numberTemporaryDatabase--;
	}
	
	public Database initCommand() throws ClassNotFoundException, IOException {
		if (!Files.exists(Paths.get(databasePath + databaseFileName))) {
        	Files.createDirectories(Paths.get(databasePath));
        	db.writeObject(databasePath + databaseFileName);
        } else {
        	System.out.println("Trying to get previous data");
        	db = db.readObject(db, databasePath + databaseFileName);
        	System.out.println(db.toString());
        }
		Files.createDirectories(Paths.get(temporaryDataBasePath));
    	db.writeObject(databasePath + getTemporaryFileName());
    	incrementNumberTemporaryDatabase();
		return db;
	}

	public void insertCommand(Database db) throws IOException {
		this.db = db;
		db.writeObject(databasePath + getTemporaryFileName());
    	incrementNumberTemporaryDatabase();
	}

	public Database undoCommand() throws ClassNotFoundException, IOException {
		if (Files.exists(Paths.get(databasePath + databaseFileName))) {
			decrementNumberTemporaryDatabase();
			db = db.readObject(db, databasePath + getPreviousTemporaryFileName());
		}
		return db;
	}
	
	/*
	public boolean isNextTemporaryFileName() { 
		return !Files.exists(Paths.get(databasePath + getTemporaryFileName()));		
	}
	 */

	//TODO
	public void redoCommand() {
		
	}

	public void resetCommand(Database db) throws IOException {		
		this.db = db;
		db.writeObject(databasePath + getTemporaryFileName());
		incrementNumberTemporaryDatabase();
	}

	public void deleteTemporaryDatabaseDirectory() {
	    File file = new File(temporaryDataBasePath);
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) f.delete();
		}
	    file.delete();
	}
	
	public void quitCommand() throws IOException {
		db.writeObject(databasePath + databaseFileName);
		deleteTemporaryDatabaseDirectory();
	}
}