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

package org.apache.poi.ss.formula;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.text.Normalizer;

/**
 * Tests {@link WorkbookEvaluator}.
 *
 * @author Josh Micich
 */
public class TestArrayFunctions extends TestCase {

    XSSFWorkbook wb;

    @Override
    protected void setUp() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("TestArrayAggregations.xlsx");
    }

    public void setArrayFormula(Workbook wb, String formula) {
        wb.getSheet("Sheet1").setArrayFormula(formula, CellRangeAddress.valueOf("F18:F18"));
    }

    public CellValue evaluate(Workbook wb) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        fe.setDebugEvaluationOutputForNextEval(true);
        return fe.evaluate(wb.getSheet("Sheet1").getRow(17).getCell(5));
    }

    public void assertFormulaResult(Workbook wb, double expected, String formula) {
        setArrayFormula(wb, formula);
        assertEquals(expected, evaluate(wb).getNumberValue(), 0.0001);
    }

    public void assertFormulaResult(Workbook wb, boolean expected, String formula) {
        setArrayFormula(wb, formula);
        assertEquals(expected, evaluate(wb).getBooleanValue());
    }

    public void assertFormulaResult(Workbook wb, String expected, String formula) {
        setArrayFormula(wb, formula);
        assertEquals(expected, evaluate(wb).getStringValue());
    }

	public void testAggregateOfBasicOperators() {
        assertFormulaResult(wb, 189691.0, "SUM(D2:D17+F2:F17)");
        assertFormulaResult(wb, -189509.0, "SUM(D2:D17-F2:F17)");
        assertFormulaResult(wb, 1242300.0, "SUM(D2:D17*F2:F17)");
        assertFormulaResult(wb, 0.007894, "SUM(D2:D17/F2:F17)");
        assertFormulaResult(wb, 316674713773269.00, "SUM(D2:D17^A2:A17)");
	}

    public void testAggregateFunctions() {
        assertFormulaResult(wb, -4790016000.0, "PRODUCT(A2:A17-D2:D17)");
    }

    public void testThreeAndMoreArguments() {
        assertFormulaResult(wb, 189827.0, "SUM(A2:A17+D2:D17+F2:F17)");
        assertFormulaResult(wb, 222677.0, "SUM(A2:A17+D2:D17+F2:F17+E2:E17)");
        //        assertFormulaResult(wb, 189827.0, "{1,2,3}*3");
    }

    public void testLogicOperators() {
        assertFormulaResult(wb, true, "OR(C2:C17=\"Sedan\")");
        assertFormulaResult(wb, true, "OR(NOT(C2:C17=\"Sedan\"))");
        //assertFormulaResult(wb, 0, "IF(C2<>\"Sedan\",1,0)");
        //assertFormulaResult(wb, 57600.0, "SUM(IF(C2:C17=\"Sedan\", INDEX(F2:F17, ROW(F2:F17), 1), \"\"))");
    }

    public void testFixed1ArgFunction() {
        assertFormulaResult(wb, -136,"SUM(-A2:A17)");
        assertFormulaResult(wb, "\u0001", "CHAR(A2:A17)");
        assertFormulaResult(wb, 0.01, "A2:A17%");
    }

    public void testFixed2ArgFunction() {
        assertFormulaResult(wb, "I", "ROMAN(A2:A17,0)");
    }

    public void testFixed3ArgFunction() {
        assertFormulaResult(wb, "Se", "MID(C2:C17,1,2)");
    }

    public void testFixed4ArgFunction() {
        assertFormulaResult(wb, "Mudan", "REPLACE(C2:C17,1,2,\"Mu\")");
    }

    public void testVar1or2ArgFunction() {
        assertFormulaResult(wb, 0.2, "TRUNC(A2:A17/D2:D17,1)");
    }

    public void testVar2or3ArgFunction() {

    }
}
