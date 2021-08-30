Static analyzer for [EO programming language](https://github.com/cqfn/eo).

[![Scala CI](https://github.com/Sitiritis/eo-static-analyzer/actions/workflows/scala.yml/badge.svg)](https://github.com/Sitiritis/eo-static-analyzer/actions/workflows/scala.yml)
# ⚠️🚧 Work in progress 🚧⚠️ 

> The project is still in active development stage and might not be usable or fully documented yet.

# Running

To run the project you will need [sbt](https://www.scala-sbt.org/1.x/docs/Setup.html) and JDK 8+.

## Sandbox

As of now there is no app to run and interact with, but one can play with the source code of the analyzer in the sandbox project.

The entrypoint is in `sandbox/src/main/scala/eo/sandbox/Sandbox.scala`.

The sandbox can be run via:

```shell
sbt sandbox/run
```

> Currently, the sandbox contains a program, that represents AST of mutually recursive EO program, transforms the AST to EO and prints it.

# Project structure

## `core` project

Defines EO AST. All other projects should depend on this one.

## `backends` project

Defines backends for the static analyzer. A backend is something that produces an output from AST.

### `eolang` subproject

Backend that is able to generate EO program (now, roughly, represented as a list of strings) from EO AST.

## `utils` project

Convenient tools that are used by other parts of the analyzer. This project must not depend on any other project.

## `sandbox` project

Enables to interactively run and test source code of the analyzer. Facilitates the development and debug of the source code and enables to see intermediate results of the development. Any other project must not depend on it. 

For more details on the project organization see:

- `build.sbt` - main build configuration file
- `project/Dependendencies.scala` - lists dependencies and versions for the projects
- `project/Compiler.scala` - lists compiler flags used to compile the project and compiler plugins
