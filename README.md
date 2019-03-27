# CnfUtils

![Build Status](https://jenkins-2.sse.uni-hildesheim.de/buildStatus/icon?job=KH_CnfUtils)

A utility plugin for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

Utilities for Boolean formulas, including CNF converters and a SAT solvers (based on [Sat4j v2.3.4](https://www.sat4j.org/) and [CryptoMiniSat 5.6.5](https://github.com/msoos/cryptominisat)).

## Usage

Place [`CnfUtils.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_CnfUtils/lastSuccessfulBuild/artifact/build/jar/CnfUtils.jar) in the plugins folder of KernelHaven.

## Dependencies

This plugin has no additional dependencies other than KernelHaven.

## License

This plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

## Used Libraries

The following libraries are used (and bundled in `lib/` or `res/`) by this plugin:

| Library | Version | License |
|---------|---------|---------|
| [Sat4j](https://www.sat4j.org/) | [2.3.5.v20130525](http://download.forge.ow2.org/sat4j/sat4j-core-v20130525.zip) | [EPL](https://www.eclipse.org/legal/epl-v10.html) or [LGPL](https://www.gnu.org/licenses/lgpl.html) |
| [CryptoMiniSat](https://github.com/msoos/cryptominisat) | [5.6.5](https://github.com/msoos/cryptominisat/releases/tag/5.6.5) | [MIT License](https://opensource.org/licenses/MIT) |
| [jbool_expressions](https://github.com/bpodgursky/jbool_expressions) | [1.21](https://mvnrepository.com/artifact/com.bpodgursky/jbool_expressions/1.21) | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) |
| [ANTLR v3 Runtime](https://www.antlr3.org/download.html) | [3.5.2](https://mvnrepository.com/artifact/org.antlr/antlr-runtime/3.5.2) | [BSD License](https://www.antlr.org/license.html) |
