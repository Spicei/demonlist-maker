package spicey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class AllRatedSurveyor {
    public static Document getHtml()
            throws URISyntaxException, IOException, BadLocationException, InterruptedException {
        ArrayList<String> extremmons = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver",
                "C:\\Utility\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        // Initialize the WebDriver (in this case, using Chrome)
        WebDriver driver = new ChromeDriver(options);

        // Load the webpage
        driver.get("https://aredl.pages.dev/#/");

        // Wait for the page to load (you might need to use explicit waits)
        // For simplicity, we'll use a sleep here, but it's not recommended in
        // production code
        try {
            Thread.sleep(3000); // Sleep for 5 seconds to allow the page to load

        } catch (InterruptedException e) {
            System.out.println("fail");
            e.printStackTrace();

        }

        // Get the page's HTML content
        String pageSource = driver.getPageSource();

        // Close the WebDriver
        driver.quit();

        Document doc = Jsoup.parse(pageSource);
        Elements levels = doc.getElementsByClass("list").get(0).getElementsByTag("tr");
        Element list = doc.getElementsByClass("list").get(0);
        levels.get(0).getElementsByTag("span").get(0).text();
        levels.get(0).getElementsByTag("p").get(0).text().substring(1);
        Elements levelContain = list.getElementsContainingText("Bloodbath");
        System.out.println(levelContain.get(levelContain.size() - 4));
        // System.out.println(levels.get(0));

        return doc;
    }

    public static int getPlacement(Element list, String lvlName) {
        Elements lvlContainer = list.getElementsContainingText(lvlName);
        Element placementContainer = lvlContainer.get(lvlContainer.size() - 4).getElementsByTag("p").get(0);
        int placement = Integer.parseInt(placementContainer.text().substring(1));
        return placement;
    }

}
