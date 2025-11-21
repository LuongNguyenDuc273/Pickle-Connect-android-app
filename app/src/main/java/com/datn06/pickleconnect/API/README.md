# API Service Architecture

## 📁 Structure

```
API/
├── ApiClient.java           // Main HTTP client with caching & auth
├── ServiceHost.java         // Enum of all service URLs
├── AuthApiService.java      // Auth & Registration endpoints
├── ApiService.java          // Legacy (deprecated) 
└── SERVICE_HOST_USAGE.txt   // Usage examples
```

## 🎯 Services

### 1. **AUTH_SERVICE** (Port 9005)
- **Purpose**: Authentication, Registration, Password Reset
- **Base URL**: `http://10.0.2.2:9005/`
- **Endpoints**: `/auth/register`, `/auth/login`, `/auth/reset-password`, etc.
- **Interface**: `AuthApiService.java`

**Example:**
```java
AuthApiService authService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);
authService.register(request).enqueue(callback);
```

### 2. **COURT_SERVICE** (Port 9008)
- **Purpose**: Court booking, facility management
- **Base URL**: `http://10.0.2.2:9008/`
- **Endpoints**: `/api/v1/booking/fields/availability`, `/api/v1/booking/create`, `/api/v1/booking/save-facility-user`
- **Interface**: `CourtApiService.java`

**Example:**
```java
CourtApiService courtService = ApiClient.createService(ServiceHost.COURT_SERVICE, CourtApiService.class);
courtService.getFieldAvailability(userId, facilityId, "2025-04-22").enqueue(callback);
```

### 3. **API_SERVICE** (Port 9003)
- **Purpose**: Main API, Home data
- **Base URL**: `http://10.0.2.2:9003/`
- **Endpoints**: `/api-andr/home/data`
- **Interface**: `ApiService.java` (legacy)

### 4. **PAYMENT_SERVICE** (Port 9010)
- **Purpose**: Payment processing (if available)
- **Base URL**: `http://10.0.2.2:9010/`
- **Interface**: TODO: Create `PaymentApiService.java`

## ✅ Benefits

1. **Clear Separation**: Each service has its own interface
2. **Type Safety**: Compile-time checking of endpoints
3. **Easy Maintenance**: Add new services by updating `ServiceHost` enum
4. **Testable**: Mock individual services easily
5. **Scalable**: No need to modify `ApiClient` when adding services

## 🔧 How to Add New Service

1. Add entry to `ServiceHost.java`:
```java
NEW_SERVICE("http://10.0.2.2:9012/", "New Service (9012)")
```

2. Create new interface `NewApiService.java`:
```java
public interface NewApiService {
    @GET("endpoint")
    Call<Response> getData();
}
```

3. Use it:
```java
NewApiService service = ApiClient.createService(ServiceHost.NEW_SERVICE, NewApiService.class);
service.getData().enqueue(callback);
```

## 📝 Migration Guide

**Old way (deprecated):**
```java
ApiClient.getApiService().register(request).enqueue(...);
```

**New way (recommended):**
```java
AuthApiService authService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);
authService.register(request).enqueue(...);
```

## 🔐 Authentication

All services automatically use the auth token set via:
```java
ApiClient.setAuthToken(token);
```

The token is added to all requests via `Authorization: Bearer {token}` header.

## 📱 Device vs Emulator

**Emulator:**
- Uses `10.0.2.2` to access host machine's localhost
- Configured in `ServiceHost` enum

**Real Device:**
- Update `ServiceHost` URLs to use your machine's LAN IP:
```java
AUTH_SERVICE("http://192.168.1.XXX:9005/", "Auth Service")
```

## 🎨 Current Usage

- ✅ `RegisterActivity.java` - Uses `AuthApiService` + `ServiceHost.AUTH_SERVICE`
- ✅ `CourtApiService.java` - Ready for booking flow implementation
- ⚠️ `LoginActivity.java` - TODO: Migrate to `AuthApiService`
- ⚠️ Booking activities - TODO: Implement using `CourtApiService`

---
**Last Updated**: November 16, 2025
