package spicey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import spicey.SheetsGanderer.Demon;

import java.util.Base64;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Hello world!
 */

public final class App {
    String gameManager = "CCGameManager.dat";

    // k18 - ATTEMPTS
    // k1 - LEVEL ID
    // k36 - JUMPS
    // k85 - CLICKS
    // k86 - BEST ATTEMPT TIME
    // k87 - SEED ???????
    // k88 - SCORES ??????
    // k89 - leaderboard valid
    // k19 - percentage
    // k71 - mana orb percent
    // k90 - leaderboard percent
    // k20 - practice percent
    // k25 - is vDemon?
    // k76 - type of vDemon (6=extreme?)
    // k2 - name of level
    // k3 - description (in base 64)

    public static void main(String[] args) throws IOException, DataFormatException, GeneralSecurityException,
            URISyntaxException, BadLocationException, InterruptedException {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());

        frame.setSize(new Dimension((int) (screen.width * 0.20), (int) (screen.height * 0.1)));
        frame.setDefaultCloseOperation(3); // 3 = exit on close
        frame.setResizable(false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setForeground(java.awt.Color.green);

        progressBar.setValue(0);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        progressBar.setPreferredSize(new Dimension((int) (screen.width * 0.15), (int) (screen.height * 0.025)));

        JLabel explainer = new JLabel("Starting...");
        explainer.setLocation(0, (int) screen.getHeight());

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

        frame.add(progressBar);
        frame.add(explainer);
        frame.setTitle("Demonlist Maker");
        frame.setIconImage(ImageIO.read(URI.create(
                "https://i.redd.it/4vt17p57bs091.png")
                .toURL()).getScaledInstance(64, 64, Image.SCALE_AREA_AVERAGING));
        frame.setVisible(true);

        String savePath = System.getenv("LOCALAPPDATA") + "\\GeometryDash\\CCGameManager2.dat";

        long totalStartTime = System.currentTimeMillis();
        explainer.setText("Loading Save File...");
        progressBar.setValue(20);
        System.out.println("Loading Save File...");
        long startTime = System.currentTimeMillis();
        String res = Xor(savePath, 11);
        System.out.println("Save File Loaded in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds!");
        System.out.println();

        explainer.setText("Decrypting Save File...");
        progressBar.setValue(40);
        System.out.println("Decrypting Save File...");
        startTime = System.currentTimeMillis();
        byte[] fin = Decrypt(res);
        String dCrip = new String(fin, StandardCharsets.UTF_8);

        // OutputStream out = new FileOutputStream(new File("DecryptedSave.txt"));
        // for (byte b : fin) {
        // out.write(b);
        // }
        System.out.println("Save File Decrypted in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds!");
        System.out.println();

        SaveLooker saveLooker = new SaveLooker();
        explainer.setText("Looking for your completed levels in save file....");
        progressBar.setValue(60);
        System.out.println("Looking for your completed levels in save file....");
        startTime = System.currentTimeMillis();
        List<String> userCompletedIDs = saveLooker.parseSave(dCrip);

        System.out.println("Found " + userCompletedIDs.size() + " completed levels in "
                + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
        System.out.println();

        SheetsGanderer sheetGanderer = new SheetsGanderer();
        explainer.setText("Getting details of all demons from GDDL Sheet...");
        progressBar.setValue(80);
        System.out.println("Getting details of all demons from GDDL Sheet...");
        startTime = System.currentTimeMillis();
        HashMap<String, Demon> demonLookup = sheetGanderer.parseSheet();
        System.out.println(
                "Got details of all demons in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds!");
        System.out.println();

        ArrayList<Demon> userCompletedDemons = new ArrayList<>();
        ArrayList<Demon> unratedExtremes = new ArrayList<>();
        ArrayList<Demon> userExtremons = new ArrayList<>();
        FileWriter fw = new FileWriter(new File("List.txt"));
        for (String ID : userCompletedIDs) {
            if (demonLookup.containsKey(ID)) {

                if (demonLookup.get(ID).diffRating > 0) {
                    userCompletedDemons.add(demonLookup.get(ID));

                } else if (demonLookup.get(ID).demonType.equals("Extreme Demon")) {
                    System.out.println("Unrated Extreme - " + demonLookup.get(ID).name);
                    unratedExtremes.add(demonLookup.get(ID));
                }
                if (demonLookup.get(ID).demonType.equals("Extreme Demon")) {
                    userExtremons.add(demonLookup.get(ID));
                }
            }

        }
        explainer.setText("Building list...");
        progressBar.setValue(100);
        System.out.println("Amount of User Demons Found: " + userCompletedDemons.size());
        Collections.sort(userCompletedDemons, new Comparator<Demon>() {
            @Override
            public int compare(Demon o1, Demon o2) {

                return Double.compare(o2.preciseDiffRating, o1.preciseDiffRating);
            }
        });
        Document allDemonHtml = null;
        try {
            allDemonHtml = AllRatedSurveyor.getHtml();
        } catch (Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "sum shit went wrong with the aredl website");
            JOptionPane.showMessageDialog(null, e);
        }

        Element list = allDemonHtml.getElementsByClass("list").get(0);

        // puts unrated extremons in place
        for (Demon vDemon : unratedExtremes) {
            for (int lvl = 0; lvl < userCompletedDemons.size(); lvl++) {
                if (userCompletedDemons.get(lvl).demonType.equals("Extreme Demon")) {

                    int homePlacement = AllRatedSurveyor.getPlacement(list, userCompletedDemons.get(lvl).name);

                    int visitingPlacement = AllRatedSurveyor.getPlacement(list, vDemon.name);
                    if (homePlacement > visitingPlacement) { // if it belongs at tthe top then end loop
                        System.out.println("#" + visitingPlacement + " - " + vDemon.name + " is harder than #"
                                + homePlacement + " - " + userCompletedDemons.get(lvl).name);

                        vDemon.preciseDiffRating = userCompletedDemons.get(lvl).preciseDiffRating;
                        userCompletedDemons.add(lvl, vDemon);
                        lvl = userCompletedDemons.size();

                    }
                }
            }
        }
        // makes extremeons more accurate in place value dude !
        for (Demon vDemon : userExtremons) {
            int startingIndex = userCompletedDemons.indexOf(vDemon);
            userCompletedDemons.remove(vDemon);
            for (int lvl = 0; lvl < userCompletedDemons.size(); lvl++) {
                if (userCompletedDemons.get(lvl).demonType.equals("Extreme Demon")) {

                    int homePlacement = AllRatedSurveyor.getPlacement(list, userCompletedDemons.get(lvl).name);

                    int visitingPlacement = AllRatedSurveyor.getPlacement(list, vDemon.name);

                    if (homePlacement > visitingPlacement) { // if the visiting demon is harder
                        System.out.println("#" + visitingPlacement + " - " + vDemon.name + " is harder than #"
                                + homePlacement + " - " + userCompletedDemons.get(lvl).name);
                        if (vDemon.preciseDiffRating == 0) {
                            vDemon.preciseDiffRating = userCompletedDemons.get(lvl).preciseDiffRating;
                        }

                        userCompletedDemons.add(lvl, vDemon);

                        lvl = userCompletedDemons.size();

                    }
                } else if (!(lvl + 1 < userCompletedDemons.size())) {
                    System.out.println(vDemon.name + " was in the correct space");
                    userCompletedDemons.add(startingIndex, vDemon);
                    lvl = userCompletedDemons.size();
                }

            }

        }
        Demon d;
        String spaces = "                                                           ";
        fw.write("#     Level Name             Demon Type      GDDL Difficulty\n");
        fw.write("================================================================\n");
        for (int i = 0; i < userCompletedDemons.size(); i++) {
            d = userCompletedDemons.get(i);

            fw.write((i + 1) + ". " + spaces.substring(String.valueOf(i + 1).length(), 4) +

                    (d.name + spaces.substring(d.name.length(), 21)) + "- " +
                    d.demonType + spaces.substring(d.demonType.length(), 14) + "- " +
                    d.preciseDiffRating + "\n");
        }

        fw.close();

        System.out.println("done in " + ((System.currentTimeMillis() - totalStartTime) / 1000) + " seconds!");
        System.exit(0);

    }

    public static String Xor(String path, int key) throws IOException {
        // ArrayList<Integer> res = new ArrayList<>();
        // InputStream inputStream = new FileInputStream(new File(path));
        // ByteArrayOutputStream birayoutstream = new ByteArrayOutputStream();
        // int byteRead = -1;

        // while ((byteRead = inputStream.read()) != -1) {
        // res.add(byteRead ^ key);

        // }

        // for (Integer bytey : res) {
        // birayoutstream.write(bytey);
        // }
        // byte[] bytes = birayoutstream.toByteArray();
        // for (byte b : bytes) {
        // System.out.println(b);
        // }

        InputStream iStream = new FileInputStream(path);
        long fileSize = new File(path).length();
        byte[] allBytes = new byte[(int) fileSize];
        int bytesRead = iStream.read(allBytes);
        iStream.close();
        String byteString = new String(allBytes, StandardCharsets.UTF_8);
        for (int bite = 0; bite < allBytes.length; bite++) {
            int bigger = Byte.toUnsignedInt(allBytes[bite]) ^ key;
            allBytes[bite] = ((byte) bigger);

        }

        return new String(allBytes, StandardCharsets.UTF_8);
    }

    public static byte[] Decrypt(String data) throws DataFormatException {
        byte[] preOutput;
        Inflater inflater = new Inflater(true);
        preOutput = Base64.getMimeDecoder()
                .decode(data.replace('-', '+').replace('_', '/').getBytes(StandardCharsets.UTF_8));
        inflater.setInput(preOutput, 10, preOutput.length - 10);
        byte[] outOutput = new byte[preOutput.length];
        inflater.inflate(outOutput);
        return outOutput;

    }

}
