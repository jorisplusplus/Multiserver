Multiserver
===========

Combine multiple servers as one  
Current minecraft version: 1.7.10  
Current forge version: 10.13.2.1230

This mod reconnects the client, no proxies or passthrough. So it should be compatible with everything.

The server sided mod allows people to switch server with commands and syncs chat with every server
The client side part is required to move automatically to a different server, but it is not required to join a server.

How does it work?
There is one master server that accepts connection from instance servers.
The instance servers have to authenticate first, after auth they are marked as live.
Players can warp to waypoints or join different servers with commands. 
The servers send the playerdata to the target server. The current server messages the player
that it can connect to the new server. The client sided mod reconnects to the different server.
That is the basic explanation.

