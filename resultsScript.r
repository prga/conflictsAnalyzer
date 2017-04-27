#to do list:
#place new column with conflict rate percentage

# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  library(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

diffConflictRateFunc <- function(conflictRate, conflictRateWithoutFP){
  diffConflictRate <- c()
  
  numberOfRows <- length(conflictRateWithoutFP)
  
  for(i in 1:numberOfRows){
    
    if(conflictRate[i]==0){
      diff = 0
    } else{
      diff = (1- (conflictRateWithoutFP[i]/conflictRate[i]))*100
    }
    diffConflictRate  <- append(diffConflictRate, diff)
    
  }  
  return(diffConflictRate)
}

computePatternPercentages <- function(conflicts, patternName){
  
  patternPercentages <- c()
  
  numberOfRows <- nrow(conflicts)
  ds <- paste(patternName, "DS", sep="")
  cl <- paste(patternName, "CL", sep="")
  ifp <- paste(patternName, "IFP", sep="")
  
  for(i in 1:numberOfRows){
    sumConflicts <- 0
    diffSpacing <- 0
    consecLines <- 0
    intersection <- 0
    
    indexes <- c(4,8,12,16,20,24,28,32)
    
    for(j in indexes){
      sumConflicts <- sum(sumConflicts, conflicts[i,j])
      diffSpacing <- sum(diffSpacing, conflicts[i,j+1])
      consecLines <- sum(consecLines, conflicts[i,j+2])
      intersection <- sum(intersection, conflicts[i,j+3])
    }
    realSumConflicts = sumConflicts - diffSpacing - consecLines + intersection
    value <- conflicts[i, patternName]
    valueDS <- conflicts[i, ds]
    valueCL <- conflicts[i, cl]
    valueIFP <- conflicts[i, ifp]
    realValue = value - valueDS - valueCL + valueIFP
    
    if(realSumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (realValue/realSumConflicts)*100
    }
    
    patternPercentages  <- append(patternPercentages, percentage)
    
  }
  return(patternPercentages)
}

computeSameSignatureCausesPercentages <- function(conflicts, causeName){
  
  causePercentages <- c()
  ds <- paste(causeName, "DS", sep="")
  
  numberOfRows <- nrow(conflicts)
  
  for(i in 1:numberOfRows){
    sumCauses <- 0
    sumCausesDS <- 0

    indexes <- c(37, 39, 41, 43, 45)
    
    for(j in indexes){
      sumCauses <- sum(sumCauses, conflicts[i,j])
      sumCausesDS <- sum(sumCausesDS, conflicts[i,j+1])

    }
    
    realSumCauses <- sumCauses - sumCausesDS
    causeValue <- conflicts[i, causeName]
    causeValueDS <- conflicts[i, ds]
    realCauseValue = causeValue - causeValueDS
    
    
    if(realSumCauses == 0){
      percentage <- 0
    }else{
      percentage <- (realCauseValue/realSumCauses)*100
    }
    
    causePercentages  <- append(causePercentages, percentage)
    
  }
  return(causePercentages)
}

computeEditSameMCFPPercentages <- function(conflicts, editSameMethodCause){
  
  editSameMethodPercentages <- c()
  numberOfRows <- nrow(conflicts)

  for(i in 1:numberOfRows){
    sumConflicts <- conflicts[i,16]
    valueDS <- conflicts[i,17]
    valueCL <- conflicts[i,18]
    valueIFP <- conflicts[i,19]

    if(editSameMethodCause == "EditSameMC"){ 
      value <- sumConflicts - valueDS - valueCL + valueIFP
    }else if(editSameMethodCause == "EditSameMCDS"){
      value <- valueDS - valueIFP
    }else if(editSameMethodCause == "EditSameMCCL"){
      value <- valueCL - valueIFP
    }else if(editSameMethodCause == "EditSameMCIFP"){
      value <- valueIFP
    }
    
    if(sumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (value/sumConflicts)*100
    }
    
    editSameMethodPercentages  <- append(editSameMethodPercentages, percentage)
    
  }
  return(editSameMethodPercentages)
}

dataFrameEditSameMCFPPercentages <- function(conflicts){
  Possible.conflicts <- computeEditSameMCFPPercentages(conflicts, "EditSameMC")
  Different.identation <- computeEditSameMCFPPercentages(conflicts, "EditSameMCDS")
  Consecutive.lines <- computeEditSameMCFPPercentages(conflicts, "EditSameMCCL")
  Intersection <- computeEditSameMCFPPercentages(conflicts, "EditSameMCIFP")
  
  result <- data.frame(Possible.conflicts, Different.identation, Consecutive.lines, Intersection)
  return(result)
}

computeSameSignatureFPPercentages <- function(conflicts, sameSignatureCause){
  
  sameSignaturePercentages <- c()
  numberOfRows <- nrow(conflicts)
  
  for(i in 1:numberOfRows){
    sumConflicts <- conflicts[i,28]
    valueDS <- conflicts[i,29]
    
    if(sameSignatureCause == "SameSignatureCM"){ 
      value <- sumConflicts - valueDS
    }else if(sameSignatureCause == "SameSignatureCMDS"){
      value <- valueDS
    }
    
    if(sumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (value/sumConflicts)*100
    }
    
    sameSignaturePercentages  <- append(sameSignaturePercentages, percentage)
    
  }
  return(sameSignaturePercentages)
}

dataFrameSameSignatureFPPercentages <- function(conflicts){
  Possible.conflicts <- computeSameSignatureFPPercentages(conflicts, "SameSignatureCM")
  Different.identation <- computeSameSignatureFPPercentages(conflicts, "SameSignatureCMDS")
  
  result <- data.frame(Possible.conflicts, Different.identation)
  return(result)
}

deleteAllFiles <- function(exportPath) {
  
  fileToRemove = paste(exportPath, "conflictResults.html", sep="")
  if (file.exists(fileToRemove)) {
    file.remove(fileToRemove)
  }
  

}

updateColumn <- function(row1, row2) {
  result <- c()
  nrows <- length(row1)
  
  for(i in 1:nrows){
    sum = row1[i] + row2[i]
    result <- append(result, sum)
  }
  
  return (result)
}

main<-function(){
importPath = "/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/conflictsAnalyzer/"
exportPath = "/Users/paolaaccioly/Documents/Doutorado/workspace_empirical/graphs/"

conflictRateFile="projectsPatternData.csv"
realConflictRateFile = "realConflictRate.csv"
conflictRateNonJavaFiles = "ConflictingScenarios.csv"


#HTML file
htmlFile = paste(exportPath, "conflictResults.html", sep="")

#delete previous files
deleteAllFiles(exportPath)

#read conflictRateNonJava table
conflictRateNonJava = read.table(file=paste(importPath, conflictRateNonJavaFiles, sep=""), header=T, sep=";")
sumNonJavaJava <- updateColumn(conflictRateNonJava$MCNonJavaMinusMCJava, conflictRateNonJava$MCJava)
sumNonJavaJavaWFP <- updateColumn(conflictRateNonJava$MCNonJavaMinusMCJavaWFP, conflictRateNonJava$MCJavaWFP)
sumNonJavaJavaWDS <- updateColumn(conflictRateNonJava$MCNonJavaMinusMCJavaWDS, conflictRateNonJava$MCJavaWDS)
sumNonJavaJavaWCL <- updateColumn(conflictRateNonJava$MCNonJavaMinusMCJavaWCL, conflictRateNonJava$MCJavaWCL)
a <- data.frame(conflictRateNonJava$ProjectName, conflictRateNonJava$TotalMC, sumNonJavaJava, sumNonJavaJavaWFP,sumNonJavaJavaWDS,sumNonJavaJavaWCL)
colnames(a) <- c("Project", "Merge_Scenarios", "Conflicting_Scenarios", "Conflicting_Scenarios_WFP", "Conflicting_Scenarios_WDS", "Conflicting_Scenarios_WCL")
sumMerge <- sum(a$Merge_Scenarios)
sumConflict <- sum(a$Conflicting_Scenarios)
sumConflictWFP <- sum (a$Conflicting_Scenarios_WFP)
sumConflictWDS <- sum (a$Conflicting_Scenarios_WDS)
sumConflictWCL <- sum (a$Conflicting_Scenarios_WCL)
geral <-data.frame(Project="TOTAL", Merge_Scenarios=sumMerge, 
                   Conflicting_Scenarios=sumConflict, Conflicting_Scenarios_WFP=sumConflictWFP,
                   Conflicting_Scenarios_WDS=sumConflictWDS, Conflicting_Scenarios_WCL=sumConflictWCL)
cRNonJavaTable <- rbind(a, geral)
cRNonJavaTable["Conflict_Rate"] <- (cRNonJavaTable$Conflicting_Scenarios/cRNonJavaTable$Merge_Scenarios)*100
cRNonJavaTable["Conflict_Rate_WFP"] <- (cRNonJavaTable$Conflicting_Scenarios_WFP/cRNonJavaTable$Merge_Scenarios)*100
cRNonJavaTable["Conflict_Rate_WDS"] <- (cRNonJavaTable$Conflicting_Scenarios_WDS/cRNonJavaTable$Merge_Scenarios)*100
cRNonJavaTable["Conflict_Rate_WCL"] <- (cRNonJavaTable$Conflicting_Scenarios_WCL/cRNonJavaTable$Merge_Scenarios)*100
attach(cRNonJavaTable)

#tables
tableNonJava <- head(cRNonJavaTable, -1)
MeanNonJava <- mean(tableNonJava$Conflict_Rate)
Standard.deviationNonJava <- sd(tableNonJava$Conflict_Rate)
metricsNonJava <- data.frame(MeanNonJava, Standard.deviationNonJava)


MeanNonJavaWFP <- mean(tableNonJava$Conflict_Rate_WFP)
Standard.deviationNonJavaWFP <- sd(tableNonJava$Conflict_Rate_WFP)
metricsNonJavaWFP <- data.frame(MeanNonJavaWFP, Standard.deviationNonJavaWFP)

MeanNonJavaWDS <- mean(tableNonJava$Conflict_Rate_WDS)
Standard.deviationNonJavaWDS <- sd(tableNonJava$Conflict_Rate_WDS)
metricsNonJavaWDS <- data.frame(MeanNonJavaWDS, Standard.deviationNonJavaWDS)

MeanNonJavaWCL <- mean(tableNonJava$Conflict_Rate_WCL)
Standard.deviationNonJavaWCL <- sd(tableNonJava$Conflict_Rate_WCL)
metricsNonJavaWCL <- data.frame(MeanNonJavaWCL, Standard.deviationNonJavaWCL)

library(beanplot)

#beanplots nonJava e nonJavaWFP
dataConflictNonJava <-data.frame(tableNonJava$Conflict_Rate, tableNonJava$Conflict_Rate_WFP)
colnames(dataConflictNonJava) <- c("Conflict_Rate", "Conflict_Rate_WFP")
beanPlotCRNonJavaFileName = paste("beanPlotCRNonJava.png")
png(paste(exportPath, beanPlotCRNonJavaFileName, sep=""))
beanplot(dataConflictNonJava,  ylab="Conflicting Scenarios %",col="green", cex=1.5,  bw="nrd0")
dev.off()

#read and edit conflict rate table
conflictRateTemp = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T, sep=",")
conflictRate2 = data.frame(conflictRateTemp$Project, conflictRateTemp$Merge_Scenarios, 
                           conflictRateTemp$Conflicting_Scenarios)
colnames(conflictRate2) <- c("Projects", "Merge_Scenarios", "Conflicting_Scenarios")
sumMergeScenarios = sum(conflictRate2$Merge_Scenarios)
sumConflictionScenarios = sum(conflictRate2$Conflicting_Scenarios)
total = data.frame(Projects="TOTAL", Merge_Scenarios=sumMergeScenarios, 
                   Conflicting_Scenarios=sumConflictionScenarios)
conflictRate = rbind(conflictRate2, total)

conflictRate["Conflict_Rate(%)"] <- (conflictRate$Conflicting_Scenarios/conflictRate$Merge_Scenarios)*100
attach(conflictRate)

newTable <- head(conflictRate, -1)
Mean <- mean(newTable$Conflict_Rate)
Standard.deviation <- sd(newTable$Conflict_Rate)
metrics <- data.frame(Mean, Standard.deviation)

#read and edit real conflict rate table
realConflictRateFileTemp = read.table(file=paste(importPath,realConflictRateFile , sep=""), header=T, sep=",")
realconflictRate2 = data.frame(realConflictRateFileTemp$Project, realConflictRateFileTemp$Merge.Scenarios, 
                                realConflictRateFileTemp$Conflicting.Scenarios)
 colnames(realconflictRate2) <- c("Project", "Merge.Scenarios", "Conflicting.Scenarios")
 realsumMergeScenarios = sum(realconflictRate2$Merge.Scenarios)
 realsumConflictingScenarios = sum(realconflictRate2$Conflicting.Scenarios)
 realtotal = data.frame(Project="TOTAL", Merge.Scenarios=realsumMergeScenarios,
                        Conflicting.Scenarios=realsumConflictingScenarios)
 realconflictRate = rbind(realconflictRate2, realtotal)

 realconflictRate["Conflict.Rate(%)"] <- 
  (realconflictRate$Conflicting.Scenarios/realconflictRate$Merge.Scenarios)*100
 attach(realconflictRate)

realNewTable <- head(realconflictRate, -1)
Mean <- mean(realNewTable$Conflict.Rate)
Standard.deviation <- sd(realNewTable$Conflict.Rate)
realMetrics <- data.frame(Mean, Standard.deviation)

#beanplot conflicting rate


#boxplot conflicting rate with and without false positives
boxplotCRFileName = paste("BoxplotCR.png")
png(paste(exportPath, boxplotCRFileName, sep=""))
conflictRateWFP <- realconflictRate
dataConflict <-data.frame(conflictRate$Conflict_Rate, conflictRateWFP$Conflict.Rate)
colnames(dataConflict) <- c("CS", "CS without spacing and consecutive lines conflicts")
boxplot(dataConflict, ylab="Conflicting Scenarios %",col="green")
dev.off()

#beanplot conflicting rate with and without false positives

realbeanplotCRFileName = paste("realBeanplotCR.png")
png(paste(exportPath, realbeanplotCRFileName, sep=""))
beanplot(dataConflict,  ylab="Conflicting Scenarios %",col="green", cex=1.5,  bw="nrd0")
dev.off()


#boxplot diff conflict rates
diffConflictRates <- diffConflictRateFunc(newTable$Conflict_Rate, realNewTable$Conflict.Rate)
boxplotDiffCR = paste("boxplotDiffCR.png")
png(paste(exportPath, boxplotDiffCR, sep=""))
boxplot(diffConflictRates, xlab="Projects", ylab="Difference of Conflict Rates %",col="green")
dev.off()

diffConflictRatesTable <- data.frame(mean(diffConflictRates), sd(diffConflictRates) )
colnames(diffConflictRatesTable) <- c("Mean", "Standard Deviation")

#beanplot diff conflict rates
beanplotDiffCR = paste("beanplotDiffCR.png")
png(paste(exportPath, beanplotDiffCR, sep=""))
beanplot(diffConflictRates, xlab="Projects", ylab="Difference of Conflict Rates %",col="green", bw="nrd0")
dev.off()

#read conflict patterns values 
DefaultValueAnnotation <- sum(conflictRateTemp$DefaultValueAnnotation)
ImplementList <- sum(conflictRateTemp$ImplementList)
ModifierList <- sum(conflictRateTemp$ModifierList)
EditSameMC <- sum(conflictRateTemp$EditSameMC)
SameSignatureCM <- sum(conflictRateTemp$SameSignatureCM)
AddSameFd <- sum(conflictRateTemp$AddSameFd)
EditSameFd <- sum(conflictRateTemp$EditSameFd)
ExtendsList <- sum(conflictRateTemp$ExtendsList)
EditSameEnumConst <- sum(conflictRateTemp$EditSameEnumConst)
TypeParametersList <- sum(conflictRateTemp$TypeParametersList)
# bar plot all conflicts

slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, 
            EditSameFd, ExtendsList, EditSameEnumConst, TypeParametersList)
labels <- c("DefaultValueA", "ImplementsList", "ModifierList", "EditSameMC", "SameSignatureMC", "AddSameFd", 
            "EditSameFd", "ExtendsList", "EditSameEnumConst", "TypeParametersList")
dat <- data.frame(Frequency = slices,Conflicts = labels)
dat$Conflicts <- reorder(dat$Conflicts, dat$Frequency)
library(ggplot2)
fstMerge <- ggplot(dat, aes(y = Frequency)) +
  geom_bar(aes(x = Conflicts),stat = "identity",fill="black", colour="black") +
  geom_text(aes(x = Conflicts, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 15) + labs(x=NULL, y=NULL)  + ylim(c(0,27000)) + ggtitle("FSTMerge") +  labs(y="Number of occurrences")

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", 
                        "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList", "EditSameEnumConst", "TypeParametersList" ,"TOTAL")
conflictsSum <- sum(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,
                    AddSameFd, EditSameFd, ExtendsList, EditSameEnumConst, TypeParametersList)
Occurrences <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,
                 AddSameFd, EditSameFd, ExtendsList, EditSameEnumConst, TypeParametersList,conflictsSum)
conflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#boxplot for each conflict pattern percentages along all projects

EditSameMCpercentages <- computePatternPercentages(conflictRateTemp, "EditSameMC")


#false positives EditSameMC
BarPlotESMCFP = paste("BarPlotESMCFP.png")
png(paste(exportPath, BarPlotESMCFP, sep=""))
sumEditSameMCDS = sum(conflictRateTemp$EditSameMCDS)
sumEditSameMCCL = sum(conflictRateTemp$EditSameMCCL)
sumEditSameMCIFP = sum(conflictRateTemp$EditSameMCIFP)
realEditSameMC = EditSameMC - sumEditSameMCDS - sumEditSameMCCL + sumEditSameMCIFP
EditSameMCDS = sumEditSameMCDS - sumEditSameMCIFP
EditSameMCCL = sumEditSameMCCL - sumEditSameMCIFP

Frequency <- c(realEditSameMC, EditSameMCCL, EditSameMCDS, sumEditSameMCIFP)
Causes <- c("Possibly Real Conflicts", "Consecutive Lines", "Different Spacing", "CL and DS" )

df <- data.frame(Frequency, Causes)
df$Causes <- reorder(df$Causes, df$Frequency)
bp <- ggplot(df, aes(y = Frequency)) +
  geom_bar(aes(x = Causes),stat = "identity",fill="green", colour="black", width=.8) +
  geom_text(aes(x = Causes, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 13) + labs(x=NULL, y=NULL) + ylim(c(0,18000))

print(bp)
dev.off()

#boxplot with the editSameMC cause percentages
BoxplotFPEditSameMC = paste("BoxplotFPEditSameMC.png")
png(paste(exportPath, BoxplotFPEditSameMC, sep=""))
allEditSameMCFPPercentages <- dataFrameEditSameMCFPPercentages(conflictRateTemp)
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1 bottom, left, top and right
boxplot(allEditSameMCFPPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()



SameSignatureCMpercentages <- computePatternPercentages(conflictRateTemp, "SameSignatureCM")


#false positives SameSignatureCM
BarPlotSSCMFP = paste("BarPlotSSCMFP.png")
png(paste(exportPath, BarPlotSSCMFP, sep=""))
sumSameSignatureMCDS = sum(conflictRateTemp$SameSignatureCMDS)
realSameSignatureMC = SameSignatureCM - sumSameSignatureMCDS

Frequency <- c(realSameSignatureMC, sumSameSignatureMCDS)
Causes <- c("Possibly Real Conflicts", "Different Spacing")
df <- data.frame(Frequency, Causes)
df$Causes <- reorder(df$Causes, df$Frequency)
bp <- ggplot(df, aes(y = Frequency)) +
  geom_bar(aes(x = Causes),stat = "identity",fill="green", colour="black", width=.8) +
  geom_text(aes(x = Causes, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 13) + labs(x=NULL, y=NULL) + ylim(c(0,3300))
print(bp)
dev.off()

#boxplot with the sameSignatureCM cause percentages
BoxplotFPSameSigCM = paste("BoxplotFPSameSigCM.png")
png(paste(exportPath, BoxplotFPSameSigCM, sep=""))
allSameSigPercentages <- dataFrameSameSignatureFPPercentages(conflictRateTemp)
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allSameSigPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()

#bar plot without false positives
realDefaultValueAnnotation <- sum(conflictRateTemp$DefaultValueAnnotation) - 
  sum(conflictRateTemp$DefaultValueAnnotationDS) - sum(conflictRateTemp$DefaultValueAnnotationCS) + 
  sum(conflictRateTemp$DefaultValueAnnotationIFP)
realImplementList <- sum(conflictRateTemp$ImplementList) - sum(conflictRateTemp$ImplementListDS) - 
  sum(conflictRateTemp$ImplementListCL) + sum(conflictRateTemp$ImplementListIFP)
realModifierList <- sum(conflictRateTemp$ModifierList) - sum(conflictRateTemp$ModifierListDS) - 
  sum(conflictRateTemp$ModifierListCL) + sum(conflictRateTemp$ModifierListIFP)
realAddSameFd <- sum(conflictRateTemp$AddSameFd) - sum(conflictRateTemp$AddSameFdDS) - 
  sum(conflictRateTemp$AddSameFdCL) + sum(conflictRateTemp$AddSameFdIFP)
realEditSameFd <- sum(conflictRateTemp$EditSameFd) - sum(conflictRateTemp$EditSameFdDS) - 
  sum(conflictRateTemp$EditSameFdCL) + sum(conflictRateTemp$EditSameFdIFP)
realExtendsList <- sum(conflictRateTemp$ExtendsList) - sum(conflictRateTemp$ExtendsListDS) - 
  sum(conflictRateTemp$ExtendsListCL) + sum(conflictRateTemp$ExtendsListIFP)
realEditSameEnumConst <- sum(conflictRateTemp$EditSameEnumConst) - sum(conflictRateTemp$EditSameEnumConstDS) -
  sum(conflictRateTemp$EditSameEnumConstCL) + sum(conflictRateTemp$EditSameEnumConstIFP)
realTypeParametersList <- sum(conflictRateTemp$TypeParametersList) - sum(conflictRateTemp$TypeParametersListDS) - 
  sum(conflictRateTemp$TypeParametersListCL) + sum(conflictRateTemp$TypeParametersListIFP)

barChartFP = paste("barChartFP.png")
png(paste(exportPath, barChartFP, sep=""))
slices <- c(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
            realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList, realEditSameEnumConst, realTypeParametersList)
labels <- c("DefaultValueA", "ImplementsList", "ModifierList", "EditSameMC", "SameSignatureMC", 
            "AddSameFd", "EditSameFd", "ExtendsList", "EditSameEnumConst", "TypeParametersList")
dat2 <- data.frame(Conflicts = labels, Frequency = slices)
dat2$Conflicts <- reorder(dat2$Conflicts, dat2$Frequency)
fstMergeWFP <- ggplot(dat2, aes(y = Frequency)) +
  geom_bar(aes(x = Conflicts),stat = "identity", fill="black", colour="black") +
  geom_text(aes(x = Conflicts, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) +
  coord_flip() + theme_grey(base_size = 15) + labs(x=NULL, y=NULL) + ylim(c(0,27000)) + 
  ggtitle("FSTMerge without potential false positives") +  labs(y="Number of occurrences")

p3 <- multiplot(fstMerge,fstMergeWFP)
print(p3)
dev.off()

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", 
                        "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList", "EditSameEnumConst","TypeParametersList" ,"TOTAL")
conflictsSum <- sum(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
                    realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList, realEditSameEnumConst, realTypeParametersList)
Occurrences <- c(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
                 realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList, realEditSameEnumConst, realTypeParametersList, conflictsSum)
realconflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#causes for SameSignatureCM
BoxplotCSSCM = paste("CausesSameSignatureCM.png")
png(paste(exportPath, BoxplotCSSCM, sep=""))

sumSmallMethod = round(((sum(conflictRateTemp$smallMethod) - sum(conflictRateTemp$smallMethodDS))/
                          realSameSignatureMC)*100, digit=1)

sumRenamedMethod = round(((sum(conflictRateTemp$renamedMethod) - sum(conflictRateTemp$renamedMethodDS))/
  realSameSignatureMC)*100, digit=1)

sumCopiedMethod= round(((sum(conflictRateTemp$copiedMethod) - sum(conflictRateTemp$copiedMethodDS))/
  realSameSignatureMC)*100, digit=1)

sumCopiedFile = round(((sum(conflictRateTemp$copiedFile) - sum(conflictRateTemp$copiedFileDS))/
                         realSameSignatureMC)*100, digit=1)

sumNoPattern = round(((sum(conflictRateTemp$noPattern) - sum(conflictRateTemp$noPatternDS))/
                        realSameSignatureMC)*100, digit=1)

Frequency <- c(sumSmallMethod, sumRenamedMethod, sumCopiedMethod, sumCopiedFile, sumNoPattern)
Causes <- c("Small methods", "Renamed Methods", "Copied Methods", "Copied Files",
            "Others")
df <- data.frame(Frequency, Causes)
df$Causes <- reorder(df$Causes, df$Frequency)
p <- ggplot(df, aes(y = Frequency)) +
  geom_bar(aes(x = Causes),stat = "identity",fill="black", colour="black") +
  geom_text(aes(x = Causes, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 15) + ylim(c(0,100)) + labs(x="Causes", y="Aggregated percentages (%)") 
  

print(p)
dev.off()

#boxplot with the samesignaturecm cause percentages
BoxplotAllCauses = paste("BoxplotAllCauses.png")
png(paste(exportPath, BoxplotAllCauses, sep=""))
smallMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "smallMethod")
renamedMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "renamedMethod")
copiedMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "copiedMethod")
copiedFile <- computeSameSignatureCausesPercentages(conflictRateTemp, "copiedFile")
noPattern <- computeSameSignatureCausesPercentages(conflictRateTemp, "noPattern")
allCausesPercentages <- data.frame(smallMethod, renamedMethod, copiedMethod,
                                   copiedFile, noPattern )
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allCausesPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1)
par(op)
dev.off()

ImplementListpercentages <- computePatternPercentages(conflictRateTemp, "ImplementList")

ModifierListpercentages <- computePatternPercentages(conflictRateTemp, "ModifierList")

AddSameFdpercentages <- computePatternPercentages(conflictRateTemp, "AddSameFd")

EditSameFdpercentages <- computePatternPercentages(conflictRateTemp, "EditSameFd")

DefaultValueAnnotationpercentages <- computePatternPercentages(conflictRateTemp, "DefaultValueAnnotation")

ExtendsListpercentages <- computePatternPercentages(conflictRateTemp, "ExtendsList")

EditSameEnumConstpercentages <- computePatternPercentages(conflictRateTemp, "EditSameEnumConst")

TypeParametersListpercentages <- computePatternPercentages(conflictRateTemp, "TypeParametersList")

#all conflicts percentages beanplot
BeanplotAllConflicts = paste("BeanplotAllConflicts.png")
png(paste(exportPath, BeanplotAllConflicts, sep=""))
EditSameMC <- EditSameMCpercentages
SameSignatureCM <- SameSignatureCMpercentages
ImplementList <- ImplementListpercentages
ModifierList <- ModifierListpercentages
AddSameFd <- AddSameFdpercentages
EditSameFd <- EditSameFdpercentages
DefaultValueA <- DefaultValueAnnotationpercentages
ExtendsList <- ExtendsListpercentages
EditSameEnumConst <- EditSameEnumConstpercentages
TypeParametersList <- TypeParametersListpercentages
allConflictsPercentage <- data.frame(EditSameMC, SameSignatureCM, 
                                     ImplementList, ModifierList, 
                                     AddSameFd, EditSameFd, 
                                     DefaultValueA, ExtendsList, EditSameEnumConst, TypeParametersList)
colnames(allConflictsPercentage) <- c("EditSameMC","SameSignatureMC", "ImplementsList", "ModifierList", 
                                      "AddSameFd", "EditSameFd", "DefaultValueA", "ExtendsList", "EditSameEnumConst", "TypeParametersList")
op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
beanplot(allConflictsPercentage, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0")
par(op)
dev.off()

#all conflicts percentages boxplot
BoxplotAllConflicts = paste("BoxplotAllConflicts.png")
png(paste(exportPath, BoxplotAllConflicts, sep=""))
op <- par(mar = c(4, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allConflictsPercentage, col="gray", xlab="Percentages(%) for each project", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()

#bar plot last project
numberOfRows <- length(conflictRateTemp[,1])
lastProject <- conflictRateTemp[numberOfRows,]
name <- lastProject$Project
DefaultValueAnnotation <- lastProject$DefaultValueAnnotation
ImplementList <- lastProject$ImplementList
ModifierList <- lastProject$ModifierList
EditSameMC <- lastProject$EditSameMC
SameSignatureCM <- lastProject$SameSignatureCM
AddSameFd <- lastProject$AddSameFd
EditSameFd <- lastProject$EditSameFd
ExtendsList <- lastProject$ExtendsList
EditSameEnumConst <- lastProject$EditSameEnumConst
TypeParametersListConst <- lastProject$TypeParametersList
barPlotFileName = paste(name, "BarPlot.png", sep="")
png(paste(exportPath, barPlotFileName, sep=""))
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, 
            EditSameFd, ExtendsList, EditSameEnumConst, TypeParametersList)
labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", 
            "AddSameFd", "EditSameFd", "ExtendsList", "EditSameEnumConst", "TypeParametersList")
par(las=2)
par(mar=c(5,8,4,2))
barplot(slices, main=name, horiz=TRUE, names.arg=labels, cex.names=0.8, col=c("darkviolet","chocolate4", 
                                                                              "darkgreen", "darkblue", "red" ,
                                                                              "darkgoldenrod2"))
dev.off()

#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflicting Scenarios Rate and Conflict Patterns Occurrences</h1>", sep="")
HTML("<link rel=stylesheet type=text/css href=R2HTML.css>", file=htmlFile, append=TRUE)
HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflicting Scenarios Rate With and Without Spacing and Consecutive Lines Edition Conflicts</h2>", file=htmlFile, append=TRUE)
HTML(cRNonJavaTable, file=htmlFile, append=TRUE)
HTML(metricsNonJava, file=htmlFile, append=TRUE)
HTML(metricsNonJavaWFP, file=htmlFile, append=TRUE)
HTML(metricsNonJavaWDS, file=htmlFile, append=TRUE)
HTML(metricsNonJavaWCL, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflicting Scenarios Rate Beanplot and Boxplot with and without spacing and consecutive lines conflicts</h2>", file=htmlFile, 
     append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=beanPlotCRNonJavaFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflicting Scenarios Rate - Just Java Files</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)
HTML(metrics, file=htmlFile, append=TRUE)
HTML("<hr><h2>Conflicting Scenarios Rate Without Spacing and Consecutive Lines Conflicts - Just Java Files</h2>", file=htmlFile, append=TRUE)
HTML(realconflictRate, file=htmlFile, append=TRUE)
HTML(realMetrics, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflicting Scenarios Rate Beanplot and Boxplot with and without spacing and consecutive lines conflicts - Just Java Files</h2>", file=htmlFile, 
     append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=realbeanplotCRFileName, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotCRFileName, Align="center", append=TRUE)

HTML("<hr><h2>Difference of Conflicting Scenarios Rates with and without spacing and consecutive lines conflicts - Just Java Files</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotDiffCR, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=beanplotDiffCR, Align="center", append=TRUE)
HTML(diffConflictRatesTable, file=htmlFile, append=TRUE)


HTML("<hr><h2>Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFP, Align="center", append=TRUE)

HTML("<hr><h2>Conflicts Table With and Without Spacing and Consecutive Lines Conflicts</h2>", file=htmlFile, append=TRUE)
HTML(conflictsTable, file=htmlFile, append=TRUE)
HTML(realconflictsTable, file=htmlFile, append=TRUE)

HTML("<hr><h2>Dispersion of the Conflict Patterns Percentages Across Projects (Without Spacing and Consecutive Lines).</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BeanplotAllConflicts, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllConflicts, Align="center", append=TRUE)

HTML("<hr><h2>EditSameMC Occurrences Composition</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotESMCFP, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotFPEditSameMC, Align="center", append=TRUE)
HTML("<hr><h2>SameSignatureMC Occurrences Composition</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotSSCMFP, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotFPSameSigCM, Align="center", append=TRUE)

HTML("<hr><h2>Causes for SameSignatureMC occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotCSSCM, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllCauses, Align="center", append=TRUE)

time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
