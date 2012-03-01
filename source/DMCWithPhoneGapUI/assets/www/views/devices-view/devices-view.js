document.addEventListener('deviceready', function() {
	window.plugins.DevicesPlugin.start();
}, true);
var dms_listview;

var add_dms = function(element) {
	console.log(element);
	var dms_item = document.createElement('li');
	dms_item.className = "li_device_item";
	dms_item.innerHTML = element;
	dms_listview.appendChild(dms_item);
};

$(document).ready(function() {
	dms_listview = document.getElementById('list_of_dms');
	$(dms_listview).listview("refresh");
});