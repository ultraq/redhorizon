
Red Horizon Runtime
===================

Runtime package for building applications with the Red Horizon engine.


Installation
------------

Requires Java 25 on macOS Monterey (12) and newer, or Windows 10 64-bit and newer.

### For Maven and Maven-compatible dependency managers

Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.redhorizon`
 - ArtifactId: `redhorizon-runtime`
 - Version: `0.43.0`

Check the [project releases](https://github.com/ultraq/redhorizon/releases)
for a list of available versions.


Usage
-----

A game can be scaffolded by extending the `nz.net.ultraq.redhorizon.runtime.Game`
class (which contains many optional methods for configuring all of the systems).
Then pass an instance of that class to the `nz.net.ultraq.redhorizon.runtime.Runtime`
constructor, and call `execute()`.

For an extremely basic example, this can all be contained within a single class:

```groovy
import nz.net.ultraq.redhorizon.runtime.Game
import nz.net.ultraq.redhorizon.runtime.Runtime

class MyGame extends Game {

  static void main(String[] args) {
    System.exit(new Runtime(new MyGame()).execute(args))
  }

  final String name = 'My cool game'
  final String version = '0.1.0'

  @Override
  void start(Scene scene) {
    // Code to set up and start your game here
  }

  @Override
  void stop(Scene scene) {
    // Code to clean up and close your game here
  }
}
```


API
---

Browse the online groovydocs for the full API:
https://javadoc.io/doc/nz.net.ultraq.redhorizon/redhorizon-runtime
