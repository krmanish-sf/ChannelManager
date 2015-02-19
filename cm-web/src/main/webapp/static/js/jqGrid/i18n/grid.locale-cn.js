;(function($){
/**
 * jqGrid Chinese Translation for v4.2
 * henryyan 2011.11.30
 * http://www.wsria.com
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 * 
 * update 2011.11.30
 *		add double u3000 SPACE for search:odata to fix SEARCH box display err when narrow width from only use of eq/ne/cn/in/lt/gt operator under IE6/7
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "{0} - {1}\u3000? {2} ?",	// ????????
		emptyrecords: "?????",
		loadtext: "???...",
		pgtext : " {0} ? {1} ?"
	},
	search : {
		caption: "??...",
		Find: "??",
		Reset: "??",
		odata : [{oper:'eq', text:'??\u3000\u3000'},{oper:'ne', text: '??\u3000\u3000'}, { oper:'lt', text:'??\u3000\u3000'},{ oper:'le', text: '????'},{ oper:'gt', text:'??\u3000\u3000'},{ oper:'ge', text:'????'},
			{oper:'bw', text:'???'},{ oper:'bn', text:'????'},{ oper:'in', text:'??\u3000\u3000'},{ oper:'ni', text:'???'},{ oper:'ew', text:'???'},{ oper:'en', text:'????'},{ oper:'cn', text:'??\u3000\u3000'},{ oper:'nc', text:'???'},{ oper:'nu', text:'???\u3000\u3000'},{ oper:'nn', text:'???'}],
		groupOps: [	{ op: "AND", text: "??" },	{ op: "OR",  text: "??" }	]
	},
	edit : {
		addCaption: "????",
		editCaption: "????",
		bSubmit: "??",
		bCancel: "??",
		bClose: "??",
		saveData: "???????????",
		bYes : "?",
		bNo : "?",
		bExit : "??",
		msg: {
			required:"?????",
			number:"???????",
			minValue:"???????? ",
			maxValue:"???????? ",
			email: "??????e-mail??",
			integer: "???????",
			date: "???????",
			url: "?????????? ('http://' ? 'https://')",
			nodefined : " ????",
			novalue : " ??????",
			customarray : "????????????",
			customfcheck : "Custom function should be present in case of custom checking!"
			
		}
	},
	view : {
		caption: "????",
		bClose: "??"
	},
	del : {
		caption: "??",
		msg: "???????",
		bSubmit: "??",
		bCancel: "??"
	},
	nav : {
		edittext: "",
		edittitle: "??????",
		addtext:"",
		addtitle: "?????",
		deltext: "",
		deltitle: "??????",
		searchtext: "",
		searchtitle: "??",
		refreshtext: "",
		refreshtitle: "????",
		alertcap: "??",
		alerttext: "?????",
		viewtext: "",
		viewtitle: "??????"
	},
	col : {
		caption: "???",
		bSubmit: "??",
		bCancel: "??"
	},
	errors : {
		errcap : "??",
		nourl : "????url",
		norecords: "????????",
		model : "colNames ? colModel ?????"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat",
		         "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
			],
			monthNames: [
				"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
				"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
			],
			AmPm : ["am","pm","AM","PM"],
			S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th'},
			srcformat: 'Y-m-d',
			newformat: 'm-d-Y',
			parseRe : /[Tt\\\/:_;.,\t\s-]/,
			masks : {
				ISO8601Long:"Y-m-d H:i:s",
				ISO8601Short:"Y-m-d",
				ShortDate: "Y/j/n",
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

