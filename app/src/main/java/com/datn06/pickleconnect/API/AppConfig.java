package com.datn06.pickleconnect.API;

/**
 * Application-wide configuration constants
 */
public class AppConfig {
    
    /**
     * MinIO/S3 Storage base URL
     * For Android Emulator: use 10.0.2.2 to access host machine's localhost
     * For Real Device: use actual IP address (e.g., 192.168.1.x)
     */
    public static final String MINIO_BASE_URL = "http://10.0.2.2:9000";
    
    /**
     * Alternative MinIO URL for real devices (update this with your actual host IP)
     */
    public static final String MINIO_BASE_URL_DEVICE = "http://192.168.1.100:9000";
    
    /**
     * Helper method to fix image URLs from backend
     * Replaces localhost references with emulator-compatible IP
     */
    public static String fixImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // Replace localhost variations with emulator host IP
        String fixedUrl = url
            .replace("http://localhost:9000", MINIO_BASE_URL)
            .replace("http://127.0.0.1:9000", MINIO_BASE_URL)
            .replace("https://localhost:9000", MINIO_BASE_URL)
            .replace("https://127.0.0.1:9000", MINIO_BASE_URL);
        
        // If relative path, prepend base URL
        if (!fixedUrl.startsWith("https://") && !fixedUrl.startsWith("http://")) {
            fixedUrl = MINIO_BASE_URL + fixedUrl;
        }
        
        return fixedUrl;
    }
    
    /**
     * Check if running on emulator
     * (You can enhance this detection if needed)
     */
    public static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.contains("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(android.os.Build.PRODUCT);
    }
    
    /**
     * Get appropriate MinIO URL based on device type
     */
    public static String getMinioUrl() {
        return isEmulator() ? MINIO_BASE_URL : MINIO_BASE_URL_DEVICE;
    }
}
