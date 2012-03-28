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

var add_device = function(element, type) {
	var device = eval(element);
	var html = "<li data-icon='false' type='" + device.type + "' udn='" + device.udn
			+ "'  onclick='onDeviceClick(this);'><a href='#' style='padding-top: 0px;padding-bottom: 0px' data-icon='delete'><img src='"
			+ device.icon + "' style='height: 100%; width: height; padding-left: 4%; float: left;'/><h3>" + device.name + "</h3><p>"
			+ device.address + "</p></a></li>";
	if (type == 'dms') {
		dms_listview.append(html);
		dms_listview.listview('refresh');
		myScroll_devi_dmS.refresh();
	} else {
		dmr_listview.append(html);
		dmr_listview.listview('refresh');
		myScroll_devi_dmR.refresh();
	}
};

var remove_device = function(udn) {
	$("li[udn='" + udn + "']").remove();
};

var onDeviceClick = function(e) {
	var type = e.getAttribute('type');
	var udn = e.getAttribute('udn');
	console.log('deviceType = ' + type.toString() + '; UDN = ' + udn.toString());

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

function setCurrentDMS(udn) {
	console.log('Set current dms ' + udn.toString());
	$("li[udn='" + udn + "']").attr('data-icon', 'check');
	dms_listview.listview('refresh');
	console.log($("li[udn='" + udn + "']").html());
}

function setCurrentDMR(udn) {
	console.log('Set current dmr ' + udn.toString());
	$("li[udn='" + udn + "']").attr('data-icon', 'check');
	dmr_listview.listview('refresh');
}
