
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class PrivacyParser {
	private Properties prop;
	private StanfordCoreNLP pipeline;
	private HashMap<String, Float> weightsTable = new HashMap<String, Float>();
	
	public PrivacyParser() {
		weightsTable.put("information", 1f);
		weightsTable.put("collect", 1.5f);
		
		prop = new Properties();
		prop.setProperty("annotators", "tokenize,ssplit,pos,lemma");
		this.pipeline = new StanfordCoreNLP(prop);
	}
	
	public List<Float> generateWeightedList (List<CoreMap> sentences) {
		List<Float> result = new ArrayList<Float>();
		for (CoreMap sentence : sentences) {
			float total = 0;
			for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
		        if (weightsTable.containsKey(cl.getString(CoreAnnotations.LemmaAnnotation.class))) {
		        	total += weightsTable.get(cl.getString(CoreAnnotations.LemmaAnnotation.class));
		        }
			}
			result.add(total);
		}
		return result;
	}
	
	public String summarize(String document) throws FileNotFoundException, UnsupportedEncodingException {
	    Annotation annotation = pipeline.process(document);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    List<Float> weightedList = generateWeightedList(sentences);
	    PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
	    for (Float weight : weightedList) {
	    	System.out.println(weight);
	    }
	    writer.close();
	    return null;
	  }
	
	public static void main (String[] args) throws IOException {
		String filename = args[0];
	    String content = IOUtils.slurpFile(filename);
		
		PrivacyParser parser = new PrivacyParser();
		parser.summarize(content);
		System.out.println("completed");
	}
}

