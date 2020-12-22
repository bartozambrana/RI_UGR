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
    private static String mensaje  ="args[0] ruta al directorio que contiene los documentos.\n" +
                                    "args[1] == APPEND añade al índice los documentos del directorio args[0].\n" +
                                    "args[1] == CREATE crea un índice desde cero." ;
    /**
     * @param args the command line arguments 
     * 
     * args[0] ruta al directorio que contiene los documentos.
     * args[1] si se introduce APPEND añade al índice los documentos del directorio args[0].
     *         si se introduce CREATE crea un índice desde cero.
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        
        if(args.length != 2){
            System.err.println(mensaje);
            System.exit(-2);
        }
            
        File dir = new File(args[0]);
        ArrayList<Json> documentosJson = new ArrayList<>();
        Index indice = new Index(args[1]);
        for(String fichero: dir.list()){
            indice.indexarDocumentos(new Json(dir.getPath() + "/" + fichero));
        }
        
        indice.closeIndex();
        
        
        
        
    }
    
}
