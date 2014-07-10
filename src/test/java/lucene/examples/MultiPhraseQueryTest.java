package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 7/10/14.
 */
public class MultiPhraseQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testMultiPhraseQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    MultiPhraseQuery q = new MultiPhraseQuery();
    q.add(new Term("text", "hello"));
    q.add(new Term[]{new Term("text", "lucene"), new Term("text", "world")});
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    reader.close();
  }
}