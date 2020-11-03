package p3;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero.
 */
public class Index {
    // Variables
    private final String indexPath = "./indices/";
    private HashMap<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
    private PerFieldAnalyzerWrapper analyzer = null;
    private boolean create = true;
    private IndexWriter writer;
    
    // Constructor del índice
    public Index() throws IOException{
        //Analizadores para cada campo
        analyzerPerField.put("author", new LowerCaseAnalyzer()); //Convierte a minúscula
        analyzerPerField.put("institution", new LowerCaseAnalyzer()); //Convierte a minúscula
        analyzerPerField.put("title", new LowerCaseAnalyzer());
        analyzerPerField.put("brief", new EnglishAnalyzer());
        analyzerPerField.put("text", new EnglishAnalyzer());
        
        analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);
        configIndex();
    }
    
    private void configIndex() throws IOException{
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(dir,config);
    }
    
    private void closeIndex() throws IOException{
        try{
            writer.commit();
            writer.close();
        }catch(IOException e){
            System.out.println("ERROR IN CLOSE INDEX: " + e);
        }
    }
    
    
}
