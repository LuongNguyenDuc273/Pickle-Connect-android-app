package com.datn06.pickleconnect.Models.Tournament;

/**
 * Constants for tournament operations
 */
public class TournamentConstants {

    // Search Types
    public static final String SEARCH_TYPE_ONGOING = "1";
    public static final String SEARCH_TYPE_UPCOMING = "2";
    public static final String SEARCH_TYPE_PAST = "3";
    public static final String SEARCH_TYPE_USER = "4";

    // Detail Types
    public static final String DETAIL_TYPE_FULL = "1"; // Full details with sponsors
    public static final String DETAIL_TYPE_BASIC = "2"; // Basic with match types

    // Match Types
    public static final String MATCH_TYPE_SINGLE_MALE = "SINGLE_MALE";
    public static final String MATCH_TYPE_SINGLE_FEMALE = "SINGLE_FEMALE";
    public static final String MATCH_TYPE_DOUBLE_MALE = "DOUBLE_MALE";
    public static final String MATCH_TYPE_DOUBLE_FEMALE = "DOUBLE_FEMALE";
    public static final String MATCH_TYPE_DOUBLE_MIXED = "DOUBLE_MIXED";

    // Tournament Status
    public static final String STATUS_PENDING = "0"; // Pending approval
    public static final String STATUS_APPROVED = "1"; // Approved
    public static final String STATUS_REJECTED = "-1"; // Rejected

    // Field Types for Registration Form
    public static final String FIELD_TYPE_TEXT = "text";
    public static final String FIELD_TYPE_NUMBER = "number";
    public static final String FIELD_TYPE_DATE = "date";
    public static final String FIELD_TYPE_SELECT = "select";
    public static final String FIELD_TYPE_RADIO = "radio";
    public static final String FIELD_TYPE_CHECKBOX = "checkbox";

    // Helper method to get match type display name
    public static String getMatchTypeDisplayName(String matchTypeCode) {
        switch (matchTypeCode) {
            case MATCH_TYPE_SINGLE_MALE:
                return "Đơn Nam";
            case MATCH_TYPE_SINGLE_FEMALE:
                return "Đơn Nữ";
            case MATCH_TYPE_DOUBLE_MALE:
                return "Đôi Nam";
            case MATCH_TYPE_DOUBLE_FEMALE:
                return "Đôi Nữ";
            case MATCH_TYPE_DOUBLE_MIXED:
                return "Đôi Nam Nữ";
            default:
                return matchTypeCode;
        }
    }

    // Helper method to get search type display name
    public static String getSearchTypeDisplayName(String searchType) {
        switch (searchType) {
            case SEARCH_TYPE_ONGOING:
                return "Đang diễn ra";
            case SEARCH_TYPE_UPCOMING:
                return "Sắp diễn ra";
            case SEARCH_TYPE_PAST:
                return "Đã kết thúc";
            case SEARCH_TYPE_USER:
                return "Giải của tôi";
            default:
                return "Tất cả";
        }
    }
}