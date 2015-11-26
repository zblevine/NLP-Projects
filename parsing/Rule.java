
public class Rule {
	private String _left;
	private String _r1;
	private String _r2;
	private double _prob;
	
	public Rule(String left, String r1, String r2, double prob) {
		_left = left;
		_r1 = r1;
		_r2 = r2;
		_prob = prob;
	}
	
	public String getLeft() {
		return _left;
	}
	
	public String getR1() {
		return _r1;
	}
	
	public String getR2() {
		return _r2;
	}
	
	public double getProb() {
		return _prob;
	}
}
