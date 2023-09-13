package spicey;

import java.io.IOException;
import java.net.URISyntaxException;

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

        // lowkey stole this but i dont remember where i got it from
        System.setProperty("webdriver.chrome.driver",
                "chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://aredl.pages.dev/#/");

        try {
            Thread.sleep(3000); // lets the non html stuff load

        } catch (InterruptedException e) {
            System.out.println("fail");
            e.printStackTrace();

        }

        String pageSource = driver.getPageSource();

        driver.quit();

        Document doc = Jsoup.parse(pageSource);

        return doc;
    }

    public static int getPlacement(Element list, String lvlName) {
        Elements lvlContainer = list.getElementsContainingText(lvlName);
        Element placementContainer = lvlContainer.get(lvlContainer.size() - 4).getElementsByTag("p").get(0);
        int placement = Integer.parseInt(placementContainer.text().substring(1));
        return placement;
    }

}
