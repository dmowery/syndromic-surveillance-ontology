22 Apr, 2010

-----------------------------------------------------------------------------------
 COMPILING
-----------------------------------------------------------------------------------

   Create a new java project in Eclipse using the existing source. The root folder
   to specify in the new project wizard should be the parent folder (ontobuilder), 
   and not src. This way the wizard would automatically configure the build path
   with all the necessary libraries (protege and other) contained in lib sub-folder. 

-----------------------------------------------------------------------------------
 RUNNING
-----------------------------------------------------------------------------------

1) OntoWriter

   Main class for run configuration in Eclipse is SDefMain.java.
   To run the program, you need to supply 3 command-line arguments: 
   - inputFileName (csv)
   - templateOntologyPath
   - outputOntologyPath

   Please note, that the last 2 arguments have to be absolute paths in the URI format. 
   Here is how the arguments look on my computer (in Eclipse), for example:
   - "D:\Anuta\Work\SyndromeDefinitions\consensus.csv"
   - "file:///D:/Anuta/Work/SyndromeDefinitions/onto/SDTemplate.owl"
   - "file:///D:/Anuta/Work/SyndromeDefinitions/onto/result.owl"

2) OntoFormatter

   Main class is SDefOntoFormatter.java. Also uses 3 command-line arguments:
   - ontologyPath (owl fil, the path should be absolute, in URI format)
   - htmlHeaderPath
   - htmlOutputPath

  The HTML header file can be found in ../input folder.

-----------------------------------------------------------------------------------
 KNOWN ISSUES:
-----------------------------------------------------------------------------------
* The OntoWriter Cannot correctly process the ’ character in the input. 
  Have to make sure ' is used instead. 
