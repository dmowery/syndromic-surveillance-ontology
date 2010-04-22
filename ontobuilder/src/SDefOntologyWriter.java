import java.net.URI;
import java.util.*;
import com.csvreader.*;

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
	
	public void processCsvInput(String inputCsvURI)
	{
		CsvReader inputRr = null;
		Vector<String> syndromeSens = new Vector<String>(4);
		Vector<String> syndromeSpec = new Vector<String>(4);
		String condition = null;
		String concept = null;
		String relation = null;
		String inclKeywords = null;
		String exclKeywords = null;
		Vector<Coding> codes = new Vector<Coding>();
		String cccID = null;
		String cccTitle = null;
		String meshCode = null;
		String icd9Code = null;
		String icd10Code = null;
		String snomedCode = null;
		
		try {
			inputRr = new CsvReader(inputCsvURI);
			inputRr.readHeaders();
			while (inputRr.readRecord())
			{
				if (inputRr.get(Constants.SENS_RESPIRATORY_DEF_COLUMN).trim().equals("1")) syndromeSens.add(Constants.RESPIRATORY_SYNDROME_NAME);
				if (inputRr.get(Constants.SENS_GI_DEF_COLUMN).trim().equals("1")) syndromeSens.add(Constants.GI_SYNDROME_NAME);
				if (inputRr.get(Constants.CONSTITUTIONAL_DEF_COLUMN).trim().equals("1")) syndromeSens.add(Constants.CONSTITUTIONAL_SYNDROME_NAME);
				if (inputRr.get(Constants.ILI_DEF_COLUMN).trim().equals("1")) syndromeSens.add(Constants.INFLUENZA_SYNDROME_NAME);
				if (inputRr.get(Constants.SPEC_RESPIRATORY_DEF_COLUMN).trim().equals("1")) syndromeSpec.add(Constants.RESPIRATORY_SYNDROME_NAME);
				if (inputRr.get(Constants.SPEC_GI_DEF_COLUMN).trim().equals("1")) syndromeSpec.add(Constants.GI_SYNDROME_NAME);
				if (inputRr.get(Constants.CONSTITUTIONAL_DEF_COLUMN).trim().equals("1")) syndromeSpec.add(Constants.CONSTITUTIONAL_SYNDROME_NAME);
				if (inputRr.get(Constants.ILI_DEF_COLUMN).trim().equals("1")) syndromeSpec.add(Constants.INFLUENZA_SYNDROME_NAME);
				
				condition = inputRr.get(Constants.CONDITION_COLUMN).trim();
				concept = inputRr.get(Constants.CONCEPT_COLUMN).trim();
				relation = inputRr.get(Constants.RELATION_COLUMN).trim();
				inclKeywords = inputRr.get(Constants.INCLUSION_KEYWORDS_REGEX_COLUMN).trim();
				exclKeywords = inputRr.get(Constants.EXCLUSION_KEYWORDS_REGEX_COLUMN).trim();
				if (relation.length() == 0) relation = null;
				if (concept.length() > 0) concept = concept.toLowerCase();
				else concept = null;
				
				cccID = inputRr.get(Constants.CCC_ID_COLUMN).trim();
				cccTitle = inputRr.get(Constants.CCC_TITLE_COLUMN).trim();
				meshCode = inputRr.get(Constants.MESH_CODE_COLUMN).trim();
				icd9Code = inputRr.get(Constants.ICD9_CODE_COLUMN).trim();
				icd10Code = inputRr.get(Constants.ICD10_CODE_COLUMN).trim();
				snomedCode = inputRr.get(Constants.SNOWMED_CODE_COLUMN).trim();
				if (cccID.length() > 0 && cccTitle.length() > 0) codes.add(new Coding(Constants.CODING_CCC, cccID, cccTitle));
				if (meshCode.length() > 0) codes.add(new Coding(Constants.CODING_MESH, meshCode, null));
				if (snomedCode.length() > 0) codes.add(new Coding(Constants.CODING_SNOMED, snomedCode, null));
				if (icd9Code.length() > 0) codes.add(new Coding(Constants.CODING_ICD9, icd9Code, null));
				if (icd10Code.length() > 0) codes.add(new Coding(Constants.CODING_ICD10, icd10Code, null));
				
				createOntologyResources(concept, condition, syndromeSens, syndromeSpec, relation, inclKeywords, exclKeywords, codes);
				syndromeSens.clear();
				syndromeSpec.clear();
				codes.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			inputRr.close();
		}
	}
	
	private void createOntologyResources(String conceptName, String conditionName, 
										Vector<String> syndromeSensDefNames, Vector<String> syndromeSpecDefNames, 
										String relation, String inclusionKeywords, String exclusionKeywords, 
										Vector<Coding> codes) throws Exception{

		//OWLNamedClass codingMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CODE);
		OWLDatatypeProperty codeIDProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_ID);
		OWLDatatypeProperty codeTitleProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_TITLE);
		OWLDatatypeProperty codingSystemProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODESYSTEM_NAME);
		OWLDatatypeProperty nameProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_NAME);
		OWLObjectProperty externalCodingsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXTERNAL_CODES);

		OWLNamedClass newConcept = null;
		if (conceptName == null) {
			// We need to have at least one concept associated with a condition (exact concept),
			// so if there isn't one, we need to create it.
			System.out.println("WARNING: Missing concept for condition " + conditionName + ". A concept with the same name will be created.");

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
			
			// 2. Add external codes:
			Coding c = null;
			String codeName = null;
			OWLIndividual code = null;
			OWLNamedClass codeClass = null;

			for (int i=0; i<codes.size(); i++){
				c = codes.get(i);
				codeName = c.system + "_" + c.codeID.trim().replace(' ', '_');
				code = owlModel.getOWLIndividual(codeName);
				if (code == null)                  {
					codeClass = owlModel.getOWLNamedClass(c.system);
					code = codeClass.createOWLIndividual(codeName);
					code.setPropertyValue(codingSystemProperty, c.system);
					code.setPropertyValue(codeIDProperty, c.codeID);
					if (c.codeTitle != null) code.setPropertyValue(codeTitleProperty, c.codeTitle);
				}
				newConcept.addPropertyValue(externalCodingsProperty, code);
			}

			// 3. Process keywords:
			processKeywordString(inclusionKeywords, newConcept, Constants.PROPERTY_HAS_INCLUSION_KEYWORDS);
			processKeywordString(exclusionKeywords, newConcept, Constants.PROPERTY_HAS_EXCLUSION_KEYWORDS);
		} else {
			System.out.println("WARNING: Non-unique concept '" + conceptName + "' resulting from artificially creating an exact concept for a condition that did not have any associated concepts listed.");
		}
		    		
		// 4. Check if ClinicalCondition exists, if not, create new subclass:
		OWLNamedClass conditionClass = owlModel.getOWLNamedClass(Constants.CLASS_CONDITION);
		OWLNamedClass conditionMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_CONDITION);
		OWLNamedClass condition = owlModel.getOWLNamedClass(conditionName.replace(' ', '_'));

		if (condition == null) {
			condition = (OWLNamedClass) conditionMetaClass.createInstance(conditionName.replace(' ', '_'));
			condition.addSuperclass(conditionClass);
			condition.removeSuperclass(owlModel.getOWLThingClass());
			condition.setPropertyValue(nameProperty, conditionName);
			
			// 5. Add new condition to syndrome definition(s):
			OWLObjectProperty sensitiveDefinitionProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SENSITIVE_DEFINITION);
			OWLObjectProperty specificDefinitionProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SPECIFIC_DEFINITION);
			OWLNamedClass syndromeSens = null; 
			OWLNamedClass syndromeSpec = null; 
			if (syndromeSensDefNames != null && syndromeSensDefNames.size() > 0) {
				for (int i=0; i<syndromeSensDefNames.size(); i++){
					syndromeSens = owlModel.getOWLNamedClass(syndromeSensDefNames.get(i));
					syndromeSens.addPropertyValue(sensitiveDefinitionProperty, condition);
				}
			}
			if (syndromeSpecDefNames != null && syndromeSpecDefNames.size() > 0) {
				for (int i=0; i<syndromeSpecDefNames.size(); i++){
					syndromeSpec = owlModel.getOWLNamedClass(syndromeSpecDefNames.get(i));
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
		OWLNamedClass umlsClass = owlModel.getOWLNamedClass(Constants.CODING_UMLS);
		OWLDatatypeProperty hasStringProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_STRING);
		OWLDatatypeProperty hasRegexProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_REGEX_STRING);
		OWLObjectProperty mapsToCodeProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_MAPS_TO_CODE);
		OWLObjectProperty isMatchedByRegexProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_MATCHED_BY_REGEX);
		OWLObjectProperty externalCodingsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXTERNAL_CODES);
		OWLDatatypeProperty codeIDProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_ID);
		OWLDatatypeProperty codeTitleProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODE_TITLE);
		OWLDatatypeProperty codingSystemProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_CODESYSTEM_NAME);
		
		OWLIndividual keywordInstance = null;
		OWLIndividual regexInstance = null;
		OWLIndividual umlsCUI = null;
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
						umlsCUI = owlModel.getOWLIndividual(cuiName);
						if (umlsCUI == null){
							umlsCUI = umlsClass.createOWLIndividual(cuiName);
							umlsCUI.setPropertyValue(codingSystemProperty, Constants.CODING_UMLS);
							umlsCUI.setPropertyValue(codeTitleProperty, cui.substring(cui.indexOf(':')+1));
							umlsCUI.setPropertyValue(codeIDProperty, cui.substring(0, cui.indexOf(':')));
						}
						concept.addPropertyValue(externalCodingsProperty, umlsCUI);
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

class Coding {
	public String system;
	public String codeID;
	public String codeTitle;
	
	public Coding(String sys, String id, String title)
	{
		system = sys;
		codeID = id;
		codeTitle = title;
	}
}

