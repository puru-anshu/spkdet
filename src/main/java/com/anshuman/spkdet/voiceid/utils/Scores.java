package com.anshuman.spkdet.voiceid.utils;


import com.anshuman.spkdet.voiceid.db.Identifier;

import java.util.*;
import java.util.Map.Entry;

public class Scores extends HashMap<Identifier, Double> {

	private static final long serialVersionUID = 599647055140479507L;

	public HashMap<Identifier, Double> getBest(Strategy[] strategy)
			throws Exception {
		Scores results = (Scores) this.clone();
		for (Strategy strat : strategy)
			results = strat.filter(results);
		return results;
	}

	public HashMap<Identifier, Double> getBest() throws Exception {
		Strategy[] s = { new Best(1) };
		return getBest(s);
	}

	public HashMap<Identifier, Double> getBestFive() throws Exception {
		Strategy[] s = { new Best(5) };
		return getBest(s);
	}

	public String toString() {
		String str = "{";
		String k = null;
		for (Identifier key : this.keySet()) {
			k = key.toString();
			if (k.equals("lenght"))
				continue;
			str += " " + k + ": " + this.get(key).toString() + ",";
		}
		return str + "}";
	}
	
	public void putAllSync(Map<? extends Identifier, ? extends Double> m){
		synchronized (this) {
//			System.out.println("AAAAAAAAAAAAAAAAA     "+m.toString());
			super.putAll(m);
		}
	}

}


class Best implements Strategy {

	class CustomComparator implements Comparator<Entry<Identifier, Double>> {
		public int compare(Entry<Identifier, Double> o1,
				Entry<Identifier, Double> o2) {
			return -Double.compare(o1.getValue(), o2.getValue());
		}
	}

	int index;

	public Best(int index) {
		super();
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ubona.voiceid.utils.Strategy#filter(com.ubona
	 * .voiceid.utils.Scores)
	 */
	@Override
	public Scores filter(Scores score) {
		ArrayList<Entry<Identifier, Double>> al = new ArrayList<Entry<Identifier, Double>>();
		al.addAll(score.entrySet());
		Collections.sort(al, new CustomComparator());
		Scores out = new Scores();
		for (int i = 0; i < index && i < al.size(); i++) {
			out.put(al.get(i).getKey(), al.get(i).getValue());
		}
		return out;
	}

}