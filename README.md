# Soccer Scores

Soccer Scores is an Android application that tracks past and future Soccer matches from various European leagues.

All team Logos are acquired from [hdlogo.wordpress.com](http://hdlogo.wordpress.com/) upon requesting permission form the owner. The original version of this app was created by Yahia Khalid Ibn El Walid Ahmed, and a modified version is given to Udacity students as part of a project for [Udacity's Android Nanodegree](https://www.udacity.com/nanodegrees-new-s/nd801) Program, where you can take Android Programming courses online.

## Pre-requisites
  * Android SDK 21 or Higher
  * Build Tools version 21.1.2
  * Android Support AppCompat 22.2.0
  * Android Support Annotations 23.1.1

## FootballData API Key is required

In order for the Soccer Scores app to function properly, an API key for [football-data.org](http://api.football-data.org/index/) must be included

You can obtain a key by following these [instructions](http://api.football-data.org/register). Include the unique key by adding the following line to app/src/main/res/values/strings.xml

`<string name="api_key">[UNIQUE_API_KEY]</string>`
