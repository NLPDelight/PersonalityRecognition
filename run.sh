#! /bin/bash

execute="io.personalityrecognition.PersonalityRecognition"
classpath="target/PersonalityRecognition-0.0.1-SNAPSHOT.jar:lib/*"

./mvnw package
java -cp $classpath $execute