package statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MergeCommit {
	
	private String name;
	
	private String SHA;
	
	private String parent1;
	
	private String parent2;
	
	private String base;
	
	
	private String NumDevCategory;
	
	public MergeCommit(String sha, String p1, String p2){
		this.SHA = sha;
		this.parent1 = p1;
		this.parent2 = p2;
		this.name = "rev_" + this.parent1.substring(0, 5) + "-"  + this.parent2.substring(0, 5);
	}
	
	public void analyzeNumberOfDevelopers(String clonePath){
		//git merge-base
		this.setMergeBase(clonePath);
		//get commit authors between base and mergecommit
		
	}
	
	public void setMergeBase(String clonePath){
		String cmd = "git merge-base " + this.parent1 + " " + this.parent2;
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd, null, new File(clonePath));
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				this.base = line;
			}
			p.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getCommitAuthors(String clonePath){
		String cmd = "git merge-base " + this.parent1 + " " + this.parent2;
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd, null, new File(clonePath));
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				
			}
			p.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
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

	public String getNumDevCategory() {
		return NumDevCategory;
	}

	public void setNumDevCategory(String numDevCategory) {
		NumDevCategory = numDevCategory;
	}
	
	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public static void main(String[] args) {
		MergeCommit mc = new MergeCommit("31defd3ef60e09d15faa9ec0e1f8ccbac98e2b07", "4cc6915", "e3439f5");
		mc.setMergeBase("/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/downloads2/andlytics");
		System.out.println(mc.getBase());
	}
	
	
	

}
