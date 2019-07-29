# Locations by Theme API
The solution for travelling is composed of themes, which are "reasons to travel" such as going to the beach or skiing. 
Each theme is a pre-selected set of filters like temperature or P.O.I.

### Installation and Getting Started

The project contains the following external dependencies:

* Amadeus: APIs for the travel industry, including Flights, Hotels, Locations and more. Subscribe for a test account 
on their [website](https://developers.amadeus.com/) and create the following environment variables 
using your Amadeus credentials: AMADEUS_CLIENT_ID and AMADEUS_CLIENT_SECRET

* Calendarific: Public Holidays and Observances API. You can subscribe for free on their [website](https://calendarific.com/).
After subscribing, create an environment variable called "HOLIDAY_API_KEY" (without the quotes) containing your Api Key.

* DarkSky: The Weather API used in this project. Subscribe for free on their [website](https://darksky.net/dev) 
and create an environment variable called "DARKSKY_API_KEY" (without the quotes) containing your Api Key.

* Yelp Fusion: It is a crowd-sourced local business review site that is used to provide ratings, pictures and links of points of interests.
Subscribe for a dev account on their [website](https://www.yelp.com/fusion) and create an environment variable called "YELP_API_KEY" (without the quotes) containing your Api Key.

To run the project, make sure you have Java installed and configured (including JAVA_HOME) and run the following command in the Terminal inside the project's root folder:

 `./mvnw spring-boot:run`


**The front-end part of this project is available** [here](https://github.com/daniloteodoro/travel-by-theme-app)