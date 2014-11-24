# A vert-x module to move socket/streams to the eventBus

The idea behind this verticle is to handle client-io (connections to 3:rd party systems) in a central verticle, and then abstract it over the eventBus so it can run on another server.
Initial benefits is that during development of IRC-client, or other (initially text based) protocols, reloading code will not require loosing the connection.

My idea is to run this module on a cloud-node, and during development, connect to it without bothering the poor lads on irc / whatever when I redeploy code to handle that protocol.

Anyways, more readme info once the project is in a stage where you have code to look at.


等一下見
