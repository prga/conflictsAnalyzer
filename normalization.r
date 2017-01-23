deleteAllFiles <- function(exportPath) {
    
    fileToRemove = paste(exportPath, "normalizationResults.html", sep="")
    if (file.exists(fileToRemove)) {
        file.remove(fileToRemove)
    }
    
    
}

main<-function(){
    importPath = "/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/"
    exportPath = "/Users/paolaaccioly/Dropbox/Public/normalization/"
    
    normalizationFile = "NormalizedData.csv"
    
    conflicts = "projectsPatternData.csv"
    
    #HTML file
    htmlFile = paste(exportPath, "normalizationResults.html", sep="")
    
    #delete previous files
    deleteAllFiles(exportPath)
    
    #read conflicts table
    projectPatterns = read.table(file=paste(importPath, conflicts, sep=""), header=T, sep=",")
    
    #read and edit norm table
    normalizationTable = read.table(file=paste(importPath, normalizationFile, sep=""), header=T, sep=",")
    
    #metrics
    normalizationTableWT <- head(normalizationTable, -1)
    
    #boxplot all conflicts
    boxplotNodes = paste("boxplotNodes.png")
    png(paste(exportPath, boxplotNodes, sep=""))
    normalizationTableWT <- head(normalizationTable, -1)
    tableNodes <- data.frame(normalizationTableWT$nEditSameMC, normalizationTableWT$nSameSignatureCM, normalizationTableWT$nEditSameFd,
                             normalizationTableWT$nAddSameFd, normalizationTableWT$nModifierList, normalizationTableWT$nExtendsList, 
                             normalizationTableWT$nEditSameEnumConst, normalizationTableWT$nImplementList)
    colnames(tableNodes) <- c("EditSameMC","SameSignatureMC", "EditSameFd",  "AddSameFd", "ModifierList", "ExtendsList", "EditSameEnumConst", "ImplementList")
    op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
    boxplot(tableNodes, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0",  outline = FALSE)
    par(op)
    dev.off()
    
    #boxplot considering chunks on editsamemc
    boxplotNodesChunks = paste("boxplotNodesChunks.png")
    png(paste(exportPath, boxplotNodesChunks, sep=""))
    normalizationTableWT <- head(normalizationTable, -1)
    tableNodes <- data.frame(normalizationTableWT$nEditSameMCChunks, normalizationTableWT$nSameSignatureCM, normalizationTableWT$nEditSameFd,
                             normalizationTableWT$nAddSameFd, normalizationTableWT$nModifierList, normalizationTableWT$nExtendsList, 
                             normalizationTableWT$nEditSameEnumConst, normalizationTableWT$nImplementList)
    colnames(tableNodes) <- c("EditSameMC","SameSignatureMC", "EditSameFd",  "AddSameFd", "ModifierList", "ExtendsList", "EditSameEnumConst", "ImplementsList")
    op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
    boxplot(tableNodes, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0",  outline = FALSE)
    par(op)
    dev.off()
    
    #boxplot considering lines on editsamemc
    boxplotNodesLines = paste("boxplotNodesLines.png")
    png(paste(exportPath, boxplotNodesLines, sep=""))
    tableLines <- data.frame(normalizationTableWT$nEditSameMCLines, normalizationTableWT$nSameSignatureCM, normalizationTableWT$nEditSameFd,
                             normalizationTableWT$nAddSameFd, normalizationTableWT$nModifierList, normalizationTableWT$nExtendsList, 
                             normalizationTableWT$nEditSameEnumConst, normalizationTableWT$nImplementList)
    colnames(tableLines) <- c("EditSameMC","SameSignatureMC", "EditSameFd",  "AddSameFd", "ModifiersList", "ExtendsList", "EditSameEnumConst", "ImplementsList")
    op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
    boxplot(tableLines, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0",  outline = FALSE)
    par(op)
    dev.off()
    
    #boxplot considering three computations of EditSameMC
    boxplotNodesEditSameMC = paste("boxplotNodesEditSameMC.png")
    png(paste(exportPath, boxplotNodesEditSameMC, sep=""))
    tableNodes <- data.frame(normalizationTableWT$nEditSameMCLines, normalizationTableWT$nEditSameMCChunks, normalizationTableWT$nEditSameMC)
    colnames(tableNodes) <- c("EditSameMCLine","EditSameMCChunk", "EditSameMCNode")
    op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
    boxplot(tableNodes, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0",  outline = FALSE)
    par(op)
    dev.off()
    
    #boxplot considering conflicts without normalization
    boxplotsConfNorm = paste("boxplotsConfNorm.png")
    png(paste(exportPath, boxplotsConfNorm, sep=""))
    conflictsTable <- data.frame(projectPatterns$EditSameMC, projectPatterns$SameSignatureCM, projectPatterns$EditSameFd, 
                                 projectPatterns$AddSameFd, projectPatterns$ModifierList, projectPatterns$ExtendsList,
                                 projectPatterns$EditSameEnumConst, projectPatterns$ImplementList)
    colnames(conflictsTable) <- c("EditSameMC","SameSignatureMC", "EditSameFd",  "AddSameFd", "ModifiersList", "ExtendsList", "EditSameEnumConst", "ImplementsList")
    op <- par(mar = c(4, 9, 1, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1 bottom, left, top, right
    par(mfrow=c(2,1))
    boxplot(tableLines, col="gray", xlab="Normalized number of conflicts", horizontal = TRUE, las=1, cex.axis=1.0, bw="nrd0", outline=FALSE)
    boxplot(conflictsTable, col="gray",  xlab="Number of conflicts", horizontal = TRUE, las=1, cex.axis=1.0, bw="nrd0", outline=FALSE)
    par(op)
    dev.off()
    
    #HTML code
    library(R2HTML)
    
    title = paste("<hr><h1>Results for Conflict Normalization</h1>", sep="")
    HTML("<link rel=stylesheet type=text/css href=R2HTML.css>", file=htmlFile, append=TRUE)
    HTML.title(title, file=htmlFile, append=TRUE)
    
    HTML("<hr><h2>Normalization Table</h2>", file=htmlFile, append=TRUE)
    HTML(normalizationTable, file=htmlFile, append=TRUE)
    #HTML(metrics, file=htmlFile, append=TRUE)
    
    HTML("<hr><h2>Conflicts Normalized by the Number of Changed Nodes</h2>", file=htmlFile, append=TRUE)
    HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotNodes, Align="center", append=TRUE)
    
    HTML("<hr><h2>Conflicts Normalized by the Number of Changed Nodes and Chunks for EditSameMC</h2>", file=htmlFile, append=TRUE)
    HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotNodesChunks, Align="center", append=TRUE)
    
    HTML("<hr><h2>Conflicts Normalized by the Number of Changed Nodes and Lines for EditSameMC</h2>", file=htmlFile, append=TRUE)
    HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotNodesLines, Align="center", append=TRUE)
    
    HTML("<hr><h2>Comparing EditSameMC normalization (Lines, Chunks, and Nodes)</h2>", file=htmlFile, append=TRUE)
    HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotNodesEditSameMC, Align="center", append=TRUE)
    
    HTML("<hr><h2>Conflicts Normalized by the Number of Changed Nodes and Lines for EditSameMC vs. Conflicts Occurences</h2>", file=htmlFile, append=TRUE)
    HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotsConfNorm, Align="center", append=TRUE)
}

main()