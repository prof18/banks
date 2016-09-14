package com.digifarm.UI;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Exec.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;


/**
 * Created by digifarmer on 9/13/16.
 **/
public class MainUI extends JFrame implements ActionListener, WindowListener {

   //database management
    private ConnectionDB conn;
    private String dbName;

    private JTextArea textArea = new JTextArea();
    private JTextField keyword;
    private JButton search;
    private JScrollPane console;

    public MainUI() {
        //cjFrame configuration
        setTitle("Banks by Digifarmer");
        setSize(700, 550);
        setResizable(false);
        setVisible(false);
        //end program when click on X
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //re-assigns standard output stream and error output stream
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);

        keyword = new JTextField("Enter keyword. Use space as separator",40);
        search = new JButton("Search");

        // creates the GUI
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        //keyword edit text placement
        constraints.insets = new Insets(20, 20, 20, 20);
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        keyword.setPreferredSize( search.getPreferredSize() );
        add(keyword, constraints);

        //search button placement
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(20, 20, 20, 20);
        constraints.anchor = GridBagConstraints.EAST;
        add(search, constraints);

        //console area placement
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        textArea.setBorder(new EmptyBorder(20,20,20,20));
        textArea.setEditable(false);
        console = new JScrollPane(textArea);
        add(console, constraints);

        //button listener
        search.addActionListener(e -> {
            textArea.setText(" ");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Main.dbSearch(keyword.getText(),conn,dbName);
                }
            });
            t.start();
        });
    }

    public void setDBConnection(ConnectionDB conn, String dbName) {
        this.conn = conn;
        this.dbName = dbName;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        conn.closeDBConnection();
        this.dispose();
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