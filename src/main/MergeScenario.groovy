package main


import java.io.File
import java.util.ArrayList;
import java.util.List;
import java.util.Map
import java.util.Observable
import java.util.regex.Pattern

import org.apache.commons.io.FileUtils

import util.CompareFiles
import util.ConflictPredictorPrinter;
import br.ufpe.cin.app.JFSTMerge
import br.ufpe.cin.mergers.NodeAndPath;
import br.ufpe.cin.mergers.SemistructuredMerge
import br.ufpe.cin.mergers.util.MergeContext;
import de.ovgu.cide.fstgen.ast.FSTNode
import de.ovgu.cide.fstgen.ast.FSTNonTerminal
import de.ovgu.cide.fstgen.ast.FSTTerminal


/**
 * @author paolaaccioly
 *
 */
class MergeScenario implements Observer {

	private String path

	private String name

	private ArrayList<MergedFile> mergedFiles

	private Map<String,Conflict> mergeScenarioSummary

	private boolean hasConflicts

	private boolean hasPredictors

	private CompareFiles compareFiles

	private JFSTMerge fstGenMerge

	private Map<String, Integer> sameSignatureCMSummary

	private int possibleRenamings

	private int filesAddedByOneDev

	private boolean gitMergeHasNoConflicts

	private Map<String, ArrayList<ConflictPredictor>> filesWithConflictPredictors

	private ConflictPredictorFactory predictorFactory

	private int methodsWithConflicts

	private Map<String, Integer> editSameMCTypeSummary

	private MergeCommit mc;

	private String replayedMergeSha;

	public List<FSTNode> deletedBaseNodes;
	
	private String baseName;
	
	private ArrayList<String> fileNotFoundConflicts;

	public MergeScenario(String path, boolean resultGitMerge){
		this.path = path
		this.gitMergeHasNoConflicts = resultGitMerge
		this.setName()
		this.setBaseName()
		//this.removeVarArgs()
		this.hasConflicts = false
		this.hasPredictors = false
		this.createMergeScenarioSummary()
		this.createSameSignatureCMSummary()
		this.createEditSameMCTypeSummary()
		this.setMergedFiles()
		this.filesWithConflictPredictors = new HashMap<String, ArrayList<ConflictPredictor>>()
		this.predictorFactory = new ConflictPredictorFactory()
	}
	
	public ArrayList<String> getFileNotFoundConflicts() {
		return this.fileNotFoundConflicts;
	}



	public void setFileNotFoundConflicts(ArrayList<String> fileNotFoundConflicts) {
		this.fileNotFoundConflicts = fileNotFoundConflicts;
	}



	public void setBaseName() {
		File f = new File(this.path)
		String text = f.getText()
		String[] lines = text.split('\n')
		this.baseName = lines[1].split('_')[2]
	}

	public void createSameSignatureCMSummary(){
		this.sameSignatureCMSummary = ConflictSummary.initializeSameSignatureCMSummary()
	}

	public void createEditSameMCTypeSummary(){
		this.editSameMCTypeSummary = ConflictSummary.initializeEditSameMCTypeSummary()
	}

	public void setMergedFiles(){
		this.compareFiles = new CompareFiles(this.path)
		this.compareFiles.ignoreFilesWeDontMerge()
		this.mergedFiles = this.compareFiles.getFilesToBeMerged()
	}

	public ArrayList<MergedFile> getMergedFiles(){
		return this.mergedFiles
	}

	public HashMap<String, Conflict> getMergeScenarioSummary(){
		return this.mergeScenarioSummary
	}

	public void setName(){
		String [] temp = this.path.split(Pattern.quote(File.separator))
		String revFile = temp[temp.length -1]
		this.name = revFile.substring(0, revFile.length()-10)
	}

	public String getName(){
		return this.name
	}

	public void analyzeConflicts(){

		this.runSSMerge()
		this.computeMethodsWithConflicts()
		this.assignLeftAndRight()
		this.checkForMethodsReferences()
		//this.compareFiles.restoreFilesWeDontMerge()

	}


	/**
	 * this methods compute the number of methods 
	 * edited by both revisions that ended up 
	 * in conflicts
	 */
	public void computeMethodsWithConflicts(){
		this.methodsWithConflicts = 0
		for(MergedFile file : this.mergedFiles){
			this.methodsWithConflicts = this.methodsWithConflicts +
					file.methodsWithConflicts
		}

	}

	public void assignLeftAndRight(){
		for(String filePath : this.filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> methods = this.filesWithConflictPredictors.get(filePath)

			for(ConflictPredictor method : methods ){
				method.assignLeftAndRight()
			}
		}
	}

	public void checkForMethodsReferences(){
		/*for each file containing conflict predictors*/
		for(String filePath : this.filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = this.filesWithConflictPredictors.get(filePath)

			/*for each conflict predictor on that file*/
			for(ConflictPredictor predictor : predictors ){

				/*if the predictor is an edited method
				 * (not considering the different spacing predictors*/
				if((predictor instanceof EditDiffMC || predictor instanceof EditSameMC) &&
				!(predictor.diffSpacing)){

					/*searches in the conflict predictor list if any other edited method calls this method*/
					predictor.lookForReferencesOnConflictPredictors(this.filesWithConflictPredictors)

				}
			}
		}
		this.removeFilesWithoutPredictors()
	}

	public void removeFilesWithoutPredictors() {
		ArrayList<String> filesWithNoPredictors = new ArrayList<String>()
		for(String filePath : this.filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = this.filesWithConflictPredictors.get(filePath)
			/*this arraylist saves the list of editdiffmc without any call references on edited methods*/
			ArrayList<ConflictPredictor> noReference = new ArrayList<ConflictPredictor>()
			for(ConflictPredictor predictor : predictors ){
				if((predictor instanceof EditDiffMC) && predictor.predictors.isEmpty()){
					noReference.add(predictor)
				}
			}
			predictors.removeAll(noReference)
			if(this.filesWithConflictPredictors.get(filePath).isEmpty()){
				filesWithNoPredictors.add(filePath)
			}
		}
		/*Remove files without predictors*/
		for(String file : filesWithNoPredictors){
			this.filesWithConflictPredictors.remove(file)
		}
		if(!this.filesWithConflictPredictors.isEmpty()){
			this.hasPredictors = true
		}
	}

	public void deleteMSDir(){
		String msPath = this.path.substring(0, (this.path.length()-26))
		File dir = new File(msPath)
		try {
			FileUtils.forceDelete(dir)
		}catch(Exception e) {
			//e.printStackTrace()
		}
		
		/*boolean deleted = dir.deleteDir()
		if(deleted){
			println 'Merge scenario ' + this.path + ' deleted!'
		}else{

			println 'Merge scenario ' + this.path + ' not deleted!'
		}*/
	}

	public void runSSMerge(){
		/*fstGenMerge.getMergeVisitor().addObserver(this)
		 String[] files = ["--expression", this.path]
		 fstGenMerge.run(files)*/
		this.fstGenMerge = new JFSTMerge()
		this.fstGenMerge.getSemistructuredMerge().addObserver(this)
		this.fstGenMerge.mergeRevisions(this.path)
	}


	public void createMergeScenarioSummary(){
		this.mergeScenarioSummary = ConflictSummary.initializeConflictsSummary()
	}

	public void updateMergeScenarioSummary(Conflict conflict){
		this.mergeScenarioSummary = ConflictSummary.updateConflictsSummary(this.mergeScenarioSummary
				, conflict)
		this.possibleRenamings = this.possibleRenamings + conflict.getPossibleRenaming()
	}

	public boolean getHasConflicts(){
		return this.hasConflicts
	}

	public void removeVarArgs(){
		String OS = System.getProperty("os.name").toLowerCase()
		String sSed = ""
		if (OS.contains('mac')){
			sSed = "xargs sed -i \'\' s/\\.\\.\\./[]/g"
		}else if(OS.contains('linux')){
			sSed = "xargs sed -i s/\\.\\.\\./[]/g"
		}
		String msPath = this.path.substring(0, (this.path.length()-26))
		String command = "grep -rl ... " + msPath
		def procGrep = command.execute()
		def procSed = sSed.execute()
		procGrep | procSed
		procSed.waitFor()
	}

	@Override
	public void update(Observable o, Object arg) {

		if(o instanceof SemistructuredMerge && arg instanceof NodeAndPath){

			FSTTerminal node = (FSTTerminal) arg.getNode();
			String filePath = this.getFilePath(arg.getFilePath())
			String[] tokens = this.name.split("_")[1].split('-')
			String mergeDir = "rev_rev_left_" +  tokens[0] + "-rev_right_" + tokens[1]
			String revLeft = "rev_left_" + tokens[0]
			filePath = filePath.replaceFirst(revLeft, mergeDir)
			if(!node.getType().contains("-Content")){

				if(this.isAConflictPredictor(node)){

					this.collectConflictPredictor(node, filePath)

				}else{
					
					this.createConflict(node, filePath, mergeDir)

				}


			}
		}else if(o instanceof SemistructuredMerge && arg instanceof MergeContext){
			this.deletedBaseNodes = arg.deletedBaseNodes;
		}else if(o instanceof SemistructuredMerge && arg instanceof String) {
			if(this.fileNotFoundConflicts == null) {
				this.fileNotFoundConflicts = new ArrayList<String>()
			}
			this.fileNotFoundConflicts.add(arg)
		}
	}

	private String getFilePath(String leftPath){
		String filePath = "";
		String left = this.name.substring(4,9);
		String right = this.name.substring(this.name.length()-5, this.name.length());
		String revision = "rev_rev_left_" + left + "-rev_right_" + right
		String leftRev = "rev_left_" + left
		String baseRev = "rev_base_" + ''
		String rightRev = "rev_right_" + right
		filePath = leftPath.replaceFirst(leftRev, revision)
		filePath = leftPath.replaceFirst(baseRev, revision)
		filePath = leftPath.replaceFirst(rightRev, revision)
		return filePath;
	}

	private void collectConflictPredictor(FSTTerminal node, String filePath){
		//if(!this.isABadParsedNode(node)){
		identifyConflictPredictor(node, this.path, filePath)
		//}
	}

	private boolean isABadParsedNode(FSTTerminal node){
		boolean isABadParsedNode = false
		ArrayList<String> parsedErrors = this.fstGenMerge.getSemistructuredMerge().getParserErrors()
		for(String fPath : parsedErrors){
			String classname = this.getClassName(node)
			File f = new File(fPath)
			String fileName = f.name
			if(fileName.contains(classname)){
				isABadParsedNode = true
			}
		}

		return isABadParsedNode
	}

	private String getClassName(FSTNode node){

		String type = node.getType()
		if(type.equals('ClassDeclaration') || type.equals('EnumDecl') ||
		type.equals('AnnotationTypeDeclaration') || type.equals('ClassOrInterfaceDecl')){
			return node.getName()
		}else{
			this.getClassName(node.getParent())
		}
	}

	private void identifyConflictPredictor(FSTTerminal arg, String mergeScenarioPath, String filePath) {
		ConflictPredictor predictor = this.predictorFactory.createConflictPredictor(arg, mergeScenarioPath, filePath)

		/*if this predictor is not null or belongs to type EditDiffMC and it is a
		 * different spacing conflict predictor do not add it to the list
		 * of conflict predictors*/	
		if(predictor != null){
			if(!(predictor instanceof EditDiffMC && predictor.diffSpacing)){

				String predictorFilePath = predictor.getFilePath()
				ArrayList<ConflictPredictor> file = this.filesWithConflictPredictors.get(predictorFilePath)

				if(file == null){
					file = new ArrayList<ConflictPredictor>()

				}

				file.add(predictor)
				this.filesWithConflictPredictors.put(predictorFilePath, file)
			}
		}

	}

	public void createConflict(FSTTerminal node, String filePath, String mergeDir){
		//if(!this.isABadParsedNode(node)){
		Conflict conflict = new Conflict(node, filePath);
		this.matchConflictWithFile(conflict, mergeDir)
		this.updateMergeScenarioSummary(conflict)
		if(!this.hasConflicts){
			this.hasConflicts = true
			this.removeNonMCBaseNodes(this.deletedBaseNodes)
		}
		//}
	}

	private void updateSameSignatureCMSummary(String cause, int ds){
		this.sameSignatureCMSummary = ConflictSummary.
				updateSameSignatureCMSummary(this.sameSignatureCMSummary, cause, ds)
	}

	private void updateEditSameMCTypeSummary(Map<String, Integer> confSummary){
		this.editSameMCTypeSummary = ConflictSummary.
				updateEditSameMCTypeSummary(this.editSameMCTypeSummary, confSummary)
	}

	private void matchConflictWithFile(Conflict conflict, String mergeDir){
		String rev_base = this.compareFiles.baseRevName
		String conflictPath = conflict.filePath
		boolean matchedFile = false
		int i = 0
		while(!matchedFile && i < this.mergedFiles.size){
			
			String mergedFilePath = this.mergedFiles.elementData(i).path.replaceFirst(rev_base, mergeDir)
			if(conflictPath.equals(mergedFilePath)){
				matchedFile = true
				boolean addedByOneDev = this.mergedFiles.get(i).isAddedByOneDev()
				this.addConflictToFile(conflict, i, addedByOneDev)
			}else{
				i++
			}
		}

		if(!matchedFile){
			MergedFile mf = new MergedFile(conflict.getFilePath())
			mf.setAddedByOneDev(true)
			this.mergedFiles.add(mf)
			this.filesAddedByOneDev++
			this.addConflictToFile(conflict, this.mergedFiles.size-1, true)
		}

	}
	
	private String getRevDir() {
		String result = ''
		String [] t = this.name.split('_')
		String [] tokens = t[1].split('-')
		
		result = 'rev_rev_left_' + tokens[0]+ '-rev_right_' + tokens[1]
		return result
	}

	private void addConflictToFile(Conflict conflict, int index, boolean matched){

		if(conflict.getType().equals(SSMergeConflicts.SameSignatureCM.toString())){

			conflict.setCauseSameSignatureCM(this.deletedBaseNodes, matched)
			String cause = conflict.getCauseSameSignatureCM()
			this.updateSameSignatureCMSummary(cause, conflict.getDifferentSpacing())

			//use the code below to skip the samesignaturemc analysis
			/*this.updateSameSignatureCMSummary(PatternSameSignatureCM.noPattern.toString(),
			 conflict.getDifferentSpacing())*/
		}

		if(conflict.getType().equals(SSMergeConflicts.EditSameMC.toString())){
			this.updateEditSameMCTypeSummary(conflict.editSameMCTypeSummary)
		}

		this.mergedFiles.elementData(index).conflicts.add(conflict)
		this.mergedFiles.elementData(index).updateMetrics(conflict)

	}

	public String printMetrics(){
		String result = ''
		for(MergedFile m : this.mergedFiles){
			if(m.conflicts.size != 0){
				result = result + m.toString()
			}
		}
		return result
	}

	private int getNumberOfFilesWithConflicts(){
		int result = 0
		for(MergedFile m : this.mergedFiles){
			if(m.hasConflicts()){
				result = result + 1
			}
		}
		return result
	}

	public String toString(){
		String report = this.name + ', ' + this.compareFiles.getNumberOfTotalFiles() +
				', ' + this.compareFiles.getFilesEditedByOneDev() + ', ' +
				this.compareFiles.getFilesThatRemainedTheSame() + ', ' +
				this.filesAddedByOneDev +', ' + this.mergedFiles.size() +
				', ' + !this.gitMergeHasNoConflicts +
				', ' + this.getNumberOfFilesWithConflicts() + ', ' +
				ConflictSummary.printConflictsSummary(this.mergeScenarioSummary) + ', ' +
				ConflictSummary.printSameSignatureCMSummary(this.sameSignatureCMSummary) + ', ' +
				this.possibleRenamings + ', ' +
				ConflictSummary.printEditSameMCTypeSummary(this.editSameMCTypeSummary)


		return report
	}

	public String computeMSSummary(){
		/*'Merge_Scenario,has_merge_Conflicts,Methods_With_Conflicts,Conflicting_EditSameMC,Conflicting_EditSameMC_DS,' +
		 'Conflicting_EditSameFD,Conflicting_EditSameFD_DS,NonConflicting_EditSameMC,' +
		 'NonConflicting_EditSameMC_DS,NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,' +
		 'EditDiffMC,EditDifffMC_EditSameMC,EditDiffMC_EditionAddsMethodInvocation,' +
		 'EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'*/
		/*set name*/
		String summary = this.name

		/*set has conflict*/
		if(this.hasConflicts){
			summary = summary + ',' + 1
		}else{
			summary = summary + ',' + 0
		}

		/*set has predictor*/
		if(this.hasPredictors){
			summary = summary + ',' + 1
		}else{
			summary = summary + ',' + 0
		}
		/*set number of conflicting editsamemc and editsamefd*/		
		summary = summary + ',' + this.methodsWithConflicts + ',' + this.mergeScenarioSummary.get('EditSameMC').getNumberOfConflicts() + ',' +
				this.mergeScenarioSummary.get('EditSameMC').getDifferentSpacing()+ ',' +
				this.mergeScenarioSummary.get('EditSameFd').getNumberOfConflicts() +
				',' + this.mergeScenarioSummary.get('EditSameFd').getDifferentSpacing()

		/*set non conflicting conflict predictors*/

		summary = summary + ',' + this.auxcomputeMSSummary()

		return summary
	}

	private String auxcomputeMSSummary(){
		String result = ''
		/*Instantiating remaining variables*/
		int nonConflicting_EditSameMC, nonConflicting_EditSameMC_DS,
		nonConflicting_EditSameFD,nonConflicting_EditSameFD_DS,
		editDiffMC,editDifffMC_EditSameMC,
		editDiffMC_EditionAddsMethodInvocation,
		editDiffMC_EditionAddsMethodInvocation_EditSameMC = 0

		/*for each file containing conflict predictors*/
		for(String filePath : this.filesWithConflictPredictors.keySet()){

			ArrayList<ConflictPredictor> predictors = this.filesWithConflictPredictors.get(filePath)

			/*for each conflict predictor in that file*/
			for(ConflictPredictor predictor : predictors ){
				int [] editDiffSummary = predictor.computePredictorSummary()
				if(predictor instanceof EditSameMC){
					nonConflicting_EditSameMC++
					if(predictor.diffSpacing){
						nonConflicting_EditSameMC_DS++
					}
					editDifffMC_EditSameMC = editDifffMC_EditSameMC + editDiffSummary[1]
					editDiffMC_EditionAddsMethodInvocation_EditSameMC = editDiffMC_EditionAddsMethodInvocation_EditSameMC +
							editDiffSummary[3]
				}else if(predictor instanceof EditSameFD){
					nonConflicting_EditSameFD++
					if(predictor.diffSpacing){
						nonConflicting_EditSameFD_DS++
					}
				}else if(predictor instanceof EditDiffMC){
					editDiffMC = editDiffMC + editDiffSummary[0]
					editDifffMC_EditSameMC = editDifffMC_EditSameMC + editDiffSummary[1]
					editDiffMC_EditionAddsMethodInvocation = editDiffMC_EditionAddsMethodInvocation + editDiffSummary[2]
					editDiffMC_EditionAddsMethodInvocation_EditSameMC = editDiffMC_EditionAddsMethodInvocation_EditSameMC +
							editDiffSummary[3]
				}
			}
		}

		/*set string result*/
		result = nonConflicting_EditSameMC + ',' + nonConflicting_EditSameMC_DS + ',' +
				nonConflicting_EditSameFD + ',' + nonConflicting_EditSameFD_DS + ',' +
				editDiffMC + ',' + editDifffMC_EditSameMC + ',' +
				editDiffMC_EditionAddsMethodInvocation + ',' +
				editDiffMC_EditionAddsMethodInvocation_EditSameMC
		return result
	}

	private void removeNonMCBaseNodes(List<FSTNode> bNodes){
		LinkedList<FSTNode> baseNodes = new LinkedList<FSTNode>(bNodes)
		for(FSTNode baseNode: baseNodes){
			if(!(baseNode.getType().equals("MethodDecl") || baseNode.getType().equals("ConstructorDecl"))){
				bNodes.remove(baseNode)
			}
		}
	}

	public int getPossibleRenamings() {
		return possibleRenamings;
	}

	public void setPossibleRenamings(int possibleRenamings) {
		this.possibleRenamings = possibleRenamings;
	}

	public boolean hasConflictsThatWereNotSolved(){
		boolean result = false

		if(this.gitMergeHasNoConflicts && (this.fileNotFoundConflicts == null)){
			//result = this.hasNonDSConflicts()
			result = this.hasConflicts
		}else{
			result = true
		}

		return result
	}

	private boolean hasNonDSConflicts(){
		boolean hasNonDSConflict = false

		int i = 0

		while((!hasNonDSConflict) && (i < SSMergeConflicts.values().length)){
			String type = SSMergeConflicts.values()[i].toString()

			Conflict conflict = this.mergeScenarioSummary.get(type)
			int diff =  conflict.getNumberOfConflicts() - conflict.getDifferentSpacing()

			if(diff >0){
				hasNonDSConflict = true
			}
			i++
		}

		return hasNonDSConflict
	}

	public Map<String, ArrayList<EditSameMC>> getFilesWithMethodsToJoana() {
		return filesWithConflictPredictors;
	}

	public void setFilesWithConflictPredictors(Map<String, ArrayList<EditSameMC>> filesWithMethodsToJoana) {
		this.filesWithConflictPredictors = filesWithMethodsToJoana;
	}

	private boolean isAConflictPredictor(FSTTerminal node){
		boolean result = false

		if(node.getType().equals("MethodDecl") || node.getType().equals("ConstructorDecl")){
			String nodeBody = node.getBody()
			if(nodeBody.contains(SemistructuredMerge.MERGE_SEPARATOR) && nodeBody.contains(SemistructuredMerge.SEMANTIC_MERGE_MARKER)){
				result = true
			}
		}

		if(node.getType().equals('FieldDecl') && node.getBody().contains(SemistructuredMerge.MERGE_SEPARATOR)){
			result = true
		}


		return result
	}


	public MergeCommit getMc() {
		return mc;
	}

	public void setMc(MergeCommit mc) {
		this.mc = mc;
	}

	public String getReplayedMergeSha() {
		return replayedMergeSha;
	}

	public void setReplayedMergeSha(String replayedMergeSha) {
		this.replayedMergeSha = replayedMergeSha;
	}

	public static void main(String[] args){
		Project project = new Project('Teste')
		MergeScenario ms = new MergeScenario('C:\\Users\\155 X-MX\\Documents\\dev\\second_study\\testes\\rev_123ab_456cd\\rev_123ab-456cd.revisions', true)
		ms.analyzeConflicts()
		//ms.deleteMSDir()
		String ms_summary = ms.computeMSSummary()
		ConflictPredictorPrinter.printMergeScenarioReport(project, ms,ms_summary)


		println 'hello'
		/*Map <String,Conflict> mergeScenarioSummary = new HashMap<String, Conflict>()
		 String type = SSMergeConflicts.EditSameMC.toString()
		 mergeScenarioSummary.put(type, new Conflict(type))
		 Conflict conflict = mergeScenarioSummary.get(type)
		 conflict.setNumberOfConflicts(5);
		 println 'hello world'*/

	}

}
