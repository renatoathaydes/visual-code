package com.athaydes.visualcode.code

import spock.lang.Specification

/**
 *
 * User: Renato
 */
class CodeInterpreterSpec extends Specification {

	def interpreter = new CodeInterpreter()

	def "Should be able to interpret simple expressions"( ) {
		when:
		def result = interpreter.read example

		then:
		result == [ new CodeResult( expected, success ) ]

		where:
		example                        | expected                             | success
		''                             | ''                                   | true
		'2 + 1'                        | '3'                                  | true
		'"Hello world!"'               | 'Hello world!'                       | true
		'"1" + "2"'                    | '12'                                 | true
		'return 1'                     | '1'                                  | true
		'return "Hi"'                  | 'Hi'                                 | true
		'x = 1'                        | '1'                                  | true
		'int x = 1;'                   | '1'                                  | true
		'String s = "hi";'             | 'hi'                                 | true
		'int m = 2 + 4;'               | '6'                                  | true
		'm + n'                        | 'MissingPropertyException'           | false
		'a'                            | 'MissingPropertyException'           | false
		'x = a'                        | 'MissingPropertyException'           | false
		'int x = a'                    | 'MissingPropertyException'           | false
		'@#$@#'                        | 'MultipleCompilationErrorsException' | false
		'throw new RuntimeException()' | 'RuntimeException'                   | false
	}

	def "Should be able to split code lines correctly"( ) {
		when:
		def result = interpreter.eachLine( example )

		then:
		result == expected

		where:
		example                   | expected
		'1'                       | [ [ '1' ] ]
		'1 + 1'                   | [ [ '1 + 1' ] ]
		'2;3'                     | [ [ '2', '3' ] ]
		'2;3;  '                  | [ [ '2', '3' ] ]
		'int x = 0;\n int y = 1;' | [ [ 'int x = 0' ], [ 'int y = 1' ] ]
		'x=0;y=1;z=2'             | [ [ 'x=0', 'y=1', 'z=2' ] ]
		'x=0\n '                  | [ [ 'x=0' ], [ ] ]
		'x=0;\ny=1;\nz=2;'        | [ [ 'x=0' ], [ 'y=1' ], [ 'z=2' ] ]
	}

	def "Should unify all results found in a single line using a '|' to separate results "( ) {
		when:
		def result = interpreter.unifyResults( examples.collect { it as CodeResult } )

		then:
		result == expected as CodeResult

		where:
		examples                          | expected
		[ [ '1', true ] ]                 | [ '1', true ]
		[ [ '1', true ], [ '2', true ] ]  | [ '1 | 2', true ]
		[ [ 'a', false ] ]                | [ 'a', false ]
		[ [ 'a', true ], [ 'b', false ] ] | [ 'a | b', false ]
	}

	def "Should interpret each line in sequence, stopping if an error occurs"( ) {
		when:
		def result = interpreter.read( example )

		then:
		result == ( 0..<expected.size() ).collect { i ->
			new CodeResult( expected[ i ], success[ i ] )
		}

		where:
		example                   | expected                            | success
		'1;\n2;'                  | [ '1', '2' ]                        | [ true, true ]
		'x = 4;y = 10;'           | [ '4 | 10' ]                        | [ true ]
		'x = 4;\ny = 10;\nz = 2;' | [ '4', '10', '2' ]                  | [ true, true, true ]
		'1;\na'                   | [ '1', 'MissingPropertyException' ] | [ true, false ]
		'a;\n1'                   | [ 'MissingPropertyException' ]      | [ false ]

	}

	def "Should remember variables defined in previous lines"( ) {
		when:
		def result = interpreter.read( example )

		then:
		result == ( 0..<expected.size() ).collect { i ->
			new CodeResult( expected[ i ], success[ i ] )
		}

		where:
		example                           | expected          | success
		'x = 5;\ny = x + 1'               | [ '5', '6' ]      | [ true, true ]
		'x = 5; y = x + 1'                | [ '5 | 6' ]       | [ true ]
		'int m = 2;\nint n = 4;\n m + n;' | [ '2', '4', '6' ] | [ true, true, true ]
		'int m = 2;int n = 4; m + n;'     | [ '2 | 4 | 6' ]   | [ true ]
	}

	def "Should understand multiple-line conditional code"( ) {

	}

}
