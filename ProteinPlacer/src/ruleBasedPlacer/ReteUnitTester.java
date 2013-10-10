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
		np.setProteinSequence(nps);
		Protein npg = new Protein();
		npg.setBlast2GoFileName("matches nucleus");
		npg.setPlacedByGOTerms(true);
		npg.setExpressionPointGOText("nucleus");
		String npgs = "rkkkkxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxnpg";
		npg.setProteinSequence(npgs);
		proteinList.add(np);
		proteinList.add(npg);
		
		//meets test for mitochondrion
		Protein mp = new Protein();
		mp.setBlast2GoFileName("matches mitochondrion");
		mp.setPlacedByText(true);
		mp.setExpressionPointText("mitochondrion");
		String mps = "xxxrxxxkxxrxxxrxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxmp";
		mp.setProteinSequence(mps);
		Protein mpg = new Protein();
		mpg.setBlast2GoFileName("matches mitochondrion");
		mpg.setPlacedByGOTerms(true);
		mpg.setExpressionPointGOText("mitochondrion");
		String mpgs = "xxxxxkxxxkxxkxxxkxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxmpg";
		mpg.setProteinSequence(mpgs);
		proteinList.add(mp);
		proteinList.add(mpg);
		
		//meets test for chloroplast plant
		Protein cpp = new Protein();
		cpp.setBlast2GoFileName("matches chloroplast plant");
		cpp.setPlacedByText(true);
		cpp.setExpressionPointText("chloroplast");
		String cpps = "xxxststststststsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpp";
		cpp.setProteinSequence(cpps);
		Protein cppg = new Protein();
		cppg.setBlast2GoFileName("matches chloroplast plant");
		cppg.setPlacedByGOTerms(true);
		cppg.setExpressionPointGOText("chloroplast");
		String cppgs = "xxxxxstxstxstxstxstxstxsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcppg";
		cppg.setProteinSequence(cppgs);
		proteinList.add(cpp);
		proteinList.add(cppg);
		
		//meets test for chloroplast dinos
		Protein cpd = new Protein();
		cpd.setBlast2GoFileName("matches chloroplast dinos");
		cpd.setPlacedByText(true);
		cpd.setExpressionPointText("chloroplast");
		String cpds = "avlavlavlccxxxststststststsxrxxxrxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpd";
		cpd.setProteinSequence(cpds);
		Protein cpdg = new Protein();
		cpdg.setBlast2GoFileName("matches chloroplast dinos");
		cpdg.setPlacedByGOTerms(true);
		cpdg.setExpressionPointGOText("chloroplast");
		String cpdgs = "avlavlavlccxxxstxstxstxstxstxstxsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxcpdg";
		cpdg.setProteinSequence(cpdgs);
		proteinList.add(cpd);
		proteinList.add(cpdg);
		
		//meets test for secretory pathway
		Protein sp = new Protein();
		sp.setBlast2GoFileName("matches secretory pathway");
		sp.setPlacedByText(true);
		sp.setExpressionPointText("secretory pathway");
		String sps = "spxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		sp.setProteinSequence(sps);
		Protein spg = new Protein();
		spg.setBlast2GoFileName("matches secretory pathway");
		spg.setPlacedByGOTerms(true);
		spg.setExpressionPointGOText("secretory pathway");
		String spgs = "spgxxxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		spg.setProteinSequence(spgs);
		proteinList.add(sp);
		proteinList.add(spg);
		
		//meets test for er retention
		Protein erp = new Protein();
		erp.setBlast2GoFileName("matches er retention");
		erp.setPlacedByText(true);
		erp.setExpressionPointText("endoplasmic reticulum");
		String erps = "erpxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxkdel";
		erp.setProteinSequence(erps);
		Protein erpg = new Protein();
		erpg.setBlast2GoFileName("matches er retention");
		erpg.setPlacedByGOTerms(true);
		erpg.setExpressionPointGOText("endoplasmic reticulum");
		String erpgs = "ergpxxxxxxxavlavlavlccxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxkdel";
		erpg.setProteinSequence(erpgs);
		proteinList.add(erp);
		proteinList.add(erpg);
		
		//meets test for peroxisome
		Protein pp = new Protein();
		pp.setBlast2GoFileName("matches peroxisome");
		pp.setPlacedByText(true);
		pp.setExpressionPointText("peroxisome");
		String pps = "ppxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxskl";
		pp.setProteinSequence(pps);
		Protein ppg = new Protein();
		ppg.setBlast2GoFileName("matches peroxisome");
		ppg.setPlacedByGOTerms(true);
		ppg.setExpressionPointGOText("peroxisome");
		String ppgs = "ppgxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxskl";
		ppg.setProteinSequence(ppgs);
		proteinList.add(pp);
		proteinList.add(ppg);
		
		//meets test for none
		Protein dp = new Protein();
		dp.setPlacedByText(true);
		dp.setExpressionPointText("");
		String dps = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		dp.setProteinSequence(dps);
		Protein dpg = new Protein();
		dpg.setPlacedByGOTerms(true);
		dpg.setExpressionPointGOText("");
		String dpgs = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		dpg.setProteinSequence(dpgs);
		proteinList.add(dp);
		proteinList.add(dpg);
		
				
		//run them through rete processor
		rp.processProteins(proteinList);	
	}//main
}
