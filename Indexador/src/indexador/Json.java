package indexador;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javafx.util.Pair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herero
 */
public class Json {
    
    // ***** Elementos para Extraer ******
    private JSONParser parser = null;
    private JSONObject jsonObject = null;
    private JSONArray array = null;
    FileReader file = null;
    
    // ***** Campos de búsqueda *******
    private Integer sizeFile = null;
    private String title = null;
    private ArrayList<Pair<String,String>> authors = new ArrayList<>();
    private String institution = null;
    private String brief = null;
    private String text = null;
    private ArrayList<String> country = new ArrayList<>();
    private String nameFile = null;

  
    public Json(String pathDocumento) throws FileNotFoundException, IOException, ParseException{
        File aux = new File(pathDocumento);
        sizeFile = (int) aux.length()/1024; // Size in KB
        nameFile = aux.getName();
        brief = "";
        
        file = new FileReader(pathDocumento);
        parser = new JSONParser();
        
        Object ob = parser.parse(file);
        array =  new JSONArray();
        this.jsonObject = (JSONObject) ob;
        this.procesarJson();
        
    }

    
    private void procesarJson(){
        
        // Obtenemos los metadatos
        JSONObject metadatos = (JSONObject) this.jsonObject.get("metadata");
        // Obtenemos el resumen.
        JSONArray resumenes = (JSONArray) this.jsonObject.get("abstract");
        // Obtenemos los autores.
        JSONArray autores = (JSONArray) metadatos.get("authors");
        
        // Obtenemos el contenido
        JSONArray contenido = (JSONArray) this.jsonObject.get("body_text");
        
        
        
       
        //******************* Asignaciones ***********************
        //Titulo
        this.title = (String) metadatos.get("title");
        //Resumen
        if(resumenes.size() > 0){
            brief = "";
        }
        for(int i = 0; i < resumenes.size(); i++){
            JSONObject  aux = (JSONObject) resumenes.get(i);
            this.brief += aux.get("text") + "\n";
        }
        //Autores e instituciones y país
        if(autores.size() > 0){
            this.institution = "";
        }
        for(int i = 0; i < autores.size(); i++){
            JSONObject  aux = (JSONObject) autores.get(i);
            JSONObject aux2 = (JSONObject) aux.get("affiliation");
            JSONObject aux3 = (JSONObject) aux2.get("location");
            
            String autor = "";
            if(aux != null)
                autor = aux.get("first") + " " + aux.get("last");
            else
                autor = "desconocido";
            
            String institucion = (String) aux2.get("institution");
            
            if(institucion == null)
                institucion = "desconocida";
            if(institucion.equals(""))
                institucion = "desconocida";
            
            Pair <String,String> nombre_institucion = new Pair(autor,institucion);
            //No siempre contiene el campo localización en el apartado de afiliación.
            if(aux3 != null){
                String pais = (String) aux3.get("country");
                if(pais == null)
                    pais = "Desconocido";
                if(pais.equals(""))
                    pais = "Desconocido";
                //Comprobamos si el país se encuentra ya almacenado.
                int posicion = this.country.indexOf(pais);
                //En el caso de que no se encuentre lo almacenamos
                if(posicion == -1)
                    this.country.add(pais);            
            }
                        
            this.institution += institucion + " | ";
            this.authors.add(nombre_institucion);
        }  
        
        //Texto
        if(contenido.size() > 0 )
            text = "";
        for(int i = 0; i < contenido.size(); i++){
            JSONObject aux = (JSONObject) contenido.get(i);
            String texto = (String) aux.get("text") + "\n";
            if(texto != null){
                text += texto;
            }
        }
        
        
    }
    
    public Integer getSizeFile() {
        return sizeFile;
    }
    
    public String getTitle() {
        return title;
    }

    public ArrayList<Pair<String, String>> getAuthors() {
        return authors;
    }

    public String getInstitution() {
        return institution;
    }

    public String getBrief() {
        return brief;
    }
    
    public String getNameFile(){
        return nameFile;
    }
    
    
    public String getText(){
        return text;
    }
    
    public ArrayList<String> getCountry(){
        return country;
    }
    
    
    
        
}
