package com.datn06.pickleconnect.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // ⚠️ QUAN TRỌNG: Thay đổi URL này theo môi trường
    // Emulator: Sử dụng ServiceHost enum để quản lý các service
    // Điện thoại thật: http://192.168.1.XXX:PORT/ (IP máy tính)
    
    // Default service: Auth Service (member-command-api) on port 9005
    public static final String BASE_URL = ServiceHost.AUTH_SERVICE.getUrl();
    
    // Alternative service URLs (kept for backward compatibility)
    public static final String ALT_BASE_URL = ServiceHost.COURT_SERVICE.getUrl();

    // Lưu nhiều Retrofit instances (key = baseUrl)
    private static final Map<String, Retrofit> retrofitMap = new HashMap<>();
    private static Retrofit retrofit = null; // giữ for default BASE_URL
    private static String authToken = null;
    private static Context applicationContext = null; // ✅ ADDED: Store application context

    // ✅ ADDED: Initialize context (call this in Application class or first Activity)
    public static void init(Context context) {
        if (context != null) {
            applicationContext = context.getApplicationContext();
            // Load token from SharedPreferences if exists
            loadTokenFromPreferences();
        }
    }

    // ✅ ADDED: Load token from SharedPreferences
    private static void loadTokenFromPreferences() {
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
            String savedToken = prefs.getString("token", null);
            if (savedToken != null && !savedToken.isEmpty()) {
                authToken = savedToken;
            }
        }
    }

    // Hàm set token sau khi login
    public static void setAuthToken(String token) {
        authToken = token;
        // Reset tất cả retrofit instances để interceptor sẽ được tái tạo với token mới
        retrofit = null;
        retrofitMap.clear();
    }

    public static Retrofit getRetrofitInstance() {
        return getRetrofitInstance(BASE_URL);
    }

    // Trả về Retrofit instance cho baseUrl cụ thể (không ghi đè nếu đã có)
    public static Retrofit getRetrofitInstance(String baseUrl) {
        if (baseUrl == null) baseUrl = BASE_URL;

        // ✅ ADDED: Load token if not loaded yet
        if (authToken == null && applicationContext != null) {
            loadTokenFromPreferences();
        }

        // Nếu yêu cầu instance mặc định
        if (BASE_URL.equals(baseUrl) && retrofit != null) {
            return retrofit;
        }

        // Kiểm tra cache
        if (retrofitMap.containsKey(baseUrl)) {
            return retrofitMap.get(baseUrl);
        }

        // Logging - để xem request/response trong Logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor - tự động thêm token vào header
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();

                // Thêm Authorization header và X-Userinfo header nếu có token
                if (authToken != null && !authToken.isEmpty()) {
                    // 1. Thêm Authorization Bearer token
                    builder.addHeader("Authorization", "Bearer " + authToken);
                    
                    // 2. Parse JWT và tạo X-Userinfo header cho backend port 9008
                    try {
                        String xUserInfo = generateXUserInfo(authToken);
                        builder.addHeader("X-Userinfo", xUserInfo);
                        Log.d("ApiClient", "Generated X-Userinfo: " + xUserInfo);
                    } catch (Exception e) {
                        Log.e("ApiClient", "Failed to generate X-Userinfo: " + e.getMessage());
                        // Fallback: empty JSON
                        String emptyJson = "{\"preferred_username\":\"\",\"sub\":\"\",\"email\":\"\"}";
                        String encoded = Base64.encodeToString(emptyJson.getBytes(), Base64.NO_WRAP);
                        builder.addHeader("X-Userinfo", encoded);
                    }
                }

                builder.addHeader("Content-Type", "application/json");
                builder.addHeader("Accept", "application/json");

                return chain.proceed(builder.build());
            }
        };

        // Tạo OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Tạo Retrofit
        Retrofit r = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Lưu cache
        if (BASE_URL.equals(baseUrl)) {
            retrofit = r;
        }
        retrofitMap.put(baseUrl, r);

        return r;
    }

    // Hàm để lấy ApiService
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    // Lấy ApiService theo baseUrl (ví dụ gọi port 9008)
    public static ApiService getApiService(String baseUrl) {
        return getRetrofitInstance(baseUrl).create(ApiService.class);
    }

    // Generic helpers: tạo service theo class
    public static <T> T createService(Class<T> serviceClass) {
        return getRetrofitInstance().create(serviceClass);
    }

    public static <T> T createService(String baseUrl, Class<T> serviceClass) {
        return getRetrofitInstance(baseUrl).create(serviceClass);
    }

    public static <T> T createService(ServiceHost host, Class<T> serviceClass) {
        if (host == null) return createService(serviceClass);
        return getRetrofitInstance(host.getUrl()).create(serviceClass);
    }

    // ✅ ADDED: Parse JWT token and generate X-Userinfo header
    private static String generateXUserInfo(String jwtToken) throws Exception {
        // JWT format: header.payload.signature
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) {
            throw new Exception("Invalid JWT token format");
        }

        // Decode payload (part[1])
        String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
        
        // Parse JSON payload
        JSONObject jsonPayload = new JSONObject(payload);
        
        // Extract required fields
        String preferredUsername = jsonPayload.optString("preferred_username", "");
        String sub = jsonPayload.optString("sub", "");
        String email = jsonPayload.optString("email", "");
        
        // Create X-Userinfo JSON
        JSONObject xUserInfoJson = new JSONObject();
        xUserInfoJson.put("preferred_username", preferredUsername);
        xUserInfoJson.put("sub", sub);
        xUserInfoJson.put("email", email);
        
        // Encode to Base64
        String xUserInfoString = xUserInfoJson.toString();
        String encoded = Base64.encodeToString(xUserInfoString.getBytes(), Base64.NO_WRAP);
        
        Log.d("ApiClient", "JWT Payload: " + payload);
        Log.d("ApiClient", "X-Userinfo JSON: " + xUserInfoString);
        
        return encoded;
    }
}
