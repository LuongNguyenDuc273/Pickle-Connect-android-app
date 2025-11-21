# 🔐 OAuth2/Keycloak Authentication Flow cho Android App

## 📋 Tổng quan vấn đề

### Backend Architecture:
```
┌─────────────────────────────────────────────────────────────┐
│  Port 9005: member-command-api (Register)                   │
│  - POST /auth/register (username/password registration)     │
│  - Auto-generate password, send SMS                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Port 9011: pickle-connect-api-web (OAuth2 Login)           │
│  - GET /api-web/auth/login                                  │
│    → Redirect to /oauth2/authorization/keycloak             │
│    → Keycloak login page                                    │
│    → Callback with tokens (stored in cookies)              │
│  - GET /api-web/auth/user-info (get user data + token)     │
│  - POST /api-web/auth/logout                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Keycloak Server: http://localhost:8080                     │
│  - Realm: pickle-connect-web                                │
│  - Client: pickle-web                                       │
│  - Handles OAuth2 Authorization Code Flow                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Other APIs (Court, Payment, etc.)                          │
│  - Require header: X-Userinfo: {userId}                     │
│  - OR Authorization: Bearer {access_token}                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚨 Vấn đề với Android:

### ❌ **Backend OAuth2 flow dành cho WEB (browser):**
1. User click "Login" → Redirect to Keycloak login page (HTML form)
2. User nhập username/password trên web
3. Keycloak redirect về app với authorization code
4. Backend exchange code for tokens
5. Tokens stored in **HTTP-only cookies** (không access được từ JavaScript/Android)

### ❌ **Android KHÔNG thể dùng flow này vì:**
- Android không có "cookies" như browser
- Redirect flow phức tạp (cần setup Deep Link + WebView)
- Không lấy được token từ HTTP-only cookies

---

## ✅ GIẢI PHÁP: Resource Owner Password Credentials (ROPC) Flow

### 🔄 Flow cho Android:

```
1. User Register (Port 9005)
   → POST /auth/register
   → Backend tạo user trong Keycloak
   → Password sent via SMS

2. User Login (Direct to Keycloak)
   → POST to Keycloak Token Endpoint
   → Send: username + password
   → Receive: access_token + refresh_token

3. Store tokens locally (SharedPreferences/EncryptedSharedPreferences)

4. Call APIs with token
   → Option A: Header: Authorization: Bearer {access_token}
   → Option B: Header: X-Userinfo: {userId} (nếu backend accept)

5. Refresh token when expired
   → POST to Keycloak with refresh_token
   → Get new access_token
```

---

## 🛠️ Implementation Steps

### **BƯỚC 1: Tạo Keycloak Login API (Direct Token)**

Backend cần expose endpoint mới hoặc Android gọi **trực tiếp** vào Keycloak:

#### Option A: Gọi trực tiếp Keycloak Token Endpoint

```http
POST http://localhost:8080/realms/pickle-connect-web/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=pickle-web
&client_secret=K7eqGdqNUqcHzjWc2doJNNbRwvcptkLR
&username={username}
&password={password}
&scope=openid profile email
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "xxx",
  "scope": "openid profile email"
}
```

#### Option B: Backend tạo endpoint wrapper

```java
// AuthController.java (port 9011)
@PostMapping("/login-mobile")
public ResponseEntity<LoginMobileResponse> loginMobile(@RequestBody LoginMobileRequest request) {
    // Call Keycloak token endpoint
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", clientProps.getClientId());
    body.add("client_secret", clientProps.getClientSecret());
    body.add("username", request.getUsername());
    body.add("password", request.getPassword());
    body.add("scope", "openid profile email");
    
    HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);
    
    ResponseEntity<Map> response = restTemplate.postForEntity(
        keycloakProperties.getTokenUri(), 
        req, 
        Map.class
    );
    
    // Extract tokens and user info
    Map<String, Object> tokenData = response.getBody();
    String accessToken = (String) tokenData.get("access_token");
    String refreshToken = (String) tokenData.get("refresh_token");
    
    // Decode JWT to get userId
    Jwt jwt = jwtDecoder.decode(accessToken);
    String userId = jwt.getClaimAsString("sub");
    String email = jwt.getClaimAsString("email");
    
    LoginMobileResponse mobileResponse = new LoginMobileResponse();
    mobileResponse.setAccessToken(accessToken);
    mobileResponse.setRefreshToken(refreshToken);
    mobileResponse.setUserId(userId);
    mobileResponse.setEmail(email);
    mobileResponse.setExpiresIn((Integer) tokenData.get("expires_in"));
    
    return ResponseEntity.ok(mobileResponse);
}
```

---

### **BƯỚC 2: Android Implementation**

#### 2.1. Tạo LoginMobileRequest & Response DTOs

```java
// LoginMobileRequest.java
public class LoginMobileRequest {
    private String username;
    private String password;
    
    // Getters/Setters
}

// LoginMobileResponse.java
public class LoginMobileResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private Integer expiresIn;
    
    // Getters/Setters
}
```

#### 2.2. Cập nhật ApiClient để auto-attach token

```java
// ApiClient.java
public class ApiClient {
    
    // Thêm method để set token
    public static void setAuthToken(String token) {
        // Clear cache khi đổi token
        retrofitMap.clear();
        
        // Store token in interceptor
        authInterceptor.setToken(token);
    }
    
    // Auth Interceptor
    private static class AuthInterceptor implements Interceptor {
        private String token;
        
        public void setToken(String token) {
            this.token = token;
        }
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            if (token == null || token.isEmpty()) {
                return chain.proceed(original);
            }
            
            Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token);
            
            // Some APIs might need X-Userinfo
            // Extract userId from JWT if needed
            try {
                String userId = extractUserIdFromToken(token);
                if (userId != null) {
                    builder.header("X-Userinfo", userId);
                }
            } catch (Exception e) {
                // Log but don't fail
            }
            
            return chain.proceed(builder.build());
        }
        
        private String extractUserIdFromToken(String token) {
            try {
                // Decode JWT payload (base64)
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(
                        android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
                    );
                    JSONObject json = new JSONObject(payload);
                    return json.optString("sub");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
```

#### 2.3. Token Storage (Encrypted SharedPreferences)

```java
// TokenManager.java
public class TokenManager {
    private static final String PREF_NAME = "auth_tokens";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EXPIRES_AT = "expires_at";
    
    private final SharedPreferences prefs;
    
    public TokenManager(Context context) {
        // Use EncryptedSharedPreferences for better security
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveTokens(String accessToken, String refreshToken, 
                          String userId, int expiresIn) {
        long expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply();
        
        // Set token in ApiClient
        ApiClient.setAuthToken(accessToken);
    }
    
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
    
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }
    
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    public boolean isTokenExpired() {
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0);
        return System.currentTimeMillis() > expiresAt - 60000; // 1 min buffer
    }
    
    public void clearTokens() {
        prefs.edit().clear().apply();
        ApiClient.setAuthToken(null);
    }
}
```

#### 2.4. Update AuthApiService

```java
// AuthApiService.java
public interface AuthApiService {
    
    // Existing register
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    
    // NEW: Mobile login (if backend implements wrapper)
    @POST("auth/login-mobile")
    Call<BaseResponse<LoginMobileResponse>> loginMobile(@Body LoginMobileRequest request);
    
    // Refresh token
    @POST("auth/refresh-token")
    Call<BaseResponse<LoginMobileResponse>> refreshToken(@Body RefreshTokenRequest request);
}
```

#### 2.5. Update LoginActivity

```java
// LoginActivity.java
private void handleLogin() {
    String username = etUsername.getText().toString().trim();
    String password = etPassword.getText().toString().trim();
    
    if (!validateInputs(username, password)) {
        return;
    }
    
    showLoading(true);
    
    LoginMobileRequest request = new LoginMobileRequest();
    request.setUsername(username);
    request.setPassword(password);
    
    // Option A: Call backend wrapper
    AuthApiService authService = ApiClient.createService(
        ServiceHost.AUTH_SERVICE,  // Port 9011
        AuthApiService.class
    );
    
    authService.loginMobile(request).enqueue(new Callback<BaseResponse<LoginMobileResponse>>() {
        @Override
        public void onResponse(Call call, Response<BaseResponse<LoginMobileResponse>> response) {
            showLoading(false);
            
            if (response.isSuccessful() && response.body() != null) {
                BaseResponse<LoginMobileResponse> baseResponse = response.body();
                
                if ("00".equals(baseResponse.getCode())) {
                    LoginMobileResponse data = baseResponse.getData();
                    
                    // Save tokens
                    TokenManager tokenManager = new TokenManager(LoginActivity.this);
                    tokenManager.saveTokens(
                        data.getAccessToken(),
                        data.getRefreshToken(),
                        data.getUserId(),
                        data.getExpiresIn()
                    );
                    
                    // Navigate to home
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showError(baseResponse.getMessage());
                }
            } else {
                showError("Đăng nhập thất bại");
            }
        }
        
        @Override
        public void onFailure(Call call, Throwable t) {
            showLoading(false);
            showError("Lỗi kết nối: " + t.getMessage());
        }
    });
}
```

---

## 📊 Comparison: Web vs Mobile Flow

| Aspect | Web (Current) | Mobile (Proposed) |
|--------|---------------|-------------------|
| **Login Endpoint** | `/oauth2/authorization/keycloak` (redirect) | Direct Keycloak token endpoint |
| **Grant Type** | Authorization Code | Resource Owner Password |
| **Token Storage** | HTTP-only cookies | Encrypted SharedPreferences |
| **Token Access** | Automatic (cookies) | Manual (add to headers) |
| **User Experience** | Browser redirect | Native login form |
| **Security** | High (cookies protected) | Medium (need encryption) |

---

## 🔒 Security Considerations

### ✅ Recommendations:
1. **Use HTTPS** in production
2. **Encrypt tokens** with Android KeyStore
3. **Implement token refresh** logic
4. **Add biometric** authentication
5. **Implement certificate pinning**
6. **Obfuscate** app with ProGuard

### ⚠️ Risks:
- ROPC flow less secure than Authorization Code flow
- Tokens stored on device (can be extracted if rooted)
- Need to handle token refresh properly

---

## 🎯 Recommendation: Which Option?

### **Best Practice**: Backend tạo endpoint wrapper

**Reasons:**
1. ✅ Kiểm soát security từ backend
2. ✅ Có thể add extra validation
3. ✅ Log login attempts
4. ✅ Consistent với architecture hiện tại
5. ✅ Dễ maintain hơn

### **Alternative**: Gọi trực tiếp Keycloak

**Only if:**
- Backend team không có time
- Cần deploy nhanh
- Accept security tradeoff

---

## 📝 Next Steps

1. **Backend**: Implement `/auth/login-mobile` endpoint
2. **Android**: Update LoginActivity với TokenManager
3. **Test**: Verify token works với các API khác (Court, Payment)
4. **Security**: Implement EncryptedSharedPreferences
5. **UX**: Add auto-refresh token logic

---

Bạn muốn tôi implement option nào? 🤔
