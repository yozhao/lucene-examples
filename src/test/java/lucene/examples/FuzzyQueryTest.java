package lucene.examples;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 15/12/22.
 */
// Fuzzy query searches docs under an edit distance filter
public class FuzzyQueryTest {
  static RAMDirectory dir = new RAMDirectory();
  static IndexReader reader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0, new StandardAnalyzer(CharArraySet.EMPTY_SET));
    IndexWriter writer = new IndexWriter(dir, config);

    Document doc = new Document();
    doc.add(new TextField("text", "hello world", Field.Store.NO));
    // doc 0
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "hello lucene", Field.Store.NO));
    // doc 1
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "holly lucene", Field.Store.NO));
    // doc 2
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "helloo lucene", Field.Store.NO));
    // doc 3
    writer.addDocument(doc);

    writer.forceMerge(1);
    writer.commit();
    writer.close();

    reader = DirectoryReader.open(dir);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    reader.close();
  }


  @Test
  public void testFuzzyQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    FuzzyQuery query = new FuzzyQuery(new Term("text", "hello"), 0);

    TopDocs docs = searcher.search(query, 10);
    Assert.assertEquals(2, docs.totalHits);
    Assert.assertEquals(0, docs.scoreDocs[0].doc);
    Assert.assertEquals(1, docs.scoreDocs[1].doc);

    query = new FuzzyQuery(new Term("text", "hello"), 1);
    docs = searcher.search(query, 10);
    Assert.assertEquals(3, docs.totalHits);
    Assert.assertEquals(3, docs.scoreDocs[0].doc);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(1, docs.scoreDocs[2].doc);

    query = new FuzzyQuery(new Term("text", "hello"), 2);
    docs = searcher.search(query, 10);
    Assert.assertEquals(4, docs.totalHits);
    Assert.assertEquals(3, docs.scoreDocs[0].doc);
    Assert.assertEquals(2, docs.scoreDocs[1].doc);
    Assert.assertEquals(0, docs.scoreDocs[2].doc);
    Assert.assertEquals(1, docs.scoreDocs[3].doc);

    query = new FuzzyQuery(new Term("text", "hello"), 2, 2);
    docs = searcher.search(query, 10);
    Assert.assertEquals(3, docs.totalHits);
    Assert.assertEquals(3, docs.scoreDocs[0].doc);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(1, docs.scoreDocs[2].doc);
  }
}
