;(function($){
/**
 * jqGrid Turkish Translation
 * Erhan G?ndo?an (erhan@trposta.net)
 * http://blog.zakkum.com
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "{0}-{1} listeleniyor. Toplam:{2}",
	    emptyrecords: "Kay?t bulunamad?",
		loadtext: "Y?kleniyor...",
		pgtext : "{0}/{1}. Sayfa"
	},
	search : {
	    caption: "Arama...",
	    Find: "Bul",
	    Reset: "Temizle",	    
	    odata: [{ oper:'eq', text:"e?it"},{ oper:'ne', text:"e?it de?il"},{ oper:'lt', text:"daha az"},{ oper:'le', text:"daha az veya e?it"},{ oper:'gt', text:"daha fazla"},{ oper:'ge', text:"daha fazla veya e?it"},{ oper:'bw', text:"ile ba?layan"},{ oper:'bn', text:"ile ba?lamayan"},{ oper:'in', text:"i?inde"},{ oper:'ni', text:"i?inde de?il"},{ oper:'ew', text:"ile biten"},{ oper:'en', text:"ile bitmeyen"},{ oper:'cn', text:"i?eren"},{ oper:'nc', text:"i?ermeyen"}],
	    groupOps: [	{ op: "VE", text: "t?m" },	{ op: "VEYA",  text: "herhangi" }	]
	},
	edit : {
	    addCaption: "Kay?t Ekle",
	    editCaption: "Kay?t D?zenle",
	    bSubmit: "G?nder",
	    bCancel: "?ptal",
		bClose: "Kapat",
		saveData: "Veriler de?i?ti! Kay?t edilsin mi?",
		bYes : "Evet",
		bNo : "Hay?t",
		bExit : "?ptal",
	    msg: {
	        required:"Alan gerekli",
	        number:"L?tfen bir numara giriniz",
	        minValue:"girilen de?er daha b?y?k ya da buna e?it olmal?d?r",
	        maxValue:"girilen de?er daha k???k ya da buna e?it olmal?d?r",
	        email: "ge?erli bir e-posta adresi de?ildir",
	        integer: "L?tfen bir tamsay? giriniz",
			url: "Ge?erli bir URL de?il. ('http://' or 'https://') ?n eki gerekli.",
			nodefined : " is not defined!",
			novalue : " return value is required!",
			customarray : "Custom function should return array!",
			customfcheck : "Custom function should be present in case of custom checking!"
		}
	},
	view : {
	    caption: "Kay?t G?r?nt?le",
	    bClose: "Kapat"
	},
	del : {
	    caption: "Sil",
	    msg: "Se?ilen kay?tlar silinsin mi?",
	    bSubmit: "Sil",
	    bCancel: "?ptal"
	},
	nav : {
		edittext: " ",
	    edittitle: "Se?ili sat?r? d?zenle",
		addtext:" ",
	    addtitle: "Yeni sat?r ekle",
	    deltext: " ",
	    deltitle: "Se?ili sat?r? sil",
	    searchtext: " ",
	    searchtitle: "Kay?tlar? bul",
	    refreshtext: "",
	    refreshtitle: "Tabloyu yenile",
	    alertcap: "Uyar?",
	    alerttext: "L?tfen bir sat?r se?iniz",
		viewtext: "",
		viewtitle: "Se?ilen sat?r? g?r?nt?le"
	},
	col : {
	    caption: "S?tunlar? g?ster/gizle",
	    bSubmit: "G?nder",
	    bCancel: "?ptal"	
	},
	errors : {
		errcap : "Hata",
		nourl : "Bir url yap?land?r?lmam??",
		norecords: "??lem yap?lacak bir kay?t yok",
	    model : "colNames uzunlu?u <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Paz", "Pts", "Sal", "?ar", "Per", "Cum", "Cts",
				"Pazar", "Pazartesi", "Sal?", "?ar?amba", "Per?embe", "Cuma", "Cumartesi"
			],
			monthNames: [
				"Oca", "?ub", "Mar", "Nis", "May", "Haz", "Tem", "A?u", "Eyl", "Eki", "Kas", "Ara",
				"Ocak", "?ubat", "Mart", "Nisan", "May?s", "Haziran", "Temmuz", "A?ustos", "Eyl?l", "Ekim", "Kas?m", "Aral?k"
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

