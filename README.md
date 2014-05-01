Analytics
====================

Analytics is a small scripted project to gather data from various websites and
services for stats pertaining to NYSenate.gov and the social media accounts of
senators.


Requirements
--------------------

* Maven - mvn2 required.
* Java - java7 preferred, java6 compatible


Installation
--------------------

* Clone the repository
* Run ``bin/setup.sh`` to do a local maven install various dependencies
* Copy ``analytics.ini.example`` to ``analytics.ini`` and fill in the blanks
* mvn clean package
* Run ``bin/cron.sh analytics.ini`` once a month



How it works
--------------------

Analytics uses the NYSenate.gov API to gather the social media links for all of
our current senators. It then uses the various social media APIs to get stats
on their accounts. Livestream channels are gathered from the INI file and google
analytics reports are configured via their respective INI sections.

The Excel Workbook is compiled in two steps. In the first step, each individual
worksheet is generated as a standalone CSV file in the "scatch" folder.  In the
second step, each of these sheets is combined into a single workbook. This workbook
is then emailed to the configured recipients automatically.


Misc. Notes
-------------------

 * For various bad reasons you need to make the scratch and scratch/GA
   folders manually before running Main.java*

