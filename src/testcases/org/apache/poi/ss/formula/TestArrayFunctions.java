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
import org.apache.poi.hssf.usermodel.examples.CellTypes;
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

    public String int2type(int type) {
        switch (type) {
            case 0: return "Number";
            case 1: return "String";
            case 2: return "Formula";
            case 3: return "Blank";
            case 4: return "Boolean";
            case 5: return "Error";
        }
        return "42";
    }

    public void assertType(int expected, CellValue value) {
        int actual = value.getCellType();
        assertEquals( String.format("Expect type %s, but get %s", int2type(expected), int2type(actual)),
                expected, actual);
    }

    public void assertFormulaResult(Workbook wb, double expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_NUMERIC, value);
        assertEquals(expected, value.getNumberValue(), 0.01);
    }

    public void assertFormulaResult(Workbook wb, boolean expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_BOOLEAN, value);
        assertEquals(expected, value.getBooleanValue());
    }

    public void assertFormulaResult(Workbook wb, String expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_STRING, value);
        assertEquals(expected, value.getStringValue());
    }

    public void assertFormulaResult(Workbook wb, ErrorEval expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_ERROR, value);
        assertEquals(expected, ErrorEval.valueOf(value.getErrorValue()));
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
        assertFormulaResult(wb, -189600,"SUM(-F2:F17)");
        assertFormulaResult(wb, "\u0001", "CHAR(F2:F17/F2:F17)");
        assertFormulaResult(wb, 105.0, "F2:F17%");
    }

    public void testFixed2ArgFunction() {
        assertFormulaResult(wb, "I", "ROMAN(F2:F17/F2:F17,0)");
    }

    public void testFixed3ArgFunction() {
        assertFormulaResult(wb, "10", "MID(F2:F17,1,2)");
    }

    public void testFixed4ArgFunction() {
        assertFormulaResult(wb, "Mu500", "REPLACE(F2:F17,1,2,\"Mu\")");
    }

    public void testVar1or2ArgFunction() {
        assertFormulaResult(wb, 1.0, "TRUNC(F2:F17/F2:F17,1)");
    }

    public void testVar2or3ArgFunction() {

    }

    public void testVar3or4ArgFunction() {
        assertFormulaResult(wb, "105Do0", "SUBSTITUTE(F2:F17,\"0\",\"Do\",2)");
    }

    public void testIf() {
        assertFormulaResult(wb, 1, "SUM(IF(F2:F17=10500, A2:A17, 0))");
    }

    public void testChoose() {
        assertFormulaResult(wb, "Barnhill", "CHOOSE(A2:A17,B2:B17,C2:C17,D2:D17,E2:E17,F2:F17)");
    }

    public void testTextFunction() {
        assertFormulaResult(wb, "Sedan1", "CONCATENATE(C2:C17,A2:A17)");
        assertFormulaResult(wb, 5, "LEN(F2:F17)");
        assertFormulaResult(wb, "00", "RIGHT(F2:F17,2)");
    }
    public void testFreeRefFunction() {
        assertFormulaResult(wb, "Total Sales", "INDIRECT(CONCATENATE(\"f\", A2:A17))");
    }

    public void testFunctionsWithNoSupportOfArrayArgs() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "DELTA(A2:A17,B2:B17)");
        assertFormulaResult(wb, ErrorEval.NUM_ERROR, "DEC2HEX(A2:A17)");
        assertFormulaResult(wb, ErrorEval.NUM_ERROR, "DEC2BIN(A2:A17)");
        assertFormulaResult(wb, ErrorEval.NUM_ERROR, "BIN2DEC(A2:A17)");
    }

    public void testAddress() {
        assertFormulaResult(wb, "Sedan!$E$1", "ADDRESS(A2:A17,D2:D17,1,1,C2:C17)");

    }
}
