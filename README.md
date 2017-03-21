# AutoPark

> Amazon Go for Parking Lots

## Problems

- Kiosks and gateway employees add labour costs
- Paper tickets have an environmental impact
- Tickets are easy to lose
- Automated systems still require labour to check tickets on dashboards
- One can take multiple tickets to try get the latest time

## Solution: AutoPark

- Android system that uses cameras to take a of the vehicle on entry and exit of the parking garage, recording time for payment purposes
- Stores pictures on Amazon S3
- Uses Machine Learning (Google's Tensorflow) to determine similarity between vehicles
- Swipe your credit card using Square Payments reader on entrance and you are paid on exit
- Keep track of remaining spots

## Features

- If you avoid the camera on the way out, you are charged the maximum


- Being an Android app, it receives Over The Air Updates via the play store
- Compatible with wide amount of Android-supported devices that already have cheap, high-quality cameras
- Closed Circuit Camera can be used for surveillance by law enforcement

## Future Innovations

- NFC payments support
- More memes
- Broadcast available quantity of spots online
- A real gate

## Objects for database

* Vehicle inside lot:
  * Credit Card: will most probably be stored through payment processor
  * Plate Number
  * Time In
  * Time Out: optional
* Lot data:
  * Available Spots
  * Max Spots
  * Pricing Structure

## Setup

1. Install the Android apk onto your phone
2. Open http://park4pepe.com/
3. Put your square reader into your phone

