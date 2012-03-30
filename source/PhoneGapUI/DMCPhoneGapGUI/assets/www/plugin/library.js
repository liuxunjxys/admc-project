LibraryPlugin.prototype.browse = function(objectID) {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'browse', [ objectID ]);
};


LibraryPlugin.prototype.back = function(objectID) {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'back', [ objectID ]);
};