package com.athaydes.visualcode.code

import spock.lang.Specification

import static com.athaydes.visualcode.code.StatementType.IF

/**
 *
 * User: Renato
 */
class StatementTypeSpec extends Specification {

	def "IF statements should be recognized"( ) {
		given:
		def matcher = IF.pattern.matcher( example )

		expect:
		matcher.matches() == matches
		!group1 || group1 == matcher.group( 1 )
		!group2 || group2 == matcher.group( 2 )

		where:
		example | matches | group1 | group2
		''           | false | ''      | ''
		'if'         | false | ''      | ''
		'if ()'      | false | ''      | ''
		'if (a)'     | true  | 'a'     | ''
		'if ("b") {' | true  | '"b"'   | ''
		'if (a > b)' | true  | 'a > b' | ''

	}

}
