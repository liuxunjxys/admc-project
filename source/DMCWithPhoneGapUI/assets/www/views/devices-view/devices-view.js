document.addEventListener('deviceready', function() {
	window.plugins.DevicesPlugin.start();
}, true);

var dms_listview;
var dmr_listview;

var add_device = function(element, type) {
	console.log(element);
	var device_item = document.createElement('li');
	device_item.className = "li_device_item";
	device_item.innerHTML = element;
	if (type == 'dms')
		dms_listview.appendChild(device_item);
	else
		dmr_listview.appendChild(device_item);
};

$(document).ready(function() {
	dms_listview = document.getElementById('list_of_dms');
	dmr_listview = document.getElementById('list_of_dmr');
});

function onDeviceClick(e) {
	var type = e.getAttribute('type');
	var udn = e.getAttribute('udn');
	console.log('deviceType = ' + type.toString() + '; UDN = '
			+ type.toString());

	if (type == 'dms') {
		choseDMS(udn);
	} else if (type == 'dmr') {
		choseDMR(udn);
	}
}

function choseDMS(udn) {
	window.plugins.DevicesPlugin.setDMS(udn);
}

function choseDMR(udn) {
	window.plugins.DevicesPlugin.setDMR(udn);
}