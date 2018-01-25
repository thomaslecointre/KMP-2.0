package query;

import java.util.HashMap;
import java.util.HashSet;

import model.Data;
import persistence.Database;

public class Result {

	private HashMap<String, HashSet<Data>> selectorMappings;
	private HashMap<String, Context.DataTypes> selectorTypes;
	private Database database;
	
	protected Result(Database database) {
		this.database = database;
		selectorMappings = new HashMap<>();
		selectorTypes = new HashMap<>();
	}

	protected void putData(String identifier, HashSet<Data> datafield) {
		selectorMappings.put(identifier, datafield);
	}
	
	protected void putDataType(String identifier, Context.DataTypes dataType) {
		selectorTypes.put(identifier, dataType);
	}

	@Override
	public String toString() {
		
		StringBuilder res = new StringBuilder();
		
		for (String identifier : selectorMappings.keySet()) {
			
			res.append("\n\n").append(identifier);
			HashSet<Data> datafield = selectorMappings.get(identifier);
			
			res.append("\n[");
			
			for (Data data : datafield) {
				if (selectorTypes.get(identifier).equals(Context.DataTypes.SUBJECT)) {
					res.append("\n\t").append("Key : ").append(database.findKey(identifier)).append(" => ").append(identifier);
				} else if (selectorTypes.get(identifier).equals(Context.DataTypes.RELATION)) {
					res.append("\n\t").append("Relation : ").append(identifier);
				}
			}
			
			res.append("\n]");
		}
		return res.toString();
	}
}
