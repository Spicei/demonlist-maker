package spicey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;

public class SheetsGanderer {
        private static final String APPLICATION_NAME = "MyDemonlist";
        private static String API_KEY = new TopSecret().getKey();
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        private static final String SPREADSHEET_ID = "1xaMERl70vzr8q9MqElr4YethnV15EOe8oL1UV9LLljc";
        SheetsRequestInitializer REQUEST_INITIALIZER = new SheetsRequestInitializer(API_KEY);

        public HashMap<String, Demon> parseSheet() throws GeneralSecurityException, IOException {

                HashMap<String, Demon> demonLookup = new HashMap<>();

                NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                HTTP_TRANSPORT.createRequestFactory(request -> {
                        request.setReadTimeout(0);
                        System.out.println("does it work");

                });

                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                                .setApplicationName(APPLICATION_NAME)
                                .setGoogleClientRequestInitializer(REQUEST_INITIALIZER)
                                .build();
                int amountDemons = service.spreadsheets().get(SPREADSHEET_ID).execute().getSheets().get(3)
                                .getProperties()
                                .getGridProperties().getRowCount();
                System.out.println("Amount of demons in sheet: " + (amountDemons - 1));
                String datRange = "The List!A2:G" + amountDemons;
                ArrayList<List<String>> listOfDemons = null;
                try {
                        listOfDemons = ((ArrayList<List<String>>) service.spreadsheets()
                                        .values()
                                        .get(SPREADSHEET_ID, datRange).execute().get("values"));
                } catch (Exception e) {
                        System.out.println(e);
                        JOptionPane.showMessageDialog(new JFrame(),
                                        "Request to google timed out, keep trying it will work eventually lol.");
                        System.exit(0);

                }

                Demon currDemon;
                System.out.println("Amount of demons acquired: " + listOfDemons.size());
                for (List<String> list : listOfDemons) {
                        if (!(list.get(5).equals("unrated"))) {
                                currDemon = new Demon(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4),
                                                Integer.parseInt(list.get(5)), Double.parseDouble(list.get(6)));
                                demonLookup.put(currDemon.levelID, currDemon);
                        } else if (list.get(5).equals("unrated") && list.get(3).equals("Extreme Demon")) {
                                currDemon = new Demon(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4),
                                                0, 0);
                                demonLookup.put(currDemon.levelID, currDemon);
                        }

                }

                return demonLookup;
        }

        public class Demon {
                String name;
                String creator;
                String songName;
                String demonType;
                String levelID;
                int diffRating;
                double preciseDiffRating;

                public Demon(String name, String creator, String songName, String demonType, String levelID,
                                int diffRating, double preciseDiffRating) {
                        this.name = name;
                        this.creator = creator;
                        this.songName = songName;
                        this.demonType = demonType;
                        this.levelID = levelID;
                        this.diffRating = diffRating;
                        this.preciseDiffRating = preciseDiffRating;
                }
        }

        private HttpRequestInitializer createHttpRequestInitializer(final HttpRequestInitializer requestInitializer) {
                return new HttpRequestInitializer() {
                        @Override
                        public void initialize(final HttpRequest httpRequest) throws IOException {
                                requestInitializer.initialize(httpRequest);
                                httpRequest.setConnectTimeout(3 * 60000); // 3 minutes connect timeout
                                httpRequest.setReadTimeout(3 * 60000); // 3 minutes read timeout
                        }
                };
        }

}
