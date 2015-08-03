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
	private boolean processed;
	
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
	
	/*
	public List<String> getProteinSequence(){
		return proteinSequences;
	}*/
	
	
	/**
	 * makes a string of all annotations in key/value form
	 * @return a string for use in toString
	 */
	public String listAllAnnotations(){
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
	
	public String listAllProteinSequences(){
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

	public String toString(){
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
		+ expressionPointMLS + "]\n" + listAllAnnotations();	
	}

	public List<String> getProteinSequences() {
		return proteinSequences;
	}

	public void setProteinSequences(List<String> proteinsequence) {
		this.proteinSequences = proteinsequence;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public String getBlast2GoFileName() {
		return blast2GoFileName;
	}

	public void setBlast2GoFileName(String blast2GoFileName) {
		this.blast2GoFileName = blast2GoFileName;
	}
	
	public Map<String, String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}

	public Map<String, String> getAllFoundRegionsInText() {
		return allFoundRegionsInText;
	}

	public void setAllFoundRegionsInText(Map<String, String> allFoundRegionsInText) {
		this.allFoundRegionsInText = allFoundRegionsInText;
	}
	
}//class
