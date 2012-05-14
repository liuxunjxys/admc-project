
var btn_footerControler;

var dmrController;
var dmrController_visible = true;
var btn_dmrPlayController;
var dmrList;

var homenetworkContentControler;
var homenetworkContentControler_visible = false;
var playlistContentControler;
var playlistContentControler_visible = false;

function initGlobalFooter (){
	btn_footerControler = $('#div_remote_footer_controler img');
	dmrController = $('#div_dmr_controler');
	btn_dmrPlayController = $('#div_dmr_play img');
	
	homenetworkContentControler = $('#div_content_network_controler');
	homenetworkContentControler.hide();
	
	playlistContentControler = $('#div_content_playlist_controler');
	playlistContentControler.hide();
	
	btn_footerControler.bind('tap', function(){
		onTap_btn_footerControler ($(this));
	});
	
	btn_dmrPlayController.bind('tap', function(){
		onTap_playButton ($(this));
	});
}

//just only get height of footer..
function getFooterHeight (dmrContentVisible){
	var result =  $('#div_remote_footer_controler').height();
	if (dmrContentVisible)
		result += $('#div_dmr_controler').height();
	return result;
}

function getPaddingOfNetworkTab (dmrContentVisible, networkContentControlerVisible){
	var result =  $('#div_remote_footer_controler').height();
	if (dmrContentVisible)
		result += $('#div_dmr_controler').height();
	if (networkContentControlerVisible)
		result += $('#div_content_network_controler').height();
	return result;
}

function getPaddingOfplaylistSubTab (dmrContentVisible, playlistContentControlerVisible){
	var result =  $('#div_remote_footer_controler').height();
	if (dmrContentVisible)
		result += $('#div_dmr_controler').height();
	if (playlistContentControlerVisible)
		result += $('#div_content_playlist_controler').height();
	return result;
}

//Show and hide homenetwork-content-controller
function showHomeNetworkCC (){
	if (currentSubTab == "homenetwork" && currentTab == "library"){
		homenetworkContentControler_visible = true;
		if (dmrController_visible){
			var paddingValue = getPaddingOfNetworkTab(true, true);
			homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
		}else{
			var paddingValue = getPaddingOfNetworkTab(false, true);
			homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
		}
		homenetworkContentControler.fadeIn("fast");
	}
}

function hideHomeNetworkCC (){
	homenetworkContentControler_visible = false;
	if (dmrController_visible){
		var paddingValue = getFooterHeight(true);
		homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
	}else{
		var paddingValue = getFooterHeight(false);
		homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
	}
	homenetworkContentControler.hide();
}

//Show and hide playlist-content-controller
function showPlaylistCC (){
	if (currentSubTab == "playlist" && currentTab == "library"){
		playlistContentControler_visible = true;
		if (dmrController_visible){
			var paddingValue = getPaddingOfplaylistSubTab(true, true);
			playlistSubTab.css('paddingBottom', paddingValue + 'px');
		}else{
			var paddingValue = getPaddingOfplaylistSubTab(false, true);
			playlistSubTab.css('paddingBottom', paddingValue + 'px');
		}
		playlistContentControler.fadeIn("fast");
	}
}

function hidePlaylistCC(){
	playlistContentControler_visible = false;
	if (dmrController_visible){
		var paddingValue = getFooterHeight(true);
		playlistSubTab.css('paddingBottom', paddingValue + 'px');
	}else{
		var paddingValue = getFooterHeight(false);
		playlistSubTab.css('paddingBottom', paddingValue + 'px');
	}
	playlistContentControler.hide();
}

function toggleNetworkContentControler (){
	if (homenetworkContentControler_visible){
		hideHomeNetworkCC();
	}else{
		showHomeNetworkCC();
	}
}

function togglePlaylistContentControler (){
	if (playlistContentControler_visible){
		hidePlaylistCC();
	}else{
		showPlaylistCC();
	}
}

//Event Function--------------------------
function onTap_btn_footerControler (sender){
	if (sender.attr('data-my-state') == "true"){
		dmrController_visible = false;
		//resetAll
		//setPaddingBottomCSS(getFooterHeight(dmrController_visible) + 'px');
		
		var paddingValue = getPaddingOfNetworkTab(dmrController_visible, homenetworkContentControler_visible);
		homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
		
		paddingValue = getPaddingOfplaylistSubTab(dmrController_visible, playlistContentControler_visible);
		playlistSubTab.css('paddingBottom', paddingValue + 'px');
		
		paddingValue = getFooterHeight(false);
		internetSubTab.css('paddingBottom', paddingValue + 'px');
		//Tap effect
		dmrController.animate({height: '0%'}, "fast", function (){
			dmrController.hide();
		});
		$('#div_remote_footer_controler').animate({bottom: '-1%'}, "fast");
		homenetworkContentControler.animate({bottom: '6%'}, "fast");
		playlistContentControler.animate({bottom: '6%'}, "fast");
	}else{
		dmrController_visible = true;
		dmrController.show();
		dmrController.animate({height: '15%'}, "fast");
		homenetworkContentControler.animate({bottom: '21%'}, "fast");
		playlistContentControler.animate({bottom: '21%'}, "fast");
		$('#div_remote_footer_controler').animate({bottom: '14%'}, "fast", function(){
			//resetAll
			setPaddingBottomCSS(getFooterHeight(true) + 'px');
		
			var paddingValue = getPaddingOfNetworkTab(dmrController_visible, homenetworkContentControler_visible);
			homeNetworkSubTab.css('paddingBottom', paddingValue + 'px');
			
			var paddingValue = getPaddingOfplaylistSubTab(dmrController_visible, playlistContentControler_visible);
			playlistSubTab.css('paddingBottom', paddingValue + 'px');
			
			paddingValue = getFooterHeight(true);
			internetSubTab.css('paddingBottom', paddingValue + 'px');
		});
	}
	changeStateImage(sender);
}

function onTap_playButton (sender){
	
}
