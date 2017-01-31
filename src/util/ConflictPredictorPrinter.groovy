package util

import main.ConflictPredictor;
import main.MergeScenario
import main.Project

class ConflictPredictorPrinter {

	public static String msSeparator = '#MS_XXX_MS#'

	public static String conflictPredictorSeparator = '#CP_===_CP#'

	public static String internalPredictorSeparator = '#HAS_***REFERENCE_#'

	public static void printProjectReport(Project project){
		String header = 'Project,analyzed_Merge_Scenarios,Conflicting_MergeScenarios,MS_With_Predictors,Methods_With_Conflicts,' +
				'Conflicting_EditSameMC,Conflicting_EditSameMC_DS,Conflicting_EditSameFD,' +
				'Conflicting_EditSameFD_DS,NonConflicting_EditSameMC,NonConflicting_EditSameMC_DS,' +
				'NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,EditDiffMC,EditDifffMC_EditSameMC,' +
				'EditDiffMC_EditionAddsMethodInvocation,EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ConflictPredictor_Projects_Report.csv')

		if(!file.exists()){
			file.append(header)
		}
		String projectSummary = project.getProjectCSSummary()
		file.append(projectSummary + '\n')
		//EmailSender.sendEmail(projectSummary)
	}

	public static void updateProjectData(Project project){
		String header = 'Project,analyzed_Merge_Scenarios,Conflicting_MergeScenarios,MS_With_Predictors,Methods_With_Conflicts,' +
				'Conflicting_EditSameMC,Conflicting_EditSameMC_DS,Conflicting_EditSameFD,' +
				'Conflicting_EditSameFD_DS,NonConflicting_EditSameMC,NonConflicting_EditSameMC_DS,' +
				'NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,EditDiffMC,EditDifffMC_EditSameMC,' +
				'EditDiffMC_EditionAddsMethodInvocation,EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_Project_Report.csv')

		if(file.exists()){
			file.delete()
		}

		file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_Project_Report.csv')
		file.append(header)
		file.append(project.getProjectCSSummary()+ '\n')

	}

	public static void printMergeScenarioReport(Project project, MergeScenario ms, String ms_Summary){
		String header = 'Merge_Scenario,has_merge_Conflict,has_predictor,Methods_With_Conflicts,' +
				'Conflicting_EditSameMC,Conflicting_EditSameMC_DS,' +
				'Conflicting_EditSameFD,Conflicting_EditSameFD_DS,NonConflicting_EditSameMC,' +
				'NonConflicting_EditSameMC_DS,NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,' +
				'EditDiffMC,EditDifffMC_EditSameMC,EditDiffMC_EditionAddsMethodInvocation,' +
				'EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ResultData' + File.separator + project.name +
				File.separator + 'ConflictPredictor_MS_Report.csv')

		if(!file.exists()){
			file.append(header)
		}
		file.append(ms_Summary + '\n')

		ConflictPredictorPrinter.updateProjectData(project)
		ConflictPredictorPrinter.printConflictPredictors(project.name, ms)
	}

	public static void printConflictPredictors(String projectName, MergeScenario ms){

		File file = new File('ResultData' + File.separator + projectName + File.separator +
				'ConflictPredictor_Report.csv')
		file.append(ConflictPredictorPrinter.msSeparator + '\n')
		file.append('Merge scenario: ' + ms.name + '\n')


		for(String filePath : ms.filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = ms.filesWithConflictPredictors.get(filePath)
			for(ConflictPredictor predictor : predictors){
				file.append(ConflictPredictorPrinter.conflictPredictorSeparator  + '\n')
				file.append(predictor.toString() + '\n')
				file.append(ConflictPredictorPrinter.conflictPredictorSeparator  + '\n')
			}
		}


		file.append(ConflictPredictorPrinter.msSeparator + '\n')
	}
	
	public static void printGitBlameProblem(ConflictPredictor predictor){
		File file = new File('ResultData' + File.separator + 'gitBlameHadProblem.csv')
		if(!file.exists()){
			file.append('Filepath,Signature\n')
		}
		file.append(predictor.filePath + ',' + predictor.signature + '\n')
	}
	
	
}
