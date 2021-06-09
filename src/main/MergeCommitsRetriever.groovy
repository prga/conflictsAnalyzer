package main

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk

class MergeCommitsRetriever {
	
	
	String clonePath
	String date
	public MergeCommitsRetriever(String clonePath, String date){
		this.clonePath = clonePath
		this.date = date
	}
	
	public ProcessBuilder getProcessBuilder(){
		ProcessBuilder result = null
		if(!this.date.equals('')){
			this.date = '--since=\"' + this.date + '\"'
			result = new ProcessBuilder("git", "log", "--merges", this.date)
		}else{
		result = new ProcessBuilder("git", "log", "--merges")
		}
		
		return result
	}
	
	public ArrayList<MergeCommit> retrieveMergeCommits(){
		ArrayList<MergeCommit> merges = new ArrayList<MergeCommit>()
		
		try{
			ProcessBuilder pb = this.getProcessBuilder()
			pb.directory(new File(this.clonePath))
			//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			Process p = pb.start()
			//p.waitFor()
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()))
			String line = ""
			
			while ((line=buf.readLine())!=null) {
				if(line.startsWith('commit')){
					MergeCommit merge = new MergeCommit()
					merge.setSha(line.split(' ')[1])
					line=buf.readLine()
					String[] data = line.split(' ')
					merge.setParent1(data[1])
					merge.setParent2(data[2])
					line=buf.readLine()
					line=buf.readLine()
					//Date date = this.getCommitDate(line)
					//merge.setDate(date)		
					merges.add(merge)
				}
			}
			p.getInputStream().close()
			Collections.reverse(merges)
		}catch(Exception e){
			e.printStackTrace()
		}
		return merges
	}
	
	public Date getCommitDate(String d){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy")
		Date result = null
		String[] data = d.split(' ')
		String day = data[5]
		String month = data[4]
		String year = data[7]
		String dateInString = day + '/' + month + '/' + year
		result = formatter.parse(dateInString)
		return result
	}
	
	/**
	 * @return a list of all commits with one single parent
	 */
	public ArrayList<MergeCommit> retrieveNonMergeCommits(String repo){
		ArrayList<MergeCommit> commits = new ArrayList<MergeCommit>()
		try{
			
			ProcessBuilder pb = new ProcessBuilder("git", "log")
			pb.directory(new File(this.clonePath))
			Process p = pb.start()
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()))
			String line = ""
			
			while ((line=buf.readLine())!=null) {
				if(line.startsWith('commit')){
					String sha = line.split(' ')[1]
					line=buf.readLine()
					if(!line.startsWith('Merge')){
						println sha
						MergeCommit mc = new MergeCommit()
						String parent = this.getCommitParent(sha, repo)
						if(!parent.equals('')){
							line=buf.readLine()
							Date date = this.getCommitDate(line)
							mc.setSha(sha)
							mc.setParent1(parent)
							mc.setParent2('')
							mc.setDate(date)
							commits.add(mc)
						}
						
					}
					
				}
			}
			p.getInputStream().close()
			Collections.reverse(commits)
		}catch(Exception e){
			e.printStackTrace()
		}
		return commits
	}
	
	public String getCommitParent(String sha, String repo){
		String parent = ''
		Git git = CommitterAnalyzer.openRepository(this.clonePath, repo)
		Repository repository = git.getRepository()
		parent = this.getParentSha(sha, repository)
		
		
		return parent
	}
	
	public String getParentSha(String sha, Repository repo){
		String parent = ''
		try{
			RevWalk walk = new RevWalk(repo)
			ObjectId id = repo.resolve(sha)
			RevCommit commit = walk.parseCommit(id)
			RevCommit[] cParent = commit.getParents()
			String temp = cParent[0].toString()
			parent = temp.split(' ')[1]
		}catch(Exception e){
			println 'could not find parent of commit ' + sha
		}
		
		return parent
	}
	
	public static void main(String[] args){
		/*date is optional, if you want to get all commits pass the date parameter as an empty string
		 * otherwise pass the date parameter as an string with the format "yyyy-MM-dd" */
		MergeCommitsRetriever merges = new MergeCommitsRetriever("", "")
		ArrayList<MergeCommit> list =  merges.retrieveMergeCommits();
		
	}
}
