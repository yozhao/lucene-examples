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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.sorter.SortingMergePolicy;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * Created by yozhao on 5/10/15.
 */
public class SortingMergePolicyTest {

  @Test
  public void testDocTraverse() throws Exception {
    RAMDirectory dir = new RAMDirectory();
    IndexWriter indexWriter;
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0,
        new StandardAnalyzer(CharArraySet.EMPTY_SET));

    SortField idSortField = new SortField("sorted id", SortField.Type.INT);
    SortingMergePolicy sortingMergePolicy = new SortingMergePolicy(config.getMergePolicy(), new Sort(idSortField));
    config.setMergePolicy(sortingMergePolicy);

    indexWriter = new IndexWriter(dir, config);
    Random rand = new Random(System.currentTimeMillis());
    for (int i = 0; i < 100; ++i) {
      Document doc = new Document();
      doc.add(new IntField("sorted id", rand.nextInt(10000), Field.Store.NO));
      indexWriter.addDocument(doc);
    }
    indexWriter.commit();

    DirectoryReader topReader = DirectoryReader.open(dir);
    AtomicReader atomicReader = topReader.leaves().get(0).reader();
    FieldCache.Ints ints = FieldCache.DEFAULT.getInts(atomicReader, "sorted id", true);
    // Random order
    System.out.println("Random order before merge:");
    for (int j = 0; j < atomicReader.maxDoc(); ++j) {
      System.out.println("sorted id of doc " + j + " is " + ints.get(j));
    }

    // Merge
    indexWriter.forceMerge(1);
    indexWriter.commit();
    System.out.println("\nAscending order after merge:");
    topReader.close();
    topReader = DirectoryReader.open(dir);
    atomicReader = topReader.leaves().get(0).reader();
    ints = FieldCache.DEFAULT.getInts(atomicReader, "sorted id", true);
    int lastId = -1;
    for (int j = 0; j < atomicReader.maxDoc(); ++j) {
      Assert.assertTrue(lastId <= ints.get(j));
      lastId = ints.get(j);
      System.out.println("sorted id of doc " + j + " is " + lastId);
    }

    // close
    indexWriter.close();
    topReader.close();
  }
}
