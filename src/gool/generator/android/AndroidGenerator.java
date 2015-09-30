/*
 * Copyright 2010 Pablo Arrighi, Alex Concha, Miguel Lezama for version 1.
 * Copyright 2013 Pablo Arrighi, Miguel Lezama, Kevin Mazet for version 2.    
 *
 * This file is part of GOOL.
 *
 * GOOL is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3.
 *
 * GOOL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License along with GOOL,
 * in the file COPYING.txt.  If not, see <http://www.gnu.org/licenses/>.
 */

package gool.generator.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import gool.ast.core.BinaryOperation;
import gool.ast.core.Block;
import gool.ast.core.Catch;
import gool.ast.core.ClassNew;
import gool.ast.core.Constant;
import gool.ast.core.CustomDependency;
import gool.ast.core.Dependency;
import gool.ast.core.EnhancedForLoop;
import gool.ast.core.EqualsCall;
import gool.ast.core.Expression;
import gool.ast.core.Finally;
import gool.ast.core.MainMeth;
import gool.ast.core.Meth;
import gool.ast.core.Modifier;
import gool.ast.core.Operator;
import gool.ast.core.ParentCall;
import gool.ast.core.RecognizedDependency;
import gool.ast.core.Statement;
import gool.ast.core.Throw;
import gool.ast.core.ToStringCall;
import gool.ast.core.Try;
import gool.ast.core.TypeDependency;

import gool.ast.list.ListAddCall;
import gool.ast.list.ListContainsCall;
import gool.ast.list.ListGetCall;
import gool.ast.list.ListGetIteratorCall;
import gool.ast.list.ListIsEmptyCall;
import gool.ast.list.ListRemoveAtCall;
import gool.ast.list.ListRemoveCall;
import gool.ast.list.ListSizeCall;
import gool.ast.map.MapContainsKeyCall;
import gool.ast.map.MapEntryGetKeyCall;
import gool.ast.map.MapEntryGetValueCall;
import gool.ast.map.MapGetCall;
import gool.ast.map.MapGetIteratorCall;
import gool.ast.map.MapIsEmptyCall;
import gool.ast.map.MapPutCall;
import gool.ast.map.MapRemoveCall;
import gool.ast.map.MapSizeCall;
import gool.ast.system.SystemCommandDependency;
import gool.ast.system.SystemOutDependency;
import gool.ast.system.SystemOutPrintCall;
import gool.ast.type.IType;
import gool.ast.type.TypeBool;
import gool.ast.type.TypeChar;
import gool.ast.type.TypeDecimal;
import gool.ast.type.TypeEntry;
import gool.ast.type.TypeException;
import gool.ast.type.TypeInt;
import gool.ast.type.TypeList;
import gool.ast.type.TypeMap;
import gool.ast.type.TypeObject;
import gool.ast.type.TypeString;
import gool.generator.common.CommonCodeGenerator;
import gool.generator.common.GeneratorMatcher;

public class AndroidGenerator extends CommonCodeGenerator {

	private static Map<String, Dependency> customDependencies = new HashMap<String, Dependency>();

	public void addCustomDependency(String key, Dependency value) {
		customDependencies.put(key, value);
	}

	@Override
	public String getCode(BinaryOperation binaryOp) {
		if (!(binaryOp.getLeft() instanceof Constant)
				&& binaryOp.getOperator() == Operator.EQUAL) {
			return String.format("%s==(%s)", binaryOp.getLeft(),
					binaryOp.getRight());
		} else {
			return super.getCode(binaryOp);
		}
	}

	public String getCode(ClassNew classNew) {
		if (classNew.getType().toString().equals("File"))
			return String.format(
					"new %s (Environment.getExternalStorageDirectory(),%s)",
					classNew.getType(),
					StringUtils.join(classNew.getParameters(), ", "));
		if (classNew.getType().toString().equals("FileReader")) {
			List<String> tempList = new ArrayList<String>();
			for (Expression expression : classNew.getParameters()) {
				if (expression.getType() instanceof TypeString) {
					String tempString = "new File (Environment.getExternalStorageDirectory(),"
							+ expression.toString() + ")";
					tempList.add(String.valueOf(tempString));
				} else {
					tempList.add(expression.toString());
				}
			}
			return String.format("new %s (%s)", classNew.getType(),
					StringUtils.join(tempList, ","));
		}
		if (classNew.getType().toString().equals("FileWriter")) {
			List<String> tempList2 = new ArrayList<String>();
			for (Expression expression : classNew.getParameters()) {
				if (expression.getType() instanceof TypeString) {
					String tempString = "new File (Environment.getExternalStorageDirectory(),"
							+ expression.toString() + ")";
					tempList2.add(String.valueOf(tempString));
				} else {
					tempList2.add(expression.toString());
				}
			}
			return String.format("new %s (%s)", classNew.getType(),
					StringUtils.join(tempList2, ","));
		}

		return String.format("new %s(%s)", classNew.getType(),
				StringUtils.join(classNew.getParameters(), ", "));
	}

	public String getCode(CustomDependency customDependency) {
		if (!customDependencies.containsKey(customDependency.getName())) {
			System.out.println(String.format(
					"Custom dependencies: %s, Desired: %s", customDependencies,
					customDependency.getName()));
			throw new IllegalArgumentException(
					String.format(
							"There is no equivalent type in Java for the GOOL type '%s'.",
							customDependency.getName()));
		}
		return customDependencies.get(customDependency.getName()).toString();
	}

	@Override
	public String getCode(EnhancedForLoop enhancedForLoop) {
		return String
				.format("for(%s : %s){%s}",
						enhancedForLoop.getVarDec(),
						(enhancedForLoop.getExpression().getType() instanceof TypeMap) ? String
								.format("%s.entrySet()",
										enhancedForLoop.getExpression())
								: enhancedForLoop.getExpression(),
						enhancedForLoop.getStatements());
	}

	@Override
	public String getCode(EqualsCall equalsCall) {
		return String.format("%s.equals(%s)", equalsCall.getTarget(),
				StringUtils.join(equalsCall.getParameters(), ", "));
	}

	public String getCode(ListAddCall lac) {
		return String.format("%s.add(%s)", lac.getExpression(),
				StringUtils.join(lac.getParameters(), ", "));
	}

	public String getCode(ListContainsCall lcc) {
		return String.format("%s.contains(%s)", lcc.getExpression(),
				StringUtils.join(lcc.getParameters(), ", "));
	}

	public String getCode(ListGetCall lgc) {
		return String.format("%s.get(%s)", lgc.getExpression(),
				StringUtils.join(lgc.getParameters(), ", "));
	}

	public String getCode(ListGetIteratorCall lgic) {
		return String.format("%s.getIterator()", lgic.getExpression());
	}

	public String getCode(ListIsEmptyCall liec) {
		return String.format("%s.isEmpty()", liec.getExpression());
	}

	public String getCode(ListRemoveAtCall lrc) {
		return String.format("%s.remove(%s)", lrc.getExpression(),
				StringUtils.join(lrc.getParameters(), ", "));
	}

	public String getCode(ListRemoveCall lrc) {
		return String.format("%s.remove(%s)", lrc.getExpression(),
				StringUtils.join(lrc.getParameters(), ", "));
	}

	public String getCode(ListSizeCall lsc) {
		return String.format("%s.size()", lsc.getExpression());
	}

	/**
	 * Changed from java, might cause problems if input java has more than one
	 * main method or if Android uses public static void main. Currently
	 * sufficient for preliminary tests on HelloWorld.
	 */
	public String getCode(MainMeth mainMeth) {
		return "public static void EntryMethod(String[] args)";
	}

	@Override
	public String getCode(MapContainsKeyCall mapContainsKeyCall) {
		return String.format("%s.containsKey(%s)",
				mapContainsKeyCall.getExpression(),
				StringUtils.join(mapContainsKeyCall.getParameters(), ", "));
	}

	@Override
	public String getCode(MapEntryGetKeyCall mapEntryGetKeyCall) {
		return String.format("%s.getKey()", mapEntryGetKeyCall.getExpression());
	}

	@Override
	public String getCode(MapEntryGetValueCall mapEntryGetKeyCall) {
		return String.format("%s.getValue()",
				mapEntryGetKeyCall.getExpression());
	}

	@Override
	public String getCode(MapGetCall mapGetCall) {
		return String.format("%s.get(%s)", mapGetCall.getExpression(),
				StringUtils.join(mapGetCall.getParameters(), ", "));
	}

	@Override
	public String getCode(MapGetIteratorCall mapGetIteratorCall) {
		return String.format("%s.getIterator()",
				mapGetIteratorCall.getExpression());
	}

	@Override
	public String getCode(MapIsEmptyCall mapIsEmptyCall) {
		return String.format("(%s.isEmpty()", mapIsEmptyCall.getExpression());
	}

	@Override
	public String getCode(MapPutCall mapPutCall) {
		return String.format("%s.put(%s)", mapPutCall.getExpression(),
				StringUtils.join(mapPutCall.getParameters(), ", "));
	}

	@Override
	public String getCode(MapRemoveCall mapRemoveCall) {
		return String.format("%s.remove(%s)", mapRemoveCall.getExpression(),
				StringUtils.join(mapRemoveCall.getParameters(), ", "));
	}

	@Override
	public String getCode(MapSizeCall mapSizeCall) {
		return String.format("%s.size()", mapSizeCall.getExpression());
	}

	@Override
	public String getCode(ParentCall parentCall) {
		String out = "super(";
		if (parentCall.getParameters() != null) {
			out += StringUtils.join(parentCall.getParameters(), ", ");
		}
		out += ")";
		return out;
	}

	public String getCode(SystemOutDependency systemOutDependency) {
		return "noprint";
	}

	// TODO Currently an android.widget.TextView called systemOutTextBox is used
	// as a System.out equivalent, this can probably be optimized with a
	// Singleton
	// type instance

	@Override
	public String getCode(SystemOutPrintCall systemOutPrintCall) {
		return String.format(
				"PrintOut.getSystemOutTextBox().append(%s+\"\\n\"); ",
				StringUtils.join(systemOutPrintCall.getParameters(), ","))
				+ String.format(" Log.i(\"JUnitSysOut\",String.valueOf(%s)) ",
						StringUtils.join(systemOutPrintCall.getParameters(),
								","));
	}

	public String getCode(ToStringCall tsc) {
		return String.format("%s.toString()", tsc.getTarget());
	}

	@Override
	public String getCode(TypeBool typeBool) {
		return "Boolean";
	}

	@Override
	public String getCode(TypeDecimal typeReal) {
		return "Double";
	}

	public String getCode(TypeDependency typeDependency) {
		if (typeDependency.getType() instanceof TypeList) {
			return "java.util.ArrayList";
		}
		if (typeDependency.getType() instanceof TypeMap) {
			return "java.util.HashMap";
		}
		if (typeDependency.getType() instanceof TypeEntry) {
			return "java.util.Map";
		}

		return super.getCode(typeDependency);
	}

	@Override
	public String getCode(TypeEntry typeEntry) {
		return String.format("Map.Entry<%s, %s>", typeEntry.getKeyType(),
				typeEntry.getElementType());
	}

	@Override
	public String getCode(TypeInt typeInt) {
		return "Integer";
	}

	@Override
	public String getCode(TypeList typeList) {

		IType elementType = typeList.getElementType();
		if (elementType == null) {
			elementType = TypeObject.INSTANCE;
		}
		return String.format("ArrayList<%s>", elementType);
	}

	@Override
	public String getCode(TypeMap typeMap) {
		return String.format("HashMap<%s, %s>", typeMap.getKeyType(),
				typeMap.getElementType());
	}

	@Override
	public String getCode(TypeObject typeObject) {
		return "Object";
	}

	@Override
	public String getCode(TypeString typeString) {
		return "String";
	}

	@Override
	public String getCode(TypeChar typeChar) {
		return "char";
	}

	@Override
	public String getCode(Modifier modifier) {
		if (modifier == Modifier.OVERRIDE) {
			return "";
		}
		if (modifier == Modifier.VIRTUAL) {
			return "";
		}

		return modifier.name().toLowerCase();

	}

	@Override
	public String getCode(SystemCommandDependency systemCommandDependency) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode(Try t) {
		StringBuilder result = new StringBuilder();
		result.append("try{\n");
		for (Statement statement : t.getBlock().getStatements()) {
			result.append(statement);
			if (!(statement instanceof Block)) {
				result.append(";").append("\n");
			}

		}
		result.append("\n}"); // closing bracket
		for (Catch c : t.getCatches()) {
			result.append(getCode(c));

		}
		return result.toString();
	}

	@Override
	public String getCode(Catch c) {
		StringBuilder result = new StringBuilder();
		result.append("\n catch(");
		result.append(") {\n");
		for (Statement statement : c.getBlock().getStatements()) {
			result.append(statement);
			if (!(statement instanceof Block)) {
				result.append(";").append("\n");
			}

		}

		result.append("\n}");
		return result.toString();
	}

	@Override
	public String getCode(Meth meth) {
		if (meth.getThrowStatement().size() == 0) {
			return super.getCode(meth);
		} else {
			return String.format("%s %s %s(%s) throws %s",
					getCode(meth.getModifiers()), meth.getType(),
					meth.getName(), StringUtils.join(meth.getParams(), ", "),
					StringUtils.join(meth.getThrowStatement(), ", "));
		}

	}


	@Override
	public String getCode(Throw throwStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode(TypeException typeException) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getCode(RecognizedDependency recognizedDependency) {
		String result = "";
		List<String> imports = GeneratorMatcher.matchImports(recognizedDependency.getName());
		if(imports == null)
			return result;
		for (String Import : imports) {
			if (Import.startsWith("+"))
				Import = Import.substring(1);
			result += "import " + Import + ";\n";
		}

		return result;
	}
}
