package morfologik.stemming;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class to build {@link DictionaryMetadata} instances.
 */
public final class DictionaryMetadataBuilder {
    private final EnumMap<DictionaryAttribute, String> attrs 
        = new EnumMap<DictionaryAttribute, String>(DictionaryAttribute.class);

    public DictionaryMetadataBuilder separator(char c) {
        this.attrs.put(DictionaryAttribute.SEPARATOR, Character.toString(c));
        return this;
    }

    public DictionaryMetadataBuilder encoding(Charset charset) {
        return encoding(charset.name());
    }

    public DictionaryMetadataBuilder encoding(String charsetName) {
        this.attrs.put(DictionaryAttribute.ENCODING, charsetName);
        return this;
    }

    public DictionaryMetadataBuilder usePrefixes()          { return usePrefixes(true); }
    public DictionaryMetadataBuilder usePrefixes(boolean v) { this.attrs.put(DictionaryAttribute.USES_PREFIXES, Boolean.valueOf(v).toString()); return this; }

    public DictionaryMetadataBuilder useInfixes()           { return useInfixes(true); }
    public DictionaryMetadataBuilder useInfixes(boolean v)  { this.attrs.put(DictionaryAttribute.USES_INFIXES, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder ignorePunctuation()    { return ignorePunctuation(true); }
    public DictionaryMetadataBuilder ignorePunctuation(boolean v)  { this.attrs.put(DictionaryAttribute.IGNORE_PUNCTUATION, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder ignoreNumbers()        { return ignoreNumbers(true); }
    public DictionaryMetadataBuilder ignoreNumbers(boolean v)      { this.attrs.put(DictionaryAttribute.IGNORE_NUMBERS, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder ignoreCamelCase()      { return ignoreCamelCase(true); }
    public DictionaryMetadataBuilder ignoreCamelCase(boolean v)    { this.attrs.put(DictionaryAttribute.IGNORE_CAMEL_CASE, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder ignoreAllUppercase()   { return ignoreAllUppercase(true); }
    public DictionaryMetadataBuilder ignoreAllUppercase(boolean v) { this.attrs.put(DictionaryAttribute.IGNORE_ALL_UPPERCASE, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder ignoreDiacritics()     { return ignoreDiacritics(true); }
    public DictionaryMetadataBuilder ignoreDiacritics(boolean v)   { this.attrs.put(DictionaryAttribute.IGNORE_DIACRITICS, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder convertCase()          { return convertCase(true); }
    public DictionaryMetadataBuilder convertCase(boolean v)        { this.attrs.put(DictionaryAttribute.CONVERT_CASE, Boolean.valueOf(v).toString()); return this; }
    
    public DictionaryMetadataBuilder supportRunOnWords()    { return supportRunOnWords(true); }
    public DictionaryMetadataBuilder supportRunOnWords(boolean v)  { this.attrs.put(DictionaryAttribute.RUN_ON_WORDS, Boolean.valueOf(v).toString()); return this; }

    public DictionaryMetadataBuilder locale(Locale locale) {
        return locale(locale.toString());
    }

    public DictionaryMetadataBuilder locale(String localeName) {
        this.attrs.put(DictionaryAttribute.LOCALE, localeName);
        return this;
    }

    public DictionaryMetadataBuilder withReplacementPairs(Map<String, List<String>> replacementPairs) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String,List<String>> e : replacementPairs.entrySet()) {
            String k = e.getKey();
            for (String v : e.getValue()) {
                if (builder.length() > 0) builder.append(", ");
                builder.append(k).append(" ").append(v);
            }
        }
        this.attrs.put(DictionaryAttribute.REPLACEMENT_PAIRS, builder.toString());
        return this;
    }

    public DictionaryMetadataBuilder withEquivalentChars(Map<Character, List<Character>> equivalentChars) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Character,List<Character>> e : equivalentChars.entrySet()) {
            Character k = e.getKey();
            for (Character v : e.getValue()) {
                if (builder.length() > 0) builder.append(", ");
                builder.append(k).append(" ").append(v);
            }
        }
        this.attrs.put(DictionaryAttribute.EQUIVALENT_CHARS, builder.toString());
        return this;
    }

    public DictionaryMetadataBuilder author(String author) {
        this.attrs.put(DictionaryAttribute.AUTHOR, author);
        return this;
    }

    public DictionaryMetadataBuilder creationDate(String creationDate) {
        this.attrs.put(DictionaryAttribute.CREATION_DATE, creationDate);
        return this;
    }

    public DictionaryMetadataBuilder license(String license) {
        this.attrs.put(DictionaryAttribute.LICENSE, license);
        return this;
    }

    public DictionaryMetadata build() {
        return new DictionaryMetadata(attrs);
    }

    public EnumMap<DictionaryAttribute, String> toMap()    {
        return new EnumMap<DictionaryAttribute, String>(attrs); 
    }
}
