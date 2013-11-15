;(import protein.Protein)

(defrule Secretory_pathway 
    "location is in Secretory_pathway"
    (secretory_pathway)
    =>
    (printout t "location is in Secretory_pathway
    ")
    ;(modify ?currentProtein setPlacedByRBS true)
    ;(modify ?currentProtein setExpressionPointRBS "secretory pathway"))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "secretory pathway")))

(defrule Nucleus 
    "location is in Nucleus"
    (nucleus)
    =>
    (printout t "location is in Nucleus")
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "nucleus")))
    
(defrule Mitochondrion 
    "location is in Mitochondrion"
    (mitochondrion)
    =>
    (printout t "location is in Mitochondrion
    ")
    ;(?currentProtein setPlacedByRBS true)
    ;(?currentProtein setExpressionPointRBS mitochondrion))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "mitochondrion")))

(defrule Chloroplast_plant 
    "location is in Chloroplast_plant"
    (chloroplast_plant)
    =>
    (printout t "location is in Chloroplast_plant
    ")
    ;(?currentProtein setPlacedByRBS true)
    ;(?currentProtein setExpressionPointRBS chloroplast))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "chloroplast")))

(defrule Chloroplast_dinos 
    "location is in Chloroplast_dinos"
    (chloroplast_dinos)
    =>
    (printout t "location is in Chloroplast_dinos
    ")
    ;(?currentProtein setPlacedByRBS true)
    ;(?currentProtein setExpressionPointRBS chloroplast))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "chloroplast")))

(defrule Er_retention 
    "location is in Er_retention"
    (secretory_pathway)
    (er_retention)
    =>
    (printout t "location is in Er_retention
    ")
    ;(?currentProtein setPlacedByRBS true)
    ;(?currentProtein setExpressionPointRBS "endoplasmic reticulum"))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "endoplasmic reticulum")))

(defrule Peroxisome 
    "location is in Peroxisome"
    (peroxisome)
    =>
    (printout t "location is in Peroxisome
    ")
    ;(?currentProtein setPlacedByRBS true)
    ;(?currentProtein setExpressionPointRBS peroxisome))
	(modify ?id (placedByRBS TRUE)(expressionPointRBS "peroxisome")))