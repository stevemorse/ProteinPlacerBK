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
	public String toString() {
		return "Sequence [sequence=" + sequence + ", isPlacedByText="
				+ isPlacedByText + ", isPlacedByExpandedTerms="
				+ isPlacedByGOTerms + ", isPlacedByRBS=" + isPlacedByRBS
				+ ", isPlacedByMLS=" + isPlacedByMLS + ", expressionPointText="
				+ expressionPointText + ", expressionPointExpandedText="
				+ expressionPointGOText + ", expressionPointRBS="
				+ expressionPointRBS + ", expressionPointMLS="
				+ expressionPointMLS + "]";
	}
	
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public boolean isPlacedByText() {
		return isPlacedByText;
	}
	public void setPlacedByText(boolean isPlacedByText) {
		this.isPlacedByText = isPlacedByText;
	}
	public boolean isPlacedByGOTerms() {
		return isPlacedByGOTerms;
	}
	public void setPlacedByGOTerms(boolean isPlacedByExpandedTerms) {
		this.isPlacedByGOTerms = isPlacedByExpandedTerms;
	}
	public boolean isPlacedByRBS() {
		return isPlacedByRBS;
	}
	public void setPlacedByRBS(boolean isPlacedByRBS) {
		this.isPlacedByRBS = isPlacedByRBS;
	}
	public boolean isPlacedByMLS() {
		return isPlacedByMLS;
	}
	public void setPlacedByMLS(boolean isPlacedByMLS) {
		this.isPlacedByMLS = isPlacedByMLS;
	}
	public String getExpressionPointText() {
		return expressionPointText;
	}
	public void setExpressionPointText(String expressionPointText) {
		this.expressionPointText = expressionPointText;
	}
	public String getExpressionPointGoText() {
		return expressionPointGOText;
	}
	public void setExpressionPointGOText(String expressionPointExpandedText) {
		this.expressionPointGOText = expressionPointExpandedText;
	}
	public String getExpressionPointRBS() {
		return expressionPointRBS;
	}
	public void setExpressionPointRBS(String expressionPointRBS) {
		this.expressionPointRBS = expressionPointRBS;
	}
	public String getExpressionPointMLS() {
		return expressionPointMLS;
	}
	public void setExpressionPointMLS(String expressionPointMLS) {
		this.expressionPointMLS = expressionPointMLS;
	}
	
}
