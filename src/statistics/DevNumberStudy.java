package statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DevNumberStudy {

	public static void run(String resultData, String projectList){
		//creates downloads directory
		File downloads = new File("downloads2");
		if(!downloads.exists()){
			downloads.mkdir();
		}
		
		//read project list
		File projects = new File(projectList);
		BufferedReader br = null;
		String line = "";
		
		try {
			br = new BufferedReader(new FileReader(projects));
			 while ((line = br.readLine()) != null) {
				 System.out.println("Starting to analyze project " + line);
				 Project p = new Project(line, resultData, downloads.getAbsolutePath());
				 p.analyzeMergeCommits();
				 DevNumberPrinter.printProjectReport(p.toString());
				 DevNumberPrinter.printProjectReportPerMerges(p.mergesSummary());
				 System.out.println("Finished to analyze project " + line);
				 
			 }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		DevNumberStudy.run("resultData1stround_emse/ResultData", 
				"projectsList");
	}

}
