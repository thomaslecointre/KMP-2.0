package query;

import java.util.ArrayList;
import java.util.HashMap;

import model.Data;
import persistence.Database;
import ui.Internationalization;
import ui.Internationalization.Commands;
/**
 * This class is the closest to the user. It interacts with TransactionHandler for requests.
 */
public class Result {

	private HashMap<String, ArrayList<Data>> selectorMappings;
	private HashMap<String, Context.DataTypes> selectorTypes;
	private Database database;
	private boolean associatedView;
	
	protected Result(Database database, boolean associatedView) {
		this.database = database;
		this.associatedView = associatedView;
		selectorMappings = new HashMap<>();
		selectorTypes = new HashMap<>();
	}

	/**
	 * Adds an entry to selectorMappings
	 * @param identifier the key of the entry
	 * @param datafield the value of the entry
	 */
	protected void putData(String identifier, ArrayList<Data> datafield) {
		selectorMappings.put(identifier, datafield);
	}
	
	/**
	 * Adds an entry to selectorMappings
	 * @param identifier the key of the entry
	 * @param dataType the value of the entry
	 */
	protected void putDataType(String identifier, Context.DataTypes dataType) {
		selectorTypes.put(identifier, dataType);
	}

	@Override
	public String toString() {
		
		StringBuilder res = new StringBuilder();
		Internationalization i = new Internationalization();
		for (String identifier : selectorMappings.keySet()) {
			
			res.append("\n\n").append(identifier);
			ArrayList<Data> datafield = selectorMappings.get(identifier);
			
			res.append("\n[");
			
			ArrayList<Data> alreadyFound = new ArrayList<>();
			int index = 0;
			for (Data data : datafield) {
				if (selectorTypes.get(identifier).equals(Context.DataTypes.SUBJECT)) {
					if (!alreadyFound.contains(data) && !associatedView) {
						res.append("\n\t").append(i.getMessage(Commands.RESULT_KEY)).append(database.findKey(data.getId())).append(" => ").append(data);
						alreadyFound.add(data);
					} else if (associatedView) {
						res.append("\n\t").append(index).append(") " + i.getMessage(Commands.RESULT_KEY)).append(database.findKey(data.getId())).append(" => ").append(data);
					}
				} else if (selectorTypes.get(identifier).equals(Context.DataTypes.RELATION)) {
					if (!alreadyFound.contains(data) && !associatedView) {
						res.append("\n\t").append(i.getMessage(Commands.RESULT_RELATION)).append(data);
						alreadyFound.add(data);
					} else if (associatedView) {
						res.append("\n\t").append(index).append(") " + i.getMessage(Commands.RESULT_RELATION)).append(data);
					}
				}
				index++;
			}
			
			res.append("\n]");
		}
		return res.toString();
	}

	/**
	 * Gets the size of selectorMapping
	 * @return the size of selectorMapping
	 */
	public int size() {
		return selectorMappings.values().iterator().next().size();
	}
	
	/**
	 * Gets the data linked to the identifier of selectorMapping
	 * @param identifier the key of the entry
	 * @return the data linked to the identifier of selectorMapping
	 */
	public ArrayList<Data> getData(String identifier) {
		if (selectorMappings.get(identifier) == null) {
			return new ArrayList<Data>();
		} else {
			return selectorMappings.get(identifier);
		}
	}
}
