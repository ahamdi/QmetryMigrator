package org.exoplatform.internal.qmetry;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.exoplatform.internal.qmetry.model.RequirementModel;
import org.exoplatform.internal.qmetry.model.StepModel;
import org.exoplatform.internal.qmetry.model.TestCaseModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class QMetryMigrationService {
    private final static Logger LOGGER = Logger.getLogger(QMetryMigrationService.class.getName());
    private final static Map<String, String> USERNAMES_TO_REPLACE;
    private final static String USERNAME_REPLACEMENT = "mbensalem";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");


    static {
        USERNAMES_TO_REPLACE = new HashMap<>();
        USERNAMES_TO_REPLACE.put("Farouk", "fsabbek");
        USERNAMES_TO_REPLACE.put("Nesrine", "nbenslima");
        USERNAMES_TO_REPLACE.put("Islem", "ichaieb");
        USERNAMES_TO_REPLACE.put("malek", "mebnsalem");
    }

    public static void main(String[] args) {
        String fileName = args[0];
        if (fileName == null || fileName.isEmpty() || fileName.lastIndexOf(".xlsx") < 0) {
            LOGGER.severe("Could not convert : Invalid Excel file");
            return;
        }
        String fileType = args[1];
        if (fileType == null || fileType.isEmpty()) {
            fileType = "TC";
        }
        long startTime = System.currentTimeMillis();
        if (fileType.equalsIgnoreCase("TC")) {
            LOGGER.info(String.format("Reading Test Cases from file %s", fileName));
            List<TestCaseModel> testCases = readTCExcelFile(fileName);
            if (testCases == null) {
                LOGGER.warning("Could not proceed conversion dur to previous errors !");
            }
            String convertedFileName = fileName.substring(0, fileName.lastIndexOf(".")).concat("-converted-on-").
                    concat(String.valueOf(System.currentTimeMillis())).concat(fileName.substring(fileName.lastIndexOf(".")));
            try {
                LOGGER.info(String.format("Writing Test Cases to file %s", convertedFileName));
                writeTCExcel(testCases, convertedFileName);
            } catch (IOException e) {
                LOGGER.severe("Problem converting the file " + fileName);
            }
        } else if (fileType.equalsIgnoreCase("RQ")) {
            LOGGER.info(String.format("Reading Test Cases from file %s", fileName));
            List<RequirementModel> requirements = readRQExcelFile(fileName);
            if (requirements == null) {
                LOGGER.warning("Could not proceed conversion dur to previous errors !");
            }
            String convertedFileName = fileName.substring(0, fileName.lastIndexOf(".")).concat("-converted-on-").
                    concat(String.valueOf(System.currentTimeMillis())).concat(fileName.substring(fileName.lastIndexOf(".")));
            try {
                LOGGER.info(String.format("Writing Test Cases to file %s", convertedFileName));
                writeRQExcel(requirements, convertedFileName);
            } catch (IOException e) {
                LOGGER.severe("Problem converting the file " + fileName);
            }
        } else {
            LOGGER.warning("Wrong option for exported file, please put 'TC' or 'RQ' !");
        }
        LOGGER.info(String.format("File conversion finished in %d", System.currentTimeMillis() - startTime));
    }


    private static List<RequirementModel> readRQExcelFile(String fileName) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(fileName);

            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Integer> indices = new HashMap<>();

            int i = 0;
            RequirementModel requirement = null;
            List<RequirementModel> requirements = new ArrayList<>();
            for (Row row : sheet) {
                if (i > 0) {
                    requirement = createNewRequirement(row, indices);
                    requirements.add(requirement);

                } else {
                    // Get indices of required fields
                    indices = readRequirementHeader(row);
                }
                i++;
            }
            return requirements;
        } catch (FileNotFoundException e) {
            LOGGER.severe("Can not load the file " + fileName);
        } catch (IOException e) {
            LOGGER.severe("Problem I/O");
        }
        return null;

    }

    private static Map<String, Integer> readRequirementHeader(Row row) {
        Map<String, Integer> indices = new HashMap<>();
        List<String> headersTestCase = Arrays.asList("createdByAlias", "Requirement Summary",
                "Created Date", "Requirement Folder Path", "Requirement Description");

        for (Cell cell : row) {
            String headerKey = readCell(cell);
            if (headersTestCase.contains(headerKey)) {
                indices.put(headerKey, cell.getColumnIndex());
            }
        }
        return indices;
    }

    private static RequirementModel createNewRequirement(Row row, Map<String, Integer> indices) {
        RequirementModel requirement = new RequirementModel();
        requirement.setACTION("CREATE");
        requirement.setPROJECT_ID("1");
        requirement.setPROJECT_NAME("eXoPLatform");
        requirement.setREQ_VERSION_CATEGORY("CAT_USER_STORY");
        requirement.setREQ_VERSION_CRITICALITY("MAJOR");
        requirement.setREQ_VERSION_STATUS("APPROVED");
        requirement.setREQ_VERSION_CREATED_ON(readCell(row.getCell(indices.get("Created Date"))));
        requirement.setREQ_VERSION_DESCRIPTION(readCell(row.getCell(indices.get("Requirement Description"))));
        String versionReference = readCell(row.getCell(indices.get("Requirement Summary")));
        versionReference = versionReference.substring(0, versionReference.lastIndexOf(']') + 1);
        requirement.setREQ_VERSION_REFERENCE(versionReference);
        requirement.setREQ_VERSION_CREATED_BY(replaceOldUserNames(readCell(row.getCell(indices.get("createdByAlias")))));
        requirement.setREQ_VERSION_CREATED_ON(readCell(row.getCell(indices.get("Created Date"))));
        requirement.setREQ_PATH(cleanPath(readCell(row.getCell(indices.get("Requirement Folder Path"))), requirement.getREQ_VERSION_REFERENCE()));
        return requirement;
    }

    private static List<TestCaseModel> readTCExcelFile(String fileName) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(fileName);

            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Integer> indices = new HashMap<>();

            int i = 0;
            TestCaseModel testCase = null;
            List<TestCaseModel> testCases = new ArrayList<>();
            for (Row row : sheet) {
                if (i > 0) {
                    if (isNewTestCase(row, indices)) {
                        if (testCase != null) {
                            testCases.add(testCase);
                        }
                        testCase = createNewTestCase(row, indices);
                        testCase = addStepToPreviousTestCase(testCase, row, indices);
                    } else {
                        testCase = addStepToPreviousTestCase(testCase, row, indices);
                    }
                } else {
                    // Get indices of required fields
                    indices = readHeader(row);
                }
                i++;
            }
            testCases.add(testCase);
            return testCases;
        } catch (FileNotFoundException e) {
            LOGGER.severe("Can not load the file " + fileName);
        } catch (IOException e) {
            LOGGER.severe("Problem I/O");
        }
        return null;
    }

    private static TestCaseModel addStepToPreviousTestCase(TestCaseModel testCase, Row row, Map<String, Integer> indices) {
        StepModel stepModel = new StepModel();
        stepModel.setACTION("CREATE");
        stepModel.setTC_STEP_NUM(readCell(row.getCell(indices.get("Step Order ID"))));
        stepModel.setTC_STEP_ACTION(readCell(row.getCell(indices.get("Step Description"))));
        stepModel.setTC_STEP_EXPECTED_RESULT(readCell(row.getCell(indices.get("Step Expected Outcome"))));
        stepModel.setTC_OWNER_PATH(testCase.getTC_PATH());
        return testCase.addStep(stepModel);
    }

    private static boolean isNewTestCase(Row row, Map<String, Integer> indices) {
        String value = readCell(row.getCell(indices.get("Summary")));
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private static Map<String, Integer> readHeader(Row row) {
        Map<String, Integer> indices = new HashMap<>();
        List<String> headersTestCase = Arrays.asList("Created By", "Summary",
                "Priority", "Status", "Created Date", "Test Case Folder Path",
                "Description", "Test Case Pre Condition", "Step Order ID", "Step Description", "Step Expected Outcome");

        for (Cell cell : row) {
            String headerKey = readCell(cell);
            if (headersTestCase.contains(headerKey)) {
                indices.put(headerKey, cell.getColumnIndex());
            }
        }
        return indices;
    }

    private static TestCaseModel createNewTestCase(Row row, Map<String, Integer> indices) {
        TestCaseModel testCase = new TestCaseModel();
        testCase.setPROJECT_ID("1"); //row.getCell(indices.get())
        testCase.setPROJECT_NAME("eXoPlatform");
        testCase.setACTION("CREATE");
        testCase.setTC_CUF_A1("5.3.0");
        testCase.setTC_WEIGHT("HIGH");
        testCase.setTC_NATURE("NAT_FUNCTIONAL_TESTING");
        testCase.setTC_STATUS("APPROVED");
        testCase.setTC_CREATED_BY(replaceOldUserNames(readCell(row.getCell(indices.get("Created By")))));
        testCase.setTC_CREATED_ON(readCell(row.getCell(indices.get("Created Date"))));
        testCase.setTC_NAME(readCell(row.getCell(indices.get("Summary"))));
        testCase.setTC_PRE_REQUISITE(readCell(row.getCell(indices.get("Test Case Pre Condition"))));
        testCase.setTC_PATH(cleanPath(readCell(row.getCell(indices.get("Test Case Folder Path"))), testCase.getTC_NAME()));
        return testCase;
    }

    private static String replaceOldUserNames(String userName) {
        if (USERNAMES_TO_REPLACE.get(userName) != null) {
            return USERNAMES_TO_REPLACE.get(userName);
        }
        return USERNAME_REPLACEMENT;
    }

    private static String cleanPath(String tcPATH, String tcName) {
        if (tcPATH == null || tcPATH.isEmpty()) {
            return "";
        }
        String toclean1 = "/PLF 5.3.x/Functional Test/";
        String toclean2 = "/PLF 5.3.x/";
        tcPATH = tcPATH.replace(toclean1, "/").replace(toclean2, "/");
        return "/eXoPlatform" + tcPATH + "/" + tcName;
    }


    private static Date convertDate(String dateToFormatText) {
        Date dateToFormat = Calendar.getInstance().getTime();
        try {
            dateToFormat = dateFormat.parse(dateToFormatText);
        } catch (ParseException e) {
            LOGGER.severe("Can not format date : " + dateToFormatText);
        }
        return dateToFormat;
    }

    private static String readCell(Cell cell) {
        String value;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = dateFormat.format(cell.getDateCellValue());
                } else {
                    value = cell.getNumericCellValue() + "";
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue() + "";
                break;
            case FORMULA:
                value = cell.getCellFormula() + "";
                break;
            default:
                value = " ";
        }
        if (value == null || value.isEmpty()) {
            LOGGER.warning("value is Null or empty for cell column " + cell.getColumnIndex() + " row " + cell.getRowIndex());
        }
        return value;
    }


    private static void writeRQExcel(List<RequirementModel> requirements, String convertedFileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet requirementsSheet = workbook.createSheet("REQUIREMENT");

        // Create Header for sheet requirement
        Row header = requirementsSheet.createRow(0);
        Cell cellAction = header.createCell(0);
        cellAction.setCellValue("ACTION");
        Cell cellPROJECT_ID = header.createCell(1);
        cellPROJECT_ID.setCellValue("PROJECT_ID");
        Cell cellPROJECT_NAME = header.createCell(2);
        cellPROJECT_NAME.setCellValue("PROJECT_NAME");
        Cell cellREQ_PATH = header.createCell(3);
        cellREQ_PATH.setCellValue("REQ_PATH");
        Cell cellREQ_VERSION_REFERENCE = header.createCell(4);
        cellREQ_VERSION_REFERENCE.setCellValue("REQ_VERSION_REFERENCE");
        Cell cellREQ_VERSION_NAME = header.createCell(5);
        cellREQ_VERSION_NAME.setCellValue("REQ_VERSION_NAME");
        Cell cellREQ_VERSION_CRITICALITY = header.createCell(6);
        cellREQ_VERSION_CRITICALITY.setCellValue("REQ_VERSION_CRITICALITY");
        Cell cellREQ_VERSION_CATEGORY = header.createCell(7);
        cellREQ_VERSION_CATEGORY.setCellValue("REQ_VERSION_CATEGORY");
        Cell cellREQ_VERSION_STATUS = header.createCell(8);
        cellREQ_VERSION_STATUS.setCellValue("REQ_VERSION_STATUS");
        Cell cellREQ_VERSION_DESCRIPTION = header.createCell(9);
        cellREQ_VERSION_DESCRIPTION.setCellValue("REQ_VERSION_DESCRIPTION");
        Cell cellREQ_VERSION_CREATED_ON = header.createCell(10);
        cellREQ_VERSION_CREATED_ON.setCellValue("REQ_VERSION_CREATED_ON");
        Cell cellREQ_VERSION_CREATED_BY = header.createCell(11);
        cellREQ_VERSION_CREATED_BY.setCellValue("REQ_VERSION_CREATED_BY");
        //Fill the content
        int i = 0;
        for (RequirementModel requirement : requirements) {
            i++;
            Row newRow = requirementsSheet.createRow(i);
            cellAction = newRow.createCell(0);
            cellAction.setCellValue(requirement.getACTION());
            cellPROJECT_ID = newRow.createCell(1);
            cellPROJECT_ID.setCellValue(requirement.getPROJECT_ID());
            cellPROJECT_NAME = newRow.createCell(2);
            cellPROJECT_NAME.setCellValue(requirement.getPROJECT_NAME());
            cellREQ_PATH = newRow.createCell(3);
            cellREQ_PATH.setCellValue(requirement.getREQ_PATH());
            cellREQ_VERSION_REFERENCE = newRow.createCell(4);
            cellREQ_VERSION_REFERENCE.setCellValue(requirement.getREQ_VERSION_REFERENCE());
            cellREQ_VERSION_NAME = newRow.createCell(5);
            cellREQ_VERSION_NAME.setCellValue(requirement.getREQ_VERSION_NAME());
            cellREQ_VERSION_CRITICALITY = newRow.createCell(6);
            cellREQ_VERSION_CRITICALITY.setCellValue(requirement.getREQ_VERSION_CRITICALITY());
            cellREQ_VERSION_CATEGORY = newRow.createCell(7);
            cellREQ_VERSION_CATEGORY.setCellValue(requirement.getREQ_VERSION_CATEGORY());
            cellREQ_VERSION_STATUS = newRow.createCell(8);
            cellREQ_VERSION_STATUS.setCellValue(requirement.getREQ_VERSION_STATUS());
            cellREQ_VERSION_DESCRIPTION = newRow.createCell(9);
            cellREQ_VERSION_DESCRIPTION.setCellValue(requirement.getREQ_VERSION_DESCRIPTION());
/*            cellREQ_VERSION_CREATED_ON = header.createCell(10);
            cellREQ_VERSION_CREATED_ON.setCellValue("REQ_VERSION_CREATED_ON");*/
            createDateCell(workbook, newRow, 10, convertDate(requirement.getREQ_VERSION_CREATED_ON()));
            cellREQ_VERSION_CREATED_BY = newRow.createCell(11);
            cellREQ_VERSION_CREATED_BY.setCellValue(requirement.getREQ_VERSION_CREATED_BY());
        }
        FileOutputStream outputStream = new FileOutputStream(convertedFileName);
        workbook.write(outputStream);
        workbook.close();
    }

    private static void writeTCExcel(List<TestCaseModel> testCases, String fileLocation) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet testCasesSheet = workbook.createSheet("TEST_CASES");
        Sheet stepsSheet = workbook.createSheet("STEPS");

        // Create Header for TEST_CASES sheet
        Row header = testCasesSheet.createRow(0);
        Cell cellAction = header.createCell(0);
        cellAction.setCellValue("ACTION");
        Cell cellPROJECT_ID = header.createCell(1);
        cellPROJECT_ID.setCellValue("PROJECT_ID");
        Cell cellPROJECT_NAME = header.createCell(2);
        cellPROJECT_NAME.setCellValue("PROJECT_NAME");
        Cell cellTC_PATH = header.createCell(3);
        cellTC_PATH.setCellValue("TC_PATH");
        Cell cellTC_NUM = header.createCell(4);
        cellTC_NUM.setCellValue("TC_NUM");
        Cell cellTC_NAME = header.createCell(5);
        cellTC_NAME.setCellValue("TC_NAME");
        Cell cellTC_WEIGHT = header.createCell(6);
        cellTC_WEIGHT.setCellValue("TC_WEIGHT");
        Cell cellTC_NATURE = header.createCell(7);
        cellTC_NATURE.setCellValue("TC_NATURE");
        Cell cellTC_STATUS = header.createCell(8);
        cellTC_STATUS.setCellValue("TC_STATUS");
        Cell cellTC_PRE_REQUISITE = header.createCell(9);
        cellTC_PRE_REQUISITE.setCellValue("TC_PRE_REQUISITE");
        Cell cellTC_CREATED_ON = header.createCell(10);
        cellTC_CREATED_ON.setCellValue("TC_CREATED_ON");
        Cell cellTC_CREATED_BY = header.createCell(11);
        cellTC_CREATED_BY.setCellValue("TC_CREATED_BY");
        Cell cellTC_CUF_A1 = header.createCell(12);
        cellTC_CUF_A1.setCellValue("TC_CUF_A1");

        // Create Header for STEPS sheet
        Row stepsSheetHeader = stepsSheet.createRow(0);
        Cell cellStepsAction = stepsSheetHeader.createCell(0);
        cellStepsAction.setCellValue("ACTION");
        Cell cellTC_OWNER_PATH = stepsSheetHeader.createCell(1);
        cellTC_OWNER_PATH.setCellValue("TC_OWNER_PATH");
        Cell cellTC_STEP_NUM = stepsSheetHeader.createCell(2);
        cellTC_STEP_NUM.setCellValue("TC_STEP_NUM");
        Cell cellTC_STEP_ACTION = stepsSheetHeader.createCell(3);
        cellTC_STEP_ACTION.setCellValue("TC_STEP_ACTION");
        Cell cellTC_STEP_EXPECTED_RESULT = stepsSheetHeader.createCell(4);
        cellTC_STEP_EXPECTED_RESULT.setCellValue("TC_STEP_EXPECTED_RESULT");

        //Fill the content
        int i = 0;
        int counter = 0;
        for (TestCaseModel testCase : testCases) {
            i++;
            Row newRow = testCasesSheet.createRow(i);
            cellAction = newRow.createCell(0);
            cellAction.setCellValue(testCase.getACTION());
            cellPROJECT_ID = newRow.createCell(1);
            cellPROJECT_ID.setCellValue(testCase.getPROJECT_ID());
            cellPROJECT_NAME = newRow.createCell(2);
            cellPROJECT_NAME.setCellValue(testCase.getPROJECT_NAME());
            cellTC_PATH = newRow.createCell(3);
            cellTC_PATH.setCellValue(testCase.getTC_PATH());
            cellTC_NUM = newRow.createCell(4);
            cellTC_NUM.setCellValue(testCase.getTC_NUM());
            cellTC_NAME = newRow.createCell(5);
            cellTC_NAME.setCellValue(testCase.getTC_NAME());
            cellTC_WEIGHT = newRow.createCell(6);
            cellTC_WEIGHT.setCellValue(testCase.getTC_WEIGHT());
            cellTC_NATURE = newRow.createCell(7);
            cellTC_NATURE.setCellValue(testCase.getTC_NATURE());
            cellTC_STATUS = newRow.createCell(8);
            cellTC_STATUS.setCellValue(testCase.getTC_STATUS());
            cellTC_PRE_REQUISITE = newRow.createCell(9);
            cellTC_PRE_REQUISITE.setCellValue(testCase.getTC_PRE_REQUISITE());
//            cellTC_CREATED_ON = newRow.createCell(10);
//            cellTC_CREATED_ON.setCellValue(testCase.getTC_CREATED_ON());
            createDateCell(workbook, newRow, 10, convertDate(testCase.getTC_CREATED_ON()));
            cellTC_CREATED_BY = newRow.createCell(11);
            cellTC_CREATED_BY.setCellValue(testCase.getTC_CREATED_BY());
            cellTC_CUF_A1 = newRow.createCell(12);
            cellTC_CUF_A1.setCellValue(testCase.getTC_CUF_A1());

            for (StepModel step : testCase.getSteps()) {
                counter++;
                Row stepsSheetRow = stepsSheet.createRow(counter);
                cellStepsAction = stepsSheetRow.createCell(0);
                cellStepsAction.setCellValue(step.getACTION());
                cellTC_OWNER_PATH = stepsSheetRow.createCell(1);
                cellTC_OWNER_PATH.setCellValue(step.getTC_OWNER_PATH());
                cellTC_STEP_NUM = stepsSheetRow.createCell(2);
                cellTC_STEP_NUM.setCellValue(step.getTC_STEP_NUM());
                cellTC_STEP_ACTION = stepsSheetRow.createCell(3);
                cellTC_STEP_ACTION.setCellValue(step.getTC_STEP_ACTION());
                cellTC_STEP_EXPECTED_RESULT = stepsSheetRow.createCell(4);
                cellTC_STEP_EXPECTED_RESULT.setCellValue(step.getTC_STEP_EXPECTED_RESULT());
            }
        }
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }

    private static Cell createDateCell(Workbook wb, Row row, int columnIndex, Date cellValue) {
        CellStyle cellStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("m/d/yy"));
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(cellValue);
        cell.setCellStyle(cellStyle);
        return cell;
    }
}
