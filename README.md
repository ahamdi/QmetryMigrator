# QmetryMigrator
Migration tool from Qmetry (https://www.qmetry.com) to Squash (https://www.squashtest.com)

## Build Instructions
* mvn clean compile assembly:single
This will generate a fat jar with all dependencies

## Run 

* java -jar QmetryMigrator-<VERSION>-jar-with-dependencies.jar /PATH/TO/QMETRY/TestCases.xlsx

Where /PATH/TO/QMETRY/TestCases.xlsx is the full path for the exported test cases in Excel format, make sure that the path does not contain spaces or put the path between quotes ""