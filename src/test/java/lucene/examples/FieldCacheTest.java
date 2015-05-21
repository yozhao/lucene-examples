package lucene.examples;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

/**
 * Created by yozhao on 2/12/15.
 */
public class FieldCacheTest extends TestCase {
  private IndexReader reader;

  @Override
  public void setUp() throws Exception {
    reader = ExamplesUtil.getIndexReader();
  }

  @Test
  public void testFieldCache() throws Exception {
    DirectoryReader dirReader = (DirectoryReader) reader;
    FieldCache.Ints ints = FieldCache.DEFAULT.getInts(dirReader.leaves().get(0).reader(), "id", false);
    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals(i, ints.get(i));
    }

    BinaryDocValues terms = FieldCache.DEFAULT.getTerms(dirReader.leaves().get(0).reader(), "string", false);
    BytesRef bytesRef = terms.get(0);
    Assert.assertEquals("abc", bytesRef.utf8ToString());
    bytesRef = terms.get(2);
    Assert.assertEquals("abcd", bytesRef.utf8ToString());

    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals(i, ints.get(i));
    }
    FieldCache.Doubles doubles = FieldCache.DEFAULT.getDoubles(dirReader.leaves().get(0).reader(), "docValue", false);
    Assert.assertEquals(1.23456, doubles.get(0));
    // docValue of doc 4 is omitted by NumericDocValuesField
    Assert.assertEquals(0.0, doubles.get(4));

    // tokenized and stored field
    terms = FieldCache.DEFAULT.getTerms(dirReader.leaves().get(0).reader(), "song", false);
    for (int i = 0; i < 5; ++i) {
      String song = dirReader.leaves().get(0).reader().document(i).get("song");
      bytesRef = terms.get(i);
      System.out.println("The filed is: \"" + song + "\", while filed cache gets \"" + bytesRef.utf8ToString() + "\"");
    }
  }
}