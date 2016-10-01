package sqlsugg.scoring;

import sqlsugg.mapping.*;
import sqlsugg.scoring.schemascoring.*;
import sqlsugg.template.*;
import sqlsugg.template.tgraph.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.backends.*;
import sqlsugg.mapping.maps.*;

import java.util.*;

public class Scorer {
	SchemaGraph sg;
	public TableWeighter tweighter;
	public AttributeWeighter aweighter;
	
	public Scorer (SchemaGraph pSg, SQLBackend pSql, String dbname) throws Exception {
		sg = pSg;
		tweighter = new TableWeighter (dbname, pSql);
		tweighter.loadWeights();
		aweighter = new AttributeWeighter (dbname, pSql);
		aweighter.loadWeights();
	}
	
	public void computeOverallMapScore (Template template, KeywordMap map) 
		throws Exception {
		double tw = this.getRelationWeight(template, map.getRStr());
		double aw = aweighter.getWeight(map.getRStr(), map.getAStr());
		double score = tw * aw * map.score();
		map.setScore(score);
	}
	
	public double getRelationWeight (Template template, 
			String relation) throws Exception {
		double num = 0.0;
		double den = 0.0;
		
		for (RTNode rnode : template.getRTNodes()) {
			Relation trelation = rnode.relation;
			double score = tweighter.getWeight(trelation.getName());
			if (trelation.getName().equals(relation)) {
				num = score;
			}
			den += score;
		}
		return num / den;
	}
	
	public static double getDistanceWeight (List<String> keywords, String k1, String k2) {
		double score = 0.0;
		int index1 = keywords.indexOf(k1);
		int index2 = keywords.indexOf(k2);
		if (index1 == -1 || index2 == -1) {
			return 0.0;
		} else {
			double distance = (double)Math.abs(index1 - index2);
			score = Math.log10(1 + distance);
			return score;
		}
	}
	
	public double getK2FScore (Template template, 
			K2FMap k2fMap, K2MMap k2mMap, List<String> keywords) throws Exception {
		double distance = 
			getDistanceWeight (keywords, k2fMap.getKStr(), k2mMap.getKStr());
		double funcbility = k2fMap.score();
		double tw = getRelationWeight(template, k2mMap.getRStr());
		double aw = aweighter.getWeight(k2mMap.getRStr(), k2mMap.getAStr()); 
		double score = tw * aw * funcbility / distance;
		return score;
	}
}
