package lucene.examples;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
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
    DirectoryReader dirReader = (DirectoryReader)reader;
    FieldCache.Ints ints = FieldCache.DEFAULT.getInts(dirReader.leaves().get(0).reader(), "id", false);
    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals(i, ints.get(i));
    }
    FieldCache.Doubles doubles = FieldCache.DEFAULT.getDoubles(dirReader.leaves().get(0).reader(), "docValue",
        false);
    Assert.assertEquals(1.23456, doubles.get(0));
    // docValue of doc 4 is omitted by NumericDocValuesField
    Assert.assertEquals(0.0, doubles.get(4));
  }
}