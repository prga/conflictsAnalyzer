package util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

import main.EditSameMCTypes
import merger.FSTGenMerger

class ExtractMethodBody {

	public static String[] getEditSameMCType(String nodeBody){
		String type = ''
		String mergeResult = ''
		String[] result = ['', '']
		String[] methods = this.getMethods(nodeBody)
		if(methods[0].equals('') || methods[2].equals('')){
			type = EditSameMCTypes.RenamingOrDeletion.toString()
		}else{
			String[] statements = ['', '', '']
			statements[0] = this.extractMethodBody(methods[0])
			statements[1] = this.extractMethodBody(methods[1])
			statements[2] = this.extractMethodBody(methods[2])
			mergeResult = this.getMergeResult(statements)
			if(mergeResult.contains(FSTGenMerger.DIFF3MERGE_SEPARATOR) &&
			mergeResult.contains(FSTGenMerger.DIFF3MERGE_END)){
				type = EditSameMCTypes.InsideMethod.toString()
			}else{
				type = EditSameMCTypes.OutsideMethod.toString()
			}
		}
		
		if(!type.equals('')){
			result[0] = type
			result[1] = this.getMergeResult(methods)
		}
		return result
	}

	public static String[] getMethods(String nodeBody){

		String[] tokens = nodeBody.split(FSTGenMerger.MERGE_SEPARATOR)
		tokens[0] = tokens[0].replace(FSTGenMerger.SEMANTIC_MERGE_MARKER, '').trim()
		tokens[1] = tokens[1].trim()
		tokens[2] = tokens[2].trim()
		return tokens
	}

	public static String getMergeResult(String[] methods){
		String result = ''
		long time = System.currentTimeMillis()
		File tmpDir = new File(System.getProperty("user.dir") + File.separator + "fstmerge_tmp"+time)
		tmpDir.mkdir()

		File left = File.createTempFile("left", "", tmpDir);
		File base = File.createTempFile("base", "", tmpDir);
		File right = File.createTempFile("right", "", tmpDir);
		
		if(methods[0].length()==0){
			left.append(methods[0])
		}else{
			left.append(methods[0] + '\n')
		}
		
		if(methods[1].length()==0){
			base.append(methods[1])
		}else{
			base.append(methods[1] + '\n')
		}
		
		if(methods[2].length()==0){
			right.append(methods[2])
		}else{
			right.append(methods[2] + '\n')
		}

		String mergeCmd = "diff3 --merge " + left.getPath() + " " + base.getPath() + " " + right.getPath()

		Runtime run = Runtime.getRuntime()
		Process pr = run.exec(mergeCmd)
		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()))
		String line = ''
		while ((line=buf.readLine())!=null) {
			result += line + "\n"
		}
		pr.getInputStream().close()
		
		left.delete();
		base.delete();
		right.delete();
		tmpDir.delete();

		return result
	}

	public static String extractMethodBody(String method){
		String methodBody = ''
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
		if(method.length()>1){
			methodBody = method.substring(firstBracket + 1, lastBracket)
		}

		return methodBody
	}

	public static void main(String[] args){
		String a = ''
		a = "public void m(int y){/*comment*/\n String x = \"a\";\n y++;\n \n/*comment*/}//comment";
		println a
		println '---'
		String str = ExtractMethodBody.extractMethodBody(a)
		println str
	}
}
