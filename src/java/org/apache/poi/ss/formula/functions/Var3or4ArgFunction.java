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
 * Convenience base class for any function which must take three or four
 * arguments
 *
 * @author Josh Micich
 */
abstract class Var3or4ArgFunction implements Function3Arg, Function4Arg {

	public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
       switch (args.length) {
          case 3:
             if (ArrayFunctionsHelper.isAnyIArrayEval(args)) {
                return evaluateArray(args, srcRowIndex, srcColumnIndex);
             } else {
                return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
             }
          case 4:
             if (ArrayFunctionsHelper.isAnyIArrayEval(args)) {
                return evaluateArray(args, srcRowIndex, srcColumnIndex);
             } else {
                return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2], args[3]);
             }
       }
		return ErrorEval.VALUE_INVALID;
	}

   public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
      int length = ArrayFunctionsHelper.getIArrayArg(args).getLength();
      IArrayEval[] arargs = new IArrayEval[args.length];
      for (int i = 0; i < args.length; i++) arargs[i] = ArrayFunctionsHelper.coerceToIArrayEval(args[i], length);
      int firstRow = ArrayFunctionsHelper.getFirstRow(args);
      int lastRow = ArrayFunctionsHelper.getLastRow(args, length - 1);


      ValueEval[] result = new ValueEval[length];
      for (int i = 0; i < length; i++) {
         ValueEval[] newArgs = new ValueEval[args.length];
         for (int j = 0; j < args.length; j++) newArgs[j] = arargs[j].getValue(i);
         result[i] = evaluate(newArgs, srcRowIndex, srcColumnIndex);
      }
      return new ArrayEval(result, firstRow, lastRow);
   }
}
