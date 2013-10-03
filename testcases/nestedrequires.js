define([
    'dijit/layout/BorderContainer', // should be flagged as used
    'dijit/layout/TabContainer' // should be flagged as unused
], function(BorderContainer, TabContainer) {
    return declare([], {
        _setupEvents: function() {
            var that = this;
            var x = new BorderContainer({});

            require([
                'dijit/layout/TabContainer', // should be flagged as used
                'dojo/ready', // should be flagged as unused
                'dojo/parser' // should be flagged as unused
            ], function(TabContainer, ready, parser) {
                require([
                    'dijit/layout/BorderContainer', // should be flagged as unused
                    'dojo/ready', // should be flagged as used
                    'dojo/parser' // should be flagged as unused
                ], function(BorderContainer, ready, parser) {
                    ready(function() {
                        var x = new TabContainer({});
                        var y;
                    });
                })
            })

            require([
                'dijit/layout/TabContainer', // should be flagged as used
                'dojo/ready', // should be flagged as unused
                'dojo/parser' // should be flagged as unused
            ], function(TabContainer, ready, parser) {
                var x = new TabContainer({});

            })
        }
    });
});