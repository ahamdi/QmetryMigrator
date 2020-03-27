package org.exoplatform.internal.qmetry.model;

import lombok.Data;

@Data
public class RequirementModel {
    private String ACTION;
    private String PROJECT_ID;
    private String PROJECT_NAME;
    private String REQ_PATH;
    private String REQ_VERSION_REFERENCE;
    private String REQ_VERSION_NUM;
    private String REQ_VERSION_NAME;
    private String REQ_VERSION_DESCRIPTION;
    private String REQ_VERSION_CRITICALITY;
    private String REQ_VERSION_CATEGORY;
    private String REQ_VERSION_STATUS;
    private String REQ_VERSION_CREATED_ON;
    private String REQ_VERSION_CREATED_BY;
}
