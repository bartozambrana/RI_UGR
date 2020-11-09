package p3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
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
        
        
        //COMPROBAR ÍNDICES
        File dir = new File("./documentos/document_parses/documents_json/");
        ArrayList<Json> documentosJson = new ArrayList<>();
        for(String fichero: dir.list()){
            documentosJson.add(new Json(dir.getPath() + "/" + fichero));
            
        }
        Index indice = new Index();
        indice.indexarDocumentos(documentosJson);
        
        
        Busqueda busqueda = new Busqueda();
        //ArrayList<Document> documentos = busqueda.search("size","66");
        ArrayList<Document> documentos = busqueda.booleanSearch("text","virus","title","coronavirus");
        System.out.println(documentos.size());
        for(int i = 0; i < documentos.size(); i++){
            System.out.println(documentos.get(i).get("namefile") + " - Tamaño : " + documentos.get(i).get("size"));
        }
        
        busqueda.cerrarIndex();
        
    }
    
}
