package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.junit.Test;

/**
 * Created by yozhao on 08/08/2017.
 */
public class IdfDumpTest extends TestCase {

  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testIdfDump() throws Exception {
    DefaultSimilarity similarity = new DefaultSimilarity();
    int docnum = reader.numDocs();
    Fields fields = MultiFields.getFields(reader);
    for (String field : fields) {
      Terms terms = fields.terms(field);
      TermsEnum termsEnum = terms.iterator(null);
      while (termsEnum.next() != null) {
        double idf = similarity.idf(termsEnum.docFreq(), docnum);
        System.out.println("" + field + ":" + termsEnum.term().utf8ToString() + " idf=" + idf);
      }
    }
  }
}