package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 6/5/14.
 */

public class RangeQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testStringRange() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Query q = TermRangeQuery.newStringRange("string", "abcd", "abcdef", true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(6, docs.totalHits);
    assertEquals("2", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    reader.close();
  }

  @Test
  public void testLongRange() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Query q = NumericRangeQuery.newLongRange("long", 3L, 5L, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(4, docs.totalHits);
    assertEquals("4", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    reader.close();
  }

  @Test
  public void testDoubleRange() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    Query q = NumericRangeQuery.newDoubleRange("double", 1.234567, 12.0, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals("1", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    reader.close();
  }

}


