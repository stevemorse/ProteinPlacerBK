package ruleBasedPlacer;

import java.util.ArrayList;
import java.util.List;

import protein.Protein;

public class ReteUnitTester {

	public static void main(String[] args){
		List<Protein> proteinList = new ArrayList<Protein>();
		ReteProcessor rp = new ReteProcessor();
		
		//fill test proteins
		//meets test for nucleus
		Protein np = new Protein();
		np.setBlast2GoFileName("matches nucleus");
		np.setPlacedByText(true);
		np.setExpressionPointText("nucleus");
		String nps = "xxxxxxrkrkrxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxnp";
		np.getProteinSequences().remove(0);
		np.getProteinSequences().add(nps);
		Protein npg = new Protein();
		npg.setBlast2GoFileName("matches nucleus");
		npg.setPlacedByGOTerms(true);
		npg.setExpressionPointGOText("nucleus");
		String npgs = "rkkkkxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxnpg";
		npg.getProteinSequences().remove(0);
		npg.getProteinSequences().add(npgs);
		proteinList.add(np);
		proteinList.add(npg);
		
		//meets test for mitochondrion
		Protein mp = new Protein();
		mp.setBlast2GoFileName("matches mitochondrion");
		mp.setPlacedByText(true);
		mp.setExpressionPointText("mitochondrion");
		String mps = "xxxrxxxkxxrxxxrxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxmp";
		mp.getProteinSequences().remove(0);
		mp.getProteinSequences().add(mps);
		Protein mpg = new Protein();
		mpg.setBlast2GoFileName("matches mitochondrion");
		mpg.setPlacedByGOTerms(true);
		mpg.setExpressionPointGOText("mitochondrion");
		String mpgs = "xxxxxkxxxkxxkxxxkxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxmpg";
		mpg.getProteinSequences().remove(0);
		mpg.getProteinSequences().add(mpgs);
		proteinList.add(mp);
		proteinList.add(mpg);
		
		//meets test for chloroplast plant
		Protein cpp = new Protein();
		cpp.setBlast2GoFileName("matches chloroplast plant");
		cpp.setPlacedByText(true);
		cpp.setExpressionPointText("chloroplast");
		String cpps = "xxxststststststsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpp";
		cpp.getProteinSequences().remove(0);
		cpp.getProteinSequences().add(cpps);
		Protein cppg = new Protein();
		cppg.setBlast2GoFileName("matches chloroplast plant");
		cppg.setPlacedByGOTerms(true);
		cppg.setExpressionPointGOText("chloroplast");
		String cppgs = "xxxxxstxstxstxstxstxstxsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcppg";
		cppg.getProteinSequences().remove(0);
		cppg.getProteinSequences().add(cppgs);
		proteinList.add(cpp);
		proteinList.add(cppg);
		
		//meets test for chloroplast dinos
		Protein cpd = new Protein();
		cpd.setBlast2GoFileName("matches chloroplast dinos");
		cpd.setPlacedByText(true);
		cpd.setExpressionPointText("chloroplast");
		String cpds = "avlavlavlccxxxststststststsxrxxxrxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpd";
		cpd.getProteinSequences().remove(0);
		cpd.getProteinSequences().add(cpds);
		Protein cpdg = new Protein();
		cpdg.setBlast2GoFileName("matches chloroplast dinos");
		cpdg.setPlacedByGOTerms(true);
		cpdg.setExpressionPointGOText("chloroplast");
		String cpdgs = "avlavlavlccxxxstxstxstxstxstxstxsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpdg";
		cpdg.getProteinSequences().remove(0);
		cpdg.getProteinSequences().add(cpdgs);
		proteinList.add(cpd);
		proteinList.add(cpdg);
		
		//meets test for secretory pathway
		Protein sp = new Protein();
		sp.setBlast2GoFileName("matches secretory pathway");
		sp.setPlacedByText(true);
		sp.setExpressionPointText("secretory pathway");
		String sps = "spxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		sp.getProteinSequences().remove(0);
		sp.getProteinSequences().add(sps);
		Protein spg = new Protein();
		spg.setBlast2GoFileName("matches secretory pathway");
		spg.setPlacedByGOTerms(true);
		spg.setExpressionPointGOText("secretory pathway");
		String spgs = "spgxxxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		spg.getProteinSequences().remove(0);
		spg.getProteinSequences().add(spgs);
		proteinList.add(sp);
		proteinList.add(spg);
		
		//meets test for er retention
		Protein erp = new Protein();
		erp.setBlast2GoFileName("matches er retention");
		erp.setPlacedByText(true);
		erp.setExpressionPointText("endoplasmic reticulum");
		String erps = "erpxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxkdel";
		erp.getProteinSequences().remove(0);
		erp.getProteinSequences().add(erps);
		Protein erpg = new Protein();
		erpg.setBlast2GoFileName("matches er retention");
		erpg.setPlacedByGOTerms(true);
		erpg.setExpressionPointGOText("endoplasmic reticulum");
		String erpgs = "ergpxxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxkdel";
		erpg.getProteinSequences().remove(0);
		erpg.getProteinSequences().add(erpgs);
		proteinList.add(erp);
		proteinList.add(erpg);
		
		//meets test for peroxisome
		Protein pp = new Protein();
		pp.setBlast2GoFileName("matches peroxisome");
		pp.setPlacedByText(true);
		pp.setExpressionPointText("peroxisome");
		String pps = "ppxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxskl";
		pp.getProteinSequences().remove(0);
		pp.getProteinSequences().add(pps);
		Protein ppg = new Protein();
		ppg.setBlast2GoFileName("matches peroxisome");
		ppg.setPlacedByGOTerms(true);
		ppg.setExpressionPointGOText("peroxisome");
		String ppgs = "ppgxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxskl";
		ppg.getProteinSequences().remove(0);
		ppg.getProteinSequences().add(ppgs);
		proteinList.add(pp);
		proteinList.add(ppg);
		
		//meets test for none
		Protein dp = new Protein();
		dp.setPlacedByText(true);
		dp.setExpressionPointText("");
		String dps = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		dp.getProteinSequences().remove(0);
		dp.getProteinSequences().add(dps);
		Protein dpg = new Protein();
		dpg.setPlacedByGOTerms(true);
		dpg.setExpressionPointGOText("");
		String dpgs = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		dpg.getProteinSequences().remove(0);
		dpg.getProteinSequences().add(dpgs);
		proteinList.add(dp);
		proteinList.add(dpg);
		
				
		//run them through rete processor
		rp.processProteins(proteinList);	
	}//main
}
