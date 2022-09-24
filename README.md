# Fortune Wheel

Digital recreation of a popular Italian Game.

# Technologies
* Java
* Maven
* PostgreSQL

# DESCRIPTION

This is a distributed game that handles threads and concurrency.
UML was used for every step of our software design.
The PostgreSQL database is used to store information about users, phrases, every manche of the game and every move that a player makes in the game. You can check the design of the database in the Diagrams folder
I have used different design patterns like Singleton (server), Observer(player and game server) and Proxy(menu server and game server) patterns.
Google SMTP was used to send email to users and admin.
I used CSV files to add new Phrases to the database.
