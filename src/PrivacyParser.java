
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.*;
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
	private final float THRESHOLD = 2.5f;
	
	public PrivacyParser() {
		weightsTable.put("information", 1f);
		weightsTable.put("collect", 1.5f);
		weightsTable.put("collection", 1.5f);

		prop = new Properties();
		prop.setProperty("annotators", "tokenize,ssplit,pos,lemma");
		this.pipeline = new StanfordCoreNLP(prop);
	}
	
	public List<Float> generateWeightedList (List<CoreMap> sentences) {
		List<Float> result = new ArrayList<Float>();
		for (CoreMap sentence : sentences) {
			float total = 0;
			for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
		        if (weightsTable.containsKey(cl.getString(CoreAnnotations.LemmaAnnotation.class).toLowerCase())) {
		        	total += weightsTable.get(cl.getString(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
		        }
			}
			result.add(total);
		}
		return result;
	}

	public List<Float> generatePositionWeights (List<Float> weightedList, float threshold) {
		for (int i = 0; i < weightedList.size(); i++) {
			if (weightedList.get(i).floatValue() >= threshold) {
				weightedList.set(i + 1, weightedList.get(i+1) + 0.5f);
			}
		}
		return weightedList;
	}
	
	public String summarize(String document) throws IOException {
	    Annotation annotation = pipeline.process(document);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    List<Float> weightedList = generateWeightedList(sentences);
		List<Float> postionalyWeightedList = generatePositionWeights(weightedList, THRESHOLD);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < postionalyWeightedList.size(); i++) {
			if (postionalyWeightedList.get(i).floatValue() >= THRESHOLD) {
				sb.append(i);
				sb.append(sentences.get(i).toString());
				sb.append("\n");
			}
		}
		File file = new File("final_output.txt");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

//	    for (int i = 0; i < weightedList.size(); i++) {
//			bw.write(weightedList.get(i).toString() + " " + sentences.get(i).toString() + "\n");
//		}
		bw.write(sb.toString());
		bw.close();
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

