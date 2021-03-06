/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.samples.hello;

import org.teavm.dom.ajax.XMLHttpRequest;
import org.teavm.dom.browser.Window;
import org.teavm.dom.html.HTMLButtonElement;
import org.teavm.dom.html.HTMLDocument;
import org.teavm.dom.html.HTMLElement;
import org.teavm.jso.JS;

public final class Client {
    private static Window window = (Window) JS.getGlobal();
    private static HTMLDocument document = window.getDocument();
    private static HTMLButtonElement helloButton = (HTMLButtonElement) document.getElementById("hello-button");
    private static HTMLElement responsePanel = document.getElementById("response-panel");
    private static HTMLElement thinkingPanel = document.getElementById("thinking-panel");

    private Client() {
    }

    public static void main(String[] args) {
        helloButton.addEventListener("click", evt -> sayHello());
    }

    private static void sayHello() {
        helloButton.setDisabled(true);
        thinkingPanel.getStyle().setProperty("display", "");
        final XMLHttpRequest xhr = window.createXMLHttpRequest();
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                receiveResponse(xhr.getResponseText());
            }
        });
        xhr.open("GET", "hello");
        xhr.send();
    }

    private static void receiveResponse(String text) {
        HTMLElement responseElem = document.createElement("div");
        responseElem.appendChild(document.createTextNode(text));
        responsePanel.appendChild(responseElem);
        helloButton.setDisabled(false);
        thinkingPanel.getStyle().setProperty("display", "none");
    }
}
