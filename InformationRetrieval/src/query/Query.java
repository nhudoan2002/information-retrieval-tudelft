package query;

import indexing.DocumentIndex;
import indexing.PermutermFacilities;
import indexing.TokenAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ranking.CosineRanker;
import soundex.Soundex;

public class Query {
   public static int K_TOP = 10;
   public static int MODE_BOOLEAN  = 0;
   public static int MODE_BAG_OF_WORDS = 1;


   private CommonTree tree;
   public static final boolean DEBUG = true;

   // public DocumentIndex index;
   private List<Integer> results;

   public List<Integer> rankResult() {
       try {
           if (results == null)
               getResult();
       } catch (RecognitionException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }

       // for (Integer i: )
       return null;

   }

   public Query(String query) throws RecognitionException {
       // this.index = index;
       query = query.trim().toLowerCase();
       query = query.replaceAll("[^a-zA-Z0-9() .\t" + PermutermFacilities.PERMUTERM_SYMBOL + PermutermFacilities.WILDCARD_SYMBOL + "]", "");

       QueryGrammarLexer lexer = new QueryGrammarLexer(new ANTLRStringStream(query));
       CommonTokenStream tokens = new CommonTokenStream(lexer);
       QueryGrammarParser parser = new QueryGrammarParser(tokens);
       QueryGrammarParser.query_return r = parser.query();
       tree = (CommonTree) r.getTree();
   }

   // public Query(InputStream in) throws IOException, RecognitionException {
   // ANTLRInputStream input = new ANTLRInputStream(System.in);
   // QueryGrammarLexer lexer = new QueryGrammarLexer(new
   // ANTLRInputStream(in));
   // CommonTokenStream tokens = new CommonTokenStream(lexer);
   // QueryGrammarParser parser = new QueryGrammarParser(tokens);
   // QueryGrammarParser.query_return r = parser.query();
   // tree = (CommonTree)r.getTree();
   // }


   public static List<String> takeOutQueryTerms(String inputQuery) {

       ArrayList<String> out = new ArrayList<String>();

       TokenAnalyzer ta = new TokenAnalyzer(inputQuery);
       String term = null;

       while ((term = ta.getNextToken()) != null) {
           if (!out.contains(term))
               out.add(term);
       }
       return out;
   }

   // returns document ids
   public List<Integer> getResult() throws RecognitionException {
       CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
//        tree.getText();

       QueryExecuter walker = new QueryExecuter(nodes);
       TokenStream ts = nodes.getTokenStream();

       results = walker.query();
       return results;

   }

   public void printAST() {
       System.out.println(tree.toStringTree());
   }

   public static void main(String[] args) throws Exception {
       // trim the input, insert and instead of spaces, lowercase

       File db = new File("reuters.db");
       // DocumentIndex index = null;

       if (!db.exists()) {
           System.out.print("Creating index...");
           DocumentIndex.instance().createIndex("reutersTXT/");
           DocumentIndex.instance().save("reuters.db");
           System.out.println("Done.");
       } else {
           System.out.print("Loading index...");
           DocumentIndex.instance().load("reuters.db");
           System.out.println("Done.");
       }

       int mode = MODE_BAG_OF_WORDS;

       while (true) {
           System.out.printf("Mode: %s\n", mode == MODE_BOOLEAN ? "Boolean querying" : "Bag of words");
           if (mode == MODE_BAG_OF_WORDS) {
               System.out.printf("K: %d\n",K_TOP);
           }
           System.out.printf("Enter the query (mode): ");
           Scanner in = new Scanner(System.in);

              // Reads a single line from the console
              // and stores into name variable
           String input = in.nextLine();
           input = input.toLowerCase().trim();
           if (input.equals("mode")) {
               mode = 1 - mode;
               continue;
           }
           String soundex = Soundex.guessSoundex(input);
           if (soundex != null)
               System.out.println("Did you mean: " + soundex);
           if (mode == MODE_BOOLEAN) {
               try {
                   Query q = new Query(input);
                   List<Integer> r = q.getResult();
                   System.out.println(r);
               } catch (Exception ex) {
                   System.out.println("Error while processing the boolean query.");
               }
           } else {
               System.out.println("High-idf turned off:");
               System.out.println(CosineRanker.rankingResults(input, K_TOP, false));
               System.out.println("High-idf turned on:");
               System.out.println(CosineRanker.rankingResults(input, K_TOP, true));

           }
       }


       // System.out.println(q.rankResult());
       // ---------------------------------------------------------------------------------
   }
}