import java.net.URI;

public class SDefMain {
	
	public SDefMain(String infileName){
		
	}
	
    public static void main(String[] args) {
    	try {
    		if (args.length < 3) {
    			System.out.println("One or more program arguments are missing!");
    			System.out.println("USAGE: SDefMain <inputFileName> <ontologyTemplateURI> <resultOntologyURI>");
    			return;
    		}
    		
    		String inputURI = args[0];
    		String templateURI = args[1];
    		String destinationURI = args[2];

    		// Create ontology writer object using ontology template file:
    		SDefOntologyWriter writer = new SDefOntologyWriter(templateURI);

    		// Process input file:
    		writer.processCsvInput(inputURI);

    		// Save populated ontology to a destination owl file:
    		writer.saveOntology(new URI(destinationURI));

    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }	
}