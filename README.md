# Daikin
Daikin Controller Console Application

You can control your Daikin air conditioner with installed adapter. Unit may be used in automatic scenarios 
and app allows you to switch it on/off, set up temperature and mode as well as fan speed.

To help you control it supports lookup your network to find Daikin ACs.
Also you may store your Daikin unit status into a log file (date/time, power status, inner temperature, outer temperature)

for example it sends a POST request to http://192.168.0.108//aircon/set_control_info with body
pow=1&mode=4&stemp=20.0&f_rate=B&f_dir=0&shum=0
or via
curl -X 'POST' --trace-ascii -v --data 'pow=1&mode=4&stemp=20.0&f_rate=B&f_dir=0&shum=0' http://192.168.0.108/aircon/set_control_info