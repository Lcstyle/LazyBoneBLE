# LazyBoneBLE 

This is a fork of the original tinyoshop LazyBone BLE Android App

https://www.tinyosshop.com/smartphone-switch-lazybone-v5-bluetooth

Features it enables:
1. When you change the name of the LazyBone-BLE device, the device is registered as a registered device (saved into preferences).
2. Registered Device is Auto-Started (auto-connected to and switch turned on) when the App is launched
3. There is a blueTooth onConnect device Intent receiver which looks for a specific BlueTooth device (configurable) when the app detects this blueTooth device as connected, 
it launches a high priority notification which you can tap on, this launches the LazyBone BLE app which then auto-connects and auto-turns on the switch
4. There is also the ability to select a home location.  When the home location is registered, a geoFence is created around the home location.  To
register a home location, simply press and hold on the map activity under settings.
5. if the device is currently connected and is in status Off, after exiting the home location geoFence the device will automatically be turned on.
6. when entering the home geoFence, after 2 minutes (configurable) inside the home geoFence, the device will be auto-shutoff (auto-power off)
7. WiFi Connection Manager turns on switch when WiFi network is disconnected, turns off Switch when WiFi Network Connects
8. Settings can choose WiFi or GeoFence automatic switch management based on settings in General Settings (or both)
9. Future Feature includes ability to choose Hybrid (GeoFence + WiFi) intelligent connection management, here's how it is proopsed to work:
When you leave the GeoFence, the WiFi connection manager will be enabled for auto-off, meaning next time you renter the geoFence, if WiFi reconnects, then the device will be disabled.  The benefit is that WiFi connects/disconnects will be ignored if you do not exit the home geoFence location to prevent false triggering.
