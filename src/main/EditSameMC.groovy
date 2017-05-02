package main

import java.util.List

import java.io.File


import de.ovgu.cide.fstgen.ast.FSTNode
import de.ovgu.cide.fstgen.ast.FSTNonTerminal
import de.ovgu.cide.fstgen.ast.FSTTerminal
import util.Util

class EditSameMC extends ConflictPredictor{
	
	public EditSameMC(FSTTerminal n, String path, String filePath){
		super(n, path, filePath)
		
	}

}
