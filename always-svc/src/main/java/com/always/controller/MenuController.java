package com.always.controller;

import com.always.entity.Menu;
import com.always.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.75.85:8088"})
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMenus(@RequestParam(required = false) Boolean visible) {
        Map<String, Object> response = new HashMap<>();
        List<Menu> menus;
        
        if (visible != null && visible) {
            menus = menuService.getVisibleMenus();
        } else {
            menus = menuService.getAllMenus();
        }
        
        response.put("menus", menus);
        response.put("count", menus.size());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenuById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return menuService.getMenuById(id)
                .map(menu -> {
                    response.put("menu", menu);
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Menu not found with id: " + id,
                                "timestamp", LocalDateTime.now())));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenu(@RequestBody Map<String, Object> menuData) {
        Map<String, Object> response = new HashMap<>();
        
        Menu menu = new Menu();
        menu.setName((String) menuData.get("name"));
        menu.setPath((String) menuData.get("path"));
        menu.setIcon((String) menuData.get("icon"));
        menu.setDisplayOrder((Integer) menuData.get("displayOrder"));
        menu.setVisible(menuData.get("visible") != null ? (Boolean) menuData.get("visible") : true);
        
        Menu createdMenu = menuService.createMenu(menu);
        
        response.put("message", "메뉴가 생성되었습니다.");
        response.put("menu", createdMenu);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenu(
            @PathVariable Long id,
            @RequestBody Map<String, Object> menuData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Menu menu = new Menu();
            menu.setName((String) menuData.get("name"));
            menu.setPath((String) menuData.get("path"));
            menu.setIcon((String) menuData.get("icon"));
            menu.setDisplayOrder((Integer) menuData.get("displayOrder"));
            menu.setVisible(menuData.get("visible") != null ? (Boolean) menuData.get("visible") : true);
            
            Menu updatedMenu = menuService.updateMenu(id, menu);
            
            response.put("message", "메뉴가 업데이트되었습니다.");
            response.put("menu", updatedMenu);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            menuService.deleteMenu(id);
            response.put("message", "메뉴가 삭제되었습니다.");
            response.put("id", id);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

