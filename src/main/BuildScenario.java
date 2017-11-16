package main;

public class BuildScenario {

	private TravisCommit parent1;

	private TravisCommit parent2;

	private TravisCommit mergeCommit;

	private TravisCommit replayedMergeCommit;

	public BuildScenario(String p1, String p2, String sha) {
		this.parent1 = new TravisCommit(p1);
		this.parent2 = new TravisCommit(p2);
		this.mergeCommit = new TravisCommit(sha);
		this.replayedMergeCommit = new TravisCommit("unknown");
	}

	public TravisCommit getParent1() {
		return parent1;
	}

	public void setParent1(TravisCommit parent1) {
		this.parent1 = parent1;
	}

	public TravisCommit getParent2() {
		return parent2;
	}

	public void setParent2(TravisCommit parent2) {
		this.parent2 = parent2;
	}

	public TravisCommit getMergeCommit() {
		return mergeCommit;
	}

	public void setMergeCommit(TravisCommit mergeCommit) {
		this.mergeCommit = mergeCommit;
	}

	public TravisCommit getReplayedMergeCommit() {
		return replayedMergeCommit;
	}

	public void setReplayedMergeCommit(TravisCommit replayedMergeCommit) {
		this.replayedMergeCommit = replayedMergeCommit;
	}

	/*parent1;parent2;mergeCommit;replayedMergeCommit*/
	public String toString() {
		String result = this.parent1.toString() + ";" + this.parent2.toString() + ";" +
				this.mergeCommit.toString() + ";" + this.replayedMergeCommit.toString();
		return result;
	}

}
