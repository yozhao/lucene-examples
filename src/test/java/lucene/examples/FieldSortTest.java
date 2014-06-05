package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 6/5/14.
 */
public class FieldSortTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testStringSort() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    // Sort all docs
    Sort sort = new Sort(new SortField("string", SortField.Type.STRING));
    Query q = new MatchAllDocsQuery();
    TopDocs docs = searcher.search(q, 10, sort);
    assertEquals(10, docs.totalHits);
    assertEquals("7", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("0", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[6].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[7].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[8].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[9].doc).get("id"));
    // Sort docs in a range reversely
    q = TermRangeQuery.newStringRange("string", "abcd", "abcdef", true, true);
    sort = new Sort(new SortField("string", SortField.Type.STRING, true));
    docs = searcher.search(q, 10, sort);
    assertEquals(6, docs.totalHits);
    assertEquals("6", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[5].doc).get("id"));

    reader.close();
  }

  @Test
  public void testLongSort() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    // Sort all docs
    Sort sort = new Sort(new SortField("long", SortField.Type.LONG));
    Query q = new MatchAllDocsQuery();
    TopDocs docs = searcher.search(q, 10, sort);
    assertEquals(10, docs.totalHits);
    assertEquals("8", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("0", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[6].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[7].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[8].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[9].doc).get("id"));
    // Sort docs in a range
    q = TermRangeQuery.newStringRange("string", "abcd", "abcdef", true, true);
    docs = searcher.search(q, 10, sort);
    assertEquals(6, docs.totalHits);
    assertEquals("8", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[5].doc).get("id"));
  }

  @Test
  public void testDoubleSort() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    // Sort all docs
    Sort sort = new Sort(new SortField("double", SortField.Type.DOUBLE));
    Query q = new MatchAllDocsQuery();
    TopDocs docs = searcher.search(q, 10, sort);
    assertEquals(10, docs.totalHits);
    assertEquals("6", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("0", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[6].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[7].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[8].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[9].doc).get("id"));
    // Sort docs in a range
    q = TermRangeQuery.newStringRange("string", "abcd", "abcdef", true, true);
    docs = searcher.search(q, 10, sort);
    assertEquals(6, docs.totalHits);
    assertEquals("6", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[5].doc).get("id"));
  }

  @Test
  public void testCombinedSort() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    // Sort all docs
    Sort sort = new Sort(new SortField[] { new SortField("string", SortField.Type.STRING),
        new SortField("double", SortField.Type.DOUBLE) });
    Query q = new MatchAllDocsQuery();
    TopDocs docs = searcher.search(q, 10, sort);
    assertEquals("7", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("0", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    assertEquals("4", searcher.doc(docs.scoreDocs[6].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[7].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[8].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[9].doc).get("id"));
    // Sort docs in a range
    q = TermRangeQuery.newStringRange("string", "abcd", "abcdef", true, true);
    sort = new Sort(new SortField[] { new SortField("string", SortField.Type.STRING),
        new SortField("double", SortField.Type.DOUBLE, true) });
    docs = searcher.search(q, 10, sort);
    assertEquals(6, docs.totalHits);
    assertEquals("4", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("5", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[5].doc).get("id"));
  }
}



