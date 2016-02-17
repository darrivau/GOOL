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

/**
 * Creates C++ files during the C++ generation process
 */

package gool.generator.cpp;

import gool.ast.core.ClassDef;
import gool.generator.common.CodePrinter;
import gool.generator.common.GeneratorMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logger.Log;

import org.apache.commons.lang.StringUtils;

/**
 * Provides the basic functionality to generate C++ code from a list of GOOL
 * classes.
 */
public class CppCodePrinter extends CodePrinter {
	private static final String TEMPLATE_DIR = "gool/generator/cpp/templates/";

//	private void createFinallyInclude(File outputDir) {
//		FileOutputStream goolHelperOut;
//		byte[] buffer = new byte[1024];
//		int noOfBytes;
//
//		// Helpers to create by copying the resource
//		List<String> goolHelperIn = new ArrayList<String>();
//		goolHelperIn.add("finally.h");
//		if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
//			Log.e(String.format("<CppCodePrinter> Impossible to create the directory '%s'",
//					outputDir));
//		} else {
//			// Print finally
//			for (String in : goolHelperIn) {
//				InputStream helper;
//				try {
//					helper = CppPlatform.class.getResource(in).openStream();
//					goolHelperOut = new FileOutputStream(outputDir + "/" + in);
//					while ((noOfBytes = helper.read(buffer)) != -1) {
//						goolHelperOut.write(buffer, 0, noOfBytes);
//					}
//					goolHelperOut.close();
//					helper.close();
//				} catch (IOException e) {
//					Log.e(String.format("<CppCodePrinter> Impossible to create the file '%s'",
//							in));
//				}
//			}
//		}
//	}

	public CppCodePrinter(File outputDir, Collection<File> myF) {
		super(new CppGenerator(), outputDir, myF);
		//createFinallyInclude(outputDir);
	}

	@Override
	public String getTemplateDir() {
		return TEMPLATE_DIR;
	}

	@Override
	public Map<String, String> print(ClassDef pclass){

		// GOOL library classes are printed in a different manner
		if (pclass.isGoolLibraryClass()) {
			return printGoolLibraryClass(pclass);
		}

		Map<String, String> completeClassList = new HashMap<String, String>();
		String outPutDir = ""; 
		if (!getOutputDir().getName().isEmpty())
			outPutDir = getOutputDir().getAbsolutePath() + 
			StringUtils.replace(pclass.getPackageName(), ".", File.separator) + 
			File.separator;
		/*
		 * In C++ the parent class and the interfaces are used in the same
		 * statement. Example: class Foo : public ClassBar1, InterfaceBar2 ...
		 * {}
		 */

		if (pclass.getParentClass() != null) {
			pclass.getInterfaces().add(0, pclass.getParentClass());
		}

		completeClassList.put(outPutDir + pclass.getName() + ".h", processTemplate("header.vm", pclass));

		/*
		 * Only generate header files if this element is an interface or an
		 * enumeration.
		 */
		if (!pclass.isEnum() && !pclass.isInterface()) {

			Map<String, String> sourceClass = super.print(pclass);

			for (Entry<String, String> entry : sourceClass.entrySet()){
				completeClassList.put(entry.getKey() + ".cpp", entry.getValue());
			}
		}
		return completeClassList;
	}

	@Override
	public Map <String, String> printGoolLibraryClass(ClassDef pclass){
		Map<String, String> result = new HashMap<String, String>();

		String goolClass = pclass.getPackageName() + "." + pclass.getName();

		ArrayList<String> goolClassImplems = new ArrayList<String>();
		for (String Import : GeneratorMatcher.matchImports(goolClass))
			if (Import.startsWith("+"))
				goolClassImplems.add(Import.substring(1));

		for (String goolClassImplem : goolClassImplems) {
			int dotIndex = goolClassImplem.lastIndexOf(".");
			String goolClassImplemName = goolClassImplem;
			String goolClassImplemPackage = "";
			if (dotIndex != -1){
				goolClassImplemName = goolClassImplem.substring(dotIndex + 1);	
				goolClassImplemPackage = goolClassImplem.substring(0, dotIndex);
			}
			Log.d("<CppCodePrinter - printGoolLibraryClass2Strings> " + goolClassImplemName);
			Log.d("<CppCodePrinter - printGoolLibraryClass> " + goolClassImplemPackage);

			String implemFileName = goolClassImplemName+".cpp";
			String headerFileName = goolClassImplemName+".h";
			String codeImplem = GeneratorMatcher.matchGoolClassImplementation(
					goolClass, implemFileName);
			String codeHeader = GeneratorMatcher.matchGoolClassImplementation(
					goolClass, headerFileName);

			String outputDir = ""; 
			if (!getOutputDir().getName().isEmpty()){
				if (dotIndex != -1){
					outputDir = getOutputDir().getAbsolutePath() + 
							StringUtils.replace(goolClassImplemPackage, ".",
									File.separator) + File.separator;
				}else{
					outputDir = getOutputDir().getAbsolutePath() + File.separator;
				}
			}

			if (codeImplem != null){
				result.put(outputDir + implemFileName, codeImplem);
			}
			if (codeHeader != null){
				result.put(outputDir + headerFileName, codeHeader);
			}
		}
		printedClasses.add(pclass);
		return result;
	}

	@Override
	public String getFileName(String className) {
		return className + ".cpp";
	}
}
