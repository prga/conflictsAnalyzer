package main

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId;
import java.util.Date;

class MergeCommitsRetriever {


	String clonePath
	String startAnalysisDate
	String travisLocation

	/*public MergeCommitsRetriever(String clonePath, String date){
		this.clonePath = clonePath
		this.startAnalysisDate = date
	}*/

	public MergeCommitsRetriever(String clonePath, String travisLocation){
		this.clonePath = clonePath
		this.travisLocation = travisLocation
		this.setStartAnalysisDate()
	}

	public void setStartAnalysisDate(){

		/*runs the command*/
		ProcessBuilder pb = new ProcessBuilder(this.travisLocation, "show", "1")
		pb.directory(new File(this.clonePath))
		Process p = pb.start()

		/*gets the command result*/
		BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()))
		String line=''
		boolean finishDate = false;
		while ((line = buf.readLine()) != null && !finishDate) {
			if(line.startsWith("Finished")) {
				finishDate = true;
				this.getFirstBuildDate(line)
			}
		}


	}
	
	public void getFirstBuildDate(String d){
		String[] tokens = d.split(' ')
		this.startAnalysisDate = tokens[tokens.length - 2]
		//2017-08-02
	}
	
	public String chooseRightDate(String d){
		
		String result = ''
		Date rightDate = null
		Date travisDate = this.getTravisDate(d)
		Date oneYearAgo = this.getOneYearAgoDate()

		if(travisDate.before(oneYearAgo)){
			rightDate = oneYearAgo
		}else if(travisDate.after(oneYearAgo)){
			rightDate = travisDate
		}else{
			rightDate = travisDate
		}
		
		LocalDate localDate = rightDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int year  = localDate.getYear();
		int month = localDate.getMonthValue();
		int day   = localDate.getDayOfMonth();
		result = year + '-' + month + '-' + day
		return result
		
	}

	public Date getOneYearAgoDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy")
		Date result = null
		Date actualDate = new Date()
		LocalDate localDate = actualDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int year  = localDate.getYear();
		int month = localDate.getMonthValue();
		int day   = localDate.getDayOfMonth();
		year--
		String oneYearAgo = day + '/' + month + '/' + year
		result = formatter.parse(oneYearAgo)
		return result

	}

	public Date getTravisDate(String d){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy")
		Date result = null
		String[] data = d.split(' ')
		String day = data[1]
		String month = data[2]
		String year = data[3]
		String dateInString = day + '/' + month + '/' + year
		result = formatter.parse(dateInString)
		return result
	}

	public ProcessBuilder getProcessBuilder(){
		ProcessBuilder result = null
		if(!this.startAnalysisDate.equals('')){
			this.startAnalysisDate = '--since=\"' + this.startAnalysisDate + '\"'
			result = new ProcessBuilder("git", "log", "--merges", this.startAnalysisDate)
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
					Date date = this.getCommitDate(line)
					merge.setDate(date)
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
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy", Locale.US)
		Date result = null
		String[] data = d.split(' ')
		String day = data[5]
		String month = data[4]
		String year = data[7]
		String dateInString = day + '/' + month + '/' + year
		result = formatter.parse(dateInString)
		return result
	}

	public static void main(String[] args){
		/*date is optional, if you want to get all commits pass the date parameter as an empty string
		 * otherwise pass the date parameter as an string with the format "yyyy-MM-dd" */
		/*MergeCommitsRetriever merges = new MergeCommitsRetriever("/Users/paolaaccioly/Documents/Doutorado/workspace_travis/downloads/Singularity")
		ArrayList<MergeCommit> m = merges.retrieveMergeCommits()*/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy", Locale.US)
		String dateInString = '4/May/2017'
		Date result = formatter.parse(dateInString)
		println 'hello'

	}
}
