import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {


    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Main");
        logger.log(Level.INFO, "Initializing bot.");
        new Bot(System.getenv("TELEGRAM_TOKEN"));
    }
}