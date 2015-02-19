(function($) {

	AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget
			.extend({

				start : 0,
				beforeRequest : function() {
					$(this.target).html(
							$('<img>').attr('src', 'static/img/loading.gif'));
				},

				facetLinks : function(facet_field, facet_values) {
					var links = [];
					if (facet_values) {
						links.push($('<a href="#"></a>').text(facet_values)
								.click(
										this.facetHandler(facet_field,
												facet_values)));
						/*
						 * for (var i = 0, l = facet_values.length; i < l; i++) {
						 * links.push( $('<a href="#"></a>')
						 * .text(facet_values[i])
						 * .click(this.facetHandler(facet_field,
						 * facet_values[i])) ); debugger; }
						 */
					}
					return links;
				},

				facetHandler : function(facet_field, facet_value) {
					var self = this;
					// return function() {
					self.manager.store.remove('fq');
					if (facet_value) {
						self.manager.store.addByValue('fq', facet_field + ':'
								+ AjaxSolr.Parameter.escapeValue(facet_value));
					}
					self.doRequest();
					return false;
					// };
				},

				afterRequest : function() {
					$(this.target).empty();
					for (var i = 0, l = this.manager.response.grouped.dropshipper_id.groups.length; i < l; i++) {
						var doc = this.manager.response.grouped.dropshipper_id.groups[i];
						$(this.target).append(this.template(doc));

						var items = [];
						items = items.concat(this.facetLinks('cat1', doc.cat1));

						var $links = $('#links_' + doc.id);
						$links.empty();
						for (var j = 0, m = items.length; j < m; j++) {
							$links.append($('<li></li>').append(items[j]));
						}
					}
				},

				template : function(doc) {
					var snippet = '';
					if (doc.doclist.docs.length > 0) {
						snippet = '<div><h2>'
								+ doc.doclist.docs[0].dropshipper_name
								+ '</h2>';
						snippet += '<img src="'
								+ doc.doclist.docs[0].dropshipper_logo + '"/>';
						snippet += '<p>Active products:' + doc.doclist.numFound
								+ '</p>';
						snippet += '<p>Supplier code:'
								+ doc.doclist.docs[0].dropshipper_prefix
								+ '</p>';
						snippet += '<p>Inventory Summary:'
								+ doc.doclist.docs[0].inventory_summary
								+ '</p>';
						snippet += '<p id="links_'
								+ doc.doclist.docs[0].dropshipper_id
								+ '" class="links"></p>';
					} else {
						snippet += 'No record';
					}
					var output = '';
					output += '<p>' + snippet + '</p></div>';
					return output;
				},
				init : function() {
					/*
					 * $(document).on( 'click', 'a.more', function() { var $this =
					 * $(this), span = $this.parent() .find('span'); if
					 * (span.is(':visible')) { span.hide(); $this.text('more'); }
					 * else { span.show(); $this.text('less'); } return false;
					 * });
					 */

					$('#search').on(
							'change',
							this,
							function(e) {
								var searchField = $('#searchField').val();
								if (searchField == 0) {
									alert("Please select a field to search");
									return;
								}
								e.data.facetHandler(searchField, $(this)
										.val() ? $(this).val() : '');
								/*
								 * e.data.facetHandler('name', $(this) .val() ?
								 * $(this).val() : '');
								 * e.data.facetHandler('sku', $(this) .val() ?
								 * $(this).val() : '');
								 */
							});
				}
			});
})(jQuery);
