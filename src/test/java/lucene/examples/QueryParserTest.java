package lucene.examples;

import junit.framework.Assert;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.junit.Test;

/**
 * Created by yozhao on 11/25/14.
 */
public class QueryParserTest {

  @Test
  public void testQueryParser() throws ParseException {
    QueryParser parser = new QueryParser("",  new StandardAnalyzer());
    Query query = parser.parse("小米^10 科技^2");
    Assert.assertTrue(query instanceof BooleanQuery);
    BooleanClause[] clauses = ((BooleanQuery) query).getClauses();
    Assert.assertEquals(2, clauses.length);
    Assert.assertTrue(clauses[0].getQuery() instanceof BooleanQuery);
    Assert.assertEquals("SHOULD", clauses[0].getOccur().name());
    Assert.assertEquals((float) 10.0, clauses[0].getQuery().getBoost());

    BooleanClause[] subClauses = ((BooleanQuery) clauses[0].getQuery()).getClauses();
    Assert.assertEquals(2, subClauses.length);

    Assert.assertTrue(subClauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[0].getQuery().getBoost());
    Assert.assertEquals("SHOULD", subClauses[0].getOccur().name());
    Assert.assertEquals("小", ((TermQuery) subClauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(subClauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[1].getQuery().getBoost());
    Assert.assertEquals("SHOULD", subClauses[1].getOccur().name());
    Assert.assertEquals("米", ((TermQuery) subClauses[1].getQuery()).getTerm().text());

    Assert.assertTrue(clauses[1].getQuery() instanceof BooleanQuery);
    Assert.assertEquals("SHOULD", clauses[1].getOccur().name());
    Assert.assertEquals((float) 2.0, clauses[1].getQuery().getBoost());

    subClauses = ((BooleanQuery) clauses[1].getQuery()).getClauses();
    Assert.assertEquals(2, subClauses.length);

    Assert.assertTrue(subClauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[0].getQuery().getBoost());
    Assert.assertEquals("SHOULD", subClauses[0].getOccur().name());
    Assert.assertEquals("科", ((TermQuery) subClauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(subClauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[1].getQuery().getBoost());
    Assert.assertEquals("SHOULD", subClauses[1].getOccur().name());
    Assert.assertEquals("技", ((TermQuery) subClauses[1].getQuery()).getTerm().text());

    parser = new QueryParser("", new StandardAnalyzer());
    parser.setDefaultOperator(QueryParser.Operator.AND);
    query = parser.parse("小米^10 科技^2");
    Assert.assertTrue(query instanceof BooleanQuery);
    clauses = ((BooleanQuery) query).getClauses();
    Assert.assertEquals(2, clauses.length);
    Assert.assertTrue(clauses[0].getQuery() instanceof BooleanQuery);
    Assert.assertEquals("MUST", clauses[0].getOccur().name());
    Assert.assertEquals((float) 10.0, clauses[0].getQuery().getBoost());

    subClauses = ((BooleanQuery) clauses[0].getQuery()).getClauses();
    Assert.assertEquals(2, subClauses.length);

    Assert.assertTrue(subClauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[0].getQuery().getBoost());
    Assert.assertEquals("MUST", subClauses[0].getOccur().name());
    Assert.assertEquals("小", ((TermQuery) subClauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(subClauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[1].getQuery().getBoost());
    Assert.assertEquals("MUST", subClauses[1].getOccur().name());
    Assert.assertEquals("米", ((TermQuery) subClauses[1].getQuery()).getTerm().text());

    Assert.assertTrue(clauses[1].getQuery() instanceof BooleanQuery);
    Assert.assertEquals("MUST", clauses[1].getOccur().name());
    Assert.assertEquals((float) 2.0, clauses[1].getQuery().getBoost());

    subClauses = ((BooleanQuery) clauses[1].getQuery()).getClauses();
    Assert.assertEquals(2, subClauses.length);

    Assert.assertTrue(subClauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[0].getQuery().getBoost());
    Assert.assertEquals("MUST", subClauses[0].getOccur().name());
    Assert.assertEquals("科", ((TermQuery) subClauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(subClauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals((float) 1.0, subClauses[1].getQuery().getBoost());
    Assert.assertEquals("MUST", subClauses[1].getOccur().name());
    Assert.assertEquals("技", ((TermQuery) subClauses[1].getQuery()).getTerm().text());

    query = parser.parse("\"小米 科技\"");
    Assert.assertTrue(query instanceof PhraseQuery);
    Term[] terms = ((PhraseQuery) query).getTerms();
    Assert.assertEquals(0, ((PhraseQuery) query).getSlop());
    Assert.assertEquals(4, terms.length);
    Assert.assertEquals("小", terms[0].text());
    Assert.assertEquals("米", terms[1].text());
    Assert.assertEquals("科", terms[2].text());
    Assert.assertEquals("技", terms[3].text());
    int[] positions = ((PhraseQuery) query).getPositions();
    Assert.assertEquals(0, positions[0]);
    Assert.assertEquals(1, positions[1]);
    Assert.assertEquals(2, positions[2]);
    Assert.assertEquals(3, positions[3]);

    parser = new QueryParser("", new WhitespaceAnalyzer());
    query = parser.parse("小米^10 科技^2");
    Assert.assertTrue(query instanceof BooleanQuery);
    clauses = ((BooleanQuery) query).getClauses();
    Assert.assertEquals(2, clauses.length);
    Assert.assertTrue(clauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals("SHOULD", clauses[0].getOccur().name());
    Assert.assertEquals((float) 10.0, clauses[0].getQuery().getBoost());
    Assert.assertEquals("小米", ((TermQuery) clauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(clauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals("SHOULD", clauses[1].getOccur().name());
    Assert.assertEquals((float) 2.0, clauses[1].getQuery().getBoost());
    Assert.assertEquals("科技", ((TermQuery) clauses[1].getQuery()).getTerm().text());

    query = parser.parse("\"小米 科技\"");
    Assert.assertTrue(query instanceof PhraseQuery);
    Assert.assertEquals(0, ((PhraseQuery) query).getSlop());
    terms = ((PhraseQuery) query).getTerms();
    Assert.assertEquals(2, terms.length);
    Assert.assertEquals("小米", terms[0].text());
    Assert.assertEquals("科技", terms[1].text());
    positions = ((PhraseQuery) query).getPositions();
    Assert.assertEquals(0, positions[0]);
    Assert.assertEquals(1, positions[1]);

    query = parser.parse("+小米^10 -科技^2");
    Assert.assertTrue(query instanceof BooleanQuery);
    clauses = ((BooleanQuery) query).getClauses();
    Assert.assertEquals(2, clauses.length);
    Assert.assertTrue(clauses[0].getQuery() instanceof TermQuery);
    Assert.assertEquals("MUST", clauses[0].getOccur().name());
    Assert.assertEquals((float) 10.0, clauses[0].getQuery().getBoost());
    Assert.assertEquals("小米", ((TermQuery) clauses[0].getQuery()).getTerm().text());

    Assert.assertTrue(clauses[1].getQuery() instanceof TermQuery);
    Assert.assertEquals("MUST_NOT", clauses[1].getOccur().name());
    Assert.assertEquals((float) 2.0, clauses[1].getQuery().getBoost());
    Assert.assertEquals("科技", ((TermQuery) clauses[1].getQuery()).getTerm().text());

    parser = new QueryParser("",  new StandardAnalyzer());
    query = parser.parse("title:\"小米科技\"~12");
    Assert.assertTrue(query instanceof PhraseQuery);
    Assert.assertEquals(12, ((PhraseQuery)query).getSlop());

    query = parser.parse("title:小米科技");
    Assert.assertTrue(query instanceof BooleanQuery);
    Assert.assertEquals(4, ((BooleanQuery)query).getClauses().length);
    query = parser.parse("title:小米科技~2");
    Assert.assertTrue(query instanceof FuzzyQuery);
    Assert.assertEquals(2, ((FuzzyQuery)query).getMaxEdits());
    query = parser.parse("title:(小米科技~3)");
    Assert.assertTrue(query instanceof FuzzyQuery);
    Assert.assertEquals(2, ((FuzzyQuery)query).getMaxEdits());

    query = parser.parse("title:(\"小米科技\"~3)");
    Assert.assertTrue(query instanceof PhraseQuery);
    Assert.assertEquals(3, ((PhraseQuery)query).getSlop());

    query = parser.parse("num:[1 TO 2]");
    Assert.assertTrue(query instanceof TermRangeQuery);
    Assert.assertEquals("1", ((TermRangeQuery)query).getLowerTerm().utf8ToString());
  }
}
