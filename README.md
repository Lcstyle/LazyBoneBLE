# LazyBoneBLE 

This is a fork of the original tinyoshop LazyBone BLE Android App

https://www.tinyosshop.com/smartphone-switch-lazybone-v5-bluetooth

Features it enables:
1. When you change the name of the LazyBone-BLE device, the device is registered as a registered device (saved into preferences).
2. Registered Devices are Auto-Started (auto-connected to and switch turned on) when the App is launched
3. There is a blueTooth onConnect device Intent receiver which looks for a specific BlueTooth device, when the app detects this blueTooth device as connected, 
it launches a high priority notification which you can tap on, this launches the LazyBone BLE app which then auto-connects and auto-turns on the switch
4. There is also the ability to select a home location.  When the home location is registered, a geoFence is created around the home location.  To
register a home location, simply press and hold on the map activity under settings.
5. if the device is currently connected and is in status Off, after exiting the home location geoFence the device will automatically be turned on.
6. when entering the home geoFence, after 2 minutes (120 seconds) inside the home geoFence, the device will be auto-shutoff (auto-power off)
