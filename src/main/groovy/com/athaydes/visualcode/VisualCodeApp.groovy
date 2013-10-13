package com.athaydes.visualcode

import com.athaydes.visualcode.code.CodeInterpreter
import com.athaydes.visualcode.code.CodeResult
import groovyx.javafx.beans.FXBindable
import javafx.scene.control.TextArea

import static groovyx.javafx.GroovyFX.start

/**
 *
 * User: Renato
 */
class VisualCodeApp {

	def interpreter = new CodeInterpreter()

	TextArea codeArea

	@FXBindable
	String codeResultText

	def updateCodeResult = {
		if ( codeArea ) {
			def codeResults = interpreter.read( codeArea.text )
			styleCodeArea( codeResults && codeResults.last().success )
			codeResultText = resultAsText( codeResults )
		}

	} as TimerTask

	String resultAsText( List<CodeResult> codeResultList ) {
		def resultLines = codeResultList.collect { it.result }
		def codeLines = codeArea.text.split( /\n/ )
		showEmptyResultForEmptyCodeLines( codeLines, resultLines )
		resultLines.join( '\n' )
	}

	private void showEmptyResultForEmptyCodeLines( String[] codeLines, resultLines ) {
		( 0..<( Math.min( codeLines.size(), resultLines.size() ) ) ).each {
			if ( codeLines[ it ].trim().isEmpty() ) resultLines[ it ] = ''
		}
	}

	void styleCodeArea( success ) {
		codeArea.styleClass."${success ? 'remove' : 'add'}"( 'bad-code' )
	}

	void init( ) {
		new Timer( true ).scheduleAtFixedRate(
				updateCodeResult,
				1000, 1000
		)
		start {
			stage( title: 'Visual Code', visible: true ) {
				scene( fill: WHITE, width: 800, height: 400 ) {
					stylesheets( 'com/athaydes/visualcode/visual-code.css' )
					vbox( padding: 60 ) {
						text( text: 'Enter some code', styleClass: 'code-area-label' )
						hbox() {
							codeArea = textArea( width: 300, height: 200,
									styleClass: 'code-area-text' )
							def codeResultArea = textArea( width: 300, height: 200,
									styleClass: 'code-result-text',
									editable: false )
							codeResultArea.textProperty().bind( codeResultTextProperty )
						}

					}

				}
			}
		}
	}

	static void main( args ) {
		new VisualCodeApp().init()
	}

}
