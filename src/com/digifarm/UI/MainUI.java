package com.digifarm.UI;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Exec.Main;

import javax.swing.*;
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

   //dbconnection management
    private ConnectionDB conn;
    private String dbName;

    private PrintStream standardOut;

    private JTextArea textArea = new JTextArea();
    private JTextField keyword;
    private JPanel first;
    private JButton search;
    private JLabel title1;


    public MainUI() {
        //configurazione generale del JFrame
        setTitle("Banks by Digifarmer");
        setSize(950, 750);
        setResizable(false);
        setVisible(false);
        //jframe centering
        Dimension dim = getToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
        //layout configuration
        setLayout(new GridLayout(2, 1));
        //end program when click on X
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        standardOut = System.out;

        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);

        first = new JPanel(new GridLayout(4,1));
        title1 = new JLabel("Enter keyword. (Space as separator");
        keyword = new JTextField();
        keyword.setToolTipText("Enter keyword. (Space as separator");
        search = new JButton("Search");
        first.add(title1);
        first.add(keyword);
        first.add(search);
        add(first);
        add(new JScrollPane(textArea));
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                textArea.setText(" ");
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Main.dbSearch(keyword.getText(),conn,dbName);
                    }
                });
                t.start();

            }
        });


    }

    public void setDBConnection(ConnectionDB conn, String dbName) {
        this.conn = conn;
        this.dbName = dbName;

    }


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