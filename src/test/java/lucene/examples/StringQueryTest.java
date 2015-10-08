package lucene.examples;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 15/9/21
 */
public class StringQueryTest {
  static RAMDirectory dir = new RAMDirectory();
  static IndexReader reader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0, new StandardAnalyzer(CharArraySet.EMPTY_SET));
    IndexWriter writer = new IndexWriter(dir, config);

    Document doc = new Document();
    doc.add(new TextField("text", "hello world", Field.Store.NO));
    System.out.println("text field of doc 0 is \"hello world\"");
    // doc 0
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "hello hello world", Field.Store.NO));
    System.out.println("text field of doc 1 is \"hello hello world\"");
    // doc 1
    writer.addDocument(doc);

    doc = new Document();
    Field boost = new TextField("text", "hello world", Field.Store.NO);
    boost.setBoost(0.5f);
    doc.add(boost);
    System.out.println("text field of doc 2 is \"hello world\" with 0.5 boost");
    // doc 2
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "ok finished", Field.Store.NO));
    System.out.println("text field of doc 3 is \"ok finished\"\n");
    // doc 3
    writer.addDocument(doc);

    writer.forceMerge(1);
    writer.commit();
    writer.close();

    reader = DirectoryReader.open(dir);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    reader.close();
  }

  /**
   How to get the formula see http://www.cnblogs.com/forfuture1978/archive/2010/03/07/1680007.html
   How to calculate the formula see http://lucene.apache.org/core/4_0_0/core/org/apache/lucene/search/similarities/TFIDFSimilarity.html

   score(q,d) = coord(q,d)·queryNorm(q)· ∑	( tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d))
                                       t in q

   1. coord(q,d) is a score factor based on how many of the query terms are found in the specified document.

   @see org.apache.lucene.search.similarities.DefaultSimilarity#coord(int, int)

   coord(q,d) is meanless for term query since term query has only 1 term so coord(q, d) is always 1

                                                                    1
   2. queryNorm(q) = queryNorm(sumOfSquaredWeights)   =  -------––––––––––––––
                                                          sumOfSquaredWeights½

   sumOfSquaredWeights = q.getBoost()^2  · 	∑	( idf(t)  ·  t.getBoost() )^2
                                           t in q

   q.getBoost() is 1 by default, t.getBoost() is 1 by default

   3. idf(t) stands for Inverse Document Frequency. This value correlates to the inverse of docFreq (the number of documents in which the term t appears).

                                        numDocs
                 idf(t)  =     1 + log (---------)
                                        docFreq+1

   @see org.apache.lucene.search.similarities.DefaultSimilarity#idf(long, long)

   4. tf(t in d) correlates to the term's frequency, defined as the number of times term t appears in the currently scored document d.

                 tf(t in d)   =  	frequency½

   @see org.apache.lucene.search.similarities.DefaultSimilarity#tf(float)

   5. norm(t,d) encapsulates a few (indexing time) boost and length factors:

   norm(t,d)   =   lengthNorm  · 	∏	f.boost()
                        field f in d named as t

   Field boost - set by calling field.setBoost() before adding the field to a document.
   If the document has multiple fields with the same name, all their boosts are multiplied together
   lengthNorm - computed when the document is added to the index in accordance with the number of tokens of this field
   in the document, so that shorter fields contribute more to the score.

   @see org.apache.lucene.search.similarities.DefaultSimilarity#lengthNorm(FieldInvertState)

   lengthNorm =  1.0 / Math.sqrt(numTerms)

   LengthNorm of text field in the index from doc 0 to doc 3 are 1/sqrt(2), 1/sqrt(3), 1/sqrt(2), 1/sqrt(2)

   Field boost is 1 by default
   **/

  @Test
  public void testStringQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());


    /***********************************
     **********1st query****************
     ***********************************/
    Query q = parser.parse("text:hello");
    TopDocs docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    // text:hello query hits doc 0, 1, 2 and the relevance order is 1, 0, 2
    System.out.println("The 1st query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }

    /**
     For doc 1,
     q = "text:hello"
     coord(q, d) = 1；
     q.getBoost() = 1；
     t = "text:hello"
     t.getBoost() = 1;
     idf(t) = 1 + log(4/(3+1)) = 1;
     so queryNorm(q) = 1;
     coord(q, d), q.getBoost(), t.getBoost() and queryNorm(q) are same for all docs

     text field is doc 1 is "hello hello world"
     tf(t in d) = sqrt(2) = 1.414
     f.boost() = 1;
     lengthNorm = 1.0/sqrt(3) = 0.573
     lengthNorm is encoded as a byte in index and decode in search stage.
     This encoding/decoding, while reducing index size, comes with the price of precision loss
     decode(encode(0.573)) = 0.5
     @see NormValueEncodeDecodeTest
     norm(t,d) = f.boost() * lengthNorm = 0.573

     so the final score is coord(q,d)·queryNorm(q)·	(tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d))
     = 1 * 1 * 1.414 * 1^2 * 1 * 0.5 = 0.7071
     **/
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0.707, docs.scoreDocs[0].score, 0.001f);

    /**
     text field of doc 0 is "hello world"
     tf(t in d) = sqrt(1) = 1
     f.boost() = 1;
     lengthNorm = 1.0/sqrt(2) = 0.7071
     lengthNorm is encoded as a byte in index and decode in search stage.
     This encoding/decoding, while reducing index size, comes with the price of precision loss
     decode(encode(0.7071)) = 0.625
     @see NormValueEncodeDecodeTest

     so the final score is coord(q,d)·queryNorm(q)·	(tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d))
     = 1 * 1 * 1 * 1^2 * 1 * 0.625 = 0.625
     **/
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.625, docs.scoreDocs[1].score, 0.001f);


    /**
     doc 2 is same with doc 0 except text field is degraded by 0.5
     so all values are the same except norm(t,d) is half of doc 0
     final score is half of doc 0 = 0.625/2 = 0.3125
     **/
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(0.312, docs.scoreDocs[2].score, 0.001f);


    /***********************************
     **********2nd query****************
     ***********************************/
    /**
     Boost the whole query won't change score.
     Since the boost is cancelled by queryNorm
     **/
    q = parser.parse("text:hello");
    q.setBoost(3);
    docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    System.out.println("\nThe 2nd query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0.707, docs.scoreDocs[0].score, 0.001f);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.625, docs.scoreDocs[1].score, 0.001f);


    /***********************************
     **********3rd query****************
     ***********************************/
    /**
     BooleanQuery constructed by 2 same term query will result in sqrt(2) times score of one term query
     coord(q,d) is 1 since "text:hello" is matched
     sumOfSquaredWeights = q.getBoost()^2  · 	∑	( idf(t)  ·  t.getBoost() )^2  is double
     queryNorm(q) = 1/sqrt(sumOfSquaredWeights) is  1/sqrt(2) time of one term query
     ∑	( tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d)) this part is double, since the query have 2 same terms

     So final score is 1/sqrt(2)*2 = sqrt(2) time of one term query
     **/
    q = parser.parse("text:(hello hello)");
    docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    System.out.println("\nThe 3rd query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }
    Assert.assertEquals(0.707 * Math.sqrt(2.0), docs.scoreDocs[0].score, 0.001f);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.625 * Math.sqrt(2.0), docs.scoreDocs[1].score, 0.001f);
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(0.312 * Math.sqrt(2.0), docs.scoreDocs[2].score, 0.001f);


    /***********************************
     **********4th query****************
     ***********************************/
    /**
     "text:(hello world)" is a BooleanQuery with 2 term query "text:hello" and "text:world"
     */
    q = parser.parse("text:(hello world)");
    docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    System.out.println("\nThe 4th query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }

    /**
     text field of doc 0 is "hello world"

     coord(q,d) is 1 since both "text:hello" and "text:world" are matched

     idf("text:hello") = 1 + log(4/(3+1)) = 1;
     idf("text:world") = 1 + log(4/(3+1)) = 1;
     sumOfSquaredWeights = q.getBoost()^2  · 	∑	( idf(t)  ·  t.getBoost() )^2 = 1^2 * ((1 * 1)^2 + (1 * 1)^2) = 2
     queryNorm(q) = 1/sqrt(sumOfSquaredWeights) = 0.7071

     lengthNorm = 1.0/sqrt(2) = 0.7071
     norm(t,d) = lengthNorm * f.boost() = 0.7071
     decode(encode(0.7071)) = 0.625
     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1*1*0.625 = 0.625
     for term t = "text:world", it is the same result, tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1*1*0.625 = 0.625

     final score = 0.7071 * (0.625 + 0.625) = 0.8838
     */
    Assert.assertEquals(0, docs.scoreDocs[0].doc);
    Assert.assertEquals(0.883, docs.scoreDocs[0].score, 0.001f);


    /**
     text field of doc 1 is "hello hello world"

     coord(q,d) = 1;
     queryNorm(q) = 0.7071 without difference

     lengthNorm = 1.0/sqrt(3) = 0.573
     norm(t,d) = lengthNorm * f.boost() = 0.573
     decode(encode(0.573)) = 0.5
     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = sqrt(2)*1*1*0.5 = 0.7071
     for term t = "text:world", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1*1*0.5 = 0.5

     final score = 0.7071 * (0.7071 + 0.5) = 0.8535
     */
    Assert.assertEquals(1, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.853, docs.scoreDocs[1].score, 0.001f);

    /**
     score of doc 2 is half of doc 0 = 0.8838/2 = 0.4419
     */
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(0.4419, docs.scoreDocs[2].score, 0.001f);


    /***********************************
     **********5th query****************
     ***********************************/
    /**
     "text:(hello^2 world)^3" is a BooleanQuery with 2 term query "text:hello^2" and "text:world"
     the whole BooleanQuery boost is 3, the term query "text:hello^2" boost is 2
     */
    q = parser.parse("text:(hello^2 world)");
    q.setBoost(3);
    docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    System.out.println("\nThe 5th query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }


    /**
     text field of doc 1 is "hello hello world"

     coord(q,d) is 1 since both "text:hello" and "text:world" are matched

     idf("text:hello") = 1 + log(4/(3+1)) = 1;
     idf("text:world") = 1 + log(4/(3+1)) = 1;
     sumOfSquaredWeights = q.getBoost()^2  · 	∑	( idf(t)  ·  t.getBoost() )^2 = 3^2*((1*2)^2 + (1*1)^2) = 45
     queryNorm(q) = 1/sqrt(sumOfSquaredWeights) = 0.1490

     lengthNorm = 1.0/sqrt(3) = 0.573
     norm(t,d) = lengthNorm * f.boost() = 0.573
     decode(encode(0.573)) = 0.5
     boost of "text:hello" is 3*2 = 6
     boost of "text:world" is 3 = 6
     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = sqrt(2)*1^2*6*0.5 = 4.242
     for term t = "text:world", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1^2*3*0.5 = 1.5

     final score = 0.1490 * (4.242 + 1.5) = 0.8555
     */
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0.85, docs.scoreDocs[0].score, 0.01f);

    /**
     text field of doc 0 is "hello world"

     queryNorm(q) is still 0.1490

     lengthNorm = 1.0/sqrt(2) = 0.7071
     norm(t,d) = lengthNorm * f.boost() = 0.7071
     decode(encode(0.7071)) = 0.625

     boost of "text:hello" is 3*2 = 6
     boost of "text:world" is 3 = 6
     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1^2*6*0.625 = 3.75
     for term t = "text:world", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1^2*3*0.625 = 1.875

     final score = 0.1490 * (3.75 + 1.875) = 0.8381
     */
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.838, docs.scoreDocs[1].score, 0.001f);

    /**
     score of doc 2 is half of doc 0 = 0.8381/2 = 0.4190
     */
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(0.419, docs.scoreDocs[2].score, 0.001f);

    /***********************************
     **********6th query****************
     ***********************************/
    /**
     "text:(hello lucene)" is a BooleanQuery with 2 term query "text:hello" and "text:lucene"
     "text:lucene" query won't hit any doc, so it won't contribute any score
     */

    q = parser.parse("text:(hello lucene)");
    docs = searcher.search(q, 10);
    Assert.assertEquals(3, docs.totalHits);
    System.out.println("\nThe 6th query is \"" + q.toString() + "\" and relevance order is");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }


    /**
     text field of doc 1 is "hello hello world"

     coord(q,d) is 0.5 since "text:lucene" is not matched

     idf("text:hello") = 1 + log(4/(3+1)) = 1;
     idf("text:lucene") = 1 + log(4/(0+1)) = 2.386;
     sumOfSquaredWeights = q.getBoost()^2  · 	∑	( idf(t)  ·  t.getBoost() )^2 = 6.693
     queryNorm(q) = 1/sqrt(sumOfSquaredWeights) = 0.3865

     lengthNorm = 1.0/sqrt(3) = 0.573
     norm(t,d) = lengthNorm * f.boost() = 0.573
     decode(encode(0.573)) = 0.5
     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = sqrt(2)*1^2*1*0.5 = 0.7071
     for term t = "text:lucene", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 0

     final score =  coord(q,d)·queryNorm(q)· ∑	( tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d))
                =  0.5 * 0.3865 * (0.7071 + 0) = 0.1366
     */
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0.1366, docs.scoreDocs[0].score, 0.001f);

    /**
     text field of doc 0 is "hello world"

     queryNorm(q) is still 0.3865

     lengthNorm = 1.0/sqrt(2) = 0.7071
     norm(t,d) = lengthNorm * f.boost() = 0.7071
     decode(encode(0.7071)) = 0.625

     for term t = "text:hello", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 1*1^2*1*0.625 = 0.625
     for term t = "text:lucene", tf(t in d)·idf(t)^2·t.getBoost()·norm(t,d) = 0

     final score = 0.5*0.3865*(0.625 + 0) = 0.1207
     */
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(0.120, docs.scoreDocs[1].score, 0.001f);

    /**
     final score is half score of doc 0 = 0.1207/2 = 0.0603
     */
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(0.060, docs.scoreDocs[2].score, 0.001f);
  }
}


