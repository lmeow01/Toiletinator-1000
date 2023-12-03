# Toiletinator 1000

## Developer notes

### 1. Google Maps API Key

Google Maps API key is required to use map features. Create one [here](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
and add it to `local.properties` file as `MAPS_API_KEY=your_key`. You may also try adding it to 
`secrets.properties` in case the first option doesn't work.

### 2. Location

You can emulate location in Android Studio heading to your device's `Extended Controls` and setting
a location there.

### 3. Firebase

Firebase Firestore, Authentication, and Storage are used in this project. You need to create your 
own Firebase project and add `google-services.json` file to `app` directory.
