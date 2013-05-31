This resources folder is mounted at /html on the HTTP server and provides
static HTML and Javascript to support the web interfaces.

Use the following command to update dgrid and its dependencies:
cd js
cpm upgrade dgrid

The js/dojo/ folder is set to be ignored by git. We don't need the dojo dependency
held locally; the HTML pages load it from Google CDN.

The jquery-csv dependency is maintained in this directory
because it is a manual download not available using the cpm mechanism.
Check here to see if the version can be updated:
http://code.google.com/p/jquery-csv/