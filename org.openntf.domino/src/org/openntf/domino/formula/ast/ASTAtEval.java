/* Generated By:JJTree: Do not edit this line. ASTAtEval.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
/*
 * © Copyright FOCONIS AG, 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 */
package org.openntf.domino.formula.ast;

import java.util.Set;

import org.openntf.domino.formula.EvaluateException;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.FormulaParseException;
import org.openntf.domino.formula.FormulaReturnException;
import org.openntf.domino.formula.ValueHolder;
import org.openntf.domino.formula.ValueHolder.DataType;
import org.openntf.domino.formula.parse.AtFormulaParserImpl;

public class ASTAtEval extends SimpleNode {

	public ASTAtEval(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openntf.domino.formula.ASTNode#toFormula(java.lang.StringBuilder)
	 */
	public void toFormula(final StringBuilder sb) {
		sb.append("@Eval");
		appendParams(sb);
	}

	/**
	 * Evaluates every entry and returns the last one
	 */
	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws FormulaReturnException {
		ValueHolder vhEval = children[0].evaluate(ctx);
		if (vhEval.dataType == DataType.ERROR)
			return vhEval;

		ValueHolder ret = null;
		try {
			for (int i = 0; i < vhEval.size; i++) {
				String toEval = vhEval.getString(i);
				// TODO RPr : We do not have the parser if the AST was serialized!
				Node n = (Node) parser.parse(toEval, false);
				ret = n.evaluate(ctx);
				if (ret.dataType == DataType.ERROR)
					break;
			}
			return ret;
		} catch (FormulaParseException e) {
			return ValueHolder.valueOf(new EvaluateException(codeLine, codeColumn, e));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.openntf.domino.formula.ast.SimpleNode#analyzeThis(java.util.Set, java.util.Set, java.util.Set, java.util.Set)
	 */
	@Override
	protected void analyzeThis(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {
		functions.add("@eval");
	}

}
/* JavaCC - OriginalChecksum=9f002c572c2d7c430f81c39c0b4cef35 (do not edit this line) */