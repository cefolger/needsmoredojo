Needs More Dojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier. 

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage

##### Initial Configuration

In version 0.4 and later, you can set up your project source location and dojo source location to enable certain features. By default, Needs More Dojo assumes that
your project sources are on the same level as the dojo sources.

- If you haven't setup your sources, you will get the following warning each time you load the project:
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/projectwarning.png)
- Open the settings dialog via the File menu or keyboard shortcut
- Navigate to "Needs More Dojo" under project settings which will look like this:
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/needsmoredojosettings.png)

**Dojo Sources**
- The dojo sources directory should be set one level above the dojo sources. So, if you store your dojo sources in a folder called "deps":
    - deps
        - dojo
        - dijit
        - dojox
        - util

Then set your dojo sources folder to "deps"

**Project Sources**
- Your project source directory should be set one level above all of your project packages. For example, if your sources look like this:
   - Source
        - JavaScript
           - package
             - subfolder
             - subfolder

And you reference your packages absolutely as "package/subfolder/module" Then set your source directory to "Source/JavaScript"

*Relative paths vs absolute paths*: When using the "Add new import" feature to import project modules, you will get both
absolute and relative path syntax. If you want the relative path option to appear as the first item, check the "Prefer relative paths"
checkbox.

Finally, you can use the auto-detection features to get suggestions on your source locations. For the dojo sources, it will scan
for "dojo.js." For your project, it will scan for JavaScript files that have dojo modules and give you a list of possible choices.

Hit "apply" to make sure your settings are saved.

##### Functionality

The plugin adds the following options under the Code menu:
- **Organize AMD Imports**: Sorts imports alphabetically, removes duplicates, and normalizes quotes
- **Add new AMD Import**: pops up a dialog. Type in the name of a dojo module. A second dialog will popup giving a list of suggestions.
The module will then be inserted in the define argument list and corresponding function parameter. For example:
    - Press Ctrl+Shift+O, 2
    - Enter 'BorderContainer', press enter
    - 'dijit/layout/BorderContainer' is the first choice presented. Press enter
    - The import will be inserted in both the define's argument list and the function's parameter list

- **Remove unused imports**: Removes imports that have been crossed out

These three items require configuring your dojo source and project source locations. If you do not do this, it will still work as long as your
sources have the same parent as the dojo sources.
- **Go to attach point (appears under Navigate)**. Inside a module that uses _TemplatedMixin, use this option with the caret over an attach point reference.
The attach point will be looked up in the widget's template file (specified by the templateString property) and highlighted. Press Esc to remove the highlighting
- **Navigate -> Declaration** is supported for keys imported via the dojo/i18n! plugin. In the example below, the resources module has been imported via dojo/i18n!
and can be jumped to.

<pre>
    resources['website.maintoolbar.gocontact']
</pre>

The following items are added to the Refactor menu:
- **Convert class module to util module**: Converts a normal dojo class created using declare to a pattern that will
allow only one instance to be created.
- **Convert util module to class module**: The reverse of the first conversion.

A class module looks like the following: 

<pre>
define([..], function(..., {
    return declare(....); 
}); 
</pre>

A util module looks like this: 

<pre>
define([..], function(.., {
  var util = declare(...); 
  
  util.method1 = ...
  
  return util; 
}); 
</pre>

Two inspections are enabled: 
- **Check for unused imports**: Marks unused imports with strikethrough
- **Check for mismatched imports**: Check if naming between an import and its corresponding function parameter are consistent

These inspections also appear in the 'Inspections' menu, under **JavaScript -> Needs More Dojo**

#### License

Licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) license

#### Development

You will need:
- to have checked out the [IntelliJ Community Edition SDK](http://www.jetbrains.org/pages/viewpage.action?pageId=983225)
- have access to the JavaScript plugin in plugins/JavaScript
- [Mockito](https://code.google.com/p/mockito/) for running unit tests

Simply clone the repository from GitHub or fork it for your modifications.