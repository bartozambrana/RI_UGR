package p3;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
    
    public void indexarDocumentos(ArrayList<Json> documentos) throws IOException{
        for(Json json : documentos){    
            Document doc = new Document();
            
            //Nombre documento
            doc.add(new StringField("namefile", json.getNameFile(), Field.Store.YES));
            //Autor e institución
            for(int j = 0 ; j <  json.getAuthors().size(); j++){
                doc.add(new TextField("auhtor",json.getAuthors().get(j).getKey(), Field.Store.YES));
                doc.add(new StringField("institution",json.getAuthors().get(j).getValue(),Field.Store.YES));
            }
            //Titulo
            doc.add(new TextField("title",json.getTitle(),Field.Store.YES));
            //Tamaño del fichero
            doc.add(new IntPoint("size",json.getSizeFile()));
            doc.add(new StoredField("size",json.getSizeFile()));
            //Descripción
            doc.add(new TextField("brief", json.getBrief(),Field.Store.YES));
            //Contenido
            doc.add(new TextField("text", json.getText(), Field.Store.YES));
            
            writer.addDocument(doc);
            
        }
        
        closeIndex();
    }
    
    
    
}
