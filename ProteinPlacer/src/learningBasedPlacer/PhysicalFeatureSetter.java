package learningBasedPlacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protein.Protein;
/**
 * sets the physical features values for an amino acid
 * @author steve
 * @version 1.0
 */
public class PhysicalFeatureSetter {

	/**
	 * goes through each amino acid in a protein sequence and accumulates physical property
	 * values for each amino acid in two regions, the N-terminal area and the rest of the protein.
	 * These include hydrophobicity, polarity, charge/acidity, c-beta binding, aromatic side chain,
	 * alaphatic side chain and van der Waals radius (size).
	 * @param currentProtein the current protein to accumulate physical feature values for
	 * @param numberPhysicalCatagories the number of physical properties to be examined
	 * @return a List of Doubles indicating the accumulated values for each property for each region
	 */
	public static List<Double> setPhysicalFeatures(Protein currentProtein, int numberPhysicalCatagories){
		List<Double> physicalProperties = new ArrayList<Double>();
		
		for(int physicalsCount = 0; physicalsCount < numberPhysicalCatagories; physicalsCount++){
			physicalProperties.add((double) 0);
		}
		
		//split protein into N-terminal, middle and C-terminal sections
		String proteinSequence = currentProtein.getProteinSequences().get(0);
		int sequencelength = proteinSequence.length();
		int NtermBegin = sequencelength - 100;
		if(NtermBegin <= 0){
			System.err.println("sequence length less than 100 amino acids");
			NtermBegin = sequencelength;
		}
		
		for(int aminoAcidCount = 0; aminoAcidCount < sequencelength; aminoAcidCount++){
			//hydrophibicty for body and N term
			if(Character.toUpperCase(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount))) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(0, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(1, value);
				}//if in N term section	
			}//if hydrophobic
			
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(2, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(3, value);
				}//if in N term section	
			}//if indifferent 
			
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(4, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(5, value);
				}//if in N term section	
			}//if hydrophilic
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//load polarity values
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(6, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(7, value);
				}//if in N term section	
			}//if polar
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(8, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(9, value);
				}//if in N term section	
			}//if non-polar
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//load values for acid/base (positive/negative) side chains
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(10, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(11, value);
				}//if in N term section	
			}//if positive
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(12, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(13, value);
				}//if in N term section	
			}//if neutral
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(14, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(15, value);
				}//if in N term section	
			}//if negative
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//load values for aliphatic side chains
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(16, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(17, value);
				}//if in N term section	
			}//if aliphatic
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(18, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(19, value);
				}//if in N term section	
			}//if not aliphatic
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//load values for aromatic side chains
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(20, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(21, value);
				}//if in N term section	
			}//if aromatic
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(22, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(23, value);
				}//if in N term section	
			}//if not aromatic
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//load values for C-beta branching side chains
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(20, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(21, value);
				}//if in N term section	
			}//if C-beta branching
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G' ||
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K' || 
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M' || 
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P' || 
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R' || 
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S' || Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W' || 
					Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += 1.0/(sequencelength-100);
					physicalProperties.set(22, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += 1.0/100;
					physicalProperties.set(23, value);
				}//if in N term section	
			}//if not C-beta branching
			else{
				System.err.println("error: unknown amino acid");
			}
			
			//volume of amino acid (van der Waals)
			int maxVanVolume = 163;
			Map<Character,Integer> vanVolumes = new HashMap<Character,Integer>(){
				private static final long serialVersionUID = 8256547342982927739L;
				{
				put(new Character('A'),new Integer(92));
				put(new Character('C'),new Integer(106));
				put(new Character('D'),new Integer(91));
				put(new Character('E'),new Integer(109));
				put(new Character('F'),new Integer(135));
				put(new Character('G'),new Integer(48));
				put(new Character('H'),new Integer(118));
				put(new Character('I'),new Integer(124));
				put(new Character('K'),new Integer(135));
				put(new Character('L'),new Integer(124));
				put(new Character('M'),new Integer(124));
				put(new Character('N'),new Integer(96));
				put(new Character('P'),new Integer(90));
				put(new Character('Q'),new Integer(114));
				put(new Character('R'),new Integer(148));
				put(new Character('S'),new Integer(73));
				put(new Character('T'),new Integer(93));
				put(new Character('V'),new Integer(105));
				put(new Character('W'),new Integer(163));
				put(new Character('Y'),new Integer(141));
				}};
			
			if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'A'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('A')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('A')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if A
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'C'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('C')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('C')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if C
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'D'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('D')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('D')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if D
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'E'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('E')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('E')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if E
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'F'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('F')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('F')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if F
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'G'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('G')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('G')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if G
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'H'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('H')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('H')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if H
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'I'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('I')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('I')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if I
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'K'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('K')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('K')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if K
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'L'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('L')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('L')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if L
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'M'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('M')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('M')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if M
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'N'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('N')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('N')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if N
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'P'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('P')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('P')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if P
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Q'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('Q')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('Q')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if Q
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'R'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('R')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('R')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if R
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'S'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('S')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('S')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if S
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'T'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('T')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('T')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if T
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'V'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('V')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('V')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if V
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'W'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('W')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('W')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if W
			else if(Character.toUpperCase(proteinSequence.charAt(aminoAcidCount)) == 'Y'){
				if(aminoAcidCount < NtermBegin){
					double value = physicalProperties.get(0);
					value += (vanVolumes.get('Y')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(24, value);
				}//if before N term section
				else if(aminoAcidCount >= NtermBegin){
					double value = physicalProperties.get(1);
					value += (vanVolumes.get('Y')/maxVanVolume)/(sequencelength-100);
					physicalProperties.set(25, value);
				}//if in N term section	
			}//if Y
			else{
				System.err.println("error: unknown amino acid");
			}
			
		}//for aminoAcidCount
		
		
		return physicalProperties;	
	}
	
}
