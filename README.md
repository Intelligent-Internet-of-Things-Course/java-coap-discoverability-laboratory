# Java - Laboratory CoAP

This project is associated to the CoAP Discoverability Laboratory in order to design and create a simple CoAP application emulating
an IoT scenario where a CoAP client interacts with a CoAP Coffee Machine in order to:

- Read Temperature (GET Request)
- Read the presence of a Capsule in the machine (GET Request)
- Make a default Coffee (POST Request)
- Make a custom Coffee (Short, Medium, Long) (PUT Request)
- All the defined resource support CoRE Link Format, CoRE Interfaces and Resource Discovery through /.well-known/core


Furthermore, the project defines an additional automatic client able to discover available resource on a target CoAP endpoint,
validate the available resources through the resource type (rt) field and then make a coffee :)

The project use the Python Library AioCoAP that can be installed and imported through PyCharm or the pip install command.