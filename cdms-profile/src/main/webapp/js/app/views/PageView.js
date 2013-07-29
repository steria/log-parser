app.PageView = Backbone.View.extend({

	initialize : function (options) {
		this.$el.append(app.models.navbar.getCurrentViewTemplate());
		return this;
	},

	remove: function() {
		this.$el.empty();
	},

	render: function() {

		var tconf = app.models.navbar.getCurrentTimeConfig();
        var intervalConf = tconf.pt.join('/');
        var interval = new moment().interval(intervalConf);
        var from = interval.start().toISOString();
        var to = interval.end().toISOString();
        var that = this;
        app.collections.measurementCollection = new app.MeasurementCollection();
        console.log('fetch measurementCollection')
        app.collections.measurementCollection.fetch({
            data: $.param({from:from, to:to, buckets:app.nrOfBuckets}),
            success:function(){
            	console.log('measurementCollection', app.collections.measurementCollection)
				_.each(that.$el.find('.graph'), function(element) {
					var graph = new app.GraphView({
						el: $(element)
					});
					graph.render();
				});
        	}
    	});
        app.collections.percentileCollection = new app.PercentileCollection();
        console.log('fetch percentileCollection');
        app.collections.percentileCollection.fetch({
        	data: $.param({from:from, to:to, percentages:app.percentiles}),
        	success:function () {
        		console.log('percentileCollection', app.collections.percentileCollection);
        		var table = new app.TableView({
        			el: that.$el.find('.table').first()
        		});
        		table.render();
        	}
        });
	}


})