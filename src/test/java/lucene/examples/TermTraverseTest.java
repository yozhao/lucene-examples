package lucene.examples;

import junit.framework.Assert;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by yozhao on 4/10/15.
 */
public class TermTraverseTest {
  static RAMDirectory dir = new RAMDirectory();
  static IndexReader topReader;
  static AtomicReader atomicReader;

  @BeforeClass
  public static void setup() throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48, CharArraySet.EMPTY_SET));
    IndexWriter writer = new IndexWriter(dir, config);
    FieldType docsOnlyType = new FieldType();
    docsOnlyType.setIndexed(true);
    docsOnlyType.setTokenized(true);
    docsOnlyType.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);

    FieldType docsAndFreqsType = new FieldType();
    docsAndFreqsType.setIndexed(true);
    docsAndFreqsType.setTokenized(true);
    docsAndFreqsType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS);

    FieldType docsAndFreqsAndPositionsType = new FieldType();
    docsAndFreqsAndPositionsType.setIndexed(true);
    docsAndFreqsAndPositionsType.setTokenized(true);
    docsAndFreqsAndPositionsType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);

    FieldType docsAndFreqsAndPositionsAndOffsetsType = new FieldType();
    docsAndFreqsAndPositionsAndOffsetsType.setIndexed(true);
    docsAndFreqsAndPositionsAndOffsetsType.setTokenized(true);
    docsAndFreqsAndPositionsAndOffsetsType
        .setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

    Document doc = new Document();
    doc.add(new Field("docsOnly", "the quick brown fox jumps over the lazy dog", docsOnlyType));
    doc.add(new Field("docsAndFreqs", "the quick brown fox jumps over the lazy dog", docsAndFreqsType));
    doc.add(new Field("docsAndFreqsAndPositions", "the quick brown fox jumps over the lazy dog",
        docsAndFreqsAndPositionsType));
    doc.add(new Field("docsAndFreqsAndPositionsAndOffsets", "the quick brown fox jumps over the lazy dog",
        docsAndFreqsAndPositionsAndOffsetsType));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new Field("docsOnly", "the quick car knocked down the lazy dog", docsOnlyType));
    doc.add(new Field("docsAndFreqs", "the quick car knocked down the lazy dog", docsAndFreqsType));
    doc.add(
        new Field("docsAndFreqsAndPositions", "the quick car knocked down the lazy dog", docsAndFreqsAndPositionsType));
    doc.add(new Field("docsAndFreqsAndPositionsAndOffsets", "the quick car knocked down the lazy dog",
        docsAndFreqsAndPositionsAndOffsetsType));
    writer.addDocument(doc);

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
  public void testDocsOnlyTermTraverse() throws Exception {
    System.out.println("###############testDocsOnlyTermTraverse################");
    Terms terms = atomicReader.terms("docsOnly");
    TermsEnum termsEnum = terms.iterator(null);
    BytesRef text;
    while ((text = termsEnum.next()) != null) {
      String strText = text.utf8ToString();
      System.out.println("Traverse term(docsOnly:" + strText + ").");
      Term term = new Term("docsOnly", strText);
      DocsAndPositionsEnum docsAndPositionsEnum = atomicReader.termPositionsEnum(term);
      Assert.assertNull(docsAndPositionsEnum);
      DocsEnum docsEnum = atomicReader.termDocsEnum(term);
      int docID;
      while ((docID = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
        System.out.println("DocId:" + docID);
        int freq = docsEnum.freq();
        Assert.assertEquals(1, freq);
        System.out.println("This term occurs " + freq + " times");
      }
      System.out.println("");
    }
  }

  @Test
  public void testDocsAndFreqsTermTraverse() throws Exception {
    System.out.println("###############testDocsAndFreqsTermTraverse################");
    Terms terms = atomicReader.terms("docsAndFreqs");
    TermsEnum termsEnum = terms.iterator(null);
    BytesRef text;
    while ((text = termsEnum.next()) != null) {
      String strText = text.utf8ToString();
      System.out.println("Traverse term(docsOnly:" + strText + ").");
      Term term = new Term("docsAndFreqs", strText);
      DocsAndPositionsEnum docsAndPositionsEnum = atomicReader.termPositionsEnum(term);
      Assert.assertNull(docsAndPositionsEnum);
      DocsEnum docsEnum = atomicReader.termDocsEnum(term);
      int docID;
      while ((docID = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
        System.out.println("DocId:" + docID);
        int freq = docsEnum.freq();
        System.out.println("This term occurs " + freq + " times");
      }
    }
  }

  @Test
  public void testDocsAndFreqsAndPositions() throws Exception {
    System.out.println("###############testDocsAndFreqsAndPositions################");
    Terms terms = atomicReader.terms("docsAndFreqsAndPositions");
    TermsEnum termsEnum = terms.iterator(null);
    BytesRef text;
    while ((text = termsEnum.next()) != null) {
      String strText = text.utf8ToString();
      System.out.println("Traverse term(docsAndFreqsAndPositions:" + strText + ").");
      Term term = new Term("docsAndFreqsAndPositions", strText);
      DocsAndPositionsEnum docsAndPositionsEnum = atomicReader.termPositionsEnum(term);
      int docID;
      while ((docID = docsAndPositionsEnum.nextDoc()) != DocsAndPositionsEnum.NO_MORE_DOCS) {
        System.out.println("DocId:" + docID);
        int freq = docsAndPositionsEnum.freq();
        System.out.println("This term occurs " + freq + " times");
        for (int i = 0; i < freq; i++) {
          System.out.println(
              "Position, offset:" + docsAndPositionsEnum.nextPosition() + ", [" + docsAndPositionsEnum
                  .startOffset() + "," + docsAndPositionsEnum.endOffset() + "]");
        }
        System.out.println("");
      }
    }
  }

  @Test
  public void testDocsAndFreqsAndPositionsAndOffsets() throws Exception {
    System.out.println("###############testDocsAndFreqsAndPositionsAndOffsets################");
    Terms terms = atomicReader.terms("docsAndFreqsAndPositionsAndOffsets");
    TermsEnum termsEnum = terms.iterator(null);
    BytesRef text;
    while ((text = termsEnum.next()) != null) {
      String strText = text.utf8ToString();
      System.out.println("Traverse term(docsAndFreqsAndPositionsAndOffsets:" + strText + ").");
      Term term = new Term("docsAndFreqsAndPositionsAndOffsets", strText);
      DocsAndPositionsEnum docsAndPositionsEnum = atomicReader.termPositionsEnum(term);
      int docID;
      while ((docID = docsAndPositionsEnum.nextDoc()) != DocsAndPositionsEnum.NO_MORE_DOCS) {
        System.out.println("DocId:" + docID);
        int freq = docsAndPositionsEnum.freq();
        System.out.println("This term occurs " + freq + " times");
        for (int i = 0; i < freq; i++) {
          System.out.println(
              "Position, offset:" + docsAndPositionsEnum.nextPosition() + ", [" + docsAndPositionsEnum
                  .startOffset() + "," + docsAndPositionsEnum.endOffset() + "]");
        }
        System.out.println("");
      }
    }
  }
}