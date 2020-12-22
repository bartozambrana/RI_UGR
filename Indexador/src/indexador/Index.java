package indexador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;


/**
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero.
 */
public class Index {
    // Variables
    private final String indexPath = "./../Buscador/indices/"; //Escribimos en la carpeta de índices de la aplicación buscador.
    private final String facetPath = "./../Buscador/facetas/"; //Escribimos en la carpeta de facetas de la aplicación buscador.
    private HashMap<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
    private PerFieldAnalyzerWrapper analyzer = null;
    private IndexWriter writer;
    private FacetsConfig fconfig;
    private DirectoryTaxonomyWriter taxoWriter;
    private File gestorIndices;
    private ArrayList<String> ficherosEliminar;
    
    // Constructor del índice
    public Index(String tipo) throws IOException{
        //Analizadores para cada campo
        analyzerPerField.put("author", new LowerCaseAnalyzer());                            
        analyzerPerField.put("institution", new LowerCaseAnalyzer());                       //Convierte a minúscula
        analyzerPerField.put("country", new LowerCaseAnalyzer());
        analyzerPerField.put("title", new EnglishAnalyzer());                             
        analyzerPerField.put("brief", new EnglishAnalyzer()); 
        analyzerPerField.put("text", new EnglishAnalyzer());
        
        analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);
        
        configurarIndice(tipo);
        configurarFacetas();
    }
    
    private void configurarIndice(String tipo) throws IOException{
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        
        if(tipo.equals("CREATE"))
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        else if(tipo.equals("APPEND")){
            config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        }else{
            System.err.println("Como segundo argumento no se ha establecido ni CREATE ni APPEND");
            System.exit(-1);
        }
            
        
        writer = new IndexWriter(dir,config);
    }
    
    private void configurarFacetas() throws IOException{
        FSDirectory taxoDir = FSDirectory.open(Paths.get(facetPath));
        fconfig = new FacetsConfig();
        taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
        
        fconfig.setMultiValued("institution", true);
        
    }
    
    public void closeIndex() throws IOException{
        try{
            
            writer.commit();   
            writer.close();
            taxoWriter.close();
        }catch(IOException e){
            System.err.println("ERROR IN CLOSE INDEX: " + e);
        }
    }
    
    private ArrayList<String> aplicarAnalizadorStringField(String contenido, Analyzer analizador) throws IOException{
        ArrayList<String> paises = new ArrayList<>();
        
            
        TokenStream stream = analizador.tokenStream(null, contenido);
        stream.reset();
        while(stream.incrementToken()){
            String palabra = "";
            palabra += stream.getAttribute(CharTermAttribute.class);
            paises.add(palabra);
        }
        stream.end();
        stream.close();

        return paises;
            
        
    }
    
    public void indexarDocumentos(Json json) throws IOException{
           
        Document doc = new Document();

        //Nombre documento
        doc.add(new StringField("namefile", json.getNameFile(), Field.Store.YES));
        //Autor e institución
        for(int j = 0 ; j <  json.getAuthors().size(); j++){
           doc.add(new TextField("author",json.getAuthors().get(j).getKey(), Field.Store.YES));               
           doc.add(new TextField("institution",json.getAuthors().get(j).getValue(),Field.Store.YES));
           //Faceta institución
           doc.add(new FacetField("institution",json.getAuthors().get(j).getValue()));

        }

        ArrayList<String> paises = this.aplicarAnalizadorStringField(json.getCountries(), new LowerCaseAnalyzer());
        //Paises
        for(int i = 0; i < paises.size(); i++){
            doc.add(new StringField("country",paises.get(i), Field.Store.YES));

        }

        //Titulo
        doc.add(new TextField("title",json.getTitle(),Field.Store.YES));
        //Tamaño del fichero
        doc.add(new IntPoint("size",json.getSizeFile()));
        doc.add(new StoredField("size",json.getSizeFile()));
        

        //Faceta tamaño por rango:
        long size = json.getSizeFile();
        doc.add(new NumericDocValuesField("size",size));
        FacetField faceta = new FacetField("size",json.getSizeFile().toString());

        doc.add(new FacetField("size",json.getSizeFile().toString()));
        //Descripción
        doc.add(new TextField("brief", json.getBrief(),Field.Store.YES));
        //Contenido
        doc.add(new TextField("text", json.getText(), Field.Store.NO));

        writer.addDocument(fconfig.build(taxoWriter,doc));    
    }
}
