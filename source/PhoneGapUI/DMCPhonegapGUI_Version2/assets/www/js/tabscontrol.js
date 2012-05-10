var homeNetworkSubTab;
var playlistSubTab;
var internetSubTab;

var currentSubTab = "homenetwork";
var currentTab = "library";

function initTabs (){
	setPaddingBottomCSS(getFooterHeight(true) + 'px'); //call from globalFooter.js
	
	homeNetworkSubTab = $('#div_content_homenetwork');
	playlistSubTab = $('#div_content_playlist');
	internetSubTab = $('#div_content_internet');
	
	playlistSubTab.hide();
	internetSubTab.hide();
	$('#div_content_internet_searchbar').hide();
	
	//switching to library-tab event
	$('#div_navbar_1').bind('tap', function (){
		switchingLibrarytab ();
	});
	
	//switching to nowplaying-tab event
	$('#div_navbar_2').bind('tap', function (){
		switchingNowplayingTab ();
	});
	
	//tap event on subtab
	$('.library_tab_info img').bind('tap', function(){
		switchingSubTabInLibraryTab($(this));
	});
	
	//tap event - youtube search
	$('#div_content_internet_searchbar_right img').bind('tap', function(){
		
	});
}

function setPaddingBottomCSS (value){
	$('.content_library').css('paddingBottom', value);
}

function switchingLibrarytab (){
	if (currentTab != "library"){
		//hide all content in nowplaying
		/*$('.nowplaying_tab').hide();*/
		$('#div_navbar_1 div').css("backgroundColor", "#33B5E5");
		$('#div_navbar_2 div').css("backgroundColor", "black");
		//do something to show library
		$('#div_global_library_toolbar').fadeIn("fast");
	
		switch (currentSubTab) {
		case "homenetwork":
			showHomeNetworkSubtab($(".library_tab_info img[data-href='homenetwork']"));
			break;
			
		case "playlist":
			showPlaylistSubtab($(".library_tab_info img[data-href='playlist']"));
			break;
			
		case "internet":
			showInternetSubtab($(".library_tab_info img[data-href='internet']"));
			break;
		}
		currentTab = "library";
	}
}

function switchingNowplayingTab (){
	if (currentTab != "nowplaying"){
		//hide all content in librarytab
		$('.library_tab').hide();
		$('#div_navbar_1 div').css("backgroundColor", "black");
		$('#div_navbar_2 div').css("backgroundColor", "#33B5E5");
		//do something to show nowplayingtab
		//...
		currentTab = "nowplaying";
	}
}

function switchingSubTabInLibraryTab (sender){
	var dataHref = sender.attr('data-href');
	
	if (currentSubTab != dataHref){
		switch (dataHref) {
		case "homenetwork":
			showHomeNetworkSubtab(sender);
			break;
			
		case "playlist":
			showPlaylistSubtab(sender);
			break;
			
		case "internet":
			showInternetSubtab(sender);
			break;
		}
		currentSubTab = dataHref;
	}
}

function showHomeNetworkSubtab (sender){
	playlistSubTab.hide();
	internetSubTab.hide();
	$('#div_content_internet_searchbar').hide();
	if (playlistContentControler_visible){
		playlistContentControler.hide();
	}
	homeNetworkSubTab.fadeIn("fast", function(){
		window.scrollTo(0, 0);
		if (homenetworkContentControler_visible){
			showHomeNetworkCC();
		}
	});
	
	sender.attr('src', 'img/ic_homenetwork_hl.png');
	$(".library_tab_info img[data-href='playlist']").attr('src', 'img/ic_playlist.png');
	$(".library_tab_info img[data-href='internet']").attr('src', 'img/ic_internet.png');
}

function showPlaylistSubtab (sender){
	homeNetworkSubTab.hide();
	internetSubTab.hide();
	$('#div_content_internet_searchbar').hide();
	if (homenetworkContentControler_visible){
		homenetworkContentControler.hide();
	}
	playlistSubTab.fadeIn("fast", function(){
		window.scrollTo(0, 0);
		if (playlistContentControler_visible){
			showPlaylistCC();
		}
	});
	
	sender.attr('src', 'img/ic_playlist_hl.png');
	$(".library_tab_info img[data-href='homenetwork']").attr('src', 'img/ic_homenetwork.png');
	$(".library_tab_info img[data-href='internet']").attr('src', 'img/ic_internet.png');
}

function showInternetSubtab (sender){
	homeNetworkSubTab.hide();
	playlistSubTab.hide();
	if (homenetworkContentControler_visible){
		homenetworkContentControler.hide();
	}
	if (playlistContentControler_visible){
		playlistContentControler.hide();
	}
	$('#div_content_internet_searchbar').show();
	internetSubTab.fadeIn("fast", function(){
		window.scrollTo(0, 0);
	});
	
	sender.attr('src', 'img/ic_internet_hl.png');
	$(".library_tab_info img[data-href='homenetwork']").attr('src', 'img/ic_homenetwork.png');
	$(".library_tab_info img[data-href='playlist']").attr('src', 'img/ic_playlist.png');
}

/*
var tc_handler;
var tc_slider;
var tc_mouseDown = false;
var tc_mouseX = 0;
var tc_lenght = 3;
var tc_currentIndex = 0;
var tc_xOffset = 0;
var tc_containerWidth = 0;
var tc_tolerance = 0.25;
var tc_delayTime = 300;

function initTabs (){
	$('.main_content_library').css('paddingBottom', $('#div_global_footer').css('height'));
	
	
	tc_handler = $('#div_main_content_library');
	tc_slider = $('#div_slider_container_library');
	tc_containerWidth = tc_handler.width();
	
	$('.main_content_library').css('width', tc_containerWidth);
	
	tc_handler.bind('vmousedown', function(event){
		if (!tc_mouseDown){
			tc_mouseDown = true;
			tc_mouseX = event.pageX;
		}
	});
	
	tc_handler.bind('vmousemove', function(event){
		if (tc_mouseDown){
			tc_xOffset = event.pageX - tc_mouseX;
			if (tc_xOffset < 0){
				if (tc_currentIndex < tc_lenght - 1)
					tc_slider.css('left', - tc_currentIndex * tc_containerWidth + tc_xOffset);
			}else{
				if (tc_currentIndex > 0)
					tc_slider.css('left', - tc_currentIndex * tc_containerWidth + tc_xOffset);
			}
		}
	});
	
	tc_handler.bind('vmouseup', function(event){
		tc_mouseDown = false;
		if (tc_xOffset == 0){
			return false;
		}
		
		var fullWidth = tc_containerWidth;
		var haflWidth = fullWidth / 2;
		
		if (-tc_xOffset > haflWidth - fullWidth * tc_tolerance){
			tc_currentIndex++;
			if (tc_currentIndex >= tc_lenght)
				tc_currentIndex = tc_lenght - 1;
			tc_slider.animate({left: -tc_currentIndex * tc_containerWidth}, tc_delayTime);
		}else if (tc_xOffset > haflWidth - fullWidth * tc_tolerance){
			tc_currentIndex--;
			if (tc_currentIndex < 0)
				tc_currentIndex = 0;
			tc_slider.animate({left: -tc_currentIndex * tc_containerWidth}, tc_delayTime);
		}else{
			tc_slider.animate({left: -tc_currentIndex * tc_containerWidth}, tc_delayTime);
		}
		
		tc_xOffset = 0;
		return true;
	});
}
*/