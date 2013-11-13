JForm
=====

Java library for retrieving GUI form information.

#How it works:

- Register form elements with the API using translators
    - Translators for many common swing components are already provided
- Import or export data through the translators
    - Imports/Exports can be done from/to file or memory
    - Maps or property sets can be used to store the data
    - Additional meta-data that does not appear in the GUI can be processed

#Usage:

Read through and run the demo in com.madphysicist.jform.demo package. The code is pretty short and concise.

The Javadocs for the packages are not complete yet, but the main classes are pretty well documented.

#Note:

This project depends on the Mad Physicist Utilities project. The jar file from this project must be on the classpath when building the JForm project.
