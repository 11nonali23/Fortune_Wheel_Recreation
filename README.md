# Fortune Wheel

This is a digital recreation of "Ruota della Fortuna", a famous Italian television quiz with prizes. This game was the Italian version of the US format Wheel of Fortune. The requirements for this project were to build a distributed desktop application that could make people interact over the same game board. The most challenging requirement was to build a PostgreSQL database that record every single move of each player in the game. In the Diagrams folder there are detailed diagram for each building block of this project.

# Technologies
* Java
* Maven
* PostgreSQL

# Technical description

This is a distributed game that handles calls from different sources with threads and concurrency with race condition safety.
The PostgreSQL database is used to store information about users, phrases, every manche of the game and every move that a player makes in the game. You can check the design of the database in the Diagrams folder
I have used different design patterns like Singleton (server), Observer(player and game server) and Proxy(menu server and game server) patterns.
Google SMTP was used to send email to users and admin.
I used CSV files to add new Phrases to the database.
