;(function($){
/**
 * jqGrid Slovak Translation
 * Milan Cibulka
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Zobrazen?ch {0} - {1} z {2} z?znamov",
	    emptyrecords: "Neboli n?jden? ?iadne z?znamy",
		loadtext: "Na??t?m...",
		pgtext : "Strana {0} z {1}"
	},
	search : {
		caption: "Vyh?ad?vam...",
		Find: "H?ada?",
		Reset: "Reset",
	    odata: [{ oper:'eq', text:"rovn? sa"},{ oper:'ne', text:"nerovn? sa"},{ oper:'lt', text:"men?ie"},{ oper:'le', text:"men?ie alebo rovnaj?ce sa"},{ oper:'gt', text:"v???ie"},{ oper:'ge', text:"v???ie alebo rovnaj?ce sa"},{ oper:'bw', text:"za??na s"},{ oper:'bn', text:"neza??na s"},{ oper:'in', text:"je v"},{ oper:'ni', text:"nie je v"},{ oper:'ew', text:"kon?? s"},{ oper:'en', text:"nekon?? s"},{ oper:'cn', text:"obahuje"},{ oper:'nc', text:"neobsahuje"}],
	    groupOps: [	{ op: "AND", text: "v?etk?ch" },	{ op: "OR",  text: "niektor?ho z" }	]
	},
	edit : {
		addCaption: "Prida? z?znam",
		editCaption: "Edit?cia z?znamov",
		bSubmit: "Ulo?i?",
		bCancel: "Storno",
		bClose: "Zavrie?",
		saveData: "?daje boli zmenen?! Ulo?i? zmeny?",
		bYes : "Ano",
		bNo : "Nie",
		bExit : "Zru?i?",
		msg: {
		    required:"Pole je po?adovan?",
		    number:"Pros?m, vlo?te val?dne ??slo",
		    minValue:"hodnota mus? b?? v???ia ako alebo rovn? ",
		    maxValue:"hodnota mus? b?? men?ia ako alebo rovn? ",
		    email: "nie je val?dny e-mail",
		    integer: "Pros?m, vlo?te cel? ??slo",
			date: "Pros?m, vlo?te val?dny d?tum",
			url: "nie je platnou URL. Po?adovan? prefix ('http://' alebo 'https://')",
			nodefined : " nie je definovan?!",
			novalue : " je vy?adovan? n?vratov? hodnota!",
			customarray : "Custom function mala vr?ti? pole!",
			customfcheck : "Custom function by mala by? pr?tomn? v pr?pade custom checking!"
		}
	},
	view : {
	    caption: "Zobrazi? z?znam",
	    bClose: "Zavrie?"
	},
	del : {
		caption: "Zmaza?",
		msg: "Zmaza? vybran?(?) z?znam(y)?",
		bSubmit: "Zmaza?",
		bCancel: "Storno"
	},
	nav : {
		edittext: " ",
		edittitle: "Editova? vybran? riadok",
		addtext:" ",
		addtitle: "Prida? nov? riadek",
		deltext: " ",
		deltitle: "Zmaza? vybran? z?znam ",
		searchtext: " ",
		searchtitle: "N?js? z?znamy",
		refreshtext: "",
		refreshtitle: "Obnovi? tabu?ku",
		alertcap: "Varovanie",
		alerttext: "Pros?m, vyberte riadok",
		viewtext: "",
		viewtitle: "Zobrazi? vybran? riadok"
	},
	col : {
		caption: "Zobrazit/Skr?? st?pce",
		bSubmit: "Ulo?i?",
		bCancel: "Storno"	
	},
	errors : {
		errcap : "Chyba",
		nourl : "Nie je nastaven? url",
		norecords: "?iadne z?znamy k spracovaniu",
		model : "D??ka colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Ne", "Po", "Ut", "St", "?t", "Pi", "So",
				"Nedela", "Pondelok", "Utorok", "Streda", "?tvrtok", "Piatek", "Sobota"
			],
			monthNames: [
				"Jan", "Feb", "Mar", "Apr", "M?j", "J?n", "J?l", "Aug", "Sep", "Okt", "Nov", "Dec",
				"Janu?r", "Febru?r", "Marec", "Apr?l", "M?j", "J?n", "J?l", "August", "September", "Okt?ber", "November", "December"
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

