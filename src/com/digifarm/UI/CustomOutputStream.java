package com.digifarm.UI;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by digifarmer on 9/13/16.
 *
 * Utility class needed to redirect stout to JTextArea
 *
 **/
public class CustomOutputStream extends OutputStream {

    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        //redirects text to teh area
        textArea.append(String.valueOf((char) b));
        //scroll text area to the end
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}