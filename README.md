needsmoredojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier. 

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage

The plugin will do four things:
- Allow removal of these imports
- Sort imports alphabetically and normalize quotes (no more mixed single/double quotes)
- Strikethrough unused AMD imports at the top of a dojo define(...) call in a module
- Highlight inconsistent naming in an imported module and its associated function argument

The first two items can be accessed by right clicking in the editor and going to the refactor submenu. 

The last two items run in the background

#### Development

You will need access to the JavaScript plugin for IntelliJ, or have WebStorm. 

Running unit tests requires [Mockito](https://code.google.com/p/mockito/)
