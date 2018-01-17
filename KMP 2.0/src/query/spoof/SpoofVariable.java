package query.spoof;

import java.util.ArrayList;
import java.util.HashSet;

import persistence.Database;
import query.TransactionHandler;

public class SpoofVariable {
	private HashSet<SpoofData> spoofDataSet;
	private boolean right = false;
	private boolean idsTransferred = false;
	private TransactionHandler transactionHandler;
	
	public SpoofVariable() {
		spoofDataSet = new HashSet();
	}
	
	public SpoofVariable(TransactionHandler transactionHandler) {
		this();
		this.transactionHandler = transactionHandler;
	}
	
	public void transferIDs() {
		idsTransferred = true;
		HashSet<SpoofData> evolvingSpoofResultSet = (HashSet<SpoofData>) spoofDataSet.clone();
		for(SpoofData spoofData : spoofDataSet) {
			evolvingSpoofResultSet.remove(spoofData);
			SpoofSubject spoofSubject = (SpoofSubject) spoofData;
			ArrayList<SpoofSubject> newSpoofResults = spoofSubject.transferID(transactionHandler);
			for (SpoofSubject newSpoofResult : newSpoofResults) {
				evolvingSpoofResultSet.add(newSpoofResult);
			}
		}
		spoofDataSet = evolvingSpoofResultSet;
	}

	public HashSet<SpoofData> getSpoofDataSet() {
		return spoofDataSet;
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
