/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.functions;

import java.util.Set;

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Implementation for the function COUNTIF
 * <p>
 *  Syntax: COUNTIF ( range, criteria )
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>range&nbsp;&nbsp;&nbsp;</th><td>is the range of cells to be counted based on the criteria</td></tr>
 *      <tr><th>criteria</th><td>is used to determine which cells to count</td></tr>
 *    </table>
 * </p>
 */
public final class Countif extends Fixed2ArgFunction {

	@Override
	public Set<Integer> notArrayArgs() {
		return ArrayFunctionsHelper.asSet(0);
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {

		I_MatchPredicate mp = CountUtils.createCriteriaPredicate(arg1, srcRowIndex, srcColumnIndex);
		if(mp == null) {
			// If the criteria arg is a reference to a blank cell, countif always returns zero.
			return NumberEval.ZERO;
		}
		double result = countMatchingCellsInArea(arg0, mp);
		return new NumberEval(result);
	}
	/**
	 * @return the number of evaluated cells in the range that match the specified criteria
	 */
	private double countMatchingCellsInArea(ValueEval rangeArg, I_MatchPredicate criteriaPredicate) {

		if (rangeArg instanceof RefEval) {
			return CountUtils.countMatchingCellsInRef(rangeArg, criteriaPredicate);
		} else if (rangeArg instanceof ThreeDEval) {
			return CountUtils.countMatchingCellsInArea(rangeArg, criteriaPredicate);
		} else {
			throw new IllegalArgumentException("Bad range arg type (" + rangeArg.getClass().getName() + ")");
		}
	}

	/**
	 * Creates a criteria predicate object for the supplied criteria arg
	 * @return <code>null</code> if the arg evaluates to blank.
	 */
	/* package */

}
