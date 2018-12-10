## JODConverter - Sample - Rest API

This is a sample application of a rest api that uses the spring boot integration module of the Java OpenDocument Converter (JODConverter) project to offer document conversion capabilities. The goal was to emulate a LibreOffice Online server that
would support the customization of custom (known) load/store properties.

Hint: The exposed REST-API interface imitates the LibreOffice-Online REST interface (/lool), you could think of this example as an LibreOffice-Online server.
This being a LibreOffice-Online server, you can talk to the REST api using jodconverter-online (LO online client). (`-c parameter`)

### Update

See [here](https://github.com/EugenMayer/converter) for an improved version, meaning production ready, of this project!

### Running the Project using gradle

First, [build the entire jodconverter project](https://github.com/sbraconnier/jodconverter#building-the-project)

Then, run

```Shell
gradlew :jodconverter-samples:jodconverter-sample-rest:bootRun
```

Once started, use your favorite browser and visit this page:

```
http://localhost:8080/swagger-ui.html
```

Happy conversions!!
