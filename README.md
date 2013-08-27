Needs More Dojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier.

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage
1. [Issues and Feature Requests] (#issues-and-feature-requests)
2. [Configuration](#initial-configuration)
3. [Organize AMD Imports](#organize-amd-imports)
4. [Add AMD Import](#add-amd-import)
5. [Move AMD Import](#move-amd-import)
6. [Remove AMD Import](#remove-amd-import)
7. [Remove Unused Imports](#remove-unused-imports)
8. [Rename Refactoring](#rename-refactoring)
9. [Move File Refactoring](#move-file-refactoring)
10. [Navigate to Attach Point](#navigate-to-attach-point)
11. [Convert between class style and util style module](#convert-between-class-style-and-util-style-module)
12. [Mismatched imports inspection](#mismatched-imports-inspection)
13. [Navigate to Declaration for i18n resource keys](#navigate-to-declaration-for-i18n-resource-keys)

##### Issues and Feature Requests

Most of the plugin functionality is based on my own needs. If you experience bugs or would like to see something added,
don't hesitate to open up an issue right here on github. You can also send me an email with any questions or comments.

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

> **Note: You can now use the "Not included or uses the same root as project sources" option. When checked, the plugin will
assume that the dojo sources are either a) not present or b) in the same location as your project sources. If you don't
reference the dojo sources, the add import feature will not work for dojo modules.**

You can use a zip file or jar as your dojo sources. To do this:
- Add the zip/jar as a content root. In IntelliJ, that's under File/Project Structure/Modules. In WebStorm, it's
    under File/Settings/Directories
- Open the Needs More Dojo settings by going to File/Settings/Needs More Dojo
- Specify the directory within the archive containing the dojo sources. If you are using the WebJars repository for example,
    that location looks like this:
    ![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/zipfilesources.png)
- You can also use the auto-detect feature.
- Hit "Apply" or "Ok" to save the changes.

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

Access this option with Ctrl+Shift+O, 2.
- A dialog will popup where you can type the name of the dojo module.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/addimport1.png)
- Type the name of the module you would like to import and press enter.
- A dialog will then appear listing possibilities.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/addimport2.png)
- Select one and press enter, the module will be inserted for you.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/addimport4.png)

> **Note: If you don't set the location of your project sources, this feature will search for them in the directory
containing all of the dojo sources**

By default, this feature prioritizes absolute path syntax for module paths. You can change this in the settings dialog
by checking "Prefer relative paths"

You can also import an AMD plugin by using <module>!<resource id>. For example, to import an i18n resource, type
i18!path/to/resource/file:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/addimport3.png)

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

> **Note: Needs More Dojo will import "dom-attr" "dom-class" etc. even if your cursor is over domAttr or domClass references**

##### Move AMD Import

Use this feature by placing the cursor near a module's path in your define statement. Use Ctrl+Alt+Page Up/Down to
move the import and its corresponding parameter. This will not affect plugins at the end that do not have a corresponding
parameter (such as domReady!)

##### Remove AMD Import

You can remove a single import from your list by using Ctrl+Shift+O, 4. Place the caret near or inside the define literal
or the corresponding function parameter, like this:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/removeimport.png)

Then press Ctrl+Shift+O, 4. Both locations will be removed, if possible.

##### Remove Unused Imports

This feature can be activated with the shortcut Ctrl+Shift+O, 3. It also runs in the background as an inspection. It will
scan the code for references to your AMD imports and cross out any that are unused.

Some AMD modules are not directly referenced. To prevent these from being flagged as unused, use the settings dialog
to add a new exception. After this, the import will never be flagged as unused.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/unusedimportexception.png)

##### Rename Refactoring

In versions 0.5 and later, renaming a module is now supported. When you rename a module, Needs More Dojo will scan
for AMD references to it in other project modules and update them. It supports both relative path and absolute package
syntax.

To perform a rename:
- Select Refactor -> Rename by right clicking the module in question
- Uncheck all of the options. This is important because IntelliJ will try to update your AMD references incorrectly
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/refactorrename1.png)
- Click "Refactor" to complete the rename.

> **Note: At this time, refactor previews are not available**

> **Note: Please make sure you have setup your project sources location so that the AMD update works correctly**

> **Note: At this time, refactoring of the dojo library sources is not supported.**

##### Move File Refactoring

In versions 0.5 and later, you can now move your dojo AMD modules.

To perform a move:
- Drag the file in the project tree to its new location
- Uncheck all of the options. This is important becuase IntelliJ will try to change the paths to dojo modules among other
things
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/refactormove1.png)
- Click 'OK'

> **Note: Please make sure you have setup your project sources location so that the AMD update works correctly**

> **Note: At this time, refactoring of the dojo library sources is not supported.**

##### Navigate to Attach Point

Inside a module that uses _TemplatedMixin, use this option with the caret over an attach point reference.
The attach point will be looked up in the widget's template file (specified by the templateString property) and highlighted.
Press Esc to remove the highlighting

> **Note: If you don't set the location of your project sources, this feature will search for them in the directory
containing all of the dojo sources**

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

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/mismatchedimports1.png)

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