/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.translator.ionic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.teiid.language.ColumnReference;
import org.teiid.language.Comparison;
import org.teiid.language.Comparison.Operator;
import org.teiid.language.DerivedColumn;
import org.teiid.language.Expression;
import org.teiid.language.Function;
import org.teiid.language.In;
import org.teiid.language.Literal;
import org.teiid.language.NamedTable;
import org.teiid.language.Select;
import org.teiid.language.visitor.HierarchyVisitor;
import org.teiid.metadata.Column;
import org.teiid.translator.TranslatorException;

public class IonicSQLVisitor extends HierarchyVisitor {
	private String tableName;
	private List<String> selectedColumns = new ArrayList<String>();
	private Map<String, List<String>> filterValues = new HashMap<String, List<String>>();
	protected Stack<Object> onGoingExpression  = new Stack<Object>();
	protected ArrayList<TranslatorException> exceptions = new ArrayList<TranslatorException>();
	private String functionName;
	private List<String> functionParameters;
	
	@Override
    public void visit(In obj) {
        visitNode(obj.getLeftExpression());
        Column c = (Column)this.onGoingExpression.pop();
        
        ArrayList<String> values = new ArrayList<String>();
        for (Expression expr:obj.getRightExpressions()){
        	visitNode(expr);
        	values.add(this.onGoingExpression.pop().toString());
        }
        this.filterValues.put(c.getName(), values);
    }
	
	@Override
	public void visit(Literal obj) {
		this.onGoingExpression.push(obj.getValue());
	}	
	
	@Override
	public void visit(Comparison obj) {
		if (obj.getOperator() == Operator.EQ){
			visitNode(obj.getLeftExpression());
			Column c = (Column)this.onGoingExpression.pop();
			visitNode(obj.getRightExpression());
			ArrayList<String> values = new ArrayList<String>();
			values.add(this.onGoingExpression.pop().toString());
			this.filterValues.put(c.getName(), values);
		} else {
			this.exceptions.add(new TranslatorException("Only Equality supported"));
		}
		
	}	
	
	@Override
	public void visit(Function obj) {
		String funcName = obj.getName();
		if (funcName.indexOf('.') != -1) {
			funcName = funcName.substring(funcName.indexOf('.')+1);
		}
		this.functionName = funcName;
		
    	List<Expression> args = obj.getParameters();
		if (args != null && args.size() > 0) {
			ArrayList<String> values = new ArrayList<String>();
			for (Expression expr : args) {
				visitNode(expr);
				values.add(this.onGoingExpression.pop().toString());
			}
			this.functionParameters = values;
		}
	}	
	
	@Override
	public void visit(ColumnReference obj) {
		this.onGoingExpression.push(obj.getMetadataObject());
	}	
	
	@Override
	public void visit(DerivedColumn obj) {
		if (obj.getExpression() instanceof Function){
			visitNode(obj.getExpression());
		} else {
			ColumnReference column = (ColumnReference)obj.getExpression();
			this.selectedColumns.add(column.getMetadataObject().getName());
		}
	}
	
	@Override
	public void visit(NamedTable obj) {
		this.tableName = obj.getMetadataObject().getName();
	}

	public void execute(Select command, IonicExecution ionicExecution) throws TranslatorException {
		visitNode(command);
		
		if (!this.exceptions.isEmpty()){
			throw this.exceptions.get(0);
		}
		
		if (this.tableName != null){
			ionicExecution.execute(this.tableName, this.selectedColumns, this.filterValues);
		} else if (this.functionName != null){
			ionicExecution.executeFunction(this.functionName, this.functionParameters);
		}
		
	}
}
