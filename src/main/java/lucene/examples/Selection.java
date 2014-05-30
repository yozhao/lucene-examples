package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yozhao on 5/30/14.
 */
public class Selection extends TestCase {
  String indexDir = "/tmp/LuceneExamplesFilter";

  @Override
  public void setUp() throws Exception {
    File file = new File(indexDir);
    Util.deleteDir(file);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(
        Version.LUCENE_48));
    IndexWriter writer = new IndexWriter(
        FSDirectory.open(new File(indexDir)), config);

    List<Document> docList = new ArrayList<Document>();

    Document doc = new Document();
    doc.add(new IntField("id", 0, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abc", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 1));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 1, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abc", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 2));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(2.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 2, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 1));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 3, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 2));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 4, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 3));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 5, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcde", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 4));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 6, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcdef", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 3));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 7, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcdefg", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 2));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 8, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcde", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 4));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
    docList.add(doc);

    doc = new Document();
    doc.add(new IntField("id", 9, Field.Store.YES));
    // String
    doc.add(new StringField("string", "abcd", Field.Store.NO));
    // long
    doc.add(new NumericDocValuesField("long", 5));
    // double
    doc.add(new NumericDocValuesField("double", Double.doubleToRawLongBits(1.23456)));
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
  public void testStringFilter() throws Exception {
    DirectoryReader directoryReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
    IndexSearcher searcher = new IndexSearcher(directoryReader);
    final BooleanQuery query = new BooleanQuery();
    query.add(
        new TermQuery(new Term("string","abcde")),
        BooleanClause.Occur.MUST
    );
    TopDocs docs = searcher.search(query, 10);
    assertEquals(2, docs.totalHits);
    assertEquals("5", searcher.doc(docs.scoreDocs[0].doc).get("id"));
    assertEquals("8", searcher.doc(docs.scoreDocs[1].doc).get("id"));
  }

  @Override
  protected void tearDown() throws Exception {
    File file = new File(indexDir);
    Util.deleteDir(file);
  }
}


