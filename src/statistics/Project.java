package statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Project {

	private String name;

	private String repo;

	private String resultData;

	private ArrayList<MergeCommit> conflictingMergeCommits;
	
	private String downloadPath;

	public Project(String repo, String resultData, String downloadPath){
		String[] temp = repo.split("/");
		this.repo = temp[0];
		this.name = temp[1];
		this.resultData = resultData;
		this.conflictingMergeCommits = new ArrayList<>();
		this.downloadPath = downloadPath;
	}
	
	public void analyzeMergeCommits(){
		HashMap<String, MergeCommit> mc = this.loadMC();
		this.filterConflictingMC(mc);
		this.cloneProject();
		this.getNumberOfDevs();
	}
	
	public void getNumberOfDevs(){
		for(MergeCommit mc : this.conflictingMergeCommits){
			String clone = this.downloadPath + File.separator + this.name;
			mc.analyzeNumberOfDevelopers(clone);
		}
	}
	
	public void cloneProject(){
		String cmd = "git clone https://github.com/" + this.repo + File.separator + this.name + ".git";
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd, null, new File(this.downloadPath));
			int result = p.waitFor();
			System.out.println("Process result = " + result);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	public HashMap<String, MergeCommit> loadMC(){
		HashMap<String, MergeCommit> result = new HashMap<String, MergeCommit>();
		File merges = new File(this.resultData + File.separator + this.name +
				File.separator + "mergeCommits.csv");
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(merges));
			while ((line = br.readLine()) != null) {
				if(!line.startsWith("Merge")){
					String [] data = line.split(",");
					String sha = data[0];
					String parent1 = data[1];
					String parent2 = data[2];
					MergeCommit mc = new MergeCommit(sha, parent1, parent2);
					result.put(mc.getName(), mc);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void filterConflictingMC(HashMap<String, MergeCommit> mcList){
		File report = new File(this.resultData + File.separator + this.name +
				File.separator + "MergeScenariosReport.csv");
		
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(report));
			while ((line = br.readLine()) != null) {
				if(!line.startsWith("Merge_scenario")){
					String [] data = line.split(",");
					if(!data[6].equals(" 0")){
						String name = data[0];
						MergeCommit mc = mcList.get(name);
						if(mc!=null){
							this.conflictingMergeCommits.add(mc);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public ArrayList<MergeCommit> getConflictingMergeCommits() {
		return conflictingMergeCommits;
	}

	public void setConflictingMergeCommits(ArrayList<MergeCommit> conflictingMergeCommits) {
		this.conflictingMergeCommits = conflictingMergeCommits;
	}

	public String getResultData() {
		return resultData;
	}

	public void setResultData(String resultData) {
		this.resultData = resultData;
	}

}
