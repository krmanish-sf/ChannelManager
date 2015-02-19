;(function($){
/**
 * jqGrid Romanian Translation
 * Alexandru Emil Lupu contact@alecslupu.ro
 * http://www.alecslupu.ro/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Vizualizare {0} - {1} din {2}",
		emptyrecords: "Nu exist? ?nregistr?ri de vizualizat",
		loadtext: "?nc?rcare...",
		pgtext : "Pagina {0} din {1}"
	},
	search : {
		caption: "Caut?...",
		Find: "Caut?",
		Reset: "Resetare",
		odata: [{ oper:'eq', text:"egal"},{ oper:'ne', text:"diferit"},{ oper:'lt', text:"mai mic"},{ oper:'le', text:"mai mic sau egal"},{ oper:'gt', text:"mai mare"},{ oper:'ge', text:"mai mare sau egal"},{ oper:'bw', text:"?ncepe cu"},{ oper:'bn', text:"nu ?ncepe cu"},{ oper:'in', text:"se g?se?te ?n"},{ oper:'ni', text:"nu se g?se?te ?n"},{ oper:'ew', text:"se termin? cu"},{ oper:'en', text:"nu se termin? cu"},{ oper:'cn', text:"con?ine"},{ oper:'nc', text:""}],
		groupOps: [	{ op: "AND", text: "toate" },	{ op: "OR",  text: "oricare" }	]
	},
	edit : {
		addCaption: "Ad?ugare ?nregistrare",
		editCaption: "Modificare ?nregistrare",
		bSubmit: "Salveaz?",
		bCancel: "Anulare",
		bClose: "?nchide",
		saveData: "Informa?iile au fost modificate! Salva?i modific?rile?",
		bYes : "Da",
		bNo : "Nu",
		bExit : "Anulare",
		msg: {
			required:"C?mpul este obligatoriu",
			number:"V? rug?m introduce?i un num?r valid",
			minValue:"valoarea trebuie sa fie mai mare sau egal? cu",
			maxValue:"valoarea trebuie sa fie mai mic? sau egal? cu",
			email: "nu este o adres? de e-mail valid?",
			integer: "V? rug?m introduce?i un num?r valid",
			date: "V? rug?m s? introduce?i o dat? valid?",
			url: "Nu este un URL valid. Prefixul  este necesar('http://' or 'https://')",
			nodefined : " is not defined!",
			novalue : " return value is required!",
			customarray : "Custom function should return array!",
			customfcheck : "Custom function should be present in case of custom checking!"
		}
	},
	view : {
		caption: "Vizualizare ?nregistrare",
		bClose: "?nchidere"
	},
	del : {
		caption: "?tegere",
		msg: "?terge?i ?nregistrarea (?nregistr?rile) selectate?",
		bSubmit: "?terge",
		bCancel: "Anulare"
	},
	nav : {
		edittext: "",
		edittitle: "Modific? r?ndul selectat",
		addtext:"",
		addtitle: "Adaug? r?nd nou",
		deltext: "",
		deltitle: "?terge r?ndul selectat",
		searchtext: "",
		searchtitle: "C?utare ?nregistr?ri",
		refreshtext: "",
		refreshtitle: "Re?ncarcare Grid",
		alertcap: "Avertisment",
		alerttext: "V? rug?m s? selecta?i un r?nd",
		viewtext: "",
		viewtitle: "Vizualizeaz? r?ndul selectat"
	},
	col : {
		caption: "Arat?/Ascunde coloanele",
		bSubmit: "Salveaz?",
		bCancel: "Anulare"
	},
	errors : {
		errcap : "Eroare",
		nourl : "Niciun url nu este setat",
		norecords: "Nu sunt ?nregistr?ri de procesat",
		model : "Lungimea colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0,00'},
		currency : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0,00'},
		date : {
			dayNames:   [
				"Dum", "Lun", "Mar", "Mie", "Joi", "Vin", "S?m",
				"Duminic?", "Luni", "Mar?i", "Miercuri", "Joi", "Vineri", "S?mb?t?"
			],
			monthNames: [
				"Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Noi", "Dec",
				"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
			],
			AmPm : ["am","pm","AM","PM"],
			/*
			 Here is a problem in romanian: 
					M	/	F
			 1st = primul / prima
			 2nd = Al doilea / A doua
			 3rd = Al treilea / A treia 
			 4th = Al patrulea/ A patra
			 5th = Al cincilea / A cincea 
			 6th = Al ?aselea / A ?asea
			 7th = Al ?aptelea / A ?aptea
			 .... 
			 */
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

