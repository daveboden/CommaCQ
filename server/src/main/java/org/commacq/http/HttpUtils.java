package org.commacq.http;

class HttpUtils {
	
	/**
	 * Returns a string that we hope will represent an entity
	 * or null if the URL could not be parsed. 
	 * @param target
	 * @return
	 */
	static String getEntityStringFromTarget(String target) {
		int finalSlash = target.lastIndexOf("/");
		if(finalSlash == -1 || finalSlash >= target.length() - 1) {
			return null;
		}
		
		return target.substring(finalSlash + 1);
	}
	
}
