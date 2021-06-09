package main


import java.util.ArrayList;
import modification.traversalLanguageParser.addressManagement.DuplicateFreeLinkedList


public class ConflictPrinter {

	static String conflictReportHeader 
	
	public static setconflictReportHeader(){
		this.conflictReportHeader = ''
		String noPattern = SSMergeConflicts.NOPATTERN.toString()
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			String type = c.toString()
			this.conflictReportHeader = this.conflictReportHeader +
			type + ', '
			if(!type.equals(noPattern)){
				this.conflictReportHeader = this.conflictReportHeader +
				type + 'DS, ' + type + 'CL, ' +
				type + 'IFP, '
			}
		}
		
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			String cause = p.toString()
			String diffSpacing = cause + 'DS'
			this.conflictReportHeader = this.conflictReportHeader + cause + ', ' + diffSpacing + ', '
		}
		this.conflictReportHeader = this.conflictReportHeader + 'PossibleRenamings, '
		
		for(EditSameMCTypes e : EditSameMCTypes.values()){
			String type = e.toString()
			this.conflictReportHeader = this.conflictReportHeader + type + ', '
		}
		
		this.conflictReportHeader = this.conflictReportHeader.substring(0, this.conflictReportHeader.length() - 2)
		
	}
	
	public static String getConflictReportHeader(){
		return this.conflictReportHeader
	}

	public static void printProjectData(Project p){
		String fileName = 'projectsPatternData.csv'
		def out = new File(fileName)
		
		if(!out.exists()){
		String row = 'Project, Merge_Scenarios, Conflicting_Scenarios, ' +
		this.conflictReportHeader + '\n'

		out.append(row)
		}

			out.append(p.toString() + '\n')

	}
	
	public static void updateProjectData(Project p){
		String fileName = "ResultData" + File.separator + p.getName() + File.separator + 'ProjectReport.csv'
		def out = new File(fileName)
		
		out.delete()
		out = new File(fileName)
		
		
		String row = 'Project, Merge_Scenarios, Conflicting_Scenarios, ' +
		this.conflictReportHeader + '\n'

		out.append(row)
		out.append(p.toString() + '\n')

	}
	public static void printMergeScenarioReport(MergeScenario mergeScenario, String projectName){


		File out = new File("ResultData" + File.separator + projectName + File.separator + 'MergeScenariosReport.csv')

		if(!out.exists()){
			String fileHeader = 'Merge_scenario, Total_Files, Files_Edited_By_One_Dev, ' +
					'Files_That_Remained_The_Same, Files_Added_By_Ove_Dev, Files_Merged, ' + 
					'Files_With_Conflicts, ' +
					 this.conflictReportHeader + '\n'
			out.append(fileHeader)
		}
		out.append(mergeScenario.toString() + '\n')
		printBadParsedNodes(mergeScenario, projectName)
		printMergeScenarioMetrics(mergeScenario, projectName)
		printConflictsReport(mergeScenario, projectName)
		if(mergeScenario.hasNonJavaFilesConflict){
			printMergeWithNonJavaFilesConflicting(projectName, mergeScenario)
		}
	}
	
	public static void printBadParsedNodes(MergeScenario mergeScenario, String projectName){
		File out = new File("ResultData" + File.separator + projectName + File.separator + 
			'Merge_Scenarios' + File.separator + mergeScenario.name + '_BadParsedFiles.csv')
		DuplicateFreeLinkedList<File> parsedErrors = mergeScenario.fstGenMerge.parsedErrors
		for(File f : parsedErrors){
			out.append(f.getAbsolutePath() + '\n')
		}
	}
	public static void printMergeScenarioMetrics(MergeScenario mergeScenario, String projectName){
		File out = new File('ResultData' + File.separator + projectName + File.separator +
				'Merge_Scenarios' + File.separator + mergeScenario.name + '.csv')
		if(!out.exists()){
			String header = 'File, Total_of_Conflicts, ' +
					'Conflicts_Inside_Methods, Methods_with_Conflicts, ' +
					'Conflicts_Outside_Methods, ' +
					this.conflictReportHeader + '\n'
			out.append(header)
		}

		out.append(mergeScenario.printMetrics() + '\n')


	}

	public static void printConflictsReport(MergeScenario mergeScenario, String projectName){

		def out = new File("ResultData" + File.separator + projectName + File.separator + 'ConflictsReport.csv')

		def delimiter = '========================================================='
		out.append(delimiter)
		out.append '\n'

		out.append('Revision: ' + mergeScenario.extractResult.revisionFile + '\n')

		for(MergedFile mergedFile: mergeScenario.getMergedFiles()){

			for(Conflict c: mergedFile.getConflicts()){

				def row = ['Conflict type: '+ c.getType() + '\n' + 
					'Number of Conflicts: ' + c.getNumberOfConflicts() + '\n' +
					'Different Spacing: ' + c.getDifferentSpacing() + '\n'  +
					'Consecutive Lines: ' + c.getConsecutiveLines() + '\n'+ 
					'Intersection: ' + c.getFalsePositivesIntersection() + '\n' +
					'Cause same signature: ' + c.getCauseSameSignatureCM() + '\n' +
					'Possible renaming: ' + c.getPossibleRenaming() + '\n' +
					'EditSameMCType: ' + c.editSameMCType + '\n' +
					'Conflict body: ' + '\n' + c.getBody() ]
				out.append row.join(',')
				out.append '\n'
				row = ['File path: ' + c.getFilePath()]
				out.append row.join(',')
				out.append '\n'

			}

		}
		out.append '\n'
		out.append(delimiter)
	}
	
	public static void printMergeWithNonJavaFilesConflicting(String projectName, MergeScenario ms){
		String filePath = 'ResultData' + File.separator + projectName + File.separator + 'mergeWithNonJavaFilesConflicting.csv'
		File file = new File(filePath)
		file.append(ms.name + '\n')
	}
	
	public static void printMergeCommitsList(String projectName, ArrayList<MergeCommit> mergeCommits){
		String filePath = 'ResultData' + File.separator + projectName + File.separator + 'mergeCommits.csv'
		File file = new File(filePath)
		file.delete()
		file = new File(filePath)
		file.append('Merge,Parent1,Parent2,Date\n')
		for(MergeCommit mc in mergeCommits){
			String commit = mc.sha + ',' + mc.parent1 + ',' + mc.parent2 + ',' + mc.date + '\n'
			file.append(commit)
		}
	}
	public static void main (String[] args){
		ConflictPrinter.setconflictReportHeader()
		println ConflictPrinter.getConflictReportHeader()
	}
}
