package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yozhao on 5/30/14.
 */
public class Selection extends TestCase {
  private final Directory indexDir = new RAMDirectory();

  @Override
  public void setUp() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(
        Version.LUCENE_48));
    IndexWriter writer = new IndexWriter(indexDir, config);

    List<Document> docList = new ArrayList<Document>();

    Document doc = new Document();
    doc.add(new IntField("id", 0, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abc", Field.Store.NO));
    // Text
    doc.add(new TextField("text", "hello world!", Field.Store.NO));
    // long
    doc.add(new LongField("long", 1, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.23456, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 1, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abc", Field.Store.NO));
    // long
    doc.add(new LongField("long", 2, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.234567, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 2, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // Text
    doc.add(new TextField("text", "hello world!", Field.Store.NO));
    // long
    doc.add(new LongField("long", 1, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.234561, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 3, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // Text
    doc.add(new TextField("text", "hello lucene!", Field.Store.NO));
    // long
    doc.add(new LongField("long", 2, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.23456, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 4, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // long
    doc.add(new LongField("long", 3, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 2.23456, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 5, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcde", Field.Store.NO));
    // long
    doc.add(new LongField("long", 4, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.234559, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 6, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcdef", Field.Store.NO));
    // long
    doc.add(new LongField("long", 3, Field.Store.NO));
    // No double field
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 7, Field.Store.YES));
    // No String field
    // long
    doc.add(new LongField("long", 2, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 1.23456, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 8, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcde", Field.Store.NO));
    // No long field
    // double
    doc.add(new DoubleField("double", 11.23456, Field.Store.NO));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 9, Field.Store.YES));
    // No String field
    // long
    doc.add(new LongField("long", 5, Field.Store.NO));
    // double
    doc.add(new DoubleField("double", 12.3456, Field.Store.NO));
    docList.add(doc);

    int count = 0;
    for (Document d : docList) {
      writer.addDocument(d);
      // make sure we get 2 segments
      if (++count % 5 == 0) {
        writer.commit();
      }
    }
    writer.commit();
    writer.close();
  }

  @Test
  public void testStringSelection() throws Exception {
    DirectoryReader directoryReader = DirectoryReader.open(indexDir);
    IndexSearcher searcher = new IndexSearcher(directoryReader);
    // selection of abcde in "string" field
    Query q = new TermQuery(new Term("string", "abcde"));
    TopDocs docs = searcher.search(q, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("5", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // selection of all the docs has "string" field
    q = new WildcardQuery(new Term("string", "*"));
    docs = searcher.search(q, 10);
    assertEquals(8, docs.totalHits);

    // selection of all the docs don't have "string" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        new WildcardQuery(new Term("string", "*")),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("7", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("9", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // search "world" in "text" field then filter with "string" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello lucene");
    TermQuery tQuery = new TermQuery(new Term("string", "abcd"));
    QueryWrapperFilter filter = new QueryWrapperFilter(tQuery);
    docs = searcher.search(q, filter, 10);
    // return doc 3 and doc 2, doc 3 is first since matching score is higher
    assertEquals(2, docs.totalHits);
    assertEquals("3", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    directoryReader.close();
  }

  @Test
  public void testLongSelection() throws Exception {
    DirectoryReader directoryReader = DirectoryReader.open(indexDir);
    IndexSearcher searcher = new IndexSearcher(directoryReader);
    Query q = NumericRangeQuery.newLongRange("long", 3L, 3L, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("4", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("6", searcher.doc(docs.scoreDocs[1].doc).get("id"));

    // selection of all the docs has "long" field
    q = NumericRangeQuery.newLongRange("long", Long.MIN_VALUE, Long.MAX_VALUE, true, true);
    docs = searcher.search(q, 10);
    assertEquals(9, docs.totalHits);

    // selection of all the docs don't have "long" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        NumericRangeQuery.newLongRange("long", Long.MIN_VALUE, Long.MAX_VALUE, true, true),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("8", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    // search "world" in "text" field then filter with "long" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello");
    NumericRangeQuery rQuery = NumericRangeQuery.newLongRange("long", 1L, 1L, true, true);
    QueryWrapperFilter filter = new QueryWrapperFilter(rQuery);
    docs = searcher.search(q, filter, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("2", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    directoryReader.close();
  }

  @Test
  public void testDoubleSelection() throws Exception {
    DirectoryReader directoryReader = DirectoryReader.open(indexDir);
    IndexSearcher searcher = new IndexSearcher(directoryReader);
    Query q = NumericRangeQuery.newDoubleRange("double", 1.23456, 1.23456, true, true);
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    assertEquals("7", searcher.doc(docs.scoreDocs[2].doc).get("id"));

    // selection of all the docs has "long" field
    q = NumericRangeQuery.newDoubleRange("double", Double.MIN_VALUE, Double.MAX_VALUE, true, true);
    docs = searcher.search(q, 10);
    assertEquals(9, docs.totalHits);

    // selection of all the docs don't have "long" field
    BooleanQuery bQuery = new BooleanQuery();
    MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
    bQuery.add(
        allDocsQuery,
        BooleanClause.Occur.MUST
    );
    bQuery.add(
        NumericRangeQuery.newDoubleRange("double", Double.MIN_VALUE, Double.MAX_VALUE, true, true),
        BooleanClause.Occur.MUST_NOT
    );
    docs = searcher.search(bQuery, 10);
    assertEquals(1, docs.totalHits);
    assertEquals("6", searcher.doc(docs.scoreDocs[0].doc).get("id"));

    // search "world" in "text" field then filter with "long" field
    QueryParser parser = new QueryParser(Version.LUCENE_48, "text",
        new StandardAnalyzer(Version.LUCENE_48));
    q = parser.parse("Hello");
    NumericRangeQuery rQuery = NumericRangeQuery.newDoubleRange("double", 1.23456, 1.23456, true,
        true);
    QueryWrapperFilter filter = new QueryWrapperFilter(rQuery);
    docs = searcher.search(q, filter, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("0", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("3", searcher.doc(docs.scoreDocs[1].doc).get("id"));
    directoryReader.close();
  }

}


