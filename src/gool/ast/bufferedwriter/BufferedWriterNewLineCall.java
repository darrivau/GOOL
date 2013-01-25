package gool.ast.bufferedwriter;

import gool.ast.constructs.BufferedWriterMethCall;
import gool.ast.constructs.Expression;
import gool.ast.type.TypeVoid;
import gool.generator.GoolGeneratorController;

public class BufferedWriterNewLineCall extends BufferedWriterMethCall {

	public BufferedWriterNewLineCall (Expression target) {
		super(TypeVoid.INSTANCE, target);
	}
	
	@Override
	public String callGetCode() {
		return GoolGeneratorController.generator().getCode(this);		
	}

}