package com.datn06.pickleconnect.Policy;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Policy API
 * Contains policy description/content
 * 
 * API Endpoint: POST /auth/policy
 * Backend Service: member-command-api (port 9003)
 */
public class PolicyResponse {
    
    @SerializedName("policyDescription")
    private String policyDescription;

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }
}
