var LibraryPlugin = function() {
};
LibraryPlugin.prototype.browse = function(objectID) {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'browse', [ objectID ]);
};

LibraryPlugin.prototype.back = function() {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'back', [ "" ]);
};

LibraryPlugin.prototype.getCurrentPage = function() {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'getCurrentPage', [ "" ]);
};

LibraryPlugin.prototype.nextPage = function() {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'nextPage', [ "" ]);
};

LibraryPlugin.prototype.previousPage = function() {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'previousPage', [ "" ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("LibraryPlugin", new LibraryPlugin());
});

function loadBrowseResult(e) {
	var result = eval(e);
	for ( var i = 0; i < result.length; i++) {
		var obj = result[i];
		addItemToListView(obj);
	}
}

function clearLibraryList() {
	library_listview.html('');
}

function addItemToListView(item) {
	var html = "<li data-icon='false' itemId='" + item.id + "' onclick='";
	if (item.icon.toString() == "img/folder_icon.png")
		html += "onContainerClick";
	else
		html += "onItemClick";
	html += "(\""
			+ item.id
			+ "\");'><a href='#' style='padding-top: 0px;padding-bottom: 0px' data-icon='delete'><img src='"
			+ item.icon
			+ "' style='height: 100%; width: height; padding-left: 4%; float: left;'/><h3>"
			+ item.name + "</h3><p>" + item.id + "</p></a></li>";
	console.log(html.toString());
	library_listview.append(html);
	library_listview.listview('refresh');
	myScroll_libs.refresh();
}

function onContainerClick(id) {
	console.log(id);
	window.plugins.LibraryPlugin.browse(id);
}

function onItemClick(id) {
	console.log('item');
}

function onBackClick() {
	window.plugins.LibraryPlugin.back();
}

function notifyBrowseComplete() {
	console.log("Browse complete");
	window.plugins.LibraryPlugin.getCurrentPage();
}
