import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
            botOptions.setProxyHost("96.113.166.133");
            botOptions.setProxyPort(1080);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            telegramBotsApi.registerBot(new TelegramBot(botOptions));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}