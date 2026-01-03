package com.always.service;

import com.always.entity.Menu;
import com.always.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuMapper menuMapper;

    @Autowired
    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<Menu> getAllMenus() {
        return menuMapper.findAllOrderByDisplayOrder();
    }

    public List<Menu> getVisibleMenus() {
        return menuMapper.findVisibleMenusOrderByDisplayOrder();
    }

    public Optional<Menu> getMenuById(Long id) {
        Menu menu = menuMapper.findById(id);
        return Optional.ofNullable(menu);
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        // createdAt, updatedAt 설정
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        
        menuMapper.insert(menu);
        return menu;
    }

    @Transactional
    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuMapper.findById(id);
        if (menu == null) {
            throw new RuntimeException("Menu not found with id: " + id);
        }
        
        menu.setName(menuDetails.getName());
        menu.setPath(menuDetails.getPath());
        menu.setIcon(menuDetails.getIcon());
        menu.setDisplayOrder(menuDetails.getDisplayOrder());
        menu.setVisible(menuDetails.getVisible());
        menu.setUpdatedAt(LocalDateTime.now());
        
        menuMapper.update(menu);
        return menu;
    }

    @Transactional
    public void deleteMenu(Long id) {
        if (!menuMapper.existsById(id)) {
            throw new RuntimeException("Menu not found with id: " + id);
        }
        menuMapper.deleteById(id);
    }
}
