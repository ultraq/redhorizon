/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.filetypes.ini;

import nz.net.ultraq.regexparser.Grammar;
import nz.net.ultraq.regexparser.Parser;
import nz.net.ultraq.regexparser.ParsingContext;
import nz.net.ultraq.regexparser.ParsingResult;
import nz.net.ultraq.regexparser.Rule;
import nz.net.ultraq.regexparser.expressions.ActionExpression;
import static nz.net.ultraq.regexparser.utilities.GrammarBuilder.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser-based INI file reader.
 *
 * @author Emanuel Rabina
 */
public class IniFileReader {

	private static final String SECTIONS        = "sections";
	private static final String CURRENT_SECTION = "current-section";

	private static final String PATTERN_COMMENT        = "[ \\t]*(?:;.*)?\\n";
	private static final String PATTERN_SECTION_HEADER = "\\[(.+?)\\]" + PATTERN_COMMENT;
	private static final String PATTERN_PROPERTY       = "(.+?)=(.*?)" + PATTERN_COMMENT;

	private final Parser parser;
	private final Grammar grammar;

	/**
	 * Constructor, sets the rules for the INI file grammar.
	 */
	IniFileReader() {

		grammar = new Grammar("INI File Grammar",
				StartingExpression(),
				Section(),
				SectionHeader(),
				Property(),
				Comment());
		parser = new Parser(grammar);
	}

	/**
	 * Parse the given content, converting it and returning the INI section and
	 * property data.
	 * 
	 * @param contents
	 * @return INI data as a map of sections containing a map of properties.
	 */
	public Map<String,Map<String,String>> read(String contents) {

		ParsingResult result = parser.parse(contents.replace("\r\n", "\n"));
		return getSections(result);
	}

// =============================================================================
// START GRAMMAR RULES
// =============================================================================

	/**
	 * Starting expression: zero or more sections.
	 * 
	 * @return Starting expression.
	 */
	private static Rule StartingExpression() {

		return Rule("S", Sequence(
				ZeroOrMore(Rulex("Comment")),
				Rulex("Section"),
				ZeroOrMore(Rulex("Comment"))));
	}

	/**
	 * Section rule: a section header, followed by zero or more properties.
	 * 
	 * @return Section rule.
	 */
	private static Rule Section() {

		return Rule("Section", Sequence(Rulex("SectionHeader"),
				ZeroOrMore(OrderedChoice(Rulex("Comment"), Rulex("Property")))));
	}

	/**
	 * Section header rule: a section name surrounded by square brackets.
	 * 
	 * @return Section header rule.
	 */
	private static Rule SectionHeader() {

		return Rule("SectionHeader", PATTERN_SECTION_HEADER, CreateSectionAction);
	}

	/**
	 * Property rule: a key/value pair, separated by an equals sign.
	 * 
	 * @return Property rule.
	 */
	private static Rule Property() {

		return Rule("Property", PATTERN_PROPERTY, CreatePropertyAction);
	}

	/**
	 * Comment rule: whitespace or any line whose first non-whitespace character
	 * is a semi-colon.
	 * 
	 * @return Comment rule.
	 */
	private static Rule Comment() {

		return Rule("Comment", PATTERN_COMMENT);
	}

// =============================================================================
// END GRAMMAR RULES
// =============================================================================

	/**
	 * Return the current section.
	 * 
	 * @param context
	 * @return The current section, or <tt>null</tt> if no section is current.
	 */
	private static IniFileSection getCurrentSection(ParsingContext context) {

		return (IniFileSection)context.get(CURRENT_SECTION);
	}

	/**
	 * Get the list of sections.
	 * 
	 * @param customvalues Custom value map, can be the parsing context or the
	 * 					   parsing result.
	 * @return List of sections.
	 */
	private static HashMap<String,Map<String,String>> getSections(Map<String,Object> customvalues) {

		@SuppressWarnings("unchecked")
		HashMap<String,Map<String,String>> sections =
				(HashMap<String,Map<String,String>>)customvalues.get(SECTIONS);
		if (sections == null) {
			sections = new HashMap<>();
			customvalues.put(SECTIONS, sections);
		}
		return sections;
	}

	/**
	 * Set the current section.
	 * 
	 * @param context
	 * @param section
	 */
	private static void setCurrentSection(ParsingContext context, IniFileSection section) {

		context.put(CURRENT_SECTION, section);
	}

	/**
	 * Create a new property in the current section.
	 */
	private static ActionExpression CreatePropertyAction = new ActionExpression() {

		@Override
		public boolean action(ParsingContext context) {

			IniFileSection section = getCurrentSection(context);
			if (section != null) {
				Pattern pattern = Pattern.compile(PATTERN_PROPERTY);
				Matcher matcher = pattern.matcher(context.getMatchToken());
				matcher.matches();
				section.put(matcher.group(1).trim(), matcher.group(2).trim());
			}
			return true;
		}
	};

	/**
	 * Action to create a new section.
	 */
	private static ActionExpression CreateSectionAction = new ActionExpression() {

		@Override
		public boolean action(ParsingContext context) {

			Pattern pattern = Pattern.compile(PATTERN_SECTION_HEADER);
			Matcher matcher = pattern.matcher(context.getMatchToken());
			matcher.matches();

			String sectionname = matcher.group(1);
			IniFileSection section = new IniFileSection(sectionname);
			getSections(context).put(sectionname, section);
			setCurrentSection(context, section);

			return true;
		}
	};
}
