This tool will convert QMetry exported Test cases and steps into an Excel format accepted by Squash
Assuming
$PATH_TO_QMETRY_MIGRATION_FOLDER : the full path of the folder where you put the migration tool jar file
$PATH_TO_QMETRY_EXPORTED_TC : the full path to the folder where you have the Excel file of exported Test Cases from Qmetry

1- Execute the follwing command :
> java -jar PATH_TO_QMETRY_MIGRATION_FOLDER/QmetryMigrator-1.0-SNAPSHOT.jar PATH_TO_QMETRY_EXPORTED_TC/exported-tc-to-convert.xlsx
2- Once finshed, you will find a new file inside $PATH_TO_QMETRY_EXPORTED_TC having the same file name with the prefix converted<timestamp>.xlsx
3- The logs should print informations about progress and erors if any
