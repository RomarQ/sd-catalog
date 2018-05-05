# SD - Catalog
[ SCHOOL PROJECT ]

Distributed system that simulates a Product Store via RMI services and client communication via Sockets.

# Configuration Instructions  

 > **1.** Set **ServerAddress**, **RMIServerPort**, **RMIClientPort**
 - Change constants located on shared.config file
	> Like this:   
	```java 
	public static String serverAddress = "192.168.1.78"; // your server address  
	public static int RMIServerPort = 1099;
	public static int RMIClientPort = 1100;
	```
> **2.** Set **Policy files** for both ( **Server** and **Client** )
- In [ RUN CONFIGURATIONS ] set the project root as working path and then set the commands  
  below on [ VM OPTIONS ]
	  
	```
	Client [ VM OPTIONS ] :  
	  
	    -Djava.security.policy==src/client/permissions.policy  
	  
	Server [ VM OPTIONS ] :  
	  
	    -Djava.security.policy==src/server/permissions.policy
	```

> **3.** If you are using linux make sure that your **/etc/hosts** is properly configured
- Should look something like this:
	```
	127.0.0.1           localhost  
	yourServerAddress   YourPC
