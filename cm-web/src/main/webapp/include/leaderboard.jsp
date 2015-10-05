<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="widget-box transparent" id="recent-box">
	<div class="widget-header">
		<h4 class="lighter smaller">
			<i class="icon-rss orange"></i>Sales Leaders
		</h4>
		<div class="widget-toolbar no-border">
			<ul class="nav nav-tabs" id="recent-tab">
				<li class="active"><a data-toggle="tab" href="#product-tab">Products</a></li>
				<li><a data-toggle="tab" href="#task-tab">Channels</a></li>
				<li><a data-toggle="tab" href="#Supplier-tab">Suppliers</a></li>
			</ul>
		</div>
	</div>
	<div class="widget-body">
		<div class="widget-main padding-0">
			<div class="tab-content padding-0 overflow-visible">
				<div id="product-tab" class="tab-pane active">
					<table id="tasks2" class="table hover row-border">
						<thead>
							<tr>
								<th>SKU</th>
								<th>Total Sale</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
					<!-- 					<ul id="tasks2" class="item-list"></ul> -->
				</div>
				<div id="task-tab" class="tab-pane">
					<ul id="tasks" class="item-list"></ul>
				</div>
				<div id="Supplier-tab" class="tab-pane">
					<div class="clearfix">
						<ul id="tasks1" class="item-list"></ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
