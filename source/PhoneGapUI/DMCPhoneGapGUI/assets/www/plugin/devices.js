var DevicesPlugin = function() {
};

DevicesPlugin.prototype.setDMS = function(udn) {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'setDMS', [ udn ]);
};

DevicesPlugin.prototype.setDMR = function(udn) {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'setDMR', [ udn ]);
};

PhoneGap.addConstructor(function() {
	console.log("Phonegap add constructor");
	PhoneGap.addPlugin("DevicesPlugin", new DevicesPlugin());
});

var dms_listview;
var dmr_listview;

var add_device = function(element, type) {
	console.log("Element= " + element);
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
	// var device = eval(element);
	// var html = "<div class='device_list_item' type='" + device.type + "'
	// udn='"
	// + device.udn + "' onclick='onDeviceClick(this);'>";
	// html += "<div align='center' class='device_icon'>";
	// html += "<img class='img_device_icon' src='" + device.icon + "'/></div>";
	// html += "<div class='device_info'><div class='div_device_name'>";
	// html += device.name + "</div>";
	// html += "<div class='div_device_address'>" + device.address
	// + "</div></div></div>";
	// var device_item = document.createElement('li');
	// device_item.className = "li_device_item";
	// device_item.innerHTML = html;
	// if (type == 'dms')
	// dms_listview.append(device_item);
	// else
	// dmr_listview.append(device_item);
	

	// <li><a href="#"
	// style="padding-top: 0px; padding-bottom: 0px;"> <img
	// src="img/icon_dms.png"
	// style="height: 100%; width: height; padding-left: 2%; float: left;" />
	// <h3>First</h3>
	// <p>details</p>
	// </a></li>
};

var remove_device = function(udn) {
	$("div[udn='" + udn + "']").remove();
};

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