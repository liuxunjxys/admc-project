var ApplicationPlugin = function() {
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("ApplicationPlugin", new ApplicationPlugin());
});

var YoutubePlugin = function() {
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("YoutubePlugin", new YoutubePlugin());
});

var PlaylistPlugin = function() {
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("PlaylistPlugin", new PlaylistPlugin());
});

var LibraryPlugin = function() {
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("LibraryPlugin", new LibraryPlugin());
});

var DevicesPlugin = function() {
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("DevicesPlugin", new DevicesPlugin());
});
