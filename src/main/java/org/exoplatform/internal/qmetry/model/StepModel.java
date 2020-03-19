package org.exoplatform.internal.qmetry.model;

import lombok.Data;

@Data
public class StepModel {
    private String TC_OWNER_PATH;
    private String TC_STEP_NUM;
    private String TC_STEP_EXPECTED_RESULT;
    private String TC_STEP_ACTION;
    private String ACTION;
}
