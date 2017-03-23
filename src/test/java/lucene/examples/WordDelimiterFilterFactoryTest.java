package lucene.examples;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.junit.Test;

/**
 * Created by yozhao on 16/03/2017.
 */
public class WordDelimiterFilterFactoryTest {
  @Test
  public void testWordDelimiterFilter() throws Exception {
    String testText = "I borrowed $5,400.00 at 25% interest-rate";
    Map<String,String> args = new HashMap<>();
    args.put("luceneMatchVersion", "LUCENE_4_10_0");

    WordDelimiterFilterFactory factoryDefault = new WordDelimiterFilterFactory(args);

    TokenStream ts = factoryDefault.create(new WhitespaceTokenizer(new StringReader(testText)));
    ts.reset();
    String out = "";
    while (ts.incrementToken()) {
      out += "[" + ts.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    System.out.println(out);

    args = new HashMap<>();
    args.put("types", "src/test/resources/wdftypes.txt");
    args.put("luceneMatchVersion", "LUCENE_4_10_0");
    WordDelimiterFilterFactory factoryCustom = new WordDelimiterFilterFactory(args);

    ResourceLoader resourceLoader = new FilesystemResourceLoader();
    factoryCustom.inform(resourceLoader);
    ts = factoryCustom.create(new WhitespaceTokenizer(new StringReader(testText)));
    ts.reset();
    out = "";
    while (ts.incrementToken()) {
      out += "[" + ts.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    System.out.println(out);
  }
}
