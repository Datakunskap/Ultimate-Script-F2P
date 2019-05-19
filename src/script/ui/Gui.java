package script.ui;

import net.miginfocom.swing.MigLayout;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.Coins;
import script.data.MuleArea;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Gui extends Task {

    private boolean validate = true;

    private JLabel hopCheckLabel;
    private JLabel hopChanceLabel;
    private JLabel hopPopLabel;
    private JCheckBox hopCheck;
    private JTextField hopTimeText;
    private JTextField hopPopText;

    private JLabel wLabel;
    private JTextField mWorld;
    private JButton openB;
    private JLabel mLabel;
    private JLabel m2Label;
    private JLabel kLabel;
    private JTextField muleName;
    private JTextField muleAmount;
    private JComboBox begType;
    private JTextField mKeep;
    private JFileChooser file;
    private JButton startBtn;
    private JLabel lLabel;
    private JLabel bLabel;
    private JList begAmounts;
    private JLabel aLabel;
    private JComboBox startAmount;
    private JLabel t1Label;
    private JLabel t2Label;
    private JTextField min;
    private JTextField max;
    private JButton clear;
    private JLabel tLabel;
    private JLabel timesLabel;
    private JTextField timesText;
    private JLabel posLabel;
    private JComboBox mulePos;
    private JLabel walkLabel;
    private JTextField walkText;
    private JCheckBox hopCheckf2p;

    private String readLines;
    private String[] types = new String[]{"Incremental", "Random"};


    private JFrame frame;

    public Gui() {
        frame = new JFrame("Ultimate Beggar");
        //frame.setLayout(new MigLayout());
        //frame.setPreferredSize(new Dimension(400, 950));

        hopCheckLabel = new JLabel("Wold hop? (members)");
        hopChanceLabel = new JLabel("Hop After __ Minutes Of No Completed Trades:");
        hopPopLabel = new JLabel("Population Of Worlds: >=");
        hopCheck = new JCheckBox();
        hopTimeText = new JTextField();
        hopPopText = new JTextField();

        wLabel = new JLabel("Mules World:");
        mWorld = new JTextField();
        openB = new JButton("Open File");
        posLabel = new JLabel("Select Mule Area:");
        mulePos = new JComboBox(MuleArea.values());
        m2Label = new JLabel("Mules In-Game Name:");
        mLabel = new JLabel("Amount To Mule At:");
        muleName = new JTextField();
        muleAmount = new JTextField();
        kLabel = new JLabel("Amount To Keep From Mule");
        mKeep = new JTextField();
        begType = new JComboBox(types);
        lLabel = new JLabel("Select A Text File With Beg Lines: (ENTER To Separate And $ Gets Gp)");
        tLabel = new JLabel("Begging ChangeAmount Style");
        file = new JFileChooser("C:\\Users\\bllit\\OneDrive\\Desktop");
        bLabel = new JLabel("Hold CTRL To Select Beg Amounts");
        begAmounts = new JList(Coins.values());
        aLabel = new JLabel("Select Starting Beg Amount");
        startAmount = new JComboBox(Coins.values());
        t1Label = new JLabel("Min Time Between Begs (sec)");
        t2Label = new JLabel("Max Time Between Begs (sec)");
        min = new JTextField();
        max = new JTextField();
        hopCheckf2p = new JCheckBox();
        timesLabel = new JLabel("Change Amount 1 In Every __ Begs (random)");
        timesText = new JTextField();
        walkLabel = new JLabel("Walks After 1 In Every __ Begs");
        walkText = new JTextField();
        clear = new JButton("Clear Selected");
        startBtn = new JButton("Start");

        mWorld.setText("301");
        muleName.setText("milleja1");
        muleAmount.setText("50000");
        mulePos.setSelectedIndex(3);
        mKeep.setText("5000");
        min.setText("15");
        max.setText("30");
        begAmounts.setSelectedIndices(new int[]{0, 1, 2, 3, 6, 8, 10, 11});
        begType.setSelectedIndex(0);
        timesText.setText("6");
        walkText.setText("24");
        hopCheckf2p.setSelected(true);
        hopCheck.setSelected(false);
        hopPopText.setText("900");
        hopTimeText.setText("10");

        JPanel p1 = new JPanel(new MigLayout("filly, wrap 2"));
        JPanel p2 = new JPanel(new MigLayout("filly, wrap 2"));

        p1.add(m2Label, "wrap, growx");
        p1.add(muleName, "wrap, growx");
        p1.add(wLabel, "wrap, growx");
        p1.add(mWorld, "wrap, growx");
        p1.add(posLabel, "wrap, growx");
        p1.add(mulePos, "wrap, growx");
        p1.add(mLabel, "wrap, growx");
        p1.add(muleAmount, "wrap, growx");
        p1.add(kLabel, "wrap, growx");
        p1.add(mKeep, "wrap, growx");
        p1.add(lLabel, "wrap, growx");
        p1.add(openB, "wrap, growx");
        p1.add(bLabel, "wrap, growx");
        p1.add(clear, "wrap, growx");
        p1.add(begAmounts, "wrap, growx");
        p2.add(aLabel, "wrap, growx");
        p2.add(startAmount, "wrap, growx");
        p2.add(tLabel, "wrap, growx");
        p2.add(begType, "wrap, growx");
        p2.add(timesLabel, "wrap, growx");
        p2.add(timesText, "wrap, growx");
        p2.add(t2Label, "wrap, growx");
        p2.add(max, "wrap, growx");
        p2.add(t1Label, "wrap, growx");
        p2.add(min, "wrap, growx");
        p2.add(walkLabel, "wrap, growx");
        p2.add(walkText, "wrap, growx");
        p2.add(hopCheckLabel, "wrap, growx");
        p2.add(hopCheck, "wrap, growx");
        p2.add(hopCheckf2p, "wrap, growx");
        p2.add(hopChanceLabel, "wrap, growx");
        p2.add(hopTimeText, "wrap, growx");
        p2.add(hopPopLabel, "wrap, growx");
        p2.add(hopPopText, "wrap, growx");
        p2.add(startBtn, "wrap, growx");

        clear.addActionListener(x -> clearBtnHandler());
        startBtn.addActionListener(x -> startBtnHandler());
        openB.addActionListener(x -> openBtnHandler());

        JPanel contentPane = new JPanel(new MigLayout("filly"));
        contentPane.add(p1, "growy");
        contentPane.add(p2, "growy");

        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(Game.getCanvas());
        frame.pack();

        //frame.setVisible(true);
        autoOpen();
        startBtnHandler();
    }

    @Override
    public boolean validate() {
        return validate;
    }

    @Override
    public int execute() {
        return 1000;
    }

    private void startBtnHandler() {
        if (begType.getSelectedItem().equals("Incremental")) {
            Beggar.iterAmount = true;
        } else {
            Beggar.iterAmount = false;
        }
        Beggar.muleWorld = Integer.parseInt(mWorld.getText());
        Beggar.muleArea = (MuleArea) mulePos.getSelectedItem();
        Beggar.muleName = this.muleName.getText();
        Beggar.muleAmnt = Integer.parseInt(muleAmount.getText());
        Beggar.muleKeep = Integer.parseInt(mKeep.getText());
        Beggar.gpArr = begAmounts.getSelectedValuesList();
        Beggar.gp = (Coins) startAmount.getSelectedItem();
        Beggar.maxWait = Integer.parseInt(max.getText());
        Beggar.minWait = Integer.parseInt(min.getText());
        Beggar.amountChance = Integer.parseInt(timesText.getText());
        Beggar.walkChance = Integer.parseInt(walkText.getText());

        if (hopCheck.isSelected()) {
            Beggar.worldHop = true;
            Beggar.worldPop = Integer.parseInt(hopPopText.getText());
            Beggar.hopTime = Integer.parseInt(hopTimeText.getText());
        }

        if (hopCheckf2p.isSelected()) {
            Beggar.worldHopf2p = true;
            Beggar.worldPop = Integer.parseInt(hopPopText.getText());
            Beggar.hopTime = Integer.parseInt(hopTimeText.getText());
        }

        if (readLines.contains("DEFAULT")) {
            Beggar.defaultLines();
            Beggar.defaultLines = true;
        } else {
            Beggar.convertInputLines(readLines);
            Beggar.inputLines = readLines;
            Beggar.defaultLines = false;
        }

        validate = false;
        frame.setVisible(false);
    }

    private void openBtnHandler() {
        int returnVal = file.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = file.getSelectedFile();

            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(f));
                StringBuilder sb = new StringBuilder();
                String s = br.readLine();

                while (s != null) {
                    sb.append(s);
                    sb.append(System.lineSeparator());
                    s = br.readLine();
                }
                readLines = sb.toString();
                Log.fine("Lines loaded");
                br.close();
            } catch (IOException e) {
                Log.info("File not found");
            }
        } else {
            Log.severe("Open command cancelled by user.");
        }
    }

    private void autoOpen() {

            File f = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\BegLines.txt");

            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(f));
                StringBuilder sb = new StringBuilder();
                String s = br.readLine();

                while (s != null) {
                    sb.append(s);
                    sb.append(System.lineSeparator());
                    s = br.readLine();
                }
                readLines = sb.toString();
                Log.fine("Lines loaded");
                br.close();
            } catch (IOException e) {
                Log.info("File not found");
            }
    }

    private void clearBtnHandler() {
        begAmounts.clearSelection();
    }
}
