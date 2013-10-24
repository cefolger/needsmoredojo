Needs More Dojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier. It is primarily
for use with dojo versions 1.8.0 and later due to the AMD system.

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage
1. [Issues and Feature Requests] (#issues-and-feature-requests)
2. [Quick Start](#quick-start)
3. [Configuration](#initial-configuration)
4. [Supported File Types](#supported-file-types)
5. [Organize AMD Imports](#organize-amd-imports)
6. [Add AMD Import](#add-amd-import)
7. [Move AMD Import](#move-amd-import)
8. [Remove AMD Import](#remove-amd-import)
9. [Remove Unused Imports](#remove-unused-imports)
10. [Rename Refactoring](#rename-refactoring)
11. [Move File Refactoring](#move-file-refactoring)
12. [Navigate to Attach Point](#navigate-to-attach-point)
13. [Convert between class style and util style module](#convert-between-class-style-and-util-style-module)
14. [Mismatched imports inspection](#mismatched-imports-inspection)
15. [Navigate to Declaration for i18n resource keys](#navigate-to-declaration-for-i18n-resource-keys)
16. [Toggle between Absolute and Relative AMD Imports](#toggle-between-absolute-and-relative-amd-imports)
17. [Find Cyclic Dependencies](#find-cyclic-dependencies)
18. [Navigation to modules and methods](#navigation-to-modules-and-methods)

##### Issues and Feature Requests

Most of the plugin functionality is based on my own needs. If you experience bugs or would like to see something added,
don't hesitate to open up an issue right here on github. You can also send me an email with any questions or comments.

##### Quick Start

Listed below are the keyboard shortcuts for the most common operations Needs More Dojo is used for.

| Action        | Default Shortcut |
| ------------- |:-------------:|
| Add import      | Ctrl+Shift+O, 2 |
| Remove import      | Ctrl+Shift+O, 4      |
| Remove all unused imports | Ctrl+Shift+O, 3      |
| Organize imports | Ctrl+Shift+O, 1 |
| Switch between relative and absolute path syntax | Ctrl+Shift+O, S |
| Move import up/down | Ctrl+Alt+Pg Up/Pg Down |

##### Initial Configuration

Many of the Needs More Dojo features use the location of your project sources to determine valid paths to your modules.
By default, Needs More Dojo assumes that your project sources are on the same level as the dojo sources.

> **Note: You might notice the term "project" sources instead of "module" sources. This is because Needs More Dojo originally
started based off of my own usage, which was one module per project. In version 0.7 I intend to correct this, however
for the moment it is a design limitation.**

- If you haven't set up your sources, you will get the following warning each time you load the project:
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/projectwarning2.png)
- Open the settings dialog via the File menu or keyboard shortcut
- Navigate to "Needs More Dojo" under project settings which will look like this:
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/needsmoredojosettings.png)

> **Note: You can disable Needs More Dojo for a project by using "disable for this project" in the warning or unchecking "Enable Needs More Dojo"
in the Needs More Dojo settings.**

**Dojo Sources**
- The dojo sources directory should be set one level above the dojo sources. So, if you store your dojo sources in a folder called "deps":
    - deps
        - dojo
        - dijit
        - dojox
        - util

Then set your dojo sources folder to "deps"

> **Note: You can use the "Not included or uses the same root as project sources" option. When checked, the plugin will
assume that the dojo sources are either a) not present or b) in the same location as your project sources. If you don't
reference the dojo sources, the add import feature will not work for dojo modules.**

You can use a zip file or jar as your dojo sources. To do this:
- Add the zip/jar as a content root. In IntelliJ, that's under File/Project Structure/Modules. In WebStorm, it's
    under File/Settings/Directories
- Open the Needs More Dojo settings by going to File/Settings/Needs More Dojo
- Specify the directory within the archive containing the dojo sources. If you are using the [WebJars](http://www.webjars.org/) repository for example,
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

##### Supported File Types

Needs More Dojo will work in JavaScript code snippets that are embedded in other files. By default, HTML, JSP, PHP, and js files
are marked as supported. You can add additional file types for your project in the Needs More Dojo Settings. To do this:

- Access the Needs More Dojo settings
- In the text field for "comma delimited list of supported file types," add any additional file types you want Needs More Dojo
to support
- Click "Apply" and "Ok" to persist the settings
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/filetypes.png)

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

By default, the add import dialog is not case sensitive. If you find this is causing a performance problem when trying to
add an import, you can make it case-insensitive (which is much faster) by un-checking "Use case-insensitive searching" in
the "Add new import options" section under the Needs More Dojo settings.

Finally, you can access this option when your cursor is near a module name, either in a new expression or reference. Press
Ctrl+Shift+O, 2 in these cases and the dialog will be pre-populated. In the following examples, the _ represents the cursor,
and both cases will result in "Button" being the initial value:

```javascript
    new Button_({});
```
```javascript
    new Button({
        _
    });
```

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
scan the code for references to your AMD imports and cross out any that are unused. You can also use a quick fix when the caret
is over an unused module. A quick fix will be provided to remove all unused modules or just the module you have selected.

Some AMD modules are not directly referenced. To prevent these from being flagged as unused, use the settings dialog
to add a new exception. After this, the import will never be flagged as unused.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/unusedimportexception.png)

Sometimes, you will want imports in a specific file to be kept even if they are flagged as unused. You can indicate
that an import should not be flagged as unused by using the /*NMD:Ignore*/ block comment next to the
define literal but before the comma. For example:

```javascript
define([
    'dijit/layout/ContentPane' /*NMD:Ignore*/,
    'dijit/layout/BorderContainer'
], function(ContentPane, BorderContainer) {
    /* ... */
});
```

In this example, dijit/layout/ContentPane will not be flagged as unused even if it normally would be. You can also have the
ignore comment inserted automatically by using a quick fix when the caret is over an unused module:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/ignorequickfix.png)

Simply use the "Don't flag as unused" option.

Finally, in IntelliJ IDEA you can also run this as an inspection in batch mode on your entire project or a subset. To do this, use Analyze ->
Run Inspection By Name -> Check for unused imports.

##### Rename Refactoring

> **Note: Needs More Dojo disables its refactoring support by default due to performance concerns on large projects when doing move/rename actions. You
can enable refactoring by checking "Enable refactoring support for move and rename file actions" in the Needs More Dojo settings.**

When you rename a module, Needs More Dojo will scan
for AMD references to it in other project modules and update them. It supports both relative path and absolute package
syntax.

To perform a rename:
- Select Refactor -> Rename by right clicking the module in question
- Uncheck all of the options. This is important because IntelliJ will try to update your AMD references incorrectly
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/refactorrename1.png)
- Click "Refactor" to complete the rename.

> **Note: At this time, refactor previews are not available**

> **Note: Please make sure you have setup your project sources location so that the AMD update works correctly**

##### Move File Refactoring

> **Note: Needs More Dojo disables its refactoring support by default due to performance concerns on large projects when doing move/rename actions. You
can enable refactoring by checking "Enable refactoring support for move and rename file actions" in the Needs More Dojo settings.**

Needs More Dojo will update module references when you move an AMD module.

To perform a move:
- Drag the file in the project tree to its new location
- Uncheck all of the options. This is important becuase IntelliJ will try to change the paths to dojo modules among other
things
![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/refactormove1.png)
- Click 'OK'

> **Note: Please make sure you have setup your project sources location so that the AMD update works correctly**

> **Note: At this time, refactoring of the dojo library sources is not supported.**

##### Navigate to Attach Point

Inside a module that uses _TemplatedMixin, use this option with the caret over an attach point reference. The default
key binding is Ctrl+Shift+O, A.

The attach point will be looked up in the widget's template file (specified by the templateString property) and highlighted.
Press Esc to remove the highlighting.

You can also use Ctrl+Click over an attach point reference. However, note that the attach point references are flagged
as unresolved variables even though they can be looked up.

> **Note: If you don't set the location of your project sources, this feature will search for them in the directory
containing all of the dojo sources**

##### Convert between class style and util style module

These options appear under the refactor menu. Use them to transform a module between a util style (only one instance)
and class style (many instances, directly instantiated) module.

A class module looks like the following:

```javascript
define([...], function(...), {
    return declare(...);
});
```

A util module looks like this:

```javascript
define([...], function(...), {
  var util = declare(...);

  util.method1 = ...

  return util;
});
```

##### Mismatched imports inspection

This inspection runs in the background and will check if naming is consistent between an AMD module path and its
corresponding parameter name.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/mismatchedimports1.png)

It will also provide a quick fix. The fix will rename the parameter (and update all references to it) to match the
define literal. If the fix is appropriate, just activate it and the mismatch will be corrected and unflagged. If you have
two mismatched imports in a row, you will also get a quick fix to swap the two.

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/quickfix2.png)

You can disable the mismatched imports inspection by going in the inspections menu under **JavaScript -> Needs More Dojo** and unchecking it.

Sometimes the Needs More Dojo conventions may inappropriately flag an import as mismatched. For these cases, you can
add a naming exception via the Needs More Dojo settings:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/modulenamingexception.png)

In IntelliJ IDEA, you can also run this inspection in batch mode on your entire project or a subset. To do this, use Analyze ->
Run Inspection By Name -> Check for inconsistently named imports.

##### Navigate to Declaration for i18n resource keys

For keys imported via the dojo/i18n! plugin, Navigate ... Declaration is supported. In the example below, the resources
module has been imported via dojo/i18n! and can be jumped to:

```javascript
    resources['website.maintoolbar.gocontact']
```

##### Toggle between Absolute and Relative AMD Imports

You can toggle between using a relative path and an absolute (package) path for your AMD Imports. This option
appears under the Code menu as "Toggle AMD Import Path Syntax" or via the hotkey combination Ctrl+Shift+O, S.

For example, if you have an absolute reference:
```javascript
define([
    'website/ProjectDisplay/ProjectDisplay',
    'dojo/_base/declare'
], function(ProjectDisplay, declare) {
    /* ... */
});
```
It will be converted to a relative path syntax and vice-versa:
```javascript
define([
    './ProjectDisplay',
    'dojo/_base/declare'
], function(ProjectDisplay, declare) {
    /* ... */
});
```

##### Find Cyclic Dependencies

New starting with 0.5.1 is the ability to find potential cyclic dependencies. A cyclic dependency can result in subtle
bugs because one of the dependencies gets resolved to {}. In Needs More Dojo, there are two ways to detect cyclic
dependencies. As this is a new experimental feature, please open any issues you find on the github issue page.

###### Enable the cyclic dependencies inspection.

This inspection is disabled by default, but can be enabled via the inspections menu:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/cyclicinspection.png)

When enabled, the inspection will run in the background and scan your dependency graph for cycles involving the current
module. If it finds one, it will flag it and list the path:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/cyclicinspectionwarning.png)

In IntelliJ IDEA, you can also run the inspection in batch mode to scan for cycles in your project. To do this, use
Analyze -> Run Inspection by Name -> Check for cyclic dependencies in AMD modules.

###### Run the "Find Cyclic AMD Dependencies" action.

This action differs from the inspection because it displays a sorted list of modules involved in the cyclic dependency.
You can access this action via the Code menu or with Ctrl+Shift+O, C. This action will check the entire project and
put the results in a tool window at the bottom:

![ScreenShot](https://raw.github.com/cefolger/needsmoredojo/dev/screenshots/docs/cyclicaction.png)

If you have many modules that have a cycle in the dependency graph, this output might be helpful when trying to isolate
the culprit module.

##### Navigation to modules and methods

Needs More Dojo complements the IDE's Navigate ... Declaration feature. You might notice that out of the box, there are
several places where this does not work for AMD modules and methods in them. This is not the IDE's fault, it simply requires
knowledge of Dojo's AMD system and object model in order to resolve the references correctly.

Following are some examples of where the out of the box functionality is incorrect and that Needs More Dojo fixes.

###### AMD module references
```javascript
define([
    'dijit/layout/ContentPane'
], function(ContentPane) {
    var x = new ContentPane({}); // Ctrl+Click on ContentPane
});
```

The IDE will jump to the "ContentPane" parameter instead of going to the ContentPane.js module.
###### Method references off of AMD modules
```javascript
    define([
        'dojo/dom-style'
    ], function(domStyle) {
        test: function() {
            domStyle.set(node, 'display', 'none'); // Ctrl+Click on set
        }
    });
```

The IDE will present a list of set methods in various other modules instead of jumping to set in dom-style.js
###### this.inherited references
```javascript
    define([
        'dojo/_base/declare',
        'dijit/_WidgetBase'
    ], function(declare, WidgetBase) {
        return declare([WidgetBase], {
            startup: function() {
                this.inherited(arguments); // Ctrl+Click on inherited
            }
        });
    });
```

The IDE will present a list of startup methods in various other modules instead of jumping to the one in _WidgetBase
###### references to "super" methods:
```javascript
    define([
        'dojo/_base/declare',
        'dijit/_WidgetBase'
    ], function(declare, WidgetBase) {
        return declare([WidgetBase], {
            testMethod: function() {
                this.startup(); // Ctrl+Click on startup
            }
        });
    });
```

The IDE will present a list of unrelated startup methods in other modules instead of jumping to the one in _WidgetBase

#### License

Licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) license

#### Development

You will need:
- to have checked out the [IntelliJ Community Edition SDK](http://www.jetbrains.org/pages/viewpage.action?pageId=983225)
- have access to the JavaScript plugin in plugins/JavaScript
- [Mockito](https://code.google.com/p/mockito/) for running unit tests

Simply clone the repository from GitHub or fork it for your modifications.