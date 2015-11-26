import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class parse {
	
	public static String debinarize(String[] input, HashMap<String, List<String>>[][] prevCells, 
			HashMap<String, List<Integer>>[][] coordCells, String label, int left, int right,
			Set<String> unaryPhrasals) {
		HashMap<String, List<String>> prev = prevCells[left][right];
		HashMap<String, List<Integer>> cd = coordCells[left][right];
		
		List<String> c1c2 = prev.get(label);
		List<Integer> cl = cd.get(label);
		System.out.println(label);
		System.out.println(cl);
		if(c1c2 == null) {
			return input[cl.get(0)]; //its left coordinate
		}
		if(c1c2.get(1) == null) {
			return "(" + label + " " + debinarize(input, prevCells, coordCells, c1c2.get(0),
					cl.get(0), cl.get(1), unaryPhrasals) + ")";
		}
		
		if(unaryPhrasals.contains(label)) {
			return "(" + label + " " + debinarize(input, prevCells, coordCells, c1c2.get(0),
				cl.get(0), cl.get(1), unaryPhrasals) + " " + debinarize(input, prevCells, 
						coordCells, c1c2.get(1), cl.get(1), cl.get(2), unaryPhrasals) + ")";
		}
		else {
			return debinarize(input, prevCells, coordCells, c1c2.get(0),
					cl.get(0), cl.get(1), unaryPhrasals) + " " + debinarize(input, prevCells, 
							coordCells, c1c2.get(1), cl.get(1), cl.get(2), unaryPhrasals);
		}
	}
	
	public static void fill(String[] input, List<Rule> rules, List<Rule> unaryRules,
			Set<String> unaryPhrasals, HashMap<String, Double>[][] probCells, 
			HashMap<String, List<String>>[][] prevCells, 
			HashMap<String, List<Integer>>[][] coordCells, int i, int k) {
		HashMap<String, Double> thisProbCell = new HashMap<String, Double>();
		HashMap<String, List<String>> thisPrevCell = new HashMap<String, List<String>>();
		HashMap<String, List<Integer>> thisCoordCell = new HashMap<String, List<Integer>>();
		
		if(k == i+1) {
			thisProbCell.put(input[i], 1.0);
			thisPrevCell.put(input[i], null);
			List<Integer> co = new ArrayList<Integer>(3);
			co.add(i);
			thisCoordCell.put(input[i], co);
		}
		else {
			for(int j = i+1; j < k; j++) {
				List<Integer> cds = new ArrayList<Integer>(3);
				cds.add(i);
				cds.add(j);
				cds.add(k);
				
				Iterator<String> phrasals_ij = probCells[i][j].keySet().iterator();
				while(phrasals_ij.hasNext()) {
					String ph1 = phrasals_ij.next();
					//System.out.println(i + ", " + j + ", " + ph1);
					Iterator<Rule> rls = rules.iterator();
					while(rls.hasNext()) {
						Rule r = rls.next();
						String r_left = r.getLeft();
						if(r.getR1().equals(ph1)) {
							Set<String> jk = probCells[j][k].keySet();
							jk.retainAll(unaryPhrasals);
							Iterator<String> phrasals_jk = jk.iterator();
							while(phrasals_jk.hasNext()) {
								String ph2 = phrasals_jk.next();
								//System.out.println(ph2);
								List<String> prevs = new ArrayList<String>(2);
								prevs.add(ph1);
								prevs.add(ph2);
								
								if(r.getR2().equals(ph2)) {
									double mu = r.getProb()*probCells[i][j].get(ph1)*probCells[j][k].get(ph2);
									boolean seen = false;
									if(thisProbCell.containsKey(r_left)) {
										if(thisProbCell.get(r_left) > mu) {
											seen = true;
										}
									}
									
									if(seen == false) {
										thisProbCell.put(r_left, mu);
										thisPrevCell.put(r_left, prevs);
										thisCoordCell.put(r_left, cds);
									}
								}
							}
						}
					}
				}
			}
		}
		
		Iterator<String> tc = new CopyOnWriteArrayList<String>(thisProbCell.keySet()).iterator();
		//System.out.println(thisProbCell.size());
		List<Integer> cdt = new ArrayList<Integer>(2);
		cdt.add(i);
		cdt.add(k);
		while(tc.hasNext()) {
			String phr = tc.next();
			
			List<String> prevs = new ArrayList<String>(2);
			prevs.add(phr);
			prevs.add(null);
			
			Iterator<Rule> unrules = unaryRules.iterator();
			while(unrules.hasNext()) {
				Rule r = unrules.next();
				String r_left = r.getLeft();
				double mu = thisProbCell.get(phr)*r.getProb();
				if(r.getR1().equals(phr)) {
					boolean seen = false;
					if(thisProbCell.containsKey(r_left)) {
						if(thisProbCell.get(r_left) > mu) {
							seen = true;
						}
					}
					if(seen == false) {
						thisProbCell.put(r_left, mu);
						thisPrevCell.put(r_left, prevs);
						thisCoordCell.put(r_left, cdt);
					}
				}
			}
		}
		
		probCells[i][k] = thisProbCell;
		prevCells[i][k] = thisPrevCell;
		coordCells[i][k] = thisCoordCell;
	}
	
	public static Set<String> getLeftPhrasals(List<Rule> rules) {
		Iterator<Rule> r = rules.iterator();
		Set<String> p = new HashSet<String>();
		while(r.hasNext()) {
			p.add(r.next().getLeft());
		}
		
		return p;
	}
	
	public static List<Rule> getUnaryRules(List<Rule> rules) {
		List<Rule> unaryRules = new ArrayList<Rule>();
		int rlsz = rules.size();
		for(int i = 0; i < rlsz; i++) {
			Rule ruleI = rules.get(i);
			if(ruleI.getR2() == "*NULL*") {
				unaryRules.add(ruleI);
			}
		}
		
		return unaryRules;
	}
	
	public static List<Rule> getRules(HashMap<List<String>, Integer> gc) {
		HashMap<String, Integer> parentCounts = new HashMap<String, Integer>();
		Iterator<List<String>> gcIter = gc.keySet().iterator();
		while(gcIter.hasNext()) {
			List<String> currgc = gcIter.next();
			String parent = currgc.get(0);
			if(parentCounts.containsKey(parent)) {
				parentCounts.put(parent, parentCounts.get(parent) + gc.get(currgc));
			}
			else {
				parentCounts.put(parent, gc.get(currgc));
			}
		}
		
		List<Rule> rules = new ArrayList<Rule>();
		Iterator<List<String>> gcIter2 = gc.keySet().iterator();
		while(gcIter2.hasNext()) {
			List<String> currgc2 = gcIter2.next();
			Rule curr = new Rule(currgc2.get(0), currgc2.get(1), currgc2.get(2), 
					1.0*gc.get(currgc2)/parentCounts.get(currgc2.get(0)));
			rules.add(curr);
		}
		
		return rules;
	}
	
	public static HashMap<List<String>, Integer> getGrammarCounts(List<String[]> rules) {
		int numRules = rules.size();
		HashMap<List<String>, Integer> gc = new HashMap<List<String>, Integer>();
		for(int i = 0; i < numRules; i++) {
			String[] rules_i = rules.get(i);
			List<String> rule = new ArrayList<String>(3);
			rule.add(rules_i[1]);
			rule.add(rules_i[3]);
			if(rules_i.length == 5) {
				rule.add(rules_i[4]);
			}
			else {
				rule.add("*NULL*");
			}
			gc.put(rule, Integer.parseInt(rules_i[0]));
		}
		
		return gc;
	}
	
	public static List<String[]> splitSentences(List<String> lines) {
		int numLines = lines.size();
		List<String[]> sentences = new ArrayList<String[]>(numLines);
		for(int i = 0; i < numLines; i++) {
			sentences.add(lines.get(i).split(" "));
		}
		
		return sentences;
	}
	
	public static List<String> readFile(Path file) {
		try {
			List<String> lines = Files.readAllLines(file, Charset.forName("ISO-8859-1"));
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		List<String[]> ruleStrings = splitSentences(readFile(Paths.get(args[0])));
		List<Rule> rules = getRules(getGrammarCounts(ruleStrings));
		List<Rule> unaryRules = getUnaryRules(rules);
		Set<String> up = getLeftPhrasals(unaryRules);
		
		List<String[]> sentences = splitSentences(readFile(Paths.get(args[1])));
		List<String> output = new ArrayList<String>();
		int snsz = sentences.size();
		for(int i = 0; i < 50; i++) {
			//System.out.println(i);
			String[] currIn = sentences.get(i);
			if(currIn.length > 25) {
				output.add("*IGNORE*");
			}
			else {
				HashMap<String, Double>[][] probCells = new HashMap[currIn.length+1][currIn.length+1];
				HashMap<String, List<String>>[][] prevCells = new HashMap[currIn.length+1][currIn.length+1];
				HashMap<String, List<Integer>>[][] coordCells = new HashMap[currIn.length+1][currIn.length+1];
				//for(int l = 1; l <= currIn.length; l++) {
				for(int l = 1; l <= currIn.length; l++) {
					for(int s = 0; s <= currIn.length-l; s++) {
						fill(currIn, rules, unaryRules, up, probCells, prevCells, coordCells, s, s+l);
						//System.out.println(s + ", " + (s+l));
						Iterator<String> it = prevCells[s][s+l].keySet().iterator();
						while(it.hasNext()) {
							String t = it.next();
							//System.out.println(t + ", " + prevCells[s][s+l].get(t));
						}
					}
				}
				System.out.println("Time after sentence " + (i+1) + ": " + (System.currentTimeMillis() - startTime)/1000);
				output.add(debinarize(currIn, prevCells, coordCells, "TOP", 0, currIn.length, up));
			}
		}
		
		
		
		Path out = Paths.get(args[2]);
		try {
			Files.write(out, output, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

