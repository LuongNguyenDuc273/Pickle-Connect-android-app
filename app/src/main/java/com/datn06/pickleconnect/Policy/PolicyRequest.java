package com.datn06.pickleconnect.Policy;

import com.datn06.pickleconnect.Common.BaseRequest;

/**
 * Request DTO for Policy API
 * Extends BaseRequest to inherit clientId, requestId, requestTime
 * 
 * API Endpoint: POST /auth/policy
 * Backend Service: member-command-api (port 9003)
 */
public class PolicyRequest extends BaseRequest {
    
    /**
     * Default constructor
     * Auto-generates all required fields via BaseRequest
     */
    public PolicyRequest() {
        super(); // Call BaseRequest constructor
    }
    
    // No additional fields needed for policy request
    // All required fields (clientId, requestId, requestTime) inherited from BaseRequest
}
