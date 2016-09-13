package com.digifarm.UI;

import com.digifarm.DBConnection.ConnectionDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;

/**
 * Created by digifarmer on 9/13/16.
 **/
public class ConnectionDialog extends JDialog implements ActionListener, WindowListener {

    private MainUI mainUI;

    //JDialog components
    private JPanel connectionPanel;
    private JTextField user, dbaddress, dbport, dbname;
    private JPasswordField pass;
    private JButton connect;

    public ConnectionDialog(MainUI guiDB) {
        super(guiDB, "Database Connection", true);
        this.mainUI = guiDB;
        //JDialog components creation
        connectionPanel = new JPanel(new GridLayout(6, 1));
        //TODO: change with username
        user = new JTextField("marco");
        //TODO: change with password
        pass = new JPasswordField("");
        dbaddress = new JTextField("localhost");
        dbport = new JTextField("5432");
        //TODO: change with database name
        dbname = new JTextField("mondial");
        connect = new JButton("Connect");
        user.setHorizontalAlignment(JTextField.CENTER);
        dbaddress.setHorizontalAlignment(JTextField.CENTER);
        dbport.setHorizontalAlignment(JTextField.CENTER);
        dbname.setHorizontalAlignment(JTextField.CENTER);
        pass.setHorizontalAlignment(JPasswordField.CENTER);
        //listeners
        connect.addActionListener(this);
        addWindowListener(this);
        //add jDialog components
        connectionPanel.add(user);
        connectionPanel.add(pass);
        connectionPanel.add(dbaddress);
        connectionPanel.add(dbport);
        connectionPanel.add(dbname);
        connectionPanel.add(connect);
        add(connectionPanel);
        //jDialog look configuration
        setSize(250, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ConnectionDialog(new MainUI());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            ConnectionDB conn = new ConnectionDB(user.getText(), new String(pass.getPassword()), dbaddress.getText(), dbport.getText(), dbname.getText());
            mainUI.setDBConnection(conn);
            System.out.println("Connected");
            //show main ui
            mainUI.setVisible(true);
            //close dialog
            dispose();
        } catch(SQLException se) {
            se.printStackTrace();
            JOptionPane.showMessageDialog(this, "SQLException: " + se.getMessage());
        } catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            JOptionPane.showMessageDialog(this, "Driver not found: " + cnfe.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        dispose();
        mainUI.dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }

}