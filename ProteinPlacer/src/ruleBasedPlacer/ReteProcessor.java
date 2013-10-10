package ruleBasedPlacer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import protein.Protein;
import jess.*;

public class ReteProcessor {
	private static File proteinsOutFile = new File ("/home/steve/Desktop/ProteinPlacer/data/allRuleBasedResults.bin");
	
	private static int nucleusCount = 0;
	private static int mitochondrionCount = 0;
	private static int chloroplast_plantCount = 0;
	private static int chloroplast_dinosCount = 0;
	private static int secretory_pathwayCount = 0;
	private static int er_retentionCount = 0;
	private static int peroxisomeCount = 0;

	public void processProteins(List<Protein> proteinList){
		Protein currentProtein = null;
		Rete engine = new Rete();
		try {
			engine.defclass("Protein", "protein.Protein", null);
			engine.batch("/home/steve/git/ProteinPlacer/ProteinPlacer/src/ruleBasedPlacer/rules.clp");
		} catch (JessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		//set up watch on rete engine
		Context context = engine.getGlobalContext();
		Funcall f;
		try {
			f = new Funcall("watch", engine);
			f.arg("all");
			f.execute(context);
			//engine.executeCommand("facts");
			engine.eval("(facts)");
			engine.eval("(rules)");
		} catch (JessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//make an output stream
		FileOutputStream fout;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(proteinsOutFile);
			oos = new ObjectOutputStream(fout);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		ListIterator<Protein> proteinListIter = proteinList.listIterator();
		while(proteinListIter.hasNext()){
			currentProtein = proteinListIter.next();
			//if matched then define a rete instance of the protein in the rete engine
			if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
				int currentFactId = 0;
				try {
					Value currentFact = engine.definstance("Protein", currentProtein, false);
					currentFactId = currentFact.intValue(engine.getGlobalContext());
					System.out.println("fact no. for: " + currentProtein.getBlast2GoFileName() + " is: " + currentFactId);
					engine.eval("(bind ?id " + currentFactId + " )");
					//engine.batch("C:\\Users\\Steve\\workspace2\\ProteinPlacer\\src\\ruleBasedPlacer\\rules.clp");
				} catch (JessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
				//apply motif tests for cell locations to the protein
				if(checkSecretoryPathway(currentProtein) && !checkChloroplastDinos(currentProtein)){
					try {
						engine.assertString("(secretory_pathway)");
						secretory_pathwayCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"secretory_pathway\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkSecretoryPathway " + je.getMessage());
						je.printStackTrace();
					}
				}
				
				if(checkNucleus(currentProtein)){
					try {
						engine.assertString("(nucleus)", engine.getGlobalContext());
						nucleusCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"nucleus\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkNucleus " + je.getMessage());
						je.printStackTrace();
					}
				}
				
				if(checkMitochondrion(currentProtein)){
					try {
						engine.assertString("(mitochondrion)");
						mitochondrionCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"mitochondrion\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkMitochondrion " + je.getMessage());
						je.printStackTrace();
					}
				}
				
				if(checkChloroplastPlant(currentProtein)){
					try {
						engine.assertString("(chloroplast_plant)");
						chloroplast_plantCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"chloroplast\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkChloroplastPlant " + je.getMessage());
						je.printStackTrace();
					}
				}
				
				if(checkChloroplastDinos(currentProtein)){
					try {
						engine.assertString("(chloroplast_dinos)");
						chloroplast_dinosCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"chloroplast\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkChloroplastDinos " + je.getMessage());
						je.printStackTrace();
					}
				}
				
				if(checkERRetention(currentProtein)){
					try {
						engine.assertString("(er_retention)");
						er_retentionCount++;
						/*
						if(checkSecretoryPathway(currentProtein)){
							engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"endoplasmic reticulum\"))");
						*/
					} catch (JessException je) {
						System.out.println("jess fail in checkERRetention " + je.getMessage());
						je.printStackTrace();
					}
				}
		
				
				if(checkPeroxisome(currentProtein)){
					try {
						engine.assertString("(peroxisome)");
						peroxisomeCount++;
						//engine.eval("(modify ?id (placedByRBS TRUE)(expressionPointRBS \"peroxisome\"))");
					} catch (JessException je) {
						System.out.println("jess fail in checkPeroxisome " + je.getMessage());
						je.printStackTrace();
					}
				}//if checkPeroxisome
			}//if protein sequence is matched
			
			try {
				engine.run();
				engine.reset();
			} catch (JessException je) {
				System.out.println("jess fail in reseting facts " + je.getMessage());
				je.printStackTrace();
			}
			
			//print the processed protein to file
			try {
				oos.writeObject(currentProtein);
			} catch (IOException ioe) {
				System.out.println("problem writing protein to file: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}//while proteinListIter
		
		try {
			oos.close();
		} catch (IOException ioe) {
			System.out.println("problem closingfile: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		//make region output
		System.out.println("nucleusCount: " + nucleusCount);
		System.out.println("mitochondrionCount: " + mitochondrionCount);
		System.out.println("chloroplast_plantCount: " + chloroplast_plantCount);
		System.out.println("chloroplast_dinosCount: " + chloroplast_dinosCount);
		System.out.println("secretory_pathwayCount: " + secretory_pathwayCount);
		System.out.println("er_retentionCount: " + er_retentionCount);
		System.out.println("peroxisomeCount: " + peroxisomeCount);
	}//processProteins
	
	public boolean checkNucleus(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
System.out.println("in checkNucleus seq is: " + seq);
			int arginineOrLysine = 0;
			for(int aminoAcidCount = 0; aminoAcidCount < seq.length(); aminoAcidCount++){
				if(seq.charAt(aminoAcidCount) == 'r' || seq.charAt(aminoAcidCount) == 'k'){
					arginineOrLysine++;
				}//if
				else{
					arginineOrLysine = 0;
				}//else
				if(arginineOrLysine == 5){
System.out.println("match in checkNucleus");
					return true;
				}
			}//for
		}//if
		return false;
	}//method checkNucleus
	
	public boolean checkMitochondrion(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String first20 = seq.substring(0,19);
System.out.println("in checkMitochondrion first20 is: " + first20);
			for(int aminoAcidCount = 0; aminoAcidCount < first20.length() -12; aminoAcidCount++){
				if((first20.charAt(aminoAcidCount) == 'r' || first20.charAt(aminoAcidCount) == 'k') &&
						(first20.charAt(aminoAcidCount + 1) != 'r' && first20.charAt(aminoAcidCount + 1) != 'k') &&
						(first20.charAt(aminoAcidCount + 2) != 'r' && first20.charAt(aminoAcidCount + 2) != 'k') &&
						(first20.charAt(aminoAcidCount + 3) != 'r' && first20.charAt(aminoAcidCount + 3) != 'k') &&
						(first20.charAt(aminoAcidCount + 4) == 'r' || first20.charAt(aminoAcidCount + 4) == 'k') &&
						(first20.charAt(aminoAcidCount + 5) != 'r' && first20.charAt(aminoAcidCount + 5) != 'k') &&
						(first20.charAt(aminoAcidCount + 6) != 'r' && first20.charAt(aminoAcidCount + 6) != 'k') &&
						(first20.charAt(aminoAcidCount + 7) == 'r' || first20.charAt(aminoAcidCount + 7) == 'k') &&
						(first20.charAt(aminoAcidCount + 8) != 'r' && first20.charAt(aminoAcidCount + 8) != 'k') &&
						(first20.charAt(aminoAcidCount + 9) != 'r' && first20.charAt(aminoAcidCount + 9) != 'k') &&
						(first20.charAt(aminoAcidCount + 10) != 'r' && first20.charAt(aminoAcidCount + 10) != 'k') &&
						(first20.charAt(aminoAcidCount + 11) == 'r' || first20.charAt(aminoAcidCount + 11) == 'k')){
System.out.println("match in checkMitochondrion");
					return true;
				}//if
			}//for
			
		}//if
		return false;
	}//method checkMitochondrion
	
	public boolean checkChloroplastPlant(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String first50 = seq.substring(0,49);
System.out.println("in checkChloroplastPlant first50 is: " + first50);
			int serineOrThreonine = 0;
			for(int aminoAcidCount = 0; aminoAcidCount < first50.length(); aminoAcidCount++){
				if(first50.charAt(aminoAcidCount) == 's' || first50.charAt(aminoAcidCount) == 't'){
					serineOrThreonine++;
				}//if
			}//for
System.out.println("serineOrThreonine: " + serineOrThreonine);
			if(serineOrThreonine >= first50.length()/4){
System.out.println("match in checkChloroplastPlant");
				return true;
			}
		}//if
		return false;
	}//method checkChloroplastPlant
	
	public boolean checkChloroplastDinos(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String first15 = seq.substring(0,14);
System.out.println("in checkChloroplastDinos first15 is: " + first15);
			int hydrophobic = 0;
			int semiHydrophobic = 0;
			int endOfHydrophopicRegion = 15;
			for(int aminoAcidCount = 0; aminoAcidCount < first15.length(); aminoAcidCount++){
				if(first15.charAt(aminoAcidCount) == 'a' || first15.charAt(aminoAcidCount) == 'v' ||
					first15.charAt(aminoAcidCount) == 'l' || first15.charAt(aminoAcidCount) == 'i' ||
					first15.charAt(aminoAcidCount) == 'p' || first15.charAt(aminoAcidCount) == 'm' ||
					first15.charAt(aminoAcidCount) == 'f' || first15.charAt(aminoAcidCount) == 'w' ){
					hydrophobic++;
				}//if
				else if(first15.charAt(aminoAcidCount) == 'g' || first15.charAt(aminoAcidCount) == 's' ||
						first15.charAt(aminoAcidCount) == 't' || first15.charAt(aminoAcidCount) == 'c' ||
						first15.charAt(aminoAcidCount) == 'n' || first15.charAt(aminoAcidCount) == 'q' ||
						first15.charAt(aminoAcidCount) == 'y' ){
					semiHydrophobic++;
				}//else if
				else{
					endOfHydrophopicRegion = aminoAcidCount;
				}//else
			}//for
			if(hydrophobic+semiHydrophobic >= 10 && semiHydrophobic <= 2){
				String choppedSeq = seq.substring(endOfHydrophopicRegion);
				Protein fakeProtein = new Protein();
				fakeProtein.setProteinSequence(choppedSeq);
				if(checkChloroplastPlant(fakeProtein)){
System.out.println("match in checkChloroplastDinos");
					return true;
				}//if checkChloroplastPlant fakeProtein
			}//if hydrophobic region
		}//if matched
		return false;
	}//method checkChloroplastDinos
	
	public boolean checkSecretoryPathway(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String first30 = seq.substring(0,29);
System.out.println("in checkSecretoryPathway first30 is: " + first30);
			int hydrophobic = 0;
			int semiHydrophobic = 0;
			for(int aminoAcidCount = 0; aminoAcidCount < first30.length(); aminoAcidCount++){
				if(first30.charAt(aminoAcidCount) == 'a' || first30.charAt(aminoAcidCount) == 'v' ||
						first30.charAt(aminoAcidCount) == 'l' || first30.charAt(aminoAcidCount) == 'i' ||
						first30.charAt(aminoAcidCount) == 'p' || first30.charAt(aminoAcidCount) == 'm' ||
						first30.charAt(aminoAcidCount) == 'f' || first30.charAt(aminoAcidCount) == 'w' ){
						hydrophobic++;
					}//if
					else if(first30.charAt(aminoAcidCount) == 'g' || first30.charAt(aminoAcidCount) == 's' ||
							first30.charAt(aminoAcidCount) == 't' || first30.charAt(aminoAcidCount) == 'c' ||
							first30.charAt(aminoAcidCount) == 'n' || first30.charAt(aminoAcidCount) == 'q' ||
							first30.charAt(aminoAcidCount) == 'y' ){
						semiHydrophobic++;
					}//else if
					else{
						hydrophobic = 0;
						semiHydrophobic = 0;	
					}//else
				if(hydrophobic+semiHydrophobic >= 10 && semiHydrophobic <= 2){
System.out.println("match in checkSecretoryPathway");
					return true;
				}//if hydrophobic region
			}//for
		}//if
		return false;
	}//method checkSecretoryPathway
	
	public boolean checkERRetention(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String last4 = seq.substring(seq.length()-4,seq.length());
System.out.println("in checkERRetention last4 is: " + last4);
			if(last4.compareTo("kdel") == 0){
System.out.println("match in checkERRetention");
				return true;
			}//if
		}//if
		return false;
	}//method checkERRetention
	
	public boolean checkPeroxisome(Protein currentProtein){
		if(currentProtein.getProteinSequence().compareTo("NOT MATCHED") != 0){
			String seq = currentProtein.getProteinSequence();
			String last3 = seq.substring(seq.length()-3,seq.length());
System.out.println("in checkPeroxisome last3 is: " + last3);
			//System.out.println(seq);
			//System.out.println(last3);
			if(last3.compareTo("skl") == 0){
System.out.println("match in checkPeroxisome");
				return true;
			}//if skl
		}//if
		return false;
	}////method checkPeroxisome
	
	
	public List<Protein> loadProteins (File proteinsInFile){
		List<Protein> proteinList = new ArrayList<Protein>();
		Protein currentProtein = null;
		
		InputStream file = null;
		InputStream buffer = null;
		ObjectInput input = null;
		try{
			file = new FileInputStream(proteinsInFile);
		    buffer = new BufferedInputStream(file);
		    input = new ObjectInputStream (buffer);
			while(true){
				currentProtein = (Protein) input.readObject();
				proteinList.add(currentProtein);
			}//while
		} catch(ClassNotFoundException cnfe){
			System.out.println("class not found: " + cnfe.getMessage());
		} catch(IOException ioe){
			System.out.println("class not found: " + ioe.getMessage());
		}
		
		System.out.println("number of proteins to process is: " + proteinList.size());
		return proteinList;
	}//loadProteins
}//ReteTester
