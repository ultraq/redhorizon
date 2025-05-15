
Red Horizon Runtime
===================

Runtime package for building applications with the Red Horizon engine.


Installation
------------

Requires Java 21 on macOS 12 Monterey and newer, or Windows 10 64-bit and newer.

### For Maven and Maven-compatible dependency managers

Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.redhorizon`
 - ArtifactId: `redhorizon-runtime`

Check the [project releases](https://github.com/ultraq/redhorizon/releases)
for a list of available versions.  Each release page also includes a
downloadable JAR if you want to manually add it to your project classpath.


Usage
-----

In a nutshell, a game/application can be built and run by creating an instance
of `nz.net.ultraq.redhorizon.runtime.Runtime`, passing an instance of
`nz.net.ultraq.redhorizon.runtime.Application` to the constructor, configuring
the runtime with the methods available on the runtime instance, and then calling
`execute()`.

For an extremely basic game example, this can all be contained within a single
class:

```groovy
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime

class MyGame implements Application {

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
