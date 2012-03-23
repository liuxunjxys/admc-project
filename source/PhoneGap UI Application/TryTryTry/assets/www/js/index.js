var myScroll_libs;
var myScroll_you;
var myScroll_playlists;

$(document).ready(function(){
	
	$('div.div_subcontent').show();
	cssConfig(); // must show before..
	$('div.div_subcontent').hide(); // all hide
	
	$('div#content_devices').show(); //first tab appear
	
	$('div[data-role="navbar"] a').live('click', function(){
		$(this).addClass('ui-btn-active');
		$('div.div_subcontent').hide();
		
		var datahref_info = $(this).attr('data-href');
		$('div#' + datahref_info).fadeIn('slow');
		
		switch (datahref_info) {
		case "content_devices":
			
			break;
			
		case "content_library":
			setTimeout(function () {
				myScroll_libs.refresh();
			}, 0);
			break;
			
		case "content_youtube":
			setTimeout(function () {
				myScroll_you.refresh();
			}, 0);
			break;
			
		case "content_playlists":
			setTimeout(function () {
				myScroll_playlists.refresh();
			}, 0);
			break;
		}
	});
});


function cssConfig (){
	cssConfig_Libs();
	cssConfig_You();
	cssConfig_Playlists ();
	
	iScroolConfig();
}

/*LIB CONFIG*/
function cssConfig_Libs(){
	var height_of_toolbar = $('#div_libs_toolbar').height();
	var width_of_toolbar = $('#div_libs_toolbar').width();
	
	var height_of_input = height_of_toolbar - 10; //subtract margin value
	var width_of_input = width_of_toolbar - 15 - height_of_toolbar; //subtrac margin and default boder of input
	
	$('div#div_libs_toolbar input').css('height', height_of_input + 'px');
	$('div#div_libs_toolbar input').css('width', width_of_input + 'px');
}

/*YOU CONFIG*/
function cssConfig_You (){
	var height_of_toolbar = $('#div_you_toolbar').height();
	var width_of_toolbar = $('#div_you_toolbar').width();
	
	var height_of_input = height_of_toolbar - 10; //subtract margin value
	var width_of_input = width_of_toolbar - 15 - height_of_toolbar; //subtrac margin and default boder of input

	$('div#div_you_toolbar input').css('height', height_of_input + 'px');
	$('div#div_you_toolbar input').css('width', width_of_input + 'px');
}

/*PLAYLISTS CONFIG*/
function cssConfig_Playlists (){
	var height_of_toolbar = $('#div_play_toolbar').height();
	var width_of_toolbar = $('#div_play_toolbar').width();
	
	var height_of_input = height_of_toolbar - 10; //subtract margin value
	var left_of_input_ = width_of_toolbar * 2.5 / 100 - 5; // width = 95% -> left = 2.5 % and then subtract 5px margin left
	
	$('div#div_play_toolbar input').css('height', height_of_input + 'px');
	$('div#div_play_toolbar input').css('left', left_of_input_ + 'px');
}

/*IScrool config*/
function iScroolConfig (){
	setTimeout(function() {
		myScroll_libs = new iScroll('div_wrapper_libs', { hScroll: false, vScrollbar: false });
		myScroll_you = new iScroll('div_wrapper_you', { hScroll: false, vScrollbar: false });
		myScroll_playlists = new iScroll('div_wrapper_play', { hScroll: false, vScrollbar: false });
	}, 200);
}



