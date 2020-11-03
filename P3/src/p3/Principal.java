package p3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.util.Pair;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero
 */
public class Principal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        
        /*Json json  = new Json("./documentos/document_parses/pdf_json/0000028b5cc154f68b8a269f6578f21e31f62977.json");
        
        System.out.println("Titulo : " +json.getTitle());
        
        ArrayList<Pair<String, String>> autores = json.getAuthors();
        for (int i = 0; i < autores.size(); i++){
            System.out.println("Autor: " + autores.get(i).getKey() + ", Institución: " + autores.get(i).getValue());
        }
        
        
        System.out.println("Instituciones: " + json.getInstitution());
        System.out.println("Resumen: " + json.getBrief());
        System.out.println("Tamaño Fichero KB: " + json.getSizeFile());
        System.out.println("Texto Fichero: " + json.getText()); 
        System.out.println("Nombre Fichero: " + json.getNameFile());*/
        
        File dir = new File("./documentos/document_parses/documents_json/");
        ArrayList<Json> documentosJson = new ArrayList<>();
        for(String fichero: dir.list()){
            
            documentosJson.add(new Json(dir.getPath() + "/" + fichero));
        }
            
        
        Index indice = new Index();
        indice.indexarDocumentos(documentosJson);
        
        
        Directory direc = FSDirectory.open(Paths.get("./indices/"));
        IndexReader reader = DirectoryReader.open(direc);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        QueryParser parser = new QueryParser("title", new LowerCaseAnalyzer());
        Query q = parser.parse("title:the");
        
        System.out.println("Documentos encontrados: "+ searcher.search(q,12).totalHits);
    }
    
}
