package normalization

import main.Extractor
import main.ExtractorResult
import main.GremlinProject
import main.GremlinQueryApp
import main.MergeCommit
import main.MergeCommitsRetriever;
import main.SSMergeNode

class NormalizedProject {

	String name

	int numberOfChangesInsideMethodsChunks

	int numberOfChangesInsideMethodsLines

	Map<String, Integer> changesSummary

	ArrayList<EvoScenario> evoScenarios

	public NormalizedProject(String n){
		this.name = n
		this.evoScenarios = new ArrayList<EvoScenario>()
		this.initializeChangesSummary()
	}

	public void initializeChangesSummary(){
		this.changesSummary = new HashMap<String, Integer>()
		for(SSMergeNode node in SSMergeNode){
			this.changesSummary.put(node.toString(), 0)
		}
	}

	public void computeNumberOfChanges(String projectRepo, String gitMinerDir, String downloadDir){
		//get commits list
		//ArrayList<MergeCommit> temp = this.runGremLinQuery(projectRepo, gitMinerDir)
		//ArrayList<MergeCommit> commits = this.filterMergeCommits(temp)
		
		//create extractor
		Extractor extractor = this.createExtractor(this.name, projectRepo, downloadDir)
		
		//get commits list
		String clonePath = downloadDir + File.separator + this.name + File.separator +
		'git'
		println 'retrieving commit list of project ' + this.name
		MergeCommitsRetriever m = new MergeCommitsRetriever(clonePath, '')
		ArrayList<MergeCommit> commits = m.retrieveNonMergeCommits(projectRepo)
		NormalizedConflictPrinter.printCommitList(this.name, commits)
		println 'commit list retrieved. Starting to analyse commits changes'

		//analyse commit scenarios
		this.analyseEvoScenarios(commits, extractor)
	}
	
	public ArrayList<MergeCommit> filterMergeCommits(ArrayList<MergeCommit> mergeCommits){
		ArrayList<MergeCommit> commits = new ArrayList<MergeCommit>()
		for(MergeCommit mc : mergeCommits){
			if(mc.parent2.equals('')){
				commits.add(mc)
			}
		}
		
		return commits
	}

	public void analyseEvoScenarios(ArrayList<MergeCommit> commits, Extractor extractor ){

		int current = 0 //jumps the first project commit
		int end = commits.size()

		//if project execution breaks, update current with next merge scenario number
		while(current < end){
			int index = current + 1
			println 'Analyzing commit [' + index + '] from a total of [' + end +
					'] merge scenarios\n'

			MergeCommit mc = commits.get(current)

			if(!mc.parent1.equals('')){
				this.analyseEvoScenario(mc, extractor)
			}

			current++

		}
	}

	public void analyseEvoScenario(MergeCommit scenario, Extractor extractor){
		ExtractorResult er = extractor.extractEvoScenario(scenario)
		String revisionFile = er.getRevisionFile()
		if(!revisionFile.equals("")){
			this.analyseChanges(scenario, er)
		}
	}
	
	
	public void analyseChanges (MergeCommit mc, ExtractorResult er){
		EvoScenario scenario = new EvoScenario(mc, er)
		scenario.analyseChanges()
		this.evoScenarios.add(scenario)
		this.updateProjectMetrics(scenario)
		NormalizedConflictPrinter.printEvoScenarioReport(scenario, this.name)
		NormalizedConflictPrinter.printNormalizedProjectIteration(this)
		if(scenario.hasConflictsOnNonJavaFiles){
			NormalizedConflictPrinter.printMergeScenariosWithConflictsOnNonJavaFiles(scenario, this.name)
		}
		scenario.deleteEvoDir()
	}

	private void updateProjectMetrics(EvoScenario evo){

		//update changesSummary
		for(SSMergeNode node in SSMergeNode){
			int value = evo.changesSummary.get(node.toString())
			int newValue = value + this.changesSummary.get(node.toString())
			this.changesSummary.put(node.toString(), newValue)
		}

		//update other metrics
		this.numberOfChangesInsideMethodsChunks = this.numberOfChangesInsideMethodsChunks +
				evo.numberOfChangesInsideMethodsChunks

		this.numberOfChangesInsideMethodsLines = this.numberOfChangesInsideMethodsLines +
				evo.numberOfChangesInsideMethodsLines
	}

	public String toString(){
		String result = this.name + ', ' + this.evoScenarios.size() + ', '

		//print changesSummary
		for(SSMergeNode node in SSMergeNode.values()){
			result = result + this.changesSummary.get(node.toString()) + ', '
		}

		//print other metrics
		result = result + this.numberOfChangesInsideMethodsChunks + ', ' +
				this.numberOfChangesInsideMethodsLines

		return result
	}

	public ArrayList<MergeCommit> runGremLinQuery(String projectRepo, String gitMinerDir){
		ArrayList<MergeCommit> result = new ArrayList<MergeCommit>()
		String gitminerBase = gitMinerDir + File.separator + this.name + 'graph.db'

		println "starting to query the gremlin database and download merge revision"
		GremlinQueryApp gq = new GremlinQueryApp()
		result = gq.runAllCommits(this.name, projectRepo, gitminerBase)


		return result
	}

	private Extractor createExtractor(String projectName, String projectRepo, String downloadDir){
		GremlinProject gProject = new GremlinProject(this.name, projectRepo)
		Extractor extractor = new Extractor(gProject, downloadDir)

		return extractor
	}

}
