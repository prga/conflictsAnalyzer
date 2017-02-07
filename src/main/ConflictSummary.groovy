package main

public class ConflictSummary {


	public static String printConflictsSummary(HashMap<String, Conflict> projectSummary){
		String result = ''

		String noPattern = SSMergeConflicts.NOPATTERN.toString()
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			String type = c.toString()
			Conflict conflict = projectSummary.get(type)
			result = result + conflict.getNumberOfConflicts() + ', '
			if(!type.equals(noPattern)){
				result = result + conflict.getDifferentSpacing() + ', ' +
						conflict.getConsecutiveLines() + ', ' + conflict.getFalsePositivesIntersection() +
						', '
			}
		}
		result = result.subSequence(0, result.length()-2)
		return result
	}

	public static HashMap<String, Conflict> initializeConflictsSummary(){
		HashMap<String, Conflict> conflictSummary = new HashMap<String, Conflict>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			conflictSummary.put(type, new Conflict(type))
		}
		return conflictSummary
	}

	public static HashMap<String, Conflict> updateConflictsSummary(HashMap<String, Conflict> projectSummary, Conflict conflict){

		String conflictType = conflict.getType()
		Conflict c2 = projectSummary.get(conflictType)

		//get new values
		int numberOfConflicts = conflict.getNumberOfConflicts() + c2.getNumberOfConflicts()
		int differentSpacing = conflict.getDifferentSpacing() + c2.getDifferentSpacing()
		int consecutiveLines = conflict.getConsecutiveLines() + c2.getConsecutiveLines()
		int falsePositivesIntersection = conflict.falsePositivesIntersection +
				c2.getFalsePositivesIntersection()

		//set new values
		c2.setNumberOfConflicts(numberOfConflicts)
		c2.setDifferentSpacing(differentSpacing)
		c2.setConsecutiveLines(consecutiveLines)
		c2.setFalsePositivesIntersection(falsePositivesIntersection)

		return projectSummary

	}

	public static HashMap<String, Integer> initializeSameSignatureCMSummary(){
		HashMap<String, Integer> sameSignatureCMSummary = new HashMap<String, Integer>()
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			String type = p.toString();
			sameSignatureCMSummary.put(type, 0)
			String ds = type + 'DS'
			sameSignatureCMSummary.put(ds, 0)
		}

		return sameSignatureCMSummary
	}

	public static HashMap<String, Conflict> updateSameSignatureCMSummary(HashMap<String, Integer> summary, String cause, int ds){

		String conflictType = cause
		int quantity = summary.get(conflictType)
		quantity++
		summary.put(conflictType, quantity)
		if(ds==1){
			String diffSpacing = conflictType + 'DS'
			int quantity2 = summary.get(diffSpacing)
			quantity2++
			summary.put(diffSpacing, quantity2)
		}

		return summary

	}
	
	public static String printSameSignatureCMSummary(HashMap<String, Integer> summary){
		String result = ''
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			String cause = p.toString()
			String diffSpacing = cause + 'DS'
			result = result + summary.get(cause) + ', ' + summary.get(diffSpacing) + ', '
			
			
		}
		result = result.subSequence(0, result.length()-2)
		return result
	}
	
	public static HashMap<String, Integer> initializeEditSameMCTypeSummary(){
		HashMap<String, Integer> editSameMCTypeSummary = new HashMap<String, Integer>()
		for(EditSameMCTypes p : EditSameMCTypes.values()){
			String type = p.toString();
			editSameMCTypeSummary.put(type, 0)

		}

		return editSameMCTypeSummary
	}
	
	public static HashMap<String, Conflict> updateEditSameMCTypeSummary(HashMap<String, Conflict> editSameMCTypeSummary, HashMap<String, Conflict> confSummary){
		for(EditSameMCTypes p : EditSameMCTypes.values()){
			String type = p.toString()
			int newQuantity = editSameMCTypeSummary.get(type) + confSummary.get(type)
			editSameMCTypeSummary.put(type, newQuantity)
		}

		return editSameMCTypeSummary
	}

	public static String printEditSameMCTypeSummary(HashMap<String, Integer> summary){
		String result = ''
		for(EditSameMCTypes c : EditSameMCTypes.values()){
			String type = c.toString()
			int quantity = summary.get(type)
			result = result + quantity + ', '
		}
		result = result.subSequence(0, result.length()-2)
		return result
	}
}
