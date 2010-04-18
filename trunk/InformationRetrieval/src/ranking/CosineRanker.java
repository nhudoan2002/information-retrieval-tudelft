package ranking;

import indexing.DocumentIndex;
import indexing.TokenAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;



public class CosineRanker {
	
	static private double MIN_SPECIALITY = 3;

	private CosineRanker(){}
	
	public static List<Integer> rankingResults (String inputQuery, int k_top, boolean highIDF){
		
		List <String> queryParsed = takeOutQueryTerms(inputQuery);
		if (highIDF)
			getHighIDFQueryTerms(queryParsed);
		
			
		return calculateRankings (queryParsed, k_top);
	}
	
	private static List<Integer> calculateRankings (List<String> inputQuery, int k_top){//, TreeMap<Integer, Integer> docIDsLenghts){
		//access to DocumentIndex.document_IDs_And_Lenghts
//		List<Integer> ranked = fullyClone(unranked);
		List<Integer> ranked = new ArrayList<Integer>();
		double w_f_and_q = 0.0;
		Double d;
		HashMap<Integer, Double> docIDtoScore = new HashMap<Integer, Double>();
		
		MaxHeap scores = new MaxHeap();
		

		for ( String term: inputQuery){
			
			if(term == null || term == "")
				break;
			//
			System.out.println(DocumentIndex.instance().getTermPostingList(term).size() + " - " + DocumentIndex.instance().getTermPosting(term).postingListOfWeights.size());
			//
						
			w_f_and_q = speciality(term);
			for (Integer i: DocumentIndex.instance().getTermPostingList(term) ){
				
				d = DocumentIndex.instance().getTermPosting(term).postingListOfWeights.get(i) * w_f_and_q;
				if ( ! docIDtoScore.containsKey(i) )
					docIDtoScore.put(i, d);
				else
					docIDtoScore.put(i, d + docIDtoScore.get(i));
			}
			
			DocumentIndex.instance().getTermPosting(term);
		}
		
		for(Integer i : docIDtoScore.keySet()){
			scores.enqueue(new Score_DocID_Combination(i, docIDtoScore.get(i)));
		}
		int loop = Math.min(k_top, scores.size());
		assert (k_top <= scores.size());
		for(int i=0; i < loop; i++ )
			ranked.add(scores.dequeue().docID);
		return ranked;
		
	}
	
	private static double speciality (String term){
		
		return Math.log10(DocumentIndex.instance().document_IDs_And_Lenghts.size()/DocumentIndex.instance().getTermPostingList(term).size());
	}
	
	private static List<String> takeOutQueryTerms (String inputQuery){
		
		ArrayList<String> out = new ArrayList<String>();

		TokenAnalyzer ta = new TokenAnalyzer(inputQuery);
        String term = null;
		
		while ((term = ta.getNextToken()) != null) {
            if (!out.contains(term))
            	out.add(term);
        }
		return out;
	}
	
	private static void getHighIDFQueryTerms (List<String> queryTerms){
		
		int i = 0;
		while(i < queryTerms.size()){
			if (speciality(queryTerms.get(i)) < CosineRanker.MIN_SPECIALITY)
				queryTerms.remove(i);
			else
				i++;
		}
	}
	
	private static List<Integer> fullyClone (List<Integer> toBeCloned){
		
		List<Integer> cloned = new ArrayList<Integer>();
		for (Integer i: toBeCloned)
			cloned.add(Integer.valueOf(i.intValue()));
		return cloned;
	}
}