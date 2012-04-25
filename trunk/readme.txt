Please make sure the two property files in folder jatr_v1.0/test are copied to your compiled class folder before running
the application. Also you may need to change the settings in jatr.properties.



The toolkit is implemented for developing and experimenting Automatic Term Recognition algorithms in Java.
The implementation follows discussion in paper "Z. Zhang, J. Iria, C. Brewster and F. Ciravegna. 
A Comparative Evaluation of Term Recognition Algorithms. In Proceedings of The sixth international conference 
on Language Resources and Evaluation, (LREC 2008), May 28-31, 2008, Marrakech, Morocco.", available at 
http://www.dcs.shef.ac.uk/~ziqizhang/

It has implemented 5 algorithms discussed in the paper and tested under same settings (incl. preprocessing of
corpus, candidate term normalisation etc). 

Please note that we do not claim to have replicated the process of each ATR method mentioned in reference literature,
but the real "ranking algorithm", i.e., given a list of candidate terms (lexicon units such as n-grams, noun phrases),
how does each ATR method rank them according to how much it believes a candidate belongs to the domain that the corpus is
represents. The ultimate purpose is to put the algorithms under same condition and compare their performances.

Therefore you may notice certain adaptations for some of the algorithms mentioned, such as:

C-value - no frequency threshold has been applied to filter the candidate term list; applied to both single- and multi
          word candidate terms

GlossEx - does not filter pre-modifier

TermEx - does not take into account of text structure (we were not able to because the paper does not mention details
         regarding to this)


Thank you for you interest in this tool, we hope it can be useful to you!