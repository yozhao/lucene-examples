package lucene.examples;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 4/9/15.
 */
public class DocValuesTest {
  static final String NUMERIC_FIELD = "numeric";
  static final String SORTED_FIELD = "sorted";
  static final String SORTEDSET_FIELD = "sortedset";

  static RAMDirectory dir = new RAMDirectory();
  static long[] numericVals = new long[] {12, 13, 0, 100, 19};
  static String[] sortedVals = new String[] {"lucene", "facet", "abacus", "search", null};
  static String[][] sortedSetVals = new String[][] {{"lucene", "search"}, {"search"}, {"facet", "abacus", "search"},
      {}};

  static IndexReader topReader;
  static AtomicReader atomicReader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48));
    IndexWriter writer = new IndexWriter(dir, config);
    for (int i = 0; i < numericVals.length; ++i) {
      Document doc = new Document();
      doc.add(new NumericDocValuesField(NUMERIC_FIELD, numericVals[i]));
      doc.add(new SortedDocValuesField(SORTED_FIELD, new BytesRef(sortedVals[i])));
      String[] sortedSetVal = sortedSetVals[i];
      for (String value : sortedSetVal) {
        doc.add(new SortedSetDocValuesField(SORTEDSET_FIELD, new BytesRef(value)));
      }
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
  public void testNumericDocValues() throws Exception {
    NumericDocValues docVals = atomicReader.getNumericDocValues(NUMERIC_FIELD);
    Assert.assertEquals(12, docVals.get(0));
    Assert.assertEquals(13, docVals.get(1));
    Assert.assertEquals(0, docVals.get(2));
    Assert.assertEquals(100, docVals.get(3));
    Assert.assertEquals(19, docVals.get(4));
  }

  @Test
  public void testSortedDocValues() throws Exception {
    SortedDocValues docVals = atomicReader.getSortedDocValues(SORTED_FIELD);
    for (int i = 0; i < atomicReader.maxDoc(); ++i) {
      Assert.assertEquals(0, docVals.getOrd(i));
      BytesRef bref = new BytesRef();
      docVals.get(i, bref);
      System.out.println(bref.utf8ToString());
    }
  }

  @Test
  public void testSortedSetDocValues() throws Exception {
    SortedSetDocValues docVals = atomicReader.getSortedSetDocValues(SORTEDSET_FIELD);
    for (int i = 0; i < atomicReader.maxDoc(); ++i) {
      docVals.setDocument(i);
      long expectedOrd;
      while ((expectedOrd = docVals.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
        TestCase.assertEquals(0, expectedOrd);
        BytesRef bref = new BytesRef();
        docVals.lookupOrd(expectedOrd, bref);
        System.out.println(bref.utf8ToString());
      }
    }
  }
}
