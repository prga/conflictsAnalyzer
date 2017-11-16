package main;

public class TravisCommit {
	
	private String sha;
	
	private String buildID;
	
	private String buildStatus;
	
	public TravisCommit(String sha) {
		this.sha = sha;
		this.buildID = "unknown";
		this.buildStatus = "unknown";
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getBuildID() {
		return buildID;
	}

	public void setBuildID(String buildID) {
		this.buildID = buildID;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}
	
	/*"sha; buildId; buildStatus"*/
	public String toString() {
		String result = this.sha + ";" + this.buildID + ";" + this.buildStatus;
		return result;
	}
	

}
