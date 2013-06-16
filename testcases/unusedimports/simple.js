/**
 * only b should be highlighted
 * bug: symptom of multiple defines
 */
define([
    'a',
    'b'
], function(a, b) {
    var used = new a();
});
