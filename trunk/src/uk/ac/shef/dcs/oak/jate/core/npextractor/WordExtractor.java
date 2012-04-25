package uk.ac.shef.dcs.oak.jate.core.npextractor;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.JATEProperties;
import uk.ac.shef.dcs.oak.jate.model.Corpus;
import uk.ac.shef.dcs.oak.jate.model.Document;
import uk.ac.shef.dcs.oak.jate.util.control.Normalizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extracts words from texts. Words will be lemmatised to reduce deviations. Characters which are not one of the followings
 * are replaced by whitespaces:
 * <br>letter, digit, -
 *
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */

public class WordExtractor extends CandidateTermExtractor {

	/**
	 * Creates an instance with specified stopwords list and normaliser
	 * @param stop a list of words which are unlikely to occur in a domain specific candidate term
	 * @param normaliser an instance of a Normalizer which returns candidate term to canonical form
	 */
	public WordExtractor(StopList stop, Normalizer normaliser) {
		_stoplist = stop;
		_normaliser = normaliser;
	}

	public Map<String,Set<String>> extract(Corpus c) throws JATEException {
		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		for (Document d : c) {
			for(Map.Entry<String, Set<String>> e: extract(d).entrySet()){
				Set<String> variants = res.get(e.getKey());
				variants=variants==null?new HashSet<String>():variants;
				variants.addAll(e.getValue());
				res.put(e.getKey(),variants);
			}
		}

		return res;
	}

	public Map<String,Set<String>> extract(Document d) throws JATEException {
		return extract(d.getContent());
	}

	public Map<String,Set<String>> extract(String content) throws JATEException {
		String[] words = content.replaceAll(JATEProperties.NP_FILTER_PATTERN, " ").replaceAll("\\s+", " ").trim().split(" ");
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();

		for (String w : words) {
			String nw=w.trim();
			//
			nw=nw.toLowerCase();
			//if(_stoplist.isStopWord(nw)) continue;
			nw= _normaliser.normalize(nw);
			//
			if(!containsLetter(nw)&&!containsDigit(nw)) continue;
			//String lemma = _normaliser.normalize(w.trim());
			//word should be treated separately to NP, as different forms of a word should be treated separately in counting
			if (nw.length()>0) {
				Set<String> variants = result.get(nw);
				variants=variants==null?new HashSet<String>():variants;
				variants.add(w);
				result.put(nw,variants);
			}
/*			String lemma = _normaliser.normalize(w.trim());
			if (lemma.length()>0) {
				result.add(lemma);
			}*/
		}
		return result;
	}
}
