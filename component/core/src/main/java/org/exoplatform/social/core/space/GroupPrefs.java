/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space;

import java.util.Collection;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.common.utils.GroupNode;
import org.exoplatform.social.common.utils.GroupTree;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 24, 2012  
 */
public class GroupPrefs {
  private static String ON_RESTRICTED_KEY = "SOCIAL_SPACE_ADMIN_ON_RESTRICTED_KEY";
  private static String GROUP_RESTRICTED_KEY = "SOCIAL_SPACE_ADMIN_GROUPS_RESTRICTED_KEY";
  
  private SettingService settingService = null;
  private OrganizationService orgSrv = null;
  
  private static final Log LOG = ExoLogger.getLogger(GroupPrefs.class);
  private static GroupTree treeAllGroups = GroupTree.createInstance();
  private static GroupTree treeRestrictedGroups = GroupTree.createInstance();
  
  private boolean isOnRestricted;
  
  public GroupPrefs(SettingService settingService) {
    this.settingService = settingService;
    this.orgSrv = SpaceUtils.getOrganizationService();
    loadSetting();
  }
  
  private void loadSetting() {
    SettingValue<?>  value = this.settingService.get(Context.GLOBAL, Scope.PORTAL, ON_RESTRICTED_KEY);
    
    //isRestricted value
    if (value != null) {
      Boolean boolValue = (Boolean) value.getValue();
      isOnRestricted = boolValue.booleanValue();
    }
    
    //restricted groups
    value = this.settingService.get(Context.GLOBAL, Scope.PORTAL, GROUP_RESTRICTED_KEY);
    
    if (value != null) {
      //
      String[] groupIds = value.getValue().toString().split(",");
      try {
        for(String id : groupIds) {
          Group currentGroup = orgSrv.getGroupHandler().findGroupById(id);
          Group parentGroup = orgSrv.getGroupHandler().findGroupById(currentGroup.getParentId());
          Collection children = orgSrv.getGroupHandler().findGroups(currentGroup);
          
          treeRestrictedGroups.addSibilings(buildGroupNode(parentGroup, currentGroup, children));
        }
      } catch (Exception e) {
        // 
        LOG.warn("Cannot get all groups.");
      }
    }
    
    //all groups
    Collection<?> allGroups = null;
    try {
      allGroups = orgSrv.getGroupHandler().findGroups(null);
      
      for (Object group : allGroups) {
        if (group instanceof Group) {
          Group grp = (Group) group;
          Group parentGroup = grp.getParentId() != null ? orgSrv.getGroupHandler().findGroupById(grp.getParentId()) : null;
          Collection children = orgSrv.getGroupHandler().findGroups(grp);
          treeAllGroups.addSibilings(buildGroupNode(parentGroup, grp, children));
        }
      }
      
    } catch (Exception e) {
      // 
      LOG.warn("Cannot get all groups.");
    }
  }
  
  private GroupNode buildGroupNode(Group parentGroup, Group currentGroup, Collection children) {
    GroupNode currentNode = null;
    try {
      if (currentGroup != null) {
        currentNode = GroupNode.createInstance(currentGroup.getId(), currentGroup.getLabel());
        
        //
        if (parentGroup != null)
          currentNode.setParent(GroupNode.createInstance(parentGroup.getId(), parentGroup.getLabel()));
        
        //
        if (children != null) {
          for (Object g : children) {
            if (g instanceof Group) {
              Group grp = (Group) g;
              Collection subChildren = orgSrv.getGroupHandler().findGroups(grp);
              GroupNode child = GroupNode.createInstance(grp.getId(), grp.getLabel());
              child.setHasChildren(subChildren.size() > 0);
              currentNode.addChildren(child);
            }
          }
        }
        
      }
    } catch (Exception e) {
      // 
      LOG.warn("Cannot build group node.");
    }
    return currentNode;
  }
  
  public GroupTree getGroups() {
    return treeAllGroups;  
  }
  
  public void setGroups(GroupTree tree) {
    this.treeAllGroups = tree;
  }
  
  public static GroupTree getRestrictedGroups() {
    return treeRestrictedGroups;  
  }
  
  public void addRestrictedGroups(String groupId) {
    try {
      Group group = orgSrv.getGroupHandler().findGroupById(groupId);
      treeRestrictedGroups.addSibilings(GroupNode.createInstance(group.getId(), group.getLabel()));
      
      //
      this.settingService.set(Context.GLOBAL, Scope.PORTAL, GROUP_RESTRICTED_KEY, SettingValue.create(treeRestrictedGroups.toValue()));
    } catch (Exception e) {
      // 
      LOG.warn("Cannot get group for '" + groupId + "'");
    }
  }
  
  public void removeRestrictedGroups(String groupId) {
    treeRestrictedGroups.remove(groupId);
  }
  
  public boolean isOnRestricted() {
    return isOnRestricted;
  }

  public void setOnRestricted(boolean isOnRestricted) {
    this.isOnRestricted = isOnRestricted;
    this.settingService.set(Context.GLOBAL, Scope.PORTAL, ON_RESTRICTED_KEY, SettingValue.create(isOnRestricted));
  }
  
  public boolean hasParent(String groupKey) {
    try {
      if (groupKey == null) {
        GroupNode curGroup = getGroups().getSibilings().get(0);
        return curGroup.hasParent();
      } else {
        Group selectedGroup = orgSrv.getGroupHandler().findGroupById(groupKey);
        return selectedGroup.getParentId() != null;
      }  
    } catch (Exception e) {
      return false;
    }
  }
  
  public void downLevel(String groupKey, GroupTree tree) {
    
    try {
      Group selectedGroup = orgSrv.getGroupHandler().findGroupById(groupKey);
      if (selectedGroup != null) {
        Group parentGroup = orgSrv.getGroupHandler().findGroupById(selectedGroup.getParentId());
        Collection parentChildren = orgSrv.getGroupHandler().findGroups(parentGroup);

        if (parentChildren.size() > 0) {
          tree.clear();
        }
        
        for (Object group : parentChildren) {
          if (group instanceof Group) {
            Group grp = (Group) group;
            Group newParentGroup = grp.getParentId() != null ? orgSrv.getGroupHandler().findGroupById(grp.getParentId()) : null;
            Collection newchildren = orgSrv.getGroupHandler().findGroups(grp);
            tree.addSibilings(buildGroupNode(newParentGroup, grp, newchildren));
          }
        }
        
      }
    } catch (Exception e) {
      // 
      LOG.warn("Cannot down level of node.");
    }
  }

  
  public GroupTree upLevel(GroupTree tree) {
    GroupNode groupNode = tree.getSibilings().size() > 0 ? tree.getSibilings().get(0) : null;
    GroupTree newTree = GroupTree.createInstance();
    try {

      if (groupNode != null) {
        GroupNode myParent = groupNode.getParent();
        if (myParent != null) {

          // get parent
          Group parentGroup = orgSrv.getGroupHandler().findGroupById(myParent.getId());
          Collection<?> children = null;
          if (parentGroup.getParentId() != null) {
            Group ancestorGroup = orgSrv.getGroupHandler().findGroupById(parentGroup.getParentId());
            children = orgSrv.getGroupHandler().findGroups(ancestorGroup);
          } else {
            children = orgSrv.getGroupHandler().findGroups(null);
          }

          // get children of ancestor, then push into tree
          for (Object group : children) {
            if (group instanceof Group) {
              Group grp = (Group) group;
              Group newParentGroup = grp.getParentId() != null ? orgSrv.getGroupHandler().findGroupById(grp.getParentId()) : null;
              Collection newchildren = orgSrv.getGroupHandler().findGroups(grp);
              newTree.addSibilings(buildGroupNode(newParentGroup, grp, newchildren));
            }
          }

        }

      }
      return newTree;
    } catch (Exception e) {
      //
      LOG.warn("Cannot down level of node.");
      return newTree;
    }
  }
  
}