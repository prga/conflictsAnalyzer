package main


import java.util.LinkedList
import java.util.Map;
import java.util.Observable;

import merger.FSTGenMerger;
import merger.MergeVisitor
import modification.traversalLanguageParser.addressManagement.DuplicateFreeLinkedList
import sun.tools.jar.Main;
import util.CompareFiles;
import composer.rules.ImplementsListMerging
import de.ovgu.cide.fstgen.ast.FSTNode;
import de.ovgu.cide.fstgen.ast.FSTTerminal;


class MergeScenario implements Observer {

	private String name

	private ArrayList<MergedFile> mergedFiles

	private Map<String,Conflict> mergeScenarioSummary

	private boolean hasConflicts
	
	private boolean hasNonJavaFilesConflict

	private CompareFiles compareFiles
	
	private FSTGenMerger fstGenMerge
	
	private Map<String, Integer> sameSignatureCMSummary
	
	private Map<String, Integer> editSameMCTypeSummary
	
	private int possibleRenamings

	private int filesAddedByBothDevs
	
	private ExtractorResult extractResult
	
	public MergeScenario(ExtractorResult er){
		
		this.extractResult = er
		this.setHasNonJavaFilesConflict()
		this.setName()
		//this.removeVarArgs()
		this.hasConflicts = false
		this.createMergeScenarioSummary()
		this.createSameSignatureCMSummary()
		this.createEditSameMCTypeSummary()
		this.setMergedFiles()
	}
	
	public void setHasNonJavaFilesConflict(){
		if(this.extractResult.nonJavaFilesWithConflict.size()>0){
			this.hasNonJavaFilesConflict = true
		}
	}
	
	public void createSameSignatureCMSummary(){
		this.sameSignatureCMSummary = ConflictSummary.initializeSameSignatureCMSummary()
	}
	
	public void createEditSameMCTypeSummary(){
		this.editSameMCTypeSummary = ConflictSummary.initializeEditSameMCTypeSummary()
	}
	
	public void setMergedFiles(){
		this.compareFiles = new CompareFiles(extractResult.revisionFile)
		/*pre process step*/
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
		String [] temp = extractResult.revisionFile.split('/')
		String revFile = temp[temp.length -1]
		this.name = revFile.substring(0, revFile.length()-10)
	}

	public String getName(){
		return this.name
	}

	public void analyzeConflicts(){

		this.runSSMerge()
		this.compareFiles.restoreFilesWeDontMerge()
	}

	public void deleteMSDir(){
		String msPath = extractResult.revisionFile.substring(0, (extractResult.revisionFile.length()-26))
		File dir = new File(msPath)
		boolean deleted = dir.deleteDir()
		if(deleted){
			println 'Merge scenario ' + extractResult.revisionFile + ' deleted!'
		}else{

			println 'Merge scenario ' + extractResult.revisionFile + ' not deleted!'
		}
	}

	public void runSSMerge(){
		this.fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", extractResult.revisionFile]
		fstGenMerge.run(files)
		
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
		String msPath = extractResult.revisionFile.substring(0, (extractResult.revisionFile.length()-26))
		String command = "grep -rl ... " + msPath
		def procGrep = command.execute()
		def procSed = sSed.execute()
		procGrep | procSed
		procSed.waitFor()
	}

	@Override
	public void update(Observable o, Object arg) {

		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){

			FSTTerminal node = (FSTTerminal) arg

			if(!node.getType().contains("-Content")){
				
				
				this.createConflict(node)
			}
		}
	}
	
	private boolean isABadParsedNode(FSTTerminal node){
		boolean isABadParsedNode = false
		DuplicateFreeLinkedList<File> parsedErrors = this.fstGenMerge.parsedErrors
		for(File f : parsedErrors){
			String classname = this.getClassName(node)
			String fileName = f.name
			if(fileName.contains(classname) || classname.equals('')){
				isABadParsedNode = true
			}
		}

		return isABadParsedNode
	}
	
	private String getClassName(FSTNode node){
		String name = ''
		if(node!=null){
			String type = node.getType()
			if(type.equals('ClassDeclaration')){
				name = node.getName()
				return name
			}else{
				this.getClassName(node.getParent())
			}
		}else{
			return name
		}
	}
	
	public void createConflict(FSTTerminal node){
		if(!this.isABadParsedNode(node)){
			if(!this.hasConflicts){
				this.hasConflicts = true
				this.removeNonMCBaseNodes(fstGenMerge.baseNodes)
			}
			Conflict conflict = new Conflict(node, extractResult.revisionFile);
			this.matchConflictWithFile(conflict)
			this.updateMergeScenarioSummary(conflict)
		}
	}
	
	private void updateSameSignatureCMSummary(String cause, int ds){
		this.sameSignatureCMSummary = ConflictSummary.
		updateSameSignatureCMSummary(this.sameSignatureCMSummary, cause, ds)
	}
	
	private void updateEditSameMCTypeSummary(String type){
		this.editSameMCTypeSummary = ConflictSummary.
		updateEditSameMCTypeSummary(this.editSameMCTypeSummary, type)
	}
	
	private void matchConflictWithFile(Conflict conflict){
		String rev_base = this.compareFiles.baseRevName
		String conflictPath = conflict.filePath
		boolean matchedFile = false
		int i = 0
		while(!matchedFile && i < this.mergedFiles.size){
			String mergedFilePath = this.mergedFiles.elementData(i).path.replaceFirst(rev_base, this.name)
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
			this.filesAddedByBothDevs++
			this.addConflictToFile(conflict, this.mergedFiles.size-1, true)
		}
		
	}

	private void addConflictToFile(Conflict conflict, int index, boolean matched){
		
		if(conflict.getType().equals(SSMergeConflicts.SameSignatureCM.toString())){
			
			conflict.setCauseSameSignatureCM(fstGenMerge.baseNodes, matched)
			String cause = conflict.getCauseSameSignatureCM()
			this.updateSameSignatureCMSummary(cause, conflict.getDifferentSpacing())
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
				this.filesAddedByBothDevs +', ' + this.mergedFiles.size() +
				', ' + this.getNumberOfFilesWithConflicts() + ', ' + 
				ConflictSummary.printConflictsSummary(this.mergeScenarioSummary) + ', ' +
				ConflictSummary.printSameSignatureCMSummary(this.sameSignatureCMSummary) + ', ' +
				this.possibleRenamings + ', ' + ConflictSummary.printEditSameMCTypeSummary(this.editSameMCTypeSummary)

		return report
	}
	
	private void removeNonMCBaseNodes(LinkedList<FSTNode> bNodes){
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

	public static void main(String[] args){
		ExtractorResult er = new ExtractorResult()
		er.revisionFile = '/Users/paolaaccioly/Desktop/Teste/interfaceParameters/rev.revisions'
		MergeScenario ms = new MergeScenario(er)
		ms.analyzeConflicts()
		println 'hello'
		//Conflict test = ms.mergeScenarioSummary.get(SSMergeConflicts.EditSameEnumConst.toString())
		//ConflictPrinter.printBadParsedNodes(ms, 'TGM')
		//println 'hello'
		/*Map <String,Conflict> mergeScenarioSummary = new HashMap<String, Conflict>()
		 String type = SSMergeConflicts.EditSameMC.toString()
		 mergeScenarioSummary.put(type, new Conflict(type))
		 Conflict conflict = mergeScenarioSummary.get(type)
		 conflict.setNumberOfConflicts(5);
		 println 'hello world' test*/

	}

}
