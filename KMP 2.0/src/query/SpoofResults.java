package query;

import java.util.ArrayList;
import java.util.HashSet;

import persistence.Database;

public class SpoofResults {
	private HashSet<SpoofResult> spoofResultSet;
	private boolean right = false;
	private boolean idsTransferred = false;
	private TransactionHandler transactionHandler;
	
	public SpoofResults(TransactionHandler transactionHandler) {
		this.transactionHandler = transactionHandler;
		spoofResultSet = new HashSet();
	}
	
	public void transferIDs() {
		idsTransferred = true;
		HashSet<SpoofResult> evolvingSpoofResultSet = (HashSet<SpoofResult>) spoofResultSet.clone();
		for(SpoofResult spoofResult : spoofResultSet) {
			evolvingSpoofResultSet.remove(spoofResult);
			ArrayList<SpoofResult> newSpoofResults = spoofResult.transferID(transactionHandler);
			for (SpoofResult newSpoofResult : newSpoofResults) {
				evolvingSpoofResultSet.add(newSpoofResult);
			}
		}
		spoofResultSet = evolvingSpoofResultSet;
	}

	public HashSet<SpoofResult> getSpoofResultSet() {
		return spoofResultSet;
	}

	public boolean usedOnTheRight() {
		return right;
	}

	public void isUsedOnTheRight() {
		right = true;
	}

	public boolean isIdsTransferred() {
		return idsTransferred;
	}
}
