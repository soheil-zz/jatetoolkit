package uk.ac.shef.dcs.oak.jate.debug;

import org.apache.log4j.Logger;
import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.*;
import uk.ac.shef.dcs.oak.jate.core.feature.*;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndex;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NGramExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.WordExtractor;
import uk.ac.shef.dcs.oak.jate.io.GlobalIndexWriterHSQL;
import uk.ac.shef.dcs.oak.jate.io.ResultWriter2File;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.model.Term;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AlgorithmTester {

	private Map<Algorithm, AbstractFeatureWrapper> _algregistry = new HashMap<Algorithm, AbstractFeatureWrapper>();
	private static Logger _logger = Logger.getLogger(AlgorithmTester.class);

	public void registerAlgorithm(Algorithm a, AbstractFeatureWrapper f) {
		_algregistry.put(a, f);
	}

	public void execute(GlobalIndex index) throws JATEException, IOException {
		_logger.info("Initializing outputter, loading NP mappings...");
		ResultWriter2File writer = new ResultWriter2File(index);
		if (_algregistry.size() == 0) throw new JATEException("No algorithm registered!");
		_logger.info("Running NP recognition...");

		/*.extractNP(c);*/
		for (Map.Entry<Algorithm, AbstractFeatureWrapper> en : _algregistry.entrySet()) {
			_logger.info("Running feature store builder and ATR..." + en.getKey().toString());
			Term[] result = en.getKey().execute(en.getValue());
			writer.output(result, en.getKey().toString() + ".txt");
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) System.out.println("Usage: java AlgorithmTester [corpus_path] [reference_corpus_path]");
		else {
			try {
				System.out.println(new Date());
				StopList stop = new StopList(true);
				Lemmatizer lemmatizer = new Lemmatizer();
				//CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(stop, lemmatizer);
                CandidateTermExtractor npextractor = new NGramExtractor(stop, lemmatizer);
				CandidateTermExtractor wordextractor = new WordExtractor(stop, lemmatizer);
				TermFreqCounter npcounter = new TermFreqCounter();
				WordCounter wordcounter = new WordCounter();

                /* IN-MEMORY INDEX
                   create an in-memory index builder
                  [Enable this block while disabling the HSQL index for smaller datasets and faster performance]
                 */

                GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
				GlobalIndexMem wordDocIndex = builder.build(new CorpusImpl(args[0]), wordextractor);
				GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(args[0]), npextractor);

                GlobalIndexWriterHSQL.persist(wordDocIndex, "D:\\work\\JATR_SDK\\jate_1.0\\test/worddb");
                GlobalIndexWriterHSQL.persist(termDocIndex, "D:\\work\\JATR_SDK\\jate_1.0\\test/termdb");


                /* HSQL INDEX
                If you use HSQL to store the index, you MUST CREATE SEPARATE GlobalIndexBuilderHSQL instance
                for each type of index, e.g., noun phrases, or words.
                [Enable this blcok while disabling the IN-MEMORY INDEX for larger datasets, but slower performance]
                 */
                /*GlobalIndexBuilderHSQL builderNPs = new GlobalIndexBuilderHSQL("D:/work/JATR_SDK/jate_1.0/test/nps");
                GlobalIndexBuilderHSQL builderWords = new GlobalIndexBuilderHSQL("D:/work/JATR_SDK/jate_1.0/test/words");
                GlobalIndexHSQL termDocIndex=builderNPs.build(new CorpusImpl(args[0]), npextractor);
                GlobalIndexHSQL wordDocIndex =builderWords.build(new CorpusImpl(args[0]), wordextractor);*/


                /*
                Build required features (subject to your algorithms of choice) using the index
                 */
				FeatureCorpusTermFrequency wordFreq =
						//new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(wordDocIndex);
                        new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter, lemmatizer).build(wordDocIndex);
				FeatureDocumentTermFrequency termDocFreq =
						//new FeatureBuilderDocumentTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
                        new FeatureBuilderDocumentTermFrequencyMultiThread(wordcounter, lemmatizer).build(termDocIndex);
				FeatureTermNest termNest =
						//new FeatureBuilderTermNest().build(termDocIndex);
                        new FeatureBuilderTermNestMultiThread().build(termDocIndex);
				FeatureRefCorpusTermFrequency bncRef =
						new FeatureBuilderRefCorpusTermFrequency(args[1]).build(null);
				FeatureCorpusTermFrequency termCorpusFreq =
						//new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
                        new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter, lemmatizer).build(termDocIndex);

				AlgorithmTester tester = new AlgorithmTester();
				tester.registerAlgorithm(new TFIDFAlgorithm(), new TFIDFFeatureWrapper(termCorpusFreq));
				tester.registerAlgorithm(new GlossExAlgorithm(), new GlossExFeatureWrapper(termCorpusFreq, wordFreq, bncRef));
				tester.registerAlgorithm(new WeirdnessAlgorithm(), new WeirdnessFeatureWrapper(wordFreq, termCorpusFreq, bncRef));
				tester.registerAlgorithm(new CValueAlgorithm(), new CValueFeatureWrapper(termCorpusFreq, termNest));
				tester.registerAlgorithm(new TermExAlgorithm(), new TermExFeatureWrapper(termDocFreq, wordFreq, bncRef));

				tester.execute(termDocIndex);
				System.out.println(new Date());

                /*termDocIndex.closeDatabase();
                wordDocIndex.closeDatabase();*/
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
