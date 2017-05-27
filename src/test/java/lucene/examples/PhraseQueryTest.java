package lucene.examples;

import java.io.StringReader;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

/**
 * Created by yozhao on 7/7/14.
 */
public class PhraseQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testPhraseQuery1() throws Exception {
    System.out.println("testPhraseQuery1");
    System.out.println("==================================================");
    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new StandardAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("刘德"));
    stream.reset();

    PhraseQuery q = new PhraseQuery();
    q.setSlop(0);
    while (stream.incrementToken()) {
      q.add(new Term("artist", stream.getAttribute(CharTermAttribute.class).toString()));
    }

    TopDocs docs = searcher.search(q, 10);
    assertEquals(5, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    System.out.println("Hits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("artist: " + searcher.doc(docs.scoreDocs[i].doc).get("artist"));
    }
    System.out.println("");

    q.setSlop(1);
    docs = searcher.search(q, 10);
    assertEquals(6, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[2].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[3].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[4].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[5].doc).get("id"));
    System.out.println("Hits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("artist: " + searcher.doc(docs.scoreDocs[i].doc).get("artist"));
    }
    System.out.println("");
    reader.close();
  }

  @Test
  public void testPhraseQuery2() throws Exception {
    System.out.println("testPhraseQuery2");
    System.out.println("==================================================");

    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new StandardAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("天意刘"));
    stream.reset();

    PhraseQuery q = new PhraseQuery();
    q.setSlop(0);
    while (stream.incrementToken()) {
      q.add(new Term("song", stream.getAttribute(CharTermAttribute.class).toString()));
    }

    TopDocs docs = searcher.search(q, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    System.out.println("Hits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("song: " + searcher.doc(docs.scoreDocs[i].doc).get("song"));
    }
    System.out.println("");
    reader.close();
  }

  @Test
  public void testPhraseQuery3() throws Exception {
    System.out.println("testPhraseQuery3");
    System.out.println("==================================================");

    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new StandardAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("刘德华 柯受良 吴宗宪"));
    stream.reset();

    PhraseQuery q = new PhraseQuery();
    q.setSlop(0);
    while (stream.incrementToken()) {
      q.add(new Term("artist", stream.getAttribute(CharTermAttribute.class).toString()));
    }

    TopDocs docs = searcher.search(q, 10);
    assertEquals(1, docs.totalHits);

    System.out.println("Hits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("artist: " + searcher.doc(docs.scoreDocs[i].doc).get("artist"));
      System.out.println("song: " + searcher.doc(docs.scoreDocs[i].doc).get("song"));
    }
    System.out.println("");
    reader.close();
  }

  @Test
  public void testPhraseQuery4() throws Exception {
    System.out.println("testPhraseQuery4");
    System.out.println("==================================================");

    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new StandardAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("天意"));
    stream.reset();

    PhraseQuery q1 = new PhraseQuery();
    q1.setSlop(0);
    PhraseQuery q2 = new PhraseQuery();
    q2.setSlop(0);
    while (stream.incrementToken()) {
      String term = stream.getAttribute(CharTermAttribute.class).toString();
      q1.add(new Term("artist", term));
      q2.add(new Term("song", term));
    }

    BooleanQuery bQuery = new BooleanQuery(true);
    bQuery.add(q2, BooleanClause.Occur.SHOULD);
    bQuery.add(q1, BooleanClause.Occur.SHOULD);
    TopDocs docs = searcher.search(bQuery, 10);
    assertEquals(5, docs.totalHits);

    System.out.println("Hits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("artist: " + searcher.doc(docs.scoreDocs[i].doc).get("artist"));
      System.out.println("song: " + searcher.doc(docs.scoreDocs[i].doc).get("song"));
      System.out.println("score: " + docs.scoreDocs[i].score);
    }
    System.out.println("");
    reader.close();
  }

  @Test
  public void testPhraseQuery5() throws Exception {
    System.out.println("testPhraseQuery5");
    System.out.println("==================================================");

    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query query = parser.parse("span:(\"quick fox\"~1)");

    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(query, 10);

    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    System.out.println("Query: " + query + "\nHits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("span: " + searcher.doc(docs.scoreDocs[i].doc).get("span"));
    }
    System.out.println("");

    query = parser.parse("span:(\"quick jumps\"~1)");
    docs = searcher.search(query, 10);
    assertEquals(0, docs.totalHits);

    query = parser.parse("span:(\"fox quick\"~2)");
    docs = searcher.search(query, 10);
    assertEquals(0, docs.totalHits);

    query = parser.parse("span:(\"fox quick\"~3)");
    docs = searcher.search(query, 10);
    assertEquals(2, docs.totalHits);
    System.out.println("Query: " + query + "\nHits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("span: " + searcher.doc(docs.scoreDocs[i].doc).get("span"));
    }
    System.out.println("");

    query = parser.parse("span:(\"fox quick dog\"~5)");
    docs = searcher.search(query, 10);
    assertEquals(0, docs.totalHits);

    query = parser.parse("span:(\"fox quick dog\"~6)");
    docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits);
    System.out.println("Query: " + query + "\nHits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("span: " + searcher.doc(docs.scoreDocs[i].doc).get("span"));
    }
    System.out.println("");

    query = parser.parse("span:(\"lazy jumps quick\"~7)");
    docs = searcher.search(query, 10);
    assertEquals(0, docs.totalHits);

    query = parser.parse("span:(\"lazy jumps quick\"~8)");
    docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits);
    System.out.println("Query: " + query + "\nHits:");
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("span: " + searcher.doc(docs.scoreDocs[i].doc).get("span"));
    }
    System.out.println("");

    reader.close();
  }

  @Test
  public void testPhraseQuery6() throws Exception {
    System.out.println("testPhraseQuery6");
    System.out.println("==================================================");

    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query query = parser.parse("phrase:(\"quick dog\"~10)");

    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(query, 10);

    assertEquals(2, docs.totalHits);
    assertEquals("1", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("0", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    for (int i = 0; i < docs.totalHits; ++i) {
      System.out.println("id: " + searcher.doc(docs.scoreDocs[i].doc).get("id"));
      System.out.println("span: " + searcher.doc(docs.scoreDocs[i].doc).get("phrase"));
      System.out.println("score: " + docs.scoreDocs[i].score);
    }
  }
}


