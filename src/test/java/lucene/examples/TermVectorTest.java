package lucene.examples;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.junit.Test;

/**
 * Created by yozhao on 4/8/15.
 */
public class TermVectorTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testTermVector() throws Exception {
    Terms terms = reader.getTermVector(0, "tv0");
    System.out.println("Term vector field value: " + reader.document(0).get("tv0"));
    Assert.assertEquals(1, terms.getDocCount());
    Assert.assertEquals(8, terms.getSumDocFreq());
    System.out.println(terms.getSumTotalTermFreq());
    TermsEnum termsEnum = null;
    DocsAndPositionsEnum docsAndPositionsEnum = null;
    termsEnum = terms.iterator(termsEnum);
    while ((termsEnum.next()) != null) {
      docsAndPositionsEnum = termsEnum.docsAndPositions(null, docsAndPositionsEnum);
      Assert.assertNotNull(docsAndPositionsEnum);
      docsAndPositionsEnum.nextDoc();
      int freq = docsAndPositionsEnum.freq();
      String info = termsEnum.term().utf8ToString() + " occurs " + termsEnum.totalTermFreq() + " times, ";
      for (int i = 0; i < freq; ++i) {
        int position = docsAndPositionsEnum.nextPosition();
        int start = docsAndPositionsEnum.startOffset();
        int end = docsAndPositionsEnum.endOffset();
        if (i > 0) {
          info += " ";
        }
        info += position + ":" + start + ":" + end;
      }
      System.out.println(info);
    }
  }
}
