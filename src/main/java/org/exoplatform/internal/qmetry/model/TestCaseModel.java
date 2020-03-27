package org.exoplatform.internal.qmetry.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseModel that = (TestCaseModel) o;
        if(that.steps.size() != this.steps.size()){
            return false;
        }
        for(StepModel step : steps){
            if(! that.steps.contains(step)){
                return false;
            }
        }
        return Objects.equals(this.TC_NAME, that.TC_NAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PROJECT_ID, ACTION, TC_PATH, PROJECT_NAME, TC_NUM, TC_REFERENCE, TC_NAME, TC_WEIGHT, TC_NATURE, TC_TYPE, TC_STATUS, TC_CREATED_ON, TC_CREATED_BY, TC_PRE_REQUISITE, TC_CUF_A1, steps);
    }
}
