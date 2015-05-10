package lucene.examples;

import java.util.Random;

import junit.framework.Assert;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.sorter.SortingMergePolicy;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 5/10/15.
 */
public class SortingMergePolicyTest {
  static RAMDirectory dir = new RAMDirectory();
  static IndexReader topReader;
  static AtomicReader atomicReader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0,
        new StandardAnalyzer(CharArraySet.EMPTY_SET));

    SortField idSortField = new SortField("id", SortField.Type.INT);
    SortingMergePolicy sortingMergePolicy = new SortingMergePolicy(config.getMergePolicy(), new Sort(idSortField));
    config.setMergePolicy(sortingMergePolicy);

    IndexWriter writer = new IndexWriter(dir, config);
    Random rand = new Random(System.currentTimeMillis());
    for (int i = 0; i < 1000; ++i) {
      Document doc = new Document();
      doc.add(new IntField("id", rand.nextInt(10000), Field.Store.NO));
      writer.addDocument(doc);
    }
    writer.forceMerge(1);
    writer.commit();
    writer.close();
    topReader = DirectoryReader.open(dir);
    atomicReader = topReader.leaves().get(0).reader();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    topReader.close();
  }

  @Test
  public void testDocTraverse() throws Exception {
    FieldCache.Ints ints = FieldCache.DEFAULT.getInts(atomicReader, "id", true);
    int lastId = -1;
    for (int j = 0; j < atomicReader.maxDoc(); ++j) {
      Assert.assertTrue(lastId <= ints.get(j));
      lastId = ints.get(j);
    }
  }
}
