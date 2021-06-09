package nonJavaFilesAnalysis



class ProjectSummary {

	String name
	List totalMergeCommits
	List mergeCommitsConflictsJavaFiles
	List mergeCommitsConflictsJavaFilesWFP
	List mergeCommitsConflictsJavaFilesWDS
	List mergeCommitsConflictsJavaFilesWCL
	List mergeCommitsConflictsNonJavaFiles

	public ProjectSummary(String name){
		this.name = name
		totalMergeCommits = new ArrayList<String>()
		mergeCommitsConflictsJavaFiles = new ArrayList<String>()
		mergeCommitsConflictsJavaFilesWFP = new ArrayList<String>()
		mergeCommitsConflictsJavaFilesWDS = new ArrayList<String>()
		mergeCommitsConflictsJavaFilesWCL = new ArrayList<String>()
		mergeCommitsConflictsNonJavaFiles = new ArrayList<String>()
	}
	
	public void teste(){
		ArrayList<String> resultado = this.removeOneListFromTheOther(this.mergeCommitsConflictsNonJavaFiles, this.totalMergeCommits)
		println 'hello'
	}
	
	public String toString(){
		String result = ''
		ArrayList<String> NonJavaMinusJava = this.removeOneListFromTheOther(this.mergeCommitsConflictsNonJavaFiles, this.mergeCommitsConflictsJavaFiles)
		ArrayList<String> NonJavaMinusJavaWFP = this.removeOneListFromTheOther(this.mergeCommitsConflictsNonJavaFiles, this.mergeCommitsConflictsJavaFilesWFP)
		ArrayList<String> NonJavaMinusJavaWDS  = this.removeOneListFromTheOther(this.mergeCommitsConflictsNonJavaFiles,this.mergeCommitsConflictsJavaFilesWDS)
		ArrayList<String> NonJavaMinusJavaWCL  = this.removeOneListFromTheOther(this.mergeCommitsConflictsNonJavaFiles,this.mergeCommitsConflictsJavaFilesWCL)

		result = this.name + ';' + this.totalMergeCommits.size() + ';' +
				this.mergeCommitsConflictsJavaFiles.size() + ';' + this.mergeCommitsConflictsJavaFilesWFP.size() +
				';' + this.mergeCommitsConflictsJavaFilesWDS.size() + ';' + this.mergeCommitsConflictsJavaFilesWCL.size() +
				';' + this.mergeCommitsConflictsNonJavaFiles.size() + ';'+ NonJavaMinusJava.size() + ';' +
				NonJavaMinusJavaWFP.size() + ';' + NonJavaMinusJavaWDS.size() + ';' + NonJavaMinusJavaWCL.size()

		return result
	}

	public ArrayList<String> removeOneListFromTheOther(ArrayList<String> first, ArrayList<String> second){
		ArrayList<String> result = new ArrayList<String>(first)
		for(String s in second){
			if(first.contains(s)){
				result.remove(s)
			}
		}
		return result
	}


}
