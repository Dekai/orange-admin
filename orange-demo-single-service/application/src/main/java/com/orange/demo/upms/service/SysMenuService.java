package com.orange.demo.upms.service;

import com.alibaba.fastjson.JSONObject;
import com.orange.demo.common.core.base.service.BaseService;
import com.orange.demo.common.sequence.wrapper.IdGeneratorWrapper;
import com.orange.demo.common.core.base.dao.BaseDaoMapper;
import com.orange.demo.common.core.constant.GlobalDeletedFlag;
import com.orange.demo.common.core.object.CallResult;
import com.orange.demo.upms.dao.SysMenuMapper;
import com.orange.demo.upms.dao.SysMenuPermCodeMapper;
import com.orange.demo.upms.dao.SysRoleMenuMapper;
import com.orange.demo.upms.model.SysMenu;
import com.orange.demo.upms.model.SysMenuPermCode;
import com.orange.demo.upms.model.SysRoleMenu;
import com.orange.demo.upms.model.constant.SysMenuType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单数据服务类。
 *
 * @author Jerry
 * @date 2020-09-24
 */
@Service
public class SysMenuService extends BaseService<SysMenu, Long> {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysMenuPermCodeMapper sysMenuPermCodeMapper;
    @Autowired
    private SysPermCodeService sysPermCodeService;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回主对象的Mapper对象。
     *
     * @return 主对象的Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysMenu> mapper() {
        return sysMenuMapper;
    }

    /**
     * 保存新增的菜单对象。
     *
     * @param sysMenu       新增的菜单对象。
     * @param permCodeIdSet 权限字Id列表。
     * @return 新增后的菜单对象。
     */
    @Transactional(rollbackFor = Exception.class)
    public SysMenu saveNew(SysMenu sysMenu, Set<Long> permCodeIdSet) {
        sysMenu.setMenuId(idGenerator.nextLongId());
        sysMenu.setCreateTime(new Date());
        sysMenu.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        sysMenuMapper.insert(sysMenu);
        if (permCodeIdSet != null) {
            List<SysMenuPermCode> sysMenuPermCodeList = new LinkedList<>();
            for (Long permCodeId : permCodeIdSet) {
                SysMenuPermCode menuPermCode = new SysMenuPermCode();
                menuPermCode.setMenuId(sysMenu.getMenuId());
                menuPermCode.setPermCodeId(permCodeId);
                sysMenuPermCodeList.add(menuPermCode);
            }
            sysMenuPermCodeMapper.insertList(sysMenuPermCodeList);
        }
        return sysMenu;
    }

    /**
     * 更新菜单对象。
     *
     * @param sysMenu         更新的菜单对象。
     * @param originalSysMenu 原有的菜单对象。
     * @param permCodeIdSet   权限字Id列表。
     * @return 更新成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysMenu sysMenu, SysMenu originalSysMenu, Set<Long> permCodeIdSet) {
        sysMenu.setCreateTime(originalSysMenu.getCreateTime());
        sysMenu.setMenuType(originalSysMenu.getMenuType());
        sysMenu.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        if (sysMenuMapper.updateByPrimaryKey(sysMenu) != 1) {
            return false;
        }
        SysMenuPermCode deletedMenuPermCode = new SysMenuPermCode();
        deletedMenuPermCode.setMenuId(sysMenu.getMenuId());
        sysMenuPermCodeMapper.delete(deletedMenuPermCode);
        if (permCodeIdSet != null) {
            List<SysMenuPermCode> sysMenuPermCodeList = new LinkedList<>();
            for (Long permCodeId : permCodeIdSet) {
                SysMenuPermCode menuPermCode = new SysMenuPermCode();
                menuPermCode.setMenuId(sysMenu.getMenuId());
                menuPermCode.setPermCodeId(permCodeId);
                sysMenuPermCodeList.add(menuPermCode);
            }
            sysMenuPermCodeMapper.insertList(sysMenuPermCodeList);
        }
        return true;
    }

    /**
     * 删除指定的菜单。
     *
     * @param menuId 菜单主键Id。
     * @return 删除成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long menuId) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(menuId);
        menu.setDeletedFlag(GlobalDeletedFlag.DELETED);
        if (sysMenuMapper.updateByPrimaryKeySelective(menu) != 1) {
            return false;
        }
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setMenuId(menuId);
        sysRoleMenuMapper.delete(roleMenu);
        SysMenuPermCode menuPermCode = new SysMenuPermCode();
        menuPermCode.setMenuId(menuId);
        sysMenuPermCodeMapper.delete(menuPermCode);
        return true;
    }

    /**
     * 获取全部菜单列表。
     *
     * @return 全部菜单列表。
     */
    public List<SysMenu> getAllMenuList() {
        Example e = new Example(SysMenu.class);
        e.orderBy("showOrder");
        Example.Criteria c = e.createCriteria();
        c.andIn("menuType", Arrays.asList(SysMenuType.TYPE_MENU, SysMenuType.TYPE_DIRECTORY));
        c.andEqualTo("deletedFlag", GlobalDeletedFlag.NORMAL);
        return sysMenuMapper.selectByExample(e);
    }

    /**
     * 获取指定用户Id的菜单列表。
     *
     * @param userId 用户主键Id。
     * @return 用户关联的菜单列表。
     */
    public List<SysMenu> getMenuListByUserId(Long userId) {
        return sysMenuMapper.getMenuListByUserId(userId);
    }

    /**
     * 判断当前菜单是否存在子菜单。
     *
     * @param menuId 菜单主键Id。
     * @return 存在返回true，否则false。
     */
    public boolean hasChildren(Long menuId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(menuId);
        return this.getCountByFilter(menu) > 0;
    }

    /**
     * 验证菜单对象关联的数据是否都合法。
     *
     * @param sysMenu              当前操作的对象。
     * @param originalSysMenu      原有对象。
     * @param permCodeIdListString 逗号分隔的权限Id列表。
     * @return 验证结果。
     */
    public CallResult verifyRelatedData(SysMenu sysMenu, SysMenu originalSysMenu, String permCodeIdListString) {
        JSONObject jsonObject = null;
        if (this.needToVerify(sysMenu, originalSysMenu, SysMenu::getParentId)) {
            // menu、ui fragment和button类型的menu不能没有parentId
            if (sysMenu.getParentId() == null) {
                if (sysMenu.getMenuType() != SysMenuType.TYPE_DIRECTORY) {
                    return CallResult.error("数据验证失败，当前类型菜单项的上级菜单不能为空！");
                }
            } else {
                String errorMessage = checkErrorOfNonDirectoryMenu(sysMenu);
                if (errorMessage != null) {
                    return CallResult.error(errorMessage);
                }
            }
        }
        if (StringUtils.isNotBlank(permCodeIdListString)) {
            Set<Long> permCodeIdSet = Arrays.stream(
                    permCodeIdListString.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            if (!sysPermCodeService.existAllPrimaryKeys(permCodeIdSet)) {
                return CallResult.error("数据验证失败，存在不合法的权限字，请刷新后重试！");
            }
            jsonObject = new JSONObject();
            jsonObject.put("permCodeIdSet", permCodeIdSet);
        }
        return CallResult.ok(jsonObject);
    }

    private String checkErrorOfNonDirectoryMenu(SysMenu sysMenu) {
        // 判断父节点是否存在
        SysMenu parentSysMenu = getById(sysMenu.getParentId());
        if (parentSysMenu == null) {
            return "数据验证失败，关联的上级菜单并不存在，请刷新后重试！";
        }
        // 逐个判断每种类型的菜单，他的父菜单的合法性，先从目录类型和菜单类型开始
        if (sysMenu.getMenuType() == SysMenuType.TYPE_DIRECTORY
                || sysMenu.getMenuType() == SysMenuType.TYPE_MENU) {
            // 他们的上级只能是目录
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_DIRECTORY) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是目录类型！";
            }
        } else if (sysMenu.getMenuType() == SysMenuType.TYPE_UI_FRAGMENT) {
            // ui fragment的上级只能是menu类型
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_MENU) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是菜单类型和按钮类型！";
            }
        } else if (sysMenu.getMenuType() == SysMenuType.TYPE_BUTTON) {
            // button的上级只能是menu和ui fragment
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_MENU
                    && parentSysMenu.getMenuType() != SysMenuType.TYPE_UI_FRAGMENT) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是菜单类型和UI片段类型！";
            }
        }
        return null;
    }    
}
