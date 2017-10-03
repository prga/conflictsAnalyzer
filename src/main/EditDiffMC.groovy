/**
 * @author paolaaccioly
 *
 */
package main

import de.ovgu.cide.fstgen.ast.FSTTerminal



class EditDiffMC extends ConflictPredictor{

	public EditDiffMC(FSTTerminal n, String msp, String filePath){

		super(n, msp, filePath)
	}

	@Override
	public void setDiffSpacing(){
		this.diffSpacing = false
		String [] nodeBodyWithoutSpacing = this.getNodeWithoutSpacing()
		String [] splitNodeBody = this.splitNodeBody().clone()
		if(this.leftOrRight.equals('left') && nodeBodyWithoutSpacing[0].equals(nodeBodyWithoutSpacing[1])){
			this.diffSpacing = true
			this.node.body = splitNodeBody[2]
		}else if(this.leftOrRight.equals('right') && nodeBodyWithoutSpacing[2].equals(nodeBodyWithoutSpacing[1])){
			this.diffSpacing = true
			this.node.body = splitNodeBody[0]
		}

	}

	@Override
	public void setLeftOrRight(){
		String [] splitNodeBody = this.splitNodeBody().clone()
		if(splitNodeBody[0].equals(splitNodeBody[1])){
			this.leftOrRight = 'right'
		}else{
			this.leftOrRight = 'left'
		}
	}

	public static void main(String[] args){
		//String method ='public\n int\n sub\n (int a, int b) {\n int result = 0;\n int sub = a-b;\n if(sub>0){\n result = sub;\n }\n return result;\n}\n//comment1\n//comment2'
		//println method

	}
}
