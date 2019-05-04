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

    private String readLines;
    private String[] types = new String[]{"Incremental", "Random"};


    private JFrame frame;

    public Gui() {
        frame = new JFrame("Ultimate Beggar");
        frame.setLayout(new MigLayout());
        frame.setPreferredSize(new Dimension(400, 950));

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
        timesLabel = new JLabel("Change Amount 1 In Every __ Begs (random)");
        timesText = new JTextField();
        clear = new JButton("Clear Selected");
        startBtn = new JButton("Start");

        mWorld.setText("301");
        muleName.setText("milleja1");
        muleAmount.setText("25000");
        mulePos.setSelectedIndex(0);
        mKeep.setText("5000");
        min.setText("15");
        max.setText("20");
        begAmounts.setSelectedIndices(new int[]{0, 5, 7, 9, 10});
        begType.setSelectedIndex(1);
        timesText.setText("10");

        frame.add(m2Label, "wrap, growx");
        frame.add(muleName, "wrap, growx");
        frame.add(wLabel, "wrap, growx");
        frame.add(mWorld, "wrap, growx");
        frame.add(posLabel, "wrap, growx");
        frame.add(mulePos, "wrap, growx");
        frame.add(mLabel, "wrap, growx");
        frame.add(muleAmount, "wrap, growx");
        frame.add(kLabel, "wrap, growx");
        frame.add(mKeep, "wrap, growx");
        frame.add(lLabel, "wrap, growx");
        frame.add(openB, "wrap, growx");
        frame.add(bLabel, "wrap, growx");
        frame.add(clear, "wrap, growx");
        frame.add(begAmounts, "wrap, growx");
        frame.add(aLabel, "wrap, growx");
        frame.add(startAmount, "wrap, growx");
        frame.add(tLabel, "wrap, growx");
        frame.add(begType, "wrap, growx");
        frame.add(timesLabel, "wrap, growx");
        frame.add(timesText, "wrap, growx");
        frame.add(t2Label, "wrap, growx");
        frame.add(max, "wrap, growx");
        frame.add(t1Label, "wrap, growx");
        frame.add(min, "wrap, growx");
        frame.add(startBtn, "wrap, growx");

        clear.addActionListener(x -> clearBtnHandler());
        startBtn.addActionListener(x -> startBtnHandler());
        openB.addActionListener(x -> openBtnHandler());

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(Game.getCanvas());
        frame.pack();

        frame.setVisible(true);
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


    private void clearBtnHandler() {
        begAmounts.clearSelection();
    }
}
