![FlyInn Logo](https://i.ibb.co/5nnFp9J/black.png)

## Project Description
Remote control and screen sharing for Android devices with a focus on easy pairing and usage on shared third-party hardware.
Seamless access to personal mobile device through an external screen integrated into an external screen, which allows comprehensive control of the device.

---

## Motivation
The world of city mobility is transforming to a more flexible environment. Vehicles are not a person owned item, they are rather shared. The technology presented in the FlyInn project shall improve multimodal mobility and traffic safety.
While using shared bikes, scooters, etc. the user is able to control his mobile without touching it physically at all times. FlyInn allows to put the users in their known habitat instead of putting barriers and unknown interfaces between them and their phone.

---

## Build status
[![Build Status](https://travis-ci.com/amos-flyinn/amos-tub-ws18-proj1.svg?branch=master)](https://travis-ci.com/amos-flyinn/amos-tub-ws18-proj1)

---

## Demo
Click the thumbnail to watch the FlyInn app in action!  
[![Click to watch demo video on YouTube](https://img.youtube.com/vi/Vic-glthkUI/0.jpg)](https://www.youtube.com/watch?v=Vic-glthkUI)


---

## Prerequisites
The minimum API level is API 21 (Android 5 Lollipop) for the client device and API 28 (Android 9 Pie) on the server side.
FlyInn requires the following setting to be enabled on both the client and the server side:
* ADB over Network
* USB debugging

---

## Installation
The installation of FlyInn is as easy as copying the desired *.apk file (client or server) to the device and installing it like any other app.

---

## Features
This section provides an overview of FlyInn's features. It includes features which have not yet been implemented but are scheduled for the final project release. These are marked with a :construction: sign.
### Device Connection
FlyInn implements a client-server architecture:
* Client: user's smartphone
* Server: bicycle's embedded screen (can be another Android smartphone or an Android Things device)

The connection is established as follows:
1. Searching for peers: After starting the app, if there is no established connection yet, both devices search for peers to connect to. If the user's smartphone has identified servers in its vicinity, it provides a list of unique server names to the user.
2. Authentication: After a server device has been chosen for the connection, an authentication token is shown on the client screen. The token must be input on the server device in order to establish the connection.
3. Successful pairing: After the devices are connected, the content of the smartphone screen is shown on the embedded device. The user now has the options of returning to the home screen or closing the connection.

:exclamation: Pressing the power button on the client device to lock the screen disconnects the devices. To ensure correct functionality of the FlyInn App, the client device screen must always be active.

### Screen mirroring
As long as there is a device connection, the smartphone screen will be mirrored on the embedded device. If the user chooses to return to the home screen, the app is still running in the background.
The user can now put his phone away and still see its screen and any changes in real time on the embedded device.

:construction: FlyInn only supports portrait mode. Therefore, landscape mode will be inactive as long as the app is running.

### Input mirroring
Remote interaction with the smartphone is possible through input mirroring. Touch events on the embedded device will be transmitted to the smartphone in real time. The users can now comfortably configure routes in their navigation app, make phone calls, check their e-mail account, etc. -- all while their smartphone is safe in their pocket.

:construction: For Android smartphones which do not have a back-button on the touchscreen, the back-button is simulated on the embedded device to facilitate navigation.

### Configuration
:construction: Some aspects of the app can be configured to fit the user's personal taste:
* Screen ratio: Crop, stretch or pad the image of the smartphone screen on the embedded device
* Proximity sensor: Enable/disable the sensor inside the app
* UI accessibility aspects: Adapt the color scheme and text size to enhance readability

---

## Contributors
The FlyInn application is a product of the [AMOS (Agile Methods and Open Source) project](https://www.qds.tu-berlin.de/menue/lehre/wintersemester/pj_das_amos_projekt/) offered for Computer Science students at the Technische Universität Berlin.

### Project Team
Luk Burchard  
Max Zhao  
Alejandro Jaramillo  
León Viktor Avilés Podgurski  
Erik Zöllner  
Eliza Danila  
Steffen Loos  
Duy Anh Charlie Nguyen  
Abdulrahim Al Methiab  
Sebastian Brito  


---

## LICENSE
The code is submitted under the MIT License described in the [LICENSE](https://github.com/amos-flyinn/amos-tub-ws18-proj1/blob/master/LICENSE) file.
