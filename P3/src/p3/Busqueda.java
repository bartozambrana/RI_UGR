
package p3;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import static org.apache.lucene.document.IntPoint.newExactQuery;
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
    private IndexSearcher searcher;
    private QueryParser parser;
    private Query query;
    private ArrayList<Analyzer> analyzers;
    private IndexReader reader;
    
    
    
    public Busqueda() throws IOException {
        this.indexPath = "./indices/";
        
        try {
            
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            reader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(reader);
            
        } catch (IOException e) {
            System.err.println("Error Al obtener el Documento Asociado a Índice: " + e);
            System.exit(-1);
        }
        // Analizadores utilizados para los campos
        analyzers = new ArrayList<>();
        analyzers.add(new LowerCaseAnalyzer());
        analyzers.add(new EnglishAnalyzer());
        analyzers.add( new WhitespaceAnalyzer());
    }
    
    public ArrayList<Document> search(String campo, String consulta) throws ParseException, IOException{
        ArrayList<Document> documentos = new ArrayList<>();
        
        if(campo.equals("title") || campo.equals("author") || campo.equals("institution") ){
            System.out.println("ENTRO A CONSULTAR");
            if(campo.equals("institution"))
                documentos = consultar(consulta,campo,3,Analizadores.DEFAULT);
            else{
                System.out.println("ELSE");
                documentos = consultar(consulta, campo, 2, Analizadores.LOWERCASE);
            }
                    
        }else if(campo.equals("brief") || campo.equals("text")){
            documentos = consultar(consulta, campo, 2, Analizadores.ENGLISH);
        }else if(campo.equals("size")){
            documentos = consultar(consulta, campo,1,Analizadores.WHITESPACE);
        }
        
        
        return documentos;
    }
    
    public ArrayList<Document> booleanSearch(String campo, String consulta, String campo2, String consulta2) throws IOException{
        ArrayList<Document> documentos;
        
        Query q1 = getQuery(campo,consulta);
        Query q2 = getQuery(campo2,consulta2);
        
        BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.MUST);
        BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.MUST);
        
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
        bqbuilder.add(bc1);
        bqbuilder.add(bc2);
        
        BooleanQuery bq = bqbuilder.build();
        
        documentos = obtenerDocumentos(searcher.search(bq,20));
        
        return documentos;
    }
    
    
    private Query getQuery(String campo, String consulta) throws IOException{
        Query resultado = null;
        
        if(campo.equals("title") || campo.equals("author") || campo.equals("institution") ){
            
            if(campo.equals("institution")){
                Term termino = new Term(campo,consulta);
                resultado = new TermQuery(termino);
            }else{
                resultado = obtenerPhraseQuery(Analizadores.LOWERCASE, consulta, campo);
            }
                    
        }else if(campo.equals("brief") || campo.equals("text")){
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
        
        resultadoConsulta = searcher.search(query,20);
        ArrayList<Document> documentos = obtenerDocumentos(resultadoConsulta);
        
        return documentos;
    }
    
    
    private PhraseQuery obtenerPhraseQuery(Analizadores analizador,String consulta, String campo) throws IOException{
        Analyzer analyzer ;
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        switch(analizador){
            case LOWERCASE: 
                analyzer = new LowerCaseAnalyzer();
                System.out.println("lowercaseanalyzer");
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
            System.out.println(palabra);
            Term aux = new Term(campo,palabra);
            builder.add(new Term(campo,palabra));
            System.out.println(" Término añadido: " + aux.toString());
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
            }catch(IOException e2){
               System.err.println("Error Al cerrar el Documento Asociado a Índice: " + e2);
               System.exit(-2); 
            }
    }
    
    
    
}
