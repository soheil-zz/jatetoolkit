package uk.ac.shef.dcs.oak.jate.core.npextractor;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.JATEProperties;
import uk.ac.shef.dcs.oak.jate.model.Corpus;
import uk.ac.shef.dcs.oak.jate.util.control.Normalizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.model.Document;

import java.util.Map;
import java.util.Set;

/**
 * Extract lexical units from texts.
 *
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */


public abstract class CandidateTermExtractor {
	protected StopList _stoplist;
	protected Normalizer _normaliser;

	/**
	 * @param c corpus
	 * @return a map containing mappings from term canonical form to its variants found in the corpus
	 * @throws uk.ac.shef.dcs.oak.jate.JATEException
	 */
	public abstract Map<String, Set<String>> extract(Corpus c) throws JATEException;

	/**
	 * @param d document
	 * @return a map containing mappings from term canonical form to its variants found in the document
	 * @throws uk.ac.shef.dcs.oak.jate.JATEException
	 */
	public abstract Map<String, Set<String>> extract(Document d) throws JATEException;

	/**
	 * @param content a string
	 * @return a map containing mappings from term canonical form to its variants found in the string
	 * @throws uk.ac.shef.dcs.oak.jate.JATEException
	 */
	public abstract Map<String, Set<String>> extract(String content) throws JATEException;

	protected boolean containsLetter(String np) {
		char[] chars = np.toCharArray();
		for (char c : chars) {
			if (Character.isLetter(c)) return true;
		}
		return false;
	}

	protected boolean containsDigit(String word) {
		for (char c : word.toCharArray()) {
			if (Character.isDigit(c)) return true;
		}
		return false;
	}

    protected String applyNPCharRemoval(String in) {
        return in.replaceAll(JATEProperties.NP_FILTER_PATTERN, " ").replaceAll("\\s+", " ").trim();
    }

    protected String[] applyNPSplitList(String in) {
        StringBuilder sb = new StringBuilder();
        if (in.indexOf(" and ") != -1) {
            String[] parts = in.split("\\band\\b");
            for (String s : parts) sb.append(s.trim() + "|");
        }
        if (in.indexOf(" or ") != -1) {
            String[] parts = in.split("\\bor\\b");
            for (String s : parts) sb.append(s.trim() + "|");
        }
        if (in.indexOf(",") != -1) {
            if (!containsDigit(in)) {
                String[] parts = in.split("\\,");
                for (String s : parts) sb.append(s.trim() + "|");
            }
        } else {
            sb.append(in);
        }
        String v = sb.toString();
        if (v.endsWith("|")) v = v.substring(0, v.lastIndexOf("|"));
        return v.toString().split("\\|");
    }

    protected String applyNPTrimStopwords(String in, StopList stop) {
        //check the entire string first (e.g., "e. g. " and "i. e. " which will fail the following checks
        if (stop.isStopWord(_normaliser.normalize(in).replaceAll("\\s+", "").trim())) return null;

        String[] e = in.split("\\s+");
        if (e == null || e.length < 1) return in;

        int head = e.length;
        int end = -1;
        for (int i = 0; i < e.length; i++) {
            if (!stop.isStopWord(e[i])) {
                head = i;
                break;
            }
        }

        for (int i = e.length - 1; i > -1; i--) {
            if (!stop.isStopWord(e[i])) {
                end = i;
                break;
            }
        }

        if (head <= end) {
            String trimmed = "";
            for (int i = head; i <= end; i++) {
                trimmed += e[i] + " ";
            }
            return trimmed.trim();
        }
        return null;
    }

    protected boolean hasReasonableNumChars(String s) {
        int len = s.length();
        if (len < 2) return false;
        if (len < 5) {
            char[] chars = s.toCharArray();
            int num = 0;
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLetter(chars[i]) || Character.isDigit(chars[i]))
                    num++;
                if (num > 2) break;
            }
            if (num > 2) return true;
            return false;
        }
        return true;
    }
}
