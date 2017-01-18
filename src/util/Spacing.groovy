package util

class Spacing {

	public static String removeSpacing(String m){
		String result = ''
		if(!m.contains('\"')){
			result = m.replaceAll("\\s+","")
		}else{
			result = this.auxRemoveSpacing(m)
		}
		return result
	}

	private static String auxRemoveSpacing(String m){
		String result = ''
		boolean insideQuotes = false
		for(int i = 0; i < m.length(); i++){
			String c = m.getAt(i)
			if(c.equals('\"')){

				if(insideQuotes){
					String previous = m.getAt(i-1)
					if(!previous.equals('\\')){
						insideQuotes = false
					}
				}else{
					insideQuotes = true
				}
			}

			if(insideQuotes){
				result = result + c
			}else if(!this.isInvisibleChar(c)){
				result = result + c
			}
		}
		return result
	}

	public static boolean isInvisibleChar(String c){
		return c.equals('\n') || c.equals(' ')
	}

	public static void main(String[] args) {
		File f = new File('/Users/paolaaccioly/Desktop/Teste/jdimeTests/base/Case.java')
		String text = f.getText()
		println text
		println 'XXX'
		String result = Spacing.removeSpacing(text)
		println result
	}
}
