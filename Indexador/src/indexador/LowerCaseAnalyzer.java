
package indexador;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;


/**
 *
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero
 */
public class LowerCaseAnalyzer extends Analyzer{
    
    @Override
    protected TokenStreamComponents createComponents(String fieldname) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream source = new LowerCaseFilter(tokenizer);
        return new Analyzer.TokenStreamComponents(tokenizer,source);
    }
    
}
