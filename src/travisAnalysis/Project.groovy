package travisAnalysis

import main.Extractor
import main.GremlinProject
import main.SSMergeConflicts

/**
 * @author paolaaccioly
 *
 */
class Project {

	String repo
	String name
	String mergeReport
	String mergeCommits
	String downloadPath
	String resultPath
	String commitsPath
	Extractor extractor

	/*this structure maps each commit to its associated builds, and
	 *  each build to its associated state and finished time*/
	Hashtable<String, Hashtable<String,ArrayList<String>>> travisAnalysis

	public Project(String repo, String mergeCommits, String mergeReport, String downloadPath){
		this.repo = repo
		this.setName()
		this.mergeCommits = mergeCommits
		this.mergeReport = mergeReport
		this.downloadPath = downloadPath
		this.cloneProject()

	}

	public void setName(){
		String[] temp = this.repo.split(Pattern.quote(File.separator))
		this.name = temp[1]
	}

	public analyzeMerges(){
		println 'loading conflict predictor analysis from project ' + this.name
		Hashtable<String, ArrayList<String>> mergeCommits = this.loadMergeCommitsFile()
		Hashtable<String, Boolean> merges = this.loadHasRealFSTMergeConflicts()
		/*run travis analysis*/
		println 'executing build and tests analysis from project ' + this.name
		this.runTravisAnalysis()

		File mergeReport = new File(this.mergeReport)
		String text = mergeReport.getText()
		String[] lines = text.split('\n')

		/*for each merge commit*/

		for(int i = 1; i < lines.length; i++){
			int totalMerges = lines.length - 1
			println 'analysing merge scenario [' + i + '] from [' + totalMerges + ']'
			String metrics = lines[i]
			String revName = metrics.split(',')[0]
			ArrayList<String> value = mergeCommits.get(revName)

			/*in case the merge commit was discarded due to jgit API internal problems*/
			if(value!=null){
				String parent1 = value.get(0)
				String parent2 = value.get(1)
				String sha = value.get(2)
				Boolean hasRealFSTMergeConflict = merges.get(revName)
				Hashtable<String, ArrayList<String>> commitBuilds = this.travisAnalysis.get(sha)

				MergeScenario merge = new MergeScenario(this.name, sha, parent1, parent2, metrics,
						this.downloadPath, commitBuilds, this.extractor, hasRealFSTMergeConflict)
				PrintBuildAndTestAnalysis.printMergeScenario(this.resultPath, merge.toString())

				/*if there are more than one merge commit with the same parents,
				 * remove the one collected on this iteration*/
				if(value.size > 3){
					value.remove(2)
					mergeCommits.put(revName, value)
				}
			}
		}

		println 'finished the analysis for project ' + this.name
	}

	public Hashtable<String, ArrayList<String>> loadMergeCommitsFile(){
		ArrayList<String> shas = new ArrayList<String>()
		Hashtable<String, ArrayList<String>> result = new Hashtable<String, ArrayList<String>>()
		File mergeCommits = new File(this.mergeCommits)
		String text = mergeCommits.getText()
		String[] lines = text.split('\n')

		/*for each merge commit*/
		for(int i = 1; i < lines.length; i++){
			String[] line = lines[i].split(',')
			String sha = line[0]

			/*stores all merge commits shas to use as input for travis analysis*/
			shas.add(sha)

			String parent1 = line[1]
			String parent2 = line[2]
			String revName = 'rev_' + parent1.substring(0,5) + '-' + parent2.substring(0,5)
			/*in case more than one merge commit has the same parents*/
			ArrayList<String> value = result.get(revName)
			if(value == null){
				value = new ArrayList<String>()
				value.add(parent1)
				value.add(parent2)
				value.add(sha)
			}else{
				value.add(sha)
			}
			result.put(revName, value)
		}

		this.printShas(shas)
		return result
	}

	public void runTravisAnalysis(){
		/*instantiate result variable*/
		this.travisAnalysis = new Hashtable<String, Hashtable<String,ArrayList<String>>>()

		/*run travis script*/
		boolean scriptExecuted = this.executeScript()

		if(scriptExecuted){
			/*read resulting csv*/
			this.readCSV()
		}else{
			println 'error while executing travis script'
		}


	}

	public boolean executeScript(){
		boolean result = false
		try{
			/*set command*/
			String projectClone = this.downloadPath + File.separator + this.name + File.separator +'git'
			String fullCommitsPath = System.getProperty("user.dir") + File.separator + this.commitsPath
			String command = "ruby travisBuildAnalysis.rb " + projectClone + " " + fullCommitsPath

			/*run command line*/
			Process process = Runtime.getRuntime().exec(command)
			process.waitFor()

			/*read script output and verify if it was executed correctly*/
			BufferedReader processIn = new BufferedReader(new InputStreamReader(process.getInputStream()))
			String line
			while ((line = processIn.readLine()) != null) {
				println line
				if(line.contains('Finish')){
					result = true
				}
			}
		}catch(Exception e){
			e.printStackTrace()
		}

		return result
	}

	public void readCSV(){
		File resultFile = new File('TravisResults' + File.separator + this.name + 'BUILDS.csv')
		if(resultFile.exists()){
			String text = resultFile.getText()
			String[] lines = text.split('\n')
			/*for each lines of the resulting csv*/
			for(int i = 1; i < lines.length; i++){
				String[] line = lines[i].split(',')
				String state = line[0]
				String commit = line[1]
				String buildId = line[2]
				String finished_at = line[3]
				Hashtable<String, ArrayList<String>> commitBuilds = this.travisAnalysis.get(commit)
				if(commitBuilds == null){
					commitBuilds = new Hashtable<String, ArrayList<String>>()
	
				}
				ArrayList<String> buildData = new ArrayList<String>()
				buildData.add(state)
				buildData.add(finished_at)
				commitBuilds.put(buildId, buildData)
				this.travisAnalysis.put(commit, commitBuilds)
	
			}
		}
		
	}

	public void printShas(ArrayList<String> shas){
		/*make dir*/
		this.resultPath = 'ResultData' + File.separator + this.name +
				File.separator + 'buildAndTest'

		/*create and print file*/
		this.commitsPath = this.resultPath + File.separator + 'commits'
		File dir = new File(commitsPath)
		dir.mkdirs()
		
		String filePath = this.commitsPath + File.separator + 'commits.csv'
		File file = new File(filePath)
		/*deletes file if it already exists*/
		file.delete()
		file = new File(filePath)
		String commits = 'Commits\n'
		for(String sha in shas){
			commits = commits + sha + '\n'
		}
		file.append(commits)
		
	}

	public void cloneProject(){
		GremlinProject project = new GremlinProject(this.name, this.repo, 'graphbase')
		extractor = new Extractor(project, this.downloadPath)
	}

	public Hashtable<String, Boolean> loadHasRealFSTMergeConflicts(){
		Hashtable<String, Boolean> result = new Hashtable<String, Boolean>()
		File conflictPredictor = new File(this.mergeReport)
		String path = conflictPredictor.getParent() + File.separator + 'MergeScenariosReport.csv'
		File file = new File(path)
		String text = file.getText()
		String[] lines = text.split('\n')
		for(int i = 1; i < lines.length; i++){
			String data = lines[i]
			String revName = data.split(', ')[0]
			Boolean hasRealFSTMergeConflicts = this.hasRealConflicts(data)
			result.put(revName, hasRealFSTMergeConflicts)
		}
		return result
	}
	
	private boolean hasRealConflicts(String line){
		boolean hasRealConflicts = false
		String[] data = line.split(', ')
		int i = 8
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			if(!c.toString().equals(SSMergeConflicts.NOPATTERN.toString())){
				int total = Integer.parseInt(data[i])
				i++
				int ds = Integer.parseInt(data[i])
				int realConflicts = total - ds
				if(realConflicts > 0){
					hasRealConflicts = true
				}
				i = i + 3
			}

		}

		return hasRealConflicts
	} 
	
	public String computeProjectSummary(){
		Hashtable<String, String> predictors = this.fillPredictors()
		File merge_result = new File (this.resultPath + File.separator + 'Merge_Scenario_Report.csv')
		String pSummary = ''
		for(String predictor : predictors.keySet()){
			int column = Integer.parseInt(predictors.get(predictor))
			String summary = predictor + ',' + this.computePredictorSummary(column, merge_result) + '\n'
			pSummary = pSummary + summary
		}
		return pSummary
	}

	public String computePredictorSummary(int column, File mergeScenariosReport){
		/*String header = 'total_merge_scenario,merge_scenarios,conflict_predictor,' +
		 'parents_build_passed,build_passed,test_passed\n'*/
		int merge_scenarios, conflict_predictor,
		parents_build_failed,build_passed,test_passed = 0
		
		String text = mergeScenariosReport.getText()
		String[] lines = text.split('\n')
		int total_merge_scenario = lines.length - 1
		String result = total_merge_scenario + ','
		/*for each analyzed merge scenario*/
		for(int i = 1; i < lines.length; i++){
			/*get string containing the merge scenario results*/
			String[] data = lines[i].split(',')

			/* if there is no fstMerge conflicts - excluding
			 * different spacing conflicts*/
			int hasRealFSTMergeConflicts = Integer.parseInt(data[3])
			int predictor = Integer.parseInt(data[column])

			if((hasRealFSTMergeConflicts==0) && (predictor >0)){
				merge_scenarios++
				conflict_predictor = conflict_predictor + predictor
				parents_build_failed = parents_build_failed + Integer.parseInt(data[6])
				build_passed = build_passed + Integer.parseInt(data[7])
				test_passed = test_passed + Integer.parseInt(data[8])
			}
		}
		int parents_build_passed = merge_scenarios - parents_build_failed
		result = result + merge_scenarios + ',' + conflict_predictor + ',' + parents_build_passed + ',' +
				build_passed + ',' + test_passed
		return result
	}
 
 
	/**
	 * @return a hashtable mapping the predictor's name to the
	 * column number in the merge scenario report
	 */

	private Hashtable<String, String> fillPredictors(){
		Hashtable<String, String> predictors = new ArrayList<String>()
		predictors.put('ncEditSameMC', '11')
		predictors.put('ncEditSameFd', '12')
		predictors.put('editDiffMC', '13')
		predictors.put('editDiffEditSame', '14')
		predictors.put('editDiffAddsCall', '15')
		predictors.put('editDiffEditSameAddsCall', '16')

		return predictors
	}
	
	public static void main (String[] args){
		Project project = new Project ('leusonmario/javaToy',
				'/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/conflictsAnalyzer/ResultData/javatoy/mergeCommits.csv',
				'/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/conflictsAnalyzer/ResultData/javatoy/ConflictPredictor_MS_Report.csv',
				'/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/downloads')
		project.analyzeMerges()

	}
	
	

}
