package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 4/3/15.
 */
public class TermQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testTermQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Term term = new Term("text", "hello");
    TermQuery termQuery = new TermQuery(term);
    termQuery.setBoost(0);
    TopDocs docs = searcher.search(termQuery, 10);
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println(searcher.doc(docs.scoreDocs[i].doc).get("id") + ":"
          + docs.scoreDocs[i].score);
    }
    assertEquals(1, docs.totalHits);
    assertEquals("3", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    reader.close();
  }
}
