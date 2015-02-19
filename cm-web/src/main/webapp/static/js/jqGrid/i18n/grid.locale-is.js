;(function($){
/**
 * jqGrid Icelandic Translation
 * jtm@hi.is Univercity of Iceland
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Sko?a {0} - {1} af {2}",
	    emptyrecords: "Engar f?rslur",
		loadtext: "Hle?ur...",
		pgtext : "S??a {0} af {1}"
	},
	search : {
	    caption: "Leita...",
	    Find: "Leita",
	    Reset: "Endursetja",
	    odata: [{ oper:'eq', text:"sama og"},{ oper:'ne', text:"ekki sama og"},{ oper:'lt', text:"minna en"},{ oper:'le', text:"minna e?a jafnt og"},{ oper:'gt', text:"st?rra en"},{ oper:'ge', text:"st?rra e?a jafnt og"},{ oper:'bw', text:"byrjar ?"},{ oper:'bn', text:"byrjar ekki ?"},{ oper:'in', text:"er ?"},{ oper:'ni', text:"er ekki ?"},{ oper:'ew', text:"endar ?"},{ oper:'en', text:"endar ekki ?"},{ oper:'cn', text:"inniheldur"},{ oper:'nc', text:"inniheldur ekki"}],
	    groupOps: [	{ op: "AND", text: "allt" },	{ op: "OR",  text: "e?a" }	]
	},
	edit : {
	    addCaption: "B?ta vi? f?rslu",
	    editCaption: "Breyta f?rslu",
	    bSubmit: "Vista",
	    bCancel: "H?tta vi?",
		bClose: "Loka",
		saveData: "G?gn hafa breyst! Vista breytingar?",
		bYes : "J?",
		bNo : "Nei",
		bExit : "H?tta vi?",
	    msg: {
	        required:"Reitur er nau?synlegur",
	        number:"Vinsamlega settu inn t?lu",
	        minValue:"gildi ver?ur a? vera meira en e?a jafnt og ",
	        maxValue:"gildi ver?ur a? vera minna en e?a jafnt og ",
	        email: "er ekki l?glegt email",
	        integer: "Vinsamlega settu inn t?lu",
			date: "Vinsamlega setti inn dagsetningu",
			url: "er ekki l?glegt URL. Vantar ('http://' e?a 'https://')",
			nodefined : " er ekki skilgreint!",
			novalue : " skilagildi nau?synlegt!",
			customarray : "Fall skal skila fylki!",
			customfcheck : "Fall skal vera skilgreint!"
		}
	},
	view : {
	    caption: "Sko?a f?rslu",
	    bClose: "Loka"
	},
	del : {
	    caption: "Ey?a",
	    msg: "Ey?a v?ldum f?rslum ?",
	    bSubmit: "Ey?a",
	    bCancel: "H?tta vi?"
	},
	nav : {
		edittext: " ",
	    edittitle: "Breyta f?rslu",
		addtext:" ",
	    addtitle: "N? f?rsla",
	    deltext: " ",
	    deltitle: "Ey?a f?rslu",
	    searchtext: " ",
	    searchtitle: "Leita",
	    refreshtext: "",
	    refreshtitle: "Endurhla?a",
	    alertcap: "Vi?v?run",
	    alerttext: "Vinsamlega veldu f?rslu",
		viewtext: "",
		viewtitle: "Sko?a valda f?rslu"
	},
	col : {
	    caption: "S?na / fela d?lka",
	    bSubmit: "Vista",
	    bCancel: "H?tta vi?"	
	},
	errors : {
		errcap : "Villa",
		nourl : "Vantar sl??",
		norecords: "Engar f?rslur valdar",
	    model : "Lengd colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Sun", "M?n", "?ri", "Mi?", "Fim", "F?s", "Lau",
				"Sunnudagur", "M?nudagur", "?ri?judagur", "Mi?vikudagur", "Fimmtudagur", "F?studagur", "Laugardagur"
			],
			monthNames: [
				"Jan", "Feb", "Mar", "Apr", "Ma?", "J?n", "J?l", "?g?", "Sep", "Oct", "N?v", "Des",
				"Jan?ar", "Febr?ar", "Mars", "Apr?l", "Ma?", "J?n?", "J?l?", "?g?st", "September", "Okt?ber", "N?vember", "Desember"
			],
			AmPm : ["am","pm","AM","PM"],
			S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th'},
			srcformat: 'Y-m-d',
			newformat: 'd/m/Y',
			parseRe : /[Tt\\\/:_;.,\t\s-]/,
			masks : {
	            ISO8601Long:"Y-m-d H:i:s",
	            ISO8601Short:"Y-m-d",
	            ShortDate: "n/j/Y",
	            LongDate: "l, F d, Y",
	            FullDateTime: "l, F d, Y g:i:s A",
	            MonthDay: "F d",
	            ShortTime: "g:i A",
	            LongTime: "g:i:s A",
	            SortableDateTime: "Y-m-d\\TH:i:s",
	            UniversalSortableDateTime: "Y-m-d H:i:sO",
	            YearMonth: "F, Y"
	        },
	        reformatAfterEdit : false
		},
		baseLinkUrl: '',
		showAction: '',
	    target: '',
	    checkbox : {disabled:true},
		idName : 'id'
	}
});
})(jQuery);

