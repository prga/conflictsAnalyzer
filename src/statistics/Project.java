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
	
	private HashMap<String, int[]> authorsSummary;

	public Project(String repo, String resultData, String downloadPath){
		String[] temp = repo.split("/");
		this.repo = temp[0];
		this.name = temp[1];
		this.resultData = resultData;
		this.conflictingMergeCommits = new ArrayList<>();
		this.downloadPath = downloadPath;
		this.initializeAuthorSummary();
	}
	
	public void initializeAuthorSummary(){
		this.authorsSummary = new HashMap<String, int[]>();
		for(NumDevCategories c : NumDevCategories.values()){
			String cat = c.toString();
			int[] numbers = new int[4];
			this.authorsSummary.put(cat, numbers);
		}
	}
	
	public void updateAuthorSummary(String category, int[] mcConflicts){
		int[] values = this.authorsSummary.get(category);
		for(int i = 0; i < values.length; i++){
			values[i] = values[i] + mcConflicts[i];
		}
		this.authorsSummary.put(category, values);
	}
	
	public String toString(){
		String result = this.name;
		for(NumDevCategories c : NumDevCategories.values()){
			String cat = c.toString();
			int[] values = this.authorsSummary.get(cat);
			for(int i = 0; i < values.length; i++){
				result = result + ";" + values[i];
			}
			
		}
		return result;
	}
	
	public void analyzeMergeCommits(){
		System.out.println("Loading merge commit data");
		HashMap<String, ArrayList<MergeCommit>> mc = this.loadMC();
		this.filterConflictingMC(mc);
		System.out.println("Merge commit data loaded");
		if(!cloneExists()){
			System.out.println("Cloning project " + this.name);
			this.cloneProject();
			System.out.println("Project cloned successfully");
		}
		System.out.println("Starting to get the number of developers");
		this.getNumberOfDevs();
		System.out.println("Finished to analyze the number of developers");
	}
	
	public void getNumberOfDevs(){
		for(MergeCommit mc : this.conflictingMergeCommits){
			System.out.println("Analyzing merge " + mc.getName());
			String clone = this.downloadPath + File.separator + this.name;
			mc.analyzeNumberOfDevelopers(clone);
			DevNumberPrinter.printMergeCommitReport(this.name, mc.toString());
			this.updateAuthorSummary(mc.getNumDevCategory(), mc.getConfSummary());
		}
	}
	
	public boolean cloneExists(){
		boolean cloneExists = false;
		File file = new File(this.downloadPath + File.separator + this.name);
		if(file.exists()){
			cloneExists = true;
		}
		return cloneExists;
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

	public HashMap<String, ArrayList<MergeCommit>> loadMC(){
		HashMap<String, ArrayList<MergeCommit>> result = new HashMap<String, ArrayList<MergeCommit>>();
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
					String rev_name = mc.getName();
					ArrayList<MergeCommit> mcs = null;
					if(result.containsKey(rev_name)){
						mcs = result.get(rev_name);
					}else{
						mcs = new ArrayList<MergeCommit>();
					}
					mcs.add(mc);
					result.put(rev_name, mcs);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void filterConflictingMC(HashMap<String, ArrayList<MergeCommit>> mcList){
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
						ArrayList<MergeCommit> mcs = mcList.get(name);
						if(mcs!=null){
							MergeCommit mc = mcs.get(0);
							mc.computeConfSummary(data);
							this.conflictingMergeCommits.add(mc);
							mcs.remove(0);
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

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public HashMap<String, int[]> getAuthorsSummary() {
		return authorsSummary;
	}

	public void setAuthorsSummary(HashMap<String, int[]> authorsSummary) {
		this.authorsSummary = authorsSummary;
	}
	
	public static void main(String[] args) {
		Project p = new Project("AndlyticsProject/andlytics", 
				"/Users/paolaaccioly/Dropbox/workspace_emse/ResultData", "downloads2");
		p.analyzeMergeCommits();
		System.out.println(p);
	}

}
