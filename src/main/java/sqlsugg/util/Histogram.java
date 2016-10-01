package sqlsugg.util;
/**
 * The histogram is to summarize the distribution of values of the type T.
 * In default, we assume that the range of the values is [0,1].
 * If the range of values is out of [0,1], it is better to normalize to this range.
 * @author jfan
 *
 */

import java.util.*;

public class Histogram {
	class Cell {
		double ub; //open interval
		double lb;//closed interval
		double avg;//the sum value of the T type.
		double freq;//the frequency that a T variable has values in the interval [lb, ub).
		
		Set<Double> div;
		
		public Cell (double l, double u) {
			lb = l;
			ub = u;
			avg = 0.0;
			freq = 0.0;
			div = new TreeSet<Double> ();
		}
		
		public String toString () {
			java.text.DecimalFormat f = new java.text.DecimalFormat("#0.00");   
			
			return "[" + //f.format(lb) + "," + 
						//f.format(ub) + "," + 
					    //f.format(avg) + "," +
						div.toString() +
						f.format(freq) + "]";
		}
	}
	
	List<Cell> cells = new LinkedList<Cell> ();
	double upperBound;
	double lowerBound;
	
	public Histogram (double lb, double ub, int num, boolean intBound) {
		upperBound = ub;
		lowerBound = lb;
		double step = (ub - lb) / num;
		if (step == 0.0) {
			System.out.println ("$");
		}
		double s = lb;
		while (s < ub) {
			Cell cell = null;
			if (intBound) {
				cell = new Cell ((int)(s + 1), (int)(s + step + 1));
			} else {
				cell = new Cell (s, s + step);
			}
			cells.add(cell);
			s += step;
		}
	}
	
	double representative (Cell c) {
		return c.avg;
	}
	
	Cell getCell (double value) {
		int count = 0;
		for (Cell c: cells) {
			if (c.lb <= value && 
					c.ub > value) {
				return c;
			} else {
				if (count == cells.size() - 1) {
					if (c.ub == value) {
						return c;
					}
				}
			}
			count ++;
		}
		//System.out.println ("Missing:" + value + "\t" + cells);
		return null;
	}
	

	public double getProb(Double value) {
		Cell c = this.getCell(value);
		return c.freq;
	}

	public List<Double> getValues() {
		List<Double> values = new LinkedList<Double> ();
		for (Cell c: cells) {
			if (c.freq > 0.0) {
				values.add(c.avg);
			}
		}
		return values;
	}

	public void addValue(Double value) {
		Cell c = this.getCell(value);
		c.avg += value;
		c.freq += 1;
		c.div.add(value);
	}

	public void normalize() {
		double sum = 0.0;
		for (Cell c: cells) {
			sum += c.freq;
			if (c.freq > 0.0) {
				c.avg /= c.freq;
			}
		}
		for (Cell c: cells) {
			c.freq /= sum;
		}
	}
	

	public double getCumProb (double value, Op op) {
		Cell c = this.getCell(value);
		if (c == null) {
			if (op == Op.EQUALS) {
				return 0.0;
			} else if (op == Op.NGT) {
				if (value >= upperBound) {
					c = cells.get(cells.size() - 1);
					double distance = Math.abs(value - c.avg);
					return 1 / Math.log10(10 + distance);
				}
				return 0.0;
			} else if (op == Op.NLT) {
				if (value <= upperBound) {
					c = cells.get(0);
					double distance = Math.abs(value - c.avg);
					return 1 / Math.log10(10 + distance);
				}
				return 0.0;
			}
		}
		if (op == Op.EQUALS) {
			return c.freq;
		} else if (op == Op.NGT) {
			double score = 0.0;
			boolean find = false;
			for (Cell fc : cells) {
				if (!find) {
					score += fc.freq;
				}
				if (fc == c) {
					find = true;
				}
			}
			return score;
		} else if (op == Op.NLT) {
			double score = 0.0;
			boolean find = false;
			for (Cell fc : cells) {
				if (fc == c) {
					find = true;
				}
				if (find) {
					score += fc.freq;
				}
			}
			return score;
		} else {
			return -1;
		}
	}

	public Double expectation() {
		double exp = 0.0;
		List<Double> values = this.getValues();
		for (double v: values) {
			exp += v * this.getProb(v);
		}
		return exp;
	}
	
	public String toString () {
		String ret = "";
		for (Cell c: cells) {
			if (c.freq > 0.0) {
				ret += c + ",";
			}
		}
		return ret;
	}
	
}
