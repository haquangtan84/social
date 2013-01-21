var UIRestrictSpaceCreator = {
  init : function() {
    /*
     * var restrictSpaceCreator = document.getElementById("UIRestrictSpaceCreator"); 
     * var restrictStatusEl = $(restrictSpaceCreator).find('#RestrictStatus');
     * var uiActionEl = $(restrictSpaceCreator).find('.UIAction');
     * restrictStatusEl.on('click', function(evt) { uiActionEl.toggle(); });
     */
  },
  addGroup : function(selectedElId, groupName) {
    var addedContent = $('<li/>', {
      'id' : selectedElId
    }).on('click', function() {
      $('#RestrictGroups').jzLoad("Controller.removeGroup()", {
        groupId : selectedElId
      }, function() {
      });
    });

    if ($("#RestrictGroups").children(':last-child').length == 0) {
      $("#RestrictGroups").append(addedContent);
    } else {
      $("#RestrictGroups").children(':last-child').after(addedContent);
    }

    $('#RestrictGroups').children(':last-child').jzLoad('Controller.doSelectGroup()', {
      groupId : selectedElId,
      groupName : groupName
    }, function() {
    });

  },
  initTree : function() {

    //
    $('#UpLevelBtn').off('click').css('cursor', 'pointer').on('click', function() {
      var uiDialog = $('.UIDialog');

      $('#PopupAddGroup').empty().append($('<div class="UpdateContent"></div>'));
      $('#PopupAddGroup').find('div.UpdateContent').jzLoad('Controller.backToParentGroup()', {}, function() {
        uiDialog.find('.UpdateContent').remove();
        uiDialog.append($('#PopupAddGroup').find('div.UpdateContent'));
        UIRestrictSpaceCreator.initTree();
        uiDialog.dialog({
          width : 'auto'
        });
      });

    });

    selectedGroup = null;
    $('#msg').html('');
    $('#RightSelector>ul').html('');
    $('#navText').text('...');
    $('#UISelectGroup').jstree({
      "plugins" : [ "themes", "html_data", "ui", "crrm" ],
      "core" : {
        "initially_open" : [ "phtml_1" ]
      }
    }).bind("select_node.jstree", function(event, data) {

      $('#RightSelector>ul').html('');
      selectedGroup = data.rslt.obj.attr("id");
      selectedGroupId = data.rslt.obj.attr("val");
      var selectedEl = $(this).find('#' + selectedGroup);
      var subGroups = selectedEl.find('li');

      // nav
      $('#GroupNavigation').html("");

      var subGroupNum = subGroups.length;
      var addedEl = null;
      $(selectedEl).off('dblclick');
      if (subGroupNum === 0) { // has no children
        var parent = $(selectedEl).closest('ul').closest('li');

        if (parent.length === 1) {
          $('#navText').text($(parent).attr('title') + '>' + $(selectedEl).attr('title'));
        } else {
          $('#navText').text($(selectedEl).attr('title'));
        }

        addedEl = $('<li/>', {
          'id' : $(selectedEl).attr('id'),
          'class' : 'displayblock'
        }).on('click', function() {
          $('.UIDialog').dialog("close");
          UIRestrictSpaceCreator.addGroup($(selectedEl).attr('val'), $(selectedEl).attr('title'));
        }).append($('<a href="javascript:void(0)"></a>').css({
          'cursor' : 'pointer',
          'color' : 'blue',
          'padding-left' : '10px',
          'white-space' : 'nowrap',
          'margin-left' : '10px'
        }).html('Select this group...'));

        $('#RightSelector>ul').append(addedEl);

        //
        if ($(selectedEl).find('a').attr('id') == "SubChild") {
          $('#PopupAddGroup').empty().append($('<div class="UpdateContent"></div>'));
          $('#PopupAddGroup').find('div.UpdateContent').jzLoad("Controller.doAccessChildGroup()", {
            groupId : selectedGroupId
          }, function() {
            $('.UIDialog').find('div.UpdateContent').remove();
            $('.UIDialog').append($('#PopupAddGroup').find('div.UpdateContent'));
            UIRestrictSpaceCreator.initTree();
            $('.UIDialog').dialog({
              width : 'auto'
            });
          });
        }
      } else { 
        
        // has children
        $('#navText').text($(selectedEl).attr('title'));

        addedEl = $('<li/>', {
          'id' : $(selectedEl).attr('id'),
          'class' : 'icon-chevron-right displayblock'
        }).on('click', function() {
          $('.UIDialog').dialog('close');
          UIRestrictSpaceCreator.addGroup($(selectedEl).attr('val'), $(selectedEl).attr('title'));
        }).append($('<a href="javascript:void(0)"></a>').css({
          'cursor' : 'pointer',
          'color' : 'blue',
          'padding-left' : '10px',
          'white-space' : 'nowrap',
          'margin-left' : '10px'
        }).html('*(any)'));
        $('#RightSelector>ul').append(addedEl);

        $.each(subGroups, function(idx, el) {
          $(el).unbind('click');

          addedEl = $('<li/>', {
            'id' : $(el).attr('id'),
            'class' : 'icon-chevron-right displayblock'
          }).on('click', function() {
            $('.UIDialog').dialog("close");
            UIRestrictSpaceCreator.addGroup($(el).attr('val'), $(el).attr('title'));
          }).append($('<a href="javascript:void(0)">' + $(el).attr('title') + '</a>').css({
            'cursor' : 'pointer',
            'color' : 'blue',
            'padding-left' : '10px',
            'white-space' : 'nowrap',
            'margin-left' : '10px'
          }));
          $('#RightSelector>ul').append(addedEl);
        });
      }

      $('#msg').html('');
    });
  },
  iphoneSwitch : function(start_state, strFunc, options) {

    var state = start_state == 'on' ? start_state : 'off';

    // define default settings
    var settings = {
      mouse_over : 'pointer',
      mouse_out : 'default',
      switch_on_container_path : '/juzu-social/img/iphone_switch_container_on.png',
      switch_off_container_path : '/juzu-social/img/iphone_switch_container_off.png',
      switch_path : '/juzu-social/img/iphone_switch.png',
      switch_height : 27,
      switch_width : 94
    };

    if (options) {
      $.extend(settings, options);
    }

    // init for remove button
    $("#RestrictGroups").delegate('li', 'click', function() {
      var $this = $(this);
      $('#RestrictGroups').jzLoad("Controller.removeGroup()", {
        groupId : $this.attr('id')
      }, function() {
      });
    });

    // Select group
    var selectedGroup = null;
    $('#PopupAddGroup').empty().append($('<div class="UIDialog"><div class="UpdateContent"></div></div>'));
    $('#PopupAddGroup').find('div.UpdateContent').jzLoad('Controller.doAddGroup()', {}, function() {});

    $('#AddGroupButton').on('click', function() {
      UIRestrictSpaceCreator.initTree();
      $(".UIDialog").dialog({
        width : 'auto'
      });
    });

    // create the switch
    return $('#RestrictButton').each(
        function() {

          var container;
          var image;

          // make the container
          container = $('<div class="iphone_switch_container" style="position: relative; overflow: hidden"></div>').height(settings.switch_height).width(settings.switch_width);

          // make the switch image based on starting state
          image = $('<img class="iphone_switch" style="height:' + settings.switch_height + 'px; width:' + settings.switch_width + 'px; background-image:url('
              + settings.switch_path + '); background-repeat:none; background-position:' + (state == 'on' ? 0 : -53) + 'px" src="'
              + (state == 'on' ? settings.switch_on_container_path : settings.switch_off_container_path) + '" /></div>');

          // insert into placeholder
          $(this).html($(container).html($(image)));

          $(this).mouseover(function() {
            $(this).css("cursor", settings.mouse_over);
          });

          $(this).mouseout(function() {
            $(this).css("background", settings.mouse_out);
          });

          // click handling
          $(this).click(function() {
            $('.jz').jzAjax('Controller.switchMode()');
            if (state == 'on') {
              $(this).find('.iphone_switch').animate({
                backgroundPosition : -53
              }, "slow", function() {
                $(this).attr('src', settings.switch_off_container_path);
                $('#GroupList').removeClass('UIGroupListVisible').addClass('UIGroupListInvisible');
                $('#RestrictionMsg').html('Restrict who can create new spaces');
                eval(strFunc);
              });
              state = 'off';
            } else {
              $(this).find('.iphone_switch').animate({
                backgroundPosition : 0
              }, "slow", function() {
                $('#GroupList').removeClass('UIGroupListInvisible').addClass('UIGroupListVisible');
                $('#RestrictionMsg').html('Restrict who can create new spaces');
                eval(strFunc);
              });
              $(this).find('.iphone_switch').attr('src', settings.switch_on_container_path);
              state = 'on';
            }
          });

        });
  }
};