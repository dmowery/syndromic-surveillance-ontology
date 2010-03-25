#!/usr/bin/perl -W

# classify.pl
# CGI script reads from tab deliminated spreadsheet to classify 
# input string with RegExps and output syndrome.  7th Dec 09 (M. Conway)

# Modified Feb 2010.  New data file added, underlining removed, and slight
# change to formatting. (MC)

use strict;
use CGI;
my $cgi = new CGI;



my $input_text =  $cgi->param('comments');

# reformat the data file so that every line begins with a regular
# expression
open(DATA, "./consensus.txt") ||
    die("Cannot open data file consensus.txt: $!\n");
my @holder; 

while (<DATA>) {
    if ($. == 1) { next; }             # ignore first line
    chomp($_);
    my @fields = split(/\t/, $_);
    my $syn_sensresp = $fields[0];     # A1
    my $syn_specresp = $fields[1];     # B2
    my $syn_sensgi = $fields[2];       # C3
    my $syn_specgi = $fields[3];       # D4
    my $syn_cons = $fields[4];         # E5
    my $syn_ILI = $fields[5];          # F6
    my $concept_name = $fields[6];     # G7
    my $subconcept_name = $fields[7];  # H8
    my $relation = $fields[8];         # I9     Relation to concept 
    my $ccc_title = $fields[15];       # P16
    my $DATA = $fields[16];            # Q17

   #  Parse the data field into its 3 constitutent parts
   #     "keyword [\bREGEX\b] [C000000000000:Term]:::" 
   my @triples = split(/:::/, $DATA);

   
    foreach my $triple (@triples) {
        my $keyword;
        my $regexp;
        my $cui;
        if ($triple =~ /^(.*?)\s+(\[.*?\])\s+(\[.*?\])/) {
            $keyword = $1;
            $regexp = $2;
            $cui    = $3;
        }#end if

	# line too long...
	# @holder contains every regular expression and associated information
	# in an array that we can loop through.  This isn't scalable, but as
	# it's only a small dataset, that's OK.
        push(@holder, "$regexp\t$keyword\t$cui\t$syn_sensresp\t$syn_specresp\t$syn_sensgi\t$syn_specgi\t$syn_cons\t$syn_ILI\t$concept_name\t$subconcept_name\t$relation\t$ccc_title\n");     
    }#end foreach
} #end outer while


my @seen_subconcepts;
my @results; 
my $result_string = "";
my $string = "";

 {
    $result_string = $input_text;
    $string = $input_text;
    #iterate through regular expressions looking for a 
    #match
    for (my $i = 0;$i < scalar(@holder);$i++) {
        my $temp = $holder[$i];
        my @h = split (/\t/, $temp);
        my $regexp = $h[0];
        # remove leading and trailing square brackets
        $regexp =~ s/^\s*\[//g;
        $regexp =~ s/\]\s*$//g;
        if ($regexp eq "") {next;}
        #manages string underlining
        # $result_string =~ s/($regexp)/<u>$1<\/u>/i; REDUNDANT

        #matching and avoiding repeats
        if ($string =~ /$regexp/igs) {
            my @temp = split(/\t/, $holder[$i]);
            my $sub = $temp[10];
            my $subconcept_flag = 0;
            foreach my $el (@seen_subconcepts) {
                if ($sub eq $el) {
                   $subconcept_flag = 1;
                } #end if
            }#end foreach
            if ($subconcept_flag == 0) {
                push(@seen_subconcepts, $sub);
                push(@results, $holder[$i]);    
            }#end if                  
        }#end if
    }#end for
} #end outer brace

# formatting results string (remove multiple tags)
#$result_string =~ s/(<u>)+/<u>/g;
#$result_string =~ s/(<\/u>)+/<\/u>/g;  REDUNDANT NOT STRING UNDERLINING ABANDONED

# Print output
print $cgi->header();
print $cgi->start_html(-title => "Classification results",
                   -style => { -src => "nformat.css" },
                   );




print "<h1>" . "Results" . "</h1>";
my $number_results = scalar(@results);
print "<p>[$number_results results]</p>"; if ($number_results == 0) {exit;}

print "<h2>" . "Input text" . "</h2>";
print "<p><i>" . $input_text . "</i></p>";

print "<h2>" . "Syndromes" . "</h2>";
 

#foreach my $result (@results) { print "$result\n"; }   
my $counter = 1;
foreach my $result (@results) {
    print "<p><b>" . "Matched concept " . $counter . "</b></p>";
    my @array = split(/\t/, $result);

     
    print "<table cellpadding=\"2\">";
    my $concept_name = $array[9];
    print "<tr>" . "<td>"  . "Concept name:" . "</td>" .   "<td>" . $concept_name . "</td>" . "</tr>";;

    my $subconcept_name = $array[10];
    print "<tr>  <td>" . " Subconcept name:" . "</td>" . "<td>" . $subconcept_name . "</td>" . "</tr>";


    my $regexp = $array[0];
    print "<tr> <td> Regular expression:</td>" . "<td><tt>$regexp</tt></td></tr>";

    my $cui = $array[2];
    print "<tr><td>UMLS CUI:</td><td> $cui</td></tr>";

    my $sensitive_resp = $array[3]; my $flag = "";
    if ($sensitive_resp =~ /1/) { $flag = "yes"} else { $flag = "no";}
    print "<tr><td>Sensitive resp. syndrome:</td><td> $flag</td></tr>"; 

    my $specific_resp = $array[4]; $flag = "";
    if ($specific_resp =~ /1/) {$flag = "yes"} else { $flag = "no"; }
    print "<tr><td>Specific resp. syndrome:</td><td>  $flag </td></tr>";


    my $sensitive_gi = $array[5];  $flag = "";
    if ($sensitive_gi =~ /1/) {$flag = "yes"} else { $flag = "no"; }
        print "<tr><td>Sensitive GI syndrome:</td><td> $flag</td></tr>";

    my $specific_gi = $array[6];  $flag = "";
    if ($specific_gi =~ /1/) {$flag = "yes"} else { $flag = "no"; }
            print "<tr><td>Specific GI syndrome:</td><td>  $flag</td></tr>";

    my $const = $array[7];  $flag = "";
    if ($const =~ /1/) {$flag = "yes"} else { $flag = "no"; }
            print "<tr><td>Constitutional syndrome:</td><td>$flag</td></tr>";;

    my $ILI = $array[8];  $flag = "";
    if ($ILI =~ /1/) {$flag = "yes"} else { $flag = "no"; }
                print "<tr><td>ILI syndrome:</td><td> $flag</td></tr>";;
    
    my $CCC_title = $array[12];
    print "<tr><td>CCC title:</td><td>$CCC_title</td></tr>";
   print "</table>";
    
    $counter++;
}

my $num = scalar(@results);
if ($num == 0) {print "<b>No results</b>";}
print "<p></p>";
print "<p></p>";
print "<p></p>";
print "<hr />";
print "<i>classify.pl. ";
print scalar(localtime(time));
print ". Contact: <a href=\"mailto:conwaym\@pitt.edu\">Mike Conway</a>\n";
print "</body>";
