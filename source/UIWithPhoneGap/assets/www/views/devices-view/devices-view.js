
$(document).ready(function(){
	var dms_id = 1;
	var dmr_id = 1;
	
	$("#btn_add_dmsitem").click(function(){
		addDMSItem("img/dms_icon.png", "Server Name", "Server Address", "true");
	});
	
	$("#btn_add_dmritem").click(function(){
		addDMRItem("img/dmr_icon.png", "DMR Name", "DMR Address", "true")
	});
	
	function addDMSItem (imgSrc, serverName, serverAdress, isSelected){
		alert('ItemID:' + dms_id);
		
		var dms_listview = document.getElementById('list_of_dms');
		var dms_item = document.createElement('li');

		
		dms_item.innerHTML = "" +
				"<a " + "id='" + dms_id + "'" + " class='dms_listitem' href='javascript:dmSItem_ClickEvent(this)' style='padding-bottom: 0px; padding-top: 0px; padding-left: 5px;'>" +
					"<table>" +
						"<tr class='tr_itemcontent'>" +
							"<td class='td_listitem_left'>" +
								"<img class='img_imgoflistitem' alt='Server image' src='" + imgSrc + "'>" +
							"</td>" +
							"<td class='td_listitem_middle'>" +
								"<h5>" + serverName + "</h5>" +
								"<p>" + serverAdress + "</p>" +
							"</td>" +
						"</tr>" +
					"</table>" +
				"</a>" +
				"<a href='#' data-role='button' data-theme='c'></a>";
		
		console.log(dms_item.innerHTML);
		dms_listview.appendChild(dms_item);
		
		/*
		var dmsitem_maintag = document.createElement('a');
		dmsitem_maintag.setAttribute('id', dms_id);
		dmsitem_maintag.setAttribute("href", "javascript:dmSItem_ClickEvent('" + "dasjdada" + "')");
		dmsitem_maintag.innerHTML = 
			"<table>" +
				"<tr class='tr_itemcontent'>" +
					"<td class='td_listitem_left'>" +
						"<img class='img_imgoflistitem' alt='Server image' src='" + imgSrc + "'>" +
					"</td>" +
					"<td class='td_listitem_middle'>" +
						"<h5>" + serverName + "</h5>" +
						"<p>" + serverAdress + "</p>" +
					"</td>" +
				"</tr>" +
			"</table>";
		dms_item.appendChild(dmsitem_maintag);
		dms_listview.appendChild(dms_item);
		*/
		
		var list = document.getElementById('list_of_dms');
		$(list).listview("refresh");
		
		dms_id++;
	}
	
	function addDMRItem (imgSrc, DMRName, DMRAddress, isSelected){
		alert('ItemID:' + dmr_id);
		
		var dmr_listview = document.getElementById('list_of_dmr');
		var dmr_item = document.createElement('li');

		dmr_item.innerHTML = "" +
				"<a " + "id='" + dmr_id + "'" + " class='dmr_listitem' href='javascript:dmRItem_ClickEvent()' style='padding-bottom: 0px; padding-top: 0px; padding-left: 5px;'>" +
					"<table>" +
						"<tr class='tr_itemcontent'>" +
							"<td class='td_listitem_left'>" +
								"<img class='img_imgoflistitem' alt='DMR image' src='" + imgSrc + "'>" +
							"</td>" +
							"<td class='td_listitem_middle'>" +
								"<h5>" + DMRName + "</h5>" +
								"<p>" + DMRAddress + "</p>" +
							"</td>" +
						"</tr>" +
					"</table>" +
				"</a>" +
				"<a href='#' data-role='button' data-theme='c'></a>";
		
		dmr_listview.appendChild(dmr_item);

		var list = document.getElementById('list_of_dmr');
		$(list).listview("refresh");
		
		dmr_id++;
	}
});