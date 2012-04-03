var myScroll_devi_dmS;
var myScroll_devi_dmR;
var myScroll_libs;
var myScroll_you;
var myScroll_playlists;

var dms_listview; // list dms
var dmr_listview; // list dmr
var library_listview;

$(document).ready(function() {
	iScrollConfig();
	myInitPage();
	myInitDevicesSide();
	myInitPlaylistsSide();
});

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
		myScroll_you = new iScroll('div_wrapper_you', {
			hScroll : false,
			vScrollbar : false
		});
		myScroll_playlists = new iScroll('div_wrapper_play', {
			hScroll : false,
			vScrollbar : false
		});
	}, 200);
}

/* Index side */
function myInitPage() {
	$('div.div_subcontent').hide(); // all hide
	$('div#content_devices').show(); // first tab appear
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
				myScroll_you.refresh();
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
	$('div#div_navbar_1 a').addClass('ui-btn-active');
	// init variable
	// view devices:
	dms_listview = $('#div_wrraper_devi_dmS ul:first');
	dmr_listview = $('#div_wrraper_devi_dmR ul:first');
	library_listview = $('#div_wrapper_libs ul:first');
}

/* Devices side */
function myInitDevicesSide() {
	$('div#content_devices div.div_devi_subcontent').hide();
	$('div#div_devi_dmS').show();
	setTimeout(function() {
		myScroll_devi_dmS.refresh();
	}, 0);

	$('div.div_devi_subcontent div img.img_devi_navigate').live('click', function() {
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
/* Library side */
function onClick_previousResult(){
	console.log('view previous result');
}

function onClick_nextResult(){
	console.log('view next result');
}

/* Youtube side */

/*Playlists side*/

/* Playlist side */


