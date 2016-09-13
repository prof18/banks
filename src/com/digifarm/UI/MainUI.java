package com.digifarm.UI;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Exec.Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private JPanel first, inside;
    private JButton search;
    private JLabel title1 = new JLabel("Enter keyword. Use space as separator");
    private JScrollPane console;

    private Border blackline =  BorderFactory.createLineBorder(Color.black);


    public MainUI() {
        //configurazione generale del JFrame
        setTitle("Banks by Digifarmer");
        setSize(950, 350);
     //   setResizable(false);
        setVisible(false);
        //jframe centering
        Dimension dim = getToolkit().getScreenSize();
        //setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
        //layout configuration
      //  setLayout(new GridLayout(0, 1));
        //end program when click on X
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        standardOut = System.out;

        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);

      //first = new JPanel(new GridLayout(7,1));
     //   first.setBorder(new EmptyBorder(0, 20, 20, 20));
     /* //  first.setBorder(blackline);
        inside = new JPanel(new GridLayout(1,2,100,0));

        title1 = new JLabel("Enter keyword. Use space as separator");
        title1.setHorizontalAlignment(JLabel.CENTER);

        //keyword.setPreferredSize(new Dimension(150, 80));
        */
        keyword = new JTextField();
        search = new JButton("Search");

        // creates the GUI
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

       // add(title1,constraints);

        constraints.gridx = 1;
        constraints.fill=GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(20, 20, 20, 20);
        add(keyword, constraints);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(20, 20, 20, 20);
        constraints.anchor = GridBagConstraints.EAST;

        add(search, constraints);



        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;





        console = new JScrollPane(textArea);
        add(console, constraints);
        textArea.setBorder(new EmptyBorder(20,20,20,20));
        textArea.setEditable(false);
        //textArea.setBorder(BorderFactory.createLineBorder(Color.black));
       /* first.add(title1);
        inside.add(keyword);
        inside.add(search, BorderLayout.EAST);
        first.add(inside);*/
   //     add(first);

       // add(console);

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