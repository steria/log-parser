app.Navbar = Backbone.Model.extend({

	initialize : function(options) {
		this.set({'selectedView' : ''})
	},

	getCurrentViewTemplate : function(){
		var view = this.get('selectedView');

		switch (view) {
			case 'last3h':
				return templates.defaultPage();
			case 'last24h':
				return templates.defaultPage();
			case 'last72h':
				return templates.defaultPage();
			case 'last1w':
				return templates.defaultPage();
			case 'last2w':
				return templates.defaultPage();
			case 'overview':
				return templates.overviewPage();
		}
	},

	getCurrentTimeConfig : function () {
		var view = this.get('selectedView');

		switch (view) {
			case 'last3h':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['PT3H/']
        		}
			case 'last24h':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['PT24H/']
        		}
			case 'last72h':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['PT72H/']
        		}
			case 'last1w':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['P1W/']
        		}
			case 'last2w':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['P2W/']
        		}
        	case 'overview':
				return {
            		realtime: false,
            		pollInterval: 1000,
            		pt: ['PT3H/']
        		}
		}
	}
})