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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.ArrayFunctionsHelper;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

import java.util.Set;

/**
 * Implementation of 'Analysis Toolpak' Excel function IFERROR()<br/>
 *
 * Returns an error text if there is an error in the evaluation<p/>
 * 
 * <b>Syntax</b><br/>
 * <b>IFERROR</b>(<b>expression</b>, <b>string</b>)
 * 
 * @author Johan Karlsteen
 */
final class IfError implements FreeRefFunction {

	public static final FreeRefFunction instance = new IfError();

	private IfError() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		if (args.length != 2) {
			return ErrorEval.VALUE_INVALID;
		}

		ValueEval val;
		try {
			val = evaluateInternal(args[0], args[1], ec.getRowIndex(), ec.getColumnIndex());
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		return val;
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

	private static ValueEval evaluateInternal(ValueEval arg, ValueEval iferror, int srcCellRow, int srcCellCol) throws EvaluationException {
		arg = WorkbookEvaluator.dereferenceResult(arg, srcCellRow, srcCellCol);
		if(arg instanceof ErrorEval) {
			return iferror;
		} else {
			return arg;
		}
	}
}
