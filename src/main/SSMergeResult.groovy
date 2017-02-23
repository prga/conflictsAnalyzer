package main

import java.util.ArrayList;
import java.util.Map;

class SSMergeResult {
	
	private String mergeScenarioName
	
	private boolean hasConflicts
	
	private Map<String, ArrayList<EditSameMC>> filesWithMethodsToJoana
	
	private boolean hasPredictors
	

	public SSMergeResult(String mScenarioName, boolean hc, Map<String, ArrayList<EditSameMC>> fwmtj, boolean hasPredictors){

		this.mergeScenarioName = mScenarioName
		this.hasConflicts = hc
		this.filesWithMethodsToJoana = fwmtj
		this.hasPredictors = hasPredictors
		
	}
	
	
	
	public boolean getHasPredictors() {
		return hasPredictors;
	}



	public void setHasPredictors(boolean hasPredictors) {
		this.hasPredictors = hasPredictors;
	}



	public boolean getHasConflicts() {
		return hasConflicts;
	}
	public void setHasConflicts(boolean hasConflicts) {
		this.hasConflicts = hasConflicts;
	}
	public Map<String, ArrayList<EditSameMC>> getFilesWithMethodsToJoana() {
		return filesWithMethodsToJoana;
	}
	public void setFilesWithMethodsToJoana(Map<String, ArrayList<EditSameMC>> filesWithMethodsToJoana) {
		this.filesWithMethodsToJoana = filesWithMethodsToJoana;
	}

}
