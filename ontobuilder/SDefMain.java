import java.io.*;
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

    		// Open input file for reading:
    		BufferedReader input = new BufferedReader(new FileReader(inputURI));
    		// Create ontology writer object using ontology template file:
    		SDefOntologyWriter writer = new SDefOntologyWriter(templateURI);
    		
    		// Read input line-by-line and feed each line to ontology writer:
    		String line = null;
    		line = input.readLine();
    		while ((line = input.readLine()) != null){
    			writer.translateInputLine(line);
    		}

    		// Save populated ontology to a destination owl file:
    		writer.saveOntology(new URI(destinationURI));
    		input.close();

    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }	
}