/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;

public final class XMLGrammarPoolImplDS extends XMLGrammarPoolImpl {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XMLGrammarPoolImplDS.class.getName());

    private static final Grammar[] INITIAL_GRAMMAR_SET = new Grammar[0];

    /**
     * @see org.apache.xerces.xni.grammars.XMLGrammarPool#retrieveInitialGrammarSet(String)
     */
    @Override
    public Grammar[] retrieveInitialGrammarSet(final String grammarType) {
        return INITIAL_GRAMMAR_SET;
    }

    @Override
    public boolean equals(final XMLGrammarDescription desc1, final XMLGrammarDescription desc2) {
    	if ((desc1 instanceof XSDDescription) && (desc2 instanceof XSDDescription)) {
    		final String 	systemId1 	= ((XSDDescription)desc1).getLiteralSystemId();
    		final String 	systemId2 	= ((XSDDescription)desc2).getLiteralSystemId();
    		final boolean 	equals 		= systemId1.equals(systemId2);
    		//logger.info("system-id1: " + systemId1 + ", system-id2: " + systemId2 + ", equals: " + equals);
    		return equals;
    	} else {
    		return super.equals(desc1, desc2);
    	}
    }

}
