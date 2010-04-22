public interface Constants {
	
	public static final String METACLASS_CONCEPT = "ConceptMetaClass";
	public static final String METACLASS_CONDITION = "ConditionMetaClass";
	public static final String METACLASS_SYNDROME = "SyndromeMetaClass";
	public static final String CLASS_CONCEPT = "ClinicalConcept";
	public static final String CLASS_CONDITION = "ClinicalCondition";
	public static final String CLASS_SYNDROME = "Syndrome";
	public static final String CLASS_CODE = "Coding";
	public static final String CLASS_KEYWORD = "Keyword";
	public static final String CLASS_REGEX = "RegularExpression";
	public static final String PROPERTY_HAS_SENSITIVE_DEFINITION = "hasSensitiveDefinition";
	public static final String PROPERTY_HAS_SPECIFIC_DEFINITION = "hasSpecificDefinition";
	public static final String PROPERTY_HAS_EXACT_CONCEPT = "hasExactConcept";
	public static final String PROPERTY_HAS_SYNONYMOUS_CONCEPTS = "hasSynonymousConcepts";
	public static final String PROPERTY_HAS_RELATED_CONCEPTS = "hasRelatedConcepts";
	public static final String PROPERTY_HAS_EXTERNAL_CODES = "hasExternalCodings";
	public static final String PROPERTY_HAS_INCLUSION_KEYWORDS = "hasInclusionKeywords";
	public static final String PROPERTY_HAS_EXCLUSION_KEYWORDS = "hasExclusionKeywords";
	public static final String PROPERTY_MATCHES_CONCEPT = "matchesConcept";
	public static final String PROPERTY_MAPS_TO_CODE = "mapsToCode";
	public static final String PROPERTY_MATCHES_KEYWORDS = "matchesKeywords";
	public static final String PROPERTY_IS_MATCHED_BY_REGEX = "isMatchedByRegEx";
	public static final String PROPERTY_IS_SYNONYMOUS_CONCEPT = "isSynonymousTo";
	public static final String PROPERTY_IS_RELATED_CONCEPT = "isRelatedTo";
	public static final String PROPERTY_IS_EXACT_CONCEPT = "isExactConceptFor";
	public static final String PROPERTY_IS_IN_SENSITIVE_DEFINITION = "isInSensitiveDefinitionOf";
	public static final String PROPERTY_IS_IN_SPECIFIC_DEFINITION = "isInSpecificDefinitionOf";
	public static final String PROPERTY_HAS_NAME = "hasName";
	public static final String PROPERTY_HAS_STRING = "hasString";
	public static final String PROPERTY_HAS_REGEX_STRING = "hasRegExString";
	public static final String PROPERTY_CODE_TITLE = "codeTitle";
	public static final String PROPERTY_CODE_ID = "codeID";
	public static final String PROPERTY_CODESYSTEM_NAME = "codingSystemName";
	
	// Column names in the input CSV file:
	public static String SENS_RESPIRATORY_DEF_COLUMN = "Sensitive Respiratory Syndrome from CC";
	public static String SPEC_RESPIRATORY_DEF_COLUMN = "Specific Respiratory Syndrome from CC";
	public static String SENS_GI_DEF_COLUMN = "Sensitive GI Syndrome from CC";
	public static String SPEC_GI_DEF_COLUMN = "Specific GI Syndrome from CC";
	public static String CONSTITUTIONAL_DEF_COLUMN = "Constitutional Syndrome from CC";
	public static String ILI_DEF_COLUMN = "Influenza-like Syndrome from CC";
	public static String CONDITION_COLUMN = "Concept Name";
	public static String CONCEPT_COLUMN = "Sub-concept Name";
	public static String RELATION_COLUMN = "Relation to Concept Name (Concept name, Related concept, Synonym)";
	public static String INCLUSION_KEYWORDS_REGEX_COLUMN = "keywords, regular expressions and CUIS";
	public static String EXCLUSION_KEYWORDS_REGEX_COLUMN = "Exclusion Keywords and Patterns";
	public static String CCC_ID_COLUMN = "CCC_ComplaintID";
	public static String CCC_TITLE_COLUMN = "CCC_Title";
	public static String MESH_CODE_COLUMN = "MeSH Code";
	public static String SNOWMED_CODE_COLUMN = "SNOMED Code";
	public static String ICD9_CODE_COLUMN = "ICD-9 Code";
	public static String ICD10_CODE_COLUMN = "ICD-10 Code";
	
	// Syndrome names:
	public static String RESPIRATORY_SYNDROME_NAME = "RespiratorySyndrome";
	public static String GI_SYNDROME_NAME = "GastroIntestinalSyndrome";
	public static String CONSTITUTIONAL_SYNDROME_NAME = "ConstitutionalSyndrome";
	public static String INFLUENZA_SYNDROME_NAME = "InfluenzaLikeIllnessSyndrome";
	
	// Coding systems:
	public static String CODING_CCC = "CCC";
	public static String CODING_MESH = "MeSH";
	public static String CODING_SNOMED = "SNOMED";
	public static String CODING_UMLS = "UMLS";
	public static String CODING_ICD9 = "ICD_9";
	public static String CODING_ICD10 = "ICD_10";
	
}