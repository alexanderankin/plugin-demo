# plugin demo

(janky) gradle plugin development environment

[sdkman]: https://sdkman.io

## usage

first build the parent project, then run gradle in the `plugin-consumer`:

```shell
./gradlew && (cd plugin-consumer; ./gradlew)
```

the `plugin-consumer` needs the plugin to be actually published
before it starts the gradle "configuration" stage, so
these projects are not connected using traditional gradle means.

However, it is still useful to have both sides (client + library)
in one place to demonstrate the plugin completely.
