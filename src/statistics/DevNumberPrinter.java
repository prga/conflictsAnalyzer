package statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DevNumberPrinter {

	public static void printProjectReport(String projectSummary){
		String filePath = "NumDevs.csv";
		File file = new File(filePath);
		String content = "";
		if(!file.exists()){
			content = "name";
			for(NumDevCategories c : NumDevCategories.values()){
				String cat = c.toString();
				content = content + ";" + cat + ";" + cat + "DS;" +
				cat + "CL;" + cat + "IFP";
			}
			content = content + "\n";
		}
		content = content + projectSummary + "\n";
		DevNumberPrinter.writeContent(filePath, content);

	}

	public static void printMergeCommitReport(String projectName, String mergeSummary){
		String dirPath = "ResultData" + File.separator + projectName;
		File dir = new File(dirPath);
		if(!dir.exists()){
			dir.mkdir();
		}
		String filePath =  dirPath + File.separator + "mc_numDevs.csv";
		File file = new File(filePath);
		String content = "";
		if(!file.exists()){
			content = "name;category;numDev;conflict;conflictDS;conflictCL;"
					+ "conflictsIFP;hasConflictsJava;hasConflictsNonJava\n";
		}
		content = content + mergeSummary + "\n";
		DevNumberPrinter.writeContent(filePath, content);
	}
	
	public static void printProjectReportPerMerges(String mergesSummary){
		String filePath = "Project_devs.csv";
		File file = new File(filePath);
		String content = "";
		if(!file.exists()){
			content = "name";
			for(NumDevCategories c : NumDevCategories.values()){
				String cat = c.toString();
				content = content + ";" + cat + "_with_Conflict;" + cat 
						+ "_without_Conflict";
			}	
			content = content + "\n";
		}
		content = content + mergesSummary + "\n";
		DevNumberPrinter.writeContent(filePath, content);
	}

	public static void writeContent(String filePath, String content){
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;		
		try {
			fw = new FileWriter(filePath, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.print(content);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
