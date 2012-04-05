var YoutubePlugin = function() {
};

YoutubePlugin.prototype.addToPlaylist = function(idx, proxy) {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'YoutubePlugin', 'addToPlaylist', [ idx, proxy ]);
};

YoutubePlugin.prototype.query = function(queryString) {
	showLoadingIcon();
	PhoneGap.exec(null, null, 'YoutubePlugin', 'query', [ queryString ]);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin("YoutubePlugin", new YoutubePlugin());
});

function showYoutubeResult(e) {
	console.log('show result');
	console.log(e);
	var result = eval(e);
	for ( var i = 0; i < result.length; i++) {
		console.log('add to list');
		var obj = result[i];
		yt_addItemToListView(obj);
	}
	myScroll_libs.scrollTo(0, 0, 0);
	hideLoadingIcon();
}

function yt_addItemToListView(item) {
	var html = "<li index='" + item.idx + "'";

	html += "data-icon='false' ";

	if (item.url != null) {
		html += "url='" + item.url + "' ";
	}
	html += "onclick='onYoutubeItemClick(\'" + item.idx + "\");'>";
	html += "<a href='#' style='padding-top: 0px;padding-bottom: 0px' data-icon='delete'><img src='"
			+ item.thumb
			+ "' style='height: 100%; width: height; padding-left: 4%; float: left;'/><h3>"
			+ item.title + "</h3><p>" + item.duration + "</p></a></li>";
	youttube_listview.append(html);
	youttube_listview.listview('refresh');
	youttube_listview.refresh();
	console.log(html);
}