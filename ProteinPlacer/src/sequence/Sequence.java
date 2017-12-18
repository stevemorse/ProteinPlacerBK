package sequence;

import java.io.Serializable;

public class Sequence implements Serializable{
	
	/**
	 * represents a generic sequence
	 */
	private static final long serialVersionUID = -1695813529904820870L;

	protected String	sequence;
	
	protected boolean isPlacedByText;
	protected boolean isPlacedByGOTerms;
	protected boolean isPlacedByRBS;
	protected boolean isPlacedByMLS;
	
	protected String expressionPointText;
	protected String expressionPointGOText;
	protected String expressionPointRBS;
	protected String expressionPointMLS;
	
	public Sequence() {
	}
	
	public Sequence(String sequence) {
		super();
		this.sequence = sequence;
	}
	
	@Override
	public synchronized String toString() {
		return "Sequence [sequence=" + sequence + ", isPlacedByText="
				+ isPlacedByText + ", isPlacedByExpandedTerms="
				+ isPlacedByGOTerms + ", isPlacedByRBS=" + isPlacedByRBS
				+ ", isPlacedByMLS=" + isPlacedByMLS + ", expressionPointText="
				+ expressionPointText + ", expressionPointExpandedText="
				+ expressionPointGOText + ", expressionPointRBS="
				+ expressionPointRBS + ", expressionPointMLS="
				+ expressionPointMLS + "]";
	}
	
	public synchronized String getSequence() {
		return sequence;
	}
	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
	}	
	public synchronized boolean isPlacedByText() {
		return isPlacedByText;
	}
	public synchronized void setPlacedByText(boolean isPlacedByText) {
		this.isPlacedByText = isPlacedByText;
	}
	public synchronized boolean isPlacedByGOTerms() {
		return isPlacedByGOTerms;
	}
	public synchronized void setPlacedByGOTerms(boolean isPlacedByGOTerms) {
		this.isPlacedByGOTerms = isPlacedByGOTerms;
	}
	public synchronized boolean isPlacedByRBS() {
		return isPlacedByRBS;
	}
	public synchronized void setPlacedByRBS(boolean isPlacedByRBS) {
		this.isPlacedByRBS = isPlacedByRBS;
	}
	public synchronized boolean isPlacedByMLS() {
		return isPlacedByMLS;
	}
	public synchronized void setPlacedByMLS(boolean isPlacedByMLS) {
		this.isPlacedByMLS = isPlacedByMLS;
	}
	public synchronized String getExpressionPointText() {
		return expressionPointText;
	}
	public synchronized void setExpressionPointText(String expressionPointText) {
		this.expressionPointText = expressionPointText;
	}
	public synchronized String getExpressionPointGoText() {
		return expressionPointGOText;
	}
	public synchronized void setExpressionPointGOText(String expressionPointExpandedText) {
		this.expressionPointGOText = expressionPointExpandedText;
	}
	public synchronized String getExpressionPointRBS() {
		return expressionPointRBS;
	}
	public synchronized void setExpressionPointRBS(String expressionPointRBS) {
		this.expressionPointRBS = expressionPointRBS;
	}
	public synchronized String getExpressionPointMLS() {
		return expressionPointMLS;
	}
	public synchronized void setExpressionPointMLS(String expressionPointMLS) {
		this.expressionPointMLS = expressionPointMLS;
	}
	
}
