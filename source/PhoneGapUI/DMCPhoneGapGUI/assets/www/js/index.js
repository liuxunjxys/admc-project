var myScroll_devi_dmS;
var myScroll_devi_dmR;
var myScroll_libs;
var myScroll_youtube;
var myScroll_playlists;
var icon_loading;

var dms_listview; // list dms
var dmr_listview; // list dmr
var library_listview;
var youttube_listview;

var time_to_swap_image = 200;

$(document).ready(function() {
	iScrollConfig();
	myInitPage();
	myInitDevicesSide();
	myInitYoutubeSide();
	myInitPlaylistsSide();
});

//=============================GLOBAL MEDTHODS==========================================
function iScrollConfig() {
	setTimeout(function() {
		myScroll_devi_dmS = new iScroll('div_wrraper_devi_dmS', {
			hScroll : false,
			vScrollbar : false
		});
		myScroll_devi_dmR = new iScroll('div_wrraper_devi_dmR', {
			hScroll : false,
			vScrollbar : false
		});
		myScroll_libs = new iScroll('div_wrapper_libs', {
			hScroll : false,
			vScrollbar : false
		});
		myScroll_youtube = new iScroll('div_wrapper_you', {
			hScroll : false,
			vScrollbar : false
		});
		myScroll_playlists = new iScroll('div_wrapper_play', {
			hScroll : false,
			vScrollbar : false
		});
	}, 200);
}

function changeImagePath(sender, imgPath) {
	$(sender).attr('src', imgPath);
}

function changeImagePathWithTimeOut (sender, imgPath, timeOut){
	setTimeout(function() {
		$(sender).attr('src', imgPath);
	}, timeOut);
}

function changeStateImage (sender){
	var currentPath = $(sender).attr('data-current-path');
	var stateAImgPath = $(sender).attr('data-state-a');
	var stateBImgPath = $(sender).attr('data-state-b');
	if (currentPath == stateAImgPath){
		$(sender).attr('data-current-path', stateBImgPath);
	}else{
		$(sender).attr('data-current-path', stateAImgPath);
	}
	changeImagePathWithTimeOut(sender, $(sender).attr('data-current-path'), time_to_swap_image);
	var currentState = $(sender).attr('data-my-state');
	if (currentState == 'true'){
		currentState = 'false';
	}else{
		currentState = 'true';
	}
}
//=========================================PAGE SIDE=================================
function myInitPage() {
	$('div.div_subcontent').hide(); // all hide
	$('div#content_devices').show(); // first tab appear
	$('div#div_navbar_1 a').addClass('ui-btn-active');
	$('div[data-role="navbar"] a').live('click', function() {
		$('div.div_subcontent').hide();
		var datahref_info = $(this).attr('data-href');
		$('div#' + datahref_info).fadeIn('slow');

		switch (datahref_info) {
		case "content_devices":

			break;

		case "content_library":
			setTimeout(function() {
				myScroll_libs.refresh();
			}, 0);
			break;

		case "content_youtube":
			setTimeout(function() {
				myScroll_youtube.refresh();
				console.log('refresh done!');
			}, 0);
			break;

		case "content_playlists":
			setTimeout(function() {
				myScroll_playlists.refresh();
			}, 0);
			break;
		}
		$(this).addClass('ui-btn-active');
	});
	// init variable
	// view devices:
	dms_listview = $('#div_wrraper_devi_dmS ul:first');
	dmr_listview = $('#div_wrraper_devi_dmR ul:first');
	library_listview = $('#div_wrapper_libs ul:first');
	youttube_listview = $('#div_wrapper_you ul:first');

	icon_loading = $('#icon_loading');
	icon_loading.hide();
	
	//Mapping process for all image on page
	$('.img_normal').live('vmousedown', function() {
		changeImagePath($(this), $(this).attr('data-highlight-image'));
	});
	
	$('.img_normal').live('vmouseup', function() {
		changeImagePathWithTimeOut($(this), $(this).attr('data-normal-image'), time_to_swap_image);
	});
	
	$('.img_double_state').live('vmousedown', function(){
		var currentPath = $(this).attr('data-current-path');
		var stateAImgPath = $(this).attr('data-state-a');
		var highlightImagePath = "";
		if (currentPath == stateAImgPath){
			highlightImagePath = $(this).attr('data-highlight-a');
		}else{
			highlightImagePath = $(this).attr('data-highlight-b');
		}
		changeImagePath($(this), highlightImagePath);
	});
	
	$('.img_double_state').live('vmouseup', function() {
		changeImagePathWithTimeOut($(this), $(this).attr('data-current-path'), time_to_swap_image);
	});
}

/* User interface */
function showLoadingIcon() {
	icon_loading.show();
}

function hideLoadingIcon() {
	icon_loading.hide();
}

//================================================DEVICES SIDE===========================
function myInitDevicesSide() {
	$('div#content_devices div.div_devi_subcontent').hide();
	$('div#div_devi_dmS').show();
	setTimeout(function() {
		myScroll_devi_dmS.refresh();
	}, 0);

	$('div.div_devi_subcontent div img.img_devi_navigate').live('click',
			function() {
				var id = $(this).attr('id');
				switch (id) {
				case "img_devi_goNext":
					$('div#div_devi_dmS').hide();
					$('div#div_devi_dmR').fadeIn('slow');
					setTimeout(function() {
						myScroll_devi_dmR.refresh();
					}, 0);
					break;

				case "img_devi_goPrevious":
					$('div#div_devi_dmR').hide();
					$('div#div_devi_dmS').fadeIn('slow');
					setTimeout(function() {
						myScroll_devi_dmS.refresh();
					}, 0);
					break;
				}
			});
}
//==========================================LIBRARY SIDE=================================
function onClick_previousResult() {
	console.log('view previous result');
	var btn_prev = $('#btn_prevPage');
	if (btn_prev.attr("enable") == "true") {
		console.log('previous page');
		window.plugins.LibraryPlugin.previousPage();
	}

}

function onClick_nextResult() {
	console.log('view next result');
	var btn_next = $('#btn_nextPage');
	if (btn_next.attr("enable") == "true") {
		console.log('next page');
		window.plugins.LibraryPlugin.nextPage();
	}
}

function enableBackButton() {
	console.log('enable back button');
	$('#btn_back').attr("enable", "true");
}

function disableBackButton() {
	console.log('disable back button');
	$('#btn_back').attr("enable", "false");
}

function enableNextPageButton() {
	console.log('enable next page button');
	$('#btn_nextPage').attr("enable", "true");
}

function disableNextPageButton() {
	console.log('disable next page button');
	$('#btn_nextPage').attr("enable", "false");
}

function enablePrevPageButton() {
	console.log('enable prev page button');
	$('#btn_prevPage').attr("enable", "true");
}

function disablePrevPageButton() {
	console.log('disable prev page button');
	$('#btn_prevPage').attr("enable", "false");
}

//=============================================YOUTUBE SIDE==============================
function myInitYoutubeSide (){
	$('#div_you_toolbar_right img').bind('tap', function (){
		onClick_search_you ($(this));
	});
	
	$('#div_you_proxy img').bind('tap', function (){
		onClick_activateProxy_you ($(this));
	});
	
}

// Button search event
function onClick_search_you(sender) {
	console.log('Search');
	window.plugins.YoutubePlugin.query($('#div_you_toolbar_left input').val());
}

// Button use proxy event
function onClick_activateProxy_you(sender) {
	var state = $(sender).attr('data-my-state');
	if (state == "true"){ //activated
		changeStateImage(sender);
	}else{
		changeStateImage(sender);
	}
}

//==============================================PLAYLISTS SIDE==============================

function myInitPlaylistsSide (){
	$('#img_media_control_previous').bind('tap', function(){
		onClick_previous_play ($(this));
	});
	
	$('#img_media_control_stop').bind('tap', function(){
		onClick_stop_play ($(this));
	});
	
	$('#img_media_control_next').bind('tap', function(){
		onClick_next_play ($(this));
	});
	
	$('#img_media_control_play').bind('tap', function(){
		onClick_play_play($(this));
	});
	
	$('#img_media_control_volume').bind('tap', function(){
		onClick_volume_play($(this));
	});
	
	$('#div_field_seekbar input').bind('change', function(){
		onChange_durationBar($(this));
	})
	
	$('#div_play_volume_left input').bind('change', function(){
		onChange_volumeBar($(this));
	})
}
//Parameter note: sender is seekbar (slider) that you want to set it values
//This method for init-time (if needed - in myInitPlaylistsSide method)
//By default (in html file): 
//	duration-seekbar: currentValue = 0, maxValue = 300
//	volume-seekbar: currentValue = 50, maxValue = 50
function setValueForSeekBar (sender, currentValue, maxValue){
	$(sender).attr('max', maxValue);
	$(sender).attr('value', currentValue);
}

//SEEK BAR EVENT-----------------------------------
//Seek bar - duration of content
function onChange_durationBar (sender){
	var currentValue = $(sender).attr('value');
	var maxValue = $(sender).attr('max');
	
	console.log('max: ' + currentValue);
	console.log('current: ' + maxValue);
}

//Seek bar - volume
function onChange_volumeBar (sender){
	var currentValue = $(sender).attr('value');
	var maxValue = $(sender).attr('max');
	
	console.log('max: ' + currentValue);
	console.log('current: ' + maxValue);
}


//MEDIA CONTROLER EVENT-----------------------------
//OnClick event of PLAY button
function onClick_play_play (sender){
	var state = $(sender).attr('data-my-state');
	if (state == "true"){
		//convert to pausing state
		console.log('convert to PAUSING state');
		changeStateImage(sender);//call when successful
	} else {
		//convert to playing state
		console.log('convert to PLAYING state');
		changeStateImage(sender);//call when successful
	}
}

//OnClick event of NEXT button
function onClick_next_play (sender){
	console.log('CLick event on NEXT button');
}

//OnClick event of PREVIOUS button
function onClick_previous_play (sender){
	console.log('CLick event on PREVIOUS button');
}

//OnClick event of STOP button
function onClick_stop_play (sender){
	console.log('CLick event on STOP button');
}

//OnClick event of VOLUME button{
function onClick_volume_play (sender){
	var state = $(sender).attr('data-my-state');
	if (state == "true"){
		//convert to mute state
		console.log('convert to MUTE state');
		changeStateImage(sender);//call when successful
	}else{
		//convert to activate state
		console.log('convert to ACTIVATED state');
		changeStateImage(sender);//call when successful
	}
}

/*
NOTICE: About state of double-state button (be defined in data-my-state attribute)
On Youtube-view:
	+ Proxy button:
		- true: proxy-mode is ACTIVATED
		- false: proxy-mode is DEACTIVATED
		* default: false
On Playlists-view:
	+ Play/Pause button:
		- true: on PLAYING state
		- false: on PLAUSING state
		* default: false
	+ Volume button:
		- true: on ACTIVATED mode
		- false: on MUTE mode
		* default: true
*/