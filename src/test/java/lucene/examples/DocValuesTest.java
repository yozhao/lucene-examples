package lucene.examples;

import junit.framework.Assert;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.FieldCache;
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
  static final String BINARY_FIELD = "binary";
  static final String SORTED_FIELD = "sorted";
  static final String SORTEDSET_FIELD = "sortedset";

  static long[] numericVals = new long[] {12, 13, 0, 100, 19};
  static String[] binary = new String[] {"lucene", "doc", "value", "test", "example"};
  static String[] sortedVals = new String[] {"lucene", "facet", "abacus", "search", null};
  static String[][] sortedSetVals = new String[][] {{"lucene", "search"}, {"search"}, {"facet", "abacus", "search"}, {},
      {}};

  static IndexReader topReader;
  static AtomicReader atomicReader;

  @BeforeClass
  public static void setup() throws Exception {
    RAMDirectory dir = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48));
    IndexWriter writer = new IndexWriter(dir, config);
    for (int i = 0; i < numericVals.length; ++i) {
      Document doc = new Document();
      doc.add(new NumericDocValuesField(NUMERIC_FIELD, numericVals[i]));
      doc.add(new BinaryDocValuesField(BINARY_FIELD, new BytesRef(binary[i])));
      if (sortedVals[i] != null) {
        doc.add(new SortedDocValuesField(SORTED_FIELD, new BytesRef(sortedVals[i])));
      }
      for (String value : sortedSetVals[i]) {
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
  public void testBinaryDocValues() throws Exception {
    BinaryDocValues docVals = atomicReader.getBinaryDocValues(BINARY_FIELD);
    BytesRef bytesRef = new BytesRef();
    docVals.get(0, bytesRef);
    Assert.assertEquals("lucene", bytesRef.utf8ToString());
    docVals.get(1, bytesRef);
    Assert.assertEquals("doc", bytesRef.utf8ToString());
    docVals.get(2, bytesRef);
    Assert.assertEquals("value", bytesRef.utf8ToString());
    docVals.get(3, bytesRef);
    Assert.assertEquals("test", bytesRef.utf8ToString());
    docVals.get(4, bytesRef);
    Assert.assertEquals("example", bytesRef.utf8ToString());
  }

  @Test
  public void testSortedDocValues() throws Exception {
    SortedDocValues docVals = atomicReader.getSortedDocValues(SORTED_FIELD);
    String ordInfo = "", values = "";
    for (int i = 0; i < atomicReader.maxDoc(); ++i) {
      ordInfo += docVals.getOrd(i) + ":";
      BytesRef bytesRef = new BytesRef();
      docVals.get(i, bytesRef);
      values += bytesRef.utf8ToString() + ":";
    }
    Assert.assertEquals("2:1:0:3:-1:", ordInfo);
    Assert.assertEquals("lucene:facet:abacus:search::", values);
  }

  @Test
  public void testSortedSetDocValues() throws Exception {
    SortedSetDocValues docVals = atomicReader.getSortedSetDocValues(SORTEDSET_FIELD);
    String info = "";
    for (int i = 0; i < atomicReader.maxDoc(); ++i) {
      docVals.setDocument(i);
      long ord;
      info += "Doc " + i;
      while ((ord = docVals.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
        info += ", " + ord + "/";
        BytesRef bytesRef = new BytesRef();
        docVals.lookupOrd(ord, bytesRef);
        info += bytesRef.utf8ToString();
      }
      info += ";";
    }
    Assert.assertEquals("Doc 0, 2/lucene, 3/search;Doc 1, 3/search;Doc 2, 0/abacus, 1/facet, 3/search;Doc 3;Doc 4;",
        info);
  }
}
