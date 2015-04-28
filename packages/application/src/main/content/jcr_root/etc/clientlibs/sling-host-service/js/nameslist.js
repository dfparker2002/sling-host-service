jQuery(function() {
    var namesList = jQuery("ul#names");
    jQuery.getJSON("/bin/example/names", function(data) {

        jQuery.each(data.names, function(key, val) {
            namesList.append("<li>" + val + "</li>");
        });
    });
});