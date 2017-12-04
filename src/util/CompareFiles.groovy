package util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils

import main.MergedFile;

class CompareFiles {

	private String leftRevName

	private String baseRevName

	private String rightRevName

	private String revDir
	
	private String mergeDir
	
	private String fstmergeDir

	private File tempDir

	private int filesEditedByOneDev

	private int filesThatRemainedTheSame

	private ArrayList<MergedFile> filesToBeMerged

	public CompareFiles(String revFile){

		this.setDirNames(revFile)
		this.filesToBeMerged = new ArrayList<MergedFile>()
	}
	
	

	private void setDirNames(String revFile){
		File file = new File(revFile)
		this.revDir = new File(revFile).getParent()
		String[] revs = new File(revFile).text.split('\n')
		this.leftRevName = revs[0].trim()
		this.baseRevName = revs[1].trim()
		this.rightRevName = revs[2].trim()
		this.mergeDir = this.revDir + File.separator + 'rev_merged_git'
		try{
			String rev = new File(this.revDir).getName()
			String[] tokens = rev.split('_')
			this.fstmergeDir = this.revDir + File.separator + 'rev_rev_left_' + 
			tokens[1] + '-rev_right_' + tokens[2] 
		}catch (StringIndexOutOfBoundsException e){
			this.fstmergeDir = "rev"
		}
		
		this.tempDir = new File(this.revDir + File.separator + 'temp')


	}
	
	public String getFstmergeDir(){
		return this.fstmergeDir
	}
	
	public ArrayList<MergedFile> getFilesToBeMerged(){
		return this.filesToBeMerged
	}

	public void ignoreFilesWeDontMerge(){
		//delete non java files
		this.removeNonJavaFiles();

		
		String baseFolder = this.revDir + File.separator + this.baseRevName.trim()
		//String baseFolder = "C:\\Users\\155 X-MX\\Documents\\dev\\second_study\\testes\\rev_123ab_456cd\\rev_base_90bvd"
		//moveFilesOnSingleVersion(baseFolder.replaceFirst(baseRevName, leftRevName),this.leftRevName, this.rightRevName, this.baseRevName)
		//moveFilesOnSingleVersion(baseFolder.replaceFirst(baseRevName, rightRevName),this.rightRevName, this.leftRevName, this.baseRevName)
		this.iterateRevFolders(this.leftRevName, this.baseRevName, baseFolder, this.rightRevName)
	}
	
	public void moveFilesOnSingleVersion(String currentFolder, String currentRevName, String otherRevName, String baseRevName)
	{
		File directory = new File(currentFolder)
		if(directory.exists())
		{
			File[] fList = directory.listFiles()
			for(File file: fList)
			{
				if(file.isDirectory())
				{
					moveFilesOnSingleVersion(file.getAbsolutePath(), currentRevName, otherRevName, baseRevName);
				}else{
					String otherFilePath = file.getAbsolutePath().replaceFirst(currentRevName, otherRevName)
					String baseFilePath = file.getAbsolutePath().replaceFirst(currentRevName, baseRevName)
					File otherFile = new File(otherFilePath)
					File baseFile = new File(baseFilePath)
					if(!otherFile.exists() && !baseFile.exists())
					{
						this.moveAndDeleteFiles(currentRevName, file)
					}else if(baseFile.exists() && !otherFile.exists())
					{
						file.delete()
						baseFile.delete()
					}
				}
			}
		}
	}

	private void iterateRevFolders(String leftRevName, String baseRevName, String baseFolder, String rightRevName){

		File directory = new File(baseFolder)
		if(directory.exists()) {
			File[] fList = directory.listFiles()
			for (File file : fList){
				if (file.isDirectory()){
					iterateRevFolders(leftRevName, baseRevName, file.getAbsolutePath(), rightRevName)
				} else {
					String leftFilePath   = file.getAbsolutePath().replaceFirst(baseRevName, leftRevName)
					String rightFilePath  = file.getAbsolutePath().replaceFirst(baseRevName, rightRevName)
					this.compareAndMoveFiles(leftFilePath, file.getAbsolutePath() ,rightFilePath)
				}
			}
		}


	}


	private void compareAndMoveFiles(String leftFile, String baseFile, String rightFile){

		File left = new File(leftFile)
		File base = new File(baseFile)
		File right = new File(rightFile)

		if(left.exists() && base.exists() && right.exists() && 
			left.isFile() && base.isFile() && right.isFile()){
			
			this.compareFiles(left, base, right)
		}

	}

	private void compareFiles (File left, File base, File right){

		boolean leftEqualsBase = FileUtils.contentEquals(left, base)
		boolean rightEqualsBase = FileUtils.contentEquals(right, base)
		boolean leftEqualRight = FileUtils.contentEquals(left, right)
		
		//use the code bellow to remove only equal files
		if(/*leftEqualRight*/ leftEqualsBase && rightEqualsBase){
			//this.moveAndDeleteFiles(this.baseRevName, base, left, right)
			FileUtils.forceDelete(left)
			FileUtils.forceDelete(base)
			FileUtils.forceDelete(right)
		}else{
			MergedFile mf = new MergedFile(base.getAbsolutePath())
			this.filesToBeMerged.add(mf)
		}
		
		//use the code below to merge only files that differ in the three revisions
		/*if(leftEqualsBase && rightEqualsBase){
			this.filesThatRemainedTheSame = this.filesThatRemainedTheSame + 1
			this.moveAndDeleteFiles(this.baseRevName, base, left, right)

		}else if((!leftEqualsBase) && rightEqualsBase){
			this.filesEditedByOneDev = this.filesEditedByOneDev + 1
			this.moveAndDeleteFiles(this.leftRevName, left, base, right)

		}else if(leftEqualsBase && (!rightEqualsBase)){
			this.filesEditedByOneDev = this.filesEditedByOneDev + 1
			this.moveAndDeleteFiles(this.rightRevName, right, base, left)

		}else if((!leftEqualsBase) && (!rightEqualsBase)){
			MergedFile mf = new MergedFile(base.getAbsolutePath())
			this.filesToBeMerged.add(mf)
		}*/

	}

	private void moveAndDeleteFiles(String revName, File toBeMoved, File toBeDeleted1 = null, File toBeDeleted2 = null){

		String temp = toBeMoved.getAbsolutePath().replaceFirst(revName, 'temp2')
		FileUtils.moveFile(toBeMoved, new File(temp))
		if(toBeDeleted1 != null)
		{
			FileUtils.forceDelete(toBeDeleted1)
		}
		if(toBeDeleted2 != null)
		{
			FileUtils.forceDelete(toBeDeleted2)
		}
	}

	public int getNumberOfTotalFiles(){

		int totalFiles = this.filesEditedByOneDev + this.filesThatRemainedTheSame + this.filesToBeMerged.size()
		return totalFiles
	}

	public void restoreFilesWeDontMerge(){

		//copy non java files from rev_merged_git dir
		File sourcedir = new File(this.revDir + File.separator + 'rev_merged_git');
		this.moveFiles(sourcedir)

		//copy java files from temp2
		sourcedir = new File(this.revDir + File.separator + 'temp2');
		if(sourcedir.exists()){
			this.moveFiles(sourcedir)
		}
		
	}

	private void moveFiles(File sourceDir){
		File[] files = sourceDir.listFiles()

		for(File file : files){

			if(file.isDirectory()){
				this.moveFiles(file)
			}else{
				this.auxMoveFiles(file)
			}
		}
	}

	private void auxMoveFiles(File sourceDir){
		String temp = ''
		String leftId = this.leftRevName.substring(this.leftRevName.length() - 5)
		String rightId = this.rightRevName.substring(this.rightRevName.length() - 5)
		String revName = 'rev_' + leftId + '-' + rightId
		
		String source = sourceDir.getAbsolutePath()

		if(source.contains('rev_merged_git') ){
			
			if(!(source.endsWith(".java"))){
				temp = sourceDir.getAbsolutePath().replaceFirst('rev_merged_git' , revName)
				FileUtils.moveFile(sourceDir, new File(temp))
			}
			
		}else{
			temp = sourceDir.getAbsolutePath().replaceFirst('temp2', revName)
			FileUtils.moveFile(sourceDir, new File(temp))
		}
	}

	public int getFilesEditedByOneDev() {
		return filesEditedByOneDev;
	}

	public int getFilesThatRemainedTheSame() {
		return filesThatRemainedTheSame;
	}

	public void removeNonJavaFiles(File dir){

		File leftFolder = new File (this.revDir + File.separator + this.leftRevName)
		this.auxRemoveNonJavaFiles(leftFolder)

		File baseFolder = new File (this.revDir + File.separator + this.baseRevName)
		this.auxRemoveNonJavaFiles(baseFolder)

		File rightFolder = new File (this.revDir + File.separator + this.rightRevName)
		this.auxRemoveNonJavaFiles(rightFolder)

	}

	private void auxRemoveNonJavaFiles(File dir){
		File[] files = dir.listFiles()

		for(File file : files){

			if(file.isFile()){

				String filePath = file.getAbsolutePath()

				if(!(filePath.endsWith(".java"))){

					if(file.delete()){
						//println(files[i].getName() + " is deleted!");
					}else{
						println(file.getName() + " delete operation has failed.");
					}
				}

			} else if (file.isDirectory()){

				this.auxRemoveNonJavaFiles(file)
			}

		}
	}
	
	private void replaceFilesAfterFSTMerge(String f){
		File first = new File(f)
		File[] files = first.listFiles()

		for(File file : files){
			if(file.isDirectory()){
				this.replaceFilesAfterFSTMerge(file.getAbsolutePath())
			} else if (file.isFile() && !file.getAbsolutePath().contains('.java.merge')){
				Path toBeMoved = file.toPath()
				String fstmerge = new File(this.fstmergeDir).getName()
				String temp = file.getAbsolutePath().replaceFirst(fstmerge, 'rev_merged_git')
				File temp2 = new File(temp)
				Path toBeReplaced = temp2.toPath()
				Files.move(toBeMoved, toBeReplaced, StandardCopyOption.REPLACE_EXISTING)
			}
		}

	}

	private void auxMoveNonJavaFiles(){

	}

	public static void main(String[] args){
		File temp = new File('/Users/paolaaccioly/Desktop/Teste/jdimeTests/base/Case.java')
		Path toBeMoved = temp.toPath()
		File temp2 = new File('/Users/paolaaccioly/Desktop/Teste/jdimeTests/left/Case.java')
		Path toBeReplaced = temp2.toPath()
		Files.move(toBeMoved, toBeReplaced, StandardCopyOption.REPLACE_EXISTING)
		
	}
}
