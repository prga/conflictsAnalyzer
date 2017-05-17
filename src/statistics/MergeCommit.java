package statistics;

import java.io.File;
import java.util.HashMap;

import main.MergedFile;

public class MergeCommit {
	
	private String name;
	
	private String SHA;
	
	private String parent1;
	
	private String parent2;
	
	private HashMap<MergedFile, Integer> conflictingMergedFiles;
	
	private int totalDevs;
	
	private String NumDevCategory;
	
	public MergeCommit(String sha, String p1, String p2){
		this.SHA = sha;
		this.parent1 = p1;
		this.parent2 = p2;
		this.name = "rev_" + this.parent1.substring(0, 5) + "-"  + this.parent2.substring(0, 5);
	}
	
	public void loadConflictingFiles(String projectPath){
		File files = new File(projectPath + File.separator + 
				"Merge_Scenarios" + File.separator + this.name + ".csv");
		if(files.exists()){
			
		}
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSHA() {
		return SHA;
	}

	public void setSHA(String sHA) {
		SHA = sHA;
	}

	public String getParent1() {
		return parent1;
	}

	public void setParent1(String parent1) {
		this.parent1 = parent1;
	}

	public String getParent2() {
		return parent2;
	}

	public void setParent2(String parent2) {
		this.parent2 = parent2;
	}

	public HashMap<MergedFile, Integer> getConflictingMergedFiles() {
		return conflictingMergedFiles;
	}

	public void setConflictingMergedFiles(HashMap<MergedFile, Integer> conflictingMergedFiles) {
		this.conflictingMergedFiles = conflictingMergedFiles;
	}

	public int getTotalDevs() {
		return totalDevs;
	}

	public void setTotalDevs(int totalDevs) {
		this.totalDevs = totalDevs;
	}

	public String getNumDevCategory() {
		return NumDevCategory;
	}

	public void setNumDevCategory(String numDevCategory) {
		NumDevCategory = numDevCategory;
	}
	
	
	
	
	

}
