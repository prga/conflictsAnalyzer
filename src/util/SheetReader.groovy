package util

import java.awt.List;

public class SheetReader {
	
	/*this function reads two sheets and outputs the projects that appear on the first sheet but not on the second sheet
	 * A-B*/
	public static void AMinusB(String sheetAPath, String sheetBPath){
		
		Map<String,Integer> sheetA = SheetReader.readSheet(sheetAPath)
		Map<String,Integer> sheetB = SheetReader.readSheet(sheetBPath)
		for(String project in sheetA.keySet()){
			String projectB = sheetB.get(project)
			if(projectB==null){
				println project
			}
		}
	}
	
	
	public static Map<String,Integer> readSheet(String sheetPath){
		File sheet = new File(sheetPath)
		Map<String,Integer> result = new HashMap<String, Integer>()
		if(sheet.exists()){
			sheet.eachLine {
				if(!it.startsWith("Project") && !it.startsWith('Merge_scenario')){
					String[] split = it.split(",")
					String projectName = split[0].trim()
					result.put(projectName, 0)
				}
			}
		}
		return result
	}
	
	/*this method read the project names in the sheet and print the double inputs*/
	public static void doubleInputs(String sheetPath){
		File sheet = new File(sheetPath)
		Map<String,Integer> result = new HashMap<String, Integer>()
		sheet.eachLine {
			if(!it.startsWith("Project")){
				String[] split = it.split(",")
				String projectName = split[0].trim()
				String nameExists = result.get(projectName)
				if(nameExists!=null){
					println projectName
				}else{
					result.put(projectName, 0)
				}
			}
		}
		println 'cabou'
	}
	
	public static void main (String[] args){
		/*SheetReader.AMinusB("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv",
			 "/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/ConflictingScenarios.csv")*/
		
		/*SheetReader.AMinusB("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData copy.csv",
			"/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv")*/
		
		/*SheetReader.AMinusB("/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/projectsChanges.csv",
				"/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv")*/
		/*/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/projectsChanges.csv*/
		//SheetReader.doubleInputs("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv")
		SheetReader.AMinusB("/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/projectsPatternData.csv",
			"/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/projectsChanges.csv",)
	}
}
 