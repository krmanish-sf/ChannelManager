;(function($){
/**
 * jqGrid Lithuanian Translation
 * aur1mas aur1mas@devnet.lt
 * http://aur1mas.devnet.lt
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Per?i?rima {0} - {1} i? {2}",
		emptyrecords: "?ra?? n?ra",
		loadtext: "Kraunama...",
		pgtext : "Puslapis {0} i? {1}"
	},
	search : {
		caption: "Paie?ka...",
		Find: "Ie?koti",
		Reset: "Atstatyti",
		odata: [{ oper:'eq', text:"lygu"},{ oper:'ne', text:"nelygu"},{ oper:'lt', text:"ma?iau"},{ oper:'le', text:"ma?iau arba lygu"},{ oper:'gt', text:"daugiau"},{ oper:'ge', text:"daugiau arba lygu"},{ oper:'bw', text:"prasideda"},{ oper:'bn', text:"neprasideda"},{ oper:'in', text:"reik?m? yra"},{ oper:'ni', text:"reik?m?s n?ra"},{ oper:'ew', text:"baigiasi"},{ oper:'en', text:"nesibaigia"},{ oper:'cn', text:"yra sudarytas"},{ oper:'nc', text:"n?ra sudarytas"}],
		groupOps: [	{ op: "AND", text: "visi" },	{ op: "OR",  text: "bet kuris" }	]
	},
	edit : {
		addCaption: "Sukurti ?ra??",
		editCaption: "Redaguoti ?ra??",
		bSubmit: "I?saugoti",
		bCancel: "At?aukti",
		bClose: "U?daryti",
		saveData: "Duomenys buvo pakeisti! I?saugoti pakeitimus?",
		bYes : "Taip",
		bNo : "Ne",
		bExit : "At?aukti",
		msg: {
			required:"Privalomas laukas",
			number:"?veskite tinkam? numer?",
			minValue:"reik?m? turi b?ti didesn? arba lygi ",
			maxValue:"reik?m? turi b?ti ma?esn? arba lygi",
			email: "neteisingas el. pa?to adresas",
			integer: "?veskite teising? sveik?j? skai?i?",
			date: "?veskite teising? dat?",
			url: "blogas adresas. Nepamir?kite prid?ti ('http://' arba 'https://')",
			nodefined : " n?ra apibr??ta!",
			novalue : " turi b?ti gra?inama kokia nors reik?m?!",
			customarray : "Custom f-ja turi gr??inti masyv?!",
			customfcheck : "Custom f-ja t?r?t? b?ti sukurta, prie? bandant j? naudoti!"
			
		}
	},
	view : {
		caption: "Per?i?r?ti ?ra?us",
		bClose: "U?daryti"
	},
	del : {
		caption: "I?trinti",
		msg: "I?trinti pa?ym?tus ?ra?us(-?)?",
		bSubmit: "I?trinti",
		bCancel: "At?aukti"
	},
	nav : {
		edittext: "",
		edittitle: "Redaguoti pa?ym?t? eilut?",
		addtext:"",
		addtitle: "Prid?ti nauj? eilut?",
		deltext: "",
		deltitle: "I?trinti pa?ym?t? eilut?",
		searchtext: "",
		searchtitle: "Rasti ?ra?us",
		refreshtext: "",
		refreshtitle: "Perkrauti lentel?",
		alertcap: "?sp?jimas",
		alerttext: "Pasirinkite eilut?",
		viewtext: "",
		viewtitle: "Per?i?r?ti pasirinkt? eilut?"
	},
	col : {
		caption: "Pasirinkti stulpelius",
		bSubmit: "Gerai",
		bCancel: "At?aukti"
	},
	errors : {
		errcap : "Klaida",
		nourl : "Url reik?m? turi b?ti perduota",
		norecords: "N?ra ?ra??, kuriuos b?t? galima apdoroti",
		model : "colNames skai?ius <> colModel skai?iui!"
	},
	formatter : {
		integer : {thousandsSeparator: "", defaultValue: '0'},
		number : {decimalSeparator:",", thousandsSeparator: "", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:",", thousandsSeparator: "", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Sek", "Pir", "Ant", "Tre", "Ket", "Pen", "?e?",
				"Sekmadienis", "Pirmadienis", "Antradienis", "Tre?iadienis", "Ketvirtadienis", "Penktadienis", "?e?tadienis"
			],
			monthNames: [
				"Sau", "Vas", "Kov", "Bal", "Geg", "Bir", "Lie", "Rugj", "Rugs", "Spa", "Lap", "Gru",
				"Sausis", "Vasaris", "Kovas", "Balandis", "Gegu??", "Bir?elis", "Liepa", "Rugpj?tis", "Rugs?jis", "Spalis", "Lapkritis", "Gruodis"
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

