package util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Map
import main.ConflictSummary
import main.EditSameMC;
import main.EditSameMCTypes
import br.ufpe.cin.mergers.SemistructuredMerge

class ExtractMethodBody {

	public static Map<String, Integer> getEditSameMCType(String nodeBody, int possibleRenaming){
		Map<String, Integer> summary = ConflictSummary.initializeEditSameMCTypeSummary()
		if(possibleRenaming > 0){
			summary.put(EditSameMCTypes.RenamingOrDeletion.toString(), 1)
		}else{
			String[] methodParts = this.extractMethodBody(nodeBody)

			int conflictsOutsideMethods = 0
			int conflictsInsideMethods = 0

			int conflictEndings = methodParts[0].split(SemistructuredMerge.DIFF3MERGE_END).length - 1
			int conflictStarters = methodParts[2].split(SemistructuredMerge.DIFF3MERGE_SEPARATOR).length - 1
			conflictsOutsideMethods = conflictEndings + conflictStarters
			if(conflictsOutsideMethods > 0){
				summary.put(EditSameMCTypes.OutsideMethod.toString(), conflictsOutsideMethods)
			}
			
			conflictStarters = nodeBody.split(SemistructuredMerge.DIFF3MERGE_SEPARATOR).length - 1
			conflictsInsideMethods = conflictStarters - conflictsOutsideMethods
			if(conflictsInsideMethods > 0){
				summary.put(EditSameMCTypes.InsideMethod.toString(), conflictsInsideMethods)
			}
		}

		return summary
	}
	
	public static String[] getMethods(String nodeBody){
		String[] result = ['','','']
		String[] tokens = nodeBody.split(SemistructuredMerge.MERGE_SEPARATOR)
		result[0] = tokens[0].replace(SemistructuredMerge.SEMANTIC_MERGE_MARKER, '').trim()
		result[1] = tokens[1].trim()
		if(tokens.length > 2){
			result[2] = tokens[2].trim()
		}else{
			result[2] = ''
		}
		return result
	}

	public static String[] extractMethodBody(String method){
		String[] methodParts = ['', '', '']
		String beforeFirstBrace = ''
		String insideBraces = ''
		String afterLastBrace = ''
		int firstBracket = 0;
		int stringSize = method.length()
		int lastBracket = stringSize -1;
		boolean foundFirstBracket, foundLastBracket = false;

		//find method first bracket position
		while(!foundFirstBracket && firstBracket < stringSize){
			String c = method.charAt(firstBracket)
			if(c.equals('{')){
				foundFirstBracket = true
			}else{
				firstBracket++
			}
		}

		//find method last bracket position
		while(!foundLastBracket && lastBracket > 0){
			String c = method.charAt(lastBracket)
			if(c.equals('}')){
				foundLastBracket = true
			}else{
				lastBracket--
			}
		}

		//get substring
		if(method.length()>1 && lastBracket >0){
			beforeFirstBrace = method.substring(0, firstBracket + 1)
			insideBraces = method.substring(firstBracket + 1, lastBracket)
			afterLastBrace = method.substring(lastBracket, method.length()-1)
		}else{
			beforeFirstBrace = method
		}
		methodParts[0] = beforeFirstBrace
		methodParts[1] = insideBraces
		methodParts[2] = afterLastBrace

		return methodParts
	}

	public static void main(String[] args){
		String a = ''
		//a = "antes{dentro}<<<<<<< depois >>>>>>>"
		a = 'antes {\ndentro\n}<<<<<<< depois >>>>>>>'
		println a
		println '---'
		String[] str = ExtractMethodBody.extractMethodBody(a)
		println str[0] + ', ' + str[1] + ', ' + str[2]
	}
}
