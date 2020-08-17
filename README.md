# pack.png WorldQuery Plugin

This is a [PaperMC](https://papermc.io/) plugin made for the [pack.png project](https://packpng.com/).

The purpose of this plugin is to continuously dump block data within a target world area to a CSV file, for usage in external tools.

## Configuration

See [config.yml](src/main/resources/config.yml).

## Building

Requires [sbt](https://www.scala-sbt.org/).

Just run `sbt assembly` from the project root. This will write a fat `.jar` to `target/scala-2.13/`.

## Known Deficiencies

* Non-exhaustive block monitoring
    * The plugin watches for certain block change events instead of continuously scanning the full area, but this
    currently does not capture all possible events.
    * For example, if a change is made to the target area via [WorldEdit](https://github.com/EngineHub/WorldEdit), the
    plugin will need to be reloaded to see the change.
