/**
 * test cases for detecting dijit naming inconsistencies
 */
define([
    'dijit/f',
    'dijit/form/Form',
    'dijit/TitlePane',
    'dijit/form/TextBox',
    'dijit/layout/ContentPane',
    'dijit/Foo',
    'bar',
    'dojox/widget/dong'
], function(f, Form, titlePane, textBox, ContentPane, Foo, Bar, Dong) {
    /**
     * test stuff from dojox/widget
     *
     */
    var foo = {
        bar: function() {
            var testing;
            testing  = new Dong({});
        }
    };

    /**
     * this should not highlight "another" just "nested"
     */
    var another = function() {
        var i = 3;

        var nested = new Form();
    };

    /**
     * test elements that appear nested in a variable
     * @type {*}
     */
    var CommentManager = declare(null, {
        constructor: function() {
            /**
             * this should not generate a false positive
             */
            this._commentData = [];
        },
        createForm: function(replies, data, d_target, isNewThread) {
            var d_textBox = new textBox({ name: "text", value: "", placeHolder: I18nUtil.get('commentmanager_comment_placeholder') }, d_textBoxDom);
            var d_subjectBox = new textBox({ name: "subject", value: "", placeHolder: I18nUtil.get('commentmanager_subject_placeholder') }, d_subjectBoxDom);
            var form = new Form({ encType: 'multipart/form-data', method: 'POST' }, d_formDom);
            var textBox = new textBox();
        }

    });

    var helloThereTextBox = new textBox();
    var d = new f();

    /**
     * test simple variable declarations
     * @type {dijit.form.Form}
     */
    var form = new Form({ encType: 'multipart/form-data', method: 'POST' }, d_formDom);
    /**
     * test assignment
     * @type {dijit.layout.ContentPane}
     */
    element = new ContentPane({
        // this ID is required for extracting form parameters
        // note that a table is used here because other elements use tables
        // TODO encapsulate a group into a component
        id: item.id,
        content: '<table class="entry-label"><tr><td><label>%0</label></td></tr></table>'.tokenize(item.label),
        "class": "entry"
    });
    /**
     * test definition expression with params inside constructor
     * @type {dijit.layout.ContentPane}
     */
    this.pane2 = new ContentPane({
        title: item.label,
        id: 'dynamic_' + item.id
    });
    /**
     * test definition expression
     * @type {dijit.layout.ContentPane}
     */
    this.pane = new ContentPane({});
    this.foo = new Foo();
    this.text = new Foo().bar();
    this.text = new Foo();
    this.dong = new Dong();
    /**
     * test nested return statement inside variable declaration
     * @param comment
     * @param first
     * @return {dijit.TitlePane}
     */
    var createTitlePane = function(comment, first) {
        var content = "<div class='comment'>" + populateComment(comment, true) + "</div>"
        return new titlePane({
            title: comment.subject,
            content: content,
            open: first
        });
    };

    /**
     * false test cases. Should not generate false positives
     */
    this.id = test.id;
    this.test = require('foo');
    this.test = require('foo') + 3;
    this.text = new Bar();
    this.text = Bar.foo();
    this.text = new foo.Bar();
    this.john = new Baz();
    var d_nested = new Form();

});

