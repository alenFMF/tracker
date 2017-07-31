## Instalation  

* Install JDK 8

<http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>

* Install the latest Eclipse IDE for Java EE Developers (Neon 3, 64-bit, 10 Apr 2017)

<https://www.eclipse.org/downloads/?>

* Clone the project from GIT repository

<https://github.com/alenFMF/tracker.git>

* Install postgresql

<https://www.postgresql.org/download/>

   * Set password for user 'progress' and port for PostgreSQL during installation (usually 5432).
   * After installation JDBC driver has to be installed as addition (last step of the installation)
   * Set PATH on the system to the bin directory of the postgres installation (e.g.
C:\Program Files\PostgreSQL\9.6\bin)

```
psql -h localhost -U postgress
```

```
create database gooptigps;
create user goopti with password 'goopti';
grant all privileges on database gooptigps to goopti;
```

* Open project in Eclipse (File - Open project ...)

* Install Jetty server from Eclipse.
    * Help -> Eclipse Marketplace
    * Find and install Jetty

* Configure database connection (port) in file src/main/webapp/WEB-INF/spring/root-context.xml

* Run project
    * Run -> Run
    * Select Run with Jetty (on the bottom of the list)





-----


Projekt sofinancirata Republika Slovenija in Evropska unija iz [Evropskega socialnega sklada](http://www.eu-skladi.si/). 

![Logo](https://github.com/jborlinic/strojno_ucenje/blob/master/logo.png)
