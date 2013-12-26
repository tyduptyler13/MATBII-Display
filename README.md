MATBII-Display
==============

A custom MATBII display tool for its output files.

![Build Status](http://ci.myuplay.com/job/MATBII-Display/badge/icon)

Description
----
This program is designed to read through multiple files and directories to find data about any participant.

It will display the information in useful graphs and tabbed output for easy reading. These graphs can be exported as 
images if needed and some of the data can be parsed into one master file for statistics.

How to use
----
Run the jar by double clicking it (It should work if you have java installed). Then use
the open button to open a file directory you would like the program to search. From there,
you will see files in your directory tree on the left. Right click on any node and
you can save the output. Doing this on higher level nodes like folders will save all trials
in that folder.

Known Bugs
----
* None (YAY)

Requirements
----
This program __requires__ version 1.7 update 9 of java or later.

Building
----
You will need to fix your classpath before the first time. After that it will be fine.

To do this you need to run `mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath`.

After that you can build with `mvn jfx:jar` and the files will be placed in target\jfx\app.

There are prebuilt versions on my [jenkins](http://ci.myuplay.com/job/MATBII-Display/).
