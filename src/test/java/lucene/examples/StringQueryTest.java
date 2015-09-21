package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 9/21/15.
 */
public class StringQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testStringQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);

    // search "world" in "text" field then filter with "string" field
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query q = parser.parse("text:hello");
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals(1.1976817f, docs.scoreDocs[0].score, 0.02f);
    q = parser.parse("text:(hello hello)");
    docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    // 2 same terms in query will result in sqrt(2) score boost
    assertEquals(1.1976817f * Math.sqrt(2), docs.scoreDocs[0].score, 0.02f);
    reader.close();
  }

}


