document.addEventListener('deviceready', function() {
	window.plugins.DevicesPlugin.start();
}, true);

var dms_listview;
var dmr_listview;
var myScroll;

var add_device = function(element, type) {
	console.log(element);
	// var device_item = document.createElement('li');
	// device_item.className = "li_device_item";
	// device_item.innerHTML = element;
	// if (type == 'dms')
	// dms_listview.appendChild(device_item);
	// else
	// dmr_listview.appendChild(device_item);

	// StringBuilder result = new StringBuilder();
	// result.append("\"");
	// result.append("<div class='device_list_item' type='" + type + "' udn='" +
	// udn + "' onclick='onDeviceClick(this);'>");
	// result.append("<div align='center' class='device_icon'>");
	// result.append("<img class='img_device_icon' src='" + deviceImage +
	// "'/>");
	// result.append("</div>");
	// result.append("<div class='device_info'>");
	// result.append("<div class='div_device_name'>" + deviceName + "</div>");
	// result.append("<div class='div_device_address'>" + deviceAddress +
	// "</div>");
	// result.append("</div>");
	// result.append("</div>\"");
	var device = eval(element);
	var html = "<div class='device_list_item' type='" + device.type + "' udn='"
			+ device.udn + "' onclick='onDeviceClick(this);'>";
	html += "<div align='center' class='device_icon'>";
	html += "<img class='img_device_icon' src='" + device.icon + "'/></div>";
	html += "<div class='device_info'><div class='div_device_name'>";
	html += device.name + "</div>";
	html += "<div class='div_device_address'>" + device.address
			+ "</div></div></div>";
	var device_item = document.createElement('li');
	device_item.className = "li_device_item";
	device_item.innerHTML = html;
	if (type == 'dms')
		dms_listview.append(device_item);
	else
		dmr_listview.append(device_item);
	myScroll.refresh();
};

var remove_device = function(udn) {
	$("div[udn='" + udn + "']").remove();
};

$(document).ready(function() {
	dms_listview = $('#list_of_dms');// document.getElementById('list_of_dms');
	dmr_listview = $('#list_of_dmr');// document.getElementById('list_of_dmr');
	setTimeout(function() {
		myScroll = new iScroll('wrapper');
	}, 200);
});

var onDeviceClick = function(e) {
	var type = e.getAttribute('type');
	var udn = e.getAttribute('udn');
	console.log('deviceType = ' + type.toString() + '; UDN = '
			+ type.toString());

	if (type == 'dms') {
		choseDMS(udn);
	} else if (type == 'dmr') {
		choseDMR(udn);
	}
};

function choseDMS(udn) {
	window.plugins.DevicesPlugin.setDMS(udn);
}

function choseDMR(udn) {
	window.plugins.DevicesPlugin.setDMR(udn);
}