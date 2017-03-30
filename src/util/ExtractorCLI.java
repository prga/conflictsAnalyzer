package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import main.MergeCommit;

public class ExtractorCLI {
	private String downloadDir;
	private String fork;
	private String forkDir;
	private String originalRepo;
	private String name;
	private String username;
	private String password;
	private String token;
	private String travisLocation;
	
	public ExtractorCLI(String username, String password, String token, String travis, 
			String download, String originalRepo){
		this.username = username;
		this.password = password;
		this.token = token;
		this.travisLocation = travis;
		this.downloadDir = download;
		this.originalRepo = originalRepo;
		this.setName();
		this.setFork();
		this.setForkDir();
		this.createFork();
		this.activateTravis();
		this.cloneForkLocally();
		
	}
	

	
	public void replayBuildsOnTravis(MergeCommit mc){
		this.cloneForkLocally();
		this.resetToOldCommitAndPush(mc.getParent1());
		this.pullFromOriginalRepo();
		this.resetToOldCommitAndPush(mc.getParent2());
		System.out.println("testar até aqui");
		this.commitEditedMergeAndPush();
		//o que fazer depois?
	}
	
	public void cloneForkLocally(){
		int result = -1;
		
		//clone fork
		String cloneFork = "git clone https://github.com/" + this.fork + ".git";
		try {
			Process p = Runtime.getRuntime().exec(cloneFork, null, new File(this.downloadDir));
			result = p.waitFor();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		//creates branch for merges and pushes it
		this.createBranchAndPush();
		
	}
	
	private void createBranchAndPush(){
		int result = -1;
		String createBranch = "git checkout -b merges";
		String pushBranch = "git push origin merges";
		
		Process p;
		try {
			p = Runtime.getRuntime().exec(createBranch, null, new File(this.forkDir));
			result = p.waitFor();
			p = Runtime.getRuntime().exec(pushBranch, null, new File(this.forkDir));
			result = p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void commitEditedMergeAndPush(){
		this.replaceFiles();
	}
	
	private void pullFromOriginalRepo(){
		int result = -1;
		String pull = "git pull --no-edit https://github.com/" + this.originalRepo;
		try {
			Process p = Runtime.getRuntime().exec(pull, null, new File(this.forkDir));
			result = p.waitFor();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void replaceFiles(){
		
	}
	
	private void resetToOldCommitAndPush(String sha){
		int result = -1;
		String reset = "git reset --hard " + sha;
		String forcePush = "git push -f origin HEAD:master";
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			File file = new File(this.forkDir);
			pr = run.exec(reset, null, file);
			result = pr.waitFor();
			pr = run.exec(forcePush, null, file);
			result = pr.waitFor();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setName() {
		String[] parts = this.originalRepo.split("/");
		this.name = parts[1];
	}
	
	public String getDownloadPath() {
		return downloadDir;
	}
	
	public void setDownloadPath(String downloadPath) {
		this.downloadDir = downloadPath;
	}
	
	public String getRepo() {
		return fork;
	}
	
	public void setRepo(String repo) {
		this.fork = repo;
	}
	
	
	
	public String getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}

	public String getFork() {
		return fork;
	}

	public void setFork(String fork) {
		this.fork = fork;
	}
	
	public void setFork(){
		this.fork = this.username + File.separator + this.name;
	}

	public String getForkDir() {
		return forkDir;
	}

	public void setForkDir(String forkDir) {
		this.forkDir = forkDir;
	}
	
	public void setForkDir(){
		this.forkDir = this.downloadDir + File.separator  + this.name;
	}

	public String getOriginalRepo() {
		return originalRepo;
	}

	public void setOriginalRepo(String originalRepo) {
		this.originalRepo = originalRepo;
	}
	
	public void createFork(){
		int result = -1;
		String cmd = "curl -u " + this.username + ":" + this.password + 
		" -X POST https://api.github.com/repos/" + this.originalRepo + "/forks";
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			pr = run.exec(cmd);
			result = pr.waitFor();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line = input.readLine()) != null) {
			  System.out.println(line);
			}
			input.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void activateTravis(){	

		String cmd = this.travisLocation + " login --github-token " + this.token;
		String cmd2 = this.travisLocation + " enable -r " + this.username + "/" + this.name;
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			pr = run.exec(cmd);
			pr.waitFor();
			pr = run.exec(cmd2);
			pr.waitFor();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}



	public void setPassword(String password) {
		this.password = password;
	}

	

	public String getToken() {
		return token;
	}



	public void setToken(String token) {
		this.token = token;
	}



	public String getTravisLocation() {
		return travisLocation;
	}



	public void setTravisLocation(String travisLocation) {
		this.travisLocation = travisLocation;
	}



	public static void main(String[] args) {
		/*ExtractorCLI extractor = new ExtractorCLI("/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/downloads",
				"prga/TGM");*/
		MergeCommit mc = new MergeCommit();
		mc.setSha("a8bc1ad6788a72f02ac30b500507cec74e719f53");
		mc.setParent1("85dbaef");
		mc.setParent2("f0bb5bb");
		/*ExtractorCLI extractor = new ExtractorCLI("/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/downloads", 
				"conflictpredictor/TGM", "prga/TGM");
				extractor.replayBuildsOnTravis(mc);
				*/
		/*ExtractorCLI extractor = new ExtractorCLI(username, password, token, travis, download, originalRepo);*/
		ExtractorCLI extractor = new ExtractorCLI("conflictpredictor", "conflict1407", 
				"d55a5163864b75744b59a705d446ecacd8f2adb2", "/Users/paolaaccioly/.rvm/gems/ruby-2.3.1/bin/travis",
				"/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/downloads", "prga/TGM");
		
		
	}
	
}
