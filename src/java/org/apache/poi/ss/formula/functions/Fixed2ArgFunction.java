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
import org.apache.poi.ss.usermodel.FormulaError;

/**
 * Convenience base class for functions that must take exactly two arguments.
 *
 * @author Josh Micich
 */
public abstract class Fixed2ArgFunction implements Function2Arg {
    public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        if (ArrayFunctionsHelper.isAnyIArrayEval(args)) {
            return evaluateArray(args, srcRowIndex, srcColumnIndex);
        } else {
            return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1]);
        }
    }

    public final ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        int length = ArrayFunctionsHelper.getIArrayArg(args).getLength();
        int firstRow = ArrayFunctionsHelper.getFirstRow(args);
        int lastRow = ArrayFunctionsHelper.getLastRow(args, length - 1);

        IArrayEval a0 = ArrayFunctionsHelper.coerceToIArrayEval(args[0], length);
        IArrayEval a1 = ArrayFunctionsHelper.coerceToIArrayEval(args[1], length);

        ValueEval[] result = new ValueEval[length];
        for (int i = 0; i < length; i++) {
            result[i] = evaluate(firstRow+i, srcColumnIndex, a0.getValue(i), a1.getValue(i));
        }
        return new ArrayEval(result, firstRow, lastRow);
    }

}
