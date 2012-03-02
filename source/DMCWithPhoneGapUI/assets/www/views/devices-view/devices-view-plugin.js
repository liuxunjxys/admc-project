var DevicesPlugin = function() {
};

DevicesPlugin.prototype.start = function() {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'start', [ "" ]);
};

DevicesPlugin.prototype.setDMS = function(udn) {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'setDMS', [ udn ]);
};

DevicesPlugin.prototype.setDMR = function(udn) {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'setDMR', [ udn ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("DevicesPlugin", new DevicesPlugin());
});