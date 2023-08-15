package spicey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SaveLooker {
    public List<String> parseSave() throws IOException {
        List<String> levelsList = new ArrayList<>();

        Scanner scan = new Scanner(new File("DecryptedSave.txt"));
        String decryptedSave = "";

        while (scan.hasNext()) {
            decryptedSave += scan.next();
        }

        scan.close();

        int currentPosition = 0;
        String currentLevel;

        while (decryptedSave.indexOf("</d>", currentPosition) != -1) {

            if (currentPosition % 100000 < 5) {
                // System.out.println(currentPosition + "/" + lengthOfFile);
            }
            int nextStart = decryptedSave.indexOf("<d>", currentPosition);
            int nextEnd = decryptedSave.indexOf("</d>", currentPosition);
            if (nextStart >= nextEnd) { // if robtop brain made out of mush
                // System.out.println("start: " + nextStart);
                // System.out.println("end: " + nextEnd);
                nextEnd = decryptedSave.indexOf("</d>", nextEnd + 1);
                // System.out.println("new end: " + nextEnd);

            }
            currentLevel = decryptedSave.substring(nextStart, nextEnd);

            if (currentLevel.contains("<k>k1</k>")) { // k1 = container where id is found if no id container then the
                                                      // <d> is not a level
                // gets level id from string

                String IDContainer = "<k>k1</k><i>";
                String currentID = currentLevel.substring(currentLevel.indexOf(IDContainer) + IDContainer.length(),
                        currentLevel.indexOf("<", currentLevel.indexOf(IDContainer) + IDContainer.length()));

                Boolean isDemon = currentLevel.contains("<k>k25</k><t/>"); // k25 = isdemon and <t /> is for true obv
                Boolean isComplete = currentLevel.contains("<k>k19</k><i>100</i>"); // k19 holds percentage

                if (isDemon && isComplete) {
                    levelsList.add(currentID);
                }

                // // gets level name from string
                // String nameContainer = "<k>k2</k><s>";
                // String currentName = currentLevel.substring(
                // currentLevel.indexOf(nameContainer) + nameContainer.length(),
                // currentLevel.indexOf("<", currentLevel.indexOf(nameContainer) +
                // nameContainer.length()));

            }
            currentPosition = decryptedSave.indexOf("</d>", currentPosition) + 1;
        }

        return levelsList;
    }

    public class Level {
        public Level(String incomingID) {
            ID = incomingID;

        }

        public Level(String incomingID, String incomingProgress) {
            ID = incomingID;
            progress = incomingProgress;
        }

        String ID;
        String name;
        String progress;
    }
}
