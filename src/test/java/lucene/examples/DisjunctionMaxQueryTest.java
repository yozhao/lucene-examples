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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 15/9/21.
 */
public class DisjunctionMaxQueryTest {

  static RAMDirectory dir = new RAMDirectory();
  static IndexReader reader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0, new StandardAnalyzer(CharArraySet.EMPTY_SET));
    IndexWriter writer = new IndexWriter(dir, config);

    Document doc = new Document();
    doc.add(new TextField("text", "hello world", Field.Store.NO));
    System.out.println("text field of doc 0 is \"hello world\"");
    // doc 0
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "hello hello world", Field.Store.NO));
    System.out.println("text field of doc 1 is \"hello hello world\"");
    // doc 1
    writer.addDocument(doc);

    doc = new Document();
    Field boost = new TextField("text", "hello lucene", Field.Store.NO);
    doc.add(boost);
    System.out.println("text field of doc 2 is \"hello lucene\"");
    // doc 2
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("text", "ok finished", Field.Store.NO));
    System.out.println("text field of doc 3 is \"ok finished\"\n");
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
  public void testDisMaxQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(0.5f);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query q1 = parser.parse("text:hello");
    Query q2 = parser.parse("text:world");
    disMaxQuery.add(q1);
    disMaxQuery.add(q2);
    TopDocs docs = searcher.search(disMaxQuery, 10);
    Assert.assertEquals(3, docs.scoreDocs.length);
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }
    Assert.assertEquals(0, docs.scoreDocs[0].doc);
    Assert.assertEquals(1, docs.scoreDocs[1].doc);
    Assert.assertEquals(2, docs.scoreDocs[2].doc);

    System.out.println("\n");

    // doc 1 gets highest score due to boosted "text:hello^2" query
    q1.setBoost(2);
    docs = searcher.search(disMaxQuery, 10);
    Assert.assertEquals(3, docs.scoreDocs.length);
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    System.out.println("\n");

    // tieBreakerMultiplier = 0 means the max score of sub-queries is final score
    disMaxQuery = new DisjunctionMaxQuery(0);
    // "text:hello^10"
    q1.setBoost(10);
    disMaxQuery.add(q1);
    // "text:world"
    disMaxQuery.add(q2);
    docs = searcher.search(disMaxQuery, 10);
    Assert.assertEquals(3, docs.scoreDocs.length);
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      System.out.println("Doc " + docs.scoreDocs[i].doc + " score:" + docs.scoreDocs[i].score);
    }

    // q1 is boosted by 10, score of "text:hello^10" is higher than score of "text:world"
    // so the scores of hit docs are all determined by "text:hello"
    // doc 0 and doc 2 have same score since "text:hello" frequency and "text" field length are the same
    Assert.assertEquals(1, docs.scoreDocs[0].doc);
    Assert.assertEquals(0, docs.scoreDocs[1].doc);
    Assert.assertEquals(2, docs.scoreDocs[2].doc);
    Assert.assertEquals(docs.scoreDocs[1].score, docs.scoreDocs[2].score, 0.001);
  }
}


