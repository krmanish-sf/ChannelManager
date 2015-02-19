var Manager;

(function($) {

	$(function() {
		Manager = new AjaxSolr.Manager({
			// solrUrl: 'http://localhost:8983/solr/'
			solrUrl : 'http://199.47.222.123:8080/solr/'
		});
		Manager.addWidget(new AjaxSolr.ResultWidget({
			id : 'result',
			target : '#docs'
		}));
		Manager.addWidget(new AjaxSolr.PagerWidget({
			id : 'pager',
			target : '#pager',
			prevLabel : '&lt;',
			nextLabel : '&gt;',
			innerWindow : 1,
			renderHeader : function(perPage, offset, total) {
				$('#pager-header').html(
						$('<span></span>').text(
								'displaying ' + Math.min(total, offset + 1)
										+ ' to '
										+ Math.min(total, offset + perPage)
										+ ' of ' + total));
			}
		}));
		Manager.init();
		Manager.store.addByValue('q', '*:*');
		var params = {
			facet : false,
			'facet.field' : 'dropshipper_id',
			'indent' : true,
			'facet.limit' : 50,
			'group.field' : ['dropshipper_id','id','sku','cat1'],
			'group' : true,
			'group.limit' : 1,
			'group.facet' : true,
			'group.ngroups' : true,
			'facet.mincount' : 1,
			// 'f.dropshipper_id.facet.limit' : 5,
			'fl' : [ 'dropshipper_name', 'dropshipper_id',
					'dropshipper_prefix', 'dropshipper_fee', 'dropshipper_url',
					'inventory_summary', 'dropshipper_logo',
					'supplier_product_image' ]
		// ,'json.nl' : 'map'
		};
		for ( var name in params) {
			Manager.store.addByValue(name, params[name]);
		}
		Manager.doRequest();
	});

})(jQuery);
