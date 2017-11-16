package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import main.BuildScenario;
import main.MergeCommit;
import main.TravisCommit;

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
	private String curlLocation;
	private Map<String, MergeCommit> originalToReplayedMerge;
	private String masterBranch;

	public ExtractorCLI(String username, String password, String token, String travis, 
			String download, String originalRepo, String curl, String master){
		this.username = username;
		this.password = password;
		this.token = token;
		this.travisLocation = travis;
		this.curlLocation = curl;
		this.downloadDir = download;
		this.masterBranch = master;
		File d = new File(this.downloadDir);
		d.mkdir();
		this.originalRepo = originalRepo;
		this.setName();
		this.setFork();
		this.setForkDir();
		this.createFork();
		this.activateTravis();
		this.cloneForkLocally();
		this.createBranches();
		this.originalToReplayedMerge = new HashMap<String, MergeCommit>();

	}



	public void replayBuildsOnTravis(MergeCommit mc, String mergeDir){
		BuildScenario build = new BuildScenario(mc.getParent1(), mc.getParent2(), mc.getSha());
		System.out.println("Reseting to parent 1 and pushing to " + this.masterBranch);
		this.resetToOldCommitAndPush(mc.getParent1());
		this.mergeBranches("origHist");
		System.out.println("Waiting for parent 1 build to end");
		String parent1Build = this.checkBuildStatus(build.getParent1());
		if(parent1Build.equalsIgnoreCase("passed")) {
			System.out.println("Reseting to parent 2 and pushing to " + this.masterBranch);
			this.resetToOldCommitAndPush(mc.getParent2());
			this.mergeBranches("origHist");
			System.out.println("Waiting for parent 2 build to end");
			String parent2Build = this.checkBuildStatus(build.getParent2());
			if(parent2Build.equalsIgnoreCase("passed")) {
				System.out.println("Reseting to merge commit and pushing to " + this.masterBranch);
				this.resetToOldCommitAndPush(mc.getSha());
				this.mergeBranches("origHist");
				System.out.println("Waiting for original merge commit build to end");
				this.checkBuildStatus(build.getMergeCommit());
				System.out.println("Replacing files from original merge to "
						+ "replayed merge and pushing to merges");
				String newsha = this.commitEditedMergeAndPush(mc, mergeDir);
				TravisCommit replayedMerge = new TravisCommit(newsha);
				build.setReplayedMergeCommit(replayedMerge);
				System.out.println("Waiting for replayed merge commit build to end");
				this.checkBuildStatus(build.getReplayedMergeCommit());
			}	
		}
		System.out.println("printing results");
		this.printMergeSHAS(build);

	}

	public String checkBuildStatus(TravisCommit commit) {
		String result = "";
		String buildId = this.getLatestBuildID();
		commit.setBuildID(buildId);
		String buildStatus = "started";
		try {
			while(buildStatus.equalsIgnoreCase("started")) {
				Thread.sleep(5000);
				buildStatus = this.auxCheckBuildStatus(buildId);
			}		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String auxCheckBuildStatus(String buildID) {
		String status = "";
		ProcessBuilder pb = new ProcessBuilder("travis", "show", buildID);
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
				if(line.startsWith("State:")) {
					String[] tokens = line.split(" ");
					status = tokens[1];
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		return status;
	}

	public String getLatestBuildID() {
		String id = "";
		ProcessBuilder pb = new ProcessBuilder("travis", "show");
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
				if(line.startsWith("Build #")) {
					String[] tokens = line.split(" ");
					id = tokens[1].substring(1);
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		return id;
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

	}



	private void createBranches() {
		this.createbranch("origHist");
		this.checkoutBranch(this.masterBranch);
		this.createbranch("merges");
		this.pushBranchToRemote("merges");
		this.checkoutBranch(this.masterBranch);
	}

	private void createbranch(String branchName){
		int result = -1;
		String createBranch = "git checkout -b " + branchName;

		Process p;
		try {
			p = Runtime.getRuntime().exec(createBranch, null, new File(this.forkDir));
			result = p.waitFor();

		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

	private void pushBranchToRemote(String branchName){
		int result = -1;
		String cmd = "git config credential.helper store";
		String pushBranch = "git push origin " + branchName;

		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd, null, new File(this.forkDir));
			result = p.waitFor();
			p = Runtime.getRuntime().exec(pushBranch, null, new File(this.forkDir));
			result = p.waitFor();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

	private void mergeBranches(String branchName){
		ProcessBuilder pb = new ProcessBuilder("git", "merge", branchName);
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}

		} catch (IOException e) {

			e.printStackTrace();
		}	
	}

	private String commitEditedMergeAndPush(MergeCommit mc, String mergeDir){
		String newSha = "";
		this.checkoutBranch("merges");
		this.replaceFiles(mergeDir);
		newSha = this.commitAndPushMerge(mc);
		this.checkoutBranch(this.masterBranch);
		return newSha;
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
		this.copyFilesFromSSMerge(mergeDir, this.forkDir);

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

					e.printStackTrace();
				}
			}
		}
	}

	private void copyFilesFromSSMerge(String mergeDir, String newPath){
		File source = new File(mergeDir);
		File dest = new File(newPath);
		try {
			FileUtils.copyDirectory(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String commitAndPushMerge(MergeCommit mc){
		String newSha = "";
		this.addChangesToStagedArea();
		this.commitChanges();
		this.pushToMergeBranch();
		newSha = this.getHead();
		this.originalToReplayedMerge.put(newSha, mc);
		return newSha;

	}

	private void addChangesToStagedArea() {
		ProcessBuilder pb = new ProcessBuilder("git", "add", ".");
		pb.redirectErrorStream(true);
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			/*BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}*/

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void commitChanges() {
		ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", "\"merge\"");
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void pushToMergeBranch() {
		ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "merges");
		pb.redirectErrorStream(true);
		pb.directory(new File(this.forkDir));
		try {
			Process p = pb.start();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}

		} catch (IOException e) {

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

			e.printStackTrace();
		}
		return sha;
	}

	private void resetToOldCommitAndPush(String sha){
		int result = -1;
		String reset = "git reset --hard " + sha;
		String forcePush = "git push -f origin HEAD:" + this.masterBranch;
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

			e.printStackTrace();
		} catch (InterruptedException e) {

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
		this.fork = this.username + "/" + this.name;
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

	public void createFork() {
		String user = this.username + ":" + this.password;
		String fork = "https://api.github.com/repos/" + this.originalRepo + "/forks";
		ProcessBuilder pb = new ProcessBuilder(this.curlLocation, "-u", user, "-X", "POST", fork);
		pb.redirectErrorStream(true);
		Process process;
		try {
			process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
				System.out.println(line);
			process.waitFor();
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
			String status = "not run yet";
			while(!status.equalsIgnoreCase("enabled")) {
				Thread.sleep(5000);
				pr = run.exec(cmd2);
				pr.waitFor();
				String line;
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				while ((line = input.readLine()) != null) {
					System.out.println(line);
					status = line;
				}

				input.close();
			}

		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

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

	private void printMergeSHAS(BuildScenario build){
		String path = "ResultData" + File.separator + this.name + File.separator + "mergesOnTravis.csv";
		File f = new File(path);
		String content = "";
		try {
			if(!f.exists()){

				f.createNewFile();
				String header = "parent1SHA;parent1BuildId;parent1BuildStatus;" + 
						"parent2SHA;parent2BuildId;parent2BuildStatus;" +
						"mergeCommitSHA;mergeCommitBuildId;mergeCommitBuildStatus;" +
						"repMergeCommitSHA;repMergeCommitBuildId;repMergeCommitBuildStatus" + "\n";
				content = header;

			}
			content = build.toString() + "\n";
			FileWriter fw= new FileWriter(f.getAbsoluteFile(), true);
			BufferedWriter bw  = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			fw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		MergeCommit mc = new MergeCommit();
		mc.setSha("ccd4ddd3eeb6f219ed2e7a184fceeb4e11df7f80");
		mc.setParent1("1bca94af");
		mc.setParent2("d415ba83");
		ExtractorCLI cli = new ExtractorCLI("conflictpredictor", "conflict1407", 
				"2c373b4405e61827eb069d4cbf22fc812bbb11e4", "C:\\Ruby24-x64\\bin\\travis.bat", 
				"C:\\Users\\155 X-MX\\Documents\\dev\\second_study\\downloads\\travis", 
				"brettwooldridge/HikariCP", "C:\\Curl\\curl.exe", "dev");
		cli.replayBuildsOnTravis(mc, "C:\\Users\\155 X-MX\\Documents\\dev\\second_study\\downloads\\ssmerge\\HikariCP\\revisions\\rev_1bca9_d415b\\rev_merged_git");


	}

}
