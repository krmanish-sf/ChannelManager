<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="salesmachine.oim.api.OimConstants"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
<div class="main-content">
      <div class="breadcrumbs" id="breadcrumbs"> 
   
        <ul class="breadcrumb">
          <li><a href="index.jsp"><i
							class="icon-home home-icon"></i>Home</a></li>
          <li class="active">Shipping Mappings</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1>Shipping Mappings<small> <i
							class="icon-double-angle-right"></i> overview </small> </h1>
        </div>
        <div class="row">
        <div class="col-xs-12">
              <div class="row">
              <div class="col-sm-12 inline">
              <div class="widget-container-span">
									<div class="widget-box">
										<div class="widget-header header-color-green">
											<h5>Tip:</h5>
											<div class="widget-toolbar">
												<a href="#" data-action="close"> <i
													class="icon-remove white"></i>
												</a>
											</div>
										</div>
										<div class="widget-body alert-success">
											<div class="widget-main">
												<p>You can manage your <strong>in-bound</strong> shipping mapping from channel to <strong>out-bound</strong> values for suppliers. If you face any problem, get in touch with our <a
														href="mailto:support@inventorysource.com" target="_blank"><strong>support</strong></a> team to help you out with setup.</p>
											</div>
										</div>
									</div>
								</div> 
                      <h4 class="lighter pull-left">
									<i class="icon-exchange orange"></i> My Channel Supplier Shipping Mapping</h4>					
              </div>
			</div>
            <div class="space-2"></div>
            <div class="row">
              <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableShippingMappings"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th><i
													class="icon-user icon-2x blue visible-xs"></i>
													<span class="hidden-xs">Channel Name</span></th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
            </div>
       </div>
         </div>
         </div>
      </div> 
    </div>
  </div>
  <div class="modal fade" id="editshippingmethods" tabindex="-1"
			role="dialog" aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
							data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Shipping Methods</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                      <div class="col-sm-12">
							<table id="tableShippingMap"
										class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-barcode icon-2x blue visible-xs"></i><span
													class="hidden-xs visible-sm"></span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
													class="hidden-xs">Shipping Method</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
													class="hidden-xs">Supplier Shipping Carrier</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
													class="hidden-xs">Supplier Shipping Method</span></th>
									</tr>
								</thead>
								<tbody>
								</tbody>
							</table>
						</div>
                        
                      </div>
                    </div>
                  </div>
                </div>
			</div>
            </div>
</jsp:attribute>
	<jsp:attribute name="pagejs">
	<script type="text/javascript">
		var tableShippingMappings;
		jQuery(function($) {
			tableShippingMappings = $('#tableShippingMappings').DataTable(
					{
						"bPaginate" : false,
						"bLengthChange" : false,
						"sDom" : "t",
						"sAjaxSource" : 'aggregators/channels',
						"fnServerData" : function(sSource, aoData, fnCallback,
								oSettings) {
							oSettings.jqXHR = $(this).CRUD({
								type : "GET",
								url : sSource,
								data : aoData,
								success : function(data) {
									fnCallback(data);
									abc();
								}
							});
						},
						"sAjaxDataProp" : '',
						"aoColumns" : [ {
							"mData" : function(d) {
								return d.channelName + " - "
										+ d.oimSupportedChannels.channelName;
							}
						} ]
					});

			function abc() {
				$('#tableShippingMappings tbody tr').each(function(i, e) {
					var tr = $(this);
					var row = tableShippingMappings.row(tr);
					row.child(format(row.data())).show();
				});
			}
			function format(d) {
				var trs = '';
				for (var i = 0; i < d.oimChannelSupplierMaps.length; i++) {
					trs += '<div class="col-sm-12 col-lg-6"><table id="shipMap'
							+ d.oimChannelSupplierMaps[i].mapId
							+ '" cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;width:100%;"><thead>'
							+ '<tr>'
							+ '<td>'
							+ d.oimChannelSupplierMaps[i].oimSuppliers.supplierName
							+ '</td>'
							+ '<td>'
							+ 'Shipping Method'
							+ '</td><td></td>'
							+ '</tr></thead><tbody></tbody></table></div>'
							+ '<script type="text/javascript">jQuery(function($){$.CM.viewSupplierShippingMap('
							+ d.oimChannelSupplierMaps[i].oimSuppliers.supplierId
							+ ',"#shipMap'
							+ d.oimChannelSupplierMaps[i].mapId
							+ '");});<'+'/script>';
				}
				return trs;

			}
		});
	</script>
	</jsp:attribute>
</t:basepage>