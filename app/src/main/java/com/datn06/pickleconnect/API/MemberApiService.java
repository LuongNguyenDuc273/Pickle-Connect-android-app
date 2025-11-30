package com.datn06.pickleconnect.API;


import com.datn06.pickleconnect.Common.BaseRequest;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.Base64UploadRequest;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
import com.datn06.pickleconnect.Models.UpdateMemberRequest;
import com.datn06.pickleconnect.Models.UpdateMemberRequest;
import com.datn06.pickleconnect.Models.UpdateMemberResponse;
import com.datn06.pickleconnect.Models.UploadImageResponse;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Member API Service - Handles user profile information
 * Base URL: http://10.0.2.2:9002/ (pickle-connect-member)
 *
 * ✅ ApiClient interceptor auto-adds Authorization and X-Userinfo headers
 */
public interface MemberApiService {

    /**
     * Get current user's member information
     *
     * @param request Request containing userId, email, phoneNumber
     * @return Member info including profile and certificates
     */
    @POST("api-andr/member-info")
    Call<BaseResponse<MemberInfoResponse>> getMemberInfo(
            @Body MemberInfoRequest request
    );

    /**
     * Update user profile information
     *
     * @param request Request containing userId and updated fields
     * @return Update confirmation response
     */
    @POST("api-andr/update")
    Call<BaseResponse<UpdateMemberResponse>> updateMember(
            @Body UpdateMemberRequest request
    );

    /**
     * Upload avatar image (Base64)
     * Member Service will handle conversion and upload to S3
     *
     * @param request Base64UploadRequest containing imageData, userId, requestId, requestTime, subFolder
     * @return Upload response with file URL
     */
    @POST("api-andr/upload-avatar")
    Call<UploadImageResponse> uploadAvatar(
            @Body Base64UploadRequest request
    );
}
