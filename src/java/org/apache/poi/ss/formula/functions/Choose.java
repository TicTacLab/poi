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

import org.apache.poi.ss.formula.eval.*;

/**
 * @author Josh Micich
 */
public final class Choose implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		if (args.length < 2) {
			return ErrorEval.VALUE_INVALID;
		}

		if (ArrayFunctionsHelper.isIArrayEval(args[0])) {
			return evaluateArray(args, srcRowIndex, srcColumnIndex);
		} else {

			try {
				int ix = evaluateFirstArg(args[0], srcRowIndex, srcColumnIndex);
				if (ix < 1 || ix >= args.length) {
					return ErrorEval.VALUE_INVALID;
				}
				ValueEval result = OperandResolver.getSingleValue(args[ix], srcRowIndex, srcColumnIndex);
				if (result == MissingArgEval.instance) {
					return BlankEval.instance;
				}
				return result;
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}
	}

	public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		IArrayEval a0 = ArrayFunctionsHelper.coerceToIArrayEval(args[0]);
		int length = a0.getLength();
		int firstRow = ArrayFunctionsHelper.getFirstRow(args);
		int lastRow = ArrayFunctionsHelper.getLastRow(args, length - 1);


		ValueEval[] result = new ValueEval[length];
		for (int i = 0; i < length; i++) {
			ValueEval[] newArgs = new ValueEval[args.length];
			newArgs[0] = a0.getValue(i);
			System.arraycopy(args, 1, newArgs, 1, args.length - 1);
			result[i] = evaluate(newArgs, firstRow+i, srcColumnIndex);
		}
		return new ArrayEval(result, firstRow, lastRow);
	}

	public static int evaluateFirstArg(ValueEval arg0, int srcRowIndex, int srcColumnIndex)
			throws EvaluationException {
		ValueEval ev = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
		return OperandResolver.coerceValueToInt(ev);
	}
}
