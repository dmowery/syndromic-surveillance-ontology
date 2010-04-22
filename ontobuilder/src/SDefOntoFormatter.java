import java.io.*;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ProtegeOWL;

public class SDefOntoFormatter {
	
	private JenaOWLModel owlModel;

	public SDefOntoFormatter(String ontoURI) throws OntologyLoadException 
	{
		owlModel = ProtegeOWL.createJenaOWLModelFromURI(ontoURI);
	}
	
	public void generateHtmlSummary(String outputURI, String templateURI) throws IOException
	{
		// Open output file for writing:
		BufferedWriter output = new BufferedWriter(new FileWriter(outputURI));

		// Open HTML template and copy header portion:
		BufferedReader template = new BufferedReader(new FileReader(templateURI));
		String line = null;
		while((line=template.readLine()) != null) {
			output.write(line);
			output.newLine();
		}
		template.close();
		
		// Write some more initial HTML:
		output.write("<div class=\"content\">");		output.newLine();
		output.write("<h2>Ontology Browser</h2><br/>"); output.newLine();
		
		// Read syndrome definitions from OWL model and create main HTML content
		OWLNamedClass syndromeMetaClass = owlModel.getOWLNamedClass(Constants.METACLASS_SYNDROME);
		OWLDatatypeProperty nameProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_NAME);
		OWLObjectProperty sensDefProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SENSITIVE_DEFINITION);
		OWLObjectProperty specDefProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SPECIFIC_DEFINITION);
		OWLNamedClass syndrome = null;
		Collection syndromes = syndromeMetaClass.getInstances(false);
		Iterator i = null;
		int count = 0;
		
		// a. First list all syndromes as tab headers:
		String shortSyndromeName = null; 
		output.write("<!-- Switch tabs -->"); output.newLine();
		for (i=syndromes.iterator(); i.hasNext(); count++){
			syndrome = (OWLNamedClass)i.next();
			shortSyndromeName = syndrome.getPropertyValue(nameProperty).toString();
			shortSyndromeName = shortSyndromeName.substring(0, shortSyndromeName.lastIndexOf(' '));
			output.write("<span class=\"syndrome_tab\" id=\"tab" + count + "\" onclick=\"switchtab('" + count + "');\">" + shortSyndromeName + "</span>");
			output.newLine();
		}

		// b. Write syndrome definitions (second pass through the list of syndromes):
		count = 0;
		output.write("<!-- Syndrome descriptions -->"); output.newLine();
		for (i=syndromes.iterator(); i.hasNext(); count++){
			syndrome = (OWLNamedClass)i.next();
			output.write("<div class=\"syndrome_tab\" id=\"syndrome" + count + "\">");
			output.write("<h3>" + syndrome.getPropertyValue(nameProperty).toString() + "</h3>");
			output.newLine();
			output.write("<p>Sensitive definition: " + syndrome.getPropertyValueCount(sensDefProperty) + " conditions<br/>");
			output.write("Specific definition: " + syndrome.getPropertyValueCount(specDefProperty) + " conditions</p>");		output.newLine();
			output.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");		output.newLine();
			output.write("\t<tr>");			output.newLine();
  			output.write("\t\t<td><h4>Clinical Condition</h4></td>");		output.newLine();
  			output.write("\t\t<td><h4>Sensitive</h4></td>");		output.newLine();
  			output.write("\t\t<td><h4>Specific</h4></td>");		output.newLine();
			output.write("\t</tr>");		output.newLine();

			// c. Write a list of clinical conditions:
			listConditions(count, syndrome, output);

			output.write("</table></div>");		output.newLine();
		}
		output.write("</div>");	output.newLine();
 
		// Finally output some closing HTML:
		output.write("<div class=\"footer\">");	output.newLine();
		output.write("<p><img src=\"McGillLogo_small.gif\" alt=\"McGill logo\" />");	output.newLine();
		output.write("<img src=\"UPittLogo_small.gif\" alt=\"U of Pittsburgh logo\" />");	output.newLine();
		output.write("<img src=\"NiiLogo_small.gif\" alt=\"NII logo\" /></p>");	output.newLine();
		output.write("</div>");	output.newLine();
		output.write("</div>");	output.newLine();
		output.write("</div></body></html>");	output.newLine();
		
		output.close();
	}
	
	private void listConditions(int syndromeNum, OWLNamedClass syndrome, BufferedWriter output) throws IOException
	{
		OWLObjectProperty sensDefProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SENSITIVE_DEFINITION);
		OWLObjectProperty specDefProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SPECIFIC_DEFINITION);
		OWLObjectProperty exactConceptProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_EXACT_CONCEPT);
		OWLObjectProperty synonymousConceptsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_SYNONYMOUS_CONCEPTS);
		OWLObjectProperty relatedConceptsProperty = owlModel.getOWLObjectProperty(Constants.PROPERTY_HAS_RELATED_CONCEPTS);
		OWLDatatypeProperty nameProperty = owlModel.getOWLDatatypeProperty(Constants.PROPERTY_HAS_NAME);
		OWLNamedClass concept = null;
		
		Collection sensConditions = syndrome.getPropertyValues(sensDefProperty);
		// TODO: make the list sorted!
		Collection specConditions = syndrome.getPropertyValues(specDefProperty);
		Iterator i = null;
		int count = 0;
		
		for (i=sensConditions.iterator(); i.hasNext(); count++){
			OWLNamedClass condition = (OWLNamedClass)i.next();

			// Create a row in a table and write condition name and its relation to sensitive and specific definitions
			if (count%2 == 0) output.write("<tr class=\"odd\">"); 
			else output.write("<tr class=\"even\">");
			output.newLine();
			output.write("\t<td><p><a class=\"toggle\" onclick=\"toggle(this, 'det" + syndromeNum + "_" + count + "'); return 0;\">+</a> ");
			output.write(condition.getPropertyValue(nameProperty).toString() + "</p></td>"); output.newLine();
			output.write("\t<td><img src=\"check.gif\" alt=\"check mark\" /></td>"); output.newLine();
			if (specConditions.contains(condition)) 
				output.write("\t<td><img src=\"check.gif\" alt=\"check mark\" /></td>");
			else output.write("\t<td>&ndash;</td>"); 
			output.newLine();
			output.write("</tr>"); output.newLine();

			// Output the properties of condition
			if (count%2 == 0) output.write("<tr class=\"odd1\">"); 
			else output.write("<tr class=\"even1\">");
			output.newLine();
			output.write("\t<td colspan=\"3\">"); output.newLine();
			output.write("\t\t<p class=\"details\" id=\"det" + syndromeNum + "_" + count + "\">"); output.newLine();
			// a. Exact concept:
			concept = (OWLNamedClass)condition.getPropertyValue(exactConceptProperty);
			output.write("\t\t<span class=\"prop_name\">Exact concept:</span> " + concept.getPropertyValue(nameProperty) + "<br/>"); output.newLine();
			// b. Related concepts:
			String conceptStr = "";
			Collection concepts = condition.getPropertyValues(relatedConceptsProperty);
			Iterator j = null;
			for (j=concepts.iterator(); j.hasNext();)
				conceptStr += ((OWLNamedClass)j.next()).getPropertyValue(nameProperty) + ", ";
			if (conceptStr.length() > 0) {
				conceptStr = conceptStr.substring(0, conceptStr.length()-2);
				output.write("\t\t<span class=\"prop_name\">Related concepts:</span> " + conceptStr + "<br/>"); output.newLine();
			}
			// c. Synonymous concepts:
			conceptStr = "";
			concepts = condition.getPropertyValues(synonymousConceptsProperty);
			j = null;
			for (j=concepts.iterator(); j.hasNext();)
				conceptStr += ((OWLNamedClass)j.next()).getPropertyValue(nameProperty) + ", ";
			if (conceptStr.length() > 0) {
				conceptStr = conceptStr.substring(0, conceptStr.length()-2);
				output.write("\t\t<span class=\"prop_name\">Synonymous concepts: </span> " + conceptStr + "<br/>"); output.newLine();
			}
			output.write("\t</p>"); output.newLine();
			output.write("\t</td>"); output.newLine();

			output.write("</tr>"); output.newLine();
		}
	}
	
	public static void main(String[] args)
	{
    	try {
    		if (args.length < 3) {
    			System.out.println("One or more program arguments are missing!");
    			System.out.println("USAGE: SDefOntoFormatter <inputOwlFileName> <HTMLheaderFileName> <outputFileName>");
    			return;
    		}
    		
    		String inputURI = args[0];
    		String templateURI = args[1];
    		String outputURI = args[2];

    		// Create ontology formatter object using input ontology file:
     		SDefOntoFormatter of = new SDefOntoFormatter(inputURI);
     		of.generateHtmlSummary(outputURI, templateURI);

    	} catch(Exception e){
    		e.printStackTrace();
    	}
	}
}
