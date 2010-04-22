import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;


public class SDefOntoClassifier {

	public enum MatchType {EXACT, SYNONYM, RELATED}; 
	private JenaOWLModel owlModel;
	
	public SDefOntoClassifier(String ontoURI) throws OntologyLoadException{
		owlModel = ProtegeOWL.createJenaOWLModelFromURI(ontoURI);
	}
	
	public Classification classify(String input) throws Exception
	{
		Classification result = null;

		OWLNamedClass regexClass = owlModel.getOWLNamedClass(Constants.CLASS_REGEX);
		OWLIndividual regex = null;
		OWLDatatypeProperty regStrProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_REGEX_STRING);
		OWLObjectProperty kwdProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_MATCHES_KEYWORDS);
		OWLObjectProperty regexProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_MATCHED_BY_REGEX);
		OWLObjectProperty conceptProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_MATCHES_CONCEPT);
		OWLObjectProperty exclusionKwdsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXCLUSION_KEYWORDS);
		OWLObjectProperty exactProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_EXACT_CONCEPT);
		OWLObjectProperty synonymProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_SYNONYMOUS_CONCEPT);
		OWLObjectProperty relatedProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_RELATED_CONCEPT);
		OWLObjectProperty sensSyndromeProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_IN_SENSITIVE_DEFINITION);
		OWLObjectProperty specSyndromeProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_IS_IN_SPECIFIC_DEFINITION);
		
		// Retrieve all regex from the KB:
		Collection allRegEx = regexClass.getInstances(false);
		Iterator i = null;
		String regexStr = null; 
		
		for (i=allRegEx.iterator(); i.hasNext();)
		{
			boolean match = false;
			regex = (OWLIndividual)i.next();
			regexStr = (String) regex.getPropertyValue(regStrProperty);
			if (input.matches(regexStr))
			// If there is a match:
			{
				match = true;
				result = new Classification();
				
				// Trace the concept
				OWLIndividual keyword = (OWLIndividual)regex.getPropertyValue(kwdProperty);
				OWLNamedClass concept = (OWLNamedClass)keyword.getPropertyValue(conceptProperty);
				
				// Check if any exclusion keywords are matched as well 
				// (trace back through keywords to regex and pattern string)
				Collection exclKwds = concept.getPropertyValues(exclusionKwdsProperty);
				Iterator j = null;
				if (exclKwds.size() > 0){
					for (j=exclKwds.iterator(); j.hasNext();){
						OWLIndividual exkwd  = (OWLIndividual)j.next();
						OWLIndividual exregex = (OWLIndividual)exkwd.getPropertyValue(regexProperty); 
						if (input.matches((String)exregex.getPropertyValue(regStrProperty))) {
							match = false;
							break;
						}
					}
				}
				if (!match) continue;
				
				// If everything is OK, trace condition
				OWLNamedClass condition = (OWLNamedClass)concept.getPropertyValue(exactProperty);
				if (condition != null){
					result.matchType = MatchType.EXACT;
				} else {
					condition = (OWLNamedClass)concept.getPropertyValue(synonymProperty);
					if (condition != null){
						result.matchType = MatchType.SYNONYM;
					} else {
						condition = (OWLNamedClass)concept.getPropertyValue(relatedProperty);
						if (condition != null){
							result.matchType = MatchType.RELATED;
						} else {
							throw new Exception("ERROR: Matched orphaned Concept (no associated ClinicalCondition)");
						}
					}
				}

				Collection sensSyndromes = condition.getPropertyValues(sensSyndromeProperty);
				Collection specSyndromes = condition.getPropertyValues(sensSyndromeProperty);
				// Create a result object
			}
		}
				
		return result;
	}
}

class Classification 
{
	public String syndromeName;
	public boolean sensDef;
	public boolean specDef;
	public SDefOntoClassifier.MatchType matchType;
}
