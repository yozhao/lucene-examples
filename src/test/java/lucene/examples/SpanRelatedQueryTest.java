package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yozhao on 14-7-20.
 */
public class SpanRelatedQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testSpanTermQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    SpanTermQuery brown = new SpanTermQuery(new Term("span", "brown"));

    TopDocs docs = searcher.search(brown, 10);

    assertEquals(1, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
    Spans spans = brown.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    while (spans.next()) {
      int id = spans.doc();
      Document doc = reader.document(id);
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

      TokenStream stream = analyzer.tokenStream("", new StringReader(doc.get("span")));
      stream.reset();

      StringBuffer buffer = new StringBuffer();

      int i = 0;
      while (stream.incrementToken()) {
        if (i == spans.start()) {
          buffer.append("<");
        }
        buffer.append(stream.getAttribute(CharTermAttribute.class).toString());
        if (i + 1 == spans.end()) {
          buffer.append(">");
        }
        buffer.append(" ");
        i++;
      }
      assertEquals("quick brown <fox> jumps over lazy dog ", buffer.toString());
    }
    reader.close();
  }
}
