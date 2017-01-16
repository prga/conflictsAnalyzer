package util

import main.ConflictPrinter;
import main.SSMergeConflicts

class CSVAnalyzer {

	public static void verifyDiffsOnSameSignatureMC(){
		File file = new File('/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv')
		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			if(!projectName.equals("Project")){
				int sameSignatureCM = Integer.parseInt(data[27])
				int smallMethod = Integer.parseInt(data[36])
				int renamedMethod = Integer.parseInt(data[37])
				int copiedMethod = Integer.parseInt(data[38])
				int copiedFile = Integer.parseInt(data[39])
				int noPattern = Integer.parseInt(data[40])

				int sumCauses = smallMethod + renamedMethod + copiedMethod + copiedFile + noPattern

				int diff = sameSignatureCM - sumCauses

				if(diff != 0){
					println projectName + ' ' + diff
				}
			}
		}
	}

	public static void writeRealConflictsCSV(){
		String filepath = System.getProperty("user.dir") + File.separator + 'projectsPatternData.csv'
		File file = new File(filepath)
		File out = new File('realConflictRate.csv')
		out.delete()
		out = new File('realConflictRate.csv')
		String line = 'Project,Merge Scenarios,Conflicting Scenarios,Conflicting_Scenarios_WDS,Conflicting_Scenarios_WCL\n'
		out.append(line)

		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			String analyzedMergeScenarios = data[1]
			if(!projectName.equals("Project")){
				ArrayList<Integer> i = countMergeScenarioWithRealConflicts(projectName)
				line = projectName + ',' + analyzedMergeScenarios + ',' + i.get(0) + ',' + i.get(1) + ',' + i.get(2) + '\n'
				//println line
				out.append(line)
			}
		}
	}

	/**
	 * @param projectName
	 * @return array containing the following metrics for the report: the first integer represents
	 *  the number of conflicting scenarios without spacing and consecutive lines edition conflicts, 
	 *  the second integer represents the number of merge scenarios without spacing conflicts, and
	 *  the third integer represents the number of merge scenarios without consecutive lines edition 
	 *  conflicts
	 */
	public static ArrayList<Integer> countMergeScenarioWithRealConflicts(String projectName){
		ArrayList<Integer> result = new ArrayList<Integer>()
		int ms_wfp, ms_wds, ms_wcl = 0
		String mergeScenarioFile = 'ResultData' + File.separator + projectName + File.separator +
				'MergeScenariosReport.csv'
		/*'ResultData' + File.separator + projectName + File.separator +
				'MergeScenariosReport.csv'*/
		String msFile = new File(mergeScenarioFile).text
		String [] lines = msFile.split('\n')
		for(int i = 1; i< lines.length;  i++){
			ArrayList<Boolean> bools = hasRealConflicts(lines[i])
			if(bools.get(0)){
				ms_wfp++
			}
			if(bools.get(1)){
				ms_wds++
			}
			if(bools.get(2)){
				ms_wcl++
			}

		}
		result.add(new Integer(ms_wfp))
		result.add(new Integer(ms_wds))
		result.add(new Integer(ms_wcl))
		return result
	}

	public static ArrayList<Boolean> hasRealConflicts(String line){
		ArrayList<Boolean> result = new ArrayList<Boolean>()
		boolean hasRealConflicts, ms_wds, ms_wcl = false
		
		String[] data = line.split(',')
		int i = 7
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			if(!c.toString().equals(SSMergeConflicts.NOPATTERN.toString())){
				int total = Integer.parseInt(data[i].trim())
				i++
				int ds = Integer.parseInt(data[i].trim())
				i++
				int cl = Integer.parseInt(data[i].trim())
				i++
				int ifp = Integer.parseInt(data[i].trim())
				int realConflicts = total - ds - cl + ifp
				int hasNonDSConflicts = total - ds
				int hasNonCLConflicts = total - cl
				if(realConflicts > 0){
					hasRealConflicts = true
				}
				if(hasNonDSConflicts > 0){
					ms_wds = true
				}
				if(hasNonCLConflicts > 0){
					ms_wcl = true
				}
				
				i++
			}

		}
		result.add(new Boolean(hasRealConflicts))
		result.add(new Boolean(ms_wds))
		result.add(new Boolean(ms_wcl))
		return result
	}



	public static void writeFileMetricsCSV(){
		File file = new File('/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv')
		File out = new File('filesMetrics.csv')
		out.delete()
		out = new File('filesMetrics.csv')
		String line = 'Project,Total_files,Files_merged,Files_with_conflicts\n'
		out.append(line)

		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			if(!projectName.equals("Project")){
				line = this.computeFileMetrics(projectName)
				out.append(line + '\n')
				println line
			}
		}
	}

	public static String computeFileMetrics(String projectName){
		File out = new File('moreThan10.csv')

		if(!out.exists()){
			out.append('Project,MergedFiles,FilesWithConflict\n')
		}

		String result = ''
		int totalFiles, mergedFiles, filesWithConflicts
		int[] wasMoreThan90 = [0,0]
		String mergeScenarioFile = 'ResultData' + File.separator + projectName + File.separator +
				'MergeScenariosReport.csv'
		String msFile = new File(mergeScenarioFile).text
		String [] lines = msFile.split('\n')
		for(int i = 1; i< lines.length;  i++){
			String[] data = lines[i].split(', ')
			totalFiles = totalFiles + Integer.parseInt(data[1]) + Integer.parseInt(data[4])
			mergedFiles = mergedFiles + Integer.parseInt(data[5])
			filesWithConflicts = filesWithConflicts + Integer.parseInt(data[6])
			int[] r = this.checkPercentagePerMC(totalFiles, mergedFiles,filesWithConflicts)
			wasMoreThan90[0] =  wasMoreThan90[0] + r[0]
			wasMoreThan90[1] =  wasMoreThan90[1] + r[1]
		}
		String a = projectName + ',' + wasMoreThan90[0] +',' + wasMoreThan90[1]
		out.append(a + '\n')
		result = projectName + ',' +totalFiles + ',' + mergedFiles + ',' + filesWithConflicts
		return result
	}

	public static int[] checkPercentagePerMC(int totalFiles, int mergedFiles,int filesWithConflicts){
		int[] result = [0,0]
		int percentageMF = 0
		int percentageFWC =0
		if(totalFiles != 0){
			percentageMF = (mergedFiles/totalFiles)*100
			percentageFWC = (filesWithConflicts/totalFiles)*100
		}

		if(percentageMF >=10){
			result[0] = 1
		}
		if(percentageFWC>=10){
			result[1] = 1
		}

		return result
	}

	public static void writeProjectPatternsData(String analyzedprojectsList, String resultDataFolder){
		File analyzedProjects = new File(analyzedprojectsList)
		File finalFile = new File('projectsPatternData.csv')
		ConflictPrinter.setconflictReportHeader()
		String header = 'Project, Merge_Scenarios, Conflicting_Scenarios, ' +
				ConflictPrinter.getConflictReportHeader()
		finalFile.delete()
		finalFile.append(header + '\n')
		analyzedProjects.eachLine{
			String projectRepo = it
			String projectName = CSVAnalyzer.getProjectName(projectRepo)
			println projectRepo
			String projectReportPath = resultDataFolder + File.separator + projectName + File.separator +
					'ProjectReport.csv'
			File projectReport = new File(projectReportPath)
			String projectResult = projectReport.readLines().get(1)
			finalFile.append(projectResult + '\n')
		}
	}
	
	public static String getProjectName(String projectRepo){
		String[] projectData = projectRepo.split('/')
		String result = projectData[1].trim()
		return result
	} 
	
	public static void main(String[] args){
		CSVAnalyzer.writeRealConflictsCSV()
		//CSVAnalyzer.writeFileMetricsCSV()
		/*CSVAnalyzer.writeProjectPatternsData('/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/rodados',
				'/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/ResultData')*/
	}




}
