package org.exoplatform.internal.qmetry.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestCaseModel {
    private String PROJECT_ID;
    private String ACTION;
    private String TC_PATH;
    private String PROJECT_NAME;
    private String	TC_NUM;
    private String TC_REFERENCE;
    private String TC_NAME;
    private String TC_WEIGHT;
    private String 	TC_NATURE;
    private String TC_TYPE;
    private String TC_STATUS;
    private String 	TC_CREATED_ON;
    private String TC_CREATED_BY;
    private String TC_PRE_REQUISITE;
    private String 	TC_CUF_A1; //custom field
    private List<StepModel> steps = new ArrayList<>();

    public TestCaseModel addStep(StepModel step) {
        this.steps.add(step);
        return this;
    }
}
