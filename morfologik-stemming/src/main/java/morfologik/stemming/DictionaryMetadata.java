package morfologik.stemming;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static morfologik.stemming.DictionaryAttribute.*;

/**
 * Description of attributes, their types and default values.
 * 
 * @see Dictionary
 */
public final class DictionaryMetadata {
    /**
     * Default attribute values.
     */
    private static Map<DictionaryAttribute, String> DEFAULT_ATTRIBUTES = new DictionaryMetadataBuilder()
        .separator('+')
        .ignorePunctuation()
        .ignoreNumbers()
        .ignoreCamelCase()
        .ignoreAllUppercase()
        .ignoreDiacritics()
        .convertCase()
        .supportRunOnWords()
        .useInfixes(false)
        .usePrefixes(false)
        .toMap();

    /**
     * Required attributes.
     */
    private static EnumSet<DictionaryAttribute> REQUIRED_ATTRIBUTES = EnumSet.of(
        SEPARATOR,
        ENCODING);

	/**
	 * A separator character between fields (stem, lemma, form). The character
	 * must be within byte range (FSA uses bytes internally).
	 */
	private byte separator;
    private char separatorChar;

	/**
	 * Encoding used for converting bytes to characters and vice versa.
	 */
	private String encoding;

	private Charset charset;
    private Locale locale = Locale.getDefault();

    private boolean usesPrefixes;
    private boolean usesInfixes;
    
    /**
     * Replacement pairs for non-obvious candidate search in a speller dictionary.
     */
    private Map<String, List<String>> replacementPairs = Collections.emptyMap();

    /**
     * Equivalent characters (treated similarly as equivalent chars with and without
     * diacritics). For example, Polish <tt>ł</tt> can be specified as equivalent to <tt>l</tt>.
     * 
     * This implements a feature similar to hunspell MAP in the affix file.
     */
    private Map<Character, List<Character>> equivalentChars = Collections.emptyMap();

	/**
	 * All attributes.
	 */
    private final EnumMap<DictionaryAttribute, String> attributes;

    /**
     * All "enabled" boolean attributes.
     */
    private final EnumMap<DictionaryAttribute,Boolean> boolAttributes;

	/**
	 * Return all attributes.
	 */
    public Map<DictionaryAttribute, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

	// Cached attrs.
    public String getEncoding()      { return encoding; }
    public byte getSeparator()       { return separator; }
    public boolean isUsingPrefixes() { return usesPrefixes; }
    public boolean isUsingInfixes()  { return usesInfixes; }
    public Locale getLocale()        { return locale; }

    public Map<String, List<String>> getReplacementPairs() { return replacementPairs; }
    public Map<Character, List<Character>> getEquivalentChars() { return equivalentChars; }

    // Dynamically fetched.
    public boolean isIgnoringPunctuation()  { return boolAttributes.get(IGNORE_PUNCTUATION); }
    public boolean isIgnoringNumbers()      { return boolAttributes.get(IGNORE_NUMBERS); }
    public boolean isIgnoringCamelCase()    { return boolAttributes.get(IGNORE_CAMEL_CASE); }
    public boolean isIgnoringAllUppercase() { return boolAttributes.get(IGNORE_ALL_UPPERCASE); }
    public boolean isIgnoringDiacritics()   { return boolAttributes.get(IGNORE_DIACRITICS); }
    public boolean isConvertingCase()       { return boolAttributes.get(CONVERT_CASE); }
    public boolean isSupportingRunOnWords() { return boolAttributes.get(RUN_ON_WORDS); }

    /**
     * Create an instance from an attribute map.
     * 
     * @see DictionaryMetadataBuilder
     */
    public DictionaryMetadata(Map<DictionaryAttribute, String> userAttrs) {
        this.boolAttributes = new EnumMap<DictionaryAttribute,Boolean>(DictionaryAttribute.class);
        this.attributes = new EnumMap<DictionaryAttribute, String>(DictionaryAttribute.class);
        this.attributes.putAll(userAttrs);

        EnumMap<DictionaryAttribute, String> attrs = new EnumMap<DictionaryAttribute, String>(DEFAULT_ATTRIBUTES);
        attrs.putAll(userAttrs);

        // Convert some attrs from the map to local fields for performance reasons.
	    EnumSet<DictionaryAttribute> requiredAttributes = EnumSet.copyOf(REQUIRED_ATTRIBUTES);

	    for (Map.Entry<DictionaryAttribute,String> e : attrs.entrySet()) {
	        requiredAttributes.remove(e.getKey());

	        // Run validation and conversion on all of them.
	        Object value = e.getKey().fromString(e.getValue());
	        switch (e.getKey()) {
	            case ENCODING:
	                this.encoding = e.getValue();
	                if (!Charset.isSupported(encoding)) {
	                    throw new IllegalArgumentException("Encoding not supported on this JVM: "
	                            + encoding);
	                }
	                this.charset = (Charset) value;
	                break;

	            case SEPARATOR:
	                this.separatorChar = (Character) value; 
	                break;

	            case LOCALE:
	                this.locale = (Locale) value;
	                break;
	                
	            case REPLACEMENT_PAIRS:
                    {
                        @SuppressWarnings("unchecked")
	                    Map<String, List<String>> gvalue = (Map<String, List<String>>) value;
	                    this.replacementPairs = gvalue; 
                    }
	                break;

	            case EQUIVALENT_CHARS:
                    {
                        @SuppressWarnings("unchecked")
                        Map<Character, List<Character>> gvalue = (Map<Character, List<Character>>) value;
                        this.equivalentChars = gvalue; 
                    }
                    break;

	            case USES_INFIXES:
                case USES_PREFIXES:
	            case IGNORE_PUNCTUATION:
	            case IGNORE_NUMBERS:
	            case IGNORE_CAMEL_CASE:
	            case IGNORE_ALL_UPPERCASE:
	            case IGNORE_DIACRITICS:
	            case CONVERT_CASE:
	            case RUN_ON_WORDS:
                    this.boolAttributes.put(e.getKey(), (Boolean) value);
	                break;

	            case AUTHOR:
	            case LICENSE:
	            case CREATION_DATE:
                    // Just run validation.
                    e.getKey().fromString(e.getValue());
	                break;

                default:
                    throw new RuntimeException("Unexpected code path (attribute should be handled but is not): " + e.getKey());
	        }
	    }

        if (!requiredAttributes.isEmpty()) {
            throw new IllegalArgumentException("At least one the required attributes was not provided: "
                    + requiredAttributes.toString());
        }

	    // Cache these for performance reasons.
        this.usesInfixes = boolAttributes.get(USES_INFIXES);
        this.usesPrefixes = boolAttributes.get(USES_PREFIXES);

        // Sanity check.
	    CharsetEncoder encoder = getEncoder();
	    try {
            ByteBuffer encoded = encoder.encode(CharBuffer.wrap(new char [] { separatorChar }));
            if (encoded.remaining() > 1) {
                throw new IllegalArgumentException("Separator character is not a single byte in encoding "
                        + encoding + ": " + separatorChar);
            }
            this.separator = encoded.get();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Separator character cannot be converted to a byte in "
                    + encoding + ": " + separatorChar, e);
        }
    }

    /**
	 * Returns a new {@link CharsetDecoder} for the {@link #encoding}.
	 */
	public CharsetDecoder getDecoder() {
        try {
            return charset.newDecoder().onMalformedInput(
                    CodingErrorAction.REPORT).onUnmappableCharacter(
                    CodingErrorAction.REPORT);
        } catch (UnsupportedCharsetException e) {
            throw new RuntimeException(
                    "FSA's encoding charset is not supported: " + encoding);
        }
	}

	/**
     * Returns a new {@link CharsetEncoder} for the {@link #encoding}.
     */
    public CharsetEncoder getEncoder() {
        try {
            return charset.newEncoder();
        } catch (UnsupportedCharsetException e) {
            throw new RuntimeException(
                    "FSA's encoding charset is not supported: " + encoding);
        }
    }

	/**
	 * Returns the {@link #separator} byte converted to a single <code>char</code>. Throws
	 * a {@link RuntimeException} if this conversion is for some reason impossible
	 * (the byte is a surrogate pair, FSA's {@link #encoding} is not available). 
	 */
    public char getSeparatorAsChar() {
        return separatorChar;
    }
}
