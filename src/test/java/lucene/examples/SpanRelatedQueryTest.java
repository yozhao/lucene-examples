package lucene.examples;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.junit.Test;

/**
 * Created by yozhao on 14-7-20.
 */
public class SpanRelatedQueryTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  List<String> dumpSpans(Spans spans) throws IOException {
    List<String> result = new ArrayList<String>();
    while (spans.next()) {
      int id = spans.doc();
      Document doc = reader.document(id);
      Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

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
      result.add(buffer.toString());
    }
    return result;
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
    List<String> log = dumpSpans(spans);
    assertEquals(1, log.size());
    assertEquals("the quick <brown> fox jumps over the lazy dog ", log.get(0));
    reader.close();
  }

  @Test
  public void testSpanFirstQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    SpanTermQuery fox = new SpanTermQuery(new Term("span", "fox"));
    SpanFirstQuery firstQuery = new SpanFirstQuery(fox, 3);
    TopDocs docs = searcher.search(firstQuery, 10);
    assertEquals(0, docs.totalHits);

    firstQuery = new SpanFirstQuery(fox, 4);
    docs = searcher.search(firstQuery, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
    Spans spans = firstQuery.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    List<String> log = dumpSpans(spans);
    assertEquals(2, log.size());
    assertEquals("the quick brown <fox> jumps over the lazy dog ", log.get(0));
    assertEquals("the quick red <fox> jumps over the sleepy cat ", log.get(1));
    reader.close();
  }

  @Test
  public void testSpanNearQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    SpanTermQuery quick = new SpanTermQuery(new Term("span", "quick"));
    SpanTermQuery brown = new SpanTermQuery(new Term("span", "brown"));
    SpanTermQuery lazy = new SpanTermQuery(new Term("span", "lazy"));
    SpanNearQuery nearQuery = new SpanNearQuery(new SpanQuery[] { quick,
        brown, lazy }, 3, true);
    TopDocs docs = searcher.search(nearQuery, 10);
    assertEquals(0, docs.totalHits);

    nearQuery = new SpanNearQuery(new SpanQuery[] { quick,
        brown, lazy }, 4, true);
    docs = searcher.search(nearQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
    Spans spans = nearQuery.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    List<String> log = dumpSpans(spans);
    assertEquals(1, log.size());
    assertEquals("the <quick brown fox jumps over the lazy> dog ", log.get(0));
    reader.close();
  }

  @Test
  public void testSpanNotQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    SpanTermQuery quick = new SpanTermQuery(new Term("span", "quick"));
    SpanTermQuery fox = new SpanTermQuery(new Term("span", "fox"));
    SpanNearQuery nearQuery = new SpanNearQuery(new SpanQuery[] { quick,
        fox }, 1, true);
    TopDocs docs = searcher.search(nearQuery, 10);
    assertEquals(2, docs.totalHits);

    Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
    Spans spans = nearQuery.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    List<String> log = dumpSpans(spans);
    assertEquals(2, log.size());
    assertEquals("the <quick brown fox> jumps over the lazy dog ", log.get(0));
    assertEquals("the <quick red fox> jumps over the sleepy cat ", log.get(1));

    SpanTermQuery brown = new SpanTermQuery(new Term("span", "brown"));
    SpanNotQuery notQuery = new SpanNotQuery(nearQuery, brown);

    docs = searcher.search(notQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("1", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    spans = notQuery.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    log = dumpSpans(spans);
    assertEquals(1, log.size());
    assertEquals("the <quick red fox> jumps over the sleepy cat ", log.get(0));
    reader.close();
  }

  @Test
  public void testSpanOrQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    SpanTermQuery quick = new SpanTermQuery(new Term("span", "quick"));
    SpanTermQuery fox = new SpanTermQuery(new Term("span", "fox"));
    SpanTermQuery lazy = new SpanTermQuery(new Term("span", "lazy"));
    SpanTermQuery dog = new SpanTermQuery(new Term("span", "dog"));

    SpanNearQuery quickFox = new SpanNearQuery(new SpanQuery[] { quick, fox }, 1, true);

    SpanNearQuery lazyDog = new SpanNearQuery(new SpanQuery[] { lazy, dog }, 0, true);

    SpanNearQuery nearQuery1 = new SpanNearQuery(new SpanQuery[] { quickFox, lazyDog }, 3, true);
    TopDocs docs = searcher.search(nearQuery1, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
    Spans spans = nearQuery1.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    List<String> log = dumpSpans(spans);
    assertEquals(1, log.size());
    assertEquals("the <quick brown fox jumps over the lazy dog> ", log.get(0));

    SpanTermQuery jumps = new SpanTermQuery(new Term("span", "jumps"));
    SpanTermQuery over = new SpanTermQuery(new Term("span", "over"));
    SpanNearQuery nearQuery2 = new SpanNearQuery(new SpanQuery[] { jumps, over }, 0, true);

    SpanOrQuery orQuery = new SpanOrQuery(new SpanQuery[] { nearQuery1, nearQuery2 });

    docs = searcher.search(orQuery, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("1", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    spans = orQuery.getSpans(reader.getContext().leaves().get(0), null, termContexts);
    log = dumpSpans(spans);
    assertEquals(3, log.size());
    assertEquals("the <quick brown fox jumps over the lazy dog> ", log.get(0));
    assertEquals("the quick brown fox <jumps over> the lazy dog ", log.get(1));
    assertEquals("the quick red fox <jumps over> the sleepy cat ", log.get(2));
    reader.close();
  }
}
