@echo off
If "%4"=="" (
  java -Xmx%1m -classpath "jatr.jar;libs\wit-commons\wit-commons.jar;libs\trove\trove.jar;libs\opennlp-tools-1.3.0\maxent-2.4.0.jar;libs\opennlp-tools-1.3.0\opennlp-tools-1.3.0.jar;libs\opennlp-tools-1.3.0\jwnl.jar;libs\opennlp-tools-1.3.0\trove.jar;libs\opennlp-tools-1.3.0\jakarta-ant-optional.jar;libs\opennlp-tools-1.3.0\jwnl-1.3.3.jar;libs\opennlp-tools-1.3.0\ant.jar;libs\dragon\dragontool.jar;libs\apache-log4j-1.2.15\log4j-1.2.15.jar;libs\text\text.jar;" %2 %3
) Else (
  java -Xmx%1m -classpath "jatr.jar;libs\wit-commons\wit-commons.jar;libs\trove\trove.jar;libs\opennlp-tools-1.3.0\maxent-2.4.0.jar;libs\opennlp-tools-1.3.0\opennlp-tools-1.3.0.jar;libs\opennlp-tools-1.3.0\jwnl.jar;libs\opennlp-tools-1.3.0\trove.jar;libs\opennlp-tools-1.3.0\jakarta-ant-optional.jar;libs\opennlp-tools-1.3.0\jwnl-1.3.3.jar;libs\opennlp-tools-1.3.0\ant.jar;libs\dragon\dragontool.jar;libs\apache-log4j-1.2.15\log4j-1.2.15.jar;libs\text\text.jar;" %2 %3 %4
)