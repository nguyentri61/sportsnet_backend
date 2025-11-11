import java.security.SecureRandom;

public class RandomUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    // Hàm tạo chuỗi ngẫu nhiên, mặc định 8 ký tự
    public static String generateRandomString() {
        return generateRandomString(8);
    }

    // Hàm tạo chuỗi ngẫu nhiên với độ dài tùy chọn
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
