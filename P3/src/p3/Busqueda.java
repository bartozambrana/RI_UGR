
package p3;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import static org.apache.lucene.document.IntPoint.newExactQuery;
import org.apache.lucene.document.LongRange;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Bartolomé Zambrana Pérez y Alonso Bueno Herrero.
 */
public class Busqueda {
    
    //Variables
    private final String indexPath;
    private final String facetPath;
    private IndexSearcher searcher;
    private QueryParser parser;
    private Query query;
    //private ArrayList<Analyzer> analyzers;
    private IndexReader reader;
    private TaxonomyReader taxoReader;
    private FacetsCollector  facets;
    private FacetsConfig fconfig;
    private final Integer NDOCS = 20;
    
    
    
    public Busqueda() throws IOException {
        this.indexPath = "./indices/";
        this.facetPath = "./facetas/";
        
        
        try {
            //Obtenemos directorios
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Directory taxoDir = FSDirectory.open(Paths.get(facetPath));
            //Abrimos directorios
            reader = DirectoryReader.open(dir);
            taxoReader = new DirectoryTaxonomyReader(taxoDir);
            //Establecemos los buscadores
            searcher = new IndexSearcher(reader);
            facets = new FacetsCollector();
            //Configuramos el índice
            fconfig = new FacetsConfig();
            fconfig.setMultiValued("institution", true);
        } catch (IOException e) {
            System.err.println("Error Al obtener el Documento Asociado a Índice: " + e);
            System.exit(-1);
        }
        
    }
    
    public ArrayList<Document> search(String campo, String consulta) throws ParseException, IOException{
        ArrayList<Document> documentos = new ArrayList<>();
        
        if(campo.equals("author") || campo.equals("institution") || campo.equals("country")){
            
            if(campo.equals("country"))
                documentos = consultar(consulta,campo,3,Analizadores.DEFAULT);
            else{
                documentos = consultar(consulta, campo, 2, Analizadores.LOWERCASE);
            }
                    
        }else if(campo.equals("title") || campo.equals("brief") || campo.equals("text")){
            documentos = consultar(consulta, campo, 2, Analizadores.ENGLISH);
        }else if(campo.equals("size")){
            documentos = consultar(consulta, campo,1,Analizadores.WHITESPACE);
        }
        
        
        return documentos;
    }
    
    public List<FacetResult> obtenerFacetas() throws IOException{
        List<FacetResult> resultado;
              
        Facets facetas = new FastTaxonomyFacetCounts(taxoReader,fconfig,facets);
        resultado = facetas.getAllDims(20);
        
        return resultado;
    }
    
    public ArrayList<Document> booleanSearch(String campo, String consulta, String campo2, String consulta2) throws IOException{
        ArrayList<Document> documentos;
        TopDocs resultadoConsulta;
        
        Query q1 = getQuery(campo,consulta);
        Query q2 = getQuery(campo2,consulta2);
        
        BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.MUST);
        BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.MUST);
        
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
        bqbuilder.add(bc1);
        bqbuilder.add(bc2);
        
        BooleanQuery bq = bqbuilder.build();
        
        resultadoConsulta = FacetsCollector.search(searcher, bq,NDOCS, facets);
        documentos = obtenerDocumentos(resultadoConsulta);
        
        return documentos;
    }
    
    
    private Query getQuery(String campo, String consulta) throws IOException{
        Query resultado = null;
        
        if(campo.equals("author") || campo.equals("institution") || campo.equals("country")){
            
            if(campo.equals("country")){
                Term termino = new Term(campo,consulta);
                resultado = new TermQuery(termino);
            }else{
                resultado = obtenerPhraseQuery(Analizadores.LOWERCASE, consulta, campo);
            }
                    
        }else if(campo.equals("brief") || campo.equals("text") || campo.equals("title")){
            resultado = obtenerPhraseQuery(Analizadores.ENGLISH, consulta, campo);
        }else if(campo.equals("size")){
            resultado = newExactQuery(campo,Integer.parseInt(consulta));
        }
        
        return resultado;
    }
    
    
    private ArrayList<Document> consultar(String consulta, String campo, int tipoConsulta, Analizadores analizador) throws ParseException, IOException{
        TopDocs resultadoConsulta = null;
        switch(tipoConsulta){
            case 1:     
                query = newExactQuery(campo,Integer.parseInt(consulta));
                break;
                
            case 2:     //Búsqueda por texto libre Phrase Query
                query = obtenerPhraseQuery(analizador,consulta, campo);
                break;
                
            case 3:     //TermQuery
                Term termino = new Term(campo,consulta);
                query = new TermQuery(termino);
                break;
                       
        }
        
        //resultadoConsulta = searcher.search(query,20);
        resultadoConsulta = FacetsCollector.search(searcher, query, NDOCS, facets);
        ArrayList<Document> documentos = obtenerDocumentos(resultadoConsulta);
        
        return documentos;
    }
    
    
    private PhraseQuery obtenerPhraseQuery(Analizadores analizador,String consulta, String campo) throws IOException{
        Analyzer analyzer ;
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        switch(analizador){
            case LOWERCASE: 
                analyzer = new LowerCaseAnalyzer();
                //System.out.println("lowercaseanalyzer");
                break;
            case ENGLISH:
                analyzer = new EnglishAnalyzer();
                break;
            default:
                analyzer = new WhitespaceAnalyzer();
                break;
        }
        
        TokenStream stream = analyzer.tokenStream(null, consulta);

        stream.reset();
        while(stream.incrementToken()){
            String palabra = ""; 
            palabra += stream.getAttribute(CharTermAttribute.class);
            //System.out.println(palabra);
            Term aux = new Term(campo,palabra);
            builder.add(new Term(campo,palabra));
            //System.out.println(" Término añadido: " + aux.toString());
        }
        stream.end();
        stream.close();

        return builder.build();
        
    }
    
    
    private ArrayList<Document> obtenerDocumentos(TopDocs resultadoConsulta) throws IOException{
        ArrayList<Document> documentos = new ArrayList<>();
        ScoreDoc[] hits = resultadoConsulta.scoreDocs;
        
        for(int i = 0; i < hits.length; i++){
            documentos.add(searcher.doc(hits[i].doc));
        }
        
        return documentos;
    }
    
    public void cerrarIndex(){
        try{
                reader.close();
                taxoReader.close();
            }catch(IOException e2){
               System.err.println("Error Al cerrar el Documento Asociado a Índice: " + e2);
               System.exit(-2); 
            }
    }
    
    
    
}
