package com.datn06.pickleconnect.API;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.datn06.pickleconnect.Utils.TokenManager; // ✅ IMPORT TokenManager
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

    public static final String BASE_URL = ServiceHost.AUTH_SERVICE.getUrl();
    public static final String ALT_BASE_URL = ServiceHost.COURT_SERVICE.getUrl();

    private static final Map<String, Retrofit> retrofitMap = new HashMap<>();
    private static Retrofit retrofit = null;
    private static String authToken = null;
    private static Context applicationContext = null;
    private static TokenManager tokenManager = null; // ✅ THÊM TokenManager

    // ✅ Initialize context (call this in Application class or first Activity)
    public static void init(Context context) {
        if (context != null) {
            applicationContext = context.getApplicationContext();
            tokenManager = TokenManager.getInstance(applicationContext); // ✅ KHỞI TẠO
            loadTokenFromTokenManager(); // ✅ LOAD TỪ TokenManager
        }
    }

    // ✅ THAY ĐỔI: Load token từ TokenManager thay vì SharedPreferences
    private static void loadTokenFromTokenManager() {
        if (tokenManager != null) {
            String savedToken = tokenManager.getToken();
            if (savedToken != null && !savedToken.isEmpty()) {
                authToken = savedToken;
                Log.d("ApiClient", "Loaded token from TokenManager");
            }
        }
    }

    // ✅ THÊM: Method để reload token (gọi sau khi login/logout)
    public static void reloadToken() {
        loadTokenFromTokenManager();
        // Reset all retrofit instances to recreate interceptor with new token
        retrofit = null;
        retrofitMap.clear();
    }

    // Hàm set token sau khi login
    public static void setAuthToken(String token) {
        authToken = token;

        // ✅ THÊM: Cũng lưu vào TokenManager để đồng bộ
        if (tokenManager != null) {
            tokenManager.saveToken(token);
        }

        // Reset tất cả retrofit instances
        retrofit = null;
        retrofitMap.clear();
    }

    public static Retrofit getRetrofitInstance() {
        return getRetrofitInstance(BASE_URL);
    }

    public static Retrofit getRetrofitInstance(String baseUrl) {
        if (baseUrl == null) baseUrl = BASE_URL;

        // ✅ THAY ĐỔI: Load token từ TokenManager nếu chưa có
        if (authToken == null && tokenManager != null) {
            loadTokenFromTokenManager();
        }

        // Nếu yêu cầu instance mặc định
        if (BASE_URL.equals(baseUrl) && retrofit != null) {
            return retrofit;
        }

        // Kiểm tra cache
        if (retrofitMap.containsKey(baseUrl)) {
            return retrofitMap.get(baseUrl);
        }

        // Logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor - tự động thêm token vào header
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();

                // ✅ THÊM: Luôn lấy token mới nhất từ TokenManager mỗi lần request
                String currentToken = authToken;
                if (tokenManager != null) {
                    String freshToken = tokenManager.getToken();
                    if (freshToken != null && !freshToken.isEmpty()) {
                        currentToken = freshToken;
                    }
                }

                if (currentToken != null && !currentToken.isEmpty()) {
                    // 1. Thêm Authorization Bearer token
                    builder.addHeader("Authorization", "Bearer " + currentToken);

                    // 2. Parse JWT và tạo X-Userinfo header
                    try {
                        String xUserInfo = generateXUserInfo(currentToken);
                        builder.addHeader("X-Userinfo", xUserInfo);
                        Log.d("ApiClient", "Added headers with token");
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

    // ✅ THÊM: Get client (để dùng cho Retrofit.Builder bên ngoài nếu cần)
    public static Retrofit getClient() {
        return getRetrofitInstance();
    }

    // Hàm để lấy ApiService
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    public static ApiService getApiService(String baseUrl) {
        return getRetrofitInstance(baseUrl).create(ApiService.class);
    }

    // Generic helpers
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

    // Parse JWT token and generate X-Userinfo header
    private static String generateXUserInfo(String jwtToken) throws Exception {
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) {
            throw new Exception("Invalid JWT token format");
        }

        String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
        JSONObject jsonPayload = new JSONObject(payload);

        String preferredUsername = jsonPayload.optString("preferred_username", "");
        String sub = jsonPayload.optString("sub", "");
        String email = jsonPayload.optString("email", "");

        JSONObject xUserInfoJson = new JSONObject();
        xUserInfoJson.put("preferred_username", preferredUsername);
        xUserInfoJson.put("sub", sub);
        xUserInfoJson.put("email", email);

        String xUserInfoString = xUserInfoJson.toString();
        String encoded = Base64.encodeToString(xUserInfoString.getBytes(), Base64.NO_WRAP);

        return encoded;
    }
}