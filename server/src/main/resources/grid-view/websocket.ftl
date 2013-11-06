<!DOCTYPE html>
<html>

<head>

<title>${entityId}</title>

<link rel="stylesheet" href="/html/js/dgrid/css/dgrid.css"/>
<link rel="stylesheet" href="/html/js/dgrid/css/skins/claro.css"/>
<link rel="stylesheet" type="text/css" href="//ajax.googleapis.com/ajax/libs/dojo/1.8/dijit/themes/claro/claro.css"/>

<style type="text/css">
.dgrid-cell {
  width: 30em;
}
</style>

<script type="text/javascript">
    var baseUrl = location.pathname.replace(/\/[^/]+$/, "/../html/js/")
    dojoConfig= {
        has: {
            "dojo-firebug": true
        },
        parseOnLoad: false,
        foo: "bar",
        async: true,
        // Load dgrid and its dependencies from a local copy.
        // If we were loading everything locally, this would not
        // be necessary, since Dojo would automatically pick up
        // dgrid, xstyle, and put-selector as siblings of the dojo folder.
        packages: [
            { name: "dgrid", location: baseUrl + "dgrid" },
            { name: "xstyle", location: baseUrl + "xstyle" },
            { name: "put-selector", location: baseUrl + "put-selector" }
        ]
    };
</script>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/dojo/1.8.1/dojo/dojo.js"></script>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.js"></script>
<script type="text/javascript" src="/html/jquery.csv-0.71.js"></script>
               
<script type="text/javascript">

var store;
var grid;


require([
	"dojo/domReady!"
], function() {
    initWebSocket();
});

function initWebSocket() {
	if ("WebSocket" in window) {
	  var ws = new WebSocket("ws://localhost:8088/socket/${entityId}");
	  ws.onopen = function() {
	      console.log("WebSocket open");
	  };
	  ws.onmessage = function (evt) {
	      console.log("Received message");
	      //If this is the first message received then setup the
	      //table. Decide on the columns by parsing the CSV header.
          require([
              "dijit/registry",
              "dojo/store/Memory",
              "dgrid/OnDemandGrid",
              "dojo/_base/declare",
              "dgrid/Keyboard",
              "dgrid/extensions/ColumnResizer",
              "dojo/dom",
              "dojo/domReady!"
          ], function(registry, Memory, OnDemandGrid, declare, Keyboard, ColumnResizer) {
		      if(store == null) {
		        //Setup grid columns based on the first CSV response received
		        store = new Memory();
	            columns = $.csv2Array(evt.data)[0];
	            var columnFields = [];
	            $.each(columns, function(i, column) {
	                columnFields.push({field: column, label: column, sortable: true});
	            });
                
                var CustomGrid = declare([OnDemandGrid, Keyboard, ColumnResizer]);
	               
				grid = new CustomGrid({
		            store: store,
		            columns: columnFields
		        }, "grid");
		        
		      }
		      
		      var rows = $.csv2Dictionary(evt.data);
			  $.each(rows, function(i, row){
		       store.put(row);
			  });
			  
			  grid.refresh();
		  
		  }); //end require
      };
	  ws.onclose = function() {
	      console.log("WebSocket closed");
	  };
	} else {
	  alert("No WebSocket support");
	}
}
</script>
</head>

<body class="claro">

<div id="grid"></div>

</body>


</html>