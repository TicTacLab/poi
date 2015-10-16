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

import org.apache.poi.ss.formula.eval.ArrayEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.IArrayEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Convenience base class for functions that must take exactly three arguments.
 *
 * @author Josh Micich
 */
public abstract class Fixed3ArgFunction implements Function3Arg {
	public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		if (args.length != 3) {
			return ErrorEval.VALUE_INVALID;
		}
        if (ArrayFunctionsHelper.isAnyIArrayEval(args)) {
           return evaluateArray(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
        } else {
            return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
        }
	}

    public final ValueEval evaluateArray(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1, ValueEval arg2) {
       int length = ArrayFunctionsHelper.getIArrayArg(new ValueEval[] {arg0, arg1, arg2}).getLength();

       IArrayEval a0 = ArrayFunctionsHelper.coerceToIArrayEval(arg0, length);
       IArrayEval a1 = ArrayFunctionsHelper.coerceToIArrayEval(arg1, length);
       IArrayEval a2 = ArrayFunctionsHelper.coerceToIArrayEval(arg2, length);

        ValueEval[] result = new ValueEval[length];
        for (int i = 0; i < length; i++) {
            result[i] = evaluate(srcRowIndex, srcColumnIndex, a0.getValue(i), a1.getValue(i), a2.getValue(i));
        }
        return new ArrayEval(result, 0, 1);
    }
}
