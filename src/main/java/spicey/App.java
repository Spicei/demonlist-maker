package spicey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;
import javax.swing.*;

import spicey.SheetsGanderer.Demon;

import java.util.Base64;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
    // k25 - is demon?
    // k76 - type of demon (6=extreme?)
    // k2 - name of level
    // k3 - description (in base 64)

    public static void main(String[] args) throws IOException, DataFormatException, GeneralSecurityException {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());

        frame.setSize(new Dimension((int) (screen.width * 0.20), (int) (screen.height * 0.1)));
        frame.setDefaultCloseOperation(3); // 3 = exit on close
        frame.setResizable(false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setForeground(java.awt.Color.green);

        progressBar.setValue(0);
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
        frame.setIconImage(ImageIO.read(URI.create("https://i.redd.it/9pvybldgjym51.jpg").toURL()));
        frame.setVisible(true);

        String savePath = System.getenv("LOCALAPPDATA") + "\\GeometryDash\\CCGameManager.dat";
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
        OutputStream out = new FileOutputStream(new File("DecryptedSave.txt"));
        for (byte b : fin) {
            out.write(b);
        }
        System.out.println("Save File Decrypted in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds!");
        System.out.println();

        SaveLooker saveLooker = new SaveLooker();
        explainer.setText("Looking for your completed levels in save file....");
        progressBar.setValue(60);
        System.out.println("Looking for your completed levels in save file....");
        startTime = System.currentTimeMillis();
        List<String> userCompletedIDs = saveLooker.parseSave();
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
        FileWriter fw = new FileWriter(new File("List.txt"));
        for (String ID : userCompletedIDs) {
            if (demonLookup.containsKey(ID)) {
                userCompletedDemons.add(demonLookup.get(ID));

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
        ArrayList<Integer> res = new ArrayList<>();
        InputStream inputStream = new FileInputStream(new File(path));
        ByteArrayOutputStream birayoutstream = new ByteArrayOutputStream();
        int byteRead = -1;

        while ((byteRead = inputStream.read()) != -1) {
            res.add(byteRead ^ key);
        }

        for (Integer bytey : res) {
            birayoutstream.write(bytey);
        }
        birayoutstream.toByteArray();
        return new String(birayoutstream.toByteArray(), StandardCharsets.UTF_8);
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
