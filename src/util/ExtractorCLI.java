package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

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
	private Map<String, MergeCommit> originalToReplayedMerge;
	
	public ExtractorCLI(String username, String password, String token, String travis, 
			String download, String originalRepo){
		this.username = username;
		this.password = password;
		this.token = token;
		this.travisLocation = travis;
		this.downloadDir = download;
		File d = new File(this.downloadDir);
		d.mkdir();
		this.originalRepo = originalRepo;
		this.setName();
		this.setFork();
		this.setForkDir();
		this.createFork();
		this.activateTravis();
		this.cloneForkLocally();
		this.originalToReplayedMerge = new HashMap<String, MergeCommit>();
		
	}
	

	
	public void replayBuildsOnTravis(MergeCommit mc, String mergeDir){
		System.out.println("Reseting to parent 1 and pushing to master");
		this.resetToOldCommitAndPush(mc.getParent1());
		this.pullFromOriginalRepo();
		System.out.println("Reseting to parent 2 and pushing to master");
		this.resetToOldCommitAndPush(mc.getParent2());
		System.out.println("Replacing files from original merge to "
				+ "replayed merge and pushing to merges");
		this.commitEditedMergeAndPush(mc, mergeDir);
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
			this.checkoutBranch("master");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	private void commitEditedMergeAndPush(MergeCommit mc, String mergeDir){
		this.checkoutBranch("merges");
		this.replaceFiles(mergeDir);
		this.commitAndPushMerge(mc);
		this.checkoutBranch("master");
		this.pullFromOriginalRepo();
	}
	
	private void checkoutBranch(String branch){
		int result = -1;
		String checkout = "git checkout " + branch;
		try {
			Process p = Runtime.getRuntime().exec(checkout, null, new File(this.forkDir));
			result = p.waitFor();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
	
	private void replaceFiles(String mergeDir){
		this.removeFilesFromOriginalRepo();
		this.copyFilesFromSSMerge(mergeDir, mergeDir);
		
	}
	
	private void removeFilesFromOriginalRepo(){
		File fork = new File(this.forkDir);
		File[] files = fork.listFiles();
		for(File f : files){
			if(f.isFile()){
				f.delete();
			}else if(f.isDirectory() && !f.getName().equals(".git")){
				try {
					FileUtils.deleteDirectory(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void copyFilesFromSSMerge(String mergeDir, String newPath){
		File temp = new File(newPath);
		File [] list = temp.listFiles();
		for(File file : list){
			if(file.isDirectory()){
				this.copyFilesFromSSMerge(mergeDir, file.getAbsolutePath());
			}else if(file.isFile()){
				moveFile(mergeDir, file);
			}
		}
	}



	private void moveFile(String mergeDir, File file) {
		String path = file.getAbsolutePath().replaceFirst(mergeDir, this.forkDir);
		File temp2 = new File(path);
		Path toBeReplaced = temp2.toPath();
		try {
			Files.move(file.toPath(), toBeReplaced, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void commitAndPushMerge(MergeCommit mc){
		int result = -1;
		String add = "git add .";
		String commit = "git commit -m \"merge\" ";
		String push = "git push origin merges";
		try {
			Process p = Runtime.getRuntime().exec(add, null, new File(this.forkDir));
			result = p.waitFor();
			p = Runtime.getRuntime().exec(commit, null, new File(this.forkDir));
			result = p.waitFor();
			p = Runtime.getRuntime().exec(push, null, new File(this.forkDir));
			result = p.waitFor();
			
			String newSha = this.getHead();
			this.originalToReplayedMerge.put(newSha, mc);
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	
	}
	
	private String getHead(){
		String sha = "";
		ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				sha = line;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sha;
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


	
}
