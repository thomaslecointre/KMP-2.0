package main;

import java.util.TreeMap;

public class Database {

	private TreeMap<Integer, String[]> table;
	private int primaryIndex = 0;
	
	public Database() {
		table = new TreeMap<Integer, String[]>();
	}
	
	public void insert(String insertion) {
		String[] splittedInsertion = insertion.split(" ");
		table.put(autoIncrementValue(), splittedInsertion);
	}

	private Integer autoIncrementValue() {
		return primaryIndex++;
	}
	
	@Override
	public String toString() {
		
		StringBuilder res = new StringBuilder();
		res.append("\nWhat's in the database?\n");
		for(Integer key : table.keySet()) {
			res.append("\n").append(key).append(" | ");
			for(String string : table.get(key)) {
				res.append(string).append(" | ");
			}
		}
		return res.toString();
	}

	public void query(String query) {
		
	}

	public void removeLastEntry() {
		if(table.containsKey(primaryIndex - 1)) {
			table.remove(primaryIndex - 1);
			primaryIndex--;
		}
	}

	public void reset() {
		table.clear();
		primaryIndex = 0;
	}

}
