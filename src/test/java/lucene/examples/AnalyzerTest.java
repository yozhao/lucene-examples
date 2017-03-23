package lucene.examples;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.junit.Test;

import java.io.StringReader;

/**
 * Created by yozhao on 7/7/14.
 */
public class AnalyzerTest extends TestCase {

  @Test
  public void testStandardAnalyzer() throws Exception {
    Analyzer analyzer = new StandardAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("Hello, this is a test case. " +
        "你好，这是一个测试的实例。" + "created on 20140707"));
    stream.reset();
    String expected = "[hello][test][case][你][好][这][是][一][个][测][试][的][实][例][created][20140707]";
    String out = "";
    while (stream.incrementToken()) {
      out += "[" + stream.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    assertEquals(expected, out);
    analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
    stream = analyzer.tokenStream("", new StringReader("Hello, this is a test case. " +
        "你好，这是一个测试的实例。" + "created on 20140707"));
    stream.reset();
    expected = "[hello][this][is][a][test][case][你][好][这][是][一][个][测][试][的][实][例][created][on][20140707]";
    out = "";
    while (stream.incrementToken()) {
      out += "[" + stream.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    assertEquals(expected, out);
  }

  @Test
  public void testStopAnalyzer() throws Exception {
    Analyzer analyzer = new StopAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("Hello, this is a test case. " +
        "你好，这是一个测试的实例。" + "created on 20140707"));
    stream.reset();
    String expected = "[hello][test][case][你好][这是一个测试的实例][created]";
    String out = "";
    while (stream.incrementToken()) {
      out += "[" + stream.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    assertEquals(expected, out);
  }

  @Test
  public void testSimpleAnalyzer() throws Exception {
    Analyzer analyzer = new SimpleAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("Hello, this is a test case. " +
        "你好，这是一个测试的实例。" + "created on 20140707"));
    stream.reset();
    String expected = "[hello][this][is][a][test][case][你好][这是一个测试的实例][created][on]";
    String out = "";
    while (stream.incrementToken()) {
      out += "[" + stream.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    assertEquals(expected, out);
  }

  @Test
  public void testWhitespaceAnalyzer() throws Exception {
    Analyzer analyzer = new WhitespaceAnalyzer();
    TokenStream stream = analyzer.tokenStream("", new StringReader("Hello, this is a test case. " +
        "你好，这是一个测试的实例。" + "created on 20140707"));
    stream.reset();
    String expected = "[Hello,][this][is][a][test][case.][你好，这是一个测试的实例。created][on][20140707]";
    String out = "";
    while (stream.incrementToken()) {
      out += "[" + stream.getAttribute(CharTermAttribute.class).toString() + "]";
    }
    assertEquals(expected, out);
  }
}