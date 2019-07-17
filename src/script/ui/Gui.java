package script.ui;

import net.miginfocom.swing.MigLayout;
import org.rspeer.runetek.api.Game;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.Coins;
import script.data.MuleArea;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class Gui extends Task {

    private boolean validate = true;

    private JCheckBox hopCheck;
    private JTextField hopTimeText;
    private JTextField hopPopText;

    private JTextField mWorld;
    private JTextField muleName;
    private JTextField muleAmount;
    private JComboBox<String> begType;
    private JTextField mKeep;
    //private JFileChooser file;
    private JList<Coins> begAmounts;
    private JComboBox<Coins> startAmount;
    private JTextField min;
    private JTextField max;
    private JTextField timesText;
    private JComboBox<MuleArea> mulePos;
    private JTextField walkText;
    private JCheckBox hopCheckf2p;

    private String readLines;


    private JFrame frame;
    private Beggar main;

    public Gui(Beggar beggar) {
        main = beggar;
        frame = new JFrame("Ultimate Beggar");
        //frame.setLayout(new MigLayout());
        //frame.setPreferredSize(new Dimension(400, 950));

        JLabel hopCheckLabel = new JLabel("Wold hop? (members)");
        JLabel hopChanceLabel = new JLabel("Hop After __ Minutes Of No Completed Trades:");
        JLabel hopPopLabel = new JLabel("Population Of Worlds: >=");
        hopCheck = new JCheckBox();
        hopTimeText = new JTextField();
        hopPopText = new JTextField();

        JLabel wLabel = new JLabel("Mules World:");
        mWorld = new JTextField();
        JButton openB = new JButton("Open File");
        JLabel posLabel = new JLabel("Select Mule Area:");
        mulePos = new JComboBox<>(MuleArea.values());
        JLabel m2Label = new JLabel("Mules In-Game Name:");
        JLabel mLabel = new JLabel("Amount To Mule At:");
        muleName = new JTextField();
        muleAmount = new JTextField();
        JLabel kLabel = new JLabel("Amount To Keep From Mule");
        mKeep = new JTextField();
        String[] types = new String[]{"Incremental", "Random"};
        begType = new JComboBox<>(types);
        JLabel lLabel = new JLabel("Select A Text File With Beg Lines: (ENTER To Separate And $ Gets Gp)");
        JLabel tLabel = new JLabel("Begging ChangeAmount Style");
        //file = new JFileChooser("C:\\Users\\bllit\\OneDrive\\Desktop");
        JLabel bLabel = new JLabel("Hold CTRL To Select Beg Amounts");
        begAmounts = new JList<>(Coins.values());
        JLabel aLabel = new JLabel("Select Starting Beg Amount");
        startAmount = new JComboBox<>(Coins.values());
        JLabel t1Label = new JLabel("Min Time Between Begs (sec)");
        JLabel t2Label = new JLabel("Max Time Between Begs (sec)");
        min = new JTextField();
        max = new JTextField();
        hopCheckf2p = new JCheckBox();
        JLabel timesLabel = new JLabel("Change Amount 1 In Every __ Begs (random)");
        timesText = new JTextField();
        JLabel walkLabel = new JLabel("Walks After 1 In Every __ Begs");
        walkText = new JTextField();
        JButton clear = new JButton("Clear Selected");
        JButton startBtn = new JButton("Start");

        mWorld.setText("301");
        muleName.setText("drscatman");
        muleAmount.setText("100000");
        mulePos.setSelectedIndex(0);
        mKeep.setText("5000");
        min.setText("20");
        max.setText("35");
        begAmounts.setSelectedIndices(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        begType.setSelectedIndex(0);
        timesText.setText("5");
        walkText.setText("24");
        hopCheckf2p.setSelected(true);
        hopCheck.setSelected(false);
        hopPopText.setText("800");
        hopTimeText.setText("7");

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

        //clear.addActionListener(x -> clearBtnHandler());
        //startBtn.addActionListener(x -> startBtnHandler());
        //openB.addActionListener(x -> openBtnHandler());

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
        main.iterAmount = Objects.equals(begType.getSelectedItem(), "Incremental");
        main.muleWorld = Integer.parseInt(mWorld.getText());
        main.muleArea = (MuleArea) mulePos.getSelectedItem();
        main.muleName = this.muleName.getText();
        main.muleAmnt = Integer.parseInt(muleAmount.getText());
        main.muleKeep = Integer.parseInt(mKeep.getText());
        main.gpArr = begAmounts.getSelectedValuesList();
        main.gp = (Coins) startAmount.getSelectedItem();
        main.maxWait = Integer.parseInt(max.getText());
        main.minWait = Integer.parseInt(min.getText());
        main.amountChance = Integer.parseInt(timesText.getText());
        main.walkChance = Integer.parseInt(walkText.getText());

        if (hopCheck.isSelected()) {
            main.worldHop = true;
            main.worldPop = Integer.parseInt(hopPopText.getText());
            main.hopTime = Integer.parseInt(hopTimeText.getText());
        }

        if (hopCheckf2p.isSelected()) {
            main.worldHopf2p = true;
            main.worldPop = Integer.parseInt(hopPopText.getText());
            main.hopTime = Integer.parseInt(hopTimeText.getText());
        }

        if (readLines.contains("DEFAULT")) {
            main.defaultLines();
            main.defaultLines = true;
        } else {
            main.convertInputLines(readLines);
            main.inputLines = readLines;
            main.defaultLines = false;
        }

        validate = false;
        frame.setVisible(false);
    }

    /*private void openBtnHandler() {
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
    }*/

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
