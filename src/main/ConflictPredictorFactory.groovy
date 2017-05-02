package main


import br.ufpe.cin.mergers.SemistructuredMerge;
import de.ovgu.cide.fstgen.ast.FSTTerminal


class ConflictPredictorFactory {
	
	public ConflictPredictor createConflictPredictor(FSTTerminal node, String mergeScenarioPath, String filePath){
		ConflictPredictor result= null
		if(node.getType().equals('FieldDecl')){
			result = new EditSameFD(node, mergeScenarioPath, filePath)
		}else if(this.bothVersionsWereEdited(node)){
			result = new EditSameMC(node, mergeScenarioPath, filePath)
		}else{
			result = new EditDiffMC(node, mergeScenarioPath, filePath)
		}
		if(result.gitBlameProblem || result.node.getBody().contains(Blame.NOT_A_PREDICTOR)){
			result = null
		}
		return result
	}
	
	 
	 /* returns true if both versions (left and right) differ from base*/
	private boolean bothVersionsWereEdited(FSTTerminal node){
		String [] tokens = this.splitNodeBody(node)
		boolean result = false;
		if( (!tokens[0].equals(tokens[1])) && (!tokens[2].equals(tokens[1])) &&
				(!tokens[0].equals(tokens[2])) ){
			result = true;
		}
		return result;
	}
	
	public String[] splitNodeBody(FSTTerminal node){
		String [] splitBody = ['', '', '']
		String[] tokens = node.getBody().split(SemistructuredMerge.MERGE_SEPARATOR)
		splitBody[0] = tokens[0].replace(SemistructuredMerge.SEMANTIC_MERGE_MARKER, "").trim()
		splitBody[1] = tokens[1].trim()
		splitBody[2] = tokens[2].trim()

		return splitBody
	}
}
