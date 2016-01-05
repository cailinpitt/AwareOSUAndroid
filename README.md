# AwareOSU Android Application

### [What is AwareOSU?](https://github.com/CailinPitt/AwareOSU#awareosu)

# News

Development began December 2015, with a planned release date in February 2016.

# Features
* Retrieve crime information from around the university area using [jsoup](http://jsoup.org/)
* Search crime information for specific days
* View crime locations on Google Map
* Choose whether to receive daily reminder to read crime information

# What's going on with the code?

There are three activities:
* SplashScreen
  * First screen the user is presented with, displays white background with AwareOSU logo on top. The purpose of this screen is to web scrape crime information in the background, then pass the information to MyActivity (the main activity).
* MyActivity
  * Second screen the user is presented with, displays crime information and allows user to view map of crime locations, choose date to view crime information, or access settings page.
* UserSettingsActivity
  * Settings activity, allow user to specify notification preference.

And two fragments:
* DatePickerFragment
  * Allows the user to search for crime information for a specific day. This fragment is implemented in a DialogFragment. When the user selects a day to view, this fragment quickly web scrapes the crime information for that specific day and updates MyActivity with the requested information.
* MapFragment
  * This fragment displays crime locations on a Google Map.
