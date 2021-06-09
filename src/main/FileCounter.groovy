package main



class FileCounter {

	def countFiles(dir) {
		
		
		def count=0;
		
		dir.eachFile{
			file ->
			
			if(file.directory){
				
				count += countFiles(file)
				
			} else if(file.toString().endsWith(".java")) {
				count++
			}
			
		}
	
		
		return count
	}
	
	def countRevFiles(file){
		
		def count = 0
		
		file.eachLine{
			revPath ->
			
			def dirName = revPath.substring(0, (revPath.length() - 10))
			def dir = new File(dirName)
			println dir.toString()
			def revCount = countFiles(dir)
			println revCount
			count += revCount
			
		}
		
		return count
		
	}
	


	public static void main (String[] args){

		def file = new File('/Users/paolaaccioly/Documents/Doutorado/study_data/dropwizard/RevisionsFiles.csv')
		def fc = new FileCounter()
		def count = fc.countRevFiles(file)
		println 'Final count: ' + count
		

	}

}
