package main

import de.ovgu.cide.fstgen.ast.FSTTerminal

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;


class MergedFile {

	private ArrayList<Conflict> conflicts

	private String path

	private int numberOfConflicts

	private int methodsWithConflicts

	private int conflictsInsideMethods

	private int conflictsOutsideMethods
	
	private int possibleRenamings
	
	private boolean addedByOneDev

	private Map<String,Conflict> mergedFileSummary
	
	private Map<String, Integer> sameSignatureCMSummary
	
	private Map<String, Integer> editSameMCTypeSummary
	
	public MergedFile(String path){
		addedByOneDev = false;
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
		this.createMergedFileSummary()
		this.createSameSignatureCMSummary()
		this.createEditSameMCTypeSummary()
	}
	
	public void createEditSameMCTypeSummary(){
		this.editSameMCTypeSummary = ConflictSummary.initializeEditSameMCTypeSummary()
	}
	
	public void createSameSignatureCMSummary(){
		this.sameSignatureCMSummary = ConflictSummary.initializeSameSignatureCMSummary()
	}
	
	public void createMergedFileSummary(){
		this.mergedFileSummary = ConflictSummary.initializeConflictsSummary()
	}

	public void updateMergedFileSummary(Conflict conflict){
		this.mergedFileSummary = ConflictSummary.updateConflictsSummary(this.mergedFileSummary, conflict)
	}


	public int getNumberOfConflicts(){

		return this.numberOfConflicts;
	}

	public void updateMetrics(Conflict c){
		this.numberOfConflicts = this.numberOfConflicts + c.getNumberOfConflicts()
		if(c.getType().equals(SSMergeConflicts.EditSameMC.toString()) ||
			c.getType().equals(SSMergeConflicts.SameSignatureCM.toString())){
			this.conflictsInsideMethods = this.conflictsInsideMethods +
					c.getNumberOfConflicts()
			if(!c.getType().equals(SSMergeConflicts.SameSignatureCM.toString())){
				this.methodsWithConflicts++
			}
			this.possibleRenamings = this.possibleRenamings + c.getPossibleRenaming()
		}else{
			this.conflictsOutsideMethods++
		}

		this.updateMergedFileSummary(c)
		if(c.getType().equals(SSMergeConflicts.SameSignatureCM.toString())){
			this.updateSameSignatureCMSummary(c.getCauseSameSignatureCM(), c.getDifferentSpacing())
		}
		
		if(c.getType().equals(SSMergeConflicts.EditSameMC.toString())){
			this.updateEditSameMCTypeSummary(c.editSameMCTypeSummary)
		}
		
	}
	
	private void updateSameSignatureCMSummary(String cause, int ds){
		this.sameSignatureCMSummary = ConflictSummary.
		updateSameSignatureCMSummary(this.sameSignatureCMSummary, cause, ds)
	}
	
	private void updateEditSameMCTypeSummary(Map<String, Integer> confSummary){
		this.editSameMCTypeSummary = ConflictSummary.
				updateEditSameMCTypeSummary(this.editSameMCTypeSummary, confSummary)
	}

	public String getPath(){
		return this.path
	}

	public ArrayList<Conflict> getConflicts(){
		return this.conflicts
	}

	public boolean hasConflicts(){
		boolean hasConflicts = false
		if(this.conflicts.size != 0){
			hasConflicts = true
		}
		return hasConflicts
	}

	public int getConflictsInsideMethods(){

		return this.conflictsInsideMethods;
	}

	public int getMethodsWithConflicts(){

		return this.methodsWithConflicts
	}

	public int getConflictsOutsideMethods(){

		return this.conflictsOutsideMethods
	}
	
	public boolean isAddedByOneDev() {
		return addedByOneDev;
	}

	public void setAddedByOneDev(boolean addedByOneDev) {
		this.addedByOneDev = addedByOneDev;
	}

	public String toString(){

		String result = this.path + ', ' + this.getNumberOfConflicts() + ', ' +
				this.getConflictsInsideMethods() + ', '+ this.getMethodsWithConflicts() +
				', ' + this.getConflictsOutsideMethods() + ', ' +
				ConflictSummary.printConflictsSummary(this.mergedFileSummary) + ', ' +
				ConflictSummary.printSameSignatureCMSummary(this.sameSignatureCMSummary) + ', ' +
				this.possibleRenamings + ', ' +
				ConflictSummary.printEditSameMCTypeSummary(this.editSameMCTypeSummary) + '\n'

		return result
	}

}
