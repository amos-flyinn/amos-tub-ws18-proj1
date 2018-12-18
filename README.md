# FlyInn
[logo here]

## Description
Remote control and screen sharing for Android devices with a focus on easy pairing and usage on shared third-party hardware.
Seamless access to personal mobile device through an external screen integrated into an external screen, which allows comprehensive control of the device.

---

## Motivation
The world of city mobility is transforming to a more flexible environment. Vehicles are not a person owned item, they are rather shared. The technology presented in the FlyInn project shall improve multimodal mobility and traffic safety.
While using shared bikes, scooters, etc. the user is able to control his mobile without touching it physically at all times. FlyInn allows to put the users in their known habitat instead of putting barriers and unknown interfaces between them and their phone.

---

## Build status
[![Build Status](https://travis-ci.org/amos-flyinn/amos-tub-ws18-proj1.svg?branch=master)](https://travis-ci.org/amos-flyinn/amos-tub-ws18-proj1)

---
## Demo
[add video here with this tutorial https://stackoverflow.com/questions/11804820/embed-a-youtube-video]

---

## Features
### Device Connection
FlyInn implements a client-server architecture:
* Client: user's smartphone
* Server: bicycle's embedded screen

The connection is established as follows:
1. Searching for peers: After starting the app, if there is no established connection yet, both devices search for peers to connect to. If the user's smartphone has identified servers in its vicinity, it provides a list of unique server names to the user.
2. Authentication: After a server device has been chosen for the connection, an authentication token is shown on the client screen. The token must be input on the server device in order to establish the connection.
3. Successful pairing: After the devices are connected, the content of the smartphone screen is shown on the embedded device. The user now has the options of returning to the home screen or closing the connection.

:bangbang: Pressing the power button on the client device to lock the screen disconnects the devices. To ensure correct functionality of the FlyInn App, the client device screen must always be active.

### Screen mirroring
As long as there is a device connection, the smartphone screen will be mirrored on the embedded device. If the user chooses to return to the home screen, the app is still running in the background.
The user can now put his phone away and still see its screen and any changes in real time on the embedded device.

### Input mirroring


### Configuration

---
## Prerequisites

---
## Installation

---
## Usage

---

## Contributors
The FlyInn application is a product of the AMOS (Agile Methods and Open Source) project offered for Computer Science students at the Technische Universität Berlin.
[link to project site here]

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
The code is submitted under the MIT License described in the LICENSE file.
