package org.exoplatform.internal.qmetry.model;

import lombok.Data;

import java.util.Objects;

@Data
public class StepModel {
    private String TC_OWNER_PATH;
    private String TC_STEP_NUM;
    private String TC_STEP_EXPECTED_RESULT;
    private String TC_STEP_ACTION;
    private String ACTION;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepModel stepModel = (StepModel) o;
        return Objects.equals(TC_OWNER_PATH, stepModel.TC_OWNER_PATH) &&
                Objects.equals(TC_STEP_ACTION, stepModel.TC_STEP_ACTION);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TC_OWNER_PATH, TC_STEP_NUM, TC_STEP_EXPECTED_RESULT, TC_STEP_ACTION, ACTION);
    }
}
