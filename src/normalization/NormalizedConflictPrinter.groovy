package normalization

import main.MergeCommit
import main.SSMergeNode;

class NormalizedConflictPrinter {
	static String conflictReportHeader
	
	public static String getReportHeader(){
		String result = ''
		
		//print nodes
		for(SSMergeNode node in SSMergeNode.values()){
			result = result + node + ', '
		}
		
		result = result + 'ChangesInsideMethodsChunk, ChangesInsideMethodsLines'
		 
		return result
	}
	
	public static void printNormalizedProjectData(NormalizedProject p){
		String fileName = 'projectsChanges.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('Project , NumberOfScenarios, '+ this.getReportHeader() + '\n')
		}
		
		out.append(p.toString() + '\n')
		
	}
	
	public static void printNormalizedProjectIteration(NormalizedProject p){
		String fileName = 'ResultData' + File.separator + p.name + File.separator + 'projectsChanges.csv'
		File out = new File(fileName)
		out.delete()
		
		out.append('Project , NumberOfScenarios, '+ this.getReportHeader() + '\n')
		
		
		out.append(p.toString() + '\n')
		
	}
	
	public static void printEvoScenarioReport (EvoScenario evo, String projectName){
		String fileName = 'ResultData' + File.separator + projectName + File.separator + 'EvoScenarios.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('Scenario , '+ this.getReportHeader() + '\n')
		}
		out.append(evo.toString() + '\n')
	}
	
	public static void printMergeScenariosWithConflictsOnNonJavaFiles(EvoScenario evo, String projectName){
		String fileName = 'ResultData' + File.separator + projectName + File.separator + 'EvoScenariosWithConflicts.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('MergeScenario\n')
		}
		
		out.append(evo.rev_file + '\n')
		
	}
	
	public static void printCommitList(String projectName, ArrayList<MergeCommit> commits){
		String filePath = 'ResultData' + File.separator + projectName + File.separator + 'commits.csv'
		File file = new File(filePath)
		file.delete()
		file = new File(filePath)
		if(!file.exists()){
			file.getParentFile().mkdirs()
		}
		file.append('Commit,Parent,Date\n')
		for(MergeCommit commit in commits){
			file.append(commit.sha + ',' + commit.parent1 + ',' + commit.date + '\n')
		}
	}
}
