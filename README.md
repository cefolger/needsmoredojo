Needs More Dojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier. 

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage
1. [Configuration](#initial-configuration)
2. [Organize AMD Imports](#organize-amd-imports)
3. [Add AMD Import](#add-amd-import)
4. [Move AMD Import](#move-amd-import)
5. [Remove Unused Imports](#remove-unused-imports)
6. [Navigate to Attach Point](#navigate-...-attach-point)
7. [Convert between class style and util style module](#convert-between-class-style-and-util-style-module)
8. [Mismatched imports inspection](#mismatched-imports-inspection)
9. [Navigate to Declaration for i18n resource keys](#navigate-to-declaration-for-i18n-resource-keys)


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

You can use the auto-detection features to get suggestions on your source locations. For the dojo sources, it will scan
for "dojo.js." For your project, it will scan for JavaScript files that have dojo modules and give you a list of possible choices.

Hit "apply" to make sure your settings are saved.

##### Organize AMD Imports

This option appears under the Code menu. It sorts imports alphabetically on the module path. It also removes any duplicates
and normalizes quotes.

##### Add AMD Import

Access this option with Ctrl+Shift+O, 2. A dialog will popup where you can type the name of the dojo module. Type the
name of the module you would like to import and press enter. A dialog will then appear listing possibilities.
Select one and press enter, the module will be inserted for you.

> Note: If you don't set the location of your project sources, this feature will search for them in the directory
containing all of the dojo sources

By default, this feature prioritizes absolute path syntax for module paths. You can change this in the settings dialog
by checking "Prefer relative paths"

You can also import an AMD plugin by using <module>!<resource id>. For example, to import an i18n resource, type
i18!<path to resource file>

Finally, you can access this option when your cursor is near a module name, either in a new expression or reference. Press
Ctrl+Shift+O, 2 in these cases and the dialog will be pre-populated. In the following examples, the _ represents the cursor,
and both cases will result in "Button" being the initial value:

<pre>
    new Button_({});
</pre>
<pre>
    new Button({
        _
    });
</pre>

##### Move AMD Import

Use this feature by placing the cursor near a module's path in your define statement. Use Ctrl+Alt+Page Up/Down to
move the import and its corresponding parameter. This will not affect plugins at the end that do not have a corresponding
parameter (such as domReady!)

##### Remove Unused Imports

This feature can be activated with the shortcut Ctrl+Shift+O, 3. It also runs in the background as an inspection. It will
scan the code for references to your AMD imports and cross out any that are unused.

Some AMD modules are not directly referenced. To prevent these from being flagged as unused, use the settings dialog
to add a new exception. After this, the import will never be flagged as unused.

##### Navigate to Attach Point

Inside a module that uses _TemplatedMixin, use this option with the caret over an attach point reference.
The attach point will be looked up in the widget's template file (specified by the templateString property) and highlighted.
Press Esc to remove the highlighting

> Note: If you don't set the location of your project sources, this feature will search for them in the directory
containing all of the dojo sources

##### Convert between class style and util style module

These options appear under the refactor menu. Use them to transform a module between a util style (only one instance)
and class style (many instances, directly instantiated) module.

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

##### Mismatched imports inspection

This inspection runs in the background and will check if naming is consistent between an AMD module path and its
corresponding parameter name.

You can disable it by going in the inspections menu under **JavaScript -> Needs More Dojo** and unchecking it.

##### Navigate to Declaration for i18n resource keys

For keys imported via the dojo/i18n! plugin, Navigate ... Declaration is supported. In the example below, the resources
module has been imported via dojo/i18n! and can be jumped to:

<pre>
    resources['website.maintoolbar.gocontact']
</pre>

#### License

Licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) license

#### Development

You will need:
- to have checked out the [IntelliJ Community Edition SDK](http://www.jetbrains.org/pages/viewpage.action?pageId=983225)
- have access to the JavaScript plugin in plugins/JavaScript
- [Mockito](https://code.google.com/p/mockito/) for running unit tests

Simply clone the repository from GitHub or fork it for your modifications.