comma-cq
========

A simple continuous query engine, using only the query facilities of the underlying database. Serves up data in CSV format as strings, so your data needs to be &quot;flat&quot; and not strongly typed until it gets to your client.

"Continuous query" just means that the server gives you the initial load of what's in the database and then provides you with ticking real-time updates. It can be seen as an alternative approach to polling a database for updates.
