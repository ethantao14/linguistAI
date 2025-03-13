# EarthLinguist

## Summary

EarthLinguist is a JavaFX application that allows the user to record audio clips in their native language to describe
images in a table satisfying all checkmarks for a column in the table. The idea is to implement the famous Gavagai
experiment in an interactive way. Since no linguistic requirement is given for how the user should describe the images,
the application is suitable for any language.

The application can be used by language learners to see how native
speakers of a language describe images in their language or to see how the same scenarios are described across
languages. The application can also be used by linguists to collect data on how speakers of a language describe images
in their language; the audio clips can be used to study the phonetics and phonology of the language.

## Installation

The application is written in Java and uses JavaFX for the GUI. The application can be run on any platform that supports
Java, although it needs to be compiled for each platform. The application can be run from the command line using the
following command:

```bash
java -jar EarthLinguist.jar
```

The name of the jar may vary if it is, for example, versioned.

## Usage

The application has a simple GUI. The user can select a language from a list of languages and give some background
information about the country they grew up in. There is a 'Record' tab that allows the user to record audio clips. These
clips can be saved to a file or posted to a server, though currently the server is not implemented, and 'posting' just
saves the file to the local file system. The user can also listen to the audio clips that have been posted and download
them to their local file system. The downloaded audio clips can be played using any media player that supports the file
format and can be analyzed by linguists.

## Contributions

This application is currently not open to community pull requests; if there are suggestions, please submit them through
the GitHub issues list for the project.