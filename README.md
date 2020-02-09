# Laboratorio_B
This was my second project at the university. I had to program server, client and database of the application.

Most of the code is in Common folder.
We wrote a report in Italian to make user understand better the design.

DESCRIPTION

The goal was to recreate the famous italian TV game named "Fortune Wheel".


-->Maven was used to compile, test and run the project. In fact, by downloading the project and run mvn package command in Source Code directory you can build the jar files. If you try to run the server you will fail because you don' t know the email password.

-->UML was used for every step of our software design.

-->A postgreSql database was used to store information about users, phrases, every manche of the game and every move that a player makes in the game.

-->I have created the registration form, a menu to make user interact with the platform and the game itself.

-->I have used signleton(server), observer(player and game server) and proxy(menu server and game server) patterns.

-->Google SMTP was used to send email to users and admin.

-->I used CSV files to add new Phrases to the database.
