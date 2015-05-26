package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * Created by yozhao on 1/6/15.
 */
public class FieldTypeTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0, new StandardAnalyzer(CharArraySet.EMPTY_SET));
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, config);

    FieldType typeNormal = new FieldType();
    typeNormal.setIndexed(true);

    FieldType typeOmitFreq = new FieldType();
    typeOmitFreq.setIndexed(true);
    typeOmitFreq.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);

    FieldType typeOmitNorms = new FieldType();
    typeOmitNorms.setIndexed(true);
    typeOmitNorms.setOmitNorms(true);

    Document doc = new Document();
    Field field = new Field("normal", "小米", typeNormal);
    doc.add(field);
    field = new Field("omitFreq", "小米", typeOmitFreq);
    doc.add(field);
    field = new Field("omitNorms", "小米", typeOmitNorms);
    doc.add(field);
    writer.addDocument(doc);

    doc = new Document();
    field = new Field("normal", "小米小米", typeNormal);
    doc.add(field);
    field = new Field("omitFreq", "小米小米", typeOmitFreq);
    doc.add(field);
    field = new Field("omitNorms", "小米小米", typeOmitNorms);
    doc.add(field);
    writer.addDocument(doc);

    doc = new Document();
    field = new Field("normal", "小米小米小米", typeNormal);
    doc.add(field);
    field = new Field("omitFreq", "小米小米小米", typeOmitFreq);
    doc.add(field);
    field = new Field("omitNorms", "小米小米小米", typeOmitNorms);
    doc.add(field);
    writer.addDocument(doc);
    writer.commit();
    writer.close();

    reader = DirectoryReader.open(dir);
  }

  @Test
  public void testNormalFieldType() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query q = parser.parse("normal:小米");
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals(1, docs.scoreDocs[0].doc);
    assertEquals(2, docs.scoreDocs[1].doc);
    assertEquals(0, docs.scoreDocs[2].doc);

    System.out.println("########################Normal explanations:########################");
    Explanation explanation = searcher.explain(q, 1);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 2);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 0);
    System.out.println(explanation.toString());
  }


  @Test
  public void testOmitFreqFieldType() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query q = parser.parse("omitFreq:小米");
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals(0, docs.scoreDocs[0].doc);
    assertEquals(1, docs.scoreDocs[1].doc);
    assertEquals(2, docs.scoreDocs[2].doc);

    System.out.println("########################OmitFreq explanations:########################");
    Explanation explanation = searcher.explain(q, 0);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 1);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 2);
    System.out.println(explanation.toString());
  }

  @Test
  public void testOmitNormsFieldType() throws Exception {
    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query q = parser.parse("omitNorms:小米");
    TopDocs docs = searcher.search(q, 10);
    assertEquals(3, docs.totalHits);
    assertEquals(2, docs.scoreDocs[0].doc);
    assertEquals(1, docs.scoreDocs[1].doc);
    assertEquals(0, docs.scoreDocs[2].doc);

    System.out.println("########################OmitNorms explanations:########################");
    Explanation explanation = searcher.explain(q, 2);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 1);
    System.out.println(explanation.toString());
    explanation = searcher.explain(q, 0);
    System.out.println(explanation.toString());
  }

}
