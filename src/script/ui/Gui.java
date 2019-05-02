package script.ui;

import net.miginfocom.swing.MigLayout;
import org.rspeer.runetek.api.Game;
import org.rspeer.script.task.Task;
import script.Beggar;
import script.data.Coins;

import javax.swing.*;
import java.awt.*;

public class Gui extends Task {

    private boolean validate = true;

    private JLabel mLabel;
    private JLabel m2Label;
    private JLabel kLabel;
    private JTextField muleName;
    private JTextField muleAmount;
    private JComboBox begType;
    private JTextField mKeep;
    private JTextArea line;
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

    private String[] types = new String[] {"Incremental", "Random"};


    private JFrame frame;

    public Gui(){
        frame = new JFrame("Ultimate Beggar");
        frame.setLayout(new MigLayout());
        frame.setPreferredSize(new Dimension(300, 825));

        m2Label = new JLabel("Mules In-Game Name:");
        mLabel = new JLabel("Change Amount To Mule At:");
        muleName = new JTextField();
        muleAmount = new JTextField();
        kLabel = new JLabel("ChangeAmount To Keep From Mule");
        mKeep = new JTextField();
        begType = new JComboBox(types);
        lLabel = new JLabel("Enter Beg Lines Here:");
        tLabel = new JLabel("Begging ChangeAmount Style");
        line = new JTextArea(20, 20);
        bLabel = new JLabel("Hold CTRL To Select Beg Amounts");
        begAmounts = new JList(Coins.values());
        aLabel = new JLabel("Select Starting Beg ChangeAmount");
        startAmount = new JComboBox(Coins.values());
        t1Label = new JLabel("Min Time Between Begs (sec)");
        t2Label = new JLabel("Max Time Between Begs (sec)");
        min = new JTextField();
        max = new JTextField();
        timesLabel = new JLabel("Change Amount 1 In Every __ Begs (random)");
        timesText = new JTextField();
        clear = new JButton("Clear Selected");
        startBtn = new JButton("Start");


        JScrollPane scrollPane = new JScrollPane(line);

        muleAmount.setText("25000");
        mKeep.setText("5000");
        line.setText("DEFAULT");
        min.setText("15");
        max.setText("25");
        begAmounts.setSelectedIndices(new int[] {0,5,7,9,10});
        begType.setSelectedIndex(1);
        timesText.setText("10");

        frame.add(m2Label, "wrap, growx");
        frame.add(muleName, "wrap, growx");
        frame.add(mLabel, "wrap, growx");
        frame.add(muleAmount, "wrap, growx");
        frame.add(kLabel, "wrap, growx");
        frame.add(mKeep, "wrap, growx");
        frame.add(lLabel, "wrap, growx");
        frame.add(line, "wrap, growx");
        frame.add(bLabel,"wrap, growx");
        frame.add(clear,"wrap, growx");
        frame.add(begAmounts, "wrap, growx");
        frame.add(aLabel,"wrap, growx");
        frame.add(startAmount,"wrap, growx");
        frame.add(tLabel, "wrap, growx");
        frame.add(begType, "wrap, growx");
        frame.add(timesLabel, "wrap, growx");
        frame.add(timesText, "wrap, growx");
        frame.add(t2Label,"wrap, growx");
        frame.add(max, "wrap, growx");
        frame.add(t1Label, "wrap, growx");
        frame.add(min, "wrap, growx");
        frame.add(startBtn, "growx");

        clear.addActionListener(x -> clearBtnHandler());
        startBtn.addActionListener(x -> startBtnHandler());

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
        if(begType.getSelectedItem().equals("Incremental")){
            Beggar.iterAmount = true;
        } else {
            Beggar.iterAmount = false;
        }
        Beggar.muleName = this.muleName.getText();
        Beggar.muleAmnt = Integer.parseInt(muleAmount.getText());
        Beggar.muleKeep = Integer.parseInt(mKeep.getText());
        Beggar.gpArr = begAmounts.getSelectedValuesList();
        Beggar.gp = (Coins) startAmount.getSelectedItem();
        Beggar.maxWait = Integer.parseInt(max.getText());
        Beggar.minWait = Integer.parseInt(min.getText());
        Beggar.amountChance = Integer.parseInt(timesText.getText());

        if(line.getText().contains("DEFAULT")) {
            Beggar.loadLines();
        }

        validate = false;
        frame.setVisible(false);
    }

    private void clearBtnHandler() {
        begAmounts.clearSelection();
    }
}
