package uk.ac.shef.dcs.oak.jate.core.feature;

import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndex;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
class TermFreqCounterThread extends Thread {

        private TermFreqCounter counter;
        private boolean finished;
        private Map<String, Integer> counts;
        private Set<String> terms;
        private String context;
        private GlobalIndex index;


        public TermFreqCounterThread(TermFreqCounter counter, Set<String> terms, String context, GlobalIndex index) {
            counts = new HashMap<String, Integer>();
            this.counter = counter;
            this.terms = terms;
            this.context = context;
            this.index = index;
        }

        @Override
        public void run() {
            count(terms, context, counts);
            setFinished(true);
        }

        private void count(Set<String> terms, String context, Map<String, Integer> counts) {
            for (String t : terms) {
                counts.put(t, counter.count(context, index.retrieveVariantsOfTermCanonical(t), false));
            }
        }

        public boolean isFinished() {
            return finished;
        }

        private void setFinished(boolean finished) {
            this.finished = finished;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }
    }

