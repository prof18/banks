/*
*   Copyright 2017 Marco Gomiero, Luca Rossi
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

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