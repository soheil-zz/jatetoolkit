package uk.ac.shef.dcs.oak.jate.core.feature;

import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndex;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;

import java.util.*;

/**
 */
class TermFreqCounterMultiThreadHelper {

    private Set<TermFreqCounterThread> counters;

    public TermFreqCounterMultiThreadHelper(int threads, Set<String> terms, String context, GlobalIndex index) {
        counters = new HashSet<TermFreqCounterThread>();
        //segment task set
        for (Set<String> seg : segmentSet(terms, threads)) {
            counters.add(new TermFreqCounterThread(new TermFreqCounter(), seg, context, index));
        }

    }

    public Map<String, Integer> count() {
        //start threads
        for (TermFreqCounterThread t : counters) {
            t.start();
        }

        //check status
        boolean finished = false;
        while (!finished) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            finished = true;
            for (TermFreqCounterThread t : counters) {
                if (!t.isFinished())
                    finished = false;
            }
        }
        //assemble results.
        Map<String, Integer> results = new HashMap<String, Integer>();
        for (TermFreqCounterThread t : counters) {
            results.putAll(t.getCounts());
            t.getCounts().clear();
        }
        return results;
    }

    private List<Set<String>> segmentSet(Set<String> terms, int segments) {
        List<Set<String>> segs = new ArrayList<Set<String>>(segments);

        int size = terms.size() / segments + terms.size() % segments;
        Iterator<String> it = terms.iterator();
        int count = 0;
        Set<String> seg = new HashSet<String>();

        while (it.hasNext()) {
            if (count >= size) {
                count = 0;
                segs.add(new HashSet<String>(seg));
                seg.clear();
            }

            if (count < size) {
                seg.add(it.next());
                count++;
            }
        }
        if(seg.size()>0)
            segs.add(new HashSet<String>(seg));
        return segs;
    }


    private class TermFreqCounterThread extends Thread {

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

}
