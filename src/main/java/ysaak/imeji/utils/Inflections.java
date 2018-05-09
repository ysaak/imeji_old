package ysaak.imeji.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Inflections {
	private Inflections() {
	}

	private static final List<String> UNCOUNTABLE_LIST = new LinkedList<>();
	private static final List<Rule> PLURAL_RULE_LIST = new LinkedList<>();
	private static final List<Rule> SINGULAR_RULE_LIST = new LinkedList<>();

	public static String plurialize(String word) {
		if (word == null) {
			return null;
		}

		if (word.length() == 0) {
			return word;
		}

		if (isUncountable(word)) {
			return word;
		}

		for (Rule rule : PLURAL_RULE_LIST) {
			String result = rule.apply(word);
			if (result != null) {
				return result;
			}
		}

		return word;
	}

	public static String underscore(String camelCaseWord, char... delimiterChars) {
		if (camelCaseWord == null) {
			return null;
		}

		String result = camelCaseWord.trim();

		if (result.length() == 0) {
			return "";
		}
		result = result.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
		result = result.replaceAll("([a-z\\d])([A-Z])", "$1_$2");
		result = result.replace('-', '_');
		if (delimiterChars != null) {
			for (char delimiterChar : delimiterChars) {
				result = result.replace(delimiterChar, '_');
			}
		}
		return result.toLowerCase();
	}

	/**
	 * Determine whether the supplied word is considered uncountable by the pluralize and
	 * singularize methods.
	 *
	 * @param word the word
	 * @return true if the plural and singular forms of the word are the same
	 */
	public static boolean isUncountable(String word) {
		return word != null && UNCOUNTABLE_LIST.contains(word.trim().toLowerCase());
	}

	/* --------------------------------------------- */

	private static void addPluralize(String rule, String replacement) {
		PLURAL_RULE_LIST.add(new Rule(rule, replacement));
	}

	private static void addSingularize(String rule, String replacement) {
		SINGULAR_RULE_LIST.add(new Rule(rule, replacement));
	}

	private static void addIrregular(String singular, String plural) {
		String singularRemainder = singular.length() > 1 ? singular.substring(1) : "";
		String pluralRemainder = plural.length() > 1 ? plural.substring(1) : "";

		addPluralize("(" + singular.charAt(0) + ")" + singularRemainder + "$", "$1" + pluralRemainder);
		addSingularize("(" + plural.charAt(0) + ")" + pluralRemainder + "$", "$1" + singularRemainder);
	}

	private static void addUncountable(String... words) {
		if (words == null || words.length == 0) return;
		for (String word : words) {
			if (word != null) {
				UNCOUNTABLE_LIST.add(word.trim().toLowerCase());
			}
		}
	}

	static {
		addPluralize("$", "s");
		addPluralize("s$", "s");
		addPluralize("(ax|test)is$", "$1es");
		addPluralize("(octop|vir)us$", "$1i");
		addPluralize("(octop|vir)i$", "$1i"); // already plural
		addPluralize("(alias|status)$", "$1es");
		addPluralize("(bu)s$", "$1ses");
		addPluralize("(buffal|tomat)o$", "$1oes");
		addPluralize("([ti])um$", "$1a");
		addPluralize("([ti])a$", "$1a"); // already plural
		addPluralize("sis$", "ses");
		addPluralize("(?:([^f])fe|([lr])f)$", "$1$2ves");
		addPluralize("(hive)$", "$1s");
		addPluralize("([^aeiouy]|qu)y$", "$1ies");
		addPluralize("(x|ch|ss|sh)$", "$1es");
		addPluralize("(matr|vert|ind)ix|ex$", "$1ices");
		addPluralize("([m|l])ouse$", "$1ice");
		addPluralize("([m|l])ice$", "$1ice");
		addPluralize("^(ox)$", "$1en");
		addPluralize("(quiz)$", "$1zes");
		// Need to check for the following words that are already pluralized:
		addPluralize("(people|men|children|sexes|moves|stadiums)$", "$1"); // irregulars
		addPluralize("(oxen|octopi|viri|aliases|quizzes)$", "$1"); // special rules

		addSingularize("s$", "");
		addSingularize("(s|si|u)s$", "$1s"); // '-us' and '-ss' are already singular
		addSingularize("(n)ews$", "$1ews");
		addSingularize("([ti])a$", "$1um");
		addSingularize("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis");
		addSingularize("(^analy)ses$", "$1sis");
		addSingularize("(^analy)sis$", "$1sis"); // already singular, but ends in 's'
		addSingularize("([^f])ves$", "$1fe");
		addSingularize("(hive)s$", "$1");
		addSingularize("(tive)s$", "$1");
		addSingularize("([lr])ves$", "$1f");
		addSingularize("([^aeiouy]|qu)ies$", "$1y");
		addSingularize("(s)eries$", "$1eries");
		addSingularize("(m)ovies$", "$1ovie");
		addSingularize("(x|ch|ss|sh)es$", "$1");
		addSingularize("([m|l])ice$", "$1ouse");
		addSingularize("(bus)es$", "$1");
		addSingularize("(o)es$", "$1");
		addSingularize("(shoe)s$", "$1");
		addSingularize("(cris|ax|test)is$", "$1is"); // already singular, but ends in 's'
		addSingularize("(cris|ax|test)es$", "$1is");
		addSingularize("(octop|vir)i$", "$1us");
		addSingularize("(octop|vir)us$", "$1us"); // already singular, but ends in 's'
		addSingularize("(alias|status)es$", "$1");
		addSingularize("(alias|status)$", "$1"); // already singular, but ends in 's'
		addSingularize("^(ox)en", "$1");
		addSingularize("(vert|ind)ices$", "$1ex");
		addSingularize("(matr)ices$", "$1ix");
		addSingularize("(quiz)zes$", "$1");

		addIrregular("person", "people");
		addIrregular("man", "men");
		addIrregular("child", "children");
		addIrregular("sex", "sexes");
		addIrregular("move", "moves");
		addIrregular("stadium", "stadiums");

		addUncountable("equipment", "information", "rice", "money", "species", "series", "fish", "sheep");
	}

	private static class Rule {
		private final String expression;
		private final Pattern expressionPattern;
		private final String replacement;

		private Rule(final String expression, final String replacement) {
			this.expression = expression;
			this.replacement = replacement != null ? replacement : "";
			this.expressionPattern = Pattern.compile(this.expression, Pattern.CASE_INSENSITIVE);
		}

		private String apply( String input ) {
			Matcher matcher = this.expressionPattern.matcher(input);
			if (!matcher.find()) return null;
			return matcher.replaceAll(this.replacement);
		}
	}
}
