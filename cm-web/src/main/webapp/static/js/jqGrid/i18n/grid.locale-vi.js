;(function($){
/**
 * jqGrid Vietnamese Translation
 * L? ??nh D?ng dungtdc@gmail.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "View {0} - {1} of {2}",
		emptyrecords: "Kh?ng c? d? li?u",
		loadtext: "?ang n?p d? li?u...",
		pgtext : "Trang {0} trong t?ng s? {1}"
	},
	search : {
		caption: "T?m ki?m...",
		Find: "T?m",
		Reset: "Kh?i t?o l?i",
		odata: [{ oper:'eq', text:"b?ng"},{ oper:'ne', text:"kh?ng b?ng"},{ oper:'lt', text:"b? h?n"},{ oper:'le', text:"b? h?n ho?c b?ng"},{ oper:'gt', text:"l?n h?n"},{ oper:'ge', text:"l?n h?n ho?c b?ng"},{ oper:'bw', text:"b?t ??u v?i"},{ oper:'bn', text:"kh?ng b?t ??u v?i"},{ oper:'in', text:"trong"},{ oper:'ni', text:"kh?ng n?m trong"},{ oper:'ew', text:"k?t th?c v?i"},{ oper:'en', text:"kh?ng k?t th?c v?i"},{ oper:'cn', text:"ch?a"},{ oper:'nc', text:"kh?ng ch?a"}],
		groupOps: [	{ op: "V?", text: "t?t c?" },	{ op: "HO?C",  text: "b?t k?" }	]
	},
	edit : {
		addCaption: "Th?m b?n ghi",
		editCaption: "S?a b?n ghi",
		bSubmit: "G?i",
		bCancel: "H?y b?",
		bClose: "??ng",
		saveData: "D? li?u ?? thay ??i! C? l?u thay ??i kh?ng?",
		bYes : "C?",
		bNo : "Kh?ng",
		bExit : "H?y b?",
		msg: {
			required:"Tr??ng d? li?u b?t bu?c c?",
			number:"H?y ?i?n ??ng s?",
			minValue:"gi? tr? ph?i l?n h?n ho?c b?ng v?i ",
			maxValue:"gi? tr? ph?i b? h?n ho?c b?ng",
			email: "kh?ng ph?i l? m?t email ??ng",
			integer: "H?y ?i?n ??ng s? nguy?n",
			date: "H?y ?i?n ??ng ng?y th?ng",
			url: "kh?ng ph?i l? URL. Kh?i ??u b?t bu?c l? ('http://' ho?c 'https://')",
			nodefined : " ch?a ???c ??nh ngh?a!",
			novalue : " gi? tr? tr? v? b?t bu?c ph?i c?!",
			customarray : "H?m n?n tr? v? m?t m?ng!",
			customfcheck : "Custom function should be present in case of custom checking!"
			
		}
	},
	view : {
		caption: "Xem b?n ghi",
		bClose: "??ng"
	},
	del : {
		caption: "X?a",
		msg: "X?a b?n ghi ?? ch?n?",
		bSubmit: "X?a",
		bCancel: "H?y b?"
	},
	nav : {
		edittext: "",
		edittitle: "S?a d?ng ?? ch?n",
		addtext:"",
		addtitle: "Th?m m?i 1 d?ng",
		deltext: "",
		deltitle: "X?a d?ng ?? ch?n",
		searchtext: "",
		searchtitle: "T?m b?n ghi",
		refreshtext: "",
		refreshtitle: "N?p l?i l??i",
		alertcap: "C?nh b?o",
		alerttext: "H?y ch?n m?t d?ng",
		viewtext: "",
		viewtitle: "Xem d?ng ?? ch?n"
	},
	col : {
		caption: "Ch?n c?t",
		bSubmit: "OK",
		bCancel: "H?y b?"
	},
	errors : {
		errcap : "L?i",
		nourl : "kh?ng url ???c ??t",
		norecords: "Kh?ng c? b?n ghi ?? x? l?",
		model : "Chi?u d?i c?a colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: ".", defaultValue: '0'},
		number : {decimalSeparator:",", thousandsSeparator: ".", decimalPlaces: 2, defaultValue: '0'},
		currency : {decimalSeparator:",", thousandsSeparator: ".", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0'},
		date : {
			dayNames:   [
				"CN", "T2", "T3", "T4", "T5", "T6", "T7",
				"Ch? nh?t", "Th? hai", "Th? ba", "Th? t?", "Th? n?m", "Th? s?u", "Th? b?y"
			],
			monthNames: [
				"Th1", "Th2", "Th3", "Th4", "Th5", "Th6", "Th7", "Th8", "Th9", "Th10", "Th11", "Th12",
				"Th?ng m?t", "Th?ng hai", "Th?ng ba", "Th?ng t?", "Th?ng n?m", "Th?ng s?u", "Th?ng b?y", "Th?ng t?m", "Th?ng ch?n", "Th?ng m??i", "Th?ng m??i m?t", "Th?ng m??i hai"
			],
			AmPm : ["s?ng","chi?u","S?NG","CHI?U"],
			S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th';},
			srcformat: 'Y-m-d',
			newformat: 'n/j/Y',
			parseRe : /[Tt\\\/:_;.,\t\s-]/,
			masks : {
				// see http://php.net/manual/en/function.date.php for PHP format used in jqGrid
				// and see http://docs.jquery.com/UI/Datepicker/formatDate
				// and https://github.com/jquery/globalize#dates for alternative formats used frequently
				// one can find on https://github.com/jquery/globalize/tree/master/lib/cultures many
				// information about date, time, numbers and currency formats used in different countries
				// one should just convert the information in PHP format
				ISO8601Long:"Y-m-d H:i:s",
				ISO8601Short:"Y-m-d",
				// short date:
				//    n - Numeric representation of a month, without leading zeros
				//    j - Day of the month without leading zeros
				//    Y - A full numeric representation of a year, 4 digits
				// example: 3/1/2012 which means 1 March 2012
				ShortDate: "n/j/Y", // in jQuery UI Datepicker: "M/d/yyyy"
				// long date:
				//    l - A full textual representation of the day of the week
				//    F - A full textual representation of a month
				//    d - Day of the month, 2 digits with leading zeros
				//    Y - A full numeric representation of a year, 4 digits
				LongDate: "l, F d, Y", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy"
				// long date with long time:
				//    l - A full textual representation of the day of the week
				//    F - A full textual representation of a month
				//    d - Day of the month, 2 digits with leading zeros
				//    Y - A full numeric representation of a year, 4 digits
				//    g - 12-hour format of an hour without leading zeros
				//    i - Minutes with leading zeros
				//    s - Seconds, with leading zeros
				//    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
				FullDateTime: "l, F d, Y g:i:s A", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy h:mm:ss tt"
				// month day:
				//    F - A full textual representation of a month
				//    d - Day of the month, 2 digits with leading zeros
				MonthDay: "F d", // in jQuery UI Datepicker: "MMMM dd"
				// short time (without seconds)
				//    g - 12-hour format of an hour without leading zeros
				//    i - Minutes with leading zeros
				//    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
				ShortTime: "g:i A", // in jQuery UI Datepicker: "h:mm tt"
				// long time (with seconds)
				//    g - 12-hour format of an hour without leading zeros
				//    i - Minutes with leading zeros
				//    s - Seconds, with leading zeros
				//    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
				LongTime: "g:i:s A", // in jQuery UI Datepicker: "h:mm:ss tt"
				SortableDateTime: "Y-m-d\\TH:i:s",
				UniversalSortableDateTime: "Y-m-d H:i:sO",
				// month with year
				//    Y - A full numeric representation of a year, 4 digits
				//    F - A full textual representation of a month
				YearMonth: "F, Y" // in jQuery UI Datepicker: "MMMM, yyyy"
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

