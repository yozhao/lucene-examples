package lucene.examples;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
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
  public void testTermVectorWithPositionsAndOffsets() throws Exception {
    System.out.println("testTermVectorWithPositionsAndOffsets");
    Terms terms = reader.getTermVector(0, "tv0");
    System.out.println("Term vector field value: " + reader.document(0).get("tv0"));
    Assert.assertEquals(1, terms.getDocCount());
    Assert.assertEquals(8, terms.getSumDocFreq());
    TermsEnum termsEnum = null;
    DocsAndPositionsEnum docsAndPositionsEnum = null;
    termsEnum = terms.iterator(termsEnum);
    while ((termsEnum.next()) != null) {
      docsAndPositionsEnum = termsEnum.docsAndPositions(null, docsAndPositionsEnum);
      Assert.assertNotNull(docsAndPositionsEnum);
      docsAndPositionsEnum.nextDoc();
      int freq = docsAndPositionsEnum.freq();
      String info = termsEnum.term().utf8ToString() + " occurs " + freq + " times, ";
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
    System.out.println("\n");
  }


  @Test
  public void testTermVectorWithoutPositionsOrOffsets() throws Exception {
    System.out.println("testTermVectorWithoutPositionsOrOffsets");
    Terms terms = reader.getTermVector(0, "tv1");
    Assert.assertEquals(1, terms.getDocCount());
    Assert.assertEquals(8, terms.getSumDocFreq());
    TermsEnum termsEnum = null;
    DocsEnum docsEnum = null;
    DocsAndPositionsEnum docsAndPositionsEnum = null;
    termsEnum = terms.iterator(termsEnum);
    while ((termsEnum.next()) != null) {
      docsEnum = termsEnum.docs(null, docsEnum);
      docsEnum.nextDoc();
      System.out.println(termsEnum.term().utf8ToString() + " occurs " + docsEnum.freq() + " times ");
      docsAndPositionsEnum = termsEnum.docsAndPositions(null, docsAndPositionsEnum);
      Assert.assertNull(docsAndPositionsEnum);
    }
    System.out.println("\n");
    DelimitedPayloadTokenFilter
  }

  @Test
  public void testTermVectorWithPositions() throws Exception {
    System.out.println("testTermVectorWithPositions");
    Terms terms = reader.getTermVector(0, "tv2");
    System.out.println("Term vector field value: " + reader.document(0).get("tv0"));
    Assert.assertEquals(1, terms.getDocCount());
    Assert.assertEquals(8, terms.getSumDocFreq());
    TermsEnum termsEnum = null;
    DocsAndPositionsEnum docsAndPositionsEnum = null;
    termsEnum = terms.iterator(termsEnum);
    while ((termsEnum.next()) != null) {
      docsAndPositionsEnum = termsEnum.docsAndPositions(null, docsAndPositionsEnum);
      Assert.assertNotNull(docsAndPositionsEnum);
      docsAndPositionsEnum.nextDoc();
      int freq = docsAndPositionsEnum.freq();
      String info = termsEnum.term().utf8ToString() + " occurs " + freq + " times, ";
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
    System.out.println("\n");
  }

  @Test
  public void testTermVectorWithOffsets() throws Exception {
    System.out.println("testTermVectorWithOffsets");
    Terms terms = reader.getTermVector(0, "tv3");
    System.out.println("Term vector field value: " + reader.document(0).get("tv0"));
    Assert.assertEquals(1, terms.getDocCount());
    Assert.assertEquals(8, terms.getSumDocFreq());
    TermsEnum termsEnum = null;
    DocsAndPositionsEnum docsAndPositionsEnum = null;
    termsEnum = terms.iterator(termsEnum);
    while ((termsEnum.next()) != null) {
      docsAndPositionsEnum = termsEnum.docsAndPositions(null, docsAndPositionsEnum);
      Assert.assertNotNull(docsAndPositionsEnum);
      docsAndPositionsEnum.nextDoc();
      int freq = docsAndPositionsEnum.freq();
      String info = termsEnum.term().utf8ToString() + " occurs " + freq + " times, ";
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
    System.out.println("\n");
  }

  @Test
  public void testTermVectorWithoutVector() throws Exception {
    System.out.println("testTermVectorWithoutVector");
    Terms terms = reader.getTermVector(0, "tv4");
    Assert.assertEquals(null, terms);
    System.out.println("\n");
  }
}
