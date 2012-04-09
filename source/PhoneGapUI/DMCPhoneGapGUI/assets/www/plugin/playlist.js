var PlaylistPlugin = function() {
};

PlaylistPlugin.prototype.loadPlaylist = function() {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'PlaylistPlugin', 'loadPlaylist', [ "" ]);
};

PlaylistPlugin.prototype.itemClick = function(idx) {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'PlaylistPlugin', 'itemClick', [ idx ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("PlaylistPlugin", new PlaylistPlugin());
});

function loadPlaylistItems(e) {
	var result = eval(e);
	for ( var i = 0; i < result.length; i++) {
		var obj = result[i];
		addPlaylistItem(obj);
	}
	myScroll_playlist.scrollTo(0, 0, 0);
	hideLoadingIcon();
	playlist_listview.listview('refresh');
	myScroll_playlist.refresh();
}

function addPlaylistItem(item) {
	var html = "<li idx='" + item.idx + "'";

	if (item.selected == "true") {
		html += "data-icon='added-to-playlist' ";
	} else {
		html += "data-icon='false' ";
	}

	html += "onclick='onPlaylistItemClick(\"" + item.idx + "\");'>";

	html += "<a href='#' style='padding-top: 0px;padding-bottom: 0px' data-icon='delete'><img src='" + item.icon
			+ "' style='height: 100%; width: height; padding-left: 4%; float: left;'/><h3>" + item.name + "</h3><p>"
			+ (item.childCount != null ? (item.childCount.toString() + " childs") : " ") + "</p></a></li>";
	playlist_listview.append(html);
}

function onPlaylistItemClick(e) {
	window.plugins.PlaylistPlugin.itemClick(e);
}

function clearPlaylist() {
	playlist_listview.html("");
	playlist_listview.listview("refresh");
}