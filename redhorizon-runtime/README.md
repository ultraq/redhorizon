
Red Horizon Runtime
===================

[![Maven Central](https://img.shields.io/maven-central/v/nz.net.ultraq.redhorizon/redhorizon-runtime)](https://central.sonatype.com/artifact/nz.net.ultraq.redhorizon/redhorizon-runtime)

Runtime package for building games with the Red Horizon engine.


Installation
------------

Requires Java 25 on either macOS 12 Monterey and newer or Windows 10 64-bit and
newer.

Add a dependency to your project with the following co-ordinates:

- GroupId: `nz.net.ultraq.redhorizon`
- ArtifactId: `redhorizon-runtime`
- Version: `0.43.0`

Check the [project tags](https://github.com/ultraq/redhorizon/tags) for a list
of available versions.


Usage
-----

A game can be scaffolded by extending the `nz.net.ultraq.redhorizon.runtime.Application`
class (which contains many optional methods for configuring all of the systems).
Then pass an instance of that class to the `nz.net.ultraq.redhorizon.runtime.Runtime`
constructor, and call `execute()`.

For an extremely basic example, this can all be contained within a single class:

```groovy
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime

class MyGame extends Game {

  static void main(String[] args) {
    System.exit(new Runtime(new MyGame()).execute(args))
  }

	MyGame() {
		super('My cool game', '0.1.0')
	}
}
```


API
---

Browse the online groovydocs for the full API:
https://javadoc.io/doc/nz.net.ultraq.redhorizon/redhorizon-runtime
