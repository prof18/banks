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

package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarmer on 9/30/16.
 **/
public class Levels {

    /*
    *   Key     --> The Backward node
    *   Value   --> List of the nodes pointed by the key
    *
    *   We find the backward nodes starting from the node in the list. We populate
    *   the map in this way, because it'll be more easy to create the edge.
    */
    private HashMap<Node, ArrayList<Node>> backward = new HashMap<>();
    /*
    *   Key     --> The input node
    *   Value   --> List of the forward nodes of the key
    */
    private HashMap<Node, ArrayList<Node>> forward = new HashMap<>();

    public HashMap<Node, ArrayList<Node>> getBackward() {
        return backward;
    }

    public HashMap<Node, ArrayList<Node>> getForward() {
        return forward;
    }

}
