package lucene.examples;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.join.FixedBitSetCachingWrapperFilter;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinCollector;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JoinSearchTest {

  static RAMDirectory dir = new RAMDirectory();
  static IndexReader reader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0,
        new StandardAnalyzer(CharArraySet.EMPTY_SET));
    IndexWriter writer = new IndexWriter(dir, config);

    // index order matters
    Document doc = new Document();
    doc.add(new StringField("dealId", "0", Field.Store.YES));
    doc.add(new TextField("dealName", "hello lucene", Field.Store.YES));
    doc.add(new StringField("parentId", "0", Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("dealId", "1", Field.Store.YES));
    doc.add(new TextField("dealName", "hello world", Field.Store.YES));
    doc.add(new StringField("parentId", "0", Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("poiId", "0", Field.Store.YES));
    doc.add(new TextField("poiName", "poi 0", Field.Store.YES));
    doc.add(new StringField("docType", "poi", Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("dealId", "2", Field.Store.YES));
    doc.add(new TextField("dealName", "hello hello lucene", Field.Store.YES));
    doc.add(new StringField("parentId", "1", Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("dealId", "3", Field.Store.YES));
    doc.add(new TextField("dealName", "hello lucene world", Field.Store.YES));
    doc.add(new StringField("parentId", "1", Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("poiId", "1", Field.Store.YES));
    doc.add(new TextField("poiName", "poi 1", Field.Store.YES));
    doc.add(new StringField("docType", "poi", Field.Store.YES));
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
  public void testQueryTimeJoin() throws Exception {
    IndexSearcher indexSearcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query fromQuery = parser.parse("dealName:hello");
    String fromField = "parentId";
    String toField = "poiId";
    Query toQuery = JoinUtil.createJoinQuery(fromField, false, toField, fromQuery, indexSearcher, ScoreMode.Max);
    TopDocs results = indexSearcher.search(toQuery, 10);
    for (int i = 0; i < results.scoreDocs.length; ++i) {
      System.out.println("Parent doc poiId is " + indexSearcher.doc(results.scoreDocs[i].doc).get("poiId"));
      System.out
          .println("Parent doc score is " + results.scoreDocs[i].score + " which is max score of children's scores");
    }
  }

  @Test
  public void testIndexTimeJoin() throws Exception {
    IndexSearcher indexSearcher = new IndexSearcher(reader);
    Filter parentFilter = new FixedBitSetCachingWrapperFilter(
        new QueryWrapperFilter(new TermQuery(new Term("docType", "poi"))));
    QueryParser parser = new QueryParser("", new StandardAnalyzer());
    Query fromQuery = parser.parse("dealName:hello");
    ToParentBlockJoinQuery query = new ToParentBlockJoinQuery(fromQuery, parentFilter, ScoreMode.Avg);

    ToParentBlockJoinCollector collector = new ToParentBlockJoinCollector(Sort.RELEVANCE, 10, true, true);
    indexSearcher.search(query, collector);
    TopGroups<Integer> groups = collector.getTopGroupsWithAllChildDocs(query, Sort.RELEVANCE, 0, 0, true);
    for (int i = 0; i < groups.totalGroupCount; ++i) {
      System.out.println("group " + i + " poi id is " + indexSearcher.doc(groups.groups[i].groupValue).get("poiId"));
      System.out
          .println("group " + i + " score is " + groups.groups[i].score + " which is avg score of children's score");
      System.out.println(
          "group " + i + " max score is " + groups.groups[i].maxScore + " which is max score of children's score");
      for (int j = 0; j < groups.groups[i].scoreDocs.length; ++j) {
        System.out.println(
            "Children doc " + j + " deal id is " + indexSearcher.doc(groups.groups[i].scoreDocs[j].doc).get("dealId"));
        System.out.println(
            "Children doc " + j + " score is " + groups.groups[i].scoreDocs[j].score);
      }
      System.out.println("");
    }
  }

}


