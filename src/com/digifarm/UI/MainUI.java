package com.digifarm.UI;

import com.digifarm.DBConnection.ConnectionDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

/**
 * Created by marco on 9/13/16.
 */
public class MainUI extends JFrame implements ActionListener, WindowListener {

    //indicatori delle caselle di testo
    private final int MSG_TEXT = 1000;

    //oggetto per la gestione della connessione al DB
    private ConnectionDB conn;

    //lista delle query disponibili
    ArrayList<QueryObject> query;
    ArrayList<ComboBoxObject> combo;

    //JPanels
    private JPanel uno, due, tre, queryPanel, msgPanel;
    private JScrollPane sqlScroll, resScroll;
    private JTextArea sqlText;
    private JTextField msgText;
    private JLabel titolo1, titolo2, titolo3;
    private JLabel[] paramLabels;
    private JButton execButton;

    private JComboBox<String> queryList;
    private JComboBox[] paramComboBox;

    private JTable results;

    public MainUI() {
        //configurazione generale del JFrame
        setTitle("DigiFarm GUI");
        setSize(950, 750);
        setResizable(false);
        setVisible(false);
        //centro il JFrame
        Dimension dim = getToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
        //configurazione del layout
        setLayout(new GridLayout(3, 1));
        //popolo la lista delle query e delle combobox disponibili
      /*  populateComboList();
        populateQueryList();*/
        //creo i componenti del JFrame
        //----- Pannello uno -----
        uno = new JPanel(new GridLayout(7, 1));
        queryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        msgPanel = new JPanel(new GridLayout(1, 2));
       /* String[] titoli = new String[query.size()];
        for(int i = 0; i < titoli.length; i++) {
            titoli[i] = query.get(i).title;
        }
        queryList = new JComboBox<>(titoli);
        queryList.setSize(500, queryList.getPreferredSize().height);
        queryList.setMaximumRowCount(titoli.length);*/
        execButton = new JButton("Esegui Query");
        titolo1 = new JLabel("Di seguito \u00e8 visualizzabile la query SQL");
        titolo1.setHorizontalAlignment(JLabel.CENTER);
        titolo1.setForeground(Color.BLUE);
        titolo2 = new JLabel("Di seguito vi sono alcuni parametri per le query parametriche");
        titolo2.setHorizontalAlignment(JLabel.CENTER);
        titolo2.setForeground(Color.BLUE);
        titolo3 = new JLabel("Nota/Messaggio/NuovaPassword");
        titolo3.setHorizontalAlignment(JLabel.CENTER);
        msgText = new JTextField();
        msgText.setEnabled(false);
        sqlText = new JTextArea();
        sqlText.setEditable(false);
   /*   sqlText.setText(query.get(0).sql);
        sqlScroll = new JScrollPane(sqlText, JScrollPane.VERTICAL_SCROLLBAR_NEVER ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //----- Pannello due -----
        int combo_size = combo.size();
        due = new JPanel(new GridLayout(combo_size, 2));
        paramComboBox = new JComboBox[combo_size];
        paramLabels = new JLabel[combo_size];
        ComboBoxObject cbo;
        for(int i = 0; i < combo_size; i++) {
            cbo = combo.get(i);
            paramLabels[i] = new JLabel(cbo.name);
            paramLabels[i].setHorizontalAlignment(JLabel.CENTER);
            paramComboBox[i] = new JComboBox<>(cbo.params);
            paramComboBox[i].setEnabled(false);
        }*/

    }

    public void setDBConnection(ConnectionDB conn) {
        this.conn = conn;
    }






    private void updateGUI() {
        int index = queryList.getSelectedIndex();
        QueryObject qo = query.get(index);
        //copio la query nella JTextArea
        sqlText.setText(qo.sql);
        //disattivo l'inserimento nella JTextField
        msgText.setEnabled(false);
        //disattivo tutti i parametri delle JComboBox
        for(int i = 0; i < paramComboBox.length; i++) {
            paramComboBox[i].setEnabled(false);
        }
        //abilito i parametri utilizzati dalla query
        int[] usedParams = qo.params;
        for(int i = 0; i < usedParams.length; i++) {
            if(usedParams[i] > paramComboBox.length) {
                switch(usedParams[i]) {
                    case MSG_TEXT:
                        msgText.setEnabled(true);
                        break;
                    default:
                        break;
                }
            } else {
                paramComboBox[usedParams[i]].setEnabled(true);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == execButton) {
           // executeQuery();
        } else if(src == queryList) {
            updateGUI();
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
        //chiusura della connessione al DB
        conn.closeDBConnection();
        dispose();
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

    private class QueryObject {
        private String title, sql, fields;
        private int[] params;

        public QueryObject(String title, String sql, String fields, int[] params) {
            this.title = title;
            this.sql = sql;
            this.fields = fields;
            this.params = params;
        }
    }

    private class ComboBoxObject {
        private String name, type;
        private String[] params;

        public ComboBoxObject(String name, String type, String[] params) {
            this.name = name;
            this.type = type;
            this.params = params;
        }
    }
}