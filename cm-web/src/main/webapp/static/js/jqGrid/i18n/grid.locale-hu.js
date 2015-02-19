;(function($){
/**
 * jqGrid Hungarian Translation
 * ?rszigety ?d?m udx6bs@freemail.hu
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/

$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Oldal {0} - {1} / {2}",
		emptyrecords: "Nincs tal?lat",
		loadtext: "Bet?lt?s...",
		pgtext : "Oldal {0} / {1}"
	},
	search : {
		caption: "Keres?s...",
		Find: "Keres",
		Reset: "Alap?rtelmezett",
		odata: [{ oper:'eq', text:"egyenl?"},{ oper:'ne', text:"nem egyenl?"},{ oper:'lt', text:"kevesebb"},{ oper:'le', text:"kevesebb vagy egyenl?"},{ oper:'gt', text:"nagyobb"},{ oper:'ge', text:"nagyobb vagy egyenl?"},{ oper:'bw', text:"ezzel kezd?dik"},{ oper:'bn', text:"nem ezzel kezd?dik"},{ oper:'in', text:"tartalmaz"},{ oper:'ni', text:"nem tartalmaz"},{ oper:'ew', text:"v?gz?dik"},{ oper:'en', text:"nem v?gz?dik"},{ oper:'cn', text:"tartalmaz"},{ oper:'nc', text:"nem tartalmaz"}],
		groupOps: [	{ op: "AND", text: "all" },	{ op: "OR",  text: "any" }	]
	},
	edit : {
		addCaption: "?j t?tel",
		editCaption: "T?tel szerkeszt?se",
		bSubmit: "Ment?s",
		bCancel: "M?gse",
		bClose: "Bez?r?s",
		saveData: "A t?tel megv?ltozott! T?tel ment?se?",
		bYes : "Igen",
		bNo : "Nem",
		bExit : "M?gse",
		msg: {
			required:"K?telez? mez?",
			number:"K?rj?k, adjon meg egy helyes sz?mot",
			minValue:"Nagyobb vagy egyenl?nek kell lenni mint ",
			maxValue:"Kisebb vagy egyenl?nek kell lennie mint",
			email: "hib?s emailc?m",
			integer: "K?rj?k adjon meg egy helyes eg?sz sz?mot",
			date: "K?rj?k adjon meg egy helyes d?tumot",
			url: "nem helyes c?m. El?tag k?telez? ('http://' vagy 'https://')",
			nodefined : " nem defini?lt!",
			novalue : " visszat?r?si ?rt?k k?telez?!!",
			customarray : "Custom function should return array!",
			customfcheck : "Custom function should be present in case of custom checking!"
			
		}
	},
	view : {
		caption: "T?tel megtekint?se",
		bClose: "Bez?r?s"
	},
	del : {
		caption: "T?rl?s",
		msg: "Kiv?laztott t?tel(ek) t?rl?se?",
		bSubmit: "T?rl?s",
		bCancel: "M?gse"
	},
	nav : {
		edittext: "",
		edittitle: "T?tel szerkeszt?se",
		addtext:"",
		addtitle: "?j t?tel hozz?ad?sa",
		deltext: "",
		deltitle: "T?tel t?rl?se",
		searchtext: "",
		searchtitle: "Keres?s",
		refreshtext: "",
		refreshtitle: "Friss?t?s",
		alertcap: "Figyelmeztet?s",
		alerttext: "K?rem v?lasszon t?telt.",
		viewtext: "",
		viewtitle: "T?tel megtekint?se"
	},
	col : {
		caption: "Oszlopok kiv?laszt?sa",
		bSubmit: "Ok",
		bCancel: "M?gse"
	},
	errors : {
		errcap : "Hiba",
		nourl : "Nincs URL be?ll?tva",
		norecords: "Nincs feldolgoz?sra v?r? t?tel",
		model : "colNames ?s colModel hossza nem egyenl?!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0,00'},
		currency : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0,00'},
		date : {
			dayNames:   [
				"Va", "H?", "Ke", "Sze", "Cs?", "P?", "Szo",
				"Vas?rnap", "H?tf?", "Kedd", "Szerda", "Cs?t?rt?k", "P?ntek", "Szombat"
			],
			monthNames: [
				"Jan", "Feb", "M?r", "?pr", "M?j", "J?n", "J?l", "Aug", "Szep", "Okt", "Nov", "Dec",
				"Janu?r", "Febru?r", "M?rcius", "?prili", "M?jus", "J?nius", "J?lius", "Augusztus", "Szeptember", "Okt?ber", "November", "December"
			],
			AmPm : ["de","du","DE","DU"],
			S: function (j) {return '.-ik';},
			srcformat: 'Y-m-d',
			newformat: 'Y/m/d',
			parseRe : /[Tt\\\/:_;.,\t\s-]/,
			masks : {
				ISO8601Long:"Y-m-d H:i:s",
				ISO8601Short:"Y-m-d",
				ShortDate: "Y/j/n",
				LongDate: "Y. F h? d., l",
				FullDateTime: "l, F d, Y g:i:s A",
				MonthDay: "F d",
				ShortTime: "a g:i",
				LongTime: "a g:i:s",
				SortableDateTime: "Y-m-d\\TH:i:s",
				UniversalSortableDateTime: "Y-m-d H:i:sO",
				YearMonth: "Y, F"
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

