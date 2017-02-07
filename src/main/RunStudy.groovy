package main
import java.text.SimpleDateFormat
import java.util.HashMap;
import java.util.Hashtable

import util.CSVAnalyzer
import util.ConflictPredictorPrinter;

import org.apache.commons.io.FileUtils
import util.Util


/*this class is supposed to integrate all the 3 steps involved to run the study
 * gitminer/gremlinQuery/ConflictsAnalyzer
 */

class RunStudy {


	private String gitminerConfigProps = 'gitminerConfiguration.properties'
	private String projectName
	private String projectRepo
	private String gitminerLocation
	private String downloadPath
	private Hashtable<String, Conflict> projectsSummary

	public RunStudy(){
		ConflictPrinter.setconflictReportHeader()
	}

	public void run(String[] args){

		//read input files
		def projectsList = new File(args[0])
		updateGitMinerConfig(args[1])
		String projectsDatesFolder = ''
		//read project results (if available)
		try{
			projectsDatesFolder = args[2]
		}catch(Exception e){
			e.printStackTrace()
		}
		
		List<String> lines = projectsList.readLines()
		this.createResultDir()

		//lines.remove(0)
		//for each project
		lines.each() {

			//set project name
			setProjectNameAndRepo(it)

			//set projectPeriodsList
			//List<ProjectPeriod> periods = getProjectPeriods(projectsDatesFolder)

			

			/*1run gitminer*/ 
			//String graphBase = runGitMiner()
			
			/*2 use bases from gitminer*/ 
			//String graphBase = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
			//ArrayList<MergeCommit> listMergeCommits = runGremlinQuery(graphBase)
			
			/*3 read mergeCommits.csv sheets*/ 
			/*String graphBase = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
			ArrayList<MergeCommit> listMergeCommits = this.readMergeCommitsSheets(projectsDatesFolder)*/
	
			/*4 set listMergeCommits with commits that i want to analyze separately*/
			/*MergeCommit mc = new MergeCommit()
			mc.setSha('02e79d6b153d1356bc0323084846be12980a810e')
			mc.setParent1('1ebc8a2a72528eb6988fca749dfd256df712eb08')
			mc.setParent2('197878ae7573da108f07abcea8771934ecc45d42')
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
			Date d = sdf.parse("22/10/2014")
			mc.setDate(d)
			ArrayList<MergeCommit> listMergeCommits = new ArrayList<MergeCommit>()
			listMergeCommits.add(mc)*/
			
			//create project and extractor
			String graphBase = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
			Extractor extractor = this.createExtractor(this.projectName, graphBase)
			Project project = new Project(this.projectName, null)

			//for each merge scenario, clone and run SSMerge on it
			ArrayList<MergeCommit> listMergeCommits = this.getListMergeCommit(this.projectName)
			ConflictPrinter.printMergeCommitsList(this.projectName, listMergeCommits)
			analyseMergeScenario(listMergeCommits, extractor, project)

			//print project report and call R script
			ConflictPrinter.printProjectData(project)
			ConflictPredictorPrinter.printProjectReport(project)
			//this.callRScript()
		}

	}
	
	private ArrayList<MergeCommit> getListMergeCommit(String projectName){
		ArrayList<MergeCommit> result = new ArrayList<MergeCommit>()
		String projectClonePath = this.downloadPath + File.separator + this.projectName +
		File.separator + 'git'
		MergeCommitsRetriever m = new MergeCommitsRetriever(projectClonePath)
		result = m.retrieveMergeCommits()
		return result
	}
	
	private ArrayList<MergeCommit> readMergeCommitsSheets(String resultDataFolder){
		ArrayList<MergeCommit> result = new ArrayList<MergeCommit>()
		String filePath = resultDataFolder + File.separator + this.projectName + File.separator + 'mergeCommits.csv'
		File mergeCommitsFile = new File(filePath)
		if(mergeCommitsFile.exists()){
			mergeCommitsFile.eachLine {
				if(!it.startsWith('Merge')){
					MergeCommit mc = this.readMergeCommit(it.trim())
					result.add(mc)
				}
			}
		}

		return result
	}
	
	private MergeCommit readMergeCommit(String mc){
		MergeCommit result = new MergeCommit()
		String [] tokens = mc.split(',')
		result.sha = tokens[0].trim()
		result.parent1 = tokens[1].trim()
		result.parent2 = tokens[2].trim()
		return result
	}

	private ArrayList<ProjectPeriod> getProjectPeriods(String projectsDatesFolder) {

		ArrayList<ProjectPeriod> periods = new ArrayList<ProjectPeriod>()
		def projectDatesFile = new File(projectsDatesFolder + File.separator + this.projectName + ".txt")
		List<String> projectPeriodsList = projectDatesFile.readLines()
		projectPeriodsList.remove(0)

		projectPeriodsList.each(){ infoLine ->
			String[] projectInfo = infoLine.split(",")
			Date startDate = null
			Date endDate = null
			String binPath = "/bin"
			String srcPath = "/src"
			String libPaths = null
			String buildSystem = null
			if(projectInfo.length > 0 && !projectInfo[0].trim().equals(""))
			{
				startDate = Date.parse('dd/MM/yyyy', projectInfo[0])
			}

			if(projectInfo.length > 1 && !projectInfo[1].trim().equals(""))
			{
				endDate = Date.parse('dd/MM/yyyy', projectInfo[1])
			}
			if(projectInfo.length > 2 && !projectInfo[2].trim().equals("")){
				binPath = projectInfo[2].trim()
			}

			if(projectInfo.length > 3 && !projectInfo[3].trim().equals("")){
				srcPath = projectInfo[3].trim()
			}

			if(projectInfo.length > 4 && !projectInfo[4].trim().equals(""))
			{
				libPaths = projectInfo[4].trim()
			}

			if(projectInfo.length > 5 && !projectInfo[5].trim().equals(""))
			{
				buildSystem = projectInfo[5].trim()
			}
			periods.add(new ProjectPeriod(startDate, endDate, binPath, srcPath, libPaths, buildSystem))
		}
		return periods
	}

	private void createResultDir(){
		File resultDir = new File ('ResultData')
		if(!resultDir.exists()){
			resultDir.mkdirs()
		}
	}

	private void analyseMergeScenario(ArrayList listMergeCommits, Extractor extractor,
			Project project) {
		//if project execution breaks, update current with next merge scenario number
		int current = 0;
		int end = listMergeCommits.size()

		//List<ProjectPeriod> periods = project.getProjectPeriods()
		//String reportsPath = new File(downloadPath).getParent() + File.separator + "reports" + File.separator + project.name

		//for each merge scenario analyze it
		while(current < end){

			int index = current + 1;
			println 'Merge scenario [' + index + '] from a total of [' + end +
					'] merge scenarios\n'

			MergeCommit mc = listMergeCommits.get(current)

			//MatchingProjectPeriod p = this.getPeriodMatch(periods, mc)


			//if(p.periodMatch){
				println 'Analyzing merge scenario...'

				/*download left, right, and base revisions, performs the merge and saves in a
				 separate file*/
				ExtractorResult mergeResult = extractor.extractCommit(mc)

				String revisionFile = mergeResult.getRevisionFile()

				if(!revisionFile.equals("")){

					//run ssmerge and conflict analysis
					SSMergeResult ssMergeResult = runConflictsAnalyzer(project, revisionFile,
							mergeResult.getNonJavaFilesWithConflict().isEmpty())

					boolean hasConflicts = ssMergeResult.getHasConflicts()
					println hasConflicts
					/*if(!hasConflicts){*/
						//get line of the files containing methods for joana analysis
						Map<String, ArrayList<EditSameMC>> filesWithMethodsToJoana =
								ssMergeResult.getFilesWithMethodsToJoana()
						/*if(filesWithMethodsToJoana.size() > 0)
						{*/
							/*println index + ", " + filesWithMethodsToJoana.keySet()
							String revPath = revisionFile.replace(".revisions", "")
							String reportsFilePath = reportsPath + File.separator + (new File(revPath).getName())
							File reportsRevDir = new File(reportsFilePath)
							reportsRevDir.deleteDir()
							reportsRevDir.mkdirs()
							File emptyContribs = new File(reportsFilePath + File.separator + "emptyContributions.txt")
							emptyContribs.createNewFile()
							//Map ssmerge objects to joana objects
							Map<String, ModifiedMethod> methods = getJoanaMap(emptyContribs, filesWithMethodsToJoana)
							if(emptyContribs.length() == 0)
							{
								emptyContribs.delete()
							}*/
							/*if(methods.size() > 0)
							{*/

								/*String revGitPath = revPath + File.separator + "git"
								File revGitFile = new File(revGitPath)

								def repoDir = new File(downloadPath +File.separator+ projectName + File.separator + "git")
								FileUtils.copyDirectory(new File(revPath), revGitFile)
								copyGitFiles(repoDir, repoDir, revGitFile)

								File buildResultFile = new File(reportsFilePath + File.separator + "build_report.txt")
								buildResultFile.createNewFile()
								boolean[] buildResult = build(p.period.getBuildSystem(),revGitPath, buildResultFile)
								ConflictPrinter.printMergeScenariosBuildResult(ssMergeResult.mergeScenarioName, buildResult)*/
								/*if(build(p.period.getBuildSystem(),revGitPath, buildResultFile))
								{	
									//run system tests
									
									//call joana analysis
									/*println "Calling Joana"
									JoanaInvocation joana = new JoanaInvocation(revGitPath, methods, p.period.getBinPath(),
											p.period.getSrcPath(), p.period.getLibPaths(), reportsFilePath)
									joana.run()
								}*/
							/*}*/
						/*}*/
					/*}*/
				}
			//}
			//increment current
			current++

		}

	}

	private MatchingProjectPeriod getPeriodMatch(List<ProjectPeriod> periods, MergeCommit mc){
		boolean periodMatch = false

		int currentPeriod = 0
		ProjectPeriod period = null
		Date startDate = null
		Date finalDate = null

		while(currentPeriod < periods.size() && !periodMatch)
		{
			period = periods[currentPeriod]
			startDate = period.getStartDate()
			finalDate = period.getEndDate()
			periodMatch = (startDate == null || mc.date.clearTime() >= startDate) &&
					(finalDate == null || mc.date.clearTime() <= finalDate)
			if(!periodMatch)
			{
				currentPeriod++
			}
		}

		MatchingProjectPeriod result = new MatchingProjectPeriod(periodMatch, period)
		return result
	}

	/*private Map getJoanaMap(File emptyContributions,Map filesWithMethodsToJoana) {
		Map<String, ModifiedMethod> methods = new HashMap<String, ModifiedMethod>()
		for(String file : filesWithMethodsToJoana.keySet()) {
			for(EditSameMC method : filesWithMethodsToJoana.get(file)){
				if(method.leftLines.size > 0 && method.rightLines.size > 0)
				{
					List<String> constArgs;
					def constructor = method.getConstructor()
					if(constructor != null)
					{
						constArgs = Util.getArgs(Util.simplifyMethodSignature(constructor.getName()));
					}else {
						constArgs = new ArrayList<String>()
					}
					methods.put(method.getSignature(), new ModifiedMethod(method.getSignature(), constArgs, method.getLeftLines(), method.getRightLines(), method.getImportsList()))
				}else{
					println "One or more empty contributions on: "+method.getSignature()
					emptyContributions.append("One or more empty contributions on: "+method.getSignature()+"\n")
					emptyContributions.append("   Left Contribution:"+method.leftLines+"\n")
					emptyContributions.append("   Right Contribution:"+method.rightLines+"\n")
					emptyContributions.append("\n")
				}
			}
		}
		return methods
	}*/

	private def copyGitFiles(File baseDir, File srcDir, File destDir)
	{
		String basePath = baseDir.getAbsolutePath()
		String destPath = destDir.getAbsolutePath()
		File[] srcFiles = srcDir.listFiles()
		for(File file : srcFiles)
		{
			if(file.getName().contains(".git"))
			{
				if(file.isFile())
				{
					FileUtils.copyFile(file, new File(file.getAbsolutePath().replace(basePath, destPath)))
				}else if(file.isDirectory())
				{
					FileUtils.copyDirectory(file, new File(file.getAbsolutePath().replace(basePath, destPath)))
				}
			}
		}
	}

	private boolean[]  build(String fullBuildSystem, String revGitPath, File buildResultFile) {
		println "Building..."
		boolean[] result = [false,false]
		int lastSeparator = fullBuildSystem.lastIndexOf(File.separator) + 1
		String buildSystem = fullBuildSystem.substring(lastSeparator)
		String buildSystemLocation = fullBuildSystem.substring(0, lastSeparator)
		def buildCmd = buildSystemLocation + File.separator
		if(buildSystem.equals("gradlew"))
		{
			def gradlewPath = revGitPath + File.separator+"gradlew"
			buildCmd = "chmod +x "+gradlewPath + " && "+gradlewPath+" build -p"+revGitPath  /*+" -x test"*/
		}else if(buildSystem.equals("gradle"))
		{
			buildCmd += "gradle build -p"+revGitPath/*+" -x test"*/
		}else if(buildSystem.equals("ant"))
		{
			buildCmd += "ant build -buildfile "+ revGitPath + File.separator +"build.xml"
		}else if(buildSystem.equals("mvn")){
		buildCmd += "mvn compile "+ revGitPath + File.separator +"pom.xml"
		}
		//start here
		ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",buildCmd);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader buffer 	= new BufferedReader(new InputStreamReader(p.getInputStream()));
		String currentLine 		= "";
		List<String> buildLines = new ArrayList<String>()
		while ((currentLine=buffer.readLine())!=null) {
			buildLines.add(currentLine)
			buildResultFile.append(currentLine+"\n")
			println currentLine
		}
		int i = buildLines.size() - 1
		while(i >= 0 )
		{
			if(buildLines.get(i).equals("BUILD SUCCESSFUL")){
				result[0] = true
			}
			
			if(buildLines.get(i).contains("There were failing tests")){
				result[1] = true
			}
			
			i--;
		}
		return result
	}
	
	
	
	private Extractor createExtractor(String projectName, String graphBase){
		GremlinProject gProject = new GremlinProject(this.projectName,
				this.projectRepo, graphBase)
		Extractor extractor = new Extractor(gProject, this.downloadPath)

		return extractor
	}

	public Hashtable<String, Conflict> getProjectsSummary(){
		return this.projectsSummary
	}

	public void updateGitMinerConfig(String configFile){
		Properties gitminerProps =  new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())


		Properties configProps = new Properties()
		File propsFile = new File(configFile)
		configProps.load(propsFile.newDataInputStream())

		this.gitminerLocation = configProps.getProperty('gitminer.path')
		this.downloadPath = configProps.getProperty('downloads.path')
		String graphDb = this.gitminerLocation + File.separator + 'graph.db'
		String repo_Loader = this.gitminerLocation + File.separator + 'repo_loader'

		gitminerProps.setProperty('net.wagstrom.research.github.dburl', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.localStore', repo_Loader)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.dburl', graphDb)

		gitminerProps.setProperty('net.wagstrom.research.github.login', configProps.getProperty('github.login'))
		gitminerProps.setProperty('net.wagstrom.research.github.password', configProps.getProperty('github.password'))
		gitminerProps.setProperty('net.wagstrom.research.github.email', configProps.getProperty('github.email'))
		gitminerProps.setProperty('net.wagstrom.research.github.token', configProps.getProperty('github.token'))

		gitminerProps.store(gitminerPropsFile.newWriter(), null)

	}

	public void setProjectNameAndRepo(String project){
		String[] projectData = project.split('/')
		this.projectName = projectData[1].trim()
		this.projectRepo = project
		println "Starting project " + this.projectName
	}


	public String runGitMiner(){
		updateProjectRepo()
		println "Running gitminer"
		runGitminerCommand('./gitminer.sh')
		runGitminerCommand('./repository_loader.sh')
		String graphBase = renameGraph()
		println "Finished running gitminer"
		return graphBase
	}


	private String renameGraph(){

		String oldFile = this.gitminerLocation + File.separator + 'graph.db'
		String newFile = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
		new File(oldFile).renameTo(new File(newFile))

		return newFile
	}

	public void runGitminerCommand(String command){
		String propsFile = new File("").getAbsolutePath() + File.separator + this.gitminerConfigProps
		ProcessBuilder pb = new ProcessBuilder(command, "-c", propsFile)
		pb.directory(new File(this.gitminerLocation))
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void updateProjectRepo(){
		Properties gitminerProps = new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())
		gitminerProps.setProperty('net.wagstrom.research.github.projects', this.projectRepo)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', this.projectRepo)
		gitminerProps.store(gitminerPropsFile.newWriter(), null)
	}

	public ArrayList<MergeCommit> runGremlinQuery(String graphBase){
		println "starting to query the gremlin database and download merge revision"
		GremlinQueryApp gq = new GremlinQueryApp()
		ArrayList<MergeCommit> listMergeCommits = gq.run(projectName, projectRepo, graphBase)
		return listMergeCommits
	}

	public SSMergeResult runConflictsAnalyzer(Project project, String revisionFile, boolean resultGitMerge){
		println "starting to run the conflicts analyzer on revision " + revisionFile
		SSMergeResult result = project.analyzeConflicts(revisionFile, resultGitMerge)
		return result
	}

	public void callRScript(){

		CSVAnalyzer.writeRealConflictsCSV()
		String propsFile = "resultsScript.r"
		ProcessBuilder pb = new ProcessBuilder("Rscript", propsFile)
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args){
		RunStudy study = new RunStudy()
		/*String[] files= ['projectsList', 'configuration.properties', 
			'/home/ines/Dropbox/experiment/ResultData']
		*/	
		String[] files= ['projectsList', 'configuration.properties',
			'/Users/paolaaccioly/Desktop/conflictPredictor/ResultData']
			
		study.run(files)
		//println study.build("/usr/local/bin/ant", "/Users/Roberto/Documents/UFPE/Msc/Projeto/projects/temp/voldemort", new File("/Users/Roberto/Documents/UFPE/Msc/Projeto/projects/temp/report.txt"))
	}

}
