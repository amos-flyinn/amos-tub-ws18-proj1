![FlyInn Logo](https://i.ibb.co/5nnFp9J/black.png)  

[![Build Status](https://travis-ci.com/amos-flyinn/amos-tub-ws18-proj1.svg?branch=master)](https://travis-ci.com/amos-flyinn/amos-tub-ws18-proj1) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed6d8f59b86944b9b80775de6a8028c1)](https://app.codacy.com/app/lvap/amos-tub-ws18-proj1?utm_source=github.com&utm_medium=referral&utm_content=amos-flyinn/amos-tub-ws18-proj1&utm_campaign=Badge_Grade_Dashboard) [![MIT](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/amos-flyinn/amos-tub-ws18-proj1/blob/master/LICENSE)

## Project Description
Remote control and screen sharing for Android devices with a focus on easy pairing and usage on shared third-party hardware.
Seamless access to the personal mobile device through an external screen integrated into a bicycle dashboard, which allows comprehensive control of the device.

## Motivation
The world of city mobility is transforming to a more flexible environment. Vehicles are not a person owned item, they are rather shared. The technology presented in the FlyInn project shall improve multimodal mobility and traffic safety.
While using shared bikes, scooters, etc. the user is able to control his mobile without touching it physically at all times. FlyInn allows to put the users in their known habitat instead of putting barriers and unknown interfaces between them and their phone.

## Demo
[![Click to watch demo video on YouTube](https://img.youtube.com/vi/Vic-glthkUI/0.jpg)](https://www.youtube.com/watch?v=Vic-glthkUI)  
Click the thumbnail to watch the FlyInn app in action!

## Prerequisites

*   API level: at least 21 (Android 5 Lollipop) for the client, at least 25 (Android 7 Nougat) for the server
*   Google Play Services: version 7.8.0 or higher
*   Enabled settings: ADB over Network, USB debugging
*   Permissions: access location (since the apps use Android Nearby)

## Features
FlyInn provides two Android applications, the "FlyInn client app" and the "FlyInn server app". A device running the "server app" is able to control devices running the "client app" after connecting to them via a 4-digit code.

### Device Connection
FlyInn uses the following nomenclature:
*   Client: user's smartphone
*   Server: embedded screen on a vehicle (may be simulated by another Android smartphone) such as an Android Things device

The connection is established as follows:
1. Searching for peers: After starting the apps, devices search for peers to connect to.
2. Authentication: A random 4-digit authentication token is shown on client screens. The token must be input on a server device in order to establish a connection.
3. Successful pairing: After the devices are connected, a success message appears and the content of the client smartphone screen is shown on the embedded server device. The user now has the options of returning to the home screen or closing the connection.

:exclamation: Locking the screen on client (or server) devices may close the connection. To ensure correct functionality of the FlyInn app, it is recommended that the device screens always be active.

### Screen mirroring
As long as there is a device connection, the client smartphone screen will be mirrored on the embedded server device. If the user chooses to return to the home screen, the app is still running in the background.
The user can now put their phone away and still track its screen and fully interact with it in real time through the embedded server device.

### Input mirroring
Remote interaction with the smartphone is possible through input mirroring. Touch events on the embedded server device will be transmitted to the smartphone in real time. The users are now able to comfortably configure routes in their navigation apps, send text messages, check their e-mail account, etc. --- all while their smartphone is safe in their pocket.

### Status
The current state of the app on both devices is shown in the form of notifications. The server app shows a notification when initializing the app.  
The client app differentiates between four states:
*   Initializing
*   Searching for devices to connect to
*   Stopped networking
*   Shutting down


## Installation
To install the FlyInn client or server app, copy the desired *.apk file onto a device and install them like any other app.

## Troubleshooting
If the app freezes or crashes, force stop the app (from Settings -> Applications -> Application manager) before restarting it to ensure a clean restart.

## Contributors
The FlyInn application is a product of the [AMOS (Agile Methods and Open Source) project](https://www.qds.tu-berlin.de/menue/lehre/wintersemester/pj_das_amos_projekt/) offered for Computer Science students at the Technische Universität Berlin. The industry partner for the project is the [IAV Digital Lab](https://www.iav.com/en/digital-lab#overview).

### Project Team
Abdulrahim Al Methiab  
León  
Luk Burchard  
Eliza Danila  
Alejandro Jaramillo  
Steffen Loos  
Duy Anh Charlie Nguyen  
Max Zhao  
Erik
