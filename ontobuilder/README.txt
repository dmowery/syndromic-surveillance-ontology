07 Apr, 2010

Copied from Anya's email to Mike dated October 2009. May need to update!
-----------------------------------------------------------------------------------

To make it work you'll need to follow several steps:

1) Create a new java project in Eclipse using the existing source 

2) Add Protege libraries. For that you can go to Project menu >> Properties, 
   and select Build Path and then Libraries tab. You need to add ALL jar files 
   from your Protege installation. 

3) Build your project - it should compile without errors IF all the necessary 
   libraries were added to the build path.

4) To run the program, you need to supply 3 command-line arguments: 
   - inputFileName
   - templateOntologyPath
   - outputOntologyPath

   Please note, that the last 2 arguments have to be absolute paths in the URI format. 
   Here is how the arguments look on my computer (in Eclipse), for example:
   - "D:\Anuta\Work\SyndromeDefinitions\ClinicalConcepts.txt"
   - "file:///D:/Anuta/Work/SyndromeDefinitions/onto/SDTemplate.owl"
   - "file:///D:/Anuta/Work/SyndromeDefinitions/onto/result.owl"
