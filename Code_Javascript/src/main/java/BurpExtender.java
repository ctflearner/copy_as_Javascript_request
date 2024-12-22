package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements BurpExtension, ClipboardOwner, ContextMenuItemsProvider {
    private final static String EXT_NAME = "Copy as JavaScript request";
    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName(EXT_NAME);
        api.userInterface().registerContextMenuItemsProvider(this);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();

        if (!event.messageEditorRequestResponse().isPresent()) {
            return menuItems;
        }

        HttpRequestResponse requestResponse = event.messageEditorRequestResponse().get().requestResponse();

        JMenuItem contextMenu = new JMenuItem(EXT_NAME);
        contextMenu.addActionListener(e -> buildRequest(requestResponse));
        menuItems.add(contextMenu);

        return menuItems;
    }

    private void buildRequest(HttpRequestResponse requestResponse) {
        StringBuilder jsRequest = new StringBuilder();
        HttpRequest request = requestResponse.request();

        // Add JavaScript fetch with async/await
        jsRequest.append("async function sendRequest() {\n");
        jsRequest.append(processHeaders(request));
        jsRequest.append(processBody(request));
        jsRequest.append(createJSFunctionCall(request.url(), request.method()));
        jsRequest.append("}\n\n");
        jsRequest.append("// Call the function\nsendRequest().catch(console.error);\n");

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(jsRequest.toString()), this);
    }

    private String processHeaders(HttpRequest request) {
        StringBuilder jsHeaders = new StringBuilder();
        jsHeaders.append("  const headers = {\n");

        for (var header : request.headers()) {
            var name = header.name();
            var value = escape(header.value());

            // Skip Accept-Encoding header as fetch handles compression automatically
            if (name.toLowerCase().equals("accept-encoding")) {
                continue;
            }
            jsHeaders.append(String.format("    '%s': '%s',\n", name, value));
        }
        jsHeaders.append("  };\n\n");
        return jsHeaders.toString();
    }

    private String processBody(HttpRequest request) {
        StringBuilder data = new StringBuilder();

        if (request.body().length() == 0) {
            data.append("  const data = null;\n");
            return data.toString();
        }

        data.append(String.format("  const data = '%s';\n", escape(request.body().toString())));
        return data.toString();
    }

    private String createJSFunctionCall(String url, String httpMethod) {
        StringBuilder fetch = new StringBuilder();
        fetch.append("  try {\n");
        fetch.append(String.format("    const response = await fetch('%s', {\n", url));
        fetch.append(String.format("      method: '%s',\n", httpMethod));
        fetch.append("      headers: headers,\n");
        fetch.append("      body: data\n");
        fetch.append("    });\n\n");
        fetch.append("    const result = await response.text();\n");
        fetch.append("    console.log('Response:', result);\n");
        fetch.append("    return result;\n");
        fetch.append("  } catch (error) {\n");
        fetch.append("    console.error('Error:', error);\n");
        fetch.append("    throw error;\n");
        fetch.append("  }\n");
        return fetch.toString();
    }

    private String escape(String input) {
        return input.replace("\\", "\\\\").replace("'", "\\'");
    }
}