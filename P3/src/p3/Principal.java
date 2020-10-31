package p3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javafx.util.Pair;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero
 */
public class Principal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        
        Json json  = new Json("./documentos/document_parses/pdf_json/ffff79e335491d459180a337e572a19ece47a885.json");
        
        System.out.println("Titulo : " +json.getTitle());
        ArrayList<Pair<String, String>> autores = json.getAuthors();
        for (int i = 0; i < autores.size(); i++){
            System.out.println("Autor: " + autores.get(i).getKey() + ", Institución: " + autores.get(i).getValue());
        }
        System.out.println("Instituciones: " + json.getInstitution());
        System.out.println("Resumen: " + json.getBrief());
        System.out.println("Tamaño Fichero KB: " + json.getSizeFile());
        
        
    }
    
}
