;(function($){
/**
 * jqGrid Polish Translation
 * ?ukasz Schab lukasz@freetree.pl
 * http://FreeTree.pl
 *
 * Updated names, abbreviations, currency and date/time formats for Polish norms (also corresponding with CLDR v21.0.1 --> http://cldr.unicode.org/index) 
 * Tomasz P?czek tpeczek@gmail.com
 * http://tpeczek.blogspot.com; http://tpeczek.codeplex.com
 *
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "Poka? {0} - {1} z {2}",
		emptyrecords: "Brak rekord?w do pokazania",
		loadtext: "?adowanie...",
		pgtext : "Strona {0} z {1}"
	},
	search : {
		caption: "Wyszukiwanie...",
		Find: "Szukaj",
		Reset: "Czy??",
		odata: [{ oper:'eq', text:"dok?adnie"},{ oper:'ne', text:"r??ne od"},{ oper:'lt', text:"mniejsze od"},{ oper:'le', text:"mniejsze lub r?wne"},{ oper:'gt', text:"wi?ksze od"},{ oper:'ge', text:"wi?ksze lub r?wne"},{ oper:'bw', text:"zaczyna si? od"},{ oper:'bn', text:"nie zaczyna si? od"},{ oper:'in', text:"jest w"},{ oper:'ni', text:"nie jest w"},{ oper:'ew', text:"ko?czy si? na"},{ oper:'en', text:"nie ko?czy si? na"},{ oper:'cn', text:"zawiera"},{ oper:'nc', text:"nie zawiera"}],
		groupOps: [	{ op: "AND", text: "oraz" },	{ op: "OR",  text: "lub" }	]
	},
	edit : {
		addCaption: "Dodaj rekord",
		editCaption: "Edytuj rekord",
		bSubmit: "Zapisz",
		bCancel: "Anuluj",
		bClose: "Zamknij",
		saveData: "Dane zosta?y zmienione! Zapisa? zmiany?",
		bYes: "Tak",
		bNo: "Nie",
		bExit: "Anuluj",
		msg: {
			required: "Pole jest wymagane",
			number: "Prosz? wpisa? poprawn? liczb?",
			minValue: "warto?? musi by? wi?ksza lub r?wna od",
			maxValue: "warto?? musi by? mniejsza lub r?wna od",
			email: "nie jest poprawnym adresem e-mail",
			integer: "Prosz? wpisa? poprawn? liczb?",
			date: "Prosz? podaj poprawn? dat?",
			url: "jest niew?a?ciwym adresem URL. Pami?taj o prefiksie ('http://' lub 'https://')",
			nodefined: " niezdefiniowane!",
			novalue: " wymagana jest warto?? zwracana!",
			customarray: "Funkcja niestandardowa powinna zwraca? tablic?!",
			customfcheck: "Funkcja niestandardowa powinna by? obecna w przypadku niestandardowego sprawdzania!"
		}
	},
	view : {
		caption: "Poka? rekord",
		bClose: "Zamknij"
	},
	del : {
		caption: "Usu?",
		msg: "Czy usun?? wybrany rekord(y)?",
		bSubmit: "Usu?",
		bCancel: "Anuluj"
	},
	nav : {
		edittext: "",
		edittitle: "Edytuj wybrany wiersz",
		addtext: "",
		addtitle: "Dodaj nowy wiersz",
		deltext: "",
		deltitle: "Usu? wybrany wiersz",
		searchtext: "",
		searchtitle: "Wyszukaj rekord",
		refreshtext: "",
		refreshtitle: "Prze?aduj",
		alertcap: "Uwaga",
		alerttext: "Prosz? wybra? wiersz",
		viewtext: "",
		viewtitle: "Poka? wybrany wiersz"
	},
	col : {
		caption: "Poka?/Ukryj kolumny",
		bSubmit: "Zatwierd?",
		bCancel: "Anuluj"
	},
	errors : {
		errcap: "B??d",
		nourl: "Brak adresu url",
		norecords: "Brak danych",
		model : "D?ugo?? colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0,00'},
		currency : {decimalSeparator:",", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:" z?", defaultValue: '0,00'},
		date : {
			dayNames:   [
				"niedz.", "pon.", "wt.", "?r.", "czw.", "pt.", "sob.",
				"niedziela", "poniedzia?ek", "wtorek", "?roda", "czwartek", "pi?tek", "sobota"
			],
			monthNames: [
				"sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "pa?", "lis", "gru",
				"stycze?", "luty", "marzec", "kwiecie?", "maj", "czerwiec", "lipiec", "sierpie?", "wrzesie?", "pa?dziernik", "listopad", "grudzie?"
				],
			AmPm : ["","","",""],
			S: function (j) {return '';},
			srcformat: 'Y-m-d',
			newformat: 'd.m.Y',
			parseRe : /[Tt\\\/:_;.,\t\s-]/,
			masks : {
				ISO8601Long: "Y-m-d H:i:s",
				ISO8601Short: "Y-m-d",
				ShortDate: "d.m.y",
				LongDate: "l, j F Y",
				FullDateTime: "l, j F Y H:i:s",
				MonthDay: "j F",
				ShortTime: "H:i",
				LongTime: "H:i:s",
				SortableDateTime: "Y-m-d\\TH:i:s",
				UniversalSortableDateTime: "Y-m-d H:i:sO",
				YearMonth: "F Y"
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
