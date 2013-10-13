package com.athaydes.visualcode.util

import com.sun.javaws.exceptions.InvalidArgumentException
import org.junit.Test

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.shouldFail

/**
 *
 * User: Renato
 */
class TimeLimiterTest {

	def timeLimiter = new TimeLimiter()

	@Test( timeout = 2000L )
	void completesImmediatelyIfTaskEndsQuickly( ) {
		def startT = System.currentTimeMillis()
		def result = timeLimiter.abortAfter( { true }, 5, TimeUnit.SECONDS )
		assert System.currentTimeMillis() < startT + 1000
		assert result
	}

	@Test( timeout = 2000L )
	void abortsWithinTimeoutIfTaskTakesTooLong( ) {
		def startT = System.currentTimeMillis()
		shouldFail( TimeoutException, {
			timeLimiter.abortAfter( { sleep 5000 }, 500, TimeUnit.MILLISECONDS )
		} )
		assert System.currentTimeMillis() < startT + 1000
	}

	@Test( timeout = 2000L )
	void abortsWithinTimeoutIfTaskGoesIntoInfiniteLoop( ) {
		def startT = System.currentTimeMillis()
		shouldFail( TimeoutException, {
			timeLimiter.abortAfter( { while ( true ); }, 100, TimeUnit.MILLISECONDS )
		} )
		assert System.currentTimeMillis() < startT + 1000
	}

	@Test( timeout = 2000L )
	void waitsForTaskToCompleteWithinTimeout( ) {
		def startT = System.currentTimeMillis()
		def result = timeLimiter.abortAfter( { sleep 500; 'Hi' }, 2, TimeUnit.SECONDS )
		assert System.currentTimeMillis() > startT + 500
		assert result == 'Hi'
	}

	@Test( timeout = 2000L )
	void exceptionThrownByClosureIsRethrown( ) {
		shouldFail( InvalidArgumentException, {
			timeLimiter.abortAfter( { throw new InvalidArgumentException() }, 500, TimeUnit.MILLISECONDS )
		} )
	}

	@Test( timeout = 2000L )
	void canRunAgainAfterTimeout( ) {
		shouldFail( TimeoutException, {
			timeLimiter.abortAfter( { sleep 1000 }, 50, TimeUnit.MILLISECONDS )
		} )
		def result = timeLimiter.abortAfter( { 'Hi' }, 500, TimeUnit.MILLISECONDS )
		assert result == 'Hi'
	}

}
