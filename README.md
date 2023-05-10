# Creating a new project
To create a new project use the below command

```
mvn archetype:generate -DgroupId=staticTypechecker -DartifactId=staticTypechecker -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

**NOTE** this project was built using maven version 3.6.3


# Packaging
To package the project use the following command in the root folder:

```
mvn clean compile assembly:single
```