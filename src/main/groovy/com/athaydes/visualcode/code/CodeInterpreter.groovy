package com.athaydes.visualcode.code

import com.athaydes.visualcode.util.TimeLimiter
import groovy.transform.Immutable

import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class CodeInterpreter {

	TimeLimiter timeLimiter = new TimeLimiter()

	List<CodeResult> read( String code ) {
		def result = [ ]
		def lines = eachLine( code )
		def evaluatedCode = ''
		for ( i in 0..<lines.size() ) {
			def lineExpressions = lines[ i ]
			def results = readWhileValid( [ evaluatedCode ] + lineExpressions )
			result << unifyResults( results.tail() )
			if ( !results.last().success ) break
			evaluatedCode += ';' + lineExpressions.join( ';' )
		}
		result.asImmutable()
	}

	private List<CodeResult> readWhileValid( List<String> expressions ) {
		def result = [ ]
		for ( i in 0..<expressions.size() ) {
			def exprResult = readExpression( expressions[ 0..i ].join( ';' ) )
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
		code.split( /\n/ ).collect { ( it.split( /;/ )*.trim() ).grep { !it.empty } }
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
