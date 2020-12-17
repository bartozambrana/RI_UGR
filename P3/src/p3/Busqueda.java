
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
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.DrillSideways.DrillSidewaysResult;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
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
import org.apache.lucene.search.Sort;

import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;
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
    private TopFieldCollector collector;
    private Sort orden;
    
    
    
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
            //Configuramos el índice
            fconfig = new FacetsConfig();
            fconfig.setMultiValued("institution", true);
            
            
        } catch (IOException e) {
            System.err.println("Error Al obtener el Documento Asociado a Índice: " + e);
            System.exit(-1);
        }
        
    }
    
    private void configurarTopFieldFacetCollector(){
        //Configuración orden
        SortField sf = new SortField("size",SortField.Type.INT, true);
        sf.setMissingValue(0);
        orden = new Sort(sf);
        collector = TopFieldCollector.create(orden,20,0);
    }
    
    private void configurarFacetCollector(){
        facets = new FacetsCollector();
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
    
    
   
    public ArrayList<Document> booleanSearch(String campo, String consulta, String campo2, String consulta2) throws IOException{
        ArrayList<Document> documentos;
        
        TopDocs resultadoConsulta;
        configurarTopFieldFacetCollector();
        configurarFacetCollector();
            
            
        Query q1 = getQuery(campo,consulta);
        Query q2 = getQuery(campo2,consulta2);
        
        BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.MUST);
        BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.MUST);
        
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
        bqbuilder.add(bc1);
        bqbuilder.add(bc2);
        
        BooleanQuery bq = bqbuilder.build();
        
        //Almacenamos el valor en query para el drilldown
        query = bq;
        
        FacetsCollector.search(searcher, bq,NDOCS, facets);
        searcher.search(query, collector);
        documentos = obtenerDocumentos(collector.topDocs());
        
        return documentos;
    }
    
    
    private Query getQuery(String campo, String consulta) throws IOException{
        Query resultado = null;
        
        if(campo.equals("author") || campo.equals("institution") || campo.equals("country")){
            
            if(campo.equals("country")){
                Term termino = new Term(campo, aplicarAnalizadorStringField(consulta,new LowerCaseAnalyzer()).get(0));
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
        switch(tipoConsulta){
            case 1:     
                query = newExactQuery(campo,Integer.parseInt(consulta));
                break;
                
            case 2:     //Búsqueda por texto libre Phrase Query
                query = obtenerPhraseQuery(analizador,consulta, campo);
                break;
                
            case 3:     //TermQuery
                Term termino = new Term(campo, aplicarAnalizadorStringField(consulta,new LowerCaseAnalyzer()).get(0));
                query = new TermQuery(termino);
                break;
                       
        }
        
        configurarTopFieldFacetCollector();
        configurarFacetCollector();
        //resultadoConsulta = searcher.search(query,20);
        FacetsCollector.search(searcher, query, NDOCS, facets);
        searcher.search(query, collector);
        ArrayList<Document> documentos = obtenerDocumentos(collector.topDocs());
        
        return documentos;
    }
    
    
    private PhraseQuery obtenerPhraseQuery(Analizadores analizador,String consulta, String campo) throws IOException{
        Analyzer analyzer ;
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        switch(analizador){
            case LOWERCASE: 
                analyzer = new LowerCaseAnalyzer();
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
            Term aux = new Term(campo,palabra);
            builder.add(new Term(campo,palabra));
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
    
    private ArrayList<Document> obtenerNDocumentos(TopDocs resultadoConsulta, int n) throws IOException{
        
        
        ArrayList<Document> documentos = new ArrayList<>();
        ScoreDoc[] hits = resultadoConsulta.scoreDocs;
        
        if( n > hits.length )
            n = hits.length;
        
        for(int i = 0; i < n; i++){
            documentos.add(searcher.doc(hits[i].doc));
        }
        
        return documentos;
    }
    
   
    
    public List<FacetResult> obtenerTodasFacetas() throws IOException{
        List<FacetResult> resultado;
              
        Facets facetas = new FastTaxonomyFacetCounts(taxoReader,fconfig,facets);
        resultado = facetas.getAllDims(20);
        
        return resultado;
    }
    
    public FacetResult obtenerFacetaInstitucion() throws IOException{
        FacetResult resultado;
              
        Facets facetas = new FastTaxonomyFacetCounts(taxoReader,fconfig,facets);
        resultado = facetas.getTopChildren(20, "institution");
        
        return resultado;
    }
    
    public List<FacetResult> obtenerFacetaSizeRango() throws IOException{
        
        List<FacetResult> resultado;
        LongRange[] ranges = new LongRange[4];
        ranges[0] = new LongRange("[1-40]",1L, true, 40L, true);
        ranges[1] = new LongRange("[41-80]",41L, true, 80L, true);
        ranges[2] = new LongRange("[81-150]",81L, true, 150L, true);
        ranges[3] = new LongRange("[151-50000]",151L, true, 50000L, true);
        
        LongRangeFacetCounts facetas = new LongRangeFacetCounts("size",facets,ranges);
        
        resultado = facetas.getAllDims(20);
        return resultado;
    }
    
    
    public ArrayList<Document> buscarPorFaceta(String facetaElegida, String valorFaceta) throws IOException{
        ArrayList<Document> resultado;
        List<FacetResult> allDim;
        DrillDownQuery ddq = new DrillDownQuery(fconfig,query);
        
        ddq.add(facetaElegida,valorFaceta);
        
        DrillSideways ds = new DrillSideways(searcher,fconfig,taxoReader);
        
        DrillSidewaysResult dsresult = ds.search(ddq,20);
        resultado = this.obtenerDocumentos(dsresult.hits);

        return resultado;
           
    }
    
    public boolean consultaRealizada(){
        return (query == null)? false:true;
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
