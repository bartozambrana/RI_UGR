package p3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;
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
        
        
        Busqueda busqueda = new Busqueda();
        ArrayList<Document> documento = busqueda.search("country","China"); 
        //documento = busqueda.search("country","España"); 
        
        //ArrayList<Document> documentos = busqueda.booleanSearch("text","virus","title","coronavirus");
        //System.out.println(documentos.size());
        for(int i = 0; i < documento.size(); i++){
            System.out.println(documento.get(i).get("namefile") + " - Tamaño : " + documento.get(i).get("size") + "-  Institución: " + documento.get(i).get("institution"));
            
        }
        
        
        /*List<FacetResult> resultado = busqueda.obtenerFacetaSizeRango();
        System.out.println("Categorías totales " + resultado.size());
        for(FacetResult fr: resultado){
                System.out.println("Categoría " + fr.dim);
                for(LabelAndValue lv: fr.labelValues){
                    System.out.println("\t Etiq: " + lv.label + ", valor(#n)-> "+ lv.value);
                }
        }
        
        System.out.println("INSTITUCIONES");
        FacetResult resultadoInsti = busqueda.obtenerFacetaInstitucion();
        
        for(LabelAndValue lv: resultadoInsti.labelValues){
            System.out.println("\t Etiq: " + lv.label + ", valor(#n)-> "+ lv.value);
        }*/
        System.out.println("_______________________________________________________________________________________--");
        ArrayList<Document> documentos = busqueda.buscarPorFaceta("institution", "desconocida");
        for(int i = 0; i < documentos.size(); i++){
            System.out.println(documentos.get(i).get("namefile") + " - Tamaño : " + documentos.get(i).get("size") + "-  Institución: " + documentos.get(i).get("institution"));
            
        }
        busqueda.cerrarIndex();
        
    }
    
}
