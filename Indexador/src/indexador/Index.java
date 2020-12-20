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
    private final String indexPath = "./../P3/indices/"; //Escribimos en la carpeta de índices de la aplicación buscador.
    private final String facetPath = "./../P3/facetas/"; //Escribimos en la carpeta de facetas de la aplicación buscador.
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
        analyzerPerField.put("author", new LowerCaseAnalyzer());                            //Convierte a minúscula
        analyzerPerField.put("institution", new LowerCaseAnalyzer());//Convierte a minúscula
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
        else if(tipo.equals("CREATE_OR_APPEND")){
            gestorIndices = new File(indexPath);
            String listadoFicheros[] = gestorIndices.list();
            this.ficherosEliminar = new ArrayList<>();
            
            for(String fichero : listadoFicheros){
                int pos = fichero.indexOf(".si");
                int pos2 = fichero.indexOf(".cfe");
                int pos3 = fichero.indexOf(".cfs");
                if(pos != -1 || pos2 != -1 || pos3 != -1)
                    ficherosEliminar.add(fichero);
            }
            
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }
            
        else
            System.exit(-1);
        writer = new IndexWriter(dir,config);
    }
    
    private void configurarFacetas() throws IOException{
        FSDirectory taxoDir = FSDirectory.open(Paths.get(facetPath));
        fconfig = new FacetsConfig();
        taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
        
        fconfig.setMultiValued("institution", true);
        
    }
    
    private void closeIndex() throws IOException{
        try{
            
            writer.commit();   
            writer.close();
            taxoWriter.close();
            /*if(gestorIndices != null)
                for(String fichero : ficherosEliminar){
                    gestorIndices  = new File(indexPath + fichero);
                    gestorIndices.delete();
                }*/
        }catch(IOException e){
            System.out.println("ERROR IN CLOSE INDEX: " + e);
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
    
    public void indexarDocumentos(ArrayList<Json> documentos, String tipo) throws IOException{
        for(Json json : documentos){    
            Document doc = new Document();
            

            //INCLUIMOS LOS ELEMENTOS AL ÍNDICE
            
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
            //Lo instroducimos también como string para poder ser utilizado en las facetas
            
            //Faceta tamaño por rango:
            
            long size = json.getSizeFile();
            doc.add(new NumericDocValuesField("size",size));
            FacetField faceta = new FacetField("size",json.getSizeFile().toString());
            
            doc.add(new FacetField("size",json.getSizeFile().toString()));
            //Descripción
            doc.add(new TextField("brief", json.getBrief(),Field.Store.YES));
            //Contenido
            doc.add(new TextField("text", json.getText(), Field.Store.NO));

            
            //writer.addDocument(doc);
            
            writer.addDocument(fconfig.build(taxoWriter,doc));
            if(tipo.equals("CREATE_OR_APPEND")){
                writer.updateDocument(new Term(json.getBrief()),doc);
                writer.updateDocument(new Term(json.getText()), doc);
                writer.updateDocument(new Term(json.getSizeFile().toString()), doc);
                writer.updateDocument(new Term(json.getTitle()), doc);
                writer.updateDocument(new Term(json.getNameFile()), doc);
                for(int j = 0 ; j <  json.getAuthors().size(); j++){
                    writer.updateDocument(new Term(json.getAuthors().get(j).getKey()), doc);
                    writer.updateDocument(new Term(json.getAuthors().get(j).getValue()), doc);
                }
                for(int i = 0; i < paises.size(); i++){
                    writer.updateDocument(new Term(paises.get(i)), doc);
                }
            }
            
        }
        
        closeIndex();
    }
    
    
    
}
