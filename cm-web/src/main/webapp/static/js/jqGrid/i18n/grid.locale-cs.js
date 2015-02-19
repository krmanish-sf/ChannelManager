;(function($){
/**
 * jqGrid Czech Translation
 * Pavel Jirak pavel.jirak@jipas.cz
 * doplnil Thomas Wagner xwagne01@stud.fit.vutbr.cz
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Zobrazeno {0} - {1} z {2} z?znam?",
	    emptyrecords: "Nenalezeny ??dn? z?znamy",
		loadtext: "Na??t?m...",
		pgtext : "Strana {0} z {1}"
	},
	search : {
		caption: "Vyhled?v?m...",
		Find: "Hledat",
		Reset: "Reset",
	    odata: [{ oper:'eq', text:"rovno"},{ oper:'ne', text:"nerovono"},{ oper:'lt', text:"men??"},{ oper:'le', text:"men?? nebo rovno"},{ oper:'gt', text:"v?t??"},{ oper:'ge', text:"v?t?? nebo rovno"},{ oper:'bw', text:"za??n? s"},{ oper:'bn', text:"neza??n? s"},{ oper:'in', text:"je v"},{ oper:'ni', text:"nen? v"},{ oper:'ew', text:"kon?? s"},{ oper:'en', text:"nekon?? s"},{ oper:'cn', text:"obahuje"},{ oper:'nc', text:"neobsahuje"}],
	    groupOps: [	{ op: "AND", text: "v?ech" },	{ op: "OR",  text: "n?kter?ho z" }	]
	},
	edit : {
		addCaption: "P?idat z?znam",
		editCaption: "Editace z?znamu",
		bSubmit: "Ulo?it",
		bCancel: "Storno",
		bClose: "Zav??t",
		saveData: "Data byla zm?n?na! Ulo?it zm?ny?",
		bYes : "Ano",
		bNo : "Ne",
		bExit : "Zru?it",
		msg: {
		    required:"Pole je vy?adov?no",
		    number:"Pros?m, vlo?te validn? ??slo",
		    minValue:"hodnota mus? b?t v?t?? ne? nebo rovn? ",
		    maxValue:"hodnota mus? b?t men?? ne? nebo rovn? ",
		    email: "nen? validn? e-mail",
		    integer: "Pros?m, vlo?te cel? ??slo",
			date: "Pros?m, vlo?te validn? datum",
			url: "nen? platnou URL. Vy?adov?n prefix ('http://' or 'https://')",
			nodefined : " nen? definov?n!",
			novalue : " je vy?adov?na n?vratov? hodnota!",
			customarray : "Custom function m?l? vr?tit pole!",
			customfcheck : "Custom function by m?la b?t p??tomna v p??pad? custom checking!"
		}
	},
	view : {
	    caption: "Zobrazit z?znam",
	    bClose: "Zav??t"
	},
	del : {
		caption: "Smazat",
		msg: "Smazat vybran?(?) z?znam(y)?",
		bSubmit: "Smazat",
		bCancel: "Storno"
	},
	nav : {
		edittext: " ",
		edittitle: "Editovat vybran? ??dek",
		addtext:" ",
		addtitle: "P?idat nov? ??dek",
		deltext: " ",
		deltitle: "Smazat vybran? z?znam ",
		searchtext: " ",
		searchtitle: "Naj?t z?znamy",
		refreshtext: "",
		refreshtitle: "Obnovit tabulku",
		alertcap: "Varov?n?",
		alerttext: "Pros?m, vyberte ??dek",
		viewtext: "",
		viewtitle: "Zobrazit vybran? ??dek"
	},
	col : {
		caption: "Zobrazit/Skr?t sloupce",
		bSubmit: "Ulo?it",
		bCancel: "Storno"	
	},
	errors : {
		errcap : "Chyba",
		nourl : "Nen? nastavena url",
		norecords: "??dn? z?znamy ke zpracov?n?",
		model : "D?lka colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Ne", "Po", "?t", "St", "?t", "P?", "So",
				"Ned?le", "Pond?l?", "?ter?", "St?eda", "?tvrtek", "P?tek", "Sobota"
			],
			monthNames: [
				"Led", "?no", "B?e", "Dub", "Kv?", "?er", "?vc", "Srp", "Z??", "??j", "Lis", "Pro",
				"Leden", "?nor", "B?ezen", "Duben", "Kv?ten", "?erven", "?ervenec", "Srpen", "Z???", "??jen", "Listopad", "Prosinec"
			],
			AmPm : ["do","od","DO","OD"],
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

