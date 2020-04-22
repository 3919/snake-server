# PWR SNAKE PROJECT

## Requirements
 - ```gradle``` you can follow this `https://gradle.org/install/` tutorial. At least version 6.3 is required
 - ```java``` at least version 8 is required
 - ```MariaDB``` server is required with `pwr_snake` db created
 - ```tomEE plume``` download it from `http://tomee.apache.org/download-ng.html` and unpack. No more effort is required 
## Build
 1. `gradle war` this will build project
 2. `cp build/libs/snake-server-1.0-SNAPSHOT.war $TOMEEDIR/webapps/$SOMENAME.war`
 3. `$TOMEEDIR/bin/catalina.sh run` to run the server
 4. Now you can access your app with address `http://localhost:8080/$SOMENAME/$SUBSITE`

