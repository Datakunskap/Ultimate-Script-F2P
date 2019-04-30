package script.gui;

/*
 * Created by JFormDesigner on Fri Mar 01 07:42:49 GMT 2019
 */

import org.rspeer.ui.Log;
import script.tasks.Mule;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.*;

/**
 * @author Zak Smith
 */
public class GUI extends JFrame {

    public static String Username;
    public static String Password;

    public GUI() {

    }

    private Mule ctx;

    public GUI(Mule main) {
        initComponents();
        this.ctx = main;


    }

    private void button1ActionPerformed(ActionEvent e) {
        // TODO add your code here
        ctx.setStartScript(true);
        Username = textField1.getText();
        Password = textField2.getText();
        Log.info("Credentials Set");

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Zak Smith
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        textField1 = new JTextField();
        textField2 = new JTextField();
        button1 = new JButton();
        label4 = new JLabel();
        spinner1 = new JSpinner();
        button2 = new JButton();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        setTitle("Mule CP");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label1 ----
        label1.setText("Mule Credentials");
        contentPane.add(label1);
        label1.setBounds(35, 10, 175, label1.getPreferredSize().height);

        //---- label2 ----
        label2.setText("Username");
        contentPane.add(label2);
        label2.setBounds(35, 40, 155, label2.getPreferredSize().height);

        //---- label3 ----
        label3.setText("Password");
        contentPane.add(label3);
        label3.setBounds(40, 105, 130, label3.getPreferredSize().height);
        contentPane.add(textField1);
        textField1.setBounds(35, 65, 280, textField1.getPreferredSize().height);

        //---- textField2 ----
        textField2.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        contentPane.add(textField2);
        textField2.setBounds(35, 130, 280, 30);

        //---- button1 ----
        button1.setText("Set credentials");
        button1.addActionListener(e -> button1ActionPerformed(e));
        contentPane.add(button1);
        button1.setBounds(150, 10, 135, 40);

        //---- label4 ----
        label4.setText("Auto mule at:");
        contentPane.add(label4);
        label4.setBounds(680, 40, 120, label4.getPreferredSize().height);

        //---- spinner1 ----
        spinner1.setToolTipText("Set the amount to auto mule accounts at");
        spinner1.setModel(new SpinnerNumberModel(1000000, null, null, 100000));
        contentPane.add(spinner1);
        spinner1.setBounds(635, 65, 180, spinner1.getPreferredSize().height);

        //---- button2 ----
        button2.setText("SET");
        contentPane.add(button2);
        button2.setBounds(680, 105, 85, 35);

        //======== scrollPane1 ========
        {

            //---- table1 ----
            table1.setModel(new DefaultTableModel(
                    new Object[][] {
                            {"Acc name", "Status", "Magic level", "KPH", "GPH"},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                    },
                    new String[] {
                            null, null, null, null, null
                    }
            ) {
                boolean[] columnEditable = new boolean[] {
                        false, true, true, true, true
                };
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnEditable[columnIndex];
                }
            });
            table1.setFont(new Font("Arial", Font.BOLD, 14));
            table1.setFillsViewportHeight(true);
            table1.setForeground(Color.green);
            scrollPane1.setViewportView(table1);
        }
        contentPane.add(scrollPane1);
        scrollPane1.setBounds(0, 175, 870, 450);

        { // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Zak Smith
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JTextField textField1;
    private JTextField textField2;
    private JButton button1;
    private JLabel label4;
    private JSpinner spinner1;
    private JButton button2;
    private JScrollPane scrollPane1;
    private JTable table1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}