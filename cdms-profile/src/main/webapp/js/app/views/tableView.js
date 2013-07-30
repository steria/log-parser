app.TableView = Backbone.View.extend({
    initialize: function() {
        return this;
    },
    render: function() {
        this.$el.find('thead').first().append(this.createTableHead());
        this.$el.find('tbody').first().append(this.createTableBody());
        this.updateColors();
    },
    createTableHead: function() {
        head = '<tr>' +
                '	<th>Name/Percentiles</th>';
        _.each(app.percentiles, function(p) {
            head += '	<th>' + p + '</th>';
        });
        head += '</tr>';
        return head;
    },
    createTableBody: function() {
        var that = this;
        body = '';
        app.collections.procedures.each(function(pro) {
            body += '<tr data-tableof="' + pro.id + '">';
            if (_.flatten(app.graphOfs).indexOf(pro.id) !== -1) {
                body += '<td>' + that._getProcedureName(pro) + ' <i class="icon-eye-open"></i></td>';
            }
            else {
                body += '<td>' + that._getProcedureName(pro) + '</td>';
            }
            _.each(app.percentiles, function(per) {
                body += '<td data-percentile="' + per + '">' + app.collections.percentileCollection.get(pro.id)['attributes']['percentiles'][per] + '</td>';
            });
            body += '</tr>';
        })
        return body;
    },
    updateColors: function() {
        _.each(this.$el.find("tbody>tr"), function(tr) {
            tr = $(tr);
            var id = tr.data('tableof');
            _.each(tr.find("td"), function(td) {
                td = $(td);
                var per = td.data('percentile');
                if (typeof per !== 'undefined') {
                    var limit = 2000;
                    if (typeof app.percentileLimits[id] !== 'undefined') {
                        if (typeof app.percentileLimits[id][per] !== 'undefined') {
                            var actual = moment.duration(td[0].innerHTML);
                            limit = moment.duration(app.percentileLimits[id][per]);
                        }
                    }
                    
                    if (actual._milliseconds > limit._milliseconds) {
                        td.removeClass('success');
                        td.addClass('error');
                        tr.find("td").first().removeClass('success');
                        tr.find("td").first().addClass('error');
                    }else {
                        td.addClass('success');
                    }
                }
            })
        })
    },
    _getProcedureName: function(procedure) {
        function isValid(str) {
            var ans = typeof str !== 'undefined' && str !== null;
            return ans;
        }
        if (isValid(procedure.get('name'))) {
            return procedure.get('name');
        } else {
            var out = procedure.get('className');
            if (isValid(procedure.get('method'))) {
                out += "." + procedure.get('method');
            }
            return out;
        }
    }
})