package protein;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sequence.Sequence;

public class Protein extends Sequence{
	
	/**
	 * represents a protein in the system, extends sequence
	 */
	private static final long serialVersionUID = 7940145680316370293L;
	private Map<String,String> annotations = null;
	private String proteinSequence = null;
	private String blast2GoFileName = null;
	private boolean processed;
	
	public Protein() {
		super();
		setProcessed(false);
		proteinSequence = "NOT MATCHED";
	}

	public Protein(String sequence) {
		super(sequence);
		setProcessed(false);
		proteinSequence = "NOT MATCHED";
	}
	
	public Protein(String sequence, String type) {
		super(sequence);
		setProcessed(false);
		proteinSequence = "NOT MATCHED";
		if(type.compareTo("GO") == 0){
			annotations = new HashMap<String,String>();
		}
	}
	
	public Protein(String sequence, String type, String blast2GoFileName) {
		super(sequence);
		setProcessed(false);
		proteinSequence = "NOT MATCHED";
		if(type.compareTo("GO") == 0){
			annotations = new HashMap<String,String>();
		}
		this.blast2GoFileName = blast2GoFileName;
	}
	
	public Map<String,String> getAnnotations(){
		return annotations;
	}
	
	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}
	
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

	public String toString(){
		return "Protien [sequence= " +  sequence
		+ ", proteinSequence = " + proteinSequence
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

	public String getProteinSequence() {
		return proteinSequence;
	}

	public void setProteinSequence(String proteinsequence) {
		this.proteinSequence = proteinsequence;
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

	
}
