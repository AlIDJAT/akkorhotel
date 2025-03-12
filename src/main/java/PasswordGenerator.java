import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "userpassword"; // Ton mot de passe en clair
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Mot de passe chiffré : " + encodedPassword);
    }
}