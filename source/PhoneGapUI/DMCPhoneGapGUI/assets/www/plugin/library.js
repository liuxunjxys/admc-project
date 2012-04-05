var LibraryPlugin = function() {
};
LibraryPlugin.prototype.browse = function(objectID) {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'LibraryPlugin', 'browse', [ objectID ]);
};

LibraryPlugin.prototype.addToPlaylist = function(objectID) {
	PhoneGap.exec(null, null, 'LibraryPlugin', 'addToPlaylist', [ objectID ]);
};

LibraryPlugin.prototype.back = function() {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'LibraryPlugin', 'back', [ "" ]);
};

LibraryPlugin.prototype.getCurrentPage = function() {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'LibraryPlugin', 'getCurrentPage', [ "" ]);
};

LibraryPlugin.prototype.nextPage = function() {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'LibraryPlugin', 'nextPage', [ "" ]);
};

LibraryPlugin.prototype.previousPage = function() {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'LibraryPlugin', 'previousPage', [ "" ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("LibraryPlugin", new LibraryPlugin());
});

var active_span = '<span class="ui-icon ui-icon-added-to-playlist ui-icon-shadow"></span>';

function loadBrowseResult(e) {
	var result = eval(e);
	for ( var i = 0; i < result.length; i++) {
		var obj = result[i];
		addItemToListView(obj);
	}
	myScroll_libs.scrollTo(0, 0, 0);
	hideLoadingIcon();
}

function clearLibraryList() {
	library_listview.html('');
}

function addItemToListView(item) {
	var html = "<li itemId='" + item.id + "'";

	if (item.selected == "true") {
		html += "data-icon='added-to-playlist' ";
	} else {
		html += "data-icon='false' ";
	}
	if (item.url != null) {
		html += "url='" + item.url + "' ";
	}
	if (item.childCount != null) {
		html += "onclick='onContainerClick(\"" + item.id + "\");'>";
	} else
		html += "onclick='onItemClick(\"" + item.id + "\");'>";
	html += "<a href='#' style='padding-top: 0px;padding-bottom: 0px' data-icon='delete'><img src='"
			+ item.icon
			+ "' style='height: 100%; width: height; padding-left: 4%; float: left;'/><h3>"
			+ item.name
			+ "</h3><p>"
			+ (item.childCount != null ? (item.childCount.toString() + " childs")
					: " ") + "</p></a></li>";
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
	window.plugins.LibraryPlugin.addToPlaylist(id);
}

function onBackClick() {
	var backButton = $("#btn_back");
	if (backButton.attr("enable") == "true")
		window.plugins.LibraryPlugin.back();
}

function notifyBrowseComplete() {
	console.log("Browse complete");
	window.plugins.LibraryPlugin.getCurrentPage();
}

function addItemToPlaylist(url) {
	console.log(url);
	$('li').each(
			function(index) {
				if ($(this).attr('url') != null
						&& $(this).attr('url').toString() == url) {
					$(this).find('div:first').append(active_span);
				}
			});
	dmr_listview.listview('refresh');
}

function removeItemFromPlaylist(url) {
	console.log(url);
	$('li').each(
			function(index) {
				if ($(this).attr('url') != null
						&& $(this).attr('url').toString() == url) {
					$(this).find('span:first').remove();
				}
			});
	dmr_listview.listview('refresh');
}
