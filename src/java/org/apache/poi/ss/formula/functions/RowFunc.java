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

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for the Excel function ROW
 *
 * @author Josh Micich
 */
public final class RowFunc implements Function0Arg, Function1Arg {


    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
        return new NumberEval(srcRowIndex+1);
    }
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        int rnum;

        if (arg0 instanceof AreaEval) {
            rnum = ((AreaEval) arg0).getFirstRow();
        } else if (arg0 instanceof RefEval) {
            rnum = ((RefEval) arg0).getRow();
        } else {
            // anything else is not valid argument
            return ErrorEval.VALUE_INVALID;
        }

        return new NumberEval(rnum + 1);
    }
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        switch (args.length) {
            case 1:
                return evaluate(srcRowIndex, srcColumnIndex, args[0]);
            case 0:
              return new NumberEval(srcRowIndex+1);
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

            // freeze args
            if (notArrayArgs() != null) {
                for (Integer j : notArrayArgs())
                    if (j < args.length)
                        newArgs[j] = args[j];
            }

            result[i] = evaluate(newArgs, srcRowIndex, srcColumnIndex);
        }
        return new ArrayEval(result, firstRow, lastRow);
    }

    public Set<Integer> notArrayArgs() {
        Set<Integer> xs = new HashSet<Integer>();
        xs.add(0);
        return xs;
    }
}
