package com.datn06.pickleconnect.API;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.PaymentUrlResponse;
import com.datn06.pickleconnect.Models.Tournament.*;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TournamentApiService {
    /**
     * Get tournament list with filters
     * Endpoint: POST /tourney-list
     *
     * @param request Request containing userId, searchType, searchKeyword, searchLocation, page, size
     *                searchType: "1"=ongoing, "2"=upcoming, "3"=past, "4"=user tournaments
     * @return List of tournaments
     */
    @POST("api-andr/tourney-list")
    Call<BaseResponse<List<TourneyListResponse>>> getTourneyList(
            @Body TourneyListRequest request
    );

    /**
     * Get tournament detail
     * Endpoint: POST /tourney-detail
     *
     * @param request Request containing userId, tournamentId, type
     *                type: "1"=full details with sponsors, "2"=basic with match types
     * @return Tournament detail information
     */
    @POST("api-andr/tourney-detail")
    Call<BaseResponse<TourneyDetailResponse>> getTourneyDetail(
            @Body TourneyDetailRequest request
    );

    /**
     * Get tournament registration form configuration
     * Endpoint: POST /tourney-reg-config
     *
     * @param request Request containing userId, tournamentId
     * @return List of form field configurations
     */
    @POST("api-andr/tourney-reg-config")
    Call<BaseResponse<List<TourneyRegConfigResponse>>> getTourneyRegConfig(
            @Body TourneyRegConfigRequest request
    );

    /**
     * Get tournament registration types (match types)
     * Endpoint: POST /tourney-reg-type
     */
    @POST("api-andr/tourney-reg-type")
    Call<BaseResponse<TourneyRegTypeResponse>> getTourneyRegType(
            @Body TourneyRegTypeRequest request
    );

    /**
     * Register for tournament
     * Endpoint: POST /tourney-reg
     */
//    @POST("api-andr/tourney-reg")
//    Call<BaseResponse<TourneyRegResponse>> registerTourney(
//            @Body TourneyRegRequest request
//    );

    /**
     * Initialize tournament registration payment
     * Endpoint: POST /tourney-reg-init
     */
//    @POST("api-andr/tourney-reg-init")
//    Call<BaseResponse<PaymentUrlResponse>> initTourneyPayment(
//            @Body TourneyRegInitRequest request
//    );
}
