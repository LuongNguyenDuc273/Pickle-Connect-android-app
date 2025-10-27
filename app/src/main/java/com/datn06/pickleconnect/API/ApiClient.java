package com.datn06.pickleconnect.API;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // ⚠️ QUAN TRỌNG: Thay đổi URL này theo môi trường
    // Emulator: http://10.0.2.2:9003/
    // Điện thoại thật: http://192.168.1.XXX:9003/ (IP máy tính)
    private static final String BASE_URL = "http://10.0.2.2:9003/";

    private static Retrofit retrofit = null;
    private static String authToken = null;

    // Hàm set token sau khi login
    public static void setAuthToken(String token) {
        authToken = token;
        retrofit = null; // Reset để tạo lại với token mới
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // Logging - để xem request/response trong Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor - tự động thêm token vào header
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();

                    // Thêm token nếu có
                    if (authToken != null && !authToken.isEmpty()) {
                        builder.addHeader("Authorization", "Bearer " + authToken);
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
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Hàm để lấy ApiService
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
