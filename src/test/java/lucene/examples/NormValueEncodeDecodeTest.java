package lucene.examples;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.junit.Test;

/**
 * Created by yozhao on 15/10/4.
 */
public class NormValueEncodeDecodeTest {
  DefaultSimilarity defaultSimilarity = new DefaultSimilarity();

  @Test
  public void testEncodeAndDecode() {
    float norm = (float)(1/Math.sqrt(3.0));
    System.out.println("origin norm value is " + norm);
    System.out.println("norm value after encode and decode is " +
        defaultSimilarity.decodeNormValue(defaultSimilarity.encodeNormValue(norm)));
    norm = (float)(1/Math.sqrt(2));
    System.out.println("origin norm value is " + norm);
    System.out.println("norm value after encode and decode is " +
        defaultSimilarity.decodeNormValue(defaultSimilarity.encodeNormValue(norm)));
  }
}
