# Copy as JavaScript Request - Burp Suite Extension

## Overview
This Burp Suite extension allows users to convert HTTP requests into JavaScript fetch code snippets. It adds a context menu item that generates an async/await JavaScript function containing the complete HTTP request, which can be easily copied and used in JavaScript applications.

## Features
- Converts Burp Suite HTTP requests to JavaScript fetch API code
- Maintains all original headers (except Accept-Encoding)
- Handles request bodies
- Generates async/await based JavaScript code
- Includes error handling
- Automatically copies the generated code to clipboard

## Implementation Details

### Main Components

1. **BurpExtender Class**
   - Implements three interfaces:
     - `BurpExtension`: Core interface for Burp extensions
     - `ClipboardOwner`: Handles clipboard operations
     - `ContextMenuItemsProvider`: Provides context menu functionality

2. **Initialization**
   ```java
   public void initialize(MontoyaApi api)
   ```
   - Sets up the extension in Burp Suite
   - Registers the context menu provider
   - Stores the MontoyaApi reference for later use

3. **Context Menu Integration**
   ```java
   public List<Component> provideMenuItems(ContextMenuEvent event)
   ```
   - Adds "Copy as JavaScript request" to the context menu
   - Only appears when a request is selected
   - Triggers code generation when clicked

### Code Generation Process

1. **Header Processing**
   ```java
   private String processHeaders(HttpRequest request)
   ```
   - Converts HTTP headers to JavaScript object format
   - Excludes Accept-Encoding header (handled automatically by fetch)
   - Properly escapes special characters in header values

2. **Body Processing**
   ```java
   private String processBody(HttpRequest request)
   ```
   - Handles request body data
   - Returns null if no body is present
   - Escapes special characters in body content

3. **JavaScript Function Generation**
   ```java
   private String createJSFunctionCall(String url, String httpMethod)
   ```
   - Creates an async function using fetch API
   - Includes error handling with try/catch
   - Logs response and errors to console
   - Returns the response text

4. **String Escaping**
   ```java
   private String escape(String input)
   ```
   - Escapes backslashes and single quotes
   - Ensures generated JavaScript is syntactically valid

## Generated Code Structure

The extension generates JavaScript code in the following format:
```javascript
async function sendRequest() {
  const headers = {
    // Original request headers
  };

  const data = 'request body if present';

  try {
    const response = await fetch('URL', {
      method: 'HTTP_METHOD',
      headers: headers,
      body: data
    });

    const result = await response.text();
    console.log('Response:', result);
    return result;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Call the function
sendRequest().catch(console.error);
```

## Usage

1. Right-click on any request in Burp Suite
2. Select "Copy as JavaScript request" from the context menu
3. The JavaScript code will be automatically copied to your clipboard
4. Paste the code into your JavaScript project or console

## Notes

- The generated code uses modern JavaScript features (async/await)
- The extension automatically handles special characters in headers and body content
- The Accept-Encoding header is intentionally omitted as it's handled by the fetch API
- The generated code includes error handling and console logging
- The response is returned as text and logged to the console

## Technical Requirements

- Burp Suite with Montoya API support
- Java Runtime Environment
- Clipboard access permissions
