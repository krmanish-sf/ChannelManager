//http://datatables.net/plug-ins/pagination#bootstrap
//This file has been modified to support functions defined in story ISD-138. -Amit Yadav
$.extend( true, $.fn.dataTable.defaults, {
	"sDom": "<'row'<'col-sm-6'l><'col-sm-6'f>r>t<'row'<'col-sm-4'i><'col-sm-4 paging-toolbar'><'col-sm-4'p>>",
	"sPaginationType": "bootstrap",
	"oLanguage": {
		"sLengthMenu": "Display _MENU_ records"
	}
} );


/* API method to get paging information */
$.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
{
    return {
        "iStart":         oSettings._iDisplayStart,
        "iEnd":           oSettings.fnDisplayEnd(),
        "iLength":        oSettings._iDisplayLength,
        "iTotal":         oSettings.fnRecordsTotal(),
        "iFilteredTotal": oSettings.fnRecordsDisplay(),
        "iPage":          Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
        "iTotalPages":    Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
    };
}
 
/* Bootstrap style pagination control */
$.extend( $.fn.dataTableExt.oPagination, {
    "bootstrap": {
        "fnInit": function( oSettings, nPaging, fnDraw ) {
            var oLang = oSettings.oLanguage.oPaginate;
            var fnClickHandler = function ( e ) {
                e.preventDefault();
                var iListLength = 5;
                var oPaging = oSettings.oInstance.fnPagingInfo();
                var showPage;
                switch(e.data.action){
                case "next":
                	var offset = oPaging.iPage % iListLength;
                	showPage = oPaging.iPage-offset+iListLength;
                	showPage = showPage>oPaging.iTotalPages-1?oPaging.iTotalPages-1:showPage;
                	break;
                case "previous":
                	var offset = oPaging.iPage % iListLength;
                	showPage = oPaging.iPage-offset-iListLength;
                	showPage = showPage<0?0:showPage;
                	break;
                case "goto":
                	showPage = parseInt($(this).siblings('input').val(),0);
                	$(this).siblings('input').val('');
                	showPage--;
                	showPage = showPage<0?0:showPage;
                	showPage = showPage>oPaging.iTotalPages-1?oPaging.iTotalPages-1:showPage;
                	break;
                }
                oSettings._iDisplayStart = showPage * oPaging.iLength;
                fnDraw( oSettings );
            };
           
            $(nPaging).append(
                '<ul class="pagination">'+
                    '<li class="prev disabled"><a href="#"><i class="icon-double-angle-left"></i></a></li>'+
                    '<li class="next disabled"><a href="#"><i class="icon-double-angle-right"></i></a></li>'+
                '</ul>'
            );
            var els = $('a', nPaging);
            $(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
            $(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
            var nGoto = $(oSettings.nTableWrapper).find('.paging-toolbar');
            var oPaging = oSettings.oInstance.fnPagingInfo();
            nGoto.html('Jump to page <input type="text" class="width-20"> of <span>'+oPaging.iTotalPages +'</span> pages. <button>Go</button>');
            $(nGoto).find('button').bind( 'click.DT', { action: "goto" }, fnClickHandler );
            $(nGoto).find('input').on('keypress.DT',function(e){
            	if(e.charCode!=0 &&( e.charCode<48 || e.charCode>57)){
            		e.preventDefault();
            	}
            });
        },
 
        "fnUpdate": function ( oSettings, fnDraw ) {
            var iListLength = 5;
            var oPaging = oSettings.oInstance.fnPagingInfo();
            var nGoto = $(oSettings.nTableWrapper).find('.paging-toolbar');
            $(nGoto).find('span').html(oPaging.iTotalPages);
            $(nGoto).find('input').prop('placeholder',oPaging.iPage+1)
            var an = oSettings.aanFeatures.p;
            var i, j, sClass, iStart, iEnd, iHalf=Math.floor(iListLength/2);
 
            if ( oPaging.iTotalPages < iListLength) {
                iStart = 1;
                iEnd = oPaging.iTotalPages;
            }
            else if ( oPaging.iPage <= iHalf ) {
                iStart = 1;
                iEnd = iListLength;
            } else if ( oPaging.iPage >= (oPaging.iTotalPages-iHalf) ) {
                iStart = oPaging.iTotalPages - iListLength + 1;
                iEnd = oPaging.iTotalPages;
            } else {
                iStart = oPaging.iPage - iHalf + 1;
                iEnd = iStart + iListLength - 1;
            }
 
            for ( i=0, iLen=an.length ; i<iLen ; i++ ) {
                // Remove the middle elements
                $('li:gt(0)', an[i]).filter(':not(:last)').remove();
 
                // Add the new list items and their event handlers
                for ( j=iStart ; j<=iEnd ; j++ ) {
                    sClass = (j==oPaging.iPage+1) ? 'class="active"' : '';
                    $('<li '+sClass+'><a href="#">'+j+'</a></li>')
                        .insertBefore( $('li:last', an[i])[0] )
                        .bind('click', function (e) {
                            e.preventDefault();
                            oSettings._iDisplayStart = (parseInt($('a', this).text(),10)-1) * oPaging.iLength;
                            fnDraw( oSettings );
                        } );
                }
 
                // Add / remove disabled classes from the static elements
                if ( oPaging.iPage === 0 ) {
                    $('li:first', an[i]).addClass('disabled');
                } else {
                    $('li:first', an[i]).removeClass('disabled');
                }
 
                if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
                    $('li:last', an[i]).addClass('disabled');
                } else {
                    $('li:last', an[i]).removeClass('disabled');
                }
            }
        }
    }
} );
