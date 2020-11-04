
package p3;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero.
 */
public class Busqueda {
    
    //Variables
    private final String indexPath;
    private IndexSearcher searcher;
    private QueryParser parser;
    private Query query;
    private ArrayList<Pair<String,Analyzer>> analyzers;
    
    
    public Busqueda() throws IOException {
        this.indexPath = "./indices/";
        
        try {
            
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(reader);
            try{
                reader.close();
            }catch(IOException e2){
               System.err.println("Error Al cerrar el Documento Asociado a Índice: " + e2);
               System.exit(-2); 
            }
            
        } catch (IOException e) {
            System.err.println("Error Al obtener el Documento Asociado a Índice: " + e);
            System.exit(-1);
        }
        // Analizadores utilizados para los campos
        analyzers = new ArrayList<>();
        analyzers.add(new Pair<>("LowerCaseAnalyzer", new LowerCaseAnalyzer()));
        analyzers.add(new Pair<>("EnglishAnalyzer", new EnglishAnalyzer()));
        analyzers.add(new Pair<>("WhitespaceAnalyzer", new WhitespaceAnalyzer()));
    }
    
    public ArrayList<Document> search(String consulta, String tipoConsoluta, String campo){
        ArrayList<Document> documentos = new ArrayList<>();
        
        
        
        return documentos;
    }
    
}
