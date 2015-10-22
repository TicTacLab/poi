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

import java.util.*;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * Implementation for Excel EDATE () function.
 */
public class EDate implements FreeRefFunction {
    public static final FreeRefFunction instance = new EDate();

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            double startDateAsNumber = getValue(args[0]);
            int offsetInMonthAsNumber = (int) getValue(args[1]);

            Date startDate = DateUtil.getJavaDate(startDateAsNumber);
            Calendar calendar = LocaleUtil.getLocaleCalendar();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, offsetInMonthAsNumber);
            return new NumberEval(DateUtil.getExcelDate(calendar.getTime()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    @Override
    public ValueEval evaluateArray(ValueEval[] args, OperationEvaluationContext ec) {
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

            result[i] = evaluate(newArgs, ec);
        }
        return new ArrayEval(result, firstRow, lastRow);
    }

    @Override
    public Set<Integer> notArrayArgs() {
        return null;
    }

    private double getValue(ValueEval arg) throws EvaluationException {
        if (arg instanceof NumberEval) {
            return ((NumberEval) arg).getNumberValue();
        }
        if(arg instanceof BlankEval) {
            return 0;
        }
        if (arg instanceof RefEval) {
            RefEval refEval = (RefEval)arg;
            if (refEval.getNumberOfSheets() > 1) {
                // Multi-Sheet references are not supported
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            
            ValueEval innerValueEval = refEval.getInnerValueEval(refEval.getFirstSheetIndex());
            if(innerValueEval instanceof NumberEval) {
                return ((NumberEval) innerValueEval).getNumberValue();
            }
            if(innerValueEval instanceof BlankEval) {
                return 0;
            }
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }
}
