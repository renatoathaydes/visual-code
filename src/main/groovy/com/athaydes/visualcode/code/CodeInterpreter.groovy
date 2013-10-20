package com.athaydes.visualcode.code

import com.athaydes.visualcode.util.TimeLimiter
import groovy.transform.Immutable

import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * User: Renato
 */
class CodeInterpreter {

	TimeLimiter timeLimiter = new TimeLimiter()

	def comp( String code ) {
		def binding = new Binding()
		def shell = new GroovyShell( binding )
		def res = shell.evaluate( code )
		println binding.variables
		res
	}

	List<CodeResult> read( String code ) {
		def result = [ ]
		def linesByType = eachLine( code )
		def evaluatedCode = ''
		for ( entry in linesByType ) {
			//println "Reading line $entry"
			def lineExpressions = entry.values().flatten()
			//println "Line expressions: $lineExpressions"
			def results = readWhileValid( [ evaluatedCode ] + lineExpressions )
			result << unifyResults( results.tail() )
			if ( !results.last().success ) break
			evaluatedCode += ';' + lineExpressions.join( ';' )
		}
		result.asImmutable()
	}

	private List<CodeResult> readWhileValid( expressions ) {
		def result = [ ]
		for ( i in 0..<expressions.size() ) {
			def exprResult = readExpression( expressions[ 0..i ].join( '\n' ) )
			result << exprResult
			if ( !exprResult.success ) break
		}
		result
	}

	protected CodeResult readExpression( String line ) {
		try {
			String result = timeLimiter.abortAfter( {
				( Eval.me( line ) as String ) ?: ''
			}, 800, TimeUnit.MILLISECONDS )
			new CodeResult( result, true )
		} catch ( Throwable e ) {
			new CodeResult( e.class.simpleName, false )
		}
	}

	protected eachLine( String code ) {
		code.split( /\n/ ).collect {
			splitLine( it )
		}
	}

	private splitLine( String line ) {
		Matcher matcher = null
		def statementType = StatementType.values().find { type ->
			matcher = type.pattern.matcher( line )
			matcher.matches()
		}
		[ ( statementType ): matcher.group( 1 ).split( /;/ )*.trim().grep { !it.empty } ]
	}

	protected CodeResult unifyResults( List<CodeResult> results ) {
		new CodeResult( results.collect { it.result }.join( ' | ' ),
				results.empty ? true : results.last().success )
	}

}

@Immutable
class CodeResult {

	String result
	boolean success

}

enum StatementType {

	FOR( /\s*;?\s*for\s*\((.+)\)\s*\{?\s*/ ),
	WHILE( /\s*;?\s*while\s*\((.+)\)\s*\{?\s*/ ),
	IF( /\s*;?\s*if\s*\((.+)\)\s*\{?\s*/ ),
	EXPR( /(.*)/ )

	final Pattern pattern

	private StatementType( String regex ) {
		this.pattern = Pattern.compile( regex )
	}

}
