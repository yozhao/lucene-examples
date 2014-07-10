package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.junit.Test;

/**
 * Created by yozhao on 7/10/14.
 */
public class WildcardQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testWildcardQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Term term = new Term("text", "hel*");
    WildcardQuery q = new WildcardQuery(term);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[2].doc).get("id"));

    term = new Term("text", "lu*e");
    q = new WildcardQuery(term);
    docs = searcher.search(q, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("3", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    term = new Term("text", "lu?ne");
    q = new WildcardQuery(term);
    docs = searcher.search(q, 10);
    assertEquals(0, docs.totalHits);

    term = new Term("text", "lu??ne");
    q = new WildcardQuery(term);
    docs = searcher.search(q, 10);
    assertEquals(1, docs.totalHits);

    term = new Term("text", "h?l*");
    q = new WildcardQuery(term);
    docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);

    reader.close();
  }
}