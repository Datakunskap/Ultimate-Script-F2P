package script.tanner.ui;

import net.miginfocom.swing.MigLayout;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.StopWatch;
import script.Script;
import script.data.MuleArea;
import script.tanner.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class Gui extends JFrame {

    private Main main;
    private final String[] RESTOCK_OPTIONS = new String[]{"GE Restock", "Kill Cows + Loot Hides", "Loot Hides"};

    private JFrame frame;
    private JPanel p1;
    private JButton buttonStart;
    private JComboBox<String> restockOption;
    private JLabel addHidePriceLabel;
    private JTextField addHidePrice;
    private JLabel subLeatherPriceLabel;
    private JTextField subLeatherPrice;
    private JLabel resetGeTimeLabel;
    private JTextField resetGeTime;
    private JLabel intervalAmntLabel;
    private JTextField intervalAmnt;
    private JLabel muleWorldLabel;
    private JTextField muleWorld;
    private JLabel muleAmntLabel;
    private JTextField muleAmnt;
    private JLabel muleKeepLabel;
    private JTextField muleKeep;
    private JLabel muleAreaLabel;
    private JLabel muleNameLabel;
    private JTextField muleName;
    private JComboBox<MuleArea> muleArea;
    private JLabel restockOptionLabel;
    private JLabel foodLabel;
    private JTextField food;
    private JLabel lootAmountLabel;
    private JTextField lootAmount;
    private JLabel foodAmntLabel;
    private JTextField foodAmnt;

    public Gui(Main main) {
        this.main = main;
        initComponents();
        main.timeRan = StopWatch.start();
        this.setVisible(false);
    }

    private void buttonStartActionPerformed() {
        // ge restock
        if (restockOption.getSelectedItem().equals(RESTOCK_OPTIONS[0])) {
            main.killCows = false;
            main.lootCows = false;
        // kill cows + loot hide
        } else if (restockOption.getSelectedItem().equals(RESTOCK_OPTIONS[1])) {
            main.killCows = true;
            main.lootCows = true;

            main.food = food.getText();
            if(lootAmount != null && foodAmnt != null &&
                    !lootAmount.getText().equals("") && !foodAmnt.getText().equals("")) {
                main.lootAmount = Integer.parseInt(lootAmount.getText());
                main.foodAmnt = Integer.parseInt(foodAmnt.getText());
            }
        // loot hide
        } else {
            main.killCows = false;
            main.lootCows = true;

            main.food = food.getText();
            if(lootAmount != null && !lootAmount.getText().equals(""))
                main.lootAmount = Integer.parseInt(lootAmount.getText());
        }

        if (addHidePrice != null && !addHidePrice.getText().equals(""))
            main.addHidePrice = Integer.parseInt(addHidePrice.getText());
        if (subLeatherPrice != null && !subLeatherPrice.getText().equals(""))
            main.subLeatherPrice = Integer.parseInt(subLeatherPrice.getText());
        if (resetGeTime != null && !resetGeTime.getText().equals(""))
            main.resetGeTime = Integer.parseInt(resetGeTime.getText());
        if (intervalAmnt != null && !intervalAmnt.getText().equals(""))
            main.intervalAmnt = Integer.parseInt(intervalAmnt.getText());

        // Muling - if blank wont mule
        if (muleAmnt != null && muleKeep != null && muleArea != null && muleName != null && muleWorld != null &&
                !muleAmnt.getText().equals("") && !muleKeep.getText().equals("") && !muleWorld.getText().equals("")) {
            main.muleName = Script.MULE_NAME;
            main.muleWorld = Script.MULE_WORLD;
            main.muleArea.setMuleArea(Script.MULE_AREA.getMuleArea());
            main.setRandMuleKeep(85000, 100000);

            /*main.muleAmnt = Integer.parseInt(muleAmnt.getText());
            main.muleKeep = Integer.parseInt(muleKeep.getText());
            main.muleWorld = Integer.parseInt(muleWorld.getText());
            main.muleArea = (MuleArea) muleArea.getSelectedItem();
            main.muleName = muleName.getText();*/
        } else {
            main.muleAmnt = Integer.MAX_VALUE;
            main.muleKeep = Integer.MAX_VALUE;
        }

        frame.setVisible(false);
    }


    private void initComponents() {
        //======== JFrame/JPanel ========
        frame = new JFrame("AIO Tanner");
        p1 = new JPanel(new MigLayout("filly, wrap 2"));

        //======== Instantiation ========
        buttonStart = new JButton("START");

        restockOptionLabel = new JLabel("Hide Restock Method:");
        restockOption = new JComboBox<>(RESTOCK_OPTIONS);
        foodLabel = new JLabel("Food To Use:");
        food = new JTextField();
        foodAmntLabel = new JLabel("Amount Of Food To Keep In Inventory:");
        foodAmnt = new JTextField();
        lootAmountLabel = new JLabel("Number Of Hides To Loot:");
        lootAmount = new JTextField();
        addHidePriceLabel = new JLabel("Increase Set Buying GP Per Hide By:");
        addHidePrice = new JTextField();
        subLeatherPriceLabel = new JLabel("Decrease Set Selling GP Per Leather By:");
        subLeatherPrice = new JTextField();
        resetGeTimeLabel = new JLabel("Time(min) To Increase/Decrease Price:");
        resetGeTime = new JTextField();
        intervalAmntLabel = new JLabel("Amount To Increase/Decrease By Each Interval:");
        intervalAmnt = new JTextField();

        muleNameLabel = new JLabel("Mules In-Game Name:");
        muleName = new JTextField();
        muleWorldLabel = new JLabel("Mules World:");
        muleWorld = new JTextField();
        muleAmntLabel = new JLabel("Amount To Mule At:");
        muleAmnt = new JTextField();
        muleKeepLabel = new JLabel("Amount To Keep From Mule:");
        muleKeep = new JTextField();
        muleAreaLabel = new JLabel("Mule Area:");
        muleArea = new JComboBox<>(MuleArea.values());

        // Auto-GUI Set Values
        restockOption.setSelectedIndex(0);
        addHidePrice.setText("0");
        subLeatherPrice.setText("0");
        resetGeTime.setText("5");
        intervalAmnt.setText("5");
        muleName.setText("drscatman");
        muleWorld.setText("301");
        muleAmnt.setText("200000");
        muleKeep.setText("100000");
        muleArea.setSelectedIndex(0);

        //---- buttonStart ----
        buttonStart.setFocusCycleRoot(true);
        buttonStart.setFont(new Font(".SF NS Text", Font.PLAIN, 20));
        buttonStart.setBackground(new Color(57, 67, 54));
        buttonStart.addActionListener(e -> buttonStartActionPerformed());

        //======== Add to Panel ========
        addDefaultComponents();

        restockOption.addItemListener(this::restockSelectionHandler);

        setFrameSizeAndLoc();
        buttonStartActionPerformed();
    }

    private void addDefaultComponents() {
        p1.add(restockOptionLabel, "wrap, growx");
        p1.add(restockOption, "wrap, growx");
        p1.add(addHidePriceLabel, "wrap, growx");
        p1.add(addHidePrice, "wrap, growx");
        p1.add(subLeatherPriceLabel, "wrap, growx");
        p1.add(subLeatherPrice, "wrap, growx");
        p1.add(resetGeTimeLabel, "wrap, growx");
        p1.add(resetGeTime, "wrap, growx");
        p1.add(intervalAmntLabel, "wrap, growx");
        p1.add(intervalAmnt, "wrap, growx");

        addMuleComponents();
        buttonStart.setBackground(Color.CYAN.brighter());
        p1.add(buttonStart, "wrap, growx");
    }

    private void setFrameSizeAndLoc() {
        JPanel contentPane = new JPanel(new MigLayout("filly"));
        contentPane.add(p1, "growy");

        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(Game.getCanvas());
        frame.pack();
        setLocationRelativeTo(getOwner());
        frame.setVisible(false);
    }

    private void addMuleComponents() {
        muleName.setBackground(Color.CYAN.darker());
        muleWorld.setBackground(Color.CYAN.darker());
        muleArea.setBackground(Color.CYAN);
        muleAmnt.setBackground(Color.CYAN.darker());
        muleKeep.setBackground(Color.CYAN.darker());

        p1.add(muleNameLabel, "wrap, growx");
        p1.add(muleName, "wrap, growx");
        p1.add(muleWorldLabel, "wrap, growx");
        p1.add(muleWorld, "wrap, growx");
        p1.add(muleAreaLabel, "wrap, growx");
        p1.add(muleArea, "wrap, growx");
        p1.add(muleAmntLabel, "wrap, growx");
        p1.add(muleAmnt, "wrap, growx");
        p1.add(muleKeepLabel, "wrap, growx");
        p1.add(muleKeep, "wrap, growx");
    }

    private void restockSelectionHandler(ItemEvent e) {
        String selection = (String) e.getItem();
        if (!selection.equals(RESTOCK_OPTIONS[0])) {
            p1.removeAll();

            p1.add(restockOptionLabel, "wrap, growx");
            p1.add(restockOption, "wrap, growx");
            if (selection.equals(RESTOCK_OPTIONS[1])) {
                p1.add(lootAmountLabel, "wrap, growx");
                p1.add(lootAmount, "wrap, growx");
                p1.add(foodLabel, "wrap, growx");
                p1.add(food, "wrap, growx");
                p1.add(foodAmntLabel, "wrap, growx");
                p1.add(foodAmnt, "wrap, growx");
            } else {
                p1.add(lootAmountLabel, "wrap, growx");
                p1.add(lootAmount, "wrap, growx");
            }
            addMuleComponents();
            p1.add(buttonStart, "wrap, growx");

            p1.updateUI();
            setFrameSizeAndLoc();
        } else {
            p1.removeAll();
            addDefaultComponents();

            p1.updateUI();
            setFrameSizeAndLoc();
        }
    }
}
