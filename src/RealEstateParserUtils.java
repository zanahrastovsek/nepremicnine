import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zana on 07/12/16.
 */
public class RealEstateParserUtils {
    public static String getPageSource(String urlString) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            bufferedReader.close();
        } catch (IOException e) {
            Logger.getLogger(RealEstateParser.PROJECT_TAG).log(Level.SEVERE, "Error obtaining page source. Error: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static Elements getElementsForClass(Document doc, String styleClass) {
        return doc.select("." + styleClass);
    }
}
