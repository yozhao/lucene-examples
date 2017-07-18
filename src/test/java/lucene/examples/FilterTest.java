package lucene.examples;

import java.io.IOException;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.TermFilter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 18/07/2017.
 */
public class FilterTest extends TestCase {

  static IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testBooleanFilter() throws IOException {
    BooleanFilter booleanFilter = new BooleanFilter();
    TermFilter termFilter = new TermFilter(new Term("string", "abc"));
    booleanFilter.add(termFilter, BooleanClause.Occur.MUST);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(new MatchAllDocsQuery(), booleanFilter, 10);
    assertEquals(2, docs.totalHits);

    // SHOULD clause must be matched at least one
    booleanFilter = new BooleanFilter();
    booleanFilter.add(termFilter, BooleanClause.Occur.MUST);
    TermFilter nonExistentTermFilter  = new TermFilter(new Term("string", "no-such-value"));
    booleanFilter.add(nonExistentTermFilter, BooleanClause.Occur.SHOULD);
    docs = searcher.search(new MatchAllDocsQuery(), booleanFilter, 10);
    assertEquals(0, docs.totalHits);

    booleanFilter = new BooleanFilter();
    booleanFilter.add(termFilter, BooleanClause.Occur.MUST_NOT);
    docs = searcher.search(new MatchAllDocsQuery(), booleanFilter, 10);
    assertEquals(8, docs.totalHits);

    BooleanQuery booleanQuery = new BooleanQuery();
    TermQuery termQuery = new TermQuery(new Term("string", "abc"));
    booleanQuery.add(termQuery, BooleanClause.Occur.MUST_NOT);
    docs = searcher.search(booleanQuery, 10);
    assertEquals(0, docs.totalHits);
  }
}
