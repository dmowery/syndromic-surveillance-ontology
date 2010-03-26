public interface Constants {
	
	public static final String METACLASS_CONCEPT = "ConceptMetaClass";
	public static final String METACLASS_CONDITION = "ConditionMetaClass";
	public static final String METACLASS_SYNDROME = "SyndromeMetaClass";
	public static final String METACLASS_CODE = "CodingMetaClass";
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
	
	
	// Column order in the input spreadsheet:
	public static int SENS_RESPIRATORY_DEF_INDEX = 0;
	public static int SPEC_RESPIRATORY_DEF_INDEX = 1;
	public static int SENS_GI_DEF_INDEX = 2;
	public static int SPEC_GI_DEF_INDEX = 3;
	public static int CONSTITUTIONAL_DEF_INDEX = 4;
	public static int ILI_DEF_INDEX = 5;
	public static int CONDITION_INDEX = 6;
	public static int CONCEPT_INDEX = 7;
	public static int RELATION_INDEX = 8;
	public static int INCLUSION_KEYWORDS_REGEX_INDEX = 9;
	public static int EXCLUSION_KEYWORDS_REGEX_INDEX = 10;
	public static int CCC_ID_INDEX = 14;
	public static int CCC_TITLE_INDEX = 15;
	public static int INCLUSION_KEYWORDS_OLD_INDEX = 16;
	
	// Syndrome names:
	public static String RESPIRATORY_SYNDROME_NAME = "RespiratorySyndrome";
	public static String GI_SYNDROME_NAME = "GastroIntestinalSyndrome";
	public static String CONSTITUTIONAL_SYNDROME_NAME = "ConstitutionalSyndrome";
	public static String INFLUENZA_SYNDROME_NAME = "InfluenzaLikeIllnessSyndrome";
	
	// Coding systems:
	public static String CODING_CCC = "CCC";
	public static String CODING_MESH = "MeSH";
	public static String CODING_SNOMED = "SNOMED";
	public static String CODING_ICD9 = "ICD_9";
	public static String CODING_UMLS = "UMLS";
	
}