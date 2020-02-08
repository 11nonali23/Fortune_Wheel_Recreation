# Laboratorio_B
This project was my second one at the university. It is a recreation of Fortune Wheel Game (Client, Server and Database).


DESCRIPTION

In this project the goal was to recreate an online game of the famous Wheel Of Fortune game.We wrote a report in Italian to 
make user understand better the design.

We used maven to compile, test and run the project: by downloading the project and run mvn package command in Source Code directory you can have jar files in the target folder of ServerRdF, AdminRdF and PlayerRdF. To have the email password you need to contat us at nonali.andrea@gmail.com.

We needed to create the registration form, a menu to make user interact with the platform and the game itself.

We use a postgresql database to store every action of a user of the game.

We have used signleton(server), observer(player and game server) and proxy(menu server and game server) patterns.

We use STMP to send email to users and admin.

We use CSV files to add new Phrases to the database.
