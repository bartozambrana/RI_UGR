/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.parser.ParseException;

/**
 *
 * @author mipc
 */
public class Indexador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        File dir = new File("./documentos/document_parses/documents_json/");
        ArrayList<Json> documentosJson = new ArrayList<>();
        for(String fichero: dir.list()){
            documentosJson.add(new Json(dir.getPath() + "/" + fichero));
            
        }
        Index indice = new Index();
        indice.indexarDocumentos(documentosJson);
        
    }
    
}
