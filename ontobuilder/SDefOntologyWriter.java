import java.net.URI;
import java.util.*;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ProtegeOWL;

public class SDefOntologyWriter {

	private JenaOWLModel owlModel;
	private int instanceCounter;
	private Hashtable<String,String> existingKeywords;	// Keep track of keywords and regex to avoid searching
	private Hashtable<String,String> existingRegex;	// through owlModel at each insert

	public SDefOntologyWriter(String templateURI) throws OntologyLoadException {
		owlModel = ProtegeOWL.createJenaOWLModelFromURI(templateURI);
		instanceCounter = 0;
		existingKeywords = new Hashtable<String,String>();
		existingRegex = new Hashtable<String,String>();
	}
	
	public void saveOntology(URI destinationURI) throws Exception{
		owlModel.save(destinationURI);		
	}
	
	public void translateInputLine(String line){
		String[] fields = line.split("\t");
		if (fields.length <1) return;
		
		// Find out syndrome names (sensitive and specific definitions):
		Vector<String> syndromeSens = new Vector<String>(4);
		if (fields[Constants.SENS_RESPIRATORY_DEF_INDEX].trim().equals("1")) syndromeSens.add(Constants.RESPIRATORY_SYNDROME_NAME);
		if (fields[Constants.SENS_GI_DEF_INDEX].trim().equals("1")) syndromeSens.add(Constants.GI_SYNDROME_NAME);
		if (fields[Constants.CONSTITUTIONAL_DEF_INDEX].trim().equals("1")) syndromeSens.add(Constants.CONSTITUTIONAL_SYNDROME_NAME);
		if (fields[Constants.ILI_DEF_INDEX].trim().equals("1")) syndromeSens.add(Constants.INFLUENZA_SYNDROME_NAME);

		Vector<String> syndromeSpec = new Vector<String>(4);
		if (fields[Constants.SPEC_RESPIRATORY_DEF_INDEX].trim().equals("1")) syndromeSpec.add(Constants.RESPIRATORY_SYNDROME_NAME);
		if (fields[Constants.SPEC_GI_DEF_INDEX].trim().equals("1")) syndromeSpec.add(Constants.GI_SYNDROME_NAME);
		if (fields[Constants.CONSTITUTIONAL_DEF_INDEX].trim().equals("1")) syndromeSpec.add(Constants.CONSTITUTIONAL_SYNDROME_NAME);
		if (fields[Constants.ILI_DEF_INDEX].trim().equals("1")) syndromeSpec.add(Constants.INFLUENZA_SYNDROME_NAME);

		// Read other fields:
		String condition = null;
		String concept = null;
		String relation = null;
		String inclKeywords = "";
		String exclKeywords = "";
		// TODO: Need to extend this to multiple coding systems:
		String cccID = "";
		String cccTitle = "";

		condition = fields[Constants.CONDITION_INDEX];
		if (fields.length > Constants.RELATION_INDEX) {
			concept = fields[Constants.CONCEPT_INDEX].toLowerCase();
			relation = fields[Constants.RELATION_INDEX];
		}
		if (fields.length > Constants.INCLUSION_KEYWORDS_REGEX_INDEX) inclKeywords = fields[Constants.INCLUSION_KEYWORDS_REGEX_INDEX];
		if (fields.length > Constants.EXCLUSION_KEYWORDS_REGEX_INDEX) exclKeywords = fields[Constants.EXCLUSION_KEYWORDS_REGEX_INDEX];
		if (fields.length > Constants.CCC_TITLE_INDEX) {
			cccID = fields[Constants.CCC_ID_INDEX];
			cccTitle = fields[Constants.CCC_TITLE_INDEX];
		}

		try {
			createOntologyResources(concept, condition, syndromeSens, syndromeSpec, relation, inclKeywords, exclKeywords, cccID, cccTitle);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void createOntologyResources(String conceptName, String conditionName, 
										Vector<String> syndromeSensDefName, Vector<String> syndromeSpecDefName, 
										String relation, String inclusionKeywords, String exclusionKeywords, 
										String codeID, String codeTitle) throws Exception{

		OWLNamedClass codingMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CODE);
		OWLDatatypeProperty codeIDProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_ID);
		OWLDatatypeProperty codeTitleProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_TITLE);
		OWLDatatypeProperty codingSystemProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODESYSTEM_NAME);
		OWLDatatypeProperty nameProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_NAME);

		OWLNamedClass newConcept = null;
		if (conceptName == null) {
			// We need to have at least one concept associated with a condition (exact concept),
			// so if there isn't one, we need to create it.
			conceptName = conditionName.toLowerCase();
			relation = "concept name";
			
			// check if such concept already exists:
			newConcept = owlModel.getOWLNamedClass(conceptName.replace(' ', '_'));
		}
		
		if (newConcept == null) {
			
			// 1. Create new subclass(instance) of Concept:
			OWLNamedClass conceptClass = owlModel.getOWLNamedClass(Constants.CLASS_CONCEPT);
			OWLNamedClass conceptMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CONCEPT);
			newConcept = (OWLNamedClass) conceptMetaClass.createInstance(conceptName.replace(' ', '_'));
			newConcept.addSuperclass(conceptClass);
			newConcept.removeSuperclass(owlModel.getOWLThingClass());
			newConcept.setPropertyValue(nameProperty, conceptName);
			
			// 2. Process keywords:
			processKeywordString(inclusionKeywords, newConcept, Constants.PROPERTY_HAS_INCLUSION_KEYWORDS);
			processKeywordString(exclusionKeywords, newConcept, Constants.PROPERTY_HAS_EXCLUSION_KEYWORDS);
		} else {
			System.out.println("WARNING: Non-unique concept '" + conceptName + "' resulting from artificially creating an exact concept for a condition that did not have any associated concepts listed.");
		}
		    		
		// 3. Check if ClinicalCondition exists, if not, create new subclass:
		//   (we'll handle CCC codes here too)
		OWLNamedClass conditionClass = owlModel.getOWLNamedClass(Constants.CLASS_CONDITION);
		OWLNamedClass conditionMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CONDITION);
		OWLObjectProperty externalCodingsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXTERNAL_CODES);
		OWLNamedClass condition = owlModel.getOWLNamedClass(conditionName.replace(' ', '_'));
		OWLNamedClass code = null;

		if (condition == null) {
			condition = (OWLNamedClass) conditionMetaClass.createInstance(conditionName.replace(' ', '_'));
			condition.addSuperclass(conditionClass);
			condition.removeSuperclass(owlModel.getOWLThingClass());
			condition.setPropertyValue(nameProperty, conditionName);
			// 4. Since this is a new condition, there should be a new external code:
			if (codeID.length() > 0 && codeTitle.length() > 0){
				// FIXME: Extend for multiple coding systems
				String codeClassName = "CCC_" + codeTitle.trim().replace(' ', '_');
				code = owlModel.getOWLNamedClass(codeClassName); 
				if (code != null) System.out.println("WARNING: External code '" + codeClassName + "' was assigned to more than one clinical concept!");
				else {
					code = (OWLNamedClass) codingMetaClass.createInstance(codeClassName);
					code.addSuperclass(owlModel.getOWLNamedClass(Constants.CODING_CCC));
					code.removeSuperclass(owlModel.getOWLThingClass());
					code.setPropertyValue(codeTitleProperty, codeTitle);
					code.setPropertyValue(codingSystemProperty, "CCC");
					code.setPropertyValue(codeIDProperty, codeID);
				}
				condition.addPropertyValue(externalCodingsProperty, code);
			}
			
			// 5. Add new condition to syndrome definition(s):
			OWLObjectProperty sensitiveDefinitionProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SENSITIVE_DEFINITION);
			OWLObjectProperty specificDefinitionProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SPECIFIC_DEFINITION);
			OWLNamedClass syndromeSens = null; 
			OWLNamedClass syndromeSpec = null; 
			if (syndromeSensDefName != null && syndromeSensDefName.size() > 0) {
				for (int i=0; i<syndromeSensDefName.size(); i++){
					syndromeSens = owlModel.getOWLNamedClass(syndromeSensDefName.get(i));
					syndromeSens.addPropertyValue(sensitiveDefinitionProperty, condition);
				}
			}
			if (syndromeSpecDefName != null && syndromeSpecDefName.size() > 0) {
				for (int i=0; i<syndromeSpecDefName.size(); i++){
					syndromeSpec = owlModel.getOWLNamedClass(syndromeSpecDefName.get(i));
					syndromeSpec.addPropertyValue(specificDefinitionProperty, condition);
				}
			}
		}
		
		// 6. Add Concept to one of the slots of ClinicalCondition
		OWLObjectProperty p = null;
		if (relation.equalsIgnoreCase("concept name")){
			p = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXACT_CONCEPT);
		} 
		else if (relation.substring(0, 7).equalsIgnoreCase("related")){
			p = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_RELATED_CONCEPTS);
		} 
		else if (relation.equalsIgnoreCase("synonym")){
			p = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SYNONYMOUS_CONCEPTS);
		}
		else throw new Exception("Cannot determine the relation of a concept to clinical condition! Relation specified is '" + relation + "'.");
		condition.addPropertyValue(p, newConcept);
	}
	
	private void processKeywordString(String keywordString, OWLNamedClass concept, String kwdPropertyName) 
	{
		// Adds keywords to this Concept and links them to regular expressions and UMLS CUIs
		
		if (keywordString.trim().length() == 0) return;
			
		OWLObjectProperty keywordsProperty = owlModel.getOWLObjectProperty(kwdPropertyName);
		OWLNamedClass keywordClass = owlModel.getOWLNamedClass(Constants.CLASS_KEYWORD);
		OWLNamedClass regexClass = owlModel.getOWLNamedClass(Constants.CLASS_REGEX);
		OWLDatatypeProperty hasStringProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_STRING);
		OWLDatatypeProperty hasRegexProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_REGEX_STRING);
		OWLObjectProperty mapsToCodeProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_MAPS_TO_CODE);
		OWLObjectProperty isMatchedByRegexProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_MATCHED_BY_REGEX);
		OWLNamedClass codingMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CODE);
		OWLDatatypeProperty codeIDProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_ID);
		OWLDatatypeProperty codeTitleProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_TITLE);
		OWLDatatypeProperty codingSystemProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODESYSTEM_NAME);
		
		OWLIndividual keywordInstance = null;
		OWLIndividual regexInstance = null;
		OWLNamedClass umlsCUI = null;
		String kwd = null;
		String regex = null;
		String cui = null;
		boolean includeCUI = (kwdPropertyName.equals(Constants.PROPERTY_HAS_INCLUSION_KEYWORDS)); 
		String kwdType = includeCUI ? "inclusion" : "exclusion";
		
		// a. split keyword list into triples {kwd, regex, cui} and process each triple:
		String[] kwds = keywordString.trim().split(":::");
		for (int i=0; i<kwds.length; i++){
			int delim1 = kwds[i].indexOf(" [");
			int delim2 = kwds[i].lastIndexOf("[");
			try {
				if (delim1 >= 0) {
					kwd = kwds[i].substring(0, delim1).trim();
					if (includeCUI){
						regex = kwds[i].substring(delim1+2, delim2).replace(']', ' ').trim();
						cui = kwds[i].substring(delim2+1).replace(']', ' ').trim();
					} else {
						regex = kwds[i].substring(delim1+2).replace(']', ' ').trim();
						cui = null;
					}
			
					if (includeCUI){
						// b. check if cui instance exists, and create it if not:
						String cuiName = "UMLS_CUI_" + cui.substring(0, cui.indexOf(':'));
						umlsCUI = owlModel.getOWLNamedClass(cuiName);
						if (umlsCUI == null){
							umlsCUI = (OWLNamedClass) codingMetaClass.createInstance(cuiName);
							umlsCUI.addSuperclass(owlModel.getOWLNamedClass(Constants.CODING_UMLS));
							umlsCUI.removeSuperclass(owlModel.getOWLThingClass());
							umlsCUI.setPropertyValue(codingSystemProperty, "UMLS");
							umlsCUI.setPropertyValue(codeTitleProperty, cui.substring(cui.indexOf(':')+1));
							umlsCUI.setPropertyValue(codeIDProperty, cui.substring(0, cui.indexOf(':')));
						}
					}
					
					// c. check if regex instance exists and create it if not:
					if (!existingRegex.containsKey(regex))
					{
						regexInstance = regexClass.createOWLIndividual("regex_" + this.instanceCounter);
						regexInstance.setPropertyValue(hasRegexProperty, regex);
						if (includeCUI) regexInstance.setPropertyValue(mapsToCodeProperty, umlsCUI);
						//existingRegex.put(regex, "regex_" + this.instanceCounter);
						existingRegex.put(regex, regexInstance.getBrowserText());
						this.instanceCounter++;
					} else {
						regexInstance = owlModel.getOWLIndividual(existingRegex.get(regex));
					}
				} else {
					kwd = kwds[i].trim();
					regexInstance = null;
					umlsCUI = null;
				}
				
				// d. create new instance of a keyword 
				//     (supposed to be unique, but not always, so issue a warning if not):
				if (!existingKeywords.containsKey(kwd)) {
					keywordInstance = keywordClass.createOWLIndividual("kwd_" + this.instanceCounter);
					keywordInstance.setPropertyValue(hasStringProperty, kwd);
					if (regexInstance != null) keywordInstance.setPropertyValue(isMatchedByRegexProperty, regexInstance);
					existingKeywords.put(kwd, keywordInstance.getBrowserText());
					this.instanceCounter++;
				} else {
					keywordInstance = owlModel.getOWLIndividual(existingKeywords.get(kwd));
					// check if this keyword duplicates the one belonging to the same concept or another
					if (concept.hasPropertyValue(keywordsProperty, keywordInstance)) {
						System.out.println("ERROR: Duplicate " + kwdType + " keyword '" + kwd + "' in concept '" + concept.getBrowserText() + "'! [skipping keyword]");
						continue;
					} else {
						System.out.println("WARNING: The " + kwdType + " keyword '" + kwd + "' in concept '" + concept.getBrowserText() + "' duplicates the same keyword in another concept!");
					}
				}
			
				// finally, add keyword to the concept:
				concept.addPropertyValue(keywordsProperty, keywordInstance);
			} catch (Exception e){
				System.out.println("The " + kwdType + " keyword " + kwds[i] + " caused problem:");
				e.printStackTrace();
			}
		}
	}
} 
