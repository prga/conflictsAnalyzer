package util

import org.apache.commons.lang3.StringUtils
class StringSimilarity {

		/**
	 * Compute the similarity between two given strings based on the <i>Levenshtein Distance</i>.
	 * @param first
	 * @param second
	 * @return <b>double</b> similarity between 0.0 and 1.0
	 */
	public static double computeStringSimilarity(String first,String second) {
		@SuppressWarnings("unused")
		String longer = first, shorter = second;
		if (first.length() < second.length()) { // longer should always have greater length
			longer = second; 
			shorter= first;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0; /* both strings are zero length */ 
		}

		int levenshteinDistance = StringUtils.getLevenshteinDistance(first, second);
		return ((longerLength - levenshteinDistance)/(double) longerLength);
	}



}
