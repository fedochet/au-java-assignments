# Torrent client

Client application for managing torrent downloads.

## Architecture

Client starts as a command line application and waits for user commands. 

In background it talks to torrent tracker and other torrent clients who want to download files that are available on 
this computer.

If you want to share file with others, make sure you make a copy of this file in folder `files` in the client 
application folder before telling about this file to the tracker. 

## Commands

Following commands are available:

* `help` - shows some basic help info for managing the client;
* `list` - prints all files that tracker knows about;
* `stats` - shows statistics about files on this computer;