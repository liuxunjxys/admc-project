var DevicesPlugin = function() {
};

DevicesPlugin.prototype.start = function() {
	PhoneGap.exec(null, null, 'DevicesPlugin', 'start', [ "" ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("DevicesPlugin", new DevicesPlugin());
});