package protein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import sequence.Sequence;

public class Protein extends Sequence{
	
	/**
	 * represents a protein in the system, extends sequence
	 */
	private static final long serialVersionUID = 7940145680316370293L;
	private Map<String,String> annotations = null;
	private Map<String,String> allFoundRegionsInText = null;
	private List<String> proteinSequences = null;
	private String blast2GoFileName = null;
	private String errorMode = "";
	private boolean processed;
	private boolean gotGenebank;
	
	
	public Protein() {
		super();
		setProcessed(false);
		proteinSequences = new ArrayList<String>();
		proteinSequences.add("NOT MATCHED");
		allFoundRegionsInText = new HashMap<String,String>();
	}

	public Protein(String sequence) {
		super(sequence);
		setProcessed(false);
		proteinSequences = new ArrayList<String>();
		proteinSequences.add("NOT MATCHED");
		allFoundRegionsInText = new HashMap<String,String>();
	}
	
	public Protein(String sequence, String type) {
		super(sequence);
		setProcessed(false);
		proteinSequences = new ArrayList<String>();
		proteinSequences.add("NOT MATCHED");
		if(type.compareTo("GO") == 0){
			annotations = new HashMap<String,String>();
		}
		allFoundRegionsInText = new HashMap<String,String>();
	}
	
	public Protein(String sequence, String type, String blast2GoFileName) {
		super(sequence);
		setProcessed(false);
		proteinSequences = new ArrayList<String>();
		proteinSequences.add("NOT MATCHED");
		if(type.compareTo("GO") == 0){
			annotations = new HashMap<String,String>();
		}
		this.blast2GoFileName = blast2GoFileName;
		allFoundRegionsInText = new HashMap<String,String>();
	}
	
	/**
	 * makes a string of all annotations in key/value form
	 * @return a string for use in toString
	 */
	public synchronized String listAllAnnotations(){
		String annotationStr = "";
		if(annotations != null){
			Iterator<Map.Entry<String, String>> mapIterator = annotations.entrySet().iterator();
			while(mapIterator.hasNext()){
				Map.Entry<String, String> currentEntry = mapIterator.next();
				annotationStr += "Annotation Key: " + currentEntry.getKey() + "\t\tAnnotation Value: " + currentEntry.getValue() + "\n";
			}//while lit
		}//if not null
		else{
			annotationStr = "null";
		}
		return annotationStr;
	}
	
	public synchronized String listAllProteinSequences(){
		String proteinSequenceStr = "";
		int proteinCount = 0;
		if(proteinSequences != null){
			ListIterator<String> proteinSequenceLiter= proteinSequences.listIterator();
			while(proteinSequenceLiter.hasNext()){
				String currentStr = proteinSequenceLiter.next();
				proteinSequenceStr += "proteinSequence: " + proteinCount++ + "\t\tis: " + currentStr + "\n";
			}//while lit
		}//if not null
		else{
			proteinSequenceStr = "null";
		}
		return proteinSequenceStr;
	}

	public synchronized String listAllFoundRegionsInText(){
		String allFoundRegionsInTextString = "All regions found in text are: \n";
		List<String> regionAccessions = new ArrayList<String>(allFoundRegionsInText.keySet());
		ListIterator<String> regionAccessionsLiter = regionAccessions.listIterator();
		while(regionAccessionsLiter.hasNext()){
			String currentAccession = regionAccessionsLiter.next();
			allFoundRegionsInTextString = allFoundRegionsInTextString + "region: " 
			+ allFoundRegionsInText.get(currentAccession) + " found for accession: " + currentAccession + "\n";
		}//while
		return allFoundRegionsInTextString;
		
	}
	
	public synchronized String toString(){
		return "Protien [sequence= " +  sequence
		+ ", proteinSequences = " + listAllProteinSequences()
		+ ", processed = " + processed
		+ ", isPlacedByText= "
		+ isPlacedByText + ", isPlacedByExpandedTerms= "
		+ isPlacedByGOTerms + ", isPlacedByRBS= " + isPlacedByRBS
		+ ", isPlacedByMLS= " + isPlacedByMLS + ", expressionPointText= "
		+ expressionPointText + ", expressionPointGoText= "
		+ expressionPointGOText + ", expressionPointRBS= "
		+ expressionPointRBS + ", expressionPointMLS= "
		+ expressionPointMLS + "]\n" + listAllAnnotations() + listAllFoundRegionsInText();	
	}

	public synchronized List<String> getProteinSequences() {
		return proteinSequences;
	}

	public synchronized void setProteinSequences(List<String> proteinsequence) {
		this.proteinSequences = proteinsequence;
	}

	public synchronized boolean isProcessed() {
		return processed;
	}

	public synchronized void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public synchronized String getErrorMode() {
		return errorMode;
	}

	public synchronized void setErrorMode(String errorMode) {
		this.errorMode = errorMode;
	}
	public synchronized String getBlast2GoFileName() {
		return blast2GoFileName;
	}

	public synchronized void setBlast2GoFileName(String blast2GoFileName) {
		this.blast2GoFileName = blast2GoFileName;
	}
	
	public synchronized Map<String, String> getAnnotations() {
		return annotations;
	}

	public synchronized void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}

	public synchronized Map<String, String> getAllFoundRegionsInText() {
		return allFoundRegionsInText;
	}

	public synchronized void setAllFoundRegionsInText(Map<String, String> allFoundRegionsInText) {
		this.allFoundRegionsInText = allFoundRegionsInText;
	}

	public synchronized boolean isGotGenebank() {
		return gotGenebank;
	}

	public synchronized void setGotGenebank(boolean gotGenebank) {
		this.gotGenebank = gotGenebank;
	}
	
}//class
